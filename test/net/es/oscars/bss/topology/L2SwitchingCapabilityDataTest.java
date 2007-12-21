package net.es.oscars.bss.topology;

import org.testng.annotations.*;

import java.util.List;
import org.hibernate.*;

import net.es.oscars.GlobalParams;
import net.es.oscars.database.HibernateUtil;


/**
 * This class tests access to the L2SwitchingCapabilityData table, which
 *     requires a working L2SwitchingCapabilityData.java and
 *     L2SwitchingCapabilityData.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "bss.topology", "l2SwitchData" }, dependsOnGroups={ "create" })
public class L2SwitchingCapabilityDataTest {
    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    protected void setUpClass() {
        this.dbname = GlobalParams.getReservationTestDBName();
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }
        
  @Test
    public void  l2SwitchingDataQuery() {
        this.sf.getCurrentSession().beginTransaction();
        L2SwitchingCapabilityDataDAO dao =
            new L2SwitchingCapabilityDataDAO(this.dbname);
        LinkDAO linkDAO = new LinkDAO(this.dbname);
        Link link = linkDAO.queryByParam("topologyIdent", 
                                         CommonParams.getIdentifier());
        L2SwitchingCapabilityData l2SwitchingData =
            (L2SwitchingCapabilityData)
            dao.queryByParam("linkId", link.getId());
        this.sf.getCurrentSession().getTransaction().commit();
        assert l2SwitchingData != null;
    }

  @Test
    public void l2SwitchingDataList() {
        this.sf.getCurrentSession().beginTransaction();
        L2SwitchingCapabilityDataDAO dao =
            new L2SwitchingCapabilityDataDAO(this.dbname);
        List<L2SwitchingCapabilityData> l2SwitchingData = dao.list();
        this.sf.getCurrentSession().getTransaction().commit();
        assert !l2SwitchingData.isEmpty();
    }
}
