package net.es.oscars.aaa;

import org.testng.annotations.*;

import java.util.List;
import java.util.Properties;
import org.hibernate.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.HibernateUtil;

/**
 * This class tests access to the institutions table, which requires a working
 *     Institution.java and Institution.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "aaa", "institution" }, dependsOnGroups={ "create"})
public class InstitutionTest {
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
    public void institutionQuery() {
        InstitutionDAO institutionDAO = new InstitutionDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        Institution institution =
                (Institution) institutionDAO.queryByParam("name",
                                   this.props.getProperty("institutionName"));
        this.sf.getCurrentSession().getTransaction().commit();
        assert institution != null;
    }

  @Test
    public void institutionList() {
        this.sf.getCurrentSession().beginTransaction();
        InstitutionDAO institutionDAO = new InstitutionDAO(this.dbname);
        List<Institution> institutions = institutionDAO.list();
        this.sf.getCurrentSession().getTransaction().commit();
        assert !institutions.isEmpty();
    }
}
