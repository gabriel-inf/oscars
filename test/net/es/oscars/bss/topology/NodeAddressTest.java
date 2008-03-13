package net.es.oscars.bss.topology;

import org.testng.annotations.*;

import java.util.List;
import java.util.Properties;
import org.hibernate.*;

import net.es.oscars.GlobalParams;
import net.es.oscars.database.HibernateUtil;

/**
 * This class tests access to the nodeAddresses table, which requires a working
 *     NodeAddress.java and NodeAddress.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "bss.topology", "nodeAddress" },
               dependsOnGroups={ "create" })
public class NodeAddressTest {
    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    protected void setUpClass() {
        this.dbname = GlobalParams.getReservationTestDBName();
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }
        
  @Test
    public void nodeAddressQuery() {
        this.sf.getCurrentSession().beginTransaction();
        NodeAddressDAO dao = new NodeAddressDAO(this.dbname);
        NodeAddress nodeAddress = (NodeAddress)
            dao.queryByParam("address", CommonParams.getIdentifier());
        this.sf.getCurrentSession().getTransaction().commit();
        assert nodeAddress != null;
    }

  @Test
    public void nodeAddressList() {
        this.sf.getCurrentSession().beginTransaction();
        NodeAddressDAO dao = new NodeAddressDAO(this.dbname);
        List<NodeAddress> nodeAddresses = dao.list();
        this.sf.getCurrentSession().getTransaction().commit();
        assert !nodeAddresses.isEmpty();
    }
}
