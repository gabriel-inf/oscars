package net.es.oscars.bss.topology;

import net.es.oscars.*;
import net.es.oscars.bss.*;
import net.es.oscars.bss.topology.*;
import net.es.oscars.database.*;
import net.es.oscars.wsdlTypes.*;

import org.apache.log4j.*;

import org.hibernate.*;

import org.hibernate.cfg.*;

import org.jdom.*;

import java.io.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
* This class contains methods that will parse a topology XML instance and
* return a Topology element suitable for inserting into a local topology
* database.
*
* @author Evangelos Chaniotakis (haniotak@es.net)
* @author Aaron Brown (aaron@internet2.edu)
*/
public class TopologyXMLParser {

    private Logger log;
    private Namespace ns;
    private Properties props;

    private String localDomainId;

    private Topology topology;

    private Hashtable<String, String> remoteLinkMap;
    private Hashtable<String, Object> elementMap;

    /**
     * Constructor initializes logging and local properties
     */
    public TopologyXMLParser(Namespace ns) {
        this.log = Logger.getLogger(this.getClass());
        this.ns = ns;
        this.remoteLinkMap = new Hashtable<String, String>();
        this.elementMap = new Hashtable<String, Object>();
    }


    /**
     * This method will convert a Topology instance in a JDOM structure into
     * objects appropriate for insertion into the local OSCARS topology
     * database.
     *
     * @param topology the XML element for the "topology" element
     * @param operation the operation to perform (currently ignored)
     */
    public Topology parse(Element topology, String operation) {
        // TODO: support different operations
        this.log.debug("parsing domains");

        Topology retTopology = new Topology();

        List<Element> children = this.getElementChildren(topology, "domain");
        for (Element child : children) {
            // construct the domain topology identifier
            String domainId = child.getAttributeValue("id");

            // clean it up
            String domTopoIdent = domainId;

            domainId = TopologyUtil.getLSTI(domainId, "Domain");

            this.log.debug("  Got domain, id: [" + domTopoIdent + "]");

            Domain domDB = new Domain(true);

            domDB.setTopologyIdent(domTopoIdent);
            domDB.setName(domainId);

            // read in Node information for this Domain
            this.parseNodes(child, domDB);

            retTopology.addDomain(domDB);
        }

        // Now we have read in and created all of the elements we know about.
        // Now, we need to go through the remoteLinkId map and link together
        // all the elements we just created.


        for (String localLinkId : remoteLinkMap.keySet() ) {
            String remoteLinkId = remoteLinkMap.get(localLinkId);
            Link local = (Link) elementMap.get(localLinkId);
            Link remote = (Link) elementMap.get(remoteLinkId);

            if (remoteLinkId.equals("urn:ogf:network:domain=*:node=*:port=*:link=*")) {
                continue;
            }

            if (remote == null) {
                Hashtable<String, String> urnInfo = URNParser.parseTopoIdent(remoteLinkId);
                // create a new domain, node, port for this link, as needed

                this.log.debug("Remote link id: "+remoteLinkId);

                Domain d;
                Node n;
                Port p;

                if (urnInfo.get("type") == null || (urnInfo.get("type").equals("link") == false)) {
                    this.log.error("Received an invalid link identifier for a remote link id: '"+remoteLinkId+"'");
                    continue;
                }

                p = (Port) elementMap.get(urnInfo.get("portFQID"));
                if (p == null) {
                    n = (Node) elementMap.get(urnInfo.get("nodeFQID"));
                    if (n == null) {
                        d = (Domain) elementMap.get(urnInfo.get("domainFQID")); 
                        if (d == null) {
                            d = new Domain(true);
                            d.setName(urnInfo.get("domainId"));
                            d.setTopologyIdent(urnInfo.get("domainId"));

                            retTopology.addDomain(d);
                        }

                        n = new Node(d, true);
                        n.setTopologyIdent(urnInfo.get("nodeId"));
                        n.setDomain(d);
                    }

                    p = new Port(n, true);
                    p.setTopologyIdent(urnInfo.get("portId"));
                    p.setAlias(urnInfo.get("portId"));
                    p.setNode(n);
                }

                remote = new Link(p, true);
                remote.setTopologyIdent(urnInfo.get("linkId"));
                remote.setAlias(urnInfo.get("linkId"));
                remote.setRemoteLink(local);
            }

            local.setRemoteLink(remote);
        }

        return retTopology;
    }



