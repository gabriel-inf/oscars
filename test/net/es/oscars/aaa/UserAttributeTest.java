package net.es.oscars.aaa;

import org.testng.annotations.*;

import java.util.*;
import org.hibernate.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.HibernateUtil;

/**
 * This class tests methods accessing the userAttributes table, which requires
 * a working UserAttribute.java and UserAttribute.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "aaa", "userAttribute" }, dependsOnGroups={ "create" })
public class UserAttributeTest {
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
    public void getAttributesByUser() {
        UserDAO userDAO = new UserDAO(this.dbname);
        UserAttributeDAO userAttrDAO = new UserAttributeDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        User user =
                (User) userDAO.queryByParam("login",
                                            this.props.getProperty("login"));
        List<UserAttribute> userAttrs =
            userAttrDAO.getAttributesByUser(user.getId());
        this.sf.getCurrentSession().getTransaction().commit();
        assert !userAttrs.isEmpty();
    }

  @Test
    public void getUsersByAttribute() throws AAAException {
        AttributeDAO attributeDAO = new AttributeDAO(this.dbname);
        UserAttributeDAO userAttrDAO = new UserAttributeDAO(this.dbname);
        List<User> users = new ArrayList<User>();
        this.sf.getCurrentSession().beginTransaction();
        Attribute attribute =
                (Attribute) attributeDAO.queryByParam("name",
                                    this.props.getProperty("attributeName"));
        try {
            users = userAttrDAO.getUsersByAttribute(attribute.getName());
        } catch (AAAException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw ex;
        }
        this.sf.getCurrentSession().getTransaction().commit();
        assert !users.isEmpty();
    }
}
