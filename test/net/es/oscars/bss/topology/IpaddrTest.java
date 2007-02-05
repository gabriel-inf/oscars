package net.es.oscars.bss.topology;

import junit.framework.*;

import java.util.List;
import java.util.Properties;
import org.hibernate.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.Initializer;
import net.es.oscars.database.HibernateUtil;

/**
 * This class tests access to the ipaddrs table, which requires a working
 *     Ipaddr.java and Ipaddr.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class IpaddrTest extends TestCase {
    private Properties props;
    private Session session;
    private IpaddrDAO dao;

    public IpaddrTest(String name) {
        super(name);
        Initializer initializer = new Initializer();
        initializer.initDatabase();
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.bss", true);
    }

    public void setUp() {
        this.session =
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        this.dao = new IpaddrDAO();
        this.dao.setSession(this.session);
        this.session.beginTransaction();
    }
        
    public void testCreate() {
        Ipaddr ipaddr = new Ipaddr();
        ipaddr.setIp(this.props.getProperty("ipaddrIP"));
        ipaddr.setDescription(this.props.getProperty("ipaddrDescription"));

        InterfaceDAO xfaceDAO = new InterfaceDAO();
        xfaceDAO.setSession(this.session);
        String xfaceDescription = this.props.getProperty("xfaceDescription");
        Interface xface = (Interface)
            xfaceDAO.queryByParam("description", xfaceDescription);
        ipaddr.setInterface(xface);

        this.dao.create(ipaddr);
        this.session.getTransaction().commit();
        Assert.assertNotNull(ipaddr);
    }

    public void testQuery() {
        String description = this.props.getProperty("ipaddrDescription");
        Ipaddr ipaddr = 
            (Ipaddr) this.dao.queryByParam("description", description);
        this.session.getTransaction().commit();
        Assert.assertNotNull(ipaddr);
    }

    public void testList() {
        List<Ipaddr> ipaddrs = this.dao.list();
        this.session.getTransaction().commit();
        Assert.assertFalse(ipaddrs.isEmpty());
    }

    public void testGetIpType() {
        String ipType =
            this.dao.getIpType(this.props.getProperty("routerName"),
                               "ingress");
        this.session.getTransaction().commit();
        Assert.assertNotNull(ipType);
    }

    public void testCascadingDelete() {
        String description = this.props.getProperty("ipaddrDescription");
        Ipaddr ipaddr =
            (Ipaddr) this.dao.queryByParam("description", description);
        this.session.getTransaction().commit();
        // if cascading delete works with router testRemove, this will
        // be null
        Assert.assertNull(ipaddr);
    }
}