    /**
     * This method will go through the children of an XML Element of type "domain"
     * and convert them to Node objects, under the provided Domain object
     *
     * @param domXML the JDOM "domain" element
     * @param domDB the parent Domain object under which the Node objects
     * will be created.
     */
    @SuppressWarnings("unchecked")
    protected void parseNodes(Element domXML, Domain domDB) {
        this.log.debug("parsing nodes");

        String nodeTopoIdent = "";

        Iterator nodeXMLIterator = this.getElementChildren(domXML, "node").iterator();

        // now go through all of domXML's children elements
        while (nodeXMLIterator.hasNext()) {
            Element nodeXML = (Element) nodeXMLIterator.next();
            String fqid = nodeXML.getAttributeValue("id");
            nodeTopoIdent = TopologyUtil.getLSTI(fqid, "Node");

            this.log.debug("  got node, topo id: [" + nodeTopoIdent + "]");


            Node nodeDB = new Node(domDB, true);
            nodeDB.setTopologyIdent(nodeTopoIdent);

            if (!domDB.addNode(nodeDB)) {
                // oops, was added previously by a remote link
                nodeDB = domDB.getNodeByTopoId(nodeTopoIdent);
            }

            this.parseNodeAddress(nodeXML, nodeDB);
            this.parsePorts(nodeXML, nodeDB);

            this.elementMap.put(fqid, nodeDB);
        }
    }

    /**
     * This method will go over the address child element of an XML Element
     * of type "node" and convert it to a NodeAddress object under the provided
     * Node object
     *
     * @param nodeXML the JDOM "node" element
     * @param nodeDB the parent Node object under which the NodeAddress object
     * will be created.
     */
    protected void parseNodeAddress(Element nodeXML, Node nodeDB) {
        Element addressXML = this.getElementChild(nodeXML, "address");
        String address = "";
        // TODO: multiple addresses? it's 1-on-1 on the DB
        if (addressXML != null) {
           address = addressXML.getValue();
        }

        NodeAddress addrDB = nodeDB.getNodeAddress();
        this.log.debug(" node address is: [" + address + "]");

        // TODO: figure out what to do re: different operations
        if (address.equals("")) {
            this.log.debug(" no address, setting to null");
            nodeDB.setNodeAddress(null);
            return;
        } else {
            if (addrDB == null) {
                this.log.debug(" new address");
                addrDB = new NodeAddress();
            } else {
                this.log.debug(" replacing address");
            }
            addrDB.setAddress(address);
            addrDB.setNode(nodeDB);
            nodeDB.setNodeAddress(addrDB);
        }
    }

    /**
     * This method will go through the children of an XML Element of type "node"
     * and convert them to Port objects, under the provided Node object
     *
     * @param nodeXML the JDOM "node" element
     * @param nodeDB the parent Node object under which the Port objects
     * will be created.
     */
    @SuppressWarnings("unchecked")
    protected void parsePorts(Element nodeXML, Node nodeDB) {

        Iterator portXMLIterator = this.getElementChildren(nodeXML, "port").iterator();

        while (portXMLIterator.hasNext()) {
            Element portXML = (Element) portXMLIterator.next();

            String fqid = portXML.getAttributeValue("id");

            this.log.debug("  got port, id:[" + fqid + "]");

            Port portDB = new Port(nodeDB, true);

            String portTopoIdent = TopologyUtil.getLSTI(fqid, "Port");

            portDB.setTopologyIdent(portTopoIdent);

            String strCapacity = this.getElementChildValue(portXML, "capacity");
            String strMaxRCapacity = this.getElementChildValue(portXML, "maximumReservableCapacity");
            String strMinRCapacity = this.getElementChildValue(portXML, "minimumReservableCapacity");
            String strGranularity = this.getElementChildValue(portXML, "granularity");

            Long capacity = TopologyUtil.understandBandwidth(strCapacity);
            Long maxRCapacity = TopologyUtil.understandBandwidth(strMaxRCapacity);
            Long minRCapacity = TopologyUtil.understandBandwidth(strMinRCapacity);
            Long granularity = TopologyUtil.understandBandwidth(strGranularity);

            portDB.setCapacity(capacity);
            portDB.setMaximumReservableCapacity(maxRCapacity);
            portDB.setMinimumReservableCapacity(minRCapacity);
            portDB.setGranularity(granularity);
            //          portDB.setUnreservedCapacity(capacity); // TODO: think about this
            portDB.setAlias(portTopoIdent);

            if (!nodeDB.addPort(portDB)) {
                portDB = nodeDB.getPortByTopoId(portTopoIdent);
            }

            this.parseLinks(portXML, portDB);

            this.elementMap.put(fqid, portDB);
        }
    }

