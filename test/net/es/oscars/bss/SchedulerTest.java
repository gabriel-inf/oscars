package net.es.oscars.bss;

import org.testng.annotations.*;
import static org.testng.AssertJUnit.*;

import java.util.List;
import java.util.Properties;
import org.hibernate.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.Initializer;
import net.es.oscars.database.HibernateUtil;

/**
 * This class tests methods in Scheduler.java called by SOAP, as well as
 * internal methods.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "bss" })
public class SchedulerTest {
    private Properties props;
    private Session session;

  @BeforeClass
    protected void setUpClass() {
        Initializer initializer = new Initializer();
        initializer.initDatabase();
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.bss", true);
    }
        
  @BeforeMethod
    protected void setUpMethod() {
        this.session = 
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        this.session.beginTransaction();
    }

    public void testPendingReservations() {
        List<Reservation> reservations = null;

        Integer timeInterval = Integer.valueOf(this.props.getProperty("timeInterval"));
        Scheduler scheduler = new Scheduler();
        try {
            reservations = scheduler.pendingReservations(timeInterval);
        } catch (BSSException e) {
            this.session.getTransaction().rollback();
            fail("SchedulerTest.pending: " + e.getMessage());
        }
        this.session.getTransaction().commit();
    }

    public void testExpiredReservations() {
        List<Reservation> reservations = null;

        Integer timeInterval =
            Integer.valueOf(this.props.getProperty("timeInterval"));
        Scheduler scheduler = new Scheduler();
        try {
            reservations = scheduler.expiredReservations(timeInterval);
        } catch (BSSException e) {
            this.session.getTransaction().rollback();
            fail("SchedulerTest.expired: " + e.getMessage());
        }
        this.session.getTransaction().commit();
    }
}
