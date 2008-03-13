package net.es.oscars.bss.topology;

import org.testng.annotations.*;

import java.util.List;
import org.hibernate.*;

import net.es.oscars.GlobalParams;
import net.es.oscars.database.HibernateUtil;

/**
 * This class tests methods in DomainDAO.java, which requires a working
 *     Domain.java and Domain.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "bss.topology", "domain" }, dependsOnGroups={ "create" })
public class DomainTest {
    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    protected void setUpClass() {
        this.dbname = GlobalParams.getReservationTestDBName();
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }
        
  @Test
    public void domainQuery() {
        this.sf.getCurrentSession().beginTransaction();
        DomainDAO dao = new DomainDAO(this.dbname);
        Domain domain = (Domain) dao.queryByParam("topologyIdent",
                                                  CommonParams.getIdentifier());
        this.sf.getCurrentSession().getTransaction().commit();
        assert domain != null;
    }

  @Test
    public void domainList() {
        this.sf.getCurrentSession().beginTransaction();
        DomainDAO dao = new DomainDAO(this.dbname);
        List<Domain> domains = dao.list();
        this.sf.getCurrentSession().getTransaction().commit();
        assert !domains.isEmpty();
    }
}
