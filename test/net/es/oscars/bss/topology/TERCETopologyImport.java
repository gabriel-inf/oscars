package net.es.oscars.bss.topology;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Properties;

import net.es.oscars.GlobalParams;
import net.es.oscars.PropHandler;
import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.topology.Domain;
import net.es.oscars.bss.topology.DomainDAO;
import net.es.oscars.bss.topology.L2SwitchingCapabilityData;
import net.es.oscars.bss.topology.L2SwitchingCapabilityDataDAO;
import net.es.oscars.bss.topology.Link;
import net.es.oscars.bss.topology.LinkDAO;
import net.es.oscars.bss.topology.Node;
import net.es.oscars.bss.topology.NodeAddress;
import net.es.oscars.bss.topology.NodeDAO;
import net.es.oscars.bss.topology.Port;
import net.es.oscars.bss.topology.PortDAO;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.database.Initializer;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneDomainContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneNodeContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePortContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwcapContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwitchingCapabilitySpecificInfo;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneTopologyContent;
import org.testng.annotations.*;

import edu.internet2.hopi.dragon.terce.ws.service.TEDBFaultMessage;
import edu.internet2.hopi.dragon.terce.ws.service.TERCEStub;
import edu.internet2.hopi.dragon.terce.ws.types.tedb.SelectNetworkTopology;
import edu.internet2.hopi.dragon.terce.ws.types.tedb.SelectNetworkTopologyContent;
import edu.internet2.hopi.dragon.terce.ws.types.tedb.SelectNetworkTopologyResponse;
import edu.internet2.hopi.dragon.terce.ws.types.tedb.SelectNetworkTopologyResponseContent;
import edu.internet2.hopi.dragon.terce.ws.types.tedb.SelectTypes;

@Test(groups={ "staticroute.init" })
public class TERCETopologyImport {
    private Logger log;
    private String dbname;
    private Properties props;
    
    public void setUpClass(){
	PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.domain", true);
        this.log = Logger.getLogger(this.getClass());
        this.dbname = GlobalParams.getReservationTestDBName();
    }
    
    /**
     * Updates the OSCARS scheduling database with a given topology. It will insert 
     * all local devices and place holders for their immediate remote neighbors in
     * other domains. The entries for other domains will only contain default values 
     * and should not be used in scheduling.
     *
     * @param topology the topology to add to the database
     * @throws TEDBFaultMessage 
     * @throws RemoteException 
     */
    @BeforeGroups(groups={ "pathfinder.staticroute", "pathfinder.perfsonar", "pathfinder.terce" })
    @Test
    public void updateDatabase() throws BSSException, RemoteException, TEDBFaultMessage{
	this.setUpClass();
	CtrlPlaneTopologyContent topology = this.getTERCETopology();
        CtrlPlaneDomainContent[] domains = topology.getDomain();
        Initializer initializer = new Initializer();
        ArrayList<String> dbnames = new ArrayList<String>();
        dbnames.add(this.dbname);
        initializer.initDatabase(dbnames);
        Session bss =
            HibernateUtil.getSessionFactory(this.dbname).getCurrentSession();
        bss.beginTransaction();
        
        Domain localDomain = new Domain();
        localDomain.setTopologyIdent(this.props.getProperty("topologyIdent"));
        localDomain.setName("Local Domain");
        localDomain.setAbbrev("local");
        localDomain.setUrl(this.props.getProperty("url"));
        localDomain.setLocal(true);
        bss.save(localDomain);
        
        if(localDomain == null){
            throw new BSSException("No local domain in the database");
        }
        
        String localDomainId = localDomain.getTopologyIdent();
        
        this.log.debug("updateDatabase.start");
        for(CtrlPlaneDomainContent d : domains){
            d.setId(this.convertToLocalId(d.getId()));
            this.log.debug("DOMAIN: " + d.getId());
            if(d.getId().equals(localDomainId)){
                this.log.debug("local domain found: " + localDomainId);
                CtrlPlaneNodeContent[] nodes = d.getNode();
                if(nodes == null){
					    continue;
				}
				for(CtrlPlaneNodeContent n : nodes){
					Node dbNode = this.prepareNodeforDB(n, localDomain, bss);
					CtrlPlanePortContent[] ports = n.getPort();
					if(ports == null){
					    continue;
					}
					for(CtrlPlanePortContent p : ports){
						Port dbPort = this.preparePortforDB(p, dbNode, bss);
						CtrlPlaneLinkContent[] links = p.getLink();
						if(links == null){
					        continue;
					    }
                        for(CtrlPlaneLinkContent l : links){
                            Link dbLink = this.prepareLinkforDB(l, dbPort, bss);
                        }
                        /* Update remote links */
                        for(CtrlPlaneLinkContent l : links){
                            this.updateRemoteLink(l, dbPort, bss);
                        }

					}
				}
            }
        }
        
        bss.getTransaction().commit();
        this.log.debug("updateDatabase.end");
    }
    
