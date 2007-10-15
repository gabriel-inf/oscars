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
@Test(groups={ "bss/topology", "ipaddr" }, dependsOnGroups={ "create" })
public class IpaddrTest {
    private Properties props;
    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    protected void setUpClass() {
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.common", true);
        this.dbname = "testbss";
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }

  @Test
    public void ipaddrQuery() {
        this.sf.getCurrentSession().beginTransaction();
        IpaddrDAO dao = new IpaddrDAO(this.dbname);
        String description = "test suite";
        Ipaddr ipaddr = 
            (Ipaddr) dao.queryByParam("description", description);
        this.sf.getCurrentSession().getTransaction().commit();
        assert ipaddr != null;
    }

  @Test
    public void ipaddrList() {
        this.sf.getCurrentSession().beginTransaction();
        IpaddrDAO dao = new IpaddrDAO(this.dbname);
        List<Ipaddr> ipaddrs = dao.list();
        this.sf.getCurrentSession().getTransaction().commit();
        assert !ipaddrs.isEmpty();
    }

}
