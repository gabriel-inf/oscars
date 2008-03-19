package net.es.oscars.bss;

import org.testng.annotations.*;
import org.testng.Assert;

import java.util.*;
import java.util.Properties;
import org.hibernate.*;

import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlanePathContent;
import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlaneHopContent;

import net.es.oscars.wsdlTypes.*;
import net.es.oscars.GlobalParams;
import net.es.oscars.PropHandler;
import net.es.oscars.AuthHandler;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.pathfinder.*;
import net.es.oscars.bss.topology.*;


/**
 * This class tests BSS reservation-related methods called by SOAP and by WBUI.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "bss", "reservationManager" },
        dependsOnGroups = { "importTopology" })
public class ReservationManagerTest {
    private final String LAYER2_DESCRIPTION = "layer 2 test reservation";
    private final String LAYER3_DESCRIPTION = "layer 3 test reservation";
    private ReservationManager rm;
    private Properties props;
    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    protected void setUpClass() {
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.common", true);
        this.dbname = GlobalParams.getReservationTestDBName();
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
        this.rm = new ReservationManager(this.dbname);
    }

  @Test
    public void allowedTest() {
        AuthHandler authHandler = new AuthHandler();
        boolean authorized = authHandler.checkAuthorization();
        Assert.assertTrue(authorized, 
            "Not authorized to do a layer 3 reservation using traceroute from this machine. ");
    }

  @Test
    public void layer2Create1() {
        Reservation resv = new Reservation();
        PathInfo pathInfo = new PathInfo();

        this.sf.getCurrentSession().beginTransaction();
        ReservationDAO dao = new ReservationDAO(this.dbname);
        CommonReservation common = new CommonReservation();
        String vlanTag = this.props.getProperty("vlanTag");
        common.setLayer2Parameters(resv, pathInfo, vlanTag,
                                   LAYER2_DESCRIPTION + " #1");
        String login = this.props.getProperty("login");

        try {
            this.rm.create(resv, login, pathInfo);
        } catch (BSSException ex) {
            Assert.fail("Could not create reservation. ", ex);
            this.sf.getCurrentSession().getTransaction().rollback();
        }
        try {
            this.rm.store(resv);
        } catch (BSSException ex) {
            Assert.fail("Could not persist reservation.. ", ex);
            this.sf.getCurrentSession().getTransaction().rollback();
        }
        this.sf.getCurrentSession().getTransaction().commit();
    }

  @Test(dependsOnMethods={ "layer2Create1" },
        expectedExceptions={ BSSException.class })
    public void checkOversubscribed() throws BSSException {
        Reservation resv = new Reservation();
        PathInfo pathInfo = new PathInfo();

        this.sf.getCurrentSession().beginTransaction();
        ReservationDAO dao = new ReservationDAO(this.dbname);
        String vlanTag = this.props.getProperty("vlanTag");
        int newVlan = Integer.parseInt(vlanTag.trim());
        newVlan += 1;
        String newVlanStr = Integer.toString(newVlan);
        CommonReservation common = new CommonReservation();
        common.setLayer2Parameters(resv, pathInfo, newVlanStr,
                                   LAYER2_DESCRIPTION + " #2");
        resv.setBandwidth(10000000000L);
        String login = this.props.getProperty("login");

        try {
            this.rm.create(resv, login, pathInfo);
        } catch (BSSException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw(ex);
        }
        this.sf.getCurrentSession().getTransaction().commit();
    }

/*
  @Test(dependsOnMethods={ "layer2Create1" },
        expectedExceptions={ BSSException.class })
    public void checkOversubscribedVlan() throws BSSException {
        Reservation resv = new Reservation();
        PathInfo pathInfo = new PathInfo();

        this.sf.getCurrentSession().beginTransaction();
        ReservationDAO dao = new ReservationDAO(this.dbname);
        String vlanTag = this.props.getProperty("vlanTag");
        CommonReservation common = new CommonReservation();
        common.setLayer2Parameters(resv, pathInfo, vlanTag,
                                   LAYER2_DESCRIPTION + " #3");
        String login = this.props.getProperty("login");

        try {
            this.rm.create(resv, login, pathInfo);
        } catch (BSSException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw(ex);
        }
        this.sf.getCurrentSession().getTransaction().commit();
    }
*/

  @Test(dependsOnMethods={ "allowedTest" })
    public void layer3Create() {
        Reservation resv = new Reservation();
        PathInfo pathInfo = new PathInfo();

        this.sf.getCurrentSession().beginTransaction();
        ReservationDAO dao = new ReservationDAO(this.dbname);
        CommonReservation common = new CommonReservation();
        common.setLayer3Parameters(resv, pathInfo, LAYER3_DESCRIPTION);
        String login = this.props.getProperty("login");

        try {
            this.rm.create(resv, login, pathInfo);
        } catch (BSSException ex) {
            Assert.fail("Could not create reservation. ", ex);
            this.sf.getCurrentSession().getTransaction().rollback();
        }
        try {
            this.rm.store(resv);
        } catch (BSSException ex) {
            Assert.fail("Could not persist reservation.. ", ex);
            this.sf.getCurrentSession().getTransaction().rollback();
        }
        this.sf.getCurrentSession().getTransaction().commit();
    }

  @Test(dependsOnMethods={ "layer2Create1" })
    public void rmReservationQuery() throws BSSException {
        Reservation reservation = null;

        this.sf.getCurrentSession().beginTransaction();
        ReservationDAO dao = new ReservationDAO(this.dbname);
        String description = CommonParams.getReservationDescription();
        Reservation testResv =
            dao.queryByParam("description", LAYER3_DESCRIPTION);
        if (testResv == null) {
            assert false : "Could not find test reservation";
        }
        try {
            reservation = this.rm.query(testResv.getGlobalReservationId(),
                                        testResv.getLogin(), true);
        } catch (BSSException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw ex;
        }
        String testGri = reservation.getGlobalReservationId();
        String newGri = testResv.getGlobalReservationId();
        this.sf.getCurrentSession().getTransaction().commit();
        assert testGri.equals(newGri);
    }

  @Test(dependsOnMethods={ "layer2Create1" })
    public void rmAuthList() throws BSSException {
        List<Reservation> reservations = null;

        this.sf.getCurrentSession().beginTransaction();
        ReservationDAO dao = new ReservationDAO(this.dbname);
        List<String> logins = new ArrayList<String>();
        String login = this.props.getProperty("login");
        logins.add(login);
        try {
            reservations = this.rm.list(login, logins, null, null,
                                        null, null, null);
        } catch (BSSException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw ex;
        }
        this.sf.getCurrentSession().getTransaction().commit();
        assert !reservations.isEmpty();
    }

  @Test(dependsOnMethods={ "layer2Create1" })
    public void rmUserList() throws BSSException {
        List<Reservation> reservations = null;

        this.sf.getCurrentSession().beginTransaction();
        ReservationDAO dao = new ReservationDAO(this.dbname);
        List<String> logins = new ArrayList<String>();
        String login = this.props.getProperty("login");
        logins.add(login);
        try {
            reservations = this.rm.list(login, logins, null, null, null, null,
                                        null);
        } catch (BSSException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw ex;
        }
        this.sf.getCurrentSession().getTransaction().commit();
        assert !reservations.isEmpty();
    }

  @Test(dependsOnMethods={ "layer3Create" })
    public void rmReservationCancel() throws BSSException {
        List<Reservation> reservations = null;

        this.sf.getCurrentSession().beginTransaction();
        ReservationDAO dao = new ReservationDAO(this.dbname);
        Reservation resv = 
            dao.queryByParam("description", LAYER3_DESCRIPTION);
        try {
            this.rm.cancel(resv.getGlobalReservationId(), resv.getLogin(),
                           true);
        } catch (BSSException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw ex;
        }
        this.sf.getCurrentSession().getTransaction().commit();
    }
}
