package net.es.oscars.bss.topology;

import org.testng.annotations.*;

import java.util.List;
import org.hibernate.*;

import net.es.oscars.GlobalParams;
import net.es.oscars.database.HibernateUtil;

/**
 * This class tests access to the edgeInfos table, which requires a working
 *     EdgeInfo.java and EdgeInfo.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "bss", "bss.topology", "edgeInfo" }, dependsOnGroups={ "create" })
public class EdgeInfoTest {
    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    protected void setUpClass() {
        this.dbname = GlobalParams.getReservationTestDBName();
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }
        
  @Test
    public void edgeInfoQuery() {
        this.sf.getCurrentSession().beginTransaction();
        EdgeInfoDAO dao = new EdgeInfoDAO(this.dbname);
        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);
        Ipaddr ipaddr = (Ipaddr)
            ipaddrDAO.queryByParam("IP", CommonParams.getIpAddress());
        EdgeInfo edgeInfo = (EdgeInfo)
            dao.queryByParam("ipaddrId", ipaddr.getId());
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
