package net.es.oscars.pss;

import org.testng.annotations.*;
import org.testng.Assert;

import java.util.List;
import java.util.Properties;
import org.hibernate.*;

import net.es.oscars.PropHandler;
import net.es.oscars.GlobalParams;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.CommonReservation;
import net.es.oscars.bss.ReservationDAO;
import net.es.oscars.interdomain.InterdomainException;
import net.es.oscars.pss.cisco.LSP;
import net.es.oscars.pss.jnx.JnxLSP;
import net.es.oscars.notify.EmailObserver;

/**
 * This class tests methods in Scheduler.java called by SOAP, as well as
 * internal methods.  Note that it is a member of the bss group, and
 * depends on prior reservation setup.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "bss" }, dependsOnGroups = { "reservationManager" })
public class SchedulerTest {
    private final int TIME_INTERVAL = 20;  // 20 seconds
    private Properties props;
    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    protected void setUpClass() {
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.common", true);
        this.dbname = GlobalParams.getReservationTestDBName();
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
        // make sure routers aren't configured by tests
        VendorPSSFactory.setConfigurable("0");
        JnxLSP.setConfigurable("0");
        LSP.setConfigurable("0");
        // make sure no notification is sent
        EmailObserver.setNotification("0");
    }
        
  @Test
    public void testPendingReservations()
            throws PSSException, InterdomainException, Exception {
        List<Reservation> reservations = null;

        this.sf.getCurrentSession().beginTransaction();
        Scheduler scheduler = new Scheduler(this.dbname);
        ReservationDAO dao = new ReservationDAO(this.dbname);
        reservations = scheduler.pendingReservations(TIME_INTERVAL);
        Long seconds = System.currentTimeMillis()/1000;
        String layer2Description =
                CommonReservation.getScheduledLayer2Description();
        String layer3Description =
                CommonReservation.getScheduledLayer3Description();
        for (Reservation resv: reservations) {
            if (resv.getStatus().equals("FAILED")) {
                this.sf.getCurrentSession().getTransaction().rollback();
                Assert.fail(resv.getDescription() + " failed");
            // make sure these reservations get expired in next test
            } else if (resv.getDescription().equals(layer2Description)) {
                resv.setEndTime(seconds);
                dao.update(resv);
            } else if (resv.getDescription().equals(layer3Description)) {
                resv.setEndTime(seconds);
                dao.update(resv);
            }
        }
        this.sf.getCurrentSession().getTransaction().commit();
    }

  @Test(dependsOnMethods={ "testPendingReservations" })
    public void testExpiredReservations()
            throws PSSException, Exception {
        List<Reservation> reservations = null;

        this.sf.getCurrentSession().beginTransaction();
        Scheduler scheduler = new Scheduler(this.dbname);
        reservations = scheduler.expiredReservations(0);
        for (Reservation resv: reservations) {
            if (resv.getStatus().equals("FAILED")) {
                this.sf.getCurrentSession().getTransaction().rollback();
                Assert.fail(resv.getDescription() + " failed");
            }
        }
        this.sf.getCurrentSession().getTransaction().commit();
    }
}
