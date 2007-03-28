package net.es.oscars.aaa;

import org.testng.annotations.*;

import java.util.List;
import java.util.Properties;
import org.hibernate.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.Initializer;
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
    private Session session;
    private InstitutionDAO dao;

  @BeforeClass
    protected void setUpClass() {
        Initializer initializer = new Initializer();
        initializer.initDatabase();
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.aaa", true);
        this.dao = new InstitutionDAO();
    }

  @BeforeMethod
    protected void setUpMethod() {
        this.session =
            HibernateUtil.getSessionFactory("aaa").getCurrentSession();
        this.dao.setSession(this.session);
        this.session.beginTransaction();
    }

    public void testQuery() {
        Institution institution = (Institution)
            this.dao.queryByParam("name",
                             this.props.getProperty("institutionName"));
        this.session.getTransaction().commit();
        assert institution != null;
    }

    public void testList() {
        List<Institution> institutions = this.dao.findAll();
        this.session.getTransaction().commit();
        assert !institutions.isEmpty();
    }
}
