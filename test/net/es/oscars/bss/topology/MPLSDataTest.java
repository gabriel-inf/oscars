package net.es.oscars.bss.topology;

import org.testng.annotations.*;

import java.util.List;
import java.util.Properties;
import org.hibernate.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.HibernateUtil;


/**
 * This class tests access to the MPLSData table, which requires a working
 *     MPLSData.java and MPLSData.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
/* @Test(groups={ "bss/topology", "mplsData" }, dependsOnGroups={ "create" }) */
@Test(groups={ "broken" })
public class MPLSDataTest {
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
    public void  mplsDataQuery() {
        this.sf.getCurrentSession().beginTransaction();
        MPLSDataDAO dao = new MPLSDataDAO(this.dbname);
        String description = "test suite";
        MPLSData mplsData = (MPLSData)
            dao.queryByParam("description", description);
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
