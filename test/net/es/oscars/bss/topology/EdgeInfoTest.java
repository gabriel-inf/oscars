package net.es.oscars.bss.topology;

import org.testng.annotations.*;

import java.util.List;
import java.util.Properties;
import org.hibernate.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.HibernateUtil;


/**
 * This class tests access to the edgeInfos table, which requires a working
 *     EdgeInfo.java and EdgeInfo.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
/* @Test(groups={ "bss/topology", "edgeInfo" }, dependsOnGroups={ "create" }) */
@Test(groups={ "broken" })
public class EdgeInfoTest {
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
    public void edgeInfoQuery() {
        this.sf.getCurrentSession().beginTransaction();
        EdgeInfoDAO dao = new EdgeInfoDAO(this.dbname);
        String description = "test suite";
        EdgeInfo edgeInfo = (EdgeInfo)
            dao.queryByParam("description", description);
        this.sf.getCurrentSession().getTransaction().commit();
        assert edgeInfo != null;
    }

  @Test
    public void edgeInfoList() {
        this.sf.getCurrentSession().beginTransaction();
        EdgeInfoDAO dao = new EdgeInfoDAO(this.dbname);
        List<EdgeInfo> edgeInfos = dao.list();
        this.sf.getCurrentSession().getTransaction().commit();
        assert !edgeInfos.isEmpty();
    }
}
