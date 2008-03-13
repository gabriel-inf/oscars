package net.es.oscars.bss.topology;

import org.testng.annotations.*;

import java.util.List;
import org.hibernate.*;

import net.es.oscars.GlobalParams;
import net.es.oscars.database.HibernateUtil;

/**
 * This class tests access to the RouteElem table, which requires a working
 *     RouteELem.java and RouteELem.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "bss.topology", "routeElem" }, dependsOnGroups={ "create" })
public class RouteElemTest {
    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    protected void setUpClass() {
        this.dbname = GlobalParams.getReservationTestDBName();
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }
        
  @Test
    public void routeElemQuery() {
        this.sf.getCurrentSession().beginTransaction();
        RouteElemDAO dao = new RouteElemDAO(this.dbname);
        RouteElem routeElem = (RouteElem)
            dao.queryByParam("description", CommonParams.getIdentifier());
        this.sf.getCurrentSession().getTransaction().commit();
        assert routeElem != null;
    }

  @Test
    public void routeElemList() {
        this.sf.getCurrentSession().beginTransaction();
        RouteElemDAO dao = new RouteElemDAO(this.dbname);
        List<RouteElem> routeElems = dao.list();
        this.sf.getCurrentSession().getTransaction().commit();
        assert !routeElems.isEmpty();
    }
}
