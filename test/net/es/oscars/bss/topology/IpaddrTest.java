package net.es.oscars.bss.topology;

import org.testng.annotations.*;

import java.util.List;
import java.util.Properties;
import org.hibernate.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.HibernateUtil;

/**
 * This class tests access to the ipaddrs table, which requires a working
 *     Ipaddr.java and Ipaddr.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "bss/topology", "ipaddr" }, dependsOnGroups={ "interface" })
public class IpaddrTest {
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

    public void ipaddrCreate() {
        this.sf.getCurrentSession().beginTransaction();
        IpaddrDAO dao = new IpaddrDAO(this.dbname);
        Ipaddr ipaddr = new Ipaddr();
        ipaddr.setValid(true);
        ipaddr.setIP(this.props.getProperty("routerIP"));
        ipaddr.setDescription(this.props.getProperty("ipaddrDescription"));
        InterfaceDAO xfaceDAO = new InterfaceDAO(this.dbname);
        String xfaceDescription = this.props.getProperty("xfaceDescription");
        Interface xface = (Interface)
            xfaceDAO.queryByParam("description", xfaceDescription);
        ipaddr.setInterface(xface);
        dao.create(ipaddr);
        this.sf.getCurrentSession().getTransaction().commit();
        assert ipaddr != null;
    }

  @Test(dependsOnMethods={ "ipaddrCreate" })
    public void ipaddrQuery() {
        this.sf.getCurrentSession().beginTransaction();
        IpaddrDAO dao = new IpaddrDAO(this.dbname);
        String description = this.props.getProperty("ipaddrDescription");
        Ipaddr ipaddr = 
            (Ipaddr) dao.queryByParam("description", description);
        this.sf.getCurrentSession().getTransaction().commit();
        assert ipaddr != null;
    }

  @Test(dependsOnMethods={ "ipaddrCreate" })
    public void ipaddrList() {
        this.sf.getCurrentSession().beginTransaction();
        IpaddrDAO dao = new IpaddrDAO(this.dbname);
        List<Ipaddr> ipaddrs = dao.list();
        this.sf.getCurrentSession().getTransaction().commit();
        assert !ipaddrs.isEmpty();
    }

  @Test(dependsOnMethods={ "ipaddrCreate" })
    public void ipaddrGetIpType() {
        this.sf.getCurrentSession().beginTransaction();
        IpaddrDAO dao = new IpaddrDAO(this.dbname);
        String ipType =
            dao.getIpType(this.props.getProperty("routerName"),
                          this.props.getProperty("ipaddrDescription"));
        this.sf.getCurrentSession().getTransaction().commit();
        assert ipType != null;
    }
}
