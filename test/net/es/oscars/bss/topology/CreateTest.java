package net.es.oscars.bss.topology;

import org.testng.annotations.*;

import java.util.*;
import org.hibernate.*;

import net.es.oscars.GlobalParams;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.ReservationDAO;
import net.es.oscars.bss.CommonReservation;
import net.es.oscars.bss.BSSException;

/**
 * This class tests creating BSS topology database entries.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */

@Test(groups={ "bss.topology", "create" })
public class CreateTest {
    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    protected void setUpClass() {
        this.dbname = GlobalParams.getReservationTestDBName();
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }
        
  @Test
    public void domainCreate() {
        Domain domain = new Domain();
        domain.setName("test");
        domain.setAbbrev("test");
        domain.setTopologyIdent(CommonParams.getIdentifier());
        domain.setUrl("test");
        DomainDAO domainDAO = new DomainDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        domainDAO.create(domain);
        this.sf.getCurrentSession().getTransaction().commit();
        assert domain.getId() != null;
    }

  @Test(dependsOnMethods={ "domainCreate" })
    public void nodeCreate() {
        this.sf.getCurrentSession().beginTransaction();
        NodeDAO dao = new NodeDAO(this.dbname);
        Node node = new Node();
        node.setValid(true);
        node.setTopologyIdent(CommonParams.getIdentifier());
        DomainDAO domainDAO = new DomainDAO(this.dbname);
        Domain domain = domainDAO.queryByParam("topologyIdent", 
                                               CommonParams.getIdentifier());
        node.setDomain(domain);
        dao.create(node);
        this.sf.getCurrentSession().getTransaction().commit();
        assert node != null;
    }

  @Test(dependsOnMethods={ "nodeCreate" })
    public void nodeAddressCreate() {
        this.sf.getCurrentSession().beginTransaction();
        NodeAddressDAO dao = new NodeAddressDAO(this.dbname);
        NodeAddress nodeAddress = new NodeAddress();
        nodeAddress.setAddress(CommonParams.getIdentifier());
        NodeDAO nodeDAO = new NodeDAO(this.dbname);
        Node node = nodeDAO.queryByParam("topologyIdent",
                                         CommonParams.getIdentifier());
        nodeAddress.setNode(node);
        dao.create(nodeAddress);
        this.sf.getCurrentSession().getTransaction().commit();
        assert nodeAddress != null;
    }

  @Test(dependsOnMethods={ "nodeCreate", "nodeAddressCreate" })
    public void portCreate() {
        this.sf.getCurrentSession().beginTransaction();
        PortDAO dao = new PortDAO(this.dbname);
        NodeDAO nodeDAO = new NodeDAO(this.dbname);
        Node node = nodeDAO.queryByParam("topologyIdent",
                                         CommonParams.getIdentifier());

        Port port = new Port();
        port.setValid(true);
        port.setSnmpIndex(0);
        port.setTopologyIdent(CommonParams.getIdentifier());
        port.setCapacity(10000000L);
        port.setMaximumReservableCapacity(5000000L);
        port.setMinimumReservableCapacity(1000000L);
        port.setGranularity(1000000L);
        port.setUnreservedCapacity(5000000L);
        port.setNode(node);
        dao.create(port);
        this.sf.getCurrentSession().getTransaction().commit();
        assert port != null;
    }

  @Test(dependsOnMethods={ "portCreate" })
    public void linkCreate() {
        this.sf.getCurrentSession().beginTransaction();
        LinkDAO dao = new LinkDAO(this.dbname);
        PortDAO portDAO = new PortDAO(this.dbname);
        Port port = portDAO.queryByParam("topologyIdent",
                                         CommonParams.getIdentifier());

        Link link = new Link();
        link.setValid(true);
        link.setSnmpIndex(0);
        link.setCapacity(10000000L);
        link.setMaximumReservableCapacity(5000000L);
        link.setTopologyIdent(CommonParams.getIdentifier());
        link.setPort(port);
        dao.create(link);
        this.sf.getCurrentSession().getTransaction().commit();
        assert link != null;
    }

