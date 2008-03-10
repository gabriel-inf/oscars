package net.es.oscars.bss.topology;

import org.testng.annotations.*;

import java.util.List;
import org.hibernate.*;

import net.es.oscars.GlobalParams;
import net.es.oscars.database.HibernateUtil;

/**
 * This class tests access to the Layer2Data table, which requires a working
 *     Layer2Data.java and Layer2Data.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "bss", "bss.topology", "layer2Data" },
               dependsOnGroups={ "create" })
public class Layer2DataTest {
    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    protected void setUpClass() {
        this.dbname = GlobalParams.getReservationTestDBName();
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }
        
  @Test
    public void  layer2DataQuery() {
        this.sf.getCurrentSession().beginTransaction();
        Layer2DataDAO dao = new Layer2DataDAO(this.dbname);
        Layer2Data layer2Data = (Layer2Data)
            dao.queryByParam("srcEndpoint", CommonParams.getSrcEndpoint());
        this.sf.getCurrentSession().getTransaction().commit();
        assert layer2Data != null;
    }

  @Test
    public void layer2DataList() {
        this.sf.getCurrentSession().beginTransaction();
        Layer2DataDAO dao = new Layer2DataDAO(this.dbname);
        List<Layer2Data> layer2Data = dao.list();
        this.sf.getCurrentSession().getTransaction().commit();
        assert !layer2Data.isEmpty();
    }
}