    /**
     * Gets topology from TERCE
     * @param url the TERCE URL
     * @return the topology returned by the TERCE 
     */
    public CtrlPlaneTopologyContent getTERCETopology() throws RemoteException, TEDBFaultMessage{
	TERCEStub terce = new TERCEStub("http://127.0.0.1:8080/axis2/services/TERCE");
        SelectNetworkTopology selectTopology = new SelectNetworkTopology();
		SelectNetworkTopologyContent request = new SelectNetworkTopologyContent();
        SelectNetworkTopologyResponse response = null;
        SelectNetworkTopologyResponseContent responseContent = null;
        CtrlPlaneTopologyContent topology = null;

        /* Format Request */
        request.setFrom(SelectTypes.all);
        request.setDatabase("intradomain");
        
        /* Send request and get response*/
        selectTopology.setSelectNetworkTopology(request);
        response = terce.selectNetworkTopology(selectTopology);
        responseContent = response.getSelectNetworkTopologyResponse();
        topology = responseContent.getTopology();
        
        return topology;
    }
    
    /**
     * Create a new node or update an existing node given a node element 
     * from the topology schema.
     *
     * @param node the node element from the NMWG XML topology schema
     * @param parent the parent domain of this element in the database
     * @param bss the hibernate session to be used for this transaction
     * @return the updated node
     */
    private Node prepareNodeforDB(CtrlPlaneNodeContent node, Domain parent, Session bss){
        NodeDAO nodeDAO = new NodeDAO(this.dbname);
        Node dbNode = null;
        NodeAddress dbNodeAddress = null;
        
        /* Convert node ID to local ID */
        node.setId(this.convertToLocalId(node.getId()));
        this.log.debug("NODE: " + node.getId());
        dbNode = nodeDAO.fromTopologyIdent(node.getId(), parent);
        
        /* Check if node already in database */
        if(dbNode == null){
            this.log.debug("node.create - " + node.getId());
            dbNode = new Node();
        }else{
            this.log.debug("node.exists - " + node.getId());
        }
        
        /* Check if node address exists */
        dbNodeAddress = dbNode.getNodeAddress();
        String nodeAddrStr = node.getAddress().getString();
        if(nodeAddrStr == null){ 
            nodeAddrStr = node.getAddress().getValue();
        }
        
        if(dbNodeAddress == null){
            dbNodeAddress = new NodeAddress();
            dbNodeAddress.setAddress(nodeAddrStr);
            dbNodeAddress.setNode(dbNode);
            dbNode.setNodeAddress(dbNodeAddress);
        }else{
            dbNodeAddress.setAddress(nodeAddrStr);
        }
        
        /* Update node values */
        dbNode.setValid(true);
        dbNode.setTopologyIdent(node.getId());
        dbNode.setDomain(parent);
        
        bss.save(dbNode);
        bss.save(dbNodeAddress);
        return dbNode;
    }
    
