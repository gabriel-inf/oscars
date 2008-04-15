package net.es.oscars.tss.oscars;

import net.es.oscars.*;
import net.es.oscars.bss.topology.*;
import net.es.oscars.tss.*;
import net.es.oscars.wsdlTypes.*;

import org.apache.log4j.*;

import org.jdom.*;

import org.apache.axiom.om.*;

import org.ogf.schema.network.topology.ctrlplane._20070626.*;

import java.util.*;


/**
 * A TEDB that implements the interface by querying the local
 * OSCARS topology database.
 *
 * @author Evangelos Chaniotakis (haniotak@es.net)
 */
public class OSCARSDatabase implements TEDB {
    private Properties props;
    private Logger log;
    private Namespace ns;
    private String nsUri;
    private String nsPrefix;
    private String dbname;

    /**
    * Constructor that initializes properties & logging
    */

    public OSCARSDatabase() {
        PropHandler propHandler = new PropHandler("oscars.properties");
        this.props = propHandler.getPropertyGroup("topo", true);
        this.dbname = "bss";
        this.log = Logger.getLogger(this.getClass());
        this.setNsUri(this.props.getProperty("nsuri"));
        this.setNsPrefix(this.props.getProperty("nsprefix"));
        this.ns = Namespace.getNamespace(this.getNsPrefix(), this.getNsUri());
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

    /**
    * Retrieves the topology from the local database by first exporting
    * the database to a JDOM Document object, then iterating through
    * the document and populating the Axis2 types.
    *
    * A type of topology is provided by the parameter to the call,
    * but currently only <i>all</i> is supported; future work will
    * improve this.
    *
    * The values for type are:
    *   all, adjacentdomains, delta, nodes, internetworklinks
    *
    * Note: This roundabout way of talking to the database could probably
    * be improved by mapping the JDOM document directly to the Axis2
    * objects; I haven't found the way to do that yet, though. (haniotak)
    *
    * @param type the type of the topology to be retrieved
    * @return the topology Axis2 object
    * @throws TSSException
    */

    public CtrlPlaneTopologyContent selectNetworkTopology(String type)
        throws TSSException {

        this.log.info("selectNetworkTopology("+type+").start");

        TopologyXMLExporter exporter = new TopologyXMLExporter(this.dbname);

        Document doc = exporter.getTopology();
        Namespace ns = this.ns;

        Element topoXML = doc.getRootElement();

        CtrlPlaneTopologyContent topology = new CtrlPlaneTopologyContent();
        topology.setId(topoXML.getAttributeValue("id", ns));
        topology.setIdcId("esnet-idc"); // TODO: fix this

        Iterator domIt = topoXML.getChildren("domain", ns).iterator();
        while (domIt.hasNext()) {
          Element domXML = (Element) domIt.next();
          CtrlPlaneDomainContent domain = new CtrlPlaneDomainContent();
          topology.addDomain(domain);
          String domainId = domXML.getAttributeValue("id", ns);

          if (domainId == null || domainId.equals("")) {
              continue;
          }

          domain.setId(domainId);
          this.log.info("domain:"+domain.getId());

          Iterator nodeIt = domXML.getChildren("node", ns).iterator();
          while (nodeIt.hasNext()) {
            Element nodeXML = (Element) nodeIt.next();
            CtrlPlaneNodeContent node = new CtrlPlaneNodeContent();

            Element nodeAddrXML = nodeXML.getChild("address", ns);
            if (nodeAddrXML != null) {
              String nodeAddrVal = nodeAddrXML.getValue();
              CtrlPlaneAddressContent nodeAddr = new CtrlPlaneAddressContent();
              nodeAddr.setString(nodeAddrVal);
              node.setAddress(nodeAddr);
            }

            node.setId(nodeXML.getAttributeValue("id", ns));

            domain.addNode(node);

            this.log.info("node:"+node.getId());

            Iterator portIt = nodeXML.getChildren("port", ns).iterator();
            while (portIt.hasNext()) {
              Element portXML = (Element) portIt.next();
              CtrlPlanePortContent port = new CtrlPlanePortContent();

              node.addPort(port);
              port.setId(portXML.getAttributeValue("id", ns));

              this.log.info("port:"+port.getId());

              Element capXML = portXML.getChild("capacity", ns);
              if (capXML != null) {
                port.setCapacity(capXML.getValue());
              }

              Element granXML = portXML.getChild("granularity", ns);
              if (granXML != null) {
                port.setGranularity(granXML.getValue());
              }

              Element minResCapXML = portXML.getChild("minimumReservableCapacity", ns);
              if (minResCapXML != null) {
                port.setMinimumReservableCapacity(minResCapXML.getValue());
              }

              Element maxResCapXML = portXML.getChild("macimumReservableCapacity", ns);
              if (maxResCapXML != null) {
                port.setMaximumReservableCapacity(maxResCapXML.getValue());
              }

              Iterator linkIt = portXML.getChildren("link", ns).iterator();
              while (linkIt.hasNext()) {
                Element linkXML = (Element) linkIt.next();
                CtrlPlaneLinkContent link = new CtrlPlaneLinkContent();
                port.addLink(link);
                link.setId(linkXML.getAttributeValue("id", ns));
                this.log.info("link:"+link.getId());

                Element remDomXML = linkXML.getChild("remoteDomainId", ns);
                if (remDomXML != null) {
                  String remDomainId = remDomXML.getValue();
                  link.setRemoteDomainId(remDomainId);
                }
                Element remNodeXML = linkXML.getChild("remoteNodeId", ns);
                if (remNodeXML != null) {
                  String remNodeId = remNodeXML.getValue();
                  link.setRemoteNodeId(remNodeId);
                }
                Element remPortXML = linkXML.getChild("remotePortId", ns);
                if (remPortXML != null) {
                  String remPortId = remPortXML.getValue();
                  link.setRemotePortId(remPortId);
                }
                Element remLinkXML = linkXML.getChild("remoteLinkId", ns);
                if (remLinkXML != null) {
                  String remLinkId = remLinkXML.getValue();
                  link.setRemoteLinkId(remLinkId);
                }

              }
            }
          }
        }

        this.log.info("selectNetworkTopology("+type+").end");
        return topology;
    }

    /**
     * Inserts the contents of a topology Axis2 object to the database.
     * This converts the Axis2 objects into a JDOM XML document and
     * passes that into the XML importer class, which handles the
     * actual insertion into the database.
     *
     * Note: This roundabout way of inserting the topology could be made a
     * lot more straightforward if I could find a way to get a JDOM Document
     * out of an Axis2 object.
     * Not finished yet (haniotak)
     *
     * @param topology the topology to be inserted
     */

    public void insertNetworkTopology(CtrlPlaneTopologyContent topology)
        throws TSSException {
        Namespace ns = this.ns;
        this.log.info("insertNetworkTopology.start");

        Element topoXML = new Element("topology", ns);
        Document doc = new Document(topoXML);
        Attribute topoId = new Attribute("id", topology.getId(), ns);
        topoXML.setAttribute(topoId);

        Element idcId = new Element("idcId", ns);
        idcId.setText(topology.getIdcId());
        topoXML.addContent(idcId);

        CtrlPlaneDomainContent[] domains = topology.getDomain();

        if (domains != null) {
            for (CtrlPlaneDomainContent d : domains) {
                Element domXML = new Element("domain", ns);

                Attribute domId = new Attribute("id", d.getId(), ns);
                domXML.setAttribute(domId);
                this.log.info("domain id:["+domId+"]");

                topoXML.addContent(domXML);

                CtrlPlaneNodeContent[] nodes = d.getNode();

                if (nodes != null) {
                    for (CtrlPlaneNodeContent n : nodes) {
                        Element nodeXML = new Element("node", ns);
                        Attribute nodeId = new Attribute("id", n.getId(), ns);
                        Element nodeAddr = new Element("address", ns);
                        nodeAddr.setText(n.getAddress().getValue());
                        nodeXML.addContent(nodeAddr);
                        nodeXML.setAttribute(nodeId);

                        domXML.addContent(nodeXML);

                        CtrlPlanePortContent[] ports = n.getPort();

                        if (ports != null) {
                            for (CtrlPlanePortContent p : ports) {
                                Element portXML = new Element("port", ns);
                                Attribute portId = new Attribute("id", p.getId(), ns);
                                portXML.setAttribute(portId);

                                Element portCap = new Element("capacity", ns);
                                portCap.setText(p.getCapacity());
                                portXML.addContent(portCap);

                                Element portGran = new Element("granularity", ns);
                                portGran.setText(p.getGranularity());
                                portXML.addContent(portGran);

                                Element portMinResCap = new Element("minimumReservableCapacity", ns);
                                portMinResCap.setText(p.getMinimumReservableCapacity());
                                portXML.addContent(portMinResCap);

                                Element portMaxResCap = new Element("maximumReservableCapacity", ns);
                                portMaxResCap.setText(p.getMaximumReservableCapacity());
                                portXML.addContent(portMaxResCap);

                                nodeXML.addContent(portXML);

                                CtrlPlaneLinkContent[] links = p.getLink();

                                if (links != null) {
                                    for (CtrlPlaneLinkContent l : links) {
                                        Element linkXML = new Element("link", ns);
                                        Attribute linkId = new Attribute("id", l.getId(), ns);
                                        linkXML.setAttribute(linkId);

                                        Element linkRemDom = new Element("remoteDomainId", ns);
                                        linkRemDom.setText(l.getRemoteDomainId());
                                        linkXML.addContent(linkRemDom);

                                        Element linkRemNode = new Element("remoteNodeId", ns);
                                        linkRemNode.setText(l.getRemoteNodeId());
                                        linkXML.addContent(linkRemNode);

                                        Element linkRemPort = new Element("remotePortId", ns);
                                        linkRemPort.setText(l.getRemotePortId());
                                        linkXML.addContent(linkRemPort);

                                        Element linkRemLink = new Element("remoteLinkId", ns);
                                        linkRemLink.setText(l.getRemoteLinkId());
                                        linkXML.addContent(linkRemLink);

                                        portXML.addContent(linkXML);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return;
    }
}
