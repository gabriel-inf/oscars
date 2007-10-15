package net.es.oscars.bss.topology;

import org.testng.annotations.*;

import java.util.List;
import java.util.Properties;
import org.hibernate.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.HibernateUtil;


/**
 * This class tests access to the Layer3Data table, which requires a working
 *     Layer3Data.java and Layer3Data.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
/* @Test(groups={ "bss/topology", "layer3Data" }, dependsOnGroups={ "create" }) */
@Test(groups={ "broken" })
public class Layer3DataTest {
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
    public void  layer3DataQuery() {
        this.sf.getCurrentSession().beginTransaction();
        Layer3DataDAO dao = new Layer3DataDAO(this.dbname);
        String description = "test suite";
        Layer3Data layer3Data = (Layer3Data)
            dao.queryByParam("description", description);
        this.sf.getCurrentSession().getTransaction().commit();
        assert layer3Data != null;
    }

  @Test
    public void layer3DataList() {
        this.sf.getCurrentSession().beginTransaction();
        Layer3DataDAO dao = new Layer3DataDAO(this.dbname);
        List<Layer3Data> layer3Data = dao.list();
        this.sf.getCurrentSession().getTransaction().commit();
        assert !layer3Data.isEmpty();
    }
}
