package net.es.oscars.pathfinder.perfsonar;

import org.apache.log4j.*;

import java.io.IOException;
import org.apache.commons.httpclient.HttpException;

import edu.internet2.perfsonar.*;

import net.es.oscars.bss.topology.TopologyXMLParser;

import net.es.oscars.bss.topology.Topology;
import net.es.oscars.bss.topology.Domain;
import net.es.oscars.bss.topology.Node;
import net.es.oscars.bss.topology.Port;
import net.es.oscars.bss.topology.Link;

import net.es.oscars.PropHandler;

import java.util.Properties;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import org.jdom.*;

/**
 * PSGenericPathfinder exteds the GenericPathfinder class to support looking up
 * new domains using the perfSONAR Information Services. It uses the perfSONAR
 * Topology Client to lookup the topology using the specified Global Lookup
 * Services, Home Lookup Services or Topology Services. There is a compile-time
 * option that can be set to force the returned Domain to be opaque.
 *
 * @author Aaron Brown (aaron@internet2.edu)
 */
public class PSGenericPathfinder extends GenericPathfinder {
    private Logger log;
    private TSLookupClient TSClient;

    private static final boolean FORCE_OPAQUE = true;
    private static final String KNOWN_TOPOLOGY_TYPE = "http://ogf.org/schema/network/topology/ctrlPlane/20080828/";

    public PSGenericPathfinder() throws HttpException, IOException {
        this.log = Logger.getLogger(this.getClass());

        String[] gLSs = null;
        String[] hLSs = null;
        String[] TSs = null;
        Boolean setUseGlobalLS = null;
        String hints = null;

        String[] sections = { "topology", "lookup" };

        for ( String section : sections) {

                this.log.debug("Handling section: "+section);

                PropHandler propHandler = new PropHandler("oscars.properties");
                Properties props = propHandler.getPropertyGroup(section, true);

                if (hints == null) {
                    hints = props.getProperty("hints");
                }

                int i;

                if (gLSs == null) {
                    i = 1;
                    ArrayList<String> gLSList = new ArrayList<String>();
                    while(props.getProperty("global." + i) != null){
                        gLSList.add(props.getProperty("global." + i));
                        i++;
                    }
                    if(!gLSList.isEmpty()){
                        gLSs = gLSList.toArray(new String[gLSList.size()]);
                    }
                }

                if (hLSs == null) {
                    i = 1;
                    ArrayList<String> hLSList = new ArrayList<String>();
                    while(props.getProperty("home." + i) != null){
                            hLSList.add(props.getProperty("home." + i));
                            i++;
                    }
                    if(!hLSList.isEmpty()){
                            hLSs = hLSList.toArray(new String[hLSList.size()]);
                    }
                }

                if (TSs == null) {
                    i = 1;
                    ArrayList<String> TSList = new ArrayList<String>();
                    while(props.getProperty("topology." + i) != null){
                            TSList.add(props.getProperty("topology." + i));
                            i++;
                    }
                    if(!TSList.isEmpty()){
                            TSs = TSList.toArray(new String[TSList.size()]);
                    }
                }

                if (setUseGlobalLS == null) {
                        String useGlobals = props.getProperty("useGlobal");
                        if(useGlobals != null) {
                                setUseGlobalLS = ("1".equals(useGlobals) || "true".equals(useGlobals));
                        }
                }
        }

        if (setUseGlobalLS == null) {
            setUseGlobalLS = false;
        }

        try{
            if(gLSs != null || hLSs != null || TSs != null){
                this.TSClient = new TSLookupClient(gLSs, hLSs, TSs);
            }else if(hints != null){
                this.TSClient = new TSLookupClient(hints);
            }else{
                this.log.warn("No lookup service information specified, using defaults");
                this.TSClient = new TSLookupClient("http://www.perfsonar.net/gls.root.hints");
            }
        }catch(Exception e){
            this.log.error(e.getMessage());
        }

        this.TSClient.setUseGlobalLS(setUseGlobalLS);
    }

    /**
     * Looks up the given domain using the perfSONAR Information
     * Infrastructure. This method uses the perfSONAR Information
     * Infrastructure to find the desired domain.
     */
    protected Domain lookupDomain(String id) {
        Element topoXML;

        this.log.debug("Looking up domain: "+id);

        try {
            topoXML = this.TSClient.getDomain(id, KNOWN_TOPOLOGY_TYPE);
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
