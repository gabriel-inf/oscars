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
import net.es.oscars.aaa.UserManager.AuthValue;
import net.es.oscars.pathfinder.*;
import net.es.oscars.bss.topology.*;


/**
 * This class tests BSS reservation-related methods called by SOAP and by WBUI.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "bss", "reservationManager" },
        dependsOnGroups = { "importTopology", "reservationTest" })
public class ReservationManagerTest {
    private final String LAYER2_DESCRIPTION = "layer 2 test reservation";
    private ReservationManager rm;
    private Properties props;
    private SessionFactory sf;
    private String dbname;
    private boolean authorized;

  @BeforeClass
    protected void setUpClass() {
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.common", true);
        this.dbname = GlobalParams.getReservationTestDBName();
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
        this.rm = new ReservationManager(this.dbname);
        this.authorized = false;
    }

  @Test
    public void allowedTest() {
        AuthHandler authHandler = new AuthHandler();
        // can't have a failure due to this because other test modules that
        // depend on ReservationManagerTest would be skipped
        this.authorized = authHandler.checkAuthorization();
        if (!this.authorized) {
            System.err.println("Layer 3 tests will be skipped because not authorized.  They will show up as passed in the test output, however.");
        }
    }

  @Test
    public void layer2Create1() {
        Reservation resv = new Reservation();
        PathInfo pathInfo = new PathInfo();

        this.sf.getCurrentSession().beginTransaction();
        CommonReservation common = new CommonReservation();
        String vlanTag = this.props.getProperty("vlanTag").trim();
        common.setLayer2Parameters(resv, pathInfo, vlanTag,
                                   LAYER2_DESCRIPTION);
        String login = this.props.getProperty("login");

        try {
            this.rm.create(resv, login, pathInfo);
        } catch (BSSException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            Assert.fail("Could not create reservation. ", ex);
        }
        try {
            this.rm.store(resv);
        } catch (BSSException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            Assert.fail("Could not persist reservation.. ", ex);
        }
        this.sf.getCurrentSession().getTransaction().commit();
    }

  @Test
    public void layer2CreateAnyVtag() {
        Reservation resv = new Reservation();
        PathInfo pathInfo = new PathInfo();

        // Scheduler test uses this reservation
        this.sf.getCurrentSession().beginTransaction();
        CommonReservation common = new CommonReservation();
        String description =
            CommonReservation.getScheduledLayer2Description();
        common.setLayer2Parameters(resv, pathInfo, "any", description);
        String login = this.props.getProperty("login");

        try {
            this.rm.create(resv, login, pathInfo);
        } catch (BSSException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            Assert.fail("Could not create reservation. ", ex);
        }
        try {
            this.rm.store(resv);
        } catch (BSSException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            Assert.fail("Could not persist reservation.. ", ex);
        }
        this.sf.getCurrentSession().getTransaction().commit();
    }

  @Test(dependsOnMethods={ "layer2Create1" })
    public void layer2Modify1() throws BSSException {

        this.sf.getCurrentSession().beginTransaction();
        // TODO:  this needs to be reviewed as to what part of PathInfo
        //        can be changed by modify
        ReservationDAO dao = new ReservationDAO(this.dbname);
        Reservation resv =
            dao.queryByParam("description", LAYER2_DESCRIPTION);
        if (resv == null) {
            assert false : "Could not find test reservation";
        }
        // currently just copying reservation's path info
        Path path = resv.getPath();
        PathElem pathElem = path.getPathElem();
        String vlanTag = pathElem.getLinkDescr();
        Layer2Data layer2Data = path.getLayer2Data();
        Layer2Info layer2Info = new Layer2Info();
        layer2Info.setSrcEndpoint(layer2Data.getSrcEndpoint());
        layer2Info.setDestEndpoint(layer2Data.getDestEndpoint());
        VlanTag srcVtag = new VlanTag();
        srcVtag.setString(vlanTag);
        srcVtag.setTagged(true);
        layer2Info.setSrcVtag(srcVtag);
        VlanTag destVtag = new VlanTag();
        destVtag.setString(vlanTag);
        destVtag.setTagged(true);
        layer2Info.setDestVtag(srcVtag);
        PathInfo pathInfo = new PathInfo();
        pathInfo.setLayer2Info(layer2Info);
        // this is the reservation that will be cancelled
        Long seconds = resv.getEndTime();
        seconds += 3600;
        resv.setEndTime(seconds);
        try {
            this.rm.modify(resv, null, null, pathInfo);
        } catch (BSSException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw(ex);
        }
        this.sf.getCurrentSession().getTransaction().commit();
    }

  @Test(dependsOnMethods={ "layer2Create1" },
        expectedExceptions={ BSSException.class })
    public void checkOversubscribed() throws BSSException {
        Reservation resv = new Reservation();
        PathInfo pathInfo = new PathInfo();

        this.sf.getCurrentSession().beginTransaction();
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

  @Test(dependsOnMethods={ "layer2Create1" },
        expectedExceptions={ BSSException.class })
    public void checkOversubscribedVlan() throws BSSException {
        Reservation resv = new Reservation();
        PathInfo pathInfo = new PathInfo();

        this.sf.getCurrentSession().beginTransaction();
        String vlanTag = this.props.getProperty("vlanTag").trim();
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

  /*
  @Test(dependsOnMethods={ "allowedTest" })
    public void layer3Create() {
        Reservation resv = new Reservation();
        PathInfo pathInfo = new PathInfo();

        if (!this.authorized) {
            // for lack of something better, doesn't appear to be a way to
            // print out a message if something is true
            System.err.println("layer3Create skipped");
            return;
        }
        this.sf.getCurrentSession().beginTransaction();
        String description = CommonReservation.getScheduledLayer3Description();
        CommonReservation common = new CommonReservation();
        common.setLayer3Parameters(resv, pathInfo, description);
        String login = this.props.getProperty("login");

        try {
            this.rm.create(resv, login, pathInfo);
        } catch (BSSException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            Assert.fail("Could not create reservation. ", ex);
        }
        try {
            this.rm.store(resv);
        } catch (BSSException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            Assert.fail("Could not persist reservation.. ", ex);
        }
        this.sf.getCurrentSession().getTransaction().commit();
    }
*/

  @Test(dependsOnMethods={ "layer2Create1" })
    public void rmReservationQuery() throws BSSException {
        Reservation reservation = null;

        this.sf.getCurrentSession().beginTransaction();
        ReservationDAO dao = new ReservationDAO(this.dbname);
        Reservation testResv =
            dao.queryByParam("description", LAYER2_DESCRIPTION);
        if (testResv == null) {
            assert false : "Could not find test reservation";
        }
        try {
            reservation = this.rm.query(testResv.getGlobalReservationId(),
                                        null, null);
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
        List<String> logins = new ArrayList<String>();
        String login = this.props.getProperty("login");
        logins.add(login);
        try {
            reservations = this.rm.list (null, null, null, null,
                                         null, null, null, null);
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
        List<String> logins = new ArrayList<String>();
        String login = this.props.getProperty("login");
        logins.add(login);
        try {
            reservations = this.rm.list(null, null, null, null,
                                        null, null, null, null);
        } catch (BSSException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw ex;
        }
        this.sf.getCurrentSession().getTransaction().commit();
        assert !reservations.isEmpty();
    }

  @Test(dependsOnMethods={ "layer2Modify1" })
    public void rmLayer2ReservationCancel() throws BSSException {

        this.sf.getCurrentSession().beginTransaction();
        ReservationDAO dao = new ReservationDAO(this.dbname);
        Reservation resv = 
            dao.queryByParam("description", LAYER2_DESCRIPTION);
        try {
            this.rm.cancel(resv.getGlobalReservationId(), null, null);
        } catch (BSSException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw ex;
        }
        this.sf.getCurrentSession().getTransaction().commit();
    }
}
