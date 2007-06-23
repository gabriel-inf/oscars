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
@Test(groups={ "aaa", "authorization" }, dependsOnGroups={ "create" })
public class AuthorizationTest {
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
    public void authorizationQuery() throws AAAException {
        Attribute attr = null;
        Resource resource = null;
        Permission permission = null;

        AuthorizationDAO authDAO = new AuthorizationDAO(this.dbname);
        String attrName = this.props.getProperty("attributeName");
        String resourceName = this.props.getProperty("resourceName");
        String permissionName = this.props.getProperty("permissionName");

        AttributeDAO attrDAO = new AttributeDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        attr = (Attribute) attrDAO.queryByParam("name", attrName);
        if (attr == null) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw new AAAException("Attribute " + attrName + " does not exist");
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

        List<Authorization> auths =
            authDAO.query(attr.getId(), resource.getId(), permission.getId());
        this.sf.getCurrentSession().getTransaction().commit();
        assert !auths.isEmpty();
    }

  @Test
    public void authorizationList() throws AAAException {

        AuthorizationDAO authDAO = new AuthorizationDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        List<Authorization> auths = authDAO.list();
        this.sf.getCurrentSession().getTransaction().commit();
        assert !auths.isEmpty();
    }
}
