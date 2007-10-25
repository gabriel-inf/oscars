package net.es.oscars.pss;

import org.testng.annotations.*;

import java.util.List;
import java.util.Properties;
import org.hibernate.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.bss.Reservation;
import net.es.oscars.interdomain.InterdomainException;

/**
 * This class tests methods in Scheduler.java called by SOAP, as well as
 * internal methods.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "broken" })
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
        
    public void testPendingReservations()
            throws PSSException, InterdomainException, Exception {
        List<Reservation> reservations = null;

        this.sf.getCurrentSession().beginTransaction();
        Scheduler scheduler = new Scheduler(this.dbname);
        reservations = scheduler.pendingReservations(TIME_INTERVAL);
        this.sf.getCurrentSession().getTransaction().commit();
    }

    public void testExpiredReservations()
            throws PSSException, Exception {
        List<Reservation> reservations = null;

        this.sf.getCurrentSession().beginTransaction();
        Scheduler scheduler = new Scheduler(this.dbname);
        reservations = scheduler.expiredReservations(TIME_INTERVAL);
        this.sf.getCurrentSession().getTransaction().commit();
    }
}
