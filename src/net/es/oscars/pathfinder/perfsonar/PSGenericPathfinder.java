package net.es.oscars.pathfinder.perfsonar;

import java.util.Hashtable;
import java.util.HashMap;
import java.util.Properties;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Map;
import java.util.Comparator;

import java.io.IOException;
import org.apache.commons.httpclient.HttpException;

import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.topology.RouteElem;
import net.es.oscars.bss.topology.DomainDAO;
import net.es.oscars.bss.topology.Domain;
import net.es.oscars.bss.topology.Node;
import net.es.oscars.bss.topology.Port;
import net.es.oscars.bss.topology.Link;
import net.es.oscars.bss.topology.TopologyUtil;
import net.es.oscars.pathfinder.*;
import net.es.oscars.pathfinder.traceroute.*;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.PropHandler;

import net.es.oscars.pathfinder.perfsonar.util.PSGraphEdge;
import net.es.oscars.bss.topology.URNParser;

import org.jdom.*;

import edu.internet2.perfsonar.*;

import net.es.oscars.bss.topology.*;
import net.es.oscars.oscars.*;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;

import java.util.List;
import org.apache.log4j.*;

import org.jgrapht.*;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;


/**
 * PSPathfinder finds the route through the domain and toward the destination
 * by consulting a perfSONAR Topology service. It contacts the service and
 * downloads the topology for all domains. It then constructs a graph of the
 * topology and uses dijkstra's shortest path algorithm to finds a path through
 * the domain that follows any path requested by the end user. The user
 * requested paths can consist of domain, node, port and link identifiers.
 *
 * @author Aaron Brown (aaron@internet2.edu)
 */
public class PSGenericPathfinder extends GenericPathfinder {
    private Logger log;
    private TSLookupClient TSClient;
   
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

		System.out.println("Handling section: "+section);

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
                        String [] temp = new String[1];
                        gLSList.toArray(temp);
                        gLSs = temp;
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
                            String [] temp = new String[1];
                            hLSList.toArray(temp);
                            hLSs = temp;
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
                            String [] temp = new String[1];
                            TSList.toArray(temp);
                            TSs = temp;
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

    protected Domain lookupDomain(String id) {
        Element topoXML;

        System.out.println("Looking up domain: "+id);

        try {
            topoXML = this.TSClient.getDomain(id, KNOWN_TOPOLOGY_TYPE);
        } catch (PSException e) {
            this.log.error("PSException while getting domain "+id);
            topoXML = null;
        }

        if (topoXML == null) {
            this.log.error("Couldn't get find domain "+id+" in topology service");
            return null;
        }

        System.out.println("topoXML: "+topoXML);

        TopologyXMLParser parser = new TopologyXMLParser(null);
        Topology topology = parser.parse(topoXML, null);
        if (topology == null) {
            this.log.error("Couldn't parse topology");
            return null;
        }

        System.out.println("Parsed topology");

        List<Domain> domains = topology.getDomains();
        for (Domain dom : domains) {
            String domFQTI = dom.getFQTI();

            if (domFQTI.equals(id)) {
                return dom;
            }
        }

        return null;
    }
}
