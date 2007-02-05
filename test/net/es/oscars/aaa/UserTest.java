package net.es.oscars.aaa;

import junit.framework.*;

import java.util.List;
import java.util.Properties;
import org.hibernate.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.Initializer;
import net.es.oscars.database.HibernateUtil;

/**
 * This class tests methods in UserDAO.java via UserManager.java, which
 * requires a working User.java and User.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class UserTest extends TestCase {
    private Properties props;
    private Session session;
    private UserManager mgr;

    public UserTest(String name) {
        super(name);
        Initializer initializer = new Initializer();
        initializer.initDatabase();
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.aaa", true);
    }
        
    public void setUp() {
        this.session =
            HibernateUtil.getSessionFactory("aaa").getCurrentSession();
        this.mgr = new UserManager();
        this.mgr.setSession();
        this.session.beginTransaction();
    }

    public void testCreate() {
        String ulogin = this.props.getProperty("login");
        String password = this.props.getProperty("password");
        User user = new User();
        user.setCertificate(null);
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
            this.mgr.create(user, institutionName);
        } catch (AAAException e) {
            this.session.getTransaction().rollback();
            fail(e.getMessage());
        }
        this.session.getTransaction().commit();

        this.session =
            HibernateUtil.getSessionFactory("aaa").getCurrentSession();
        this.mgr = new UserManager();
        this.mgr.setSession();
        this.session.beginTransaction();
        user = this.mgr.query(ulogin);
        if (user == null) {
            this.session.getTransaction().rollback();
            fail("Unable to query newly created user: " + ulogin);
        }
        this.session.getTransaction().commit();
        Assert.assertNotNull(user);
    }

    public void testAssociation() {
        User user = null;

        String ulogin = this.props.getProperty("login");
        user = this.mgr.query(ulogin);
        if (user == null) {
            this.session.getTransaction().rollback();
            fail("Unable to query newly created user: " + ulogin);
        }
        String institutionName = user.getInstitution().getName();
        this.session.getTransaction().commit();
        Assert.assertNotNull(institutionName);
    }

    public void testVerifyLogin() {
        String userName = null;

        String password = this.props.getProperty("password");
        try {
            userName =
                this.mgr.verifyLogin(this.props.getProperty("login"),
                                    password);
        } catch (AAAException e) {
            this.session.getTransaction().rollback();
            fail(e.getMessage());
        }
        this.session.getTransaction().commit();
        Assert.assertNotNull(userName);
    }

    public void testQuery() {
        User user = null;
        String ulogin = this.props.getProperty("login");

        user = this.mgr.query(ulogin);
        if (user == null) {
            this.session.getTransaction().rollback();
            fail("Unable to query newly created user: " + ulogin);
        }
        String userLogin = user.getLogin();
        this.session.getTransaction().commit();
        Assert.assertEquals(ulogin, userLogin);
    }

    public void testList() {
        List<User> users = this.mgr.list();
        this.session.getTransaction().commit();
        Assert.assertFalse(users.isEmpty());
    }

    public void testUpdate() {
        String ulogin = this.props.getProperty("login");
        String phoneSecondary = this.props.getProperty("phoneSecondary");
        User user = this.mgr.query(ulogin);
        if (user == null) {
            this.session.getTransaction().rollback();
            fail("Unable to get user instance in order to update: " + ulogin);
        }
        user.setPhoneSecondary(this.props.getProperty("phoneSecondary"));
        try {
            this.mgr.update(user, this.props.getProperty("institutionName"),
                             user.getPassword());
        } catch (AAAException e) {
            this.session.getTransaction().rollback();
            fail(e.getMessage());
        }
        this.mgr = new UserManager();
        this.mgr.setSession();
        user = this.mgr.query(ulogin);
        if (user == null) {
            this.session.getTransaction().rollback();
            fail("Unable to query newly updated user: " + ulogin);
        }
        this.session.getTransaction().commit();
        Assert.assertEquals(user.getPhoneSecondary(), phoneSecondary);
    }

    public void testRemove() {
        String ulogin = this.props.getProperty("login");
        try {
            this.mgr.remove(ulogin);
        } catch (AAAException e) {
            this.session.getTransaction().rollback();
            fail(e.getMessage());
        }
        this.session.getTransaction().commit();
        Assert.assertTrue(true);
    }

    public void testLoginFromDN() {
        String userName = null;

        userName = this.mgr.loginFromDN(this.props.getProperty("dn"));
        this.session.getTransaction().commit();
        Assert.assertNotNull(userName);
    }
}