    /**
     * This method will go through the children of an XML Element of type "port"
     * and convert them to Link objects, under the provided Port object
     *
     * @param portXML the JDOM "port" element
     * @param portDB the parent Port object under which the Link objects
     * will be created.
     */
    protected void parseLinks(Element portXML, Port portDB) {
        String[] columns = null;

        String linkTopoIdent = "";

        Iterator linkXMLIterator = this.getElementChildren(portXML, "link").iterator();

        while (linkXMLIterator.hasNext()) {
            Element linkXML = (Element) linkXMLIterator.next();

            String tempLinkId = linkXML.getAttributeValue("id");
            columns = tempLinkId.split("##");
            String fqid;
            String ipAddress;

            if (columns.length == 2) {
                fqid = columns[0];
                ipAddress = columns[1];
            } else {
                fqid = tempLinkId;
                ipAddress = null;
            }
            this.log.debug("  link: [" + fqid + "]");

            Link linkDB = new Link(portDB, true);
            linkTopoIdent = TopologyUtil.getLSTI(fqid, "Link");
            linkDB.setTopologyIdent(linkTopoIdent);
            linkDB.setAlias(linkTopoIdent);

            Element capXML = this.getElementChild(linkXML, "capacity");
            Long capacity;
            if (capXML != null) {
                capacity = TopologyUtil.understandBandwidth(capXML.getValue());
            } else {
                capacity = portDB.getCapacity();
            }

            Element maxRCapXML = this.getElementChild(linkXML, "maximumReservableCapacity");
            Long maxRCapacity;
            if (maxRCapXML != null) {
                maxRCapacity = TopologyUtil.understandBandwidth(maxRCapXML.getValue());
            } else {
                maxRCapacity = portDB.getMaximumReservableCapacity();
            }

            Element minRCapXML = this.getElementChild(linkXML, "minimumReservableCapacity");
            Long minRCapacity;
            if (minRCapXML != null) {
                minRCapacity = TopologyUtil.understandBandwidth(minRCapXML.getValue());
            } else {
                minRCapacity = portDB.getMinimumReservableCapacity();
            }

            Element granXML = this.getElementChild(linkXML, "granularity");
            Long granularity;
            if (granXML != null) {
                granularity = TopologyUtil.understandBandwidth(granXML.getValue());
            } else {
                granularity = portDB.getGranularity();
            }

            String trafficEngineeringMetric;
            if (this.getElementChild(linkXML, "trafficEngineeringMetric") != null) {
                trafficEngineeringMetric = this.getElementChildValue(linkXML, "trafficEngineeringMetric");
                linkDB.setTrafficEngineeringMetric(trafficEngineeringMetric);
            }

            Element remoteLinkXML = this.getElementChild(linkXML, "remoteLinkId");
            if (remoteLinkXML != null) {
                    // XXX: we probably want to at minimum validate the remoteLinkXML.
                    this.remoteLinkMap.put(linkDB.getFQTI(), remoteLinkXML.getValue());
            }

            linkDB.setCapacity(capacity);
            linkDB.setUnreservedCapacity(capacity); // TODO: who handles this?
            linkDB.setMaximumReservableCapacity(maxRCapacity);
            linkDB.setMinimumReservableCapacity(minRCapacity);
            linkDB.setGranularity(granularity);

            if (!portDB.addLink(linkDB)) {
                linkDB = portDB.getLinkByTopoId(linkTopoIdent);
            }

            this.parseLinkIPAddress(linkDB, ipAddress);
            this.parseLinkSwcap(linkXML, linkDB);

            this.elementMap.put(fqid, linkDB);
        }
    }

