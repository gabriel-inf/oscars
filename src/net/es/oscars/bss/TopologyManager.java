package net.es.oscars.bss;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

import org.apache.log4j.*;
import org.hibernate.*;

import net.es.oscars.bss.events.EventProducer;
import net.es.oscars.bss.events.OSCARSEvent;
import net.es.oscars.bss.topology.*;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.wsdlTypes.PathInfo;

/**
 * This class contains methods to update the topology database, given
 * possibly new topology information.  To do so, it must
 *
 * 1) Compare the new information with existing topology information to
 *    see if the existing information is still valid.  Remove all invalidated
 *    node and port rows.
 *
 * 2) Save new topology information in the database if it doesn't exist in
 *    the old topology.
 *
 * 3) Recalculate the paths for all pending reservations.  If a path then
 *    violates policy by oversubscription or other means, the reservation is
 *    marked invalid, and the old path remains associated with the reservation.
 *
 * 4) Recalculate the paths for all active reservations.  If the path
 *    violates policy or is not the same after recalculation, the reservation
 *    is marked invalid, and the old path remains associated with the
 *    reservation.
 *
 * 5) Remove all invalidated ipaddrs that are not part of any reservation's
 *    path.  Remove all paths that are no longer associated with any
 *    reservation.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class TopologyManager {
    private Logger log;
    private SessionFactory sf;
    private String dbname;
    private PathManager pathMgr;
    private String localDomain;


    private DomainDAO domainDAO;
    private NodeDAO nodeDAO;
    private NodeAddressDAO nodeAddrDAO;
    private PortDAO portDAO;
    private LinkDAO linkDAO;
    private IpaddrDAO ipaddrDAO;
    private L2SwitchingCapabilityDataDAO l2swcapDAO;
    private HashMap<String, Link> validLinks;


    public TopologyManager(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.dbname = dbname;

        this.sf = HibernateUtil.getSessionFactory(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        this.domainDAO = new DomainDAO(this.dbname);
        String localdomain = domainDAO.getLocalDomain().getTopologyIdent();
        this.setLocalDomain(localdomain);
        this.sf.getCurrentSession().getTransaction().commit();

        this.nodeDAO = new NodeDAO(this.dbname);
        this.nodeAddrDAO = new NodeAddressDAO(this.dbname);
        this.portDAO = new PortDAO(this.dbname);
        this.linkDAO = new LinkDAO(this.dbname);
        this.ipaddrDAO = new IpaddrDAO(this.dbname);
        this.l2swcapDAO = new L2SwitchingCapabilityDataDAO(this.dbname);
        this.validLinks = new HashMap<String, Link>();

    }

    public void updateTopology(Topology topology, Hashtable<String, String> remoteLinkFQTIMap) {

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        this.log.info("updateDomains.start");
        this.sf.getCurrentSession().beginTransaction();
        try {
            // step 1
            this.log.info("merging with current topology");
            this.mergeTopology(topology, remoteLinkFQTIMap);
            this.log.info("finished merging with current topology");

            // step 2
           this.log.info("recalculating pending paths");
           // TODO: currently broken, must fix!
//           this.recalculatePaths("PENDING");
           this.log.info("recalculated pending paths");

           // step 3
           this.log.info("recalculating active paths");
           // TODO: currently broken, must fix!
//           this.recalculatePaths("ACTIVE");
           this.log.info("recalculated active paths");

           // step 4
//           this.clean();
        } catch (BSSException e) {
           this.sf.getCurrentSession().getTransaction().rollback();
           this.log.error("updateDomains: " + e.getMessage());
           e.printStackTrace(pw);
           this.log.debug("error: "+e.getMessage());
           this.log.debug(sw.toString());
           this.log.error(sw.toString());
           System.exit(-1);
        } catch (Exception e) {
           this.sf.getCurrentSession().getTransaction().rollback();
           this.log.error("updateDomains exception: " + e.getMessage());
           e.printStackTrace(pw);
           this.log.error(sw.toString());
           this.log.debug("error: "+e.getMessage());
           this.log.debug(sw.toString());
           System.exit(-1);
        }
        this.sf.getCurrentSession().getTransaction().commit();
        this.log.info("updateDom.finish");
    }

    private void mergeTopology(Topology newTopology, Hashtable<String, String> remoteLinkFQTIMap) throws BSSException {
        //        Domain domain = this.queryByParam("topologyIdent", topologyIdent);

        this.log.debug("mergeTopology.start");
        List<Domain> currentDomains = domainDAO.list();

        Topology savedTopology = new Topology();
        savedTopology.setDomains(currentDomains);

        Hashtable<String, Hashtable<String, String>> validLinkInfo = new Hashtable<String, Hashtable<String, String>>();
        for (String thisFqti : remoteLinkFQTIMap.keySet()) {
            String remFqti = remoteLinkFQTIMap.get(thisFqti);
            if (!validLinkInfo.keySet().contains(thisFqti)) {
                validLinkInfo.put(thisFqti, URNParser.parseTopoIdent(thisFqti));
            }
            if (!validLinkInfo.keySet().contains(remFqti)) {
                validLinkInfo.put(remFqti, URNParser.parseTopoIdent(remFqti));
            }
        }

        ArrayList<Domain> domainsToInsert = new ArrayList<Domain>();
        ArrayList<Domain> domainsToUpdate = new ArrayList<Domain>();
        for (Domain newDomain : newTopology.getDomains()) {
            boolean found = false;
            Domain foundDomain = null;
            for (Domain savedDomain : currentDomains) {
                if (savedDomain.equalsTopoId(newDomain)) {
                    found = true;
                    foundDomain = savedDomain;
                    foundDomain.setLocal(newDomain.isLocal());
                    // here is where we'd overwrite stuff
                    break;
                }
            }
            if (!found) {
                domainsToInsert.add(newDomain);
            } else {
                domainsToUpdate.add(foundDomain);
            }
        }
        for (Domain domain : domainsToInsert) {
            domainDAO.create(domain);
            savedTopology.addDomain(domain);
            this.log.debug("Added domain: "+domain.getFQTI());
        }

        for (Domain domain : domainsToUpdate) {
            domainDAO.update(domain);
            this.log.debug("Updated domain: "+domain.getFQTI());
        }
        savedTopology.setDomains(domainDAO.list());

        // Now that everything is saved, merge domains
        for (Domain savedDomain : savedTopology.getDomains()) {
            for (Domain newDomain : newTopology.getDomains()) {
                if (newDomain.equalsTopoId(savedDomain)) {
                    this.mergeNodes(savedDomain, newDomain, validLinkInfo);
                }
            }
        }
        this.mergeRemoteLinks(remoteLinkFQTIMap);
        this.log.debug("mergeTopology.end");
    }

    private void mergeNodes(Domain savedDomain, Domain newDomain, Hashtable<String, Hashtable<String, String>> validLinkInfo) {

        this.log.debug("mergeNodes start");
        this.log.debug("Merging nodes for: ["+savedDomain.getFQTI()+"]");

        ArrayList<Node> nodesToInsert = new ArrayList<Node>();
        ArrayList<Node> nodesToUpdate = new ArrayList<Node>();
        ArrayList<Node> nodesToInvalidate = new ArrayList<Node>();

        ArrayList<NodeAddress> nodeAddrsToInsert = new ArrayList<NodeAddress>();
        ArrayList<NodeAddress> nodeAddrsToUpdate = new ArrayList<NodeAddress>();
        ArrayList<NodeAddress> nodeAddrsToDelete = new ArrayList<NodeAddress>();

        Iterator savedNodeIt;
        Iterator newNodeIt;

        // Check for adding and updating
        if (newDomain.getNodes() == null) {
            // no adding or updating.
        } else {
            newNodeIt = newDomain.getNodes().iterator();
            while (newNodeIt.hasNext()) {
                Node newNode = (Node) newNodeIt.next();
                Node foundNode = savedDomain.getNodeByTopoId(newNode.getTopologyIdent());
                NodeAddress newNodeAddr = newNode.getNodeAddress();
                if (foundNode == null) {
                    this.log.debug("Will add node: "+newNode.getFQTI());
                    nodesToInsert.add(newNode);
                    if (newNodeAddr != null) {
                        newNodeAddr.setNode(newNode);
                        newNode.setNodeAddress(newNodeAddr);
                        nodeAddrsToInsert.add(newNodeAddr);
                    }
                } else {
                    NodeAddress foundNodeAddr = foundNode.getNodeAddress();
                    this.log.debug("Will update node: "+newNode.getFQTI());
                    foundNode.setValid(newNode.isValid());
                    nodesToUpdate.add(foundNode);

                    if (newNodeAddr != null) {
                        if (foundNodeAddr != null) {
                            //  update existing node address
                            foundNodeAddr.setNode(foundNode);
                            foundNode.setNodeAddress(foundNodeAddr);
                            foundNodeAddr.setAddress(newNodeAddr.getAddress());
                            nodeAddrsToUpdate.add(foundNodeAddr);
                        } else {
                            // insert the new one
                            newNodeAddr.setNode(foundNode);
                            foundNode.setNodeAddress(newNodeAddr);
                            nodeAddrsToInsert.add(newNodeAddr);
                        }
                    } else {
                        if (foundNodeAddr != null) {
                            nodeAddrsToDelete.add(foundNodeAddr);
                        } else {
                            // both null, nothing to do
                        }
                    }
                }
            }
        }
        // Check for invalidation
        if (savedDomain.getNodes() == null) {
            // nothing to invalidate.
        } else {
            savedNodeIt = savedDomain.getNodes().iterator();
            while (savedNodeIt.hasNext()) {
                boolean found = false;
                Node savedNode = (Node) savedNodeIt.next();
                Node foundNode = newDomain.getNodeByTopoId(savedNode.getTopologyIdent());
                // not in new topology structure
                if (foundNode == null) {
                    boolean invalidate = true;
                    // don't invalidate if in external remote links though!
                    for (String fqti : validLinkInfo.keySet()) {
                        Hashtable<String, String> results = validLinkInfo.get(fqti);
                        String domainId = results.get("domainId");
                        String nodeId = results.get("nodeId");
                        String portId = results.get("portId");
                        String linkId = results.get("linkId");
                        if (domainId.equals(savedDomain.getTopologyIdent())) {
                            if (nodeId.equals(savedNode.getTopologyIdent())) {
                                savedNode.setValid(true);
                                invalidate = false;
                            }
                        }
                    }
                    if (invalidate && savedNode.isValid()) {
                        this.log.debug("Will invalidate node: "+savedNode.getFQTI());
                        nodesToInvalidate.add(savedNode);
                    }
                }
            }
        }

        this.log.debug("Adding nodes...");
        for (Node node : nodesToInsert) {
            savedDomain.addNode(node);
            try {
                nodeDAO.create(node);
            } catch (Exception ex) {
                this.log.debug("Error: "+ex.getMessage());
            }
        }
        this.log.debug("Adding nodes finished.");

        this.log.debug("Updating nodes...");
        for (Node node : nodesToUpdate) {
            try {
                nodeDAO.update(node);
            } catch (Exception ex) {
                this.log.debug("Error: "+ex.getMessage());
            }
        }
        this.log.debug("Updating nodes finished.");

        this.log.debug("Invalidating nodes...");
        for (Node node : nodesToInvalidate) {
            this.invalidateNode(node);
        }
        this.log.debug("Invalidating nodes finished.");

        this.log.debug("Adding node addresses...");
        for (NodeAddress nodeAddr : nodeAddrsToInsert) {
            try {
                nodeAddrDAO.create(nodeAddr);
            } catch (Exception ex) {
                this.log.debug("Error: "+ex.getMessage());
            }
        }
        this.log.debug("Adding node addresses finished.");

        this.log.debug("Updating node addresses...");
        for (NodeAddress nodeAddr : nodeAddrsToUpdate) {
            try {
                nodeAddrDAO.update(nodeAddr);
            } catch (Exception ex) {
                this.log.debug("Error: "+ex.getMessage());
            }
        }
        this.log.debug("Updating node addresses finished.");

        this.log.debug("Removing stale node addresses...");
        for (NodeAddress nodeAddr : nodeAddrsToDelete) {
            try {
                nodeAddrDAO.remove(nodeAddr);
            } catch (Exception ex) {
                this.log.debug("Error: "+ex.getMessage());
            }
        }
        this.log.debug("Removing stale node addresses finished.");

        savedNodeIt = savedDomain.getNodes().iterator();
        while (savedNodeIt.hasNext()) {
            Node savedNode = (Node) savedNodeIt.next();

            Node newNode = newDomain.getNodeByTopoId(savedNode.getTopologyIdent());
            if (newNode != null) {
                this.mergePorts(savedNode, newNode, validLinkInfo);
            }
        }
        this.log.debug("Merging nodes for: ["+savedDomain.getFQTI()+"] finished.");
        this.log.debug("mergeNodes end");
        return;
    }

    private void mergePorts(Node savedNode, Node newNode, Hashtable<String, Hashtable<String, String>> validLinkInfo) {
        this.log.debug("mergePorts start");

        ArrayList<Port> portsToInsert = new ArrayList<Port>();
        ArrayList<Port> portsToUpdate = new ArrayList<Port>();
        ArrayList<Port> portsToInvalidate = new ArrayList<Port>();

        Iterator savedPortIt;
        Iterator newPortIt;

        // Check for adding and updating
        if (newNode.getPorts() == null) {
            // no adding or updating.
        } else {
            newPortIt = newNode.getPorts().iterator();
            while (newPortIt.hasNext()) {
                Port newPort = (Port) newPortIt.next();
                Port foundPort = savedNode.getPortByTopoId(newPort.getTopologyIdent());
                if (foundPort == null) {
                    this.log.debug("Will add port: "+newPort.getFQTI());
                    portsToInsert.add(newPort);
                } else {
                    this.log.debug("Will update port: "+newPort.getFQTI());
                    foundPort.setValid(newPort.isValid()); // true!
                    foundPort.setAlias(newPort.getAlias());
                    foundPort.setCapacity(newPort.getCapacity());
                    foundPort.setGranularity(newPort.getGranularity());
                    foundPort.setMaximumReservableCapacity(newPort.getMaximumReservableCapacity());
                    foundPort.setMinimumReservableCapacity(newPort.getMinimumReservableCapacity());
                    foundPort.setSnmpIndex(newPort.getSnmpIndex());
                    foundPort.setUnreservedCapacity(newPort.getUnreservedCapacity());

                    portsToUpdate.add(foundPort);
                }
            }
        }
        this.log.debug("Checking for invalidation...");
        // Check for invalidation
        if (savedNode.getPorts() == null) {
            // nothing to invalidate.
        } else {
            savedPortIt = savedNode.getPorts().iterator();
            while (savedPortIt.hasNext()) {
                boolean found = false;
                Port savedPort = (Port) savedPortIt.next();
                Port foundPort = newNode.getPortByTopoId(savedPort.getTopologyIdent());
                if (foundPort == null) {
                    boolean invalidate = true;

                    // don't invalidate if in external remote links though!
                    for (String fqti : validLinkInfo.keySet()) {
                        Hashtable<String, String> results = validLinkInfo.get(fqti);
                        String domainId = results.get("domainId");
                        String nodeId = results.get("nodeId");
                        String portId = results.get("portId");
                        String linkId = results.get("linkId");
                        if (domainId.equals(savedNode.getDomain().getTopologyIdent())) {
                            if (nodeId.equals(savedNode.getTopologyIdent())) {
                                if (portId.equals(savedPort.getTopologyIdent())) {
                                    savedPort.setValid(true);
                                    invalidate = false;
                                }
                            }
                        }
                    }
                    if (invalidate && savedPort.isValid()) {
                        this.log.debug("Will invalidate port: "+savedPort.getFQTI());
                        portsToInvalidate.add(savedPort);
                    }
                }
            }
        }
        this.log.debug("Checking for invalidation finished.");

        this.log.debug("Adding ports...");
        for (Port port : portsToInsert) {
            savedNode.addPort(port);
            try {
                portDAO.create(port);
            } catch (Exception ex) {
                this.log.debug("Error: "+ex.getMessage());
            }
        }
        this.log.debug("Adding ports finished.");

        this.log.debug("Updating ports...");
        for (Port port : portsToUpdate) {
            try {
                portDAO.update(port);
            } catch (Exception ex) {
                this.log.debug("Error: "+ex.getMessage());
            }
        }
        this.log.debug("Updating ports finished.");


        this.log.debug("Invalidating ports...");
        for (Port port : portsToInvalidate) {
            this.invalidatePort(port);
        }
        this.log.debug("Invalidating ports finished.");

        savedPortIt = savedNode.getPorts().iterator();
        while (savedPortIt.hasNext()) {
            Port savedPort = (Port) savedPortIt.next();
            Port newPort = newNode.getPortByTopoId(savedPort.getTopologyIdent());
            if (newPort!= null) {
                this.mergeLinks(savedPort, newPort, validLinkInfo);
            }
        }
        this.log.debug("mergePorts.end");
    }



    private void mergeLinks(Port savedPort, Port newPort, Hashtable<String, Hashtable<String, String>> validLinkInfo) {
        this.log.debug("mergeLinks.start");

        ArrayList<Link> linksToInsert = new ArrayList<Link>();
        ArrayList<Link> linksToUpdate = new ArrayList<Link>();
        ArrayList<Link> linksToInvalidate = new ArrayList<Link>();

        Iterator savedLinkIt;
        Iterator newLinkIt;

        // Check for adding and updating
        if (newPort.getLinks() == null) {
            // no adding or updating.
        } else {
            newLinkIt = newPort.getLinks().iterator();
            while (newLinkIt.hasNext()) {
                Link newLink = (Link) newLinkIt.next();
                Link foundLink = savedPort.getLinkByTopoId(newLink.getTopologyIdent());
                if (foundLink == null) {
                    this.log.debug("Will add link: "+newLink.getFQTI());
                    newLink.setRemoteLink(null);
                    linksToInsert.add(newLink);
                } else {
                    this.log.debug("Will update link: "+newLink.getFQTI());
                    foundLink.setCapacity(newLink.getCapacity());
                    foundLink.setGranularity(newLink.getGranularity());
                    foundLink.setMaximumReservableCapacity(newLink.getMaximumReservableCapacity());
                    foundLink.setMinimumReservableCapacity(newLink.getMinimumReservableCapacity());
                    foundLink.setAlias(newLink.getAlias());
                    foundLink.setTrafficEngineeringMetric(newLink.getTrafficEngineeringMetric());
                    foundLink.setValid(newLink.isValid()); // true!
                    foundLink.setRemoteLink(null);

                    this.log.debug("Valid link: "+foundLink.getFQTI());
                    this.validLinks.put(foundLink.getFQTI(), foundLink);

                    linksToUpdate.add(foundLink);
                }
            }
        }

        this.log.debug("Checking for invalidation...");
        // Check for invalidation
        if (savedPort.getLinks() == null) {
            // nothing to invalidate.
        } else {
            savedLinkIt = savedPort.getLinks().iterator();
            while (savedLinkIt.hasNext()) {
                boolean found = false;
                Link savedLink = (Link) savedLinkIt.next();
                Link foundLink = newPort.getLinkByTopoId(savedLink.getTopologyIdent());
                if (foundLink == null) {
                    boolean invalidate = true;

                    // don't invalidate if in external remote links though!
                    for (String fqti : validLinkInfo.keySet()) {
                        Hashtable<String, String> results = validLinkInfo.get(fqti);
                        String domainId = results.get("domainId");
                        String nodeId = results.get("nodeId");
                        String portId = results.get("portId");
                        String linkId = results.get("linkId");
                        if (domainId.equals(savedPort.getNode().getDomain().getTopologyIdent())) {
                            if (nodeId.equals(savedPort.getNode().getTopologyIdent())) {
                                if (portId.equals(savedPort.getTopologyIdent())) {
                                    if (linkId.equals(savedLink.getTopologyIdent())) {
                                        savedLink.setValid(true);
                                        invalidate = false;
                                        this.log.debug("Valid link: "+savedLink.getFQTI());
                                        this.validLinks.put(savedLink.getFQTI(), savedLink);
                                    }
                                }
                            }
                        }
                    }
                    if (invalidate && savedLink.isValid()) {
                        this.log.debug("Will invalidate link: "+savedLink.getFQTI());
                        linksToInvalidate.add(savedLink);
                    }
                }
            }
        }
        this.log.debug("Checking for invalidation finished.");

        this.log.debug("Adding links...");
        for (Link link : linksToInsert) {
            savedPort.addLink(link);
            try {
                linkDAO.create(link);
                this.validLinks.put(link.getFQTI(), link);
            } catch (Exception ex) {
                this.log.debug("Error: "+ex.getMessage());
            }
        }
        this.log.debug("Adding links finished.");

        this.log.debug("Updating links...");
        for (Link link : linksToUpdate) {
            try {
                this.validLinks.put(link.getFQTI(), link);
                linkDAO.update(link);
            } catch (Exception ex) {
                this.log.debug("Error: "+ex.getMessage());
            }
        }
        this.log.debug("Updating links finished.");

        this.log.debug("Invalidating links...");
        for (Link link : linksToInvalidate) {
            this.invalidateLink(link);
        }
        this.log.debug("Invalidating links finished.");

        savedLinkIt = savedPort.getLinks().iterator();
        while (savedLinkIt.hasNext()) {
            Link savedLink = (Link) savedLinkIt.next();
            Link newLink = newPort.getLinkByTopoId(savedLink.getTopologyIdent());
            if (newLink != null) {
                this.mergeLinkIpaddrs(savedLink, newLink);
                this.mergeLinkSwcaps(savedLink, newLink);
            }
        }
        this.log.debug("mergeLinks.end");
    }

    private void mergeRemoteLinks(Hashtable<String, String> remoteLinkFQTIMap) {
        this.log.debug("mergeRemoteLinks.start");

        for (String thisFqti : remoteLinkFQTIMap.keySet()) {
            String remFqti = remoteLinkFQTIMap.get(thisFqti);

            Link thisLink = this.validLinks.get(thisFqti);
            Link remoteLink = this.validLinks.get(remFqti);
            if (thisLink == null) {
                this.log.error("oops, tried to merge a nonexistent link");
                continue;
            }

            if (remoteLink == null) {
                remoteLink = this.insertFQTILink(remFqti, thisLink);
            }

            thisLink.setRemoteLink(remoteLink);
            linkDAO.update(thisLink);
        }
        this.log.debug("mergeRemoteLinks.end");
    }

    private void mergeLinkIpaddrs(Link savedLink, Link newLink) {
        this.log.debug("mergeLinkIpaddrs.start");

        ArrayList<Ipaddr> ipaddrsToInsert = new ArrayList<Ipaddr>();
        ArrayList<Ipaddr> ipaddrsToUpdate = new ArrayList<Ipaddr>();
        ArrayList<Ipaddr> ipaddrsToInvalidate = new ArrayList<Ipaddr>();

        Iterator savedIpaddrIt;
        Iterator newIpaddrIt;

        if (newLink.getIpaddrs() == null) {
            // no adding or updating.
        } else {
            newIpaddrIt = newLink.getIpaddrs().iterator();
            while (newIpaddrIt.hasNext()) {
                Ipaddr newIpaddr = (Ipaddr) newIpaddrIt.next();
                Ipaddr foundIpaddr = savedLink.getIpaddrByIP(newIpaddr.getIP());
                if (foundIpaddr == null) {
                    this.log.debug("Will add ipaddr: "+newIpaddr.getIP());
                    newIpaddr.setLink(savedLink);
                    newIpaddr.setValid(true);
                    ipaddrsToInsert.add(newIpaddr);
                } else {
                    this.log.debug("Will update ipaddr: "+newIpaddr.getIP());
                    foundIpaddr.setValid(newIpaddr.isValid()); // true
                    foundIpaddr.setLink(savedLink);
                    ipaddrsToUpdate.add(foundIpaddr);
                }
            }
        }

        this.log.debug("Checking for invalidation...");
        // Check for invalidation
        if (savedLink.getIpaddrs() == null) {
            // nothing to invalidate.
        } else {
            savedIpaddrIt = savedLink.getIpaddrs().iterator();
            while (savedIpaddrIt.hasNext()) {
                boolean found = false;
                Ipaddr savedIpaddr = (Ipaddr) savedIpaddrIt.next();
                Ipaddr foundIpaddr = newLink.getIpaddrByIP(savedIpaddr.getIP());

                if (foundIpaddr == null) {
                    this.log.debug("Will invalidate ipaddr: "+savedIpaddr.getIP());
                    ipaddrsToInvalidate.add(savedIpaddr);
                }
            }
        }
        this.log.debug("Checking for invalidation finished.");

        this.log.debug("Adding ipaddrs...");
        for (Ipaddr ipaddr : ipaddrsToInsert) {
            savedLink.addIpaddr(ipaddr);
            try {
                ipaddrDAO.create(ipaddr);
            } catch (Exception ex) {
                this.log.debug("Error: "+ex.getMessage());
            }
        }
        this.log.debug("Adding ipaddrs finished.");

        this.log.debug("Updating ipaddrs...");
        for (Ipaddr ipaddr : ipaddrsToUpdate) {
            ipaddrDAO.update(ipaddr);
        }
        this.log.debug("Updating ipaddrs  finished.");

        this.log.debug("Invalidating ipaddrs...");
        for (Ipaddr ipaddr : ipaddrsToInvalidate) {
            ipaddr.setValid(false);
            ipaddrDAO.update(ipaddr);
        }
        this.log.debug("Invalidating ipaddrs  finished.");

        linkDAO.update(savedLink);

        this.log.debug("mergeLinkIpaddrs.end");
    }

    private void mergeLinkSwcaps(Link savedLink, Link newLink) {
        this.log.debug("mergeLinkSwcap.start");
        String action = null;

        L2SwitchingCapabilityData savedSwCap = savedLink.getL2SwitchingCapabilityData();
        L2SwitchingCapabilityData newSwCap = newLink.getL2SwitchingCapabilityData();

        if ((savedSwCap == null) && (newSwCap == null)) {
            return;
        } else if ((savedSwCap != null) && (newSwCap == null)) {
            l2swcapDAO.remove(savedSwCap);
            savedLink.setL2SwitchingCapabilityData(null);
            linkDAO.update(savedLink);
        } else if ((savedSwCap == null) && (newSwCap != null)) {
            newSwCap.setLink(savedLink);
            l2swcapDAO.create(newSwCap);
            savedLink.setL2SwitchingCapabilityData(newSwCap);
            linkDAO.update(savedLink);
        } else if ((savedSwCap != null) && (newSwCap != null)) {
            savedSwCap.setVlanRangeAvailability(newSwCap.getVlanRangeAvailability());
            savedSwCap.setInterfaceMTU(newSwCap.getInterfaceMTU());
            l2swcapDAO.update(savedSwCap);
        }
        this.log.debug("mergeLinkSwcap.end");
    }

    private Link insertFQTILink(String linkFqti, Link remoteLink) {

        Link remLink = remoteLink;
        Port remPort = remoteLink.getPort();

        this.log.debug("Deep-creating link: ["+linkFqti+"]");
        Hashtable<String, String> results = URNParser.parseTopoIdent(linkFqti);
        if (results== null || results.get("type") == null || !results.get("type").equals("link")) {
            this.log.error("FQTI is not a link!:" + linkFqti);
            return null;
        }
        String domainId = results.get("domainId");
        String nodeId = results.get("nodeId");
        String portId = results.get("portId");
        String linkId = results.get("linkId");

        List<Domain> currentDomains = domainDAO.list();
        boolean haveDomain = false;
        boolean haveNode = false;
        boolean havePort = false;
        boolean haveLink = false;
        boolean haveL2swcap = false;

        Domain domain = null;
        for (Domain savedDomain : currentDomains) {
            if (savedDomain.getTopologyIdent().equals(domainId)) {
                this.log.debug("found domain "+domainId+" : "+savedDomain.getId());
                haveDomain = true;
                domain = savedDomain;
                break;
            }
        }
        if (!haveDomain) {
            domain = new Domain(true);
            domain.setTopologyIdent(domainId);
            domainDAO.create(domain);
        }
        Node node = null;
        if (haveDomain) {
            node = domain.getNodeByTopoId(nodeId);
        }
        if (node == null) {
            node = new Node(domain, true);
            node.setTopologyIdent(nodeId);
            domain.addNode(node);
            nodeDAO.create(node);
        } else {
            this.log.debug("found node "+nodeId+" : "+node.getId());
            haveNode = true;
            node.setValid(true);
            nodeDAO.update(node);
        }
        Port port = null;
        if (haveNode) {
            port = node.getPortByTopoId(portId);
        }
        if (port == null) {
            port = new Port(node, true);
            port.setTopologyIdent(portId);
            node.addPort(port);
            port.setCapacity(remPort.getCapacity());
            port.setGranularity(remPort.getGranularity());
            port.setMaximumReservableCapacity(remPort.getMaximumReservableCapacity());
            port.setMinimumReservableCapacity(remPort.getMinimumReservableCapacity());
            port.setUnreservedCapacity(port.getMaximumReservableCapacity());
            port.setValid(true);
            portDAO.create(port);
        } else {
            this.log.debug("found port "+portId+" : "+port.getId());
            havePort = true;
            port.setValid(true);
            if (port.getCapacity() == 0L) {
                port.setCapacity(remPort.getCapacity());
                port.setGranularity(remPort.getGranularity());
                port.setMaximumReservableCapacity(remPort.getMaximumReservableCapacity());
                port.setMinimumReservableCapacity(remPort.getMinimumReservableCapacity());
                port.setUnreservedCapacity(port.getMaximumReservableCapacity());
                this.log.debug("Updated port: ["+port.getFQTI()+"]");
            }
            portDAO.update(port);
        }
        Link link = null;
        if (havePort) {
            link = port.getLinkByTopoId(linkId);
        }
        if (link == null) {
            link = new Link(port, true);
            link.setTopologyIdent(linkId);
            link.setRemoteLink(remoteLink);
            port.addLink(link);
            link.setCapacity(port.getCapacity());
            link.setMinimumReservableCapacity(port.getMinimumReservableCapacity());
            link.setMaximumReservableCapacity(port.getMaximumReservableCapacity());
            link.setUnreservedCapacity(port.getUnreservedCapacity());
            link.setGranularity(port.getGranularity());
            linkDAO.create(link);
        } else {
            this.log.debug("found link "+linkId+" : "+link.getId());
            haveLink = true;
            link.setValid(true);
            link.setRemoteLink(remoteLink);
            linkDAO.update(link);
        }

        L2SwitchingCapabilityData l2scd = link.getL2SwitchingCapabilityData();
        if (l2scd != null) {
            this.log.debug("found l2swcap for "+linkId);
            haveL2swcap = true;
            // do nothing
        } else {
            L2SwitchingCapabilityData remL2scd = remLink.getL2SwitchingCapabilityData();
            if (remL2scd != null) {
                l2scd = new L2SwitchingCapabilityData();
                l2scd.setLink(link);
                l2scd.setVlanRangeAvailability(remL2scd.getVlanRangeAvailability());
                l2scd.setInterfaceMTU(remL2scd.getInterfaceMTU());
                l2scd.setVlanTranslation(remL2scd.getVlanTranslation());
                link.setL2SwitchingCapabilityData(l2scd);
                l2swcapDAO.create(l2scd);
                linkDAO.update(link);
            }
        }
        if (!haveDomain) {
            this.log.debug("Created domain: ["+domain.getFQTI()+"]");
        }
        if (!haveNode) {
            this.log.debug("Created node: ["+node.getFQTI()+"]");
        }
        if (!havePort) {
            this.log.debug("Created port: ["+port.getFQTI()+"]");
        }
        if (!haveLink) {
            this.log.debug("Created link: ["+link.getFQTI()+"]");
        }
        if (!haveL2swcap) {
            this.log.debug("Created l2swcap link: ["+link.getFQTI()+"]");
        }
        this.log.debug("Finished with link: ["+linkFqti+"]");
        return link;
    }

    private void invalidateNode(Node nodeDB) {
        NodeDAO nodeDAO = new NodeDAO(this.dbname);
        nodeDB.setValid(false);
        nodeDAO.update(nodeDB);

        Iterator portIt = nodeDB.getPorts().iterator();
        while (portIt.hasNext()) {
            Port portDB = (Port) portIt.next();
            this.invalidatePort(portDB);
        }
    }

    private void invalidatePort(Port portDB) {
        PortDAO portDAO = new PortDAO(this.dbname);
        portDB.setValid(false);

        Iterator linkIt = portDB.getLinks().iterator();
        while (linkIt.hasNext()) {
            Link linkDB = (Link) linkIt.next();
            this.invalidateLink(linkDB);
        }
        portDAO.update(portDB);
    }

    private void invalidateLink(Link linkDB) {
        LinkDAO linkDAO = new LinkDAO(this.dbname);
        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);
        Set<Ipaddr> currentIpaddrs = linkDB.getIpaddrs();

        for (Ipaddr currentIpaddr : currentIpaddrs) {
            currentIpaddr.setValid(false);
            ipaddrDAO.update(currentIpaddr);
        }
        linkDB.setValid(false);
        linkDAO.update(linkDB);
    }

    /**
     * Recalculates the paths for reservations with the given status.
     * If the new path violates policy by oversubscription or other means, the
     * reservation is marked invalid, and the old path remains associated with
     * the reservation.  If the reservation is active and the new path differs
     * in any way, the reservation is marked invalid.
     *
     * @param status string with status of reservations to check
     *
     * @throws BSSException
     */
    private void recalculatePaths(String status) throws BSSException {
        String ingressNodeIP = null;
        String egressNodeIP = null;
        Path path = null;
        Link link = null;
        Ipaddr ipaddr = null;

        ReservationDAO dao = new ReservationDAO(this.dbname);
        List<Reservation> reservations = dao.statusReservations(status);
        for (Reservation r : reservations) {
            Path oldPath = r.getPath(PathType.LOCAL);

            // find old ingress and egress IP's
            // TODO:  this may no longer be necessary
            List<PathElem> pathElems = oldPath.getPathElems();
            link = pathElems.get(0).getLink();
            ipaddr = link.getValidIpaddr();
            if (ipaddr != null) {
                ingressNodeIP = ipaddr.getIP();
            } else {
                ingressNodeIP = null;
            }
            link = pathElems.get(pathElems.size()-1).getLink();
            ipaddr = link.getValidIpaddr();
            if (ipaddr != null) {
                egressNodeIP = ipaddr.getIP();
            } else {
                egressNodeIP = null;
            }

            //TODO:  build layer-specific info from old path in database
            //       assuming only the hops have the possibility of changing
            PathInfo pathInfo = new PathInfo();
            try {
                //FIXME: finds path and checks for oversubscription
                this.pathMgr.calculatePaths(r);
            } catch (BSSException e) {
                String msg = "Reservation invalidated due to oversubscription.";
                EventProducer eventProducer = new EventProducer();
                eventProducer.addEvent(OSCARSEvent.RESV_INVALIDATED, "",
                    "WBUI", r, "", msg);
                this.log.warn("request may be INVALID due to oversubscription: " +
                    r.getGlobalReservationId() );
                dao.update(r);
                continue;
            }
            if (status.equals("PENDING")) {
                r.setPath(path);
                dao.update(r);
            } else if (status.equals("ACTIVE")) {
                if (!this.isDuplicate(oldPath, path)) {
                    String msg = "Reservation invalidated due to changed path.";
                    EventProducer eventProducer = new EventProducer();
                    eventProducer.addEvent(OSCARSEvent.RESV_INVALIDATED, "",
                        "WBUI", r, "", msg);
//                    r.setStatus("INVALIDATED");
                    this.log.warn("INVALIDATED request due to changed path: " +
                        r.getGlobalReservationId() );
                    dao.update(r);
                }
            }
        }
    }

    /**
     * Removes invalidated topology information, except for ipaddrs and their
     * parents associated with non-pending and non-active paths.
     * Removes paths that are no longer associated with any reservation.
     */
    private void clean() {
        this.log.info("clean.start");

        // remove all invalid ipaddrs that are not part of any reservation
        // (ipaddrs associated with pending and active reservations are
        // guaranteed to be valid because of path recalculation)
        this.log.info("removing invalid ipaddrs that are no longer in use");
        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);
        List<Ipaddr> ipaddrs = ipaddrDAO.list();
        PathElemDAO pathElemDAO = new PathElemDAO(this.dbname);
        List<PathElem> pathElems = pathElemDAO.list();
        Set<Ipaddr> ipset = new HashSet<Ipaddr>();

        for (PathElem pathElem : pathElems) {
            Link link = pathElem.getLink();
            Ipaddr ipaddr = link.getValidIpaddr();
            ipset.add(ipaddr);
        }
        LinkDAO linkDAO = new LinkDAO(this.dbname);
        for (Ipaddr ipaddr : ipaddrs) {
            if (!ipaddr.isValid()) {
                Link parent = ipaddr.getLink();

                // remove an address if it is not part of any paths
                // or if there is already an invalid copy of it
                if (!ipset.contains(ipaddr)) {
                    parent.removeIpaddr(ipaddr);
                    ipaddrDAO.remove(ipaddr);
                    linkDAO.update(parent);
                }
            }
        }
        this.log.info("finished removing invalid ipaddrs");

        // remove invalid links that now have no ipaddrs
        PortDAO portDAO = new PortDAO(this.dbname);
        List<Link> links = linkDAO.list();

        for (Link link : links) {
            Port parent = link.getPort();

            if (!link.isValid() && link.getIpaddrs().isEmpty()) {
                parent.removeLink(link);
                linkDAO.remove(link);
                portDAO.update(parent);
            }
        }
        this.log.info("finished removing invalid links");

        // remove invalid ports that now have no ipaddrs
        NodeDAO nodeDAO = new NodeDAO(this.dbname);
        List<Port> ports = portDAO.list();

        for (Port port : ports) {
            Node parent = port.getNode();

            if (!port.isValid() && port.getLinks().isEmpty()) {
                parent.removePort(port);
                portDAO.remove(port);
                nodeDAO.update(parent);
            }
        }
        this.log.info("finished removing invalid ports");

        // remove invalid nodes that now have no ports
        List<Node> nodes = nodeDAO.list();

        for (Node node : nodes) {
            if (!node.isValid() && node.getPorts().isEmpty()) {
                nodeDAO.remove(node);
            }
        }
        this.log.info("finished removing invalid nodes");
        this.log.info("clean.finish");
    }

    /**
     * Checks to see if two paths contain the same information.
     * If layer specific or MPLS-specific information associated with a
     * path is different, the paths are considered different.
     *
     * @param savedPath saved path information
     * @param checkPath unsaved path to check for duplicate
     * @return boolean indicating whether paths are the same
     */
    private boolean isDuplicate(Path savedPath, Path checkPath) {
        this.log.info("isDuplicate.start");

        if (!savedPath.equals(checkPath)) {
            this.log.debug("one path's fields are different");

            return false;
        }
        // first check that paths are the same length
        List<PathElem> savedPathElems = savedPath.getPathElems();
        List<PathElem> checkPathElems = checkPath.getPathElems();
        if (savedPathElems.size() != checkPathElems.size()) {
            this.log.debug("two paths have different lengths");

            return false;
        }
        int ctr = 0;
        // now that know paths are the same length,
        // check each element of the two paths for equality
        for (PathElem pathElem: savedPathElems) {
            if (!pathElem.equals(checkPathElems.get(ctr))) {
                this.log.debug("two paths are different");

                return false;
            }
            ctr++;
        }
        // check to see if the layer-specific information is the same
        Layer2DataDAO layer2DataDAO = new Layer2DataDAO(this.dbname);
        Layer3DataDAO layer3DataDAO = new Layer3DataDAO(this.dbname);
        MPLSDataDAO MPLSDataDAO = new MPLSDataDAO(this.dbname);
        Layer2Data savedLayer2Data = savedPath.getLayer2Data();
        Layer3Data savedLayer3Data = savedPath.getLayer3Data();
        MPLSData savedMPLSData = savedPath.getMplsData();
        Layer2Data checkLayer2Data = checkPath.getLayer2Data();
        Layer3Data checkLayer3Data = checkPath.getLayer3Data();
        MPLSData checkMPLSData = checkPath.getMplsData();

        if ((savedLayer2Data != null) && (checkLayer2Data != null)) {
            if (!savedLayer2Data.equals(checkLayer2Data)) {
                this.log.debug("layer 2 fields are different");
                return false;
            }
        }
        if (((savedLayer2Data == null) && (checkLayer2Data != null)) ||
                ((savedLayer2Data != null) && (checkLayer2Data == null))) {
            this.log.debug("one path is layer 2, the other is not");
            return false;
        }
        if ((savedLayer3Data != null) && (checkLayer3Data != null)) {
            if (!savedLayer3Data.equals(checkLayer3Data)) {
                this.log.debug("layer 3 fields are different");
                return false;
            }
        }
        if (((savedLayer3Data == null) && (checkLayer3Data != null)) ||
                ((savedLayer3Data != null) && (checkLayer3Data == null))) {
            this.log.debug("one path is layer 3, the other is not");
            return false;
        }
        if ((savedMPLSData != null) && (checkMPLSData != null)) {
            if (!savedMPLSData.equals(checkMPLSData)) {
                this.log.debug("MPLS-specific fields are different");
                return false;
            }
        }
        if (((savedMPLSData == null) && (checkMPLSData != null)) ||
                ((savedMPLSData != null) && (checkMPLSData == null))) {
            this.log.debug("one path is MPLS-specific, the other is not");
            return false;
        }
        this.log.info("isDuplicate.finish true");
        return true;
    }

    /**
     * localDomain getter
     * @return the value of localDomain
     */
    public String getLocalDomain() {
        return this.localDomain;
    }

    /**
     * localDomain setter
     * @param domainId The value to be set
     */
    public void setLocalDomain(String domainId) {
        this.localDomain = domainId;
    }
}
