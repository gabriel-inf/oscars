package net.es.oscars.aaa;

import org.testng.annotations.*;

import java.util.List;
import java.util.Properties;
import org.hibernate.*;

import net.es.oscars.database.HibernateUtil;
import net.es.oscars.PropHandler;


/**
 * This class tests creation of AAA database objects.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "aaa", "create" })
public class CreateTest {
    private final String FIRST_NAME = "test";
    private final String LAST_NAME = "suite";
    private final String EMAIL_PRIMARY = "user@yourdomain.net";
    private final String PHONE_PRIMARY = "777-777-7777";
    private final String PHONE_SECONDARY = "888-888-8888";
    private final String STATUS = "active";
    private final String DESCRIPTION = "test user";

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
    public void attributeCreate() {
        Attribute attribute = new Attribute();
        String attrName = this.props.getProperty("attributeName");
        attribute.setName(attrName);
        AttributeDAO attrDAO = new AttributeDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        attrDAO.create(attribute);
        this.sf.getCurrentSession().getTransaction().commit();
        assert attribute.getId() != null;
        assert attribute.getName() != null;
    }

  @Test
    public void permissionCreate() {
        Permission permission = new Permission();
        permission.setName(this.props.getProperty("permissionName"));
        permission.setDescription("test permission");
        PermissionDAO permissionDAO = new PermissionDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        permissionDAO.create(permission);
        this.sf.getCurrentSession().getTransaction().commit();
        assert permission.getId() != null;
        assert permission.getName() != null;
    }

  @Test
    public void resourceCreate() {
        Resource resource = new Resource();
        resource.setName(this.props.getProperty("resourceName"));
        resource.setDescription("test resource");
        ResourceDAO resourceDAO = new ResourceDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        resourceDAO.create(resource);
        this.sf.getCurrentSession().getTransaction().commit();
        assert resource.getId() != null;
        assert resource.getName() != null;
    }

  @Test
    public void institutionCreate() {
        Institution institution = new Institution();
        institution.setName(this.props.getProperty("institutionName"));
        InstitutionDAO institutionDAO = new InstitutionDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        institutionDAO.create(institution);
        this.sf.getCurrentSession().getTransaction().commit();
        assert institution.getId() != null;
        assert institution.getName() != null;
    }

  @Test(dependsOnMethods={ "institutionCreate" })
    public void userCreate() throws AAAException {
        UserManager mgr = new UserManager(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        String ulogin = this.props.getProperty("login");
        String password = this.props.getProperty("password");
        User user = new User();
        user.setCertIssuer(null);
        user.setCertSubject(this.props.getProperty("dn"));
        user.setLogin(ulogin);
        user.setLastName(LAST_NAME);
        user.setFirstName(FIRST_NAME);
        user.setEmailPrimary(EMAIL_PRIMARY);
        user.setPhonePrimary(PHONE_PRIMARY);
        user.setPhoneSecondary(PHONE_SECONDARY);
        user.setPassword(password);
        user.setDescription(DESCRIPTION);
        user.setStatus(STATUS);
        String institutionName = this.props.getProperty("institutionName");
        try {
            mgr.create(user, institutionName);
        } catch (AAAException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw ex;
        }
        this.sf.getCurrentSession().getTransaction().commit();

        mgr = new UserManager(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        user = mgr.query(ulogin);
        if (user == null) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw new AAAException("Unable to query newly created user: " + ulogin);
        }
        this.sf.getCurrentSession().getTransaction().commit();
        assert user != null;
    }

  @Test(dependsOnMethods={ "attributeCreate", "userCreate" })
    public void userAttributeCreate() {
        UserDAO userDAO = new UserDAO(this.dbname);
        AttributeDAO attrDAO = new AttributeDAO(this.dbname);
        UserAttributeDAO userAttrDAO = new UserAttributeDAO(this.dbname);
        String attrName = this.props.getProperty("attributeName");
        String login = this.props.getProperty("login");
        this.sf.getCurrentSession().beginTransaction();
        Attribute attr = (Attribute) attrDAO.queryByParam("name", attrName);
        User user = (User) userDAO.queryByParam("login", login);
        UserAttribute userAttr = new UserAttribute();
        userAttr.setUserId(user.getId());
        userAttr.setAttributeId(attr.getId());
        userAttrDAO.create(userAttr);
        this.sf.getCurrentSession().getTransaction().commit();
        assert userAttr.getId() != null;
    }

  @Test(dependsOnMethods={ "attributeCreate", "permissionCreate",
                           "resourceCreate" })
    public void authorizationCreate() {
        AuthorizationDAO authDAO = new AuthorizationDAO(this.dbname);
        AttributeDAO attrDAO = new AttributeDAO(this.dbname);
        ResourceDAO resourceDAO = new ResourceDAO(this.dbname);
        PermissionDAO permissionDAO = new PermissionDAO(this.dbname);
        String attrName = this.props.getProperty("attributeName");
        String resourceName = this.props.getProperty("resourceName");
        String permissionName = this.props.getProperty("permissionName");
        Authorization auth = new Authorization();
        this.sf.getCurrentSession().beginTransaction();
        Attribute attr = (Attribute) attrDAO.queryByParam("name", attrName);
        Permission permission = (Permission)
                permissionDAO.queryByParam("name", permissionName);
        Resource resource = (Resource) resourceDAO.queryByParam("name", resourceName);
        auth.setAttrId(attr.getId());
        auth.setResourceId(resource.getId());
        auth.setPermissionId(permission.getId());
        authDAO.create(auth);
        this.sf.getCurrentSession().getTransaction().commit();
        assert auth.getId() != null;
    }
}
