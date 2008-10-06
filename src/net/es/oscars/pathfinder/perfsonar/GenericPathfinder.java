package net.es.oscars.pathfinder.perfsonar;

import java.util.Hashtable;
import java.util.HashMap;
import java.util.HashSet;
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
import org.jgrapht.graph.DefaultEdge;


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
public class GenericPathfinder implements Comparator {
    private Logger log;
    private HashMap<String, Domain> domains;
    private DefaultDirectedWeightedGraph<String, DefaultEdge> graph;
    private Map<String, Double> costs;
    private Map<String, Double> elementWeights;
    private Map<String, Double> elementBandwidths;
    private Set<String> ignoredElements;
 
    public GenericPathfinder() throws HttpException, IOException {
        this.log = Logger.getLogger(this.getClass());
        this.domains = new HashMap<String, Domain>();
        this.graph = new DefaultDirectedWeightedGraph<String, DefaultEdge>(DefaultEdge.class);
        this.elementWeights = new HashMap<String, Double>();
        this.elementBandwidths = new HashMap<String, Double>();
        this.ignoredElements = new HashSet<String>();
    }

    public void addDomain(Domain domain) {
        if (this.domains.get(domain.getFQTI()) != null)
            return;

        this.domains.put(domain.getFQTI(), domain);
        this.updateGraph(domain);
    }

    public void ignoreElement(String element) {
        this.ignoredElements.add(element);
    }

    public double getElementWeight(String element) {
        if (this.elementWeights.get(element) != null) {
            return this.elementWeights.get(element).doubleValue();
        }

        return 0;
    }

    public void setElementWeight(String element, double weight) {
        this.elementWeights.put(element, weight);
    }

    public double getElementBandwidth(String element) {
        if (this.elementBandwidths.get(element) != null) {
            return this.elementBandwidths.get(element).doubleValue();
        }

        return 0;
    }

    public void setElementBandwidth(String element, double bandwidth) {
        this.elementBandwidths.put(element, new Double(bandwidth));
    }

    private void updateGraph(Domain domain) {
        System.out.println("updateGraph.start");

        DefaultEdge edge;
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
                        this.log.error("Parsing failed "+currURN.get("error"));
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
            this.graph.addEdge(nodeFQTI, domFQTI);

            // If it's an opaque instance, we won't know any internal links, so
            // we have to assume that if we can get to a domain, we can get out
            // of it via an external link.
            if (isOpaque) {
                this.graph.addEdge(domFQTI, nodeFQTI);
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

                this.graph.addEdge(nodeFQTI, portFQTI);
                this.graph.addEdge(portFQTI, nodeFQTI);

                Set<Link> links = port.getLinks();
                for (Link link : links) {
                    String linkFQTI = link.getFQTI();

                    System.out.println("Adding vertex "+linkFQTI);
                    this.graph.addVertex(linkFQTI);

                    this.graph.addEdge(linkFQTI, portFQTI);
                    this.graph.addEdge(portFQTI, linkFQTI);
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
                    this.graph.addEdge(linkFQTI, remLinkFQTI);
                    this.elementWeights.put(linkFQTI, edgeWeight);
                }
            }
        }

        System.out.println("updateGraph.finish");
    }

    public List<String> lookupPath(String src, String dst, double bandwidth) throws PathfinderException {
        PriorityQueue<String> elements = new PriorityQueue<String>(this.graph.vertexSet().size() + 1, this);
        Map<String, String> prevMap = new HashMap<String, String>();
        this.costs = new HashMap<String, Double>(); // reset the costs

        this.log.info("Looking up path between "+src+" and "+dst);
        System.out.println("Looking up path between "+src+" and "+dst);

        String id;

        this.costs.put(src, 0.0);
        elements.add(src);

        while ((id = elements.poll()) != null) {
            System.out.println("Found element: "+id+" -- "+bandwidth);

            if (this.elementBandwidths.get(id) != null) {
                Double elemBandwidth = this.elementBandwidths.get(id);
                if (elemBandwidth.doubleValue() < bandwidth) {
                    System.out.println("Selected element "+id+" does not have enough bandwidth to satisfy the request: "+elemBandwidth.doubleValue()+" < "+bandwidth);
                    continue;
                }
            }

            if (id.equals(src) == false && id.equals(dst) == false && this.ignoredElements.contains(id) == true) {
                System.out.println("Selected element "+id+" is being ignored.");
                continue;
            }

            if (id.equals(dst)) {
                break;
            }

            int i;

            System.out.println("Selected "+id);
            System.out.println("Priority("+elements.size()+"/"+costs.size()+"): ");
            Iterator<String> iter = elements.iterator();
            i = 0;
            while(iter.hasNext()) {
                String key = iter.next();
                System.out.println(i+"). "+key+" -- "+costs.get(key));
                i++;
            }

            System.out.println("Costs("+costs.size()+"): ");
            Iterator<String> iter2 = costs.keySet().iterator();
            i = 0;
            while(iter2.hasNext()) {
                String key = iter2.next();
                System.out.println(i+"). "+key+" -- "+costs.get(key));
                i++;
            }

            System.out.println("Domains("+this.domains.size()+"): ");
            Iterator<String> iter3 = domains.keySet().iterator();
            i = 0;
            while(iter3.hasNext()) {
                String key = iter3.next();
                System.out.println(i+"). "+key);
                i++;
            }


            Hashtable<String, String> currURN = URNParser.parseTopoIdent(id);
            if (currURN.get("error") != null) {
                this.log.error("Parsing failed "+currURN.get("error"));
                throw new PathfinderException("Couldn't parse identifier: "+id);
            }

            String currDomain = currURN.get("domainFQID");
            if (this.domains.get(currDomain) == null) {
                Domain domain = lookupDomain(currDomain);
                if (domain != null) {
                    this.updateGraph(domain);
                }
            }

            if (this.graph.containsVertex(id) == false) {
                this.log.error("Couldn't find "+id);
            }

            Double cost = costs.get(id);

            for (DefaultEdge e : this.graph.edgesOf(id)) {
                String target = this.graph.getEdgeTarget(e);

                System.out.println("Found target: "+target);

                if (costs.get(target) == null) {
                    Double targetConst = this.elementWeights.get(target);
                    if (targetConst == null) {
                        targetConst = new Double(0);
                    }

                    System.out.println("Adding '"+target+"' to queue: "+(cost.longValue() + this.graph.getEdgeWeight(e)));
                    costs.put(target, new Double(cost.doubleValue() + targetConst));
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

    double parseTEM(String trafficEngineeringMetric) {
        double weight = new Double(trafficEngineeringMetric).doubleValue();
        return weight;
    }

    protected Domain lookupDomain(String id) {
        return null;
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
