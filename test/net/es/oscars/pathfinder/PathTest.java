package net.es.oscars.pathfinder;

import junit.framework.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import org.hibernate.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.Initializer;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.topology.*;

/**
 * This class tests methods in PathDAO.java, which requires a working
 *     Path.java and Path.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class PathTest extends TestCase {
    private Properties props;
    private Session session;
    private PathDAO dao;
    private ArrayList<String> hops;

    public PathTest(String name) {
        super(name);
        Initializer initializer = new Initializer();
        initializer.initDatabase();
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.bss", true);
    }
        
    public void setUp() {
        this.session =
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        this.dao = new PathDAO();
        this.dao.setSession(this.session);
        if (this.hops == null) {
            this.hops = new ArrayList<String>();
        }
        this.session.beginTransaction();
    }

    public void testCreate() {
        ArrayList<Ipaddr> ipaddrs = new ArrayList<Ipaddr>();
        Path path = null;

        String description = this.props.getProperty("pathDescription");
        // need to save these in global variable so can remove in testRemove
        this.hops.add("134.55.209.22");
        this.hops.add("134.55.209.21");
        this.hops.add("134.55.219.10");
        this.hops.add("134.55.217.2");
        this.hops.add("134.55.207.37");
        this.hops.add("134.55.209.54");
        this.hops.add("134.55.209.58");
        this.hops.add("134.55.200.28");
        IpaddrDAO ipaddrDAO = new IpaddrDAO();
        ipaddrDAO.setSession(this.session);
        for (String hop: this.hops) {
            Ipaddr ipaddr = new Ipaddr();
            ipaddr.setIp(hop);
            ipaddr.setDescription(description);
            ipaddrDAO.create(ipaddr);
            ipaddrs.add(ipaddr);
        }
        String ingressRouter = "134.55.75.27";
        String egressRouter = "134.55.75.28";
        try {
            path = this.dao.create(ipaddrs, ingressRouter, egressRouter);
        } catch (BSSException e) {
            fail(e.getMessage());
        }
        this.session.getTransaction().commit();
        Assert.assertNotNull(path);
    }

    public void testQuery() {
        IpaddrDAO ipaddrDAO = new IpaddrDAO();
        ipaddrDAO.setSession(this.session);
        Ipaddr ipaddr = (Ipaddr) ipaddrDAO.queryByParam("description",
                                 this.props.getProperty("pathDescription"));
        Path path = (Path) this.dao.queryByParam("ipaddrId", ipaddr.getId()); 
        this.session.getTransaction().commit();
        Assert.assertNotNull(path);
    }

    public void testList() {
        List<Path> paths = this.dao.list();
        this.session.getTransaction().commit();
        Assert.assertFalse(paths.isEmpty());
    }

    public void testRemove() {
        IpaddrDAO ipaddrDAO = new IpaddrDAO();
        ipaddrDAO.setSession(this.session);
        Ipaddr ipaddr = (Ipaddr) ipaddrDAO.queryByParam("description",
                                 this.props.getProperty("pathDescription"));
        Path path = this.dao.queryByParam("ipaddrId", ipaddr.getId());
        this.dao.remove(path.getId());
        // remove ipaddr's set up for creating test path
        for (String hop: this.hops) {
            ipaddr = (Ipaddr) ipaddrDAO.queryByParam("IP", hop);
            ipaddrDAO.remove(ipaddr);
        }
        this.session.getTransaction().commit();
        // if got to here, OK (for now)
        Assert.assertTrue(true);
    }
}
