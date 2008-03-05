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

import org.jdom.input.SAXBuilder;

import java.io.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
* This class contains methods that will import a topology XML file
* into the local topology database.
* @author Evangelos Chaniotakis (haniotak@es.net)
*/
public class TopologyXMLImporter {

    private Logger log;
    private Namespace ns;
    private Properties props;

    private String rootTopoId;
    private String nsUri;
    private String nsPrefix;

    private String dbname;

    private TopologyManager topoManager;

    /**
     * Constructor initializes logging and local properties
     */
    public TopologyXMLImporter(String dbname) {
        this.log = Logger.getLogger(this.getClass());

        PropHandler propHandler = new PropHandler("oscars.properties");

        this.props = propHandler.getPropertyGroup("topo", true);

        this.setNsUri(this.props.getProperty("nsuri").trim());
        this.setNsPrefix(this.props.getProperty("nsprefix").trim());
        this.setRootTopoId(this.props.getProperty("roottopoid").trim());

        this.ns = Namespace.getNamespace(this.getNsPrefix(), this.getNsUri());
        this.dbname = dbname;

        this.topoManager = new TopologyManager(this.dbname);
    }


    /**
     * This method will convert a JDOM Document object containing topology
     * information into objects appropriate for insertion into the
     * local OSCARS topology database.
     *
     * @param doc the JDOM document object
     * @param operation the operation to perform (currently ignored)
     */
    protected void importXML(Document doc, String operation) {
        // TODO: support different operations

        Element topoXML = doc.getRootElement();

        Namespace ns = this.ns;
        this.log.debug("parsing domains");

        Topology newTopology = new Topology();

        Iterator domainIt = topoXML.getChildren("domain", ns).iterator();
        while (domainIt.hasNext()) {
            Element domXML = (Element) domainIt.next();

            // construct the domain topology identifier
            String domainId = domXML.getAttributeValue("id");

            // clean it up
            domainId = TopologyUtil.getLSTI(domainId, "Domain");

            String domTopoIdent = domainId;

            this.log.debug("  Got domain, id: [" + domTopoIdent + "]");

            Domain domDB = new Domain(true);

            domDB.setTopologyIdent(domTopoIdent);
            domDB.setName(domainId);
            domDB.setUrl("Unknown");
            domDB.setLocal(false);

            // read in Node information for this Domain
            this.parseNodes(domXML, domDB);

            newTopology.addDomain(domDB);
        }

        // this.topoManager.updateDomains((List<Domain>) domains);
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

        Iterator nodeXMLIterator = domXML.getChildren("node", this.ns).iterator();

        // now go through all of domXML's children elements
        while (nodeXMLIterator.hasNext()) {
            Element nodeXML = (Element) nodeXMLIterator.next();

            nodeTopoIdent = TopologyUtil.getLSTI(nodeXML.getAttributeValue("id"), "Node");

            this.log.debug("  got node, topo id: [" + nodeTopoIdent + "]");

            Node nodeDB = new Node(domDB, true);
            nodeDB.setTopologyIdent(nodeTopoIdent);

            this.parseNodeAddress(nodeXML, nodeDB);
            this.parsePorts(nodeXML, nodeDB);

            domDB.getNodes().add(nodeDB);
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
        Element addressXML = nodeXML.getChild("address", this.ns);

        // TODO: multiple addresses? it's 1-on-1 on the DB
        String address = addressXML.getValue();

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
        Namespace ns = this.ns;

        Iterator portXMLIterator = nodeXML.getChildren("port", ns).iterator();

        while (portXMLIterator.hasNext()) {


            Element portXML = (Element) portXMLIterator.next();
            String portId = portXML.getAttributeValue("id");

            this.log.debug("  got port, id:[" + portId + "]");

            Port portDB = new Port(nodeDB, true);

            String portTopoIdent = TopologyUtil.getLSTI(portId, "Port");

            portDB.setTopologyIdent(portTopoIdent);

            String strCapacity = portXML.getChild("capacity", ns).getValue();
            String strMaxRCapacity = portXML.getChild("maximumReservableCapacity", ns).getValue();
            String strMinRCapacity = portXML.getChild("minimumReservableCapacity", ns).getValue();
            String strGranularity = portXML.getChild("granularity", ns).getValue();

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

            boolean added = nodeDB.getPorts().add(portDB);
            if (!added) {
                this.log.debug("  Couldn't add port [" +portTopoIdent+"]");
            } else {
                this.log.debug("  Added port [" +portTopoIdent+"]");
            }


            this.parseLinks(portXML, portDB);
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
        Namespace ns = this.ns;

        String linkTopoIdent = "";

        Iterator linkXMLIterator = portXML.getChildren("link", ns).iterator();

        while (linkXMLIterator.hasNext()) {
            Element linkXML = (Element) linkXMLIterator.next();

            String tempLinkId = linkXML.getAttributeValue("id");
            columns = tempLinkId.split("##");
            String linkId;
            String ipAddress;

            if (columns.length == 2) {
                linkId = columns[0];
                ipAddress = columns[1];
            } else {
                linkId = tempLinkId;
                ipAddress = null;
            }
            this.log.debug("  link: [" + linkId + "]");

            Link linkDB = new Link(portDB, true);
            linkTopoIdent = TopologyUtil.getLSTI(linkId, "Link");
            linkDB.setTopologyIdent(linkTopoIdent);
            linkDB.setAlias(linkTopoIdent);

            Element capXML = linkXML.getChild("capacity", this.ns);
            Long capacity;
            if (capXML != null) {
                capacity = TopologyUtil.understandBandwidth(capXML.getValue());
            } else {
                capacity = portDB.getCapacity();
            }

            Element maxRCapXML = linkXML.getChild("maximumReservableCapacity", this.ns);
            Long maxRCapacity;
            if (maxRCapXML != null) {
                maxRCapacity = TopologyUtil.understandBandwidth(maxRCapXML.getValue());
            } else {
                maxRCapacity = portDB.getMaximumReservableCapacity();
            }

            Element minRCapXML = linkXML.getChild("minimumReservableCapacity", this.ns);
            Long minRCapacity;
            if (minRCapXML != null) {
                minRCapacity = TopologyUtil.understandBandwidth(minRCapXML.getValue());
            } else {
                minRCapacity = portDB.getMinimumReservableCapacity();
            }

            Element granXML = linkXML.getChild("granularity", this.ns);
            Long granularity;
            if (granXML != null) {
                granularity = TopologyUtil.understandBandwidth(granXML.getValue());
            } else {
                granularity = portDB.getGranularity();
            }

            String trafficEngineeringMetric;
            if (linkXML.getChild("trafficEngineeringMetric", ns) != null) {
                trafficEngineeringMetric = linkXML.getChild("trafficEngineeringMetric", ns).getValue();
                linkDB.setTrafficEngineeringMetric(trafficEngineeringMetric);
            }

            linkDB.setCapacity(capacity);
            linkDB.setUnreservedCapacity(capacity); // TODO: who handles this?
            linkDB.setMaximumReservableCapacity(maxRCapacity);
            linkDB.setMinimumReservableCapacity(minRCapacity);
            linkDB.setGranularity(granularity);

            this.parseLinkIPAddress(linkDB, ipAddress);
            this.parseRemoteLink(linkXML, linkDB);
            this.parseLinkSwcap(linkXML, linkDB);
            portDB.addLink(linkDB);
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
        Namespace ns = this.ns;
        int ifceMTU;
        String vlanAv;

        Element linkSwCapXML = linkXML.getChild("SwitchingCapabilityDescriptors", ns);

        if (linkSwCapXML == null) {
            return;
        }

        Element swcapTypeXML = linkSwCapXML.getChild("switchingcapType", ns);
        Element encTypeXML = linkSwCapXML.getChild("encodingType", ns);
        Element swCapSpcXML = linkSwCapXML.getChild("switchingCapabilitySpecficInfo", ns);

        if (swCapSpcXML == null) {
            return;
        }

        Element capXML = swCapSpcXML.getChild("capability", ns);
        Element ifceMTUXML = swCapSpcXML.getChild("interfaceMTU", ns);
        Element vlanAvXML = swCapSpcXML.getChild("vlanRangeAvailability", ns);

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
     * This method will examine the remoteXXX child elements of the provided JDOM "link"
     * Element object, and create a new Domain/Node/Port/Link object hierarchy. The
     * new Link object will be set as the remoteLink for the source Link object.
     *
     * @param linkXML the JDOM "link" element
     * @param linkDB the source Link object
     */
    @SuppressWarnings("unchecked")
    protected void parseRemoteLink(Element linkXML, Link linkDB) {
        Domain remoteDomainDB = null;
        Node remoteNodeDB = null;
        Port remotePortDB = null;
        Link remoteLinkDB = null;
        Element xmlObj = null;

        String remoteDomainId = "";
        String remoteNodeId = "";
        String remotePortId = "";
        String remoteLinkId = "";
        // two cases we will support:
        // either we get passed the whole hierarchy
        // remoteDomainId, remoteNodeId, remotePortId, remoteLinkId
        // or we just get a fully qualified remoteLinkId
        xmlObj = linkXML.getChild("remoteLinkId", this.ns);

        if (xmlObj != null) {
            remoteLinkId = xmlObj.getValue();
            xmlObj = null;
        } else {
            return; // we ALWAYS need a remoteLinkId
        }

        this.log.debug("  remote link src: [" + remoteLinkId + "]");
        this.log.debug("  remote link LSTI: [" + TopologyUtil.getLSTI(remoteLinkId, "Link") + "]");



        // now let's go find the rest
        xmlObj = linkXML.getChild("remoteDomainId", this.ns);
        if (xmlObj != null) {
            remoteDomainId = xmlObj.getValue();
            xmlObj = null;
        }
        if (remoteDomainId == null || remoteDomainId.equals("")) {
            remoteDomainId = TopologyUtil.getLSTI(remoteLinkId, "Domain");
        } else {
            remoteDomainId = TopologyUtil.getLSTI(remoteDomainId, "Domain");
        }

        this.log.debug("  remote domain: [" + remoteDomainId + "]");

        if (remoteDomainId == null) {
            this.log.error("couldn't determine remote domain id!");
            return;
        }

        xmlObj = linkXML.getChild("remoteNodeId", this.ns);
        if (xmlObj != null) {
            remoteNodeId = xmlObj.getValue();
            xmlObj = null;
        }
        if (remoteNodeId == null || remoteNodeId.equals("")) {
            remoteNodeId = TopologyUtil.getLSTI(remoteLinkId, "Node");
        } else {
            remoteNodeId = TopologyUtil.getLSTI(remoteNodeId, "Node");
        }
        this.log.debug("  remote Node: [" + remoteNodeId + "]");

        if (remoteNodeId == null) {
            this.log.error("couldn't determine remote Node id!");

            return;
        }

        xmlObj = linkXML.getChild("remotePortId", this.ns);
        if (xmlObj != null) {
            remotePortId = xmlObj.getValue();
            xmlObj = null;
        }
        if (remotePortId == null || remotePortId.equals("")) {
            remotePortId = TopologyUtil.getLSTI(remoteLinkId, "Port");
        } else {
            remotePortId = TopologyUtil.getLSTI(remotePortId, "Port");
        }
        this.log.debug("  remote Port: [" + remotePortId + "]");

        if (remotePortId == null) {
            this.log.error("couldn't determine remote Port id!");

            return;
        }

        // OK, we should have all the IDs set up
        remoteDomainDB = new Domain(true);
        remoteDomainDB.setName(remoteDomainId);
        remoteDomainDB.setTopologyIdent(remoteDomainId);

        remoteNodeDB = new Node(remoteDomainDB, true);
        remoteNodeDB.setTopologyIdent(remoteNodeId);
        remoteNodeDB.setDomain(remoteDomainDB);
        remoteDomainDB.getNodes().add(remoteNodeDB);

        remotePortDB = new Port(remoteNodeDB, true);
        remotePortDB.setTopologyIdent(remotePortId);
        remotePortDB.setAlias(remotePortId);
        remotePortDB.setNode(remoteNodeDB);
        remoteNodeDB.addPort(remotePortDB);

        remoteLinkDB = new Link(remotePortDB, true);
        remoteLinkDB.setTopologyIdent(TopologyUtil.getLSTI(remoteLinkId, "Link"));
        remoteLinkDB.setAlias(remoteLinkId);

        remotePortDB.addLink(remoteLinkDB);

        linkDB.setRemoteLink(remoteLinkDB);

        return;
    }




    // Getter / setter functions
    /**
     * rootTopoId getter
     * @return the value of rootTopoId (urn:ogf:network)
     */
    public String getRootTopoId() {
        return this.rootTopoId;
    }

    /**
     * rootTopoId setter
     * @param urn The value to be set
     */
    public void setRootTopoId(String urn) {
        this.rootTopoId = urn;
    }


    /**
     * nsUri getter
     * @return the value of nsUri
     */
    public String getNsUri() {
        return this.nsUri;
    }

    /**
     * nsUri setter
     * @param uri The value to be set
     */
    public void setNsUri(String uri) {
        this.nsUri = uri;
    }

    /**
     * nsPrefix getter
     * @return the value of nsPrefix
     */
    public String getNsPrefix() {
        return this.nsPrefix;
    }

    /**
     * nsPrefix setter
     * @param prefix The value to be set
     */
    public void setNsPrefix(String prefix) {
        this.nsPrefix = prefix;
    }

}
