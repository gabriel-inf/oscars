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
public class PSGenericPathfinder implements Comparator {
    private Logger log;
    private TSLookupClient TSClient;
    private HashMap<String, Domain> domains;
    private DefaultDirectedWeightedGraph<String, PSGraphEdge> graph;
    Map<String, Double> costs;
    Map<String, Double> weightOverrides;
    Map<String, Double> bandwidthOverrides;

    /**
     * Constructor
     *
     * @param dbname the name of the database to use for reservations and
     * getting the local domain.
     */
    public PSGenericPathfinder() throws HttpException, IOException {
        this.log = Logger.getLogger(this.getClass());
        this.TSClient = new TSLookupClient("http://www.perfsonar.net/gls.root.hints");
        this.domains = new HashMap<String, Domain>();
        this.weightOverrides = new HashMap<String, Double>();
        this.bandwidthOverrides = new HashMap<String, Double>();
        this.graph = new DefaultDirectedWeightedGraph<String, PSGraphEdge>(PSGraphEdge.class);
    }

    public void addDomain(Domain domain) {
        if (this.domains.get(domain.getFQTI()) != null)
            return;

        this.domains.put(domain.getFQTI(), domain);
        this.updateGraph(domain);
    }

    public void removeEdge(String src, String dst) {
        if (this.graph.containsEdge(src, dst)) {
            this.graph.removeEdge(src, dst);
        }
    }

    public double getEdgeBandwidth(String src, String dst) {
        PSGraphEdge e = this.graph.getEdge(src, dst);
        if (e == null) {
            return 0.0;
        }

        return e.getBandwidth();
    }

    public int setEdgeBandwidth(String src, String dst, double bandwidth) {
        PSGraphEdge e = this.graph.getEdge(src, dst);
        if (e == null) {
            return -1;
        }

        e.setBandwidth(bandwidth);

        return 0;
    }

    public double getEdgeWeight(String src, String dst) {
        PSGraphEdge e = this.graph.getEdge(src, dst);
        if (e == null) {
            return 0;
        }
        return this.graph.getEdgeWeight(e);
    }

    public int setEdgeWeight(String src, String dst, double weight) {
        PSGraphEdge e = this.graph.getEdge(src, dst);
        if (e == null) {
            return -1;
        }

        this.graph.setEdgeWeight(e, weight);

        return 0;
    }

    private void updateGraph(Domain domain) {
        System.out.println("updateGraph.start");

        PSGraphEdge edge;
	Set<Node> nodes;

        boolean isOpaque = true;

        // This won't link together the remoteLinkIds and the port, so we do a
        // two phase approach. First, go through and add all the ports to the
        // graph, and then do a subsequent pass to link the ports together.
        String domFQTI = domain.getFQTI();

        // Calculate if it's an opaque topology, i.e. a domain whose internal
        // links have been removed. This allows one to keep topology secret
        // while still permitting basic pathfinding to occur.
        nodes = domain.getNodes();
        for (Node node : nodes) {

            Set<Port> ports = node.getPorts();
            for (Port port : ports) {

                Set<Link> links = port.getLinks();
                for (Link link : links) {
                    if (link.getRemoteLink() == null)
                        continue;

                    String remLinkFQTI = link.getRemoteLink().getFQTI();

                    Hashtable<String, String> currURN = URNParser.parseTopoIdent(remLinkFQTI);
                    if (currURN.get("error") != null) {
                        System.out.println("Parsing failed "+currURN.get("error"));
                    } else if (currURN.get("domainFQID").equals(domFQTI)) {
                        // we found an internal link
                        isOpaque = false;
                        break;
                    }
                }

                if (isOpaque == false)
                    break;
            }

            if (isOpaque == false)
                break;
        }
 
        // we need to add the domains so that searches can be done like
        // "how do i get from domain A to domain B".
        System.out.println("Adding vertex "+domFQTI);
        this.graph.addVertex(domFQTI);

        nodes = domain.getNodes();
        for (Node node : nodes) {
            String nodeFQTI = node.getFQTI();

            System.out.println("Adding vertex "+nodeFQTI);
            this.graph.addVertex(nodeFQTI);

            // The edges will only be one way though, node -> domain. If
            // they went both ways, a valid path could be found by finding
            // a path to one node in the domain and finding a path from a
            // different node in the domain, even though there was no path
            // between the two nodes.
            edge = this.graph.addEdge(nodeFQTI, domFQTI);
            if (edge != null) {
                this.graph.setEdgeWeight(edge, 0.1d);
                edge.setBandwidth(Double.MAX_VALUE);
            }

            // If it's an opaque instance, we won't know any internal links, so
            // we have to assume that if we can get to a domain, we can get out
            // of it via an external link.
            if (isOpaque) {
                edge = this.graph.addEdge(domFQTI, nodeFQTI);
                if (edge != null) {
                    this.graph.setEdgeWeight(edge, 0.1d);
                    edge.setBandwidth(Double.MAX_VALUE);
                }
            }

            Set<Port> ports = node.getPorts();
            for (Port port : ports) {
                String portFQTI = port.getFQTI();

                Long capacity = port.getMaximumReservableCapacity();
                if (capacity == 0L) {
                    capacity = port.getCapacity();
                }

                if (capacity == 0L)
                    continue;

                System.out.println("Adding vertex "+portFQTI);
                this.graph.addVertex(portFQTI);

                edge = this.graph.addEdge(nodeFQTI, portFQTI);
                if (edge != null) {
                    this.graph.setEdgeWeight(edge, 0.1d);
                    edge.setBandwidth(capacity);
                }

                edge = this.graph.addEdge(portFQTI, nodeFQTI);
                if (edge != null) {
                    this.graph.setEdgeWeight(edge, 0.1d);
                    edge.setBandwidth(capacity);
                }

                Set<Link> links = port.getLinks();
                for (Link link : links) {
                    String linkFQTI = link.getFQTI();

                    System.out.println("Adding vertex "+linkFQTI);
                    this.graph.addVertex(linkFQTI);

                    edge = this.graph.addEdge(linkFQTI, portFQTI);
                    if (edge != null) {
                        this.graph.setEdgeWeight(edge, 0.1d);
                        edge.setBandwidth(Double.MAX_VALUE);
                    }

                    edge = this.graph.addEdge(portFQTI, linkFQTI);
                    if (edge != null) {
                        this.graph.setEdgeWeight(edge, 0.1d);
                        edge.setBandwidth(Double.MAX_VALUE);
                    }
                }
            }
        }

        nodes = domain.getNodes();
        for (Node node : nodes) {

            Set<Port> ports = node.getPorts();
            for (Port port : ports) {
                String portFQTI = port.getFQTI();

                Set<Link> links = port.getLinks();
                for (Link link : links) {
                    if (link.getRemoteLink() == null)
                        continue;

                    String linkFQTI = link.getFQTI();
                    String remLinkFQTI = link.getRemoteLink().getFQTI();

                    if (graph.containsVertex(linkFQTI) == false)
                        continue;

                    // add empty links so that we know when we'll need to
                    // lookup the next domain.
                    if (graph.containsVertex(remLinkFQTI) == false) {
                        this.graph.addVertex(remLinkFQTI);
                    }

                    Double edgeWeight = 10d;
                    if (link.getTrafficEngineeringMetric() != null) {
                        edgeWeight = this.parseTEM(link.getTrafficEngineeringMetric());
                    }

                    System.out.println("Adding edge "+linkFQTI+"->"+remLinkFQTI);
                    edge = this.graph.addEdge(linkFQTI, remLinkFQTI);
                    if (edge != null) {
                        edge.setBandwidth(Double.MAX_VALUE);
                        this.graph.setEdgeWeight(edge, edgeWeight);
                    }
                }
            }
        }

        System.out.println("updateGraph.finish");
    }

