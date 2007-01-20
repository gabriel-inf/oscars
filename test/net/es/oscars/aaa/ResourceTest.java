package net.es.oscars.aaa;

import junit.framework.*;

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
public class ResourceTest extends TestCase {
    private Properties props;
    private Session session;
    private ResourceDAO dao;

    public ResourceTest(String name) {
        super(name);
        Initializer initializer = new Initializer();
        initializer.initDatabase();
        PropHandler propHandler =
            new PropHandler("/oscars.config/properties/test.properties");
        this.props = propHandler.getPropertyGroup("test.aaa", true);
    }
        
    protected void setUp() {
        this.dao = new ResourceDAO();
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
        Assert.assertNotNull(resource);
    }

    public void testList() {
        List<Resource> resources = this.dao.findAll();
        this.session.getTransaction().commit();
        Assert.assertFalse(resources.isEmpty());
    }
}
