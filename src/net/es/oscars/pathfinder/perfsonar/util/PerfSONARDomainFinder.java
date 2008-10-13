package net.es.oscars.pathfinder.perfsonar.util;

import net.es.oscars.bss.topology.*;

import net.es.oscars.bss.topology.URNParser;

import org.jdom.*;

import net.es.oscars.bss.topology.*;
import net.es.oscars.oscars.*;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;

import java.util.List;
import java.util.Iterator;
import java.io.IOException;

import org.apache.log4j.*;
import org.apache.commons.httpclient.HttpException;

import edu.internet2.perfsonar.*;



/**
 * PSPathfinder finds the route through the domain and toward the destination
 * by consulting a perfSONAR Topology service. The pathfinder uses the
 * perfSONAR Information Services to lookup topologies from other domains and
 * does a search through those domains to find the path needed to get to the
 * destination. It then returns the intradomain path and the hop in the next
 * domain along the path.
 *
 * @author Aaron Brown (aaron@internet2.edu)
 */
public class PerfSONARDomainFinder extends TSLookupClient implements DomainFinder {
    private static final boolean FORCE_OPAQUE = true;
    private static final String KNOWN_TOPOLOGY_TYPE = "http://ogf.org/schema/network/topology/ctrlPlane/20080828/";

    /**
     * Creates a new client with the list of Global lookup services to 
     * contact determined by reading the hints file at the provided URL.
     * The result returned by the list file will be randomly re-ordered.
     * 
     * @param hintsFile the URL of the hints file to use to populate the list of global lookup services
     * @throws HttpException
     * @throws IOException
     */
    public PerfSONARDomainFinder() throws HttpException, IOException {
        super();
    }

    /**
     * Creates a new client with the list of Global lookup services to 
     * contact determined by reading the hints file at the provided URL.
     * The result returned by the list file will be randomly re-ordered.
     * 
     * @param hintsFile the URL of the hints file to use to populate the list of global lookup services
     * @throws HttpException
     * @throws IOException
     */
    public PerfSONARDomainFinder(String hintsFile) throws HttpException, IOException {
        super(hintsFile);
    }
    
    /**
     * Creates a new client with an explicitly set list of global and/or
     * home lookup services. One of the parameters may be null. If the first 
     * parameter is null then no global lookup servioces will be contacted
     * only the given home lookup services will be used. If the second paramter is
     * null the given set of global lookup services will be used to find the home
     * lookup service.
     * 
     * @param gLSList the list of global lookup services to use
     * @param hLSList the list of home lookup services to use
     */
    public PerfSONARDomainFinder(String[] gLSList, String[] hLSList, String[] TSList){
        super(gLSList, hLSList, TSList);
    }
    

    /**
     * Looks up the given domain using the perfSONAR Information
     * Infrastructure. This method uses the perfSONAR Information
     * Infrastructure to find the desired domain.
     */
    public Domain lookupDomain(String id) {
        Element topoXML;

        this.log.debug("Looking up domain: "+id);

        try {
            topoXML = this.getDomain(id, KNOWN_TOPOLOGY_TYPE);
        } catch (PSException e) {
            this.log.error("PSException while getting domain "+id);
            topoXML = null;
        }

        if (topoXML == null) {
            this.log.warn("Couldn't find domain "+id+" in topology service");
            return null;
        }

        this.log.debug("topoXML: "+topoXML);

        TopologyXMLParser parser = new TopologyXMLParser(null);
        Topology topology = parser.parse(topoXML, null);
        if (topology == null) {
            this.log.error("Couldn't parse topology");
            return null;
        }

        this.log.debug("Parsed topology");

        Domain retDomain = null;

        List<Domain> domains = topology.getDomains();
        for (Domain dom : domains) {
            String domFQTI = dom.getFQTI();

            if (domFQTI.equals(id)) {
                retDomain = dom;
                break;
            }
        }

        if (this.FORCE_OPAQUE) {
            Iterator<Node> nodeIter = retDomain.getNodes().iterator();
            while(nodeIter.hasNext()) {
                Node node = nodeIter.next();
    
                Iterator<Port> portIter = node.getPorts().iterator();
                while(portIter.hasNext()) {
                    Port port = portIter.next();
    
                    Iterator<Link> linkIter = port.getLinks().iterator();
                    while(linkIter.hasNext()) {
                        Link link = linkIter.next();

                        if (link.getRemoteLink() == null) {
                            linkIter.remove();
                        } else if (link.getRemoteLink().getPort().getNode().getDomain() == retDomain) {
                            link.getRemoteLink().getPort().removeLink(link.getRemoteLink());

                            linkIter.remove();
                        }
                    }

                    if (port.getLinks().isEmpty()) {
                        portIter.remove();
                    }
                }

                if (node.getPorts().isEmpty()) {
                    nodeIter.remove();
                }
            }
        }

        return retDomain;
    }
}
