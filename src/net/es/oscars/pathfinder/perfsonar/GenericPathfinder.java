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
    private Set<String> opaqueDomains;
    private boolean cacheFailures;
 
    public GenericPathfinder() throws HttpException, IOException {
        this.log = Logger.getLogger(this.getClass());
        this.domains = new HashMap<String, Domain>();
        this.graph = new DefaultDirectedWeightedGraph<String, DefaultEdge>(DefaultEdge.class);
        this.elementWeights = new HashMap<String, Double>();
        this.elementBandwidths = new HashMap<String, Double>();
        this.ignoredElements = new HashSet<String>();
        this.opaqueDomains = new HashSet<String>();
        this.cacheFailures = true;
    }

    public void addDomain(Domain domain) {
        if (this.domains.get(domain.getFQTI()) != null) {
            // XXX: this should remove that domain and re-add it.
            return;
        }

        boolean isOpaque = true;

        // Calculate if it's an opaque topology, i.e. a domain whose internal
        // links have been removed. This allows one to keep topology secret
        // while still permitting basic pathfinding to occur.
        Set<Node> nodes = domain.getNodes();
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
                    } else if (currURN.get("domainFQID").equals(domain.getFQTI())) {
                        // we found an internal link
                        isOpaque = false;
                        this.log.error("We found an internal link: "+remLinkFQTI);
                        break;
                    }
                }

                if (isOpaque == false)
                    break;
            }

            if (isOpaque == false)
                break;
        }

        if (isOpaque) {
            this.opaqueDomains.add(domain.getFQTI());
        }

        this.domains.put(domain.getFQTI(), domain);
        this.addDomainToGraph(domain, isOpaque);

        if (this.log.isDebugEnabled()) {
            this.log.debug("Domains: ");
            Iterator<String> iter = this.domains.keySet().iterator();
            int i = 0;
            while(iter.hasNext()) {
                String key = iter.next();
                this.log.debug(i+"). "+key);
                i++;
            }

            this.log.debug("Opaque Domains: ");
            Iterator<String> iter2 = this.opaqueDomains.iterator();
            int j = 0;
            while(iter2.hasNext()) {
                String key = iter2.next();
                this.log.debug(j+"). "+key);
                j++;
            }
        }
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

    private void addDomainToGraph(Domain domain, boolean isOpaque) {
        this.log.debug("addDomainToGraph.start");

        DefaultEdge edge;
        Set<Node> nodes;

        // This won't link together the remoteLinkIds and the port, so we do a
        // two phase approach. First, go through and add all the ports to the
        // graph, and then do a subsequent pass to link the ports together.
        String domFQTI = domain.getFQTI();

        // we need to add the domains so that searches can be done like
        // "how do i get from domain A to domain B".
        this.log.debug("Adding vertex "+domFQTI);
        this.graph.addVertex(domFQTI);

        nodes = domain.getNodes();
        for (Node node : nodes) {
            String nodeFQTI = node.getFQTI();

            this.log.debug("Adding vertex "+nodeFQTI);
            this.graph.addVertex(nodeFQTI);

            // The edges will only be one way though, node -> domain. If
            // they went both ways, a valid path could be found by finding
            // a path to one node in the domain and finding a path from a
            // different node in the domain, even though there was no path
            // between the two nodes.
            this.graph.addEdge(nodeFQTI, domFQTI);

            // If it's an opaque instance, we won't know any internal links, so
            // we have to assume that if we can get to a domain, we can get out
            // of it via any external link.
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

                this.elementBandwidths.put(portFQTI, new Double(capacity));

                this.log.debug("Adding vertex "+portFQTI);
                this.graph.addVertex(portFQTI);

                this.graph.addEdge(nodeFQTI, portFQTI);
                this.graph.addEdge(portFQTI, nodeFQTI);

                Set<Link> links = port.getLinks();
                for (Link link : links) {
                    String linkFQTI = link.getFQTI();

                    this.log.debug("Adding vertex "+linkFQTI);
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

                    this.log.debug("Adding edge "+linkFQTI+"->"+remLinkFQTI);
                    this.graph.addEdge(linkFQTI, remLinkFQTI);
                    this.elementWeights.put(linkFQTI, edgeWeight);
                }
            }
        }

        this.log.debug("addDomainToGraph.finish");
    }

    public List<String> lookupPath(String src, String dst, double bandwidth) throws PathfinderException {
        PriorityQueue<String> elements = new PriorityQueue<String>(this.graph.vertexSet().size() + 1, this);
        Map<String, String> prevMap = new HashMap<String, String>();
        this.costs = new HashMap<String, Double>(); // reset the costs

        List<String> header = new ArrayList<String>(); // reset the costs
        List<String> footer = new ArrayList<String>(); // reset the costs

        this.log.info("Looking up path between "+src+" and "+dst);

        long startTime = System.currentTimeMillis();

        // if the source is opaque, we have to assume that any element we're
        // given exists in the domain. 
        boolean srcOpaque = false;
        Hashtable<String, String> srcURN = URNParser.parseTopoIdent(src);
        if (srcURN.get("error") != null) {
            this.log.error("Parsing failed "+srcURN.get("error"));
            throw new PathfinderException("Couldn't parse identifier: "+src);
        }

        if (this.opaqueDomains.contains(srcURN.get("domainFQID"))) {
            // the source is opaque, so we add any elements in the that exist
            // in our URN, but don't exist in our knowledge of the domain, to a
            // header. These will get added to the front of our returned path.
            // We then set the src to an element that does exist. After we have
            // done the search, we will put the header back onto the returned
            // path.

            String[] types = { "linkFQID", "portFQID", "nodeFQID", "domainFQID" };
            for( String elementType : types) {
                String elementId = srcURN.get(elementType);

                if (elementId == null) {
                    continue;
                }

                if (this.graph.containsVertex(elementId) == false) {
                    header.add(elementId);
                } else {
                    this.log.debug("New src: "+src);
                    src = elementId;
                }
            }
        }

        // if the destination is opaque, we have to assume that any element
        // we're given exists in the domain. 
        boolean dstOpaque = false;
        Hashtable<String, String> dstURN = URNParser.parseTopoIdent(dst);
        if (dstURN.get("error") != null) {
            this.log.error("Parsing failed "+dstURN.get("error"));
            throw new PathfinderException("Couldn't parse identifier: "+dst);
        }

        if (this.opaqueDomains.contains(dstURN.get("domainFQID"))) {
            // the destination is opaque, so we add any elements in the that
            // exist in our URN, but don't exist in our knowledge of the
            // domain, to a header. These will get added to the front of our
            // returned path.  We then set the dst to an element that does
            // exist. After we have done the search, we will put the header
            // back onto the returned path.
            String[] types = { "linkFQID", "portFQID", "nodeFQID", "domainFQID" };
            for( String elementType : types) {
                String elementId = dstURN.get(elementType);

                if (elementId == null) {
                    continue;
                }

                if (this.graph.containsVertex(elementId) == false) {
                    footer.add(0, elementId);
                } else {
                    dst = elementId;
                    this.log.debug("New dst: "+dst);
                }
            }
        }

        String id;

        this.costs.put(src, 0.0);
        elements.add(src);

        while ((id = elements.poll()) != null) {
            this.log.debug("Found element: "+id+" -- "+bandwidth);

            if (this.elementBandwidths.get(id) != null) {
                Double elemBandwidth = this.elementBandwidths.get(id);
                if (elemBandwidth.doubleValue() < bandwidth) {
                    this.log.debug("Selected element "+id+" does not have enough bandwidth to satisfy the request: "+elemBandwidth.doubleValue()+" < "+bandwidth);
                    continue;
                }
            }

            if (id.equals(src) == false && id.equals(dst) == false && this.ignoredElements.contains(id) == true) {
                this.log.debug("Selected element "+id+" is being ignored.");
                continue;
            }

            if (id.equals(dst)) {
                break;
            }

            if (this.log.isDebugEnabled()) {
                int i;

                this.log.debug("Selected "+id);
                this.log.debug("Priority("+elements.size()+"/"+costs.size()+"): ");
                Iterator<String> iter = elements.iterator();
                i = 0;
                while(iter.hasNext()) {
                    String key = iter.next();
                    this.log.debug(i+"). "+key+" -- "+costs.get(key));
                    i++;
                }

                this.log.debug("Costs("+costs.size()+"): ");
                Iterator<String> iter2 = costs.keySet().iterator();
                i = 0;
                while(iter2.hasNext()) {
                    String key = iter2.next();
                    this.log.debug(i+"). "+key+" -- "+costs.get(key));
                    i++;
                }

                this.log.debug("Domains("+this.domains.size()+"): ");
                Iterator<String> iter3 = domains.keySet().iterator();
                i = 0;
                while(iter3.hasNext()) {
                    String key = iter3.next();
                    this.log.debug(i+"). "+key);
                    i++;
                }
            }

            Hashtable<String, String> currURN = URNParser.parseTopoIdent(id);
            if (currURN.get("error") != null) {
                this.log.error("Parsing failed "+currURN.get("error"));
                throw new PathfinderException("Couldn't parse identifier: "+id);
            }

            String currDomain = currURN.get("domainFQID");
            if (this.domains.get(currDomain) == null) {
		long stime1 = System.currentTimeMillis();

                Domain domain = lookupDomain(currDomain);
                if (domain == null) {
                    if (this.cacheFailures) {
                        Domain junk = new Domain();
                        this.domains.put(currDomain, junk);
                    }
                } else {
                    this.addDomain(domain);

                    this.log.debug("Destination domain: "+dstURN.get("domainFQID"));

                    if (this.opaqueDomains.contains(dstURN.get("domainFQID"))) {
                        // the destination is opaque, so we add any elements in the that
                        // exist in our URN, but don't exist in our knowledge of the
                        // domain, to a header. These will get added to the front of our
                        // returned path.  We then set the dst to an element that does
                        // exist. After we have done the search, we will put the header
                        // back onto the returned path.
                        this.log.debug("Opaque domains now contains what we're looking for");

                        String[] types = { "linkFQID", "portFQID", "nodeFQID", "domainFQID" };
                        for( String elementType : types) {
                            String elementId = dstURN.get(elementType);

                            this.log.debug("Element ID: "+elementId+" -- "+elementType);

                            if (elementId == null) {
                                    continue;
                            }

                            if (this.graph.containsVertex(elementId) == false) {
                                    footer.add(0, elementId);
                            } else {
                                    dst = elementId;
                                    this.log.debug("New dst: "+dst);
                            }
                        }
                    }
                }

		long etime1 = System.currentTimeMillis();
		System.out.println("Time to get "+currDomain +": "+((etime1-stime1)/1000));
            }

            if (this.graph.containsVertex(id) == false) {
                this.log.error("Couldn't find "+id);
            }

            Double cost = costs.get(id);

            for (DefaultEdge e : this.graph.edgesOf(id)) {
                String target = this.graph.getEdgeTarget(e);

                if (costs.get(target) == null) {
                    Double targetConst = this.elementWeights.get(target);
                    if (targetConst == null) {
                        targetConst = new Double(0);
                    }

                    this.log.debug("Adding '"+target+"' to queue: "+(cost.longValue() + this.graph.getEdgeWeight(e)));
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
        id = dst;
        while((id = prevMap.get(id)) != null) {
            retIds.add(0, id);
        }

        retIds.addAll(0, header);
        retIds.addAll(footer);

        if (this.log.isDebugEnabled()) {
            int i = 0;
            for(String currId : retIds) {
                this.log.debug("Path("+i+"): "+currId);
                i++;
            }
        }

        long endTime = System.currentTimeMillis();

        System.out.println("Time: "+((startTime-endTime)/1000));

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
