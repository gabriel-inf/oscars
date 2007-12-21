package net.es.oscars.bss.topology;

import org.testng.annotations.*;

import java.util.*;
import org.hibernate.*;

import net.es.oscars.GlobalParams;
import net.es.oscars.database.HibernateUtil;

/**
 * This class tests removal of BSS topology database entries, including the use
 * of associations between Node, Port, Link, and Ipaddr.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "bss.topology", "remove" }, dependsOnGroups={
    "ipaddr", "link", "port", "node", "nodeAddress", "domain", "mplsData",
    "layer2Data", "layer3Data", "pathElem", "path", "l2SwitchData" })
public class RemoveTest {
    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    protected void setUpClass() {
        this.dbname = GlobalParams.getReservationTestDBName();
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }
        

  @Test
    public void nodeAddressRemove() {
        this.sf.getCurrentSession().beginTransaction();
        String address = CommonParams.getIdentifier();
        NodeAddressDAO nodeAddressDAO = new NodeAddressDAO(this.dbname);
        NodeAddress nodeAddress = (NodeAddress)
            nodeAddressDAO.queryByParam("address", address);
        nodeAddressDAO.remove(nodeAddress);
        this.sf.getCurrentSession().getTransaction().commit();
    }

  @Test
    public void edgeInfoRemove() {
        this.sf.getCurrentSession().beginTransaction();
        String address = CommonParams.getIpAddress();
        EdgeInfoDAO edgeInfoDAO = new EdgeInfoDAO(this.dbname);
        EdgeInfo edgeInfo = (EdgeInfo)
            edgeInfoDAO.queryByParam("externalIP", address);
        edgeInfoDAO.remove(edgeInfo);
        this.sf.getCurrentSession().getTransaction().commit();
    }

  @Test
    public void l2SwitchingCapabilityRemove() {
        this.sf.getCurrentSession().beginTransaction();
        String vlanRange = "any";
        L2SwitchingCapabilityDataDAO dao =
            new L2SwitchingCapabilityDataDAO(this.dbname);
        L2SwitchingCapabilityData capability = (L2SwitchingCapabilityData)
            dao.queryByParam("vlanRangeAvailability", vlanRange);
        dao.remove(capability);
        this.sf.getCurrentSession().getTransaction().commit();
    }

  @Test(dependsOnMethods={ "nodeAddressRemove", "edgeInfoRemove",
                           "l2SwitchingCapabilityRemove" })
    public void domainRemove() {
        this.sf.getCurrentSession().beginTransaction();
        DomainDAO domainDAO = new DomainDAO(this.dbname);
        Domain domain =
                (Domain) domainDAO.queryByParam("topologyIdent",
                                               CommonParams.getIdentifier());
        domainDAO.remove(domain);
        this.sf.getCurrentSession().getTransaction().commit();
    }

  @Test(dependsOnMethods={ "domainRemove" })
    public void cascadingDeletedNode() {
        this.sf.getCurrentSession().beginTransaction();
        NodeDAO nodeDAO = new NodeDAO(this.dbname);
        Node node = (Node) nodeDAO.queryByParam("topologyIdent",
                                               CommonParams.getIdentifier());
        this.sf.getCurrentSession().getTransaction().commit();
        // if cascading delete works with domain testRemove, this will
        // be null
        assert node == null;
    }
  @Test(dependsOnMethods={ "domainRemove" })
    public void cascadingDeletedPort() {
        this.sf.getCurrentSession().beginTransaction();
        PortDAO portDAO = new PortDAO(this.dbname);
        Port port = (Port)
            portDAO.queryByParam("topologyIdent",
                                 CommonParams.getIdentifier());
        this.sf.getCurrentSession().getTransaction().commit();
        // if cascading delete works with domain testRemove, this will
        // be null
        assert port == null;
    }

  @Test(dependsOnMethods={ "domainRemove" })
    public void cascadingDeletedLink() {
        this.sf.getCurrentSession().beginTransaction();
        LinkDAO linkDAO = new LinkDAO(this.dbname);
        Link link = (Link)
            linkDAO.queryByParam("topologyIdent",
                                 CommonParams.getIdentifier());
        this.sf.getCurrentSession().getTransaction().commit();
        // if cascading delete works with domain testRemove, this will
        // be null
        assert link == null;
    }

  @Test(dependsOnMethods={ "domainRemove" })
    public void cascadingDeletedIpaddr() {
        this.sf.getCurrentSession().beginTransaction();
        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);
        Ipaddr ipaddr = (Ipaddr)
            ipaddrDAO.queryByParam("IP", CommonParams.getIpAddress());
        this.sf.getCurrentSession().getTransaction().commit();
        // if cascading delete works with domain testRemove, this will
        // be null
        assert ipaddr == null;
    }

  @Test
    public void pathRemove() {
        this.sf.getCurrentSession().beginTransaction();
        PathDAO dao = new PathDAO(this.dbname);
        PathElemDAO pathElemDAO = new PathElemDAO(this.dbname);
        PathElem pathElem =
            (PathElem) pathElemDAO.queryByParam("description",
                                                "ingress");
        Path path = (Path) dao.queryByParam("pathElemId", pathElem.getId()); 
        dao.remove(path);
        // links created in pathCreate were deleted by cascade
        this.sf.getCurrentSession().getTransaction().commit();
    }

  @Test(dependsOnMethods={ "pathRemove" })
    public void cascadingDeletedPathElem() {
        this.sf.getCurrentSession().beginTransaction();
        PathElemDAO pathElemDAO = new PathElemDAO(this.dbname);
        PathElem ingressPathElem = (PathElem)
            pathElemDAO.queryByParam("description", "ingress");
        PathElem egressPathElem = (PathElem)
            pathElemDAO.queryByParam("description", "egress");
        this.sf.getCurrentSession().getTransaction().commit();
        // if cascading delete works with path testRemove, this will
        // be null
        assert ingressPathElem == null;
        assert egressPathElem == null;
    }

  @Test(dependsOnMethods={ "pathRemove" })
    public void cascadingDeletedLayer2Data() {
        this.sf.getCurrentSession().beginTransaction();
        Layer2DataDAO layer2DataDAO = new Layer2DataDAO(this.dbname);
        Layer2Data layer2Data = (Layer2Data)
            layer2DataDAO.queryByParam("srcEndpoint",
                                       CommonParams.getSrcEndpoint());
        this.sf.getCurrentSession().getTransaction().commit();
        // if cascading delete works with path testRemove, this will
        // be null
        assert layer2Data == null;
    }

  @Test(dependsOnMethods={ "pathRemove" })
    public void cascadingDeletedLayer3Data() {
        this.sf.getCurrentSession().beginTransaction();
        Layer3DataDAO layer3DataDAO = new Layer3DataDAO(this.dbname);
        Layer3Data layer3Data = (Layer3Data)
            layer3DataDAO.queryByParam("srcHost", CommonParams.getSrcHost());
        this.sf.getCurrentSession().getTransaction().commit();
        // if cascading delete works with path testRemove, this will
        // be null
        assert layer3Data == null;
    }

  @Test(dependsOnMethods={ "pathRemove" })
    public void cascadingDeletedMplsData() {
        this.sf.getCurrentSession().beginTransaction();
        MPLSDataDAO mplsDataDAO = new MPLSDataDAO(this.dbname);
        MPLSData mplsData = (MPLSData)
            mplsDataDAO.queryByParam("burstLimit",
                                     CommonParams.getMPLSBurstLimit());
        this.sf.getCurrentSession().getTransaction().commit();
        // if cascading delete works with path testRemove, this will
        // be null
        assert mplsData == null;
    }

}
