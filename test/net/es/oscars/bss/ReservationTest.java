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
 * This class tests access to the reservations table, which requires a working
 *     Reservation.java and Reservation.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "bss" })
public class ReservationTest {
    private final Long BANDWIDTH = 25000000L;   // 25 Mbps
    private final Long BURST_LIMIT = 10000000L; // 10 Mbps
    private final int DURATION = 240;       // 4 minutes 
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
        Long seconds = 0L;
        Long bandwidth = 0L;

        // have to build everything by hand for DAO test
        Reservation resv = new Reservation();
        // this is just testing the bean and setting layer 3 info;
        // Extensive layer 2 tests will be in ReservationManagerTest
        this.sf.getCurrentSession().beginTransaction();
        ReservationDAO dao = new ReservationDAO(this.dbname);
        seconds = System.currentTimeMillis()/1000;
        resv.setStartTime(seconds);
        resv.setCreatedTime(seconds);
        seconds += DURATION;
        resv.setEndTime(seconds);

        resv.setBandwidth(BANDWIDTH);
        resv.setDescription(CommonParams.getReservationDescription());
        resv.setStatus("TEST");
        resv.setLogin(this.props.getProperty("login"));

        PathDAO pathDAO = new PathDAO(this.dbname);
        Path path = new Path();
        path.setExplicit(false);

        // set up MPLS data
        MPLSData mplsData = new MPLSData();
        mplsData.setBurstLimit(BURST_LIMIT);
        mplsData.setLspClass(LSP_CLASS);
        path.setMplsData(mplsData);

        // set up layer 2 data
        Layer2Data layer2Data = new Layer2Data();
        layer2Data.setSrcEndpoint(CommonParams.getSrcEndpoint());
        layer2Data.setDestEndpoint(CommonParams.getDestEndpoint());
        path.setLayer2Data(layer2Data);

        // set up layer 3 data (just testing Hibernate structures,
        // won't be both layer 2 and layer 3 data in real path
        Layer3Data layer3Data = new Layer3Data();
        layer3Data.setSrcHost(CommonParams.getSrcHost());
        layer3Data.setDestHost(CommonParams.getDestHost());
        path.setLayer3Data(layer3Data);

        LinkDAO linkDAO = new LinkDAO(this.dbname);

        // create ingress element in path
        // a few interdepencies to take care of...
        PathElem ingressPathElem = new PathElem();
        ingressPathElem.setDescription("ingress");
        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);
        Ipaddr ipaddr = new Ipaddr();
        ipaddr.setValid(true);
        String hop0 = "hop0";
        ipaddr.setIP(hop0);
        Link link = new Link();
        link.setValid(true);
        link.setSnmpIndex(0);
        link.setCapacity(10000000L);
        link.setMaximumReservableCapacity(5000000L);
        link.setTopologyIdent(CommonParams.getResvIdentifier());
        ipaddr.setLink(link);

        DomainDAO domainDAO = new DomainDAO(this.dbname);
        Domain domain = new Domain();
        domain.setName("test");
        domain.setAbbrev("test");
        domain.setTopologyIdent(CommonParams.getResvIdentifier());
        domain.setUrl("test");
        domainDAO.create(domain);

        NodeDAO nodeDAO = new NodeDAO(this.dbname);
        Node node = new Node();
        node.setValid(true);
        node.setTopologyIdent(CommonParams.getResvIdentifier());
        node.setDomain(domain);
        nodeDAO.create(node);

        PortDAO portDAO = new PortDAO(this.dbname);
        Port port = new Port();
        port.setValid(true);
        port.setSnmpIndex(0);
        port.setCapacity(10000000L);
        port.setMaximumReservableCapacity(5000000L);
        port.setMinimumReservableCapacity(5000000L);
        port.setUnreservedCapacity(0L);
        port.setTopologyIdent(CommonParams.getResvIdentifier());
        port.setNode(node);
        portDAO.create(port);

        link.setPort(port);
        linkDAO.create(link);
        ipaddrDAO.create(ipaddr);
        ingressPathElem.setLink(link);

        path.setPathElem(ingressPathElem);
        resv.setPath(path);
        dao.create(resv);
        this.sf.getCurrentSession().getTransaction().commit();
    }

  @Test(dependsOnMethods={ "testReservationCreate" })
    public void testReservationQuery() {
        this.sf.getCurrentSession().beginTransaction();
        ReservationDAO dao = new ReservationDAO(this.dbname);
        String description = CommonParams.getReservationDescription();
        Reservation reservation =
            dao.queryByParam("description", description);
        this.sf.getCurrentSession().getTransaction().commit();
        assert reservation != null;
    }

  @Test(dependsOnMethods={ "testReservationCreate" })
    public void testReservationAuthList() throws BSSException {
        List<Reservation> reservations = null;

        this.sf.getCurrentSession().beginTransaction();
        ReservationDAO dao = new ReservationDAO(this.dbname);
        String login = this.props.getProperty("login");
        List<String> logins = null;
        try {
             // if null, list all reservations by all users
            reservations = dao.list(logins, null, null, null, null, null);
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
            reservations = dao.list(logins, null, null, null, null, null);
        } catch (BSSException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw ex;
        }
        this.sf.getCurrentSession().getTransaction().commit();
        assert !reservations.isEmpty();
    }
  
  @Test(dependsOnMethods={ "testReservationQuery", "testReservationUserList",
                           "testReservationAuthList" })
    public void testReservationRemove() {
        this.sf.getCurrentSession().beginTransaction();
        ReservationDAO dao = new ReservationDAO(this.dbname);
        String description = CommonParams.getReservationDescription();
        Reservation reservation =
                (Reservation) dao.queryByParam("description", description);
        dao.remove(reservation);
        // clean up other objects created
        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);
        Ipaddr ipaddr =
                (Ipaddr) ipaddrDAO.queryByParam("IP", "hop0");
        ipaddrDAO.remove(ipaddr);
        LinkDAO linkDAO = new LinkDAO(this.dbname);
        Link link =
                (Link) linkDAO.queryByParam("topologyIdent",
                               CommonParams.getResvIdentifier());
        linkDAO.remove(link);
        PortDAO portDAO = new PortDAO(this.dbname);
        Port port =
                (Port) portDAO.queryByParam("topologyIdent",
                               CommonParams.getResvIdentifier());
        portDAO.remove(port);
        NodeDAO nodeDAO = new NodeDAO(this.dbname);
        Node node =
                (Node) nodeDAO.queryByParam("topologyIdent",
                               CommonParams.getResvIdentifier());
        nodeDAO.remove(node);
        this.sf.getCurrentSession().getTransaction().commit();
    }
}
