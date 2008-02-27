package net.es.oscars.bss.topology;

import net.es.oscars.*;
import net.es.oscars.database.*;

import org.apache.log4j.*;

import org.hibernate.*;

import org.jdom.*;

import java.util.*;


/**
 * This class contains methods that export the local OSCARS topology
 * database into XML.
 * @author Evangelos Chaniotakis (haniotak@es.net)
 */
public class TopologyXMLExporter {
    private Logger log;
    private Session ses;
    private Properties props;
    private Namespace ns;
    private String nsUri;
    private String nsPrefix;
    private String dbname;

    /**
     * Constructor initializes logging and local properties
     */
    public TopologyXMLExporter(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.dbname = dbname;

        PropHandler propHandler = new PropHandler("oscars.properties");
        this.props = propHandler.getPropertyGroup("topo", true);
        this.setNsUri(this.props.getProperty("nsuri").trim());
        this.setNsPrefix(this.props.getProperty("nsprefix").trim());
        this.ns = Namespace.getNamespace(this.getNsPrefix(), this.getNsUri());
    }

    /**
     * No-parameters topology getter
     * @return The topology JDOM Document object
     */
    public Document getTopology() {
        return this.getTopology("");
    }


    /**
     * This method will fetch the topology as a JDOM Document
     * object which can then be traversed, prettyprinted etc.
     * The user can specify a query to perform against the
     * database.
     * @param query the query to perform against the DB
     * @return The topology JDOM Document object
     */
    public Document getTopology(String query) {
    	
        this.ses = HibernateUtil.getSessionFactory(this.dbname).getCurrentSession();

        Transaction tx = this.ses.beginTransaction();

        this.log.debug("Start");

        Document doc = this.createXML(query);

        tx.commit();
        this.log.debug("Done");

        return doc;
    }
    
   public Document getTopology(Topology topology) {
        this.log.debug("Start");

        Document doc = this.createXML(topology);

        this.log.debug("Done");

        return doc;
    }


    /**
     * Internal method that runs the user query and fetches the topology
     * JDOM Document object.
     * Note: Need to specify how queries should run; currently
     * we support queries in the form of domain topology identifiers
     * @param query the query to perform against the DB
     * @return The topology JDOM Document object
     */
    @SuppressWarnings("unchecked")
    protected Document createXML(String query) {
        String domTopoIdent = query; // TODO: how will queries look like?
        Element topoXML = new Element("topology", this.ns);
        topoXML.setAttribute("id", "OSCARS topology"); // TODO: specify this
        
        Element idcXML = new Element("idcId", this.ns);
        
        idcXML.addContent("placeholder"); // TODO: how do we determine this?
        topoXML.addContent(idcXML);

        Document doc = new Document(topoXML);
        Element domXML;
        List<Domain> domains;
        
        if (domTopoIdent.equals("")) {
            query = "from Domain";
            domains = this.ses.createQuery(query).list();
        } else {
            query = "from Domain as dbobj where topologyIdent=:topoIdent";
            domains = this.ses.createQuery(query)
                              .setString("topoIdent", domTopoIdent).list();
        }

        for (Domain domDB : domains) {
            domXML = this.exportDomain(domDB);
            topoXML.addContent(domXML);
        }

        return doc;
    }
    
    protected Document createXML(Topology topology) {
        Element topoXML = new Element("topology", this.ns);
        topoXML.setAttribute("id", "OSCARS topology"); // TODO: specify this
        
        Element idcXML = new Element("idcId", this.ns);
        
        idcXML.addContent("placeholder"); // TODO: how do we determine this?
        topoXML.addContent(idcXML);

        Document doc = new Document(topoXML);
        Element domXML;
        List<Domain> domains = topology.getDomains();
        
        for (Domain domDB : domains) {
            domXML = this.exportDomain(domDB);
            topoXML.addContent(domXML);
        }

        return doc;
    }