    double parseTEM(String trafficEngineeringMetric) {
        double weight = new Double(trafficEngineeringMetric).doubleValue();
        return weight;
    }

    private void lookupDomain(String id) {
        Element topoXML;

        System.out.println("Looking up domain: "+id);

        try {
            topoXML = this.TSClient.getDomain(id);
        } catch (PSException e) {
            System.out.println("PSException while getting domain "+id);
            topoXML = null;
        }

        if (topoXML == null) {
            System.out.println("ERROR:Couldn't get find domain "+id+" in topology service");
            return;
        }

	System.out.println("topoXML: "+topoXML);

        TopologyXMLParser parser = new TopologyXMLParser(null);
        Topology topology = parser.parse(topoXML, null);
        if (topology == null) {
            System.out.println("ERROR:Couldn't parse topology");
            return;
        }

	System.out.println("Parsed topology");

        List<Domain> domains = topology.getDomains();
        for (Domain dom : domains) {
            String domFQTI = dom.getFQTI();
	    System.out.println("Found domain: "+domFQTI);
            if (this.domains.get(domFQTI) == null) {
                this.domains.put(domFQTI, dom);
                this.updateGraph(dom);
            }
        }
    }

    public List<String> lookupPath(String src, String dst, double bandwidth) throws PathfinderException {
        PriorityQueue<String> elements = new PriorityQueue<String>(this.graph.vertexSet().size() + 1, this);
        Map<String, String> prevMap = new HashMap<String, String>();
        this.costs = new HashMap<String, Double>(); // reset the costs

        System.out.println("Looking up path between "+src+" and "+dst);

        String id;

        this.costs.put(src, 0.0);
        elements.add(src);

        while ((id = elements.poll()) != null) {
            if (id.equals(dst)) {
                break;
            }

            Hashtable<String, String> currURN = URNParser.parseTopoIdent(id);
            if (currURN.get("error") != null) {
                // XXX fail
                System.out.println("Parsing failed "+currURN.get("error"));
                throw new PathfinderException("Couldn't parse identifier: "+id);
            }

            String currDomain = currURN.get("domainFQID");
            if (this.domains.get(currDomain) == null) {
                this.lookupDomain(currDomain);
            }

            if (this.graph.containsVertex(id) == false) {
                System.out.println("Couldn't find "+id);
            }

            double cost = costs.get(id);

            for (PSGraphEdge e : this.graph.edgesOf(id)) {
                if (e.getBandwidth() < bandwidth)
                    continue;

                String target = this.graph.getEdgeTarget(e);

                if (costs.get(target) == null || costs.get(target) > cost + this.graph.getEdgeWeight(e)) {
                    costs.put(target, cost + this.graph.getEdgeWeight(e));
                    prevMap.put(target, id);
                    elements.remove(target);
                    elements.add(target);
                }
            }
        }

        if (prevMap.get(dst) == null) {
            return null;
        }

        List<String> retIds = new ArrayList<String>();
        retIds.add(0, dst);
        System.out.println("Adding(dst): "+dst);
        id = dst;
        while((id = prevMap.get(id)) != null) {
            System.out.println("Adding: "+id);
            retIds.add(0, id);
        }

        return retIds;
    }

    public int compare(Object left, Object right) {
        String left_str = (String) left;
        String right_str = (String) right;
        int res = (int) (this.costs.get(left) - this.costs.get(right));
      
        if (res == 0)
            res = left_str.compareTo(right_str); 

        return res;
    }
}
