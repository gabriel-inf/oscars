package net.es.oscars.pathfinder.perfsonar;

import java.util.Hashtable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Map;
import java.util.Comparator;

import java.io.IOException;
import org.apache.commons.httpclient.HttpException;

import net.es.oscars.bss.topology.Domain;
import net.es.oscars.bss.topology.Node;
import net.es.oscars.bss.topology.Port;
import net.es.oscars.bss.topology.Link;
import net.es.oscars.pathfinder.*;
import net.es.oscars.pathfinder.perfsonar.util.*;


import net.es.oscars.bss.topology.URNParser;

import java.util.List;
import org.apache.log4j.*;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultEdge;


/**
 * GenericPathfinder is a Pathfinding class that, given a topology, can be used
 * to find paths between specified pair of points. By default this class can
 * only find paths between domains that it has been told about. However, the
 * class can be subclassed and the 'lookupDomain' function overridden to allow
 * it to look up domains as it is searching.
 *
 * @author Aaron Brown (aaron@internet2.edu)
 */
public class GenericPathfinder {
    private Logger log;
    private HashMap<String, Domain> domains;
    private DefaultDirectedWeightedGraph<String, DefaultEdge> graph;
    private Map<String, Double> costs;
    private Map<String, Double> elementCosts;
    private Map<String, Double> elementBandwidths;
    private Set<String> ignoredElements;
    private Set<String> opaqueDomains;
    private boolean cacheFailures;
    private ArrayList<DomainFinder> domainFinders;
    private final double DEFAULT_NONLINK_COST = 0.01;
    private final double DEFAULT_LINK_COST = 10;
    private final double OPAQUE_ESTIMATED_DOMAIN_LINKS = 7;
 
    public GenericPathfinder() throws HttpException, IOException {
        this.log = Logger.getLogger(this.getClass());
        this.domains = new HashMap<String, Domain>();
        this.graph = new DefaultDirectedWeightedGraph<String, DefaultEdge>(DefaultEdge.class);
        this.elementCosts = new HashMap<String, Double>();
        this.elementBandwidths = new HashMap<String, Double>();
        this.ignoredElements = new HashSet<String>();
        this.opaqueDomains = new HashSet<String>();
        this.cacheFailures = true;
        this.domainFinders = new ArrayList<DomainFinder>();
    }

    public void addDomainFinder(DomainFinder df) {
        this.domainFinders.add(df);
    }

    public void addDomainFinder(DomainFinder df, int priority) {
        this.domainFinders.add(priority, df);
    }

    public void removeDomainFinder(DomainFinder df) {
        this.domainFinders.remove(df);
    }

