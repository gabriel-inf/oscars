package net.es.oscars.aaa;

import org.testng.annotations.*;

import java.util.List;
import java.util.Properties;
import org.hibernate.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.HibernateUtil;

/**
 * This class tests access to the resources table, which requires a working
 *     Resource.java and Resource.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "aaa", "resource" }, dependsOnGroups={ "create" })
public class ResourceTest {
    private Properties props;
    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    protected void setUpClass() {
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.aaa", true);
        this.dbname = "testaaa";
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }
        
  @Test
    public void resourceQuery() {
        String rname = "Users";

        ResourceDAO resourceDAO = new ResourceDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        Resource resource = (Resource) resourceDAO.queryByParam("name",
                                     this.props.getProperty("resourceName"));
        this.sf.getCurrentSession().getTransaction().commit();
        assert resource != null;
    }

  @Test
    public void resourceList() {
        ResourceDAO resourceDAO = new ResourceDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        List<Resource> resources = resourceDAO.list();
        this.sf.getCurrentSession().getTransaction().commit();
        assert !resources.isEmpty();
    }
}
