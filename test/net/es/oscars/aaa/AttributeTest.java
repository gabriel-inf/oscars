package net.es.oscars.aaa;

import org.testng.annotations.*;

import java.util.List;
import java.util.Properties;
import org.hibernate.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.HibernateUtil;

/**
 * This class tests access to the attributes table, which requires a working
 *     Attribute.java and Attribute.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "aaa", "attribute" }, dependsOnGroups={ "create" })
public class AttributeTest {
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
    public void attributeQuery() {
        AttributeDAO attributeDAO = new AttributeDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        Attribute attribute =
                (Attribute) attributeDAO.queryByParam("name",
                                    this.props.getProperty("attributeName"));
        this.sf.getCurrentSession().getTransaction().commit();
        assert attribute != null;
    }

  @Test
    public void attributeList() {
        AttributeDAO attributeDAO = new AttributeDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        List<Attribute> attrs = attributeDAO.list();
        this.sf.getCurrentSession().getTransaction().commit();
        assert !attrs.isEmpty();
    }
}