    /**
     * Adds the specified Domain object to the set of graphs to search. This
     * function will check whether the given Domain is opaque, take note of
     * that fact and then adds it to the internal graph that it builds of the
     * connections between elements.
     */
    public void addDomain(Domain domain) {
        if (this.domains.get(domain.getFQTI()) != null) {
            // XXX: this should remove that domain and re-add it.
            return;
        }
        
        //If its the local domain then its not opaque, it just has one node
        boolean isOpaque = domain.isLocal() ? false : true;
        
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

    /**
     * Tells the pathfinder to not use that element when searching for paths.
     * @param element A topology identifier for the element to ignore
     */
    public void ignoreElement(String element) {
        this.ignoredElements.add(element);
    }

    /**
     * Retrieves the current cost assigned to the element
     * @param element A topology identifier for the element to ignore
     */
    public double getElementCost(String element) {
        if (this.elementCosts.get(element) != null) {
            return this.elementCosts.get(element).doubleValue();
        }

        return 0;
    }

    /**
     * Sets the cost of the specified element
     * @param element The topology identifier for the element
     * @param cost A double containing the specified cost
     */
    public void setElementCost(String element, double cost) {
        this.elementCosts.put(element, cost);
    }

    /**
     * Retrieves the bandwidth for the specified element. This bandwidth is
     * used to see whether or not a given element can be used when calculating
     * a path with a certain bandwidth. If set to 0, the bandwidth is assumed
     * infinite.
     *
     * @param element The topology identifier for the element
     */
    public double getElementBandwidth(String element) {
        if (this.elementBandwidths.get(element) != null) {
            return this.elementBandwidths.get(element).doubleValue();
        }

        return 0;
    }

    /**
     * Sets the bandwidth for the specified element. This bandwidth will
     * be used to see whether or not a given element can be used when
     * calculating a path with a certain bandwidth. If set to 0, the bandwidth
     * is assumed infinite.
     *
     * @param element The topology identifier for the element
     * @param cost A double containing the specified cost
     */
    public void setElementBandwidth(String element, double bandwidth) {
        this.elementBandwidths.put(element, new Double(bandwidth));
    }

    /**
     * Finds a path between the source and destination with the specified
     * bandwidth. This function performs a Dijkstra's search of the given
     * domains, retrieving new ones automatically if the lookupDomain function
     * has been overridden. If either the source or the destination domains are
     * opaque, the pathfinder assumes that the specified hierarchy of elements
     * exist in the opaque domains even if one of more of the elements is not
     * known to the pathfinders. Thus, a returned path may include elements
     * that do not actually exist in the known domain.
     *
     * @param src The topology identifier for the starting point
     * @param dst The topology identifier for the ending point
     * @param bandwidth The bandwidth required for this path
     * @return A List containing the identifiers, in order, for the path between the source and destination
     */
    public List<String> lookupPath(String src, String dst, double bandwidth) throws PathfinderException {
        ElementCostComparator comparator = new ElementCostComparator();
        PriorityQueue<String> elements = new PriorityQueue<String>(this.graph.vertexSet().size() + 1, comparator);
        Map<String, String> prevMap = new HashMap<String, String>();
        this.costs = new HashMap<String, Double>(); // reset the costs

        List<String> header = new ArrayList<String>(); // reset the costs
        List<String> footer = new ArrayList<String>(); // reset the costs

        this.log.info("Looking up path between "+src+" and "+dst);

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
                Domain retDomain = null;

                long measSTime = System.currentTimeMillis();
                for(DomainFinder df : this.domainFinders) {
                    Domain domain = df.lookupDomain(currDomain);

                    if (domain != null) {
                        retDomain = domain;
                        break;
                    }
                }
                long measETime = System.currentTimeMillis();

                this.log.debug("Time to discover "+currDomain+": "+(measETime-measSTime));

                if (retDomain == null) {
                    if (this.cacheFailures) {
                        Domain junk = new Domain();
                        this.domains.put(currDomain, junk);
                    }
                } else {
                    this.addDomain(retDomain);

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
            }

            if (this.graph.containsVertex(id) == false) {
                this.log.error("Couldn't find "+id);
            }

            Double cost = costs.get(id);

            for (DefaultEdge e : this.graph.edgesOf(id)) {
                String target = this.graph.getEdgeTarget(e);

                if (costs.get(target) == null) {
                    Double targetCost = this.elementCosts.get(target);

                    this.log.debug("Adding '"+target+"' to queue");
                    this.log.debug("Adding '"+target+"' to queue: "+(cost.longValue() + targetCost));
                    costs.put(target, new Double(cost.doubleValue() + targetCost));
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

        return retIds;
    }

    /**
     * Adds the specified domain to the internal graph. This
     * method iterates through the domain, placing each element as a vertex in
     * the graph, and describing each connection between them with an edge.
     *
     * @param isOpaque A boolean stating whether the given Domain is opaque
     */
    private void addDomainToGraph(Domain domain, boolean isOpaque) {
        this.log.debug("addDomainToGraph.start");

        DefaultEdge edge;
        Set<Node> nodes;

        // This won't link together the remoteLinkIds and the port, so we do a
        // two phase approach. First, go through and add all the ports to the
        // graph, and then do a subsequent pass to link the ports together.
        String domFQTI = domain.getFQTI();

        // we need to add the domains so that searches can be done like
        // "how do i get from domain A to domain B" or "how do I get to some
        // element X in the opaque domain Y"
        this.log.debug("Adding vertex "+domFQTI);

        this.graph.addVertex(domFQTI);

        double domCost;
        if (isOpaque) {
            // each bidirectional link consists of, on average, 5 elements: 2 ports, 2 uni-links and 1 node.
            domCost = this.DEFAULT_LINK_COST * (this.OPAQUE_ESTIMATED_DOMAIN_LINKS * 2) +
                                this.DEFAULT_NONLINK_COST * (this.OPAQUE_ESTIMATED_DOMAIN_LINKS * 3);
        } else {
            domCost = DEFAULT_NONLINK_COST;
        }

        this.elementCosts.put(domFQTI, new Double(domCost));

        nodes = domain.getNodes();
        for (Node node : nodes) {
            String nodeFQTI = node.getFQTI();

            this.log.debug("Adding vertex "+nodeFQTI);
            this.graph.addVertex(nodeFQTI);

            this.elementCosts.put(nodeFQTI, new Double(this.DEFAULT_NONLINK_COST));

            // The edges will only be one way though, node -> domain. If
            // they went both ways, a valid path could be found by finding
            // a path to one node in the domain and finding a path from a
            // different node in the domain, even though there was no path
            // between the two nodes.
            this.graph.addEdge(nodeFQTI, domFQTI);

            // If it's an opaque instance, we won't know any internal links, so
            // we have to assume that if we can get to a domain, we can reach
            // any node inside that domain.
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
                this.elementCosts.put(portFQTI, new Double(this.DEFAULT_NONLINK_COST));

                this.graph.addEdge(nodeFQTI, portFQTI);
                this.graph.addEdge(portFQTI, nodeFQTI);

                Set<Link> links = port.getLinks();
                for (Link link : links) {
                    String linkFQTI = link.getFQTI();

                    this.log.debug("Adding vertex "+linkFQTI);
                    this.graph.addVertex(linkFQTI);

                    double linkCost = this.DEFAULT_LINK_COST;
                    if (link.getTrafficEngineeringMetric() != null) {
                        linkCost = this.parseTEM(link.getTrafficEngineeringMetric());
                    }

                    this.elementCosts.put(linkFQTI, linkCost);

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

                        this.elementCosts.put(remLinkFQTI, this.DEFAULT_LINK_COST);
                    }

                    this.log.debug("Adding edge "+linkFQTI+"->"+remLinkFQTI);
                    this.graph.addEdge(linkFQTI, remLinkFQTI);
                }
            }
        }

        this.log.debug("addDomainToGraph.finish");
    }

    /**
     * Parses a traffic engineering metric into a cost.
     *
     * @param trafficEngineeringMetric A string containing the specified TEM
     */
    private double parseTEM(String trafficEngineeringMetric) {
        double cost = new Double(trafficEngineeringMetric).doubleValue();
        this.log.debug("parsed TEM: "+cost);
        return cost;
    }

    private class ElementCostComparator implements Comparator {
        /**
         * Compares the cost between two identifiers. This method is called by the
         * Priority Queue to compare the costs between two elements.
         *
         * @param left A string containing a topology identifier
         * @param right A string containing a topology identifier
         */
        public int compare(Object left, Object right) {
            String left_str = (String) left;
            String right_str = (String) right;
            int res = (int) (costs.get(left) - costs.get(right));
          
            if (res == 0)
                res = left_str.compareTo(right_str); 

            return res;
        }
    }
}