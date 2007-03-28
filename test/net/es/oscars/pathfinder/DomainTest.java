package net.es.oscars.pathfinder;

import org.testng.annotations.*;

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
@Test(groups={ "pathfinder" })
public class DomainTest {
    private Properties props;
    private Session session;
    private DomainDAO dao;

  @BeforeClass
    protected void setUpClass() {
        PropHandler propHandler = new PropHandler("test.properties");
        Initializer initializer = new Initializer();
        initializer.initDatabase();
        this.props = propHandler.getPropertyGroup("test.bss", true);
        this.dao = new DomainDAO();
    }
        
  @BeforeMethod
    protected void setUpMethod() {
        this.session = 
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        this.dao.setSession(this.session);
        this.session.beginTransaction();
    }

    public void testQuery() {
        Domain domain = (Domain) this.dao.queryByParam("name",
                                       this.props.getProperty("domainName"));
        this.session.getTransaction().commit();
        assert domain != null;
    }

    public void testList() {
        List<Domain> domains = this.dao.findAll();
        this.session.getTransaction().commit();
        assert !domains.isEmpty();
    }
}
