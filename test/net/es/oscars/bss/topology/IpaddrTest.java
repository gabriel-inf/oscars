package net.es.oscars.bss.topology;

import org.testng.annotations.*;

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
@Test(groups={ "bss/topology" })
public class IpaddrTest {
    private Properties props;
    private Session session;
    private IpaddrDAO dao;

  @BeforeClass
    protected void setUpClass() {
        Initializer initializer = new Initializer();
        initializer.initDatabase();
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.bss", true);
        this.dao = new IpaddrDAO();
    }

  @BeforeMethod
    protected void setUpMethod() {
        this.session =
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
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
        assert ipaddr != null;
    }

  @Test(dependsOnMethods={ "testCreate" })
    public void testQuery() {
        String description = this.props.getProperty("ipaddrDescription");
        Ipaddr ipaddr = 
            (Ipaddr) this.dao.queryByParam("description", description);
        this.session.getTransaction().commit();
        assert ipaddr != null;
    }

  @Test(dependsOnMethods={ "testCreate" })
    public void testList() {
        List<Ipaddr> ipaddrs = this.dao.list();
        this.session.getTransaction().commit();
        assert !ipaddrs.isEmpty();
    }

  @Test(dependsOnMethods={ "testCreate" })
    public void testGetIpType() {
        String ipType =
            this.dao.getIpType(this.props.getProperty("routerName"),
                               "ingress");
        this.session.getTransaction().commit();
        assert ipType != null;
    }

  @Test(dependsOnMethods={ "testCreate", "testQuery", "testList", "testGetIpType" })
    public void testCascadingDelete() {
        String description = this.props.getProperty("ipaddrDescription");
        Ipaddr ipaddr =
            (Ipaddr) this.dao.queryByParam("description", description);
        this.session.getTransaction().commit();
        // if cascading delete works with router testRemove, this will
        // be null
        assert ipaddr == null;
    }
}
