package net.es.oscars.aaa;

import org.testng.annotations.*;

import java.util.List;
import java.util.Properties;
import org.hibernate.*;

import net.es.oscars.database.HibernateUtil;
import net.es.oscars.PropHandler;


/**
 * This class tests methods in AuthorizationDAO.java, which requires a working
 *     Authorization.java and Authorization.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "aaa" })
public class AuthorizationTest {
    private Properties props;
    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    public void setUpClass() {
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.aaa", true);
        this.dbname = "aaa";
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }

  @Test
    public void authorizationQuery() throws AAAException {
        User user = null;
        Resource resource = null;
        Permission permission = null;
        Authorization auth = null;

        this.sf.getCurrentSession().beginTransaction();
        AuthorizationDAO dao = new AuthorizationDAO(this.dbname);
        String userName = this.props.getProperty("superuser");
        String resourceName = this.props.getProperty("resourceName");
        String permissionName = this.props.getProperty("permissionName");

        UserDAO userDAO = new UserDAO(this.dbname);
        user = (User) userDAO.queryByParam("login", userName);
        if (user == null) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw new AAAException("User " + userName + " does not exist");
        }

        ResourceDAO resourceDAO = new ResourceDAO(this.dbname);
        resource = (Resource) resourceDAO.queryByParam("name", resourceName);
        if (resource == null) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw new AAAException("Resource " + resourceName + " does not exist");
        }

        PermissionDAO permissionDAO = new PermissionDAO(this.dbname);
        permission = (Permission)
                permissionDAO.queryByParam("name", permissionName);
        if (permission == null) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw new AAAException("permission " + permissionName + " does not exist");
        }

        auth = (Authorization)
            dao.query(user.getId(), resource.getId(), permission.getId());
        this.sf.getCurrentSession().getTransaction().commit();
        assert auth != null;
    }

  @Test
    public void authorizationList() throws AAAException {
        List<Authorization> auths = null;

        this.sf.getCurrentSession().beginTransaction();
        AuthorizationDAO dao = new AuthorizationDAO(this.dbname);
        try {
            auths = dao.list(null);
        } catch (AAAException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw ex;
        }
        this.sf.getCurrentSession().getTransaction().commit();
        assert !auths.isEmpty();
    }
}
