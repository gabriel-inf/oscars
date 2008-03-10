package net.es.oscars.bss.topology;

import org.testng.annotations.*;

import java.util.List;
import org.hibernate.*;

import net.es.oscars.GlobalParams;
import net.es.oscars.database.HibernateUtil;

/**
 * This class tests access to the MPLSData table, which requires a working
 *     MPLSData.java and MPLSData.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "bss", "bss.topology", "mplsData" }, dependsOnGroups={ "create" })
public class MPLSDataTest {
    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    protected void setUpClass() {
        this.dbname = GlobalParams.getReservationTestDBName();
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }
        
  @Test
    public void  mplsDataQuery() {
        this.sf.getCurrentSession().beginTransaction();
        MPLSDataDAO dao = new MPLSDataDAO(this.dbname);
        MPLSData mplsData = (MPLSData)
            dao.queryByParam("burstLimit", CommonParams.getMPLSBurstLimit());
        this.sf.getCurrentSession().getTransaction().commit();
        assert mplsData != null;
    }

  @Test
    public void mplsDataList() {
        this.sf.getCurrentSession().beginTransaction();
        MPLSDataDAO dao = new MPLSDataDAO(this.dbname);
        List<MPLSData> mplsData = dao.list();
        this.sf.getCurrentSession().getTransaction().commit();
        assert !mplsData.isEmpty();
    }
}
