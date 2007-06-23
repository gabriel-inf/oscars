package net.es.oscars.bss.topology;

import org.testng.annotations.*;

import java.util.*;
import org.hibernate.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.bss.BSSException;

/**
 * This class tests methods in PathDAO.java, which requires a working
 *     Path.java and Path.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "notyet", "path" }, dependsOnGroups={ "create" })
public class PathTest {
    private Properties props;
    private SessionFactory sf;
    private String dbname;
    private ArrayList<String> hops;

  @BeforeClass
    protected void setUpClass() {
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.common", true);
        this.hops = new ArrayList<String>();
        this.dbname = "testbss";
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }
        
  @Test
    public void pathCreate() throws BSSException {
        this.sf.getCurrentSession().beginTransaction();
        PathDAO pathDAO = new PathDAO(this.dbname);
        Path path = new Path();
        path.setExplicit(false);

        PathElem pathElem0 = new PathElem();
        pathElem0.setLoose(false);
        pathElem0.setDescription("ingress");
        Ipaddr ipaddr0 = new Ipaddr();
        ipaddr0.setValid(true);
        ipaddr0.setIP(this.props.getProperty("hop0"));
        pathElem0.setIpaddr(ipaddr0);

        PathElem pathElem6 = new PathElem();
        pathElem6.setLoose(false);
        pathElem6.setDescription("egress");
        Ipaddr ipaddr6 = new Ipaddr();
        ipaddr6.setValid(true);
        ipaddr6.setIP(this.props.getProperty("hop6"));
        pathElem6.setIpaddr(ipaddr6);

        path.setPathElem(pathElem0);
        pathDAO.create(path);
        this.sf.getCurrentSession().getTransaction().commit();
        assert path != null;
    }

  @Test(dependsOnMethods={ "pathCreate" })
    public void pathQuery() {
        this.sf.getCurrentSession().beginTransaction();
        PathDAO dao = new PathDAO(this.dbname);
        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);
        Ipaddr ipaddr = (Ipaddr) ipaddrDAO.queryByParam("description",
                                 "test suite");
        Path path = (Path) dao.queryByParam("ipaddrId", ipaddr.getId()); 
        this.sf.getCurrentSession().getTransaction().commit();
        assert path != null;
    }

  @Test(dependsOnMethods={ "pathCreate" })
    public void pathList() {
        this.sf.getCurrentSession().beginTransaction();
        PathDAO dao = new PathDAO(this.dbname);
        List<Path> paths = dao.list();
        this.sf.getCurrentSession().getTransaction().commit();
        assert !paths.isEmpty();
    }

  @Test(dependsOnMethods={ "pathCreate", "pathQuery", "pathList" })
    public void pathRemove() {
        this.sf.getCurrentSession().beginTransaction();
        PathDAO dao = new PathDAO(this.dbname);
        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);
        Ipaddr ipaddr = (Ipaddr) ipaddrDAO.queryByParam("description",
                                 "test suite");
        Path path = dao.queryByParam("ipaddrId", ipaddr.getId());
        dao.remove(path);
        // remove ipaddr's set up for creating test path
        for (String hop: this.hops) {
            ipaddr = (Ipaddr) ipaddrDAO.queryByParam("IP", hop);
            ipaddrDAO.remove(ipaddr);
        }
        this.sf.getCurrentSession().getTransaction().commit();
    }
}
