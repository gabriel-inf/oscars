package net.es.oscars.bss.topology;


import net.es.oscars.bss.BSSException;

import org.apache.log4j.Logger;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneDomainContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneNodeContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePortContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwcapContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwitchingCapabilitySpecificInfo;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneTopologyContent;

public class TopologyAxis2Importer {
    private Logger log;
    private String dbname;

    public TopologyAxis2Importer(String dbname){
        this.log = Logger.getLogger(this.getClass());
        this.dbname = dbname;
    }

    /**
     * Updates the OSCARS scheduling database with a given topology. It will insert 
     * all local devices and placeholders for their immediate remote neighbors in
     * other domains. The entries for other domains will only contain default values 
     * and should not be used in scheduling.
     *
     * @param topology the topology to add to the database
     */
    public void updateDatabase(CtrlPlaneTopologyContent topology) throws BSSException{
        CtrlPlaneDomainContent[] domains = topology.getDomain();

        DomainDAO domain = new DomainDAO(this.dbname);
        Domain localDomain = domain.getLocalDomain();

        if(localDomain == null){
            throw new BSSException("No local domain in the database");
        }

        String localDomainId = localDomain.getTopologyIdent();

        this.log.debug("updateDatabase.start");
        for(CtrlPlaneDomainContent d : domains){
            String xmlDomainId = this.convertToLocalId(d.getId());
            if(xmlDomainId.equals(localDomainId)){
                this.log.debug("local domain found: " + localDomainId);
                CtrlPlaneNodeContent[] nodes = d.getNode();
                if(nodes == null){
                    continue;
                }
                for(CtrlPlaneNodeContent n : nodes){
                    Node dbNode = this.prepareNodeforDB(n, localDomain);
                    CtrlPlanePortContent[] ports = n.getPort();
                    if(ports == null){
                        continue;
                    }
                    for(CtrlPlanePortContent p : ports){
                        Port dbPort = this.preparePortforDB(p, dbNode);
                        CtrlPlaneLinkContent[] links = p.getLink();
                        if(links == null){
                            continue;
                        }
                        for(CtrlPlaneLinkContent l : links){
                            this.prepareLinkforDB(l, dbPort);
                        }
                        /* Update remote links */
                        for(CtrlPlaneLinkContent l : links){
                            this.updateRemoteLink(l, dbPort);
                        }

                    }
                }
            }
        }

        this.log.debug("updateDatabase.end");
    }

