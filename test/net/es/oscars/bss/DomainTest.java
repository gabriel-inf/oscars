package net.es.oscars.bss;

import junit.framework.*;

import java.util.List;
import java.util.Properties;
import org.hibernate.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.Initializer;
import net.es.oscars.database.HibernateUtil;

/**
 * This class tests methods in DomainDAO.java, which requires a working
 *     Domain.java and Domain.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class DomainTest extends TestCase {
    private Properties props;
    private Session session;
    private DomainDAO dao;

    public DomainTest(String name) {
        super(name);
        PropHandler propHandler = new PropHandler("test.properties");
        Initializer initializer = new Initializer();
        initializer.initDatabase();
        this.props = propHandler.getPropertyGroup("test.bss", true);
    }
        
    public void setUp() {
        this.session = 
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        this.dao = new DomainDAO();
        this.dao.setSession(this.session);
        this.session.beginTransaction();
    }

    public void testQuery() {
        Domain domain = (Domain) this.dao.queryByParam("name",
                                       this.props.getProperty("domainName"));
        this.session.getTransaction().commit();
        Assert.assertNotNull(domain);
    }

    public void testList() {
        List<Domain> domains = this.dao.findAll();
        this.session.getTransaction().commit();
        Assert.assertFalse(domains.isEmpty());
    }
}
