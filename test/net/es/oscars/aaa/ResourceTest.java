package net.es.oscars.aaa;

import org.testng.annotations.*;

import java.util.List;
import java.util.Properties;
import org.hibernate.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.Initializer;
import net.es.oscars.database.HibernateUtil;

/**
 * This class tests access to the resources table, which requires a working
 *     Resource.java and Resource.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "aaa" })
public class ResourceTest {
    private Properties props;
    private Session session;
    private ResourceDAO dao;

  @BeforeClass
    protected void setUpClass() {
        Initializer initializer = new Initializer();
        initializer.initDatabase();
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.aaa", true);
        this.dao = new ResourceDAO();
    }
        
  @BeforeMethod
    protected void setUpMethod() {
        this.session =
            HibernateUtil.getSessionFactory("aaa").getCurrentSession();
        this.dao.setSession(this.session);
        this.session.beginTransaction();
    }

    public void testQuery() {
        String rname = "Users";

        Resource resource = (Resource) this.dao.queryByParam("name",
                                     this.props.getProperty("resourceName"));
        this.session.getTransaction().commit();
        assert resource != null;
    }

    public void testList() {
        List<Resource> resources = this.dao.findAll();
        this.session.getTransaction().commit();
        assert !resources.isEmpty();
    }
}
