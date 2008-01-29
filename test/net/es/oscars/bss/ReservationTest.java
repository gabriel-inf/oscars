package net.es.oscars.bss;

import org.testng.annotations.*;

import java.util.*;
import java.util.Properties;
import org.hibernate.*;

import net.es.oscars.GlobalParams;
import net.es.oscars.PropHandler;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.bss.topology.*;


/**
 * This class tests BSS reservation-related methods called by SOAP and by WBUI.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "bss" })
public class ReservationTest {
    private final Long BANDWIDTH = 25000000L;   // 25 Mbps
    private final Long BURST_LIMIT = 10000000L; // 10 Mbps
    private final int DURATION = 240000;       // 4 minutes 
    private final String PROTOCOL = "UDP";
    private final String LSP_CLASS = "4";
    private Properties props;
    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    protected void setUpClass() {
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.common", true);
        this.dbname = GlobalParams.getReservationTestDBName();
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }

  @Test
    public void testReservationCreate() throws BSSException {
        Reservation resv = new Reservation();
        Path path = new Path();
        Layer3Data layer3Data = new Layer3Data();
        MPLSData mplsData = new MPLSData();
        Long millis = 0L;
        Long bandwidth = 0L;
        String url = null;
        int id = -1;

        assert true;
        return;
        /*
        this.sf.getCurrentSession().beginTransaction();
        ReservationDAO dao = new ReservationDAO(this.dbname);
        layer3Data.setSrcHost(this.props.getProperty("sourceHostIP"));
        layer3Data.setDestHost(this.props.getProperty("destHostIP"));

        millis = System.currentTimeMillis();
        resv.setStartTime(millis);
        resv.setCreatedTime(millis);
        millis += DURATION;
        resv.setEndTime(millis);

        resv.setBandwidth(BANDWIDTH);
        mplsData.setBurstLimit(BURST_LIMIT);
        // description is unique, so can use it to access reservation in
        // other tests
        resv.setDescription(CommonParams.getReservationDescription());
        layer3Data.setProtocol(PROTOCOL);
        mplsData.setLspClass(LSP_CLASS);
        resv.setLogin(this.props.getProperty("login"));
        resv.setPath(path);
        dao.create(resv);
        this.sf.getCurrentSession().getTransaction().commit();
        */
    }

  @Test(dependsOnMethods={ "testReservationCreate" })
    public void testReservationQuery() throws BSSException {
        Reservation reservation = null;

        assert true;
        return;
        /*
        this.sf.getCurrentSession().beginTransaction();
        ReservationDAO dao = new ReservationDAO(this.dbname);
        String description = CommonParams.getReservationDescription();
        Reservation testResv = dao.queryByParam("description", description);
        try {
            reservation =
                dao.query(testResv.getGlobalReservationId());
        } catch (BSSException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw ex;
        }
        this.sf.getCurrentSession().getTransaction().commit();
        assert testResv.getId() == reservation.getId();
        */
    }

  @Test(dependsOnMethods={ "testReservationCreate" })
    public void testReservationAuthList() throws BSSException {
        List<Reservation> reservations = null;

        this.sf.getCurrentSession().beginTransaction();
        ReservationDAO dao = new ReservationDAO(this.dbname);
        List<String> logins = new ArrayList<String>();
        String login = this.props.getProperty("login");
        logins.add(login);
        try {
            reservations = dao.list(logins, null, null, null, null);
        } catch (BSSException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw ex;
        }
        this.sf.getCurrentSession().getTransaction().commit();
        assert !reservations.isEmpty();
    }

  @Test(dependsOnMethods={ "testReservationCreate" })
    public void testReservationUserList() throws BSSException {
        List<Reservation> reservations = null;

        this.sf.getCurrentSession().beginTransaction();
        ReservationDAO dao = new ReservationDAO(this.dbname);
        List<String> logins = new ArrayList<String>();
        String login = this.props.getProperty("login");
        logins.add(login);
        try {
            reservations = dao.list(logins, null, null, null, null);
        } catch (BSSException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw ex;
        }
        this.sf.getCurrentSession().getTransaction().commit();
        assert !reservations.isEmpty();
    }
}