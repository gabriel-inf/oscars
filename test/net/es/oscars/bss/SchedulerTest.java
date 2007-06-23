package net.es.oscars.bss;

import org.testng.annotations.*;

import java.util.List;
import java.util.Properties;
import org.hibernate.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.HibernateUtil;

/**
 * This class tests methods in Scheduler.java called by SOAP, as well as
 * internal methods.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "bss" })
public class SchedulerTest {
    private final int TIME_INTERVAL = 20;  // 20 seconds
    private Properties props;
    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    protected void setUpClass() {
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.common", true);
        this.dbname = dbname;
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }
        
    public void testPendingReservations() throws BSSException {
        List<Reservation> reservations = null;

        this.sf.getCurrentSession().beginTransaction();
        Scheduler scheduler = new Scheduler(this.dbname);
        try {
            reservations = scheduler.pendingReservations(TIME_INTERVAL);
        } catch (BSSException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw ex;
        }
        this.sf.getCurrentSession().getTransaction().commit();
    }

    public void testExpiredReservations() throws BSSException {
        List<Reservation> reservations = null;

        this.sf.getCurrentSession().beginTransaction();
        Scheduler scheduler = new Scheduler(this.dbname);
        try {
            reservations = scheduler.expiredReservations(TIME_INTERVAL);
        } catch (BSSException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw ex;
        }
        this.sf.getCurrentSession().getTransaction().commit();
    }
}
