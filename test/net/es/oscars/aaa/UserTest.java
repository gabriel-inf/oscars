package net.es.oscars.aaa;

import org.testng.annotations.*;

import java.util.List;
import java.util.Properties;
import org.hibernate.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.HibernateUtil;

/**
 * This class tests methods in UserDAO.java via UserManager.java, which
 * requires a working User.java and User.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "aaa", "user" }, dependsOnGroups={ "create" })
public class UserTest {
    private final String PHONE_SECONDARY = "888-888-8888";
    private Properties props;
    private UserManager mgr;
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
    public void userAssociation() throws AAAException {
        User user = null;

        UserManager mgr = new UserManager(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        String ulogin = this.props.getProperty("login");
        user = mgr.query(ulogin);
        if (user == null) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw new AAAException("Unable to query newly created user: " + ulogin);
        }
        String institutionName = user.getInstitution().getName();
        this.sf.getCurrentSession().getTransaction().commit();
        assert institutionName != null;
    }

  @Test
    public void userVerifyLogin() throws AAAException {
        String userName = null;

        UserManager mgr = new UserManager(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        String password = this.props.getProperty("password");
        try {
            userName =
                mgr.verifyLogin(this.props.getProperty("login"),
                                    password);
        } catch (AAAException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw ex;
        }
        this.sf.getCurrentSession().getTransaction().commit();
        assert userName != null;
    }

  @Test
    public void userQuery() throws AAAException {
        User user = null;
        String ulogin = this.props.getProperty("login");

        UserManager mgr = new UserManager(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        user = mgr.query(ulogin);
        if (user == null) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw new AAAException("Unable to query newly created user: " + ulogin);
        }
        String userLogin = user.getLogin();
        this.sf.getCurrentSession().getTransaction().commit();
        assert ulogin.equals(userLogin);
    }

  @Test
      public void userList() {
          UserManager mgr = new UserManager(this.dbname);
          this.sf.getCurrentSession().beginTransaction();
          List<User> users = mgr.list();
          this.sf.getCurrentSession().getTransaction().commit();
          assert !users.isEmpty();
      }

  @Test
      public void userUpdate() throws AAAException {
          UserManager mgr = new UserManager(this.dbname);
          this.sf.getCurrentSession().beginTransaction();
          String ulogin = this.props.getProperty("login");
          User user = mgr.query(ulogin);
          if (user == null) {
              this.sf.getCurrentSession().getTransaction().rollback();
              throw new AAAException("Unable to get user instance in order to update: " + ulogin);
        }
        user.setPhoneSecondary(PHONE_SECONDARY);
        try {
            mgr.update(user, false);
        } catch (AAAException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw ex;
        }
        mgr = new UserManager(this.dbname);
        user = mgr.query(ulogin);
        if (user == null) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw new AAAException("Unable to query newly updated user: " + ulogin);
        }
        this.sf.getCurrentSession().getTransaction().commit();
        assert user.getPhoneSecondary().equals(PHONE_SECONDARY) :
            "Updated secondary phone number, " + user.getPhoneSecondary() +
            " does not equal desired phone number, " + PHONE_SECONDARY;
    }

  @Test
    public void userLoginFromDN() throws AAAException {
        String userName = null;

        UserManager mgr = new UserManager(this.dbname);
        String dn = this.props.getProperty("dn");
        this.sf.getCurrentSession().beginTransaction();
        try {
            userName = mgr.loginFromDN(dn);
        } catch (AAAException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw ex;
        }
        this.sf.getCurrentSession().getTransaction().commit();
        assert userName != null : "No user name corresponds to the DN, " + dn;
    }
}