    /**
     * Create a new port or update an existing port given a port element 
     * from the topology schema.
     *
     * @param port the port element from the NMWG XML topology schema
     * @param parent the parent node of this element in the database
     * @param bss the hibernate session to be used for this transaction
     * @return the updated port
     */
    private Port preparePortforDB(CtrlPlanePortContent port, Node parent, Session bss){
        PortDAO portDAO = new PortDAO(this.dbname);
        String portId = null;
        Port dbPort = null;    
        String capacity = null;
        String maxResCapacity = null;
        String minResCapacity = null;
        String unresCapacity = null;
        
        /* Convert port ID to local ID */
        port.setId(this.convertToLocalId(port.getId()));
        portId = port.getId();
        dbPort = portDAO.fromTopologyIdent(portId, parent);  
        this.log.debug("PORT: " + portId);
        
        /* Check if port exists */
        if(dbPort == null){
            this.log.debug("port.create - " + port.getId());
            dbPort = new Port();
        }else{
            this.log.debug("port.exists - " + port.getId());
        }        
        
        /* Check for given capacity values */
        capacity = port.getCapacity();
        maxResCapacity = port.getMaximumReservableCapacity();
        minResCapacity = port.getMinimumReservableCapacity();
        unresCapacity = port.getUnreservedCapacity();
        if(capacity != null){
            dbPort.setCapacity(Long.parseLong(capacity));
        }else{
            this.log.warn("port with topology ID " + portId + 
            " does not contain capacity.  Element was not saved.");
        }
        
        if(maxResCapacity != null){
            dbPort.setMaximumReservableCapacity(Long.parseLong(maxResCapacity));
        }else{
            dbPort.setMaximumReservableCapacity(Long.parseLong(capacity));
            this.log.warn("port with topology ID " + portId +
                " does not contain setMaximumReservableCapacity. "+
                "Capacity value set to " + capacity);
        }
        
        if(minResCapacity != null){
            dbPort.setMinimumReservableCapacity(Long.parseLong(minResCapacity));
        }else{
            dbPort.setMinimumReservableCapacity(Long.parseLong(capacity));
            this.log.warn("port with topology ID " + portId +
                " does not contain MinimumReservableCapacity. "+
                "Capacity value set to " + capacity);
        }
        
        if(unresCapacity != null){
            dbPort.setUnreservedCapacity(Long.parseLong(unresCapacity));
        }else{
            dbPort.setUnreservedCapacity(Long.parseLong(capacity));
            this.log.warn("port with topology ID " + portId +
                " does not contain UnreservedCapacity. "+
                "Capacity value set to " + capacity);
        }
        
        /* Set remaining values */
        dbPort.setValid(true);
        dbPort.setSnmpIndex(1);
        dbPort.setTopologyIdent(portId);
        dbPort.setGranularity(Long.parseLong(port.getGranularity()));
        dbPort.setAlias(portId);
        dbPort.setNode(parent);
        
        bss.save(dbPort);
         
        return dbPort;
    }
    