  @Test(dependsOnMethods={ "linkCreate" })
    public void ipaddrCreate() {
        this.sf.getCurrentSession().beginTransaction();
        IpaddrDAO dao = new IpaddrDAO(this.dbname);
        Ipaddr ipaddr = new Ipaddr();
        ipaddr.setValid(true);
        ipaddr.setIP(CommonParams.getIpAddress());
        LinkDAO linkDAO = new LinkDAO(this.dbname);
        Link link = (Link)
            linkDAO.queryByParam("topologyIdent",
                                 CommonParams.getIdentifier());
        ipaddr.setLink(link);
        dao.create(ipaddr);
        this.sf.getCurrentSession().getTransaction().commit();
        assert ipaddr != null;
    }

  @Test(dependsOnMethods={ "ipaddrCreate" })
    public void edgeInfoCreate() {
        this.sf.getCurrentSession().beginTransaction();
        EdgeInfoDAO dao = new EdgeInfoDAO(this.dbname);
        EdgeInfo edgeInfo = new EdgeInfo();
        edgeInfo.setExternalIP(CommonParams.getIpAddress());
        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);
        Ipaddr ipaddr = (Ipaddr)
            ipaddrDAO.queryByParam("IP", CommonParams.getIpAddress());
        DomainDAO domainDAO = new DomainDAO(this.dbname);
        Domain domain = (Domain)
            domainDAO.queryByParam("topologyIdent",
                                 CommonParams.getIdentifier());
        edgeInfo.setIpaddr(ipaddr);
        edgeInfo.setDomain(domain);
        dao.create(edgeInfo);
        this.sf.getCurrentSession().getTransaction().commit();
        assert edgeInfo != null;
    }

  @Test(dependsOnMethods={ "linkCreate" })
    public void l2SwitchingCapabilityDataCreate() {
        this.sf.getCurrentSession().beginTransaction();
        L2SwitchingCapabilityDataDAO dao =
            new L2SwitchingCapabilityDataDAO(this.dbname);
        L2SwitchingCapabilityData capability = new L2SwitchingCapabilityData();
        // this is just testing the bean
        capability.setVlanRangeAvailability("0");
        capability.setInterfaceMTU(1500);
        LinkDAO linkDAO = new LinkDAO(this.dbname);
        Link link = (Link)
            linkDAO.queryByParam("topologyIdent",
                                 CommonParams.getIdentifier());
        capability.setLink(link);
        dao.create(capability);
        this.sf.getCurrentSession().getTransaction().commit();
        assert capability != null;
    }

  @Test(dependsOnMethods={ "linkCreate" })
    public void routeElemCreate() {
        // depends on one of link, port, node, or domain being non-null
        // (problematic) choosing link
        this.sf.getCurrentSession().beginTransaction();
        RouteElemDAO dao = new RouteElemDAO(this.dbname);
        RouteElem routeElem = new RouteElem();
        routeElem.setDescription(CommonParams.getIdentifier());
        LinkDAO linkDAO = new LinkDAO(this.dbname);
        Link link = (Link)
            linkDAO.queryByParam("topologyIdent",
                                 CommonParams.getIdentifier());
        routeElem.setLink(link);
        dao.create(routeElem);
        this.sf.getCurrentSession().getTransaction().commit();
        assert routeElem != null;
  }

  @Test(dependsOnMethods={ "routeElemCreate" })
    public void interdomainRouteCreate() {
        this.sf.getCurrentSession().beginTransaction();
        InterdomainRouteDAO dao = new InterdomainRouteDAO(this.dbname);
        InterdomainRoute interdomainRoute = new InterdomainRoute();
        interdomainRoute.setPreference(CommonParams.getInterdomainPreference());
        interdomainRoute.setDefaultRoute(false);
        RouteElemDAO routeElemDAO = new RouteElemDAO(this.dbname);
        RouteElem routeElem = (RouteElem)
            routeElemDAO.queryByParam("description",
                                 CommonParams.getIdentifier());
        interdomainRoute.setRouteElem(routeElem);
        dao.create(interdomainRoute);
        this.sf.getCurrentSession().getTransaction().commit();
        assert interdomainRoute != null;
    }

  // Sets up all path structure in this method to test cascading save
  // as well as path creation.  Note that path has to be created as part
  // of reservation.
  @Test(dependsOnMethods={ "portCreate" })
    public void pathCreate() throws BSSException {
        this.sf.getCurrentSession().beginTransaction();
        ReservationDAO reservationDAO = new ReservationDAO(this.dbname);
        Reservation resv = new Reservation();
        CommonReservation common = new CommonReservation();
        common.setParameters(resv, "path test");
        Path path = new Path();

        // set up MPLS data
        MPLSData mplsData = new MPLSData();
        mplsData.setBurstLimit(CommonParams.getMPLSBurstLimit());
        mplsData.setLspClass("4");
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
        List<PathElem> pathElems = new ArrayList<PathElem>();

        // create ingress and egress elements in path
        PathElem ingressPathElem = new PathElem();
        PathElemParam pathElemParam = new PathElemParam();
        pathElemParam.setSwcap("test");
        pathElemParam.setType("test");
        pathElemParam.setValue("test");
        ingressPathElem.addPathElemParam(pathElemParam);
        Ipaddr ipaddr0 = new Ipaddr();
        ipaddr0.setValid(true);
        String hop0 = "hop0";
        ipaddr0.setIP(hop0);
        Link link0 = new Link();
        link0.setValid(true);
        link0.setSnmpIndex(0);
        link0.setCapacity(10000000L);
        link0.setMaximumReservableCapacity(5000000L);
        link0.setTopologyIdent(CommonParams.getPathIdentifier());
        ipaddr0.setLink(link0);
        PortDAO portDAO = new PortDAO(this.dbname);
        Port port = portDAO.queryByParam("topologyIdent",
                                         CommonParams.getIdentifier());
        link0.setPort(port);
        linkDAO.create(link0);
        ingressPathElem.setLink(link0);
        pathElems.add(ingressPathElem);

        PathElem egressPathElem = new PathElem();
        Ipaddr ipaddr1 = new Ipaddr();
        ipaddr1.setValid(true);
        String hop1 = "hop1";
        ipaddr1.setIP(hop1);
        Link link1 = new Link();
        link1.setValid(true);
        link1.setSnmpIndex(1);
        link1.setCapacity(10000000L);
        link1.setMaximumReservableCapacity(5000000L);
        link1.setTopologyIdent("test link 1");
        // just testing Hibernate; reservation scheduling wouldn't work
        // with ingress and egress having the same ports
        link1.setPort(port);
        ipaddr1.setLink(link1);
        linkDAO.create(link1);
        egressPathElem.setLink(link1);
        pathElems.add(egressPathElem);

        path.setPathElems(pathElems);
        path.setPathType(PathType.LOCAL);
        resv.setPath(path);
        reservationDAO.create(resv);
        this.sf.getCurrentSession().getTransaction().commit();
        assert path.getId() != null;
    }

  @Test(dependsOnMethods={ "pathCreate" })
    public void cascadingSavedLayer2Data() {
        this.sf.getCurrentSession().beginTransaction();
        Layer2DataDAO layer2DataDAO = new Layer2DataDAO(this.dbname);
        Layer2Data layer2Data = (Layer2Data)
            layer2DataDAO.queryByParam("srcEndpoint",
                                       CommonParams.getSrcEndpoint());
        this.sf.getCurrentSession().getTransaction().commit();
        // if cascading save worked with path create, this will
        // not be null
        assert layer2Data != null;
    }

  @Test(dependsOnMethods={ "pathCreate" })
    public void cascadingSavedLayer3Data() {
        this.sf.getCurrentSession().beginTransaction();
        Layer3DataDAO layer3DataDAO = new Layer3DataDAO(this.dbname);
        Layer3Data layer3Data = (Layer3Data)
            layer3DataDAO.queryByParam("srcHost",
                                       CommonParams.getSrcHost());
        this.sf.getCurrentSession().getTransaction().commit();
        // if cascading save worked with path create, this will
        // not be null
        assert layer3Data != null;
    }


  @Test(dependsOnMethods={ "pathCreate" })
    public void cascadingSavedMPLSData() {
        this.sf.getCurrentSession().beginTransaction();
        MPLSDataDAO mplsDataDAO = new MPLSDataDAO(this.dbname);
        MPLSData mplsData = (MPLSData)
            mplsDataDAO.queryByParam("burstLimit",
                                     CommonParams.getMPLSBurstLimit());
        this.sf.getCurrentSession().getTransaction().commit();
        // if cascading save worked with path create, this will
        // not be null
        assert mplsData != null;
    }

}
