package net.es.oscars.aaa;

import junit.framework.*;

import java.util.List;
import java.util.Properties;
import org.hibernate.*;

import net.es.oscars.database.Initializer;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.PropHandler;


/**
 * This class tests methods in AuthorizationDAO.java, which requires a working
 *     Authorization.java and Authorization.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class AuthorizationTest extends TestCase {
    private Properties props;
    private AuthorizationDAO dao;
    private Session session;

    public AuthorizationTest(String name) {
        super(name);
        Initializer initializer = new Initializer();
        initializer.initDatabase();
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.aaa", true);
    }
        
    public void setUp() {
        this.dao = new AuthorizationDAO();
        this.session =
            HibernateUtil.getSessionFactory("aaa").getCurrentSession();
        this.dao.setSession(this.session);
        this.session.beginTransaction();
    }

    public void testQuery() {
        User user = null;
        Resource resource = null;
        Permission permission = null;
        Authorization auth = null;

        String userName = this.props.getProperty("superuser");
        String resourceName = this.props.getProperty("resourceName");
        String permissionName = this.props.getProperty("permissionName");

        UserDAO userDAO = new UserDAO();
        userDAO.setSession(this.session);
        user = (User) userDAO.queryByParam("login", userName);
        if (user == null) {
            this.session.getTransaction().rollback();
            fail("User " + userName + " does not exist");
        }

        ResourceDAO resourceDAO = new ResourceDAO();
        resourceDAO.setSession(this.session);
        resource = (Resource) resourceDAO.queryByParam("name", resourceName);
        if (resource == null) {
            this.session.getTransaction().rollback();
            fail("Resource " + resourceName + " does not exist");
        }

        PermissionDAO permissionDAO = new PermissionDAO();
        permissionDAO.setSession(this.session);
        permission = (Permission)
                permissionDAO.queryByParam("name", permissionName);
        if (permission == null) {
            this.session.getTransaction().rollback();
            fail("permission " + permissionName + " does not exist");
        }

        auth = (Authorization)
            this.dao.query(user.getId(), resource.getId(), permission.getId());
        this.session.getTransaction().commit();
        Assert.assertNotNull(auth);
    }

    public void testList() {
        List<Authorization> auths = null;

        try {
            auths = this.dao.list(null);
        } catch (AAAException ex) {
            this.session.getTransaction().rollback();
            fail("AuthorizationTest.testList: " + ex.getMessage());
        }
        this.session.getTransaction().commit();
        Assert.assertFalse(auths.isEmpty());
    }
}
