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
@Test(groups={ "aaa" })
public class InstitutionTest {
    private Properties props;
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
    public void institutionQuery() {
        this.sf.getCurrentSession().beginTransaction();
        InstitutionDAO dao = new InstitutionDAO(this.dbname);
        Institution institution = (Institution)
            dao.queryByParam("name",
                             this.props.getProperty("institutionName"));
        this.sf.getCurrentSession().getTransaction().commit();
        assert institution != null;
    }

  @Test
    public void institutionList() {
        this.sf.getCurrentSession().beginTransaction();
        InstitutionDAO dao = new InstitutionDAO(this.dbname);
        List<Institution> institutions = dao.list();
        this.sf.getCurrentSession().getTransaction().commit();
        assert !institutions.isEmpty();
    }
}