    /**
     * This method will go over the SwitchingCapabilityDescriptors child element of
     * an XML Element of type "link" and convert its contents into a
     * L2SwitchingCapabilityData object under the provided Link object
     *
     * @param linkXML the JDOM "link" element
     * @param linkDB the parent Link object under which the L2SwitchingCapabilityData
     * object will be created.
     */
    protected void parseLinkSwcap(Element linkXML, Link linkDB) {
        int ifceMTU;
        String vlanAv;

        Element linkSwCapXML = this.getElementChild(linkXML, "SwitchingCapabilityDescriptors");

        if (linkSwCapXML == null) {
            return;
        }

        Element swcapTypeXML = this.getElementChild(linkSwCapXML, "switchingcapType");
        Element encTypeXML = this.getElementChild(linkSwCapXML, "encodingType");
        Element swCapSpcXML = this.getElementChild(linkSwCapXML, "switchingCapabilitySpecficInfo");

        if (swCapSpcXML == null) {
            return;
        }

        Element capXML = this.getElementChild(swCapSpcXML, "capability");
        Element ifceMTUXML = this.getElementChild(swCapSpcXML, "interfaceMTU");
        Element vlanAvXML = this.getElementChild(swCapSpcXML, "vlanRangeAvailability");

        if (ifceMTUXML == null) {
            return;
        }

        if (vlanAvXML == null) {
            return;
        }

        ifceMTU = Integer.parseInt(ifceMTUXML.getValue());
        vlanAv = vlanAvXML.getValue();

        this.log.debug("l2cap: mtu: [" + new Integer(ifceMTU).toString() + "] vlanAv: [" + vlanAv + "]");

        L2SwitchingCapabilityData l2Cap = new L2SwitchingCapabilityData();
        l2Cap.setInterfaceMTU(ifceMTU);
        l2Cap.setVlanRangeAvailability(vlanAv);
        l2Cap.setLink(linkDB);
        linkDB.setL2SwitchingCapabilityData(l2Cap);
    }

    /**
     * This method will add an IP address to the provided Link object as a child Ipaddress
     * object
     *
     * @param linkDB the parent Link object under which the Ipaddress object will be created.
     * @param ipAddress the IP address (string format)
     */
    protected void parseLinkIPAddress(Link linkDB, String ipAddress) {
        Ipaddr addrDB = null;
        boolean found = false;
        if (ipAddress == null) {
            return;
        }

        if (linkDB.getIpaddrs() != null) {
            Iterator addrIt = linkDB.getIpaddrs().iterator();

            while (addrIt.hasNext()) {
                Ipaddr thisAddr = (Ipaddr) addrIt.next();

                if (thisAddr.getIP().equals(ipAddress)) {
                    found = true;
                    addrDB = thisAddr;

                    break;
                }
            }
        }

        if (found) {
            addrDB.setValid(true);
            return;
        } else {
            addrDB = new Ipaddr();
            addrDB.setIP(ipAddress);
            addrDB.setValid(true);
            addrDB.setLink(linkDB);
            linkDB.addIpaddr(addrDB);
        }
    }

    /**
     * localDomainId getter
     * @return the value of localDomainId
     */
    public String getLocalDomainId() {
        return this.localDomainId;
    }

    /**
     * localDomainId setter
     * @param domainId The value to be set
     */
    public void setLocalDomainId(String domainId) {
        this.localDomainId = domainId;
    }

    private List<Element> getElementChildren(Element e, String name) {
        if (this.ns == null) {
            ArrayList<Element> filteredChildren = new ArrayList<Element>();

            List<Element> children = e.getChildren();

            for (Element child : children) {
                if (child.getName().equals(name)) {
                    filteredChildren.add(child);
                }
            }

            return filteredChildren;
        } else {
            return e.getChildren(name, this.ns);
        }
    }

    private Element getElementChild(Element e, String name) {
        if (this.ns == null) {
            List<Element> children = e.getChildren();

            for (Element child : children) {
                if (child.getName().equals(name)) {
                    return child;
                }
            }

            return null;
        } else {
            return e.getChild(name, this.ns);
        }
    }

    private String getElementChildValue(Element e, String name) {
        Element child_value = getElementChild(e, name);

        if (child_value == null) {
                return "";
        }

        return child_value.getValue();
    }
}
