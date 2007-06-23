package net.es.oscars.aaa;

import org.testng.annotations.*;

import java.util.List;
import java.util.Properties;
import org.hibernate.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.HibernateUtil;

/**
 * This class tests access to the permissions table, which requires a working
 *     Permission.java and Permission.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "aaa", "permission" }, dependsOnGroups={ "create" })
public class PermissionTest {
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
    public void permissionQuery() {
        PermissionDAO permissionDAO = new PermissionDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        Permission permission =
                (Permission) permissionDAO.queryByParam("name",
                                    this.props.getProperty("permissionName"));
        this.sf.getCurrentSession().getTransaction().commit();
        assert permission != null;
    }

  @Test
    public void permissionList() {
        PermissionDAO permissionDAO = new PermissionDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        List<Permission> perms = permissionDAO.list();
        this.sf.getCurrentSession().getTransaction().commit();
        assert !perms.isEmpty();
    }
}
