package net.es.oscars.bss;

import org.testng.annotations.*;

import java.util.*;
import java.util.Properties;
import org.hibernate.*;

import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlanePathContent;
import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlaneHopContent;

import net.es.oscars.GlobalParams;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.PropHandler;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.pathfinder.*;
import net.es.oscars.bss.topology.*;


/**
 * This class tests BSS reservation-related methods called by SOAP and by WBUI.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
// @Test(groups={ "bss" })
@Test(groups={ "broken" })
public class ReservationManagerTest {
    private final Long BANDWIDTH = 25000000L;   // 25 Mbps
    private final int BURST_LIMIT = 10000000; // 10 Mbps
    private final int DURATION = 240000;       // 4 minutes 
    private final String PROTOCOL = "UDP";
    private final String LSP_CLASS = "4";
    private ReservationManager rm;
    private Properties props;
    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    protected void setUpClass() {
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.common", true);
        this.dbname = GlobalParams.getReservationTestDBName();
        this.sf = HibernateUtil.getSessionFactory(dbname);
        this.rm = new ReservationManager(this.dbname);
    }

  @Test
    public void testCreate() throws BSSException {
        Reservation resv = new Reservation();
        PathInfo pathInfo = new PathInfo();
        CtrlPlanePathContent path = new CtrlPlanePathContent();
        Layer3Info layer3Info = new Layer3Info();
        MplsInfo mplsInfo = new MplsInfo();
        Long millis = 0L;
        Long bandwidth = 0L;
        String url = null;
        int id = -1;

        this.sf.getCurrentSession().beginTransaction();
        ReservationDAO dao = new ReservationDAO(this.dbname);
        layer3Info.setSrcHost(this.props.getProperty("sourceHostIP"));
        layer3Info.setDestHost(this.props.getProperty("destHostIP"));

        millis = System.currentTimeMillis();
        resv.setStartTime(millis);
        resv.setCreatedTime(millis);
        millis += DURATION;
        resv.setEndTime(millis);

        resv.setBandwidth(BANDWIDTH);
        mplsInfo.setBurstLimit(BURST_LIMIT);
        // description is unique, so can use it to access reservation in
        // other tests
        resv.setDescription(CommonParams.getReservationDescription());
        layer3Info.setProtocol(PROTOCOL);
        mplsInfo.setLspClass(LSP_CLASS);
        String account = this.props.getProperty("login");
        pathInfo.setLayer3Info(layer3Info);
        pathInfo.setMplsInfo(mplsInfo);
        pathInfo.setPath(path);

        try {
            this.rm.create(resv, account, pathInfo);
        } catch (BSSException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw ex;
        }
        id = resv.getId();
        this.sf.getCurrentSession().getTransaction().commit();
    }

  @Test(dependsOnMethods={ "testCreate" })
    public void testQuery() throws BSSException {
        Reservation reservation = null;

        this.sf.getCurrentSession().beginTransaction();
        ReservationDAO dao = new ReservationDAO(this.dbname);
        String description = CommonParams.getReservationDescription();
        Reservation testResv = dao.queryByParam("description", description);
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

  @Test(dependsOnMethods={ "testCreate" })
    public void testAuthList() throws BSSException {
        List<Reservation> reservations = null;

        this.sf.getCurrentSession().beginTransaction();
        ReservationDAO dao = new ReservationDAO(this.dbname);
        List<String> logins = new ArrayList<String>();
        String login = this.props.getProperty("login");
        logins.add(login);
        try {
            reservations = this.rm.list(login, logins, null, null,
                                        null, null);
        } catch (BSSException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw ex;
        }
        this.sf.getCurrentSession().getTransaction().commit();
        assert !reservations.isEmpty();
    }

  @Test(dependsOnMethods={ "testCreate" })
    public void testUserList() throws BSSException {
        List<Reservation> reservations = null;

        this.sf.getCurrentSession().beginTransaction();
        ReservationDAO dao = new ReservationDAO(this.dbname);
        List<String> logins = new ArrayList<String>();
        String login = this.props.getProperty("login");
        logins.add(login);
        try {
            reservations = this.rm.list(login, logins, null, null, null, null);
        } catch (BSSException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw ex;
        }
        this.sf.getCurrentSession().getTransaction().commit();
        assert !reservations.isEmpty();
    }

  @Test(dependsOnMethods={ "testCreate" })
    public void testCancel() throws BSSException {
        List<Reservation> reservations = null;

        this.sf.getCurrentSession().beginTransaction();
        ReservationDAO dao = new ReservationDAO(this.dbname);
        String description = CommonParams.getReservationDescription();
        Reservation resv = dao.queryByParam("description", description);
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
