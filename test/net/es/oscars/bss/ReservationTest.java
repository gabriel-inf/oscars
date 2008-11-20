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
@Test(groups={ "bss", "reservationTest" })
public class ReservationTest {
    private final String DESCRIPTION = "ReservationTest reservation";
    private final String TOPOLOGY_IDENT = "ReservationTest id";
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
    public void reservationDAOCreate() throws BSSException {

        // have to build everything by hand for DAO test
        Reservation resv = new Reservation();
        // this is just testing the bean, so any value will do, as long as it
        // is the correct type and required ones are non-null
        this.sf.getCurrentSession().beginTransaction();
        ReservationDAO dao = new ReservationDAO(this.dbname);
        CommonReservation common = new CommonReservation();
        common.setParameters(resv, DESCRIPTION);

        Path path = new Path();
        path.setExplicit(false);

        // set up MPLS data
        MPLSData mplsData = new MPLSData();
        mplsData.setBurstLimit(10000000L);
        mplsData.setLspClass("4");
        path.setMplsData(mplsData);

        // set up layer 2 data
        Layer2Data layer2Data = new Layer2Data();
        layer2Data.setSrcEndpoint(CommonParams.getSrcEndpoint());
        layer2Data.setDestEndpoint(CommonParams.getDestEndpoint());
        path.setLayer2Data(layer2Data);

        // set up layer 3 data (just testing Hibernate structures,
        // won't be both layer 2 and layer 3 data in real path)
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
        link.setTopologyIdent(TOPOLOGY_IDENT);
        ipaddr.setLink(link);

        DomainDAO domainDAO = new DomainDAO(this.dbname);
        Domain domain = new Domain();
        domain.setName("test");
        domain.setAbbrev("test");
        domain.setTopologyIdent(TOPOLOGY_IDENT);
        domain.setUrl("test");
        domainDAO.create(domain);

        NodeDAO nodeDAO = new NodeDAO(this.dbname);
        Node node = new Node();
        node.setValid(true);
        node.setTopologyIdent(TOPOLOGY_IDENT);
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
        port.setTopologyIdent(TOPOLOGY_IDENT);
        port.setNode(node);
        portDAO.create(port);

        link.setPort(port);
        linkDAO.create(link);
        ipaddrDAO.create(ipaddr);
        ingressPathElem.setLink(link);
        List<PathElem> pathElems = new ArrayList<PathElem>();
        pathElems.add(ingressPathElem);
        path.setPathElems(pathElems);
        resv.setPath(path, "intra");
        dao.create(resv);
        this.sf.getCurrentSession().getTransaction().commit();
    }

  @Test(dependsOnMethods={ "reservationDAOCreate" })
    public void reservationDAOQuery() {
        this.sf.getCurrentSession().beginTransaction();
        ReservationDAO dao = new ReservationDAO(this.dbname);
        Reservation reservation =
            dao.queryByParam("description", DESCRIPTION);
        this.sf.getCurrentSession().getTransaction().commit();
        assert reservation != null;
    }

  @Test(dependsOnMethods={ "reservationDAOCreate" })
    public void reservationDAOAuthList() throws BSSException {
        List<Reservation> reservations = null;

        this.sf.getCurrentSession().beginTransaction();
        ReservationDAO dao = new ReservationDAO(this.dbname);
        String login = this.props.getProperty("login");
        List<String> logins = null;
        try {
             // if null, list all reservations by all users
            reservations = dao.list(10, 0, logins, null, null, null, null,
                                    null, null);
        } catch (BSSException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw ex;
        }

        this.sf.getCurrentSession().getTransaction().commit();
        assert !reservations.isEmpty();
    }

  @Test(dependsOnMethods={ "reservationDAOCreate" })
    public void reservationDAOUserList() throws BSSException {
        List<Reservation> reservations = null;

        this.sf.getCurrentSession().beginTransaction();
        ReservationDAO dao = new ReservationDAO(this.dbname);
        List<String> logins = new ArrayList<String>();
        String login = this.props.getProperty("login");
        logins.add(login);
        try {
            reservations = dao.list(10, 0, logins, null, null, null, null,
                                    null, null);
        } catch (BSSException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw ex;
        }
        this.sf.getCurrentSession().getTransaction().commit();
        assert !reservations.isEmpty();
    }
  
  @Test(dependsOnMethods={ "reservationDAOQuery", "reservationDAOUserList",
                           "reservationDAOAuthList" })
    public void reservationDAORemove() {
        this.sf.getCurrentSession().beginTransaction();
        ReservationDAO dao = new ReservationDAO(this.dbname);
        Reservation reservation =
                (Reservation) dao.queryByParam("description", DESCRIPTION);
        dao.remove(reservation);
        // clean up other objects created
        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);
        Ipaddr ipaddr =
                (Ipaddr) ipaddrDAO.queryByParam("IP", "hop0");
        ipaddrDAO.remove(ipaddr);
        LinkDAO linkDAO = new LinkDAO(this.dbname);
        Link link =
                (Link) linkDAO.queryByParam("topologyIdent", TOPOLOGY_IDENT);
        linkDAO.remove(link);
        PortDAO portDAO = new PortDAO(this.dbname);
        Port port =
                (Port) portDAO.queryByParam("topologyIdent", TOPOLOGY_IDENT);
        portDAO.remove(port);
        NodeDAO nodeDAO = new NodeDAO(this.dbname);
        Node node =
                (Node) nodeDAO.queryByParam("topologyIdent", TOPOLOGY_IDENT);
        nodeDAO.remove(node);
        this.sf.getCurrentSession().getTransaction().commit();
    }
}
