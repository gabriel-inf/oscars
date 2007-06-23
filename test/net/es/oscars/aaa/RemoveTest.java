package net.es.oscars.aaa;

import org.testng.annotations.*;

import java.util.List;
import java.util.Properties;
import org.hibernate.*;

import net.es.oscars.database.HibernateUtil;
import net.es.oscars.PropHandler;


/**
 * This class tests removal of AAA database entries.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "aaa" }, dependsOnGroups={ "institution", "user", "resource", "permission", "authorization" })
public class RemoveTest {
    private Properties props;
    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    public void setUpClass() {
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.aaa", true);
        this.dbname = "testaaa";
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }

  @Test
    public void authorizationRemove() {
        AuthorizationDAO authDAO = new AuthorizationDAO(this.dbname);
        AttributeDAO attrDAO = new AttributeDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        Attribute attr =
                (Attribute) attrDAO.queryByParam("name",
                                     this.props.getProperty("attributeName"));
        // remove an authorization
        // TODO:  change if add more than one authorization
        Authorization authorization =
            (Authorization) authDAO.queryByParam("attrId", attr.getId());
        authDAO.remove(authorization);
        authorization = (Authorization) authDAO.queryByParam("attrId",
                                             attr.getId());
        this.sf.getCurrentSession().getTransaction().commit();
        assert authorization == null;
    }

  @Test
    public void userAttributeRemove() {
        UserAttributeDAO userAttrDAO = new UserAttributeDAO(this.dbname);
        UserDAO userDAO = new UserDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        User user = (User) userDAO.queryByParam("login",
                                             this.props.getProperty("login"));
        // remove a user attribute
        // TODO:  change if add more than one user attribute
        UserAttribute userAttr =
             (UserAttribute) userAttrDAO.queryByParam("userId", user.getId());
        userAttrDAO.remove(userAttr);
        userAttr = (UserAttribute) userAttrDAO.queryByParam("userId",
                                                            user.getId());
        this.sf.getCurrentSession().getTransaction().commit();
        assert userAttr == null;
    }

  @Test(dependsOnMethods={ "authorizationRemove" })
    public void resourceRemove() {
        ResourceDAO resourceDAO = new ResourceDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        // remove a resource
        Resource resource = (Resource) resourceDAO.queryByParam("name",
                                   this.props.getProperty("resourceName"));
        resourceDAO.remove(resource);
        resource = (Resource) resourceDAO.queryByParam("name",
                                   this.props.getProperty("resourceName"));
        this.sf.getCurrentSession().getTransaction().commit();
        assert resource == null;
    }

  @Test(dependsOnMethods={ "authorizationRemove" })
    public void permissionRemove() {
        PermissionDAO permissionDAO = new PermissionDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        // remove a permission
        Permission permission =
                (Permission) permissionDAO.queryByParam("name",
                                   this.props.getProperty("permissionName"));
        permissionDAO.remove(permission);
        permission = (Permission) permissionDAO.queryByParam("name",
                                   this.props.getProperty("permissionName"));
        this.sf.getCurrentSession().getTransaction().commit();
        assert permission == null;
    }

  @Test(dependsOnMethods={ "authorizationRemove", "userAttributeRemove" })
    public void attributeRemove() {
        AttributeDAO attrDAO = new AttributeDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        // remove an attribute
        Attribute attr = (Attribute) attrDAO.queryByParam("name",
                                   this.props.getProperty("attributeName"));
        attrDAO.remove(attr);
        attr = (Attribute) attrDAO.queryByParam("name",
                                   this.props.getProperty("permissionName"));
        this.sf.getCurrentSession().getTransaction().commit();
        assert attr == null;
    }

  @Test(dependsOnMethods={ "userAttributeRemove" })
    public void userRemove() throws AAAException {
        UserManager mgr = new UserManager(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        try {
            mgr.remove(this.props.getProperty("login"));
        } catch (AAAException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw ex;
        }
        this.sf.getCurrentSession().getTransaction().commit();
    }

  @Test(dependsOnMethods={ "userRemove" })
    public void institutionRemove() {
        this.sf.getCurrentSession().beginTransaction();
        InstitutionDAO institutionDAO = new InstitutionDAO(this.dbname);
        // remove one of the institutions
        Institution institution =
                (Institution) institutionDAO.queryByParam("name",
                                   this.props.getProperty("institutionName"));
        institutionDAO.remove(institution);
        institution = (Institution) institutionDAO.queryByParam("name",
                                   this.props.getProperty("institutionName"));
        this.sf.getCurrentSession().getTransaction().commit();
        assert institution == null;
    }
}
