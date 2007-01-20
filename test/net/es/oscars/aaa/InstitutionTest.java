package net.es.oscars.aaa;

import junit.framework.*;

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
public class InstitutionTest extends TestCase {
    private Properties props;
    private Session session;
    private InstitutionDAO dao;

    public InstitutionTest(String name) {
        super(name);
        Initializer initializer = new Initializer();
        initializer.initDatabase();
        PropHandler propHandler =
            new PropHandler("/oscars.config/properties/test.properties");
        this.props = propHandler.getPropertyGroup("test.aaa", true);
    }
        
    public void setUp() {
        this.session =
            HibernateUtil.getSessionFactory("aaa").getCurrentSession();
        this.dao = new InstitutionDAO();
        this.dao.setSession(this.session);
        this.session.beginTransaction();
    }

    public void testQuery() {
        Institution institution = (Institution)
            this.dao.queryByParam("name",
                             this.props.getProperty("institutionName"));
        this.session.getTransaction().commit();
        Assert.assertNotNull(institution);
    }

    public void testList() {
        List<Institution> institutions = this.dao.findAll();
        this.session.getTransaction().commit();
        Assert.assertFalse(institutions.isEmpty());
    }
}
