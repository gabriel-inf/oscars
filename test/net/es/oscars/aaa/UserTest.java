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
@Test(groups={ "aaa" })
public class UserTest {
    private Properties props;
    private UserManager mgr;
    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    protected void setUpClass() {
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.aaa", true);
        this.dbname = "aaa";
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }

  @Test
    public void userCreate() throws AAAException {
        UserManager mgr = new UserManager(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        String ulogin = this.props.getProperty("login");
        String password = this.props.getProperty("password");
        User user = new User();
        user.setCertIssuer(null);
        user.setCertSubject(null);
        user.setLogin(ulogin);
        user.setLastName(this.props.getProperty("lastName"));
        user.setFirstName(this.props.getProperty("firstName"));
        user.setEmailPrimary(this.props.getProperty("emailPrimary"));
        user.setPhonePrimary(this.props.getProperty("phonePrimary"));
        user.setPhoneSecondary(this.props.getProperty("phoneSecondary"));
        user.setPassword(password);
        user.setDescription(this.props.getProperty("description"));
        user.setEmailSecondary(null);
        user.setPhoneSecondary(null);
        user.setStatus("active");
        user.setActivationKey(null);
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

  @Test(dependsOnMethods={ "userCreate" })
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

  @Test(dependsOnMethods={ "userCreate" })
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

  @Test(dependsOnMethods={ "userCreate" })
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

  @Test(dependsOnMethods={ "userCreate" })
      public void userList() {
          UserManager mgr = new UserManager(this.dbname);
          this.sf.getCurrentSession().beginTransaction();
          List<User> users = mgr.list();
          this.sf.getCurrentSession().getTransaction().commit();
          assert !users.isEmpty();
      }

  @Test(dependsOnMethods={ "userCreate" })
      public void userUpdate() throws AAAException {
          UserManager mgr = new UserManager(this.dbname);
          this.sf.getCurrentSession().beginTransaction();
          String ulogin = this.props.getProperty("login");
          String phoneSecondary = this.props.getProperty("phoneSecondary");
          User user = mgr.query(ulogin);
          if (user == null) {
              this.sf.getCurrentSession().getTransaction().rollback();
              throw new AAAException("Unable to get user instance in order to update: " + ulogin);
        }
        user.setPhoneSecondary(this.props.getProperty("phoneSecondary"));
        try {
            mgr.update(user);
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
        assert user.getPhoneSecondary().equals(phoneSecondary) :
            "Updated secondary phone number, " + user.getPhoneSecondary() +
            " does not equal desired phone number, " + phoneSecondary;
    }

  @Test(dependsOnMethods={ "userCreate" })
    public void userLoginFromDN() {
        String userName = null;

        UserManager mgr = new UserManager(this.dbname);
        String dn = this.props.getProperty("dn");
        this.sf.getCurrentSession().beginTransaction();
        userName = mgr.loginFromDN(dn);
        this.sf.getCurrentSession().getTransaction().commit();
        assert userName != null : "No user name corresponds to the DN, " + dn;
    }

  @Test(dependsOnMethods={ "userCreate", "userAssociation", "userVerifyLogin", "userQuery", "userList", "userUpdate", "userLoginFromDN" })
    public void userRemove() throws AAAException {
        UserManager mgr = new UserManager(this.dbname);
        String ulogin = this.props.getProperty("login");
        this.sf.getCurrentSession().beginTransaction();
        try {
            mgr.remove(ulogin);
        } catch (AAAException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw ex;
        }
        this.sf.getCurrentSession().getTransaction().commit();
    }
}
