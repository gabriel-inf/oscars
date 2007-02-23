package net.es.oscars.bss;

import junit.framework.*;

import java.util.List;
import java.util.Properties;
import org.hibernate.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.Initializer;
import net.es.oscars.database.HibernateUtil;


/**
 * This class tests BSS reservation-related methods called by SOAP and by WBUI.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class ReservationManagerTest extends TestCase {
    private ReservationManager rm;
    private Properties props;
    private Session session;
    private ReservationDAO dao;

    public ReservationManagerTest(String name) {
        super(name);
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.bss", true);
        Initializer initializer = new Initializer();
        initializer.initDatabase();
        this.rm = new ReservationManager();
    }

    public void setUp() {
        this.rm.setSession();
        this.session = 
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        this.dao = new ReservationDAO();
        this.dao.setSession(this.session);
        this.session.beginTransaction();
    }
        
    public void testCreate() {
        Reservation resv = new Reservation();
        Domain nextDomain = null;
        Long millis = 0L;
        Long bandwidth = 0L;

        resv.setSrcHost(this.props.getProperty("sourceHostName"));
        resv.setDestHost(this.props.getProperty("destHostName"));

        millis = System.currentTimeMillis();
        resv.setStartTime(millis);
        resv.setCreatedTime(millis);
        millis += 240 * 1000;
        resv.setEndTime(millis);

        bandwidth = Long.parseLong(this.props.getProperty("bandwidth"));
        bandwidth *= 1000000;
        resv.setBandwidth(bandwidth);
        resv.setBurstLimit(
                Long.parseLong(this.props.getProperty("burstLimit")));
        // description is unique, so can use it to access reservation in
        // other tests
        resv.setDescription(this.props.getProperty("description"));
        resv.setProtocol(this.props.getProperty("protocol"));
        resv.setLspClass(this.props.getProperty("lspClass"));
        String account = this.props.getProperty("login");

        try {
            nextDomain = this.rm.create(resv, account, null, null);
        } catch (BSSException e) {
            this.session.getTransaction().rollback();
            fail("testCreate failed: " + e.getMessage());
        }
        int id = resv.getId();
        this.session.getTransaction().commit();
        Assert.assertNotNull(id);
    }

    public void testQuery() {
        Reservation reservation = null;

        String description = this.props.getProperty("description");
        Reservation testResv = this.dao.queryByParam("description", description);
        try {
            reservation = this.rm.query(this.rm.toTag(testResv), true);
        } catch (BSSException e) {
            this.session.getTransaction().rollback();
            fail("testQuery failed: " + e.getMessage());
        }
        String testTag = this.rm.toTag(reservation);
        String newTag = this.rm.toTag(testResv);
        this.session.getTransaction().commit();
        Assert.assertEquals(testTag, newTag);
    }

    public void testAuthList() {
        List<Reservation> reservations = null;

        try {
            reservations = this.rm.list(null);
        } catch (BSSException ex) {
            this.session.getTransaction().rollback();
            fail("Caught BSSException: " + ex.getMessage());
        }
        this.session.getTransaction().commit();
        Assert.assertFalse(reservations.isEmpty());
    }

    public void testUserList() {
        List<Reservation> reservations = null;

        String login = this.props.getProperty("login");
        try {
            reservations = this.rm.list(login);
        } catch (BSSException ex) {
            this.session.getTransaction().rollback();
            fail("Caught BSSException: " + ex.getMessage());
        }
        this.session.getTransaction().commit();
        Assert.assertFalse(reservations.isEmpty());
    }

    public void testCancel() {
        List<Reservation> reservations = null;

        String description = this.props.getProperty("description");
        Reservation resv = this.dao.queryByParam("description", description);
        try {
            this.rm.cancel(this.rm.toTag(resv), resv.getLogin());
        } catch (BSSException ex) {
            this.session.getTransaction().rollback();
            fail("testCancel failed: " + ex.getMessage());
        }
        this.session.getTransaction().commit();
        Assert.assertTrue(true);
    }

    public void testPathToString() {
        String description = this.props.getProperty("description");
        Reservation resv = this.dao.queryByParam("description", description);
        String pathString = this.rm.pathToString(resv, "host");
        this.session.getTransaction().commit();
        Assert.assertNotNull(pathString);
    }
}
