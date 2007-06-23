package net.es.oscars.bss.topology;

import org.testng.annotations.*;

import java.util.List;
import java.util.Properties;
import org.hibernate.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.HibernateUtil;


/**
 * This class tests access to the ports table, which requires a working
 *     Port.java and Port.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "bss/topology", "port" }, dependsOnGroups={ "create" })
public class PortTest {
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
    public void portQuery() {
        this.sf.getCurrentSession().beginTransaction();
        PortDAO dao = new PortDAO(this.dbname);
        String description = "test suite";
        Port port = (Port)
            dao.queryByParam("description", description);
        this.sf.getCurrentSession().getTransaction().commit();
        assert port != null;
    }

  @Test
    public void portList() {
        this.sf.getCurrentSession().beginTransaction();
        PortDAO dao = new PortDAO(this.dbname);
        List<Port> ports = dao.list();
        this.sf.getCurrentSession().getTransaction().commit();
        assert !ports.isEmpty();
    }
}