    /**
     * Create a new link or update an existing link given a link element 
     * from the topology schema.
     *
     * @param link the link element from the NMWG XML topology schema
     * @param parent the parent port of this element in the database
     * @param bss the hibernate session to be used for this transaction
     * @return the updated link
     */
    private Link prepareLinkforDB(CtrlPlaneLinkContent link, Port parent, Session bss){
        LinkDAO linkDAO = new LinkDAO(this.dbname);
        String linkId = null;
        Link dbLink = null;
        String capacity = null;
        String maxResCapacity = null;
        String minResCapacity = null;
        String unresCapacity = null;
        
        /* Convert link ID to local ID */
        link.setId(this.convertToLocalId(link.getId()));
        linkId = link.getId();
        dbLink = linkDAO.fromTopologyIdent(linkId, parent);
        this.log.debug("LINK: " + linkId);
        
        /* Check if link exists */
        if(dbLink == null){
            this.log.debug("link.create - " + linkId);
            dbLink = new Link();
        }else{
            this.log.debug("link.exists - " + linkId);
        }
        
        /* Check for given capacity values */
        capacity = link.getCapacity();
        maxResCapacity = link.getMaximumReservableCapacity();
        minResCapacity = link.getMinimumReservableCapacity();
        unresCapacity = link.getUnreservedCapacity();
        if(capacity != null){
            dbLink.setCapacity(Long.parseLong(capacity));
        }else{
            this.log.warn("link with topology ID " + linkId +
                " does not contain capacity.  Element was not saved.");
        }
        
        if(maxResCapacity != null){
            dbLink.setMaximumReservableCapacity(Long.parseLong(maxResCapacity));
        }else{
            dbLink.setMaximumReservableCapacity(Long.parseLong(capacity));
            this.log.warn("link with topology ID " + linkId +
                " does not contain MaximumReservableCapacity. "+
                "Capacity value set to " + capacity);
        }
        
        if(minResCapacity != null){
            dbLink.setMinimumReservableCapacity(Long.parseLong(minResCapacity));
        }else{
            dbLink.setMinimumReservableCapacity(Long.parseLong(capacity));
            this.log.warn("link with topology ID " + linkId +
                " does not contain MinimumReservableCapacity. "+
                "Capacity value set to " + capacity);
        }
        
        if(unresCapacity != null){
            dbLink.setUnreservedCapacity(Long.parseLong(unresCapacity));
        }else{
            dbLink.setUnreservedCapacity(Long.parseLong(capacity));
            this.log.warn("link with topology ID " + linkId +
                " does not contain UnreservedCapacity. "+
                "Capacity value set to " + capacity);
        }
        
        /* Set remaining values */
        dbLink.setValid(true);
        dbLink.setSnmpIndex(1);
        dbLink.setTopologyIdent(linkId);
        dbLink.setTrafficEngineeringMetric(link.getTrafficEngineeringMetric());
        dbLink.setGranularity(Long.parseLong(link.getGranularity()));
        dbLink.setAlias(linkId);
        dbLink.setPort(parent);
        
        bss.save(dbLink);
        
        /* Set switching capability info */
        CtrlPlaneSwcapContent swcap = link.getSwitchingCapabilityDescriptors();
        if(swcap != null){
            this.prepareSwcapforDB(swcap, dbLink, bss);
        }
        
        return dbLink;
    }
    
    /**
     * Updates the remoteLinkId field of a link entry in the database.
     *
     * @param link the link element from the NMWG XML topology schema
     * @param parent the parent port of the link element in the database
     * @param bss the hibernate session to be used for this transaction
     */
    private void updateRemoteLink(CtrlPlaneLinkContent link, Port parent,  Session bss){
        LinkDAO linkDAO = new LinkDAO(this.dbname);
        String linkId = link.getId();
        String remoteLinkId =link.getRemoteLinkId();
        Link dbLink = linkDAO.fromTopologyIdent(linkId, parent);
        Link dbRemoteLink = null;
        
        if(remoteLinkId != null){
            dbRemoteLink = this.urnToLink(remoteLinkId, dbLink, bss);
        }
        
        dbLink.setRemoteLink(dbRemoteLink);
        
        bss.save(dbLink);
        
        return;
    }
    
    /**
     * Updates the l2SwitchingCapabilityData table in the database
     *
     * @param swcap the CtrlPlaneSwcapContent element from the NMWG XML topology schema
     * @param parent the parent link of the CtrlPlaneSwcapContent element in the database
     * @param bss the hibernate session to be used for this transaction
     */
    private void prepareSwcapforDB(CtrlPlaneSwcapContent swcap, Link parent,  Session bss){
        CtrlPlaneSwitchingCapabilitySpecificInfo specInfo = 
            swcap.getSwitchingCapabilitySpecificInfo();
        String switchingcapType = swcap.getSwitchingcapType().toLowerCase();
        
        if(switchingcapType.equals("l2sc")){
            L2SwitchingCapabilityDataDAO swcapDAO = new L2SwitchingCapabilityDataDAO(this.dbname);
            L2SwitchingCapabilityData dbL2SwcapData = parent.getL2SwitchingCapabilityData();
            if(dbL2SwcapData == null){
                dbL2SwcapData = new L2SwitchingCapabilityData();
            }
            
            dbL2SwcapData.setLink(parent);
            dbL2SwcapData.setVlanRangeAvailability(
                specInfo.getVlanRangeAvailability());
            dbL2SwcapData.setInterfaceMTU(specInfo.getInterfaceMTU());
            dbL2SwcapData.setVlanTranslation(specInfo.getVlanTranslation());
            
            bss.save(dbL2SwcapData);
        }
        
        return;
    }
    
