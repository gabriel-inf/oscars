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
    private final Long BANDWIDTH = 25000000L;   // 25 Mbps
    private final int BURST_LIMIT = 10000000; // 10 Mbps
    private final int DURATION = 240000;       // 4 minutes 
    private final String PROTOCOL = "UDP";
    private final String LSP_CLASS = "4";
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
        Layer2Info layer2Info = new Layer2Info();

        this.sf.getCurrentSession().beginTransaction();
        ReservationDAO dao = new ReservationDAO(this.dbname);
        layer2Info.setSrcEndpoint(this.props.getProperty("layer2Src"));
        layer2Info.setDestEndpoint(this.props.getProperty("layer2Dest"));
        VlanTag srcVtag = new VlanTag();
        srcVtag.setString(this.props.getProperty("vlanTag"));
        srcVtag.setTagged(true);
        layer2Info.setSrcVtag(srcVtag);
        VlanTag destVtag = new VlanTag();
        destVtag.setString(this.props.getProperty("vlanTag"));
        destVtag.setTagged(true);
        layer2Info.setDestVtag(destVtag);

        CommonReservation common = new CommonReservation();
        common.setParameters(resv, LAYER2_DESCRIPTION + " #1");
        resv.setBandwidth(5000000000L);
        String login = this.props.getProperty("login");
        pathInfo.setLayer2Info(layer2Info);

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

  @Test(expectedExceptions={ BSSException.class })
    public void checkOversubscribed() throws BSSException {
        Reservation resv = new Reservation();
        PathInfo pathInfo = new PathInfo();
        Layer2Info layer2Info = new Layer2Info();

        this.sf.getCurrentSession().beginTransaction();
        ReservationDAO dao = new ReservationDAO(this.dbname);
        layer2Info.setSrcEndpoint(this.props.getProperty("layer2Src"));
        layer2Info.setDestEndpoint(this.props.getProperty("layer2Dest"));
        String vlanTag = this.props.getProperty("vlanTag");
        int newVlan = Integer.parseInt(vlanTag.trim());
        newVlan += 1;
        String newVlanStr = Integer.toString(newVlan);
        VlanTag srcVtag = new VlanTag();
        srcVtag.setString(newVlanStr);
        srcVtag.setTagged(true);
        layer2Info.setSrcVtag(srcVtag);
        VlanTag destVtag = new VlanTag();
        destVtag.setString(newVlanStr);
        destVtag.setTagged(true);
        layer2Info.setDestVtag(destVtag);

        CommonReservation common = new CommonReservation();
        common.setParameters(resv, LAYER2_DESCRIPTION + " #2");
        resv.setBandwidth(6000000000L);
        String login = this.props.getProperty("login");
        pathInfo.setLayer2Info(layer2Info);

        try {
            this.rm.create(resv, login, pathInfo);
        } catch (BSSException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw(ex);
        }
        this.sf.getCurrentSession().getTransaction().commit();
    }

/*
  @Test(expectedExceptions={ BSSException.class })
    public void checkOversubscribedVlan() throws BSSException {
        Reservation resv = new Reservation();
        PathInfo pathInfo = new PathInfo();
        Layer2Info layer2Info = new Layer2Info();

        this.sf.getCurrentSession().beginTransaction();
        ReservationDAO dao = new ReservationDAO(this.dbname);
        layer2Info.setSrcEndpoint(this.props.getProperty("layer2Src"));
        layer2Info.setDestEndpoint(this.props.getProperty("layer2Dest"));
        String vlanTag = this.props.getProperty("vlanTag");
        VlanTag srcVtag = new VlanTag();
        srcVtag.setString(vlanTag);
        srcVtag.setTagged(true);
        layer2Info.setSrcVtag(srcVtag);
        VlanTag destVtag = new VlanTag();
        destVtag.setString(vlanTag);
        destVtag.setTagged(true);
        layer2Info.setDestVtag(destVtag);

        CommonReservation common = new CommonReservation();
        common.setParameters(resv, LAYER2_DESCRIPTION + " #3");
        String login = this.props.getProperty("login");
        pathInfo.setLayer2Info(layer2Info);

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
        Layer3Info layer3Info = new Layer3Info();
        MplsInfo mplsInfo = new MplsInfo();

        this.sf.getCurrentSession().beginTransaction();
        ReservationDAO dao = new ReservationDAO(this.dbname);
        layer3Info.setSrcHost(this.props.getProperty("srcHost"));
        layer3Info.setDestHost(this.props.getProperty("destHost"));

        mplsInfo.setBurstLimit(BURST_LIMIT);
        CommonReservation common = new CommonReservation();
        common.setParameters(resv, LAYER3_DESCRIPTION);
        layer3Info.setProtocol(PROTOCOL);
        mplsInfo.setLspClass(LSP_CLASS);
        String login = this.props.getProperty("login");
        pathInfo.setLayer3Info(layer3Info);
        pathInfo.setMplsInfo(mplsInfo);

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

  @Test(dependsOnMethods={ "layer3Create" })
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

  @Test(dependsOnMethods={ "layer3Create" })
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

  @Test(dependsOnMethods={ "layer3Create" })
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
