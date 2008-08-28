package net.es.oscars.aaa;

import org.testng.annotations.*;

import java.util.List;
import java.util.Properties;
import org.hibernate.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.HibernateUtil;

/**
 * This class tests access to the constraints table, which requires a working
 *     Constraint.java and Constraint.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "aaa", "constraint" }, dependsOnGroups={ "create" })
public class ConstraintTest {
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
    public void constraintQuery() {
        ConstraintDAO constraintDAO = new ConstraintDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        Constraint permission =
                (Constraint) constraintDAO.queryByParam("name",
                                    this.props.getProperty("constraintName"));
        this.sf.getCurrentSession().getTransaction().commit();
        assert permission != null;
    }

  @Test
    public void constraintList() {
        ConstraintDAO constraintDAO = new ConstraintDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        List<Constraint> constraints = constraintDAO.list();
        this.sf.getCurrentSession().getTransaction().commit();
        assert !constraints.isEmpty();
    }
}