    /* Converts a given fully qualified link ID to an entry in the database.
     * It will create the domain, node, port, and/or link if it does not exist
     * and set default values for it. It is different than 
     * net.es.oscars.bss.topology.DomainDAO.set/getFullyQualifiedLink because
     * it will create the elements that don't exist.
     * 
     * @param linkId the fully qualified ID of a link
     * @param remoteLink the remote link of this element
     * @param bss the hibernate session to be used for any transactions
     * return the database entry of the link referenced by linkId
     */
    private Link urnToLink(String linkId, Link remoteLink, Session bss){
        /* Check if edge link */
        if(linkId.equals("urn:ogf:network:domain=*:node=*:port=*:link=*")){
            return null;
        }
        linkId = linkId.replace("domain=","");
        linkId = linkId.replace("node=","");
        linkId = linkId.replace("port=","");
        linkId = linkId.replace("link=","");
        
        DomainDAO domainDAO = new DomainDAO(this.dbname);
        NodeDAO nodeDAO = new NodeDAO(this.dbname);
        PortDAO portDAO = new PortDAO(this.dbname);
        LinkDAO linkDAO = new LinkDAO(this.dbname);
        String[] topoIds = linkId.split(":");
        Link link = null;
        if(topoIds.length == 7){
            /* Could use domainDAO.getfullyQulaifiedLink but
              need to go through and verify each element exists */
            Domain domain = domainDAO.fromTopologyIdent(topoIds[3]);
            if(domain == null){
                this.log.debug("Creating new domain for " + linkId);
                domain = new Domain(true);
                domain.setTopologyIdent(topoIds[3]);
                bss.save(domain);
            }
            
            Node node = nodeDAO.fromTopologyIdent(topoIds[4], domain);
            if(node == null){
                this.log.debug("Creating new node for " + linkId);
                node = new Node();
                node.setTopologyIdent(topoIds[4]);
                node.setDomain(domain);
                bss.save(node);
            }
            
            Port port = portDAO.fromTopologyIdent(topoIds[5], node);
            if(port == null){
                this.log.debug("Creating new port for " + linkId);
                port = new Port();
                port.setTopologyIdent(topoIds[5]);
                port.setNode(node);
                port.setCapacity(new Long(0));
                port.setMaximumReservableCapacity(new Long(0));
                port.setMinimumReservableCapacity(new Long(0));
                port.setUnreservedCapacity(new Long(0));
                port.setValid(false);
                port.setSnmpIndex(1);
                port.setGranularity(new Long(0));
                port.setAlias(topoIds[5]);
                bss.save(port);
            }
            
            link = linkDAO.fromTopologyIdent(topoIds[6], port);
            if(link == null){
                this.log.debug("Creating new link for " + linkId);
                link = new Link();
                link.setTopologyIdent(topoIds[6]);
                link.setPort(port);
                link.setCapacity(new Long(0));
                link.setMaximumReservableCapacity(new Long(0));
                link.setMinimumReservableCapacity(new Long(0));
                link.setUnreservedCapacity(new Long(0));
                link.setRemoteLink(remoteLink);
                bss.save(link);
            }
        }else{
            this.log.warn("invalid remote link id " + linkId +
                ". Continuing with NULL link id.");
        }
        
        return link;
    }
    
    /**
     * Extracts the local portion of a given id
     *
     * @param id the id to convert
     * @returns the local identifier
     */
    public String convertToLocalId(String id){
        String[] componentList = id.split(":");
        return componentList[componentList.length-1].replaceAll(".+\\=", "");
    }
}
