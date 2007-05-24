package net.es.oscars.bss.topology;

import org.testng.annotations.*;

import java.util.*;
import org.hibernate.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.HibernateUtil;

/**
 * This class tests associations between Router, Interface, and Ipaddr.
 * Note that ipaddr tests must be run first.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "bss/topology" }, dependsOnGroups={ "ipaddr"})
public class AssociationTest {
    private Properties props;
    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    protected void setUpClass() {
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.bss.topology", true);
        this.dbname = "bss";
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }
        
  @Test
    public void routerRemove() {
        this.sf.getCurrentSession().beginTransaction();
        String routerName = this.props.getProperty("routerName");
        RouterDAO routerDAO = new RouterDAO(this.dbname);
        Router router = (Router) routerDAO.queryByParam("name", routerName);
        routerDAO.remove(router);
        this.sf.getCurrentSession().getTransaction().commit();
    }

  @Test(dependsOnMethods={ "routerRemove" })
    public void cascadingDeletedInterface() {
        this.sf.getCurrentSession().beginTransaction();
        String description = this.props.getProperty("xfaceDescription");
        InterfaceDAO xfaceDAO = new InterfaceDAO(this.dbname);
        Interface xface = (Interface)
            xfaceDAO.queryByParam("description", description);
        this.sf.getCurrentSession().getTransaction().commit();
        // if cascading delete works with router testRemove, this will
        // be null
        assert xface == null;
    }

  // done here now that no longer needed
  // note that deletion is not cascaded anymore for ipaddrs
  @Test(dependsOnMethods={ "routerRemove" })
    public void ipaddrRemove() {
        this.sf.getCurrentSession().beginTransaction();
        String description = this.props.getProperty("ipaddrDescription");
        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);
        Ipaddr ipaddr =
            (Ipaddr) ipaddrDAO.queryByParam("description", description);
        ipaddrDAO.remove(ipaddr);
        ipaddr = (Ipaddr) ipaddrDAO.queryByParam("description", description);
        assert ipaddr == null;
        this.sf.getCurrentSession().getTransaction().commit();
    }
}
