package net.es.oscars.bss.topology;

import org.testng.annotations.*;

import java.util.List;
import org.hibernate.*;

import net.es.oscars.GlobalParams;
import net.es.oscars.database.HibernateUtil;

/**
 * This class tests access to the InterdomainRoute table, which requires a
 * working InterdomainRoute.java and InterdomainRoute.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "bss.topology", "interdomainRoute" },
               dependsOnGroups={ "create" })
public class InterdomainRouteTest {
    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    protected void setUpClass() {
        this.dbname = GlobalParams.getReservationTestDBName();
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }
        
  @Test
    public void interdomainRouteQuery() {
        this.sf.getCurrentSession().beginTransaction();
        InterdomainRouteDAO dao = new InterdomainRouteDAO(this.dbname);
        InterdomainRoute interdomainRoute = (InterdomainRoute)
            dao.queryByParam("preference",
                             CommonParams.getInterdomainPreference());
        this.sf.getCurrentSession().getTransaction().commit();
        assert interdomainRoute != null;
    }

  @Test
    public void interdomainRouteList() {
        this.sf.getCurrentSession().beginTransaction();
        InterdomainRouteDAO dao = new InterdomainRouteDAO(this.dbname);
        List<InterdomainRoute> interdomainRoutes = dao.list();
        this.sf.getCurrentSession().getTransaction().commit();
        assert !interdomainRoutes.isEmpty();
    }
}
