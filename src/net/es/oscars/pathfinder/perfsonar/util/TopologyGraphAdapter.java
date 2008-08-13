package net.es.oscars.pathfinder.perfsonar.util;

import java.util.*;

import net.es.oscars.PropHandler;
import net.es.oscars.bss.*;
import net.es.oscars.bss.topology.*;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.database.Initializer;

import org.apache.log4j.Logger;
//import org.hibernate.SessionFactory;
import org.jgrapht.*;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import net.es.oscars.bss.topology.*;

public class TopologyGraphAdapter {
    private Logger log;
    private String dbname;

    public TopologyGraphAdapter(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.dbname = dbname;
        this.log = Logger.getLogger(this.getClass());
    }

    public DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> genGraph(Topology topology, Long bandwidth, Long startTime, Long endTime, Reservation reservationToIgnore) {
        this.log.debug("genGraph.start");
        DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> g =
            new DefaultDirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);

        // Go through the reservations and produce a map of how much bandwidth
        // is being used by each port we know about.
        ReservationDAO resvDAO = new ReservationDAO(this.dbname);
        ArrayList<Reservation> reservations = new ArrayList<Reservation>(resvDAO.overlappingReservations(startTime, endTime));
        HashMap<String, Long> portRsvBw = new HashMap<String, Long>();
        for (Reservation resv : reservations) {
            if (reservationToIgnore != null &&
                    resv.getGlobalReservationId().equals(reservationToIgnore.getGlobalReservationId())) {
                // should not look at this one
            } else {
                this.log.debug("Found overlapping reservation: "+resv.getGlobalReservationId());
                Long bw = resv.getBandwidth();
                net.es.oscars.bss.topology.Path path = resv.getPath();
                net.es.oscars.bss.topology.PathElem pathElem = path.getPathElem();
                while (pathElem != null) {
                    net.es.oscars.bss.topology.Link link = pathElem.getLink();
                    String portId = link.getPort().getFQTI();
                    if (portRsvBw.containsKey(portId)) {
                        Long newbw = bw + portRsvBw.get(portId);
                        portRsvBw.put(portId, newbw);
                    } else {
                        portRsvBw.put(portId, bw);
                    }
                    pathElem = pathElem.getNextElem();
                }
            }
        }

        /*
        Iterator it = portRsvBw.keySet().iterator();
        while (it.hasNext()) {
            String p = (String) it.next();
            Long bw = portRsvBw.get(p);
            this.log.debug("port: " + p + " bw: "+bw/1000000+"Mbps");
        }
        */

        DefaultWeightedEdge edge;

        // This won't link together the remoteLinkIds and the port, so we do a
        // two phase approach. First, go through and add all the ports to the
        // graph, and then do a subsequent pass to link the ports together.
        List<Domain> domains = topology.getDomains();
        for (Domain dom : domains) {
            String domFQTI = dom.getFQTI();

            // we need to add the domains so that searches can be done like
            // "how do i get from domain A to domain B".
            this.log.debug("Adding vertex "+domFQTI);
            g.addVertex(domFQTI);

            Set<Node> nodes = dom.getNodes();
            for (Node node : nodes) {
                String nodeFQTI = node.getFQTI();

                this.log.debug("Adding vertex "+nodeFQTI);
                g.addVertex(nodeFQTI);

                // The edges will only be one way though, node -> domain. If
                // they went both ways, a valid path could be found by finding
                // a path to one node in the domain and finding a path from a
                // different node in the domain, even though there was no path
                // between the two nodes.
                edge = g.addEdge(nodeFQTI, domFQTI);
                if (edge != null) {
                    g.setEdgeWeight(edge, 0.1d);
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

                    Long reservedCapacity = portRsvBw.get(portFQTI);
                    if (reservedCapacity == null) {
                        reservedCapacity = 0L;
                    }

                    Long remainingCapacity = capacity - reservedCapacity;

                    if (bandwidth > 0 && remainingCapacity < bandwidth) {
                        continue;
                    }

                    this.log.debug("Adding vertex "+portFQTI);
                    g.addVertex(portFQTI);

                    edge = g.addEdge(nodeFQTI, portFQTI);
                    if (edge != null) {
                        g.setEdgeWeight(edge, 0.1d);
                    }

                    edge = g.addEdge(portFQTI, nodeFQTI);
                    if (edge != null) {
                        g.setEdgeWeight(edge, 0.1d);
                    }

                    Set<Link> links = port.getLinks();
                    for (Link link : links) {
                        String linkFQTI = link.getFQTI();

                        this.log.debug("Adding vertex "+linkFQTI);
                        g.addVertex(linkFQTI);

                        edge = g.addEdge(linkFQTI, portFQTI);
                        if (edge != null) {
                            g.setEdgeWeight(edge, 0.1d);
                        }

                        edge = g.addEdge(portFQTI, linkFQTI);
                        if (edge != null) {
                            g.setEdgeWeight(edge, 0.1d);
                        }
                    }
                }
            }
        }

        for (Domain dom : domains) {

            Set<Node> nodes = dom.getNodes();
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

                        if (g.containsVertex(remLinkFQTI) == false || g.containsVertex(linkFQTI) == false)
                            continue;

                        Double edgeWeight = 10d;
                        if (link.getTrafficEngineeringMetric() != null) {
                            edgeWeight = this.parseTEM(link.getTrafficEngineeringMetric());
                        }

                        this.log.debug("Adding edge "+linkFQTI+"->"+remLinkFQTI);
                        edge = g.addEdge(linkFQTI, remLinkFQTI);
                        if (edge != null) {
                            g.setEdgeWeight(edge, edgeWeight);
                        }
                    }
                }
            }
        }

        this.log.debug("genGraph.finish");
        return g;

    }

    double parseTEM(String trafficEngineeringMetric) {
        double weight = new Double(trafficEngineeringMetric).doubleValue();
        return weight;
    }
}