    /**
     * Create a new node or update an existing node given a node element 
     * from the topology schema.
     *
     * @param node the node element from the NMWG XML topology schema
     * @param parent the parent domain of this element in the database
     * @return the updated node
     */
    private Node prepareNodeforDB(CtrlPlaneNodeContent node, Domain parent){
        NodeDAO nodeDAO = new NodeDAO(this.dbname);
        NodeAddressDAO nodeAddrDAO = new NodeAddressDAO(this.dbname);
        Node dbNode = null;
        NodeAddress dbNodeAddress = null;

        /* Convert node ID to local ID */
        String xmlNodeId = this.convertToLocalId(node.getId());
        this.log.debug("NODE: " + xmlNodeId);
        dbNode = nodeDAO.fromTopologyIdent(xmlNodeId, parent);

        /* Check if node already in database */
        if(dbNode == null){
            this.log.debug("node.create - " + xmlNodeId);
            dbNode = new Node();
        }else{
            this.log.debug("node.exists - " + xmlNodeId);
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
        dbNode.setTopologyIdent(xmlNodeId);
        dbNode.setDomain(parent);

        nodeDAO.update(dbNode);
        nodeAddrDAO.update(dbNodeAddress);
        return dbNode;
    }

    /**
     * Create a new port or update an existing port given a port element 
     * from the topology schema.
     *
     * @param port the port element from the NMWG XML topology schema
     * @param parent the parent node of this element in the database
     * @return the updated port
     */
    private Port preparePortforDB(CtrlPlanePortContent port, Node parent){
        PortDAO portDAO = new PortDAO(this.dbname);
        Port dbPort = null;    
        String capacity = null;
        String maxResCapacity = null;
        String minResCapacity = null;
        String unresCapacity = null;

        /* Convert port ID to local ID */
        String xmlPortId = this.convertToLocalId(port.getId());
        dbPort = portDAO.fromTopologyIdent(xmlPortId, parent);

        /* Check if port exists */
        if(dbPort == null){
            this.log.debug("port.create - " + xmlPortId);
            dbPort = new Port();
        }else{
            this.log.debug("port.exists - " + xmlPortId);
        }        

        /* Check for given capacity values */
        capacity = port.getCapacity();
        maxResCapacity = port.getMaximumReservableCapacity();
        minResCapacity = port.getMinimumReservableCapacity();
        unresCapacity = port.getUnreservedCapacity();
        if(capacity != null){
            dbPort.setCapacity(Long.parseLong(capacity));
        }else{
            this.log.warn("port with topology ID " + xmlPortId + 
            " does not contain capacity.  Element was not saved.");
        }

        if(maxResCapacity != null){
            dbPort.setMaximumReservableCapacity(Long.parseLong(maxResCapacity));
        }else{
            dbPort.setMaximumReservableCapacity(Long.parseLong(capacity));
            this.log.warn("port with topology ID " + xmlPortId +
                    " does not contain setMaximumReservableCapacity. "+
                    "Capacity value set to " + capacity);
        }

        if(minResCapacity != null){
            dbPort.setMinimumReservableCapacity(Long.parseLong(minResCapacity));
        }else{
            dbPort.setMinimumReservableCapacity(Long.parseLong(capacity));
            this.log.warn("port with topology ID " + xmlPortId +
                    " does not contain MinimumReservableCapacity. "+
                    "Capacity value set to " + capacity);
        }

        if(unresCapacity != null){
            dbPort.setUnreservedCapacity(Long.parseLong(unresCapacity));
        }else{
            dbPort.setUnreservedCapacity(Long.parseLong(capacity));
            this.log.warn("port with topology ID " + xmlPortId +
                    " does not contain UnreservedCapacity. "+
                    "Capacity value set to " + capacity);
        }

        /* Set remaining values */
        dbPort.setValid(true);
        dbPort.setSnmpIndex(1);
        dbPort.setTopologyIdent(xmlPortId);
        dbPort.setGranularity(Long.parseLong(port.getGranularity()));
        dbPort.setAlias(xmlPortId);
        dbPort.setNode(parent);

        portDAO.update(dbPort);

        return dbPort;
    }

    /**
     * Create a new link or update an existing link given a link element 
     * from the topology schema.
     *
     * @param link the link element from the NMWG XML topology schema
     * @param parent the parent port of this element in the database
     * @return the updated link
     */
    private Link prepareLinkforDB(CtrlPlaneLinkContent link, Port parent){
        LinkDAO linkDAO = new LinkDAO(this.dbname);
        Link dbLink = null;
        String capacity = null;
        String maxResCapacity = null;
        String minResCapacity = null;
        String unresCapacity = null;

        /* Convert link ID to local ID */
        String xmlLinkId = this.convertToLocalId(link.getId());
        dbLink = linkDAO.fromTopologyIdent(xmlLinkId, parent);

        /* Check if link exists */
        if(dbLink == null){
            this.log.debug("link.create - " + xmlLinkId);
            dbLink = new Link();
        }else{
            this.log.debug("link.exists - " + xmlLinkId);
        }

        /* Check for given capacity values */
        capacity = link.getCapacity();
        maxResCapacity = link.getMaximumReservableCapacity();
        minResCapacity = link.getMinimumReservableCapacity();
        unresCapacity = link.getUnreservedCapacity();
        if(capacity != null){
            dbLink.setCapacity(Long.parseLong(capacity));
        }else{
            this.log.warn("link with topology ID " + xmlLinkId +
            " does not contain capacity.  Element was not saved.");
        }

        if(maxResCapacity != null){
            dbLink.setMaximumReservableCapacity(Long.parseLong(maxResCapacity));
        }else{
            dbLink.setMaximumReservableCapacity(Long.parseLong(capacity));
            this.log.warn("link with topology ID " + xmlLinkId +
                    " does not contain MaximumReservableCapacity. "+
                    "Capacity value set to " + capacity);
        }

        if(minResCapacity != null){
            dbLink.setMinimumReservableCapacity(Long.parseLong(minResCapacity));
        }else{
            dbLink.setMinimumReservableCapacity(Long.parseLong(capacity));
            this.log.warn("link with topology ID " + xmlLinkId +
                    " does not contain MinimumReservableCapacity. "+
                    "Capacity value set to " + capacity);
        }

        if(unresCapacity != null){
            dbLink.setUnreservedCapacity(Long.parseLong(unresCapacity));
        }else{
            dbLink.setUnreservedCapacity(Long.parseLong(capacity));
            this.log.warn("link with topology ID " + xmlLinkId +
                    " does not contain UnreservedCapacity. "+
                    "Capacity value set to " + capacity);
        }

        /* Set remaining values */
        dbLink.setValid(true);
        dbLink.setSnmpIndex(1);
        dbLink.setTopologyIdent(xmlLinkId);
        dbLink.setTrafficEngineeringMetric(link.getTrafficEngineeringMetric());
        dbLink.setGranularity(Long.parseLong(link.getGranularity()));
        dbLink.setAlias(xmlLinkId);
        dbLink.setPort(parent);

        linkDAO.update(dbLink);

        /* Set switching capability info */
        CtrlPlaneSwcapContent swcap = link.getSwitchingCapabilityDescriptors();
        if(swcap != null){
            this.prepareSwcapforDB(swcap, dbLink);
        }

        return dbLink;
    }

    /**
     * Updates the remoteLinkId field of a link entry in the database.
     *
     * @param link the link element from the NMWG XML topology schema
     * @param parent the parent port of the link element in the database
     */
    private void updateRemoteLink(CtrlPlaneLinkContent link, Port parent){
        LinkDAO linkDAO = new LinkDAO(this.dbname);
        String linkId = this.convertToLocalId(link.getId());
        String remoteLinkId =link.getRemoteLinkId();
        Link dbLink = linkDAO.fromTopologyIdent(linkId, parent);
        Link dbRemoteLink = null;

        if(remoteLinkId != null){
            dbRemoteLink = this.urnToLink(remoteLinkId, dbLink);
        }

        dbLink.setRemoteLink(dbRemoteLink);

        linkDAO.update(dbLink);

        return;
    }

    /**
     * Updates the l2SwitchingCapabilityData table in the database
     *
     * @param swcap the CtrlPlaneSwcapContent element from the NMWG XML topology schema
     * @param parent the parent link of the CtrlPlaneSwcapContent element in the database
     */
    private void prepareSwcapforDB(CtrlPlaneSwcapContent swcap, Link parent){
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

            swcapDAO.update(dbL2SwcapData);
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
     * return the database entry of the link referenced by linkId
     */
    private Link urnToLink(String linkId, Link remoteLink){
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
                domainDAO.update(domain);
            }

            Node node = nodeDAO.fromTopologyIdent(topoIds[4], domain);
            if(node == null){
                this.log.debug("Creating new node for " + linkId);
                node = new Node();
                node.setTopologyIdent(topoIds[4]);
                node.setDomain(domain);
                nodeDAO.update(node);
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
                portDAO.update(port);
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
                link.setTrafficEngineeringMetric("100");
                link.setValid(true);
                linkDAO.update(link);
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