    /**
     * Internal method that exports a single domain from the database
     * into a JDOM Element object.
     * @param domDB the Domain database object
     * @return The topology JDOM Element object
     */
    protected Element exportDomain(Domain domDB) {
        Namespace ns = this.ns;

        String domTopoIdent = TopologyUtil.getFQTI(domDB);

        this.log.info("Creating XML for domain:[" + domTopoIdent + "]");

        Element domXML = null;

        domXML = new Element("domain", ns);

        Attribute domIdXML = new Attribute("id", domTopoIdent, ns);
        domXML.setAttribute(domIdXML);

        if (domDB.getNodes() != null) {
            Iterator nodeIt = domDB.getNodes().iterator();

            while (nodeIt.hasNext()) {
                Node nodeDB = (Node) nodeIt.next();
                String nodeId = TopologyUtil.getFQTI(nodeDB);

                Element nodeXML = new Element("node", ns);

                Attribute nodeIdXML = new Attribute("id", nodeId, ns);
                nodeXML.setAttribute(nodeIdXML);

                Element addrXML = new Element("address", ns);
                NodeAddress nodeAddrDB = nodeDB.getNodeAddress();

                if (nodeAddrDB != null) {
                    String addr = nodeAddrDB.getAddress();
                    addrXML.addContent(addr);
                    nodeXML.addContent(addrXML);
                } else {
                    this.log.info("Node without address: [" +
                        nodeDB.getTopologyIdent() + "]");
                }

                if (nodeDB.getPorts() != null) {
                    Iterator portIt = nodeDB.getPorts().iterator();

                    while (portIt.hasNext()) {
                        Port portDB = (Port) portIt.next();
                        Element portXML = new Element("port", ns);

                        String portId = TopologyUtil.getFQTI(portDB);
                        Attribute portIdXML = new Attribute("id", portId, ns);
                        portXML.setAttribute(portIdXML);

                        Element portCap = new Element("capacity", ns);
                        portCap.addContent(portDB.getCapacity().toString());
                        portXML.addContent(portCap);

                        Long granularity = portDB.getGranularity();

                        if (granularity != null) {
                            Element portGran = new Element("granularity", ns);
                            portGran.addContent(granularity.toString());
                            portXML.addContent(portGran);
                        }

                        Long minResCap = portDB.getMinimumReservableCapacity();

                        if (minResCap != null) {
                            Element portMinResCap = new Element("minimumReservableCapacity",
                                    ns);
                            portMinResCap.addContent(minResCap.toString());
                            portXML.addContent(portMinResCap);
                        }

                        Long maxResCap = portDB.getMaximumReservableCapacity();

                        if (maxResCap != null) {
                            Element portMaxResCap = new Element("maximumReservableCapacity",
                                    ns);
                            portMaxResCap.addContent(maxResCap.toString());
                            portXML.addContent(portMaxResCap);
                        }

                        if (portDB.getLinks() != null) {
                            Iterator linkIt = portDB.getLinks().iterator();

                            while (linkIt.hasNext()) {
                                Link linkDB = (Link) linkIt.next();
                                Element linkXML = new Element("link", ns);

                                String linkId = TopologyUtil.getFQTI(linkDB);
                                Attribute linkIdXML = new Attribute("id",
                                        linkId, ns);
                                linkXML.setAttribute(linkIdXML);

                                Link remLinkDB = linkDB.getRemoteLink();

                                if (remLinkDB != null) {
                                    Element remLinkXML = new Element("remoteLinkId",
                                            ns);
                                    remLinkXML.addContent(TopologyUtil.getFQTI(
                                            remLinkDB));
                                    linkXML.addContent(remLinkXML);
/*
                                    Element remPortXML = new Element("remotePortId",
                                            ns);
                                    remPortXML.addContent(TopologyUtil.getFQTI(
                                            remLinkDB.getPort()));
                                    linkXML.addContent(remPortXML);

                                    Element remNodeXML = new Element("remoteNodeId",
                                            ns);
                                    remNodeXML.addContent(TopologyUtil.getFQTI(
                                            remLinkDB.getPort().getNode()));
                                    linkXML.addContent(remNodeXML);

                                    Element remDomXML = new Element("remoteDomainId",
                                            ns);
                                    remDomXML.addContent(TopologyUtil.getFQTI(
                                            remLinkDB.getPort().getNode()
                                                     .getDomain()));
                                    linkXML.addContent(remDomXML);
*/
                                }
                                L2SwitchingCapabilityData l2capDB = linkDB.getL2SwitchingCapabilityData();
                                if (l2capDB != null) {
                                	Element l2CapXML = new Element("SwitchingCapabilityDescriptors", ns);
                                    Element swCapTypeXML = new Element("switchingcapType", ns);
                                    Element encTypeXML = new Element("encodingType", ns);
                                    Element swCapSpcXML = new Element("switchingCapabilitySpecficInfo",
                                            ns);
                                    l2CapXML.addContent(swCapTypeXML);
                                    l2CapXML.addContent(encTypeXML);
                                    l2CapXML.addContent(swCapSpcXML);

                                    Element capXML = new Element("capability", ns);
                                    Element ifceMtuXML = new Element("interfaceMTU", ns);
                                    Element vlanAvXML = new Element("vlanRangeAvailability", ns);
                                    
                                    vlanAvXML.addContent(l2capDB.getVlanRangeAvailability());
                                    ifceMtuXML.addContent(new Integer(l2capDB.getInterfaceMTU()).toString());
                                    
                                    swCapSpcXML.addContent(capXML);
                                    swCapSpcXML.addContent(ifceMtuXML);
                                    swCapSpcXML.addContent(vlanAvXML);


                                    

                                    linkXML.addContent(l2CapXML);
                                	
                                }
                                
                                portXML.addContent(linkXML);
                            }

                        }

                        nodeXML.addContent(portXML);
                    }
                }

                domXML.addContent(nodeXML);
            }
        }

        return domXML;
    }
    

    // Getter / setter functions
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
