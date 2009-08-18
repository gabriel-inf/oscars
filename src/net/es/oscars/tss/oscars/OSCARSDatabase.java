package net.es.oscars.tss.oscars;

import net.es.oscars.*;
import net.es.oscars.bss.topology.*;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.tss.*;

import org.apache.log4j.*;
import org.hibernate.*;

import org.jdom.*;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneAddressContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneDomainContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneNodeContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePortContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwcapContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwitchingCapabilitySpecificInfo;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneTopologyContent;

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
    private String localdomain;

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
        SessionFactory sf = HibernateUtil.getSessionFactory(this.dbname);
        sf.getCurrentSession().beginTransaction();
        DomainDAO domainDAO = new DomainDAO(this.dbname);
        this.setLocaldomain(domainDAO.getLocalDomain().getTopologyIdent());
        sf.getCurrentSession().getTransaction().commit();
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
        Date today = new Date();
        Long now = today.getTime();
        now = now / 1000;

        CtrlPlaneTopologyContent topology = new CtrlPlaneTopologyContent();
        //this.log.info("UID: " + topology.getSerialVersionUID());
        String topologyId = this.getLocaldomain() + "-" + now.toString();
        topology.setId(topologyId);
        topology.setIdcId(this.getLocaldomain());

        Iterator domIt = topoXML.getChildren("domain", ns).iterator();
        while (domIt.hasNext()) {
          Element domXML = (Element) domIt.next();
          CtrlPlaneDomainContent domain = new CtrlPlaneDomainContent();
          topology.addDomain(domain);
          String domainId = domXML.getAttributeValue("id");

          if (domainId == null || domainId.equals("")) {
              continue;
          }

          domain.setId(domainId);
          this.log.debug("domain:"+domain.getId());

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

            node.setId(nodeXML.getAttributeValue("id"));

            domain.addNode(node);

            this.log.debug("node:"+node.getId());

            Iterator portIt = nodeXML.getChildren("port", ns).iterator();
            while (portIt.hasNext()) {
              Element portXML = (Element) portIt.next();
              CtrlPlanePortContent port = new CtrlPlanePortContent();

              node.addPort(port);
              port.setId(portXML.getAttributeValue("id"));

              this.log.debug("port:"+port.getId());

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
                link.setId(linkXML.getAttributeValue("id"));

                Element remLinkXML = linkXML.getChild("remoteLinkId", ns);
                if (remLinkXML != null) {
                  String remLinkId = remLinkXML.getValue();
                  link.setRemoteLinkId(remLinkId);
                }


                Element teMetricXML = linkXML.getChild("trafficEngineeringMetric", ns);
                if (teMetricXML != null) {
                    link.setTrafficEngineeringMetric(teMetricXML.getValue());
                } else {
                    link.setTrafficEngineeringMetric("0");
                }

                Element swCapDescXML = linkXML.getChild("SwitchingCapabilityDescriptors", ns);
                Integer ifceMtu = 0;
                String capability = "";
                String vlanRangeAv = "";
                String encType = "";
                String swCapType = "";
                if (swCapDescXML != null) {
                    Element swCapTypeXML    = swCapDescXML.getChild("switchingcapType", ns);
                    Element encTypeXML      = swCapDescXML.getChild("encodingType", ns);
                    Element swCapabSpcXML   = swCapDescXML.getChild("switchingCapabilitySpecificInfo", ns);
                    if (encType != null) encType = encTypeXML.getValue();

                    if (swCapType != null) swCapType = swCapTypeXML.getValue();

                    if (swCapabSpcXML != null) {
                        Element capabXML = swCapabSpcXML.getChild("capability", ns);
                        Element ifceMtuXML = swCapabSpcXML.getChild("interfaceMTU", ns);
                        Element vlanAvXML = swCapabSpcXML.getChild("vlanRangeAvailability", ns);
                        if (ifceMtuXML != null) ifceMtu = Integer.valueOf(ifceMtuXML.getValue());
                        if (capabXML != null)   capability = capabXML.getValue();
                        if (vlanAvXML != null)  vlanRangeAv = vlanAvXML.getValue();
                    }
                }


                CtrlPlaneSwitchingCapabilitySpecificInfo swCapSpc = new CtrlPlaneSwitchingCapabilitySpecificInfo();
                swCapSpc.setInterfaceMTU(ifceMtu);
                swCapSpc.setCapability(capability);
                swCapSpc.setVlanRangeAvailability(vlanRangeAv);

                CtrlPlaneSwcapContent swCapDesc = new CtrlPlaneSwcapContent();
                swCapDesc.setEncodingType(encType);
                swCapDesc.setSwitchingcapType(swCapType);
                swCapDesc.setSwitchingCapabilitySpecificInfo(swCapSpc);


                link.setSwitchingCapabilityDescriptors(swCapDesc);



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
                this.log.debug("domain id:["+domId+"]");

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

    public String getLocaldomain() {
        return localdomain;
    }

    public void setLocaldomain(String localdomain) {
        this.localdomain = localdomain;
    }
}
