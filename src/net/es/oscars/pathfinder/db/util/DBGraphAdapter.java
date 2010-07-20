package net.es.oscars.pathfinder.db.util;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.ReservationDAO;
import net.es.oscars.bss.topology.Domain;
import net.es.oscars.bss.topology.DomainDAO;
import net.es.oscars.bss.topology.Link;
import net.es.oscars.bss.topology.Node;
import net.es.oscars.bss.topology.Path;
import net.es.oscars.bss.topology.PathElem;
import net.es.oscars.bss.topology.PathType;
import net.es.oscars.bss.topology.Port;

import org.apache.log4j.Logger;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;


public class DBGraphAdapter {
    private String dbname;
    private Logger log;


    public DBGraphAdapter(String dbname) {
        this.dbname = dbname;
        this.log = Logger.getLogger(this.getClass());
    }

    public DefaultDirectedWeightedGraph<String, DefaultWeightedEdge>
        dbToGraph(Long bandwidth, Long startTime, Long endTime,
                  Reservation reservationToIgnore,
                  HashMap<String, Double> objectsToReweigh,
                  HashMap<String, Long> alreadyReserved) {

        this.log.debug("dbToGraph.start");
        DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> g =
            new DefaultDirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);

        DomainDAO domainDAO = new DomainDAO(this.dbname);
        ReservationDAO resvDAO = new ReservationDAO(this.dbname);
        ArrayList<Reservation> reservations = new ArrayList<Reservation>(resvDAO.overlappingReservations(startTime, endTime));
        HashMap<Port, Long> portRsvBw = new HashMap<Port, Long>();
        for (Reservation resv : reservations) {
            if (reservationToIgnore != null &&
                    resv.getGlobalReservationId().equals(reservationToIgnore.getGlobalReservationId())) {
                // should not look at this one
            } else {
                this.log.debug("Found overlapping reservation: "+resv.getGlobalReservationId());
                Long bw = resv.getBandwidth();
                Path path = null;
                // FIXME: better error handling
                try {
                    path = resv.getPath(PathType.LOCAL);
                } catch (BSSException ex) {
                    this.log.error(ex);
                    return null;
                }
                List<PathElem> pathElems = path.getPathElems();
                for (PathElem pathElem: pathElems) {
                    Link link = pathElem.getLink();
                    if (link == null) {
                        link = domainDAO.getFullyQualifiedLink(pathElem.getUrn());
                    }
                    if (link == null) {
                        this.log.debug("Could not resolve link for urn:"+pathElem.getUrn());
                        return null;
                    }
                    Port port = link.getPort();
                    if (portRsvBw.containsKey(port)) {
                        Long newbw = bw + portRsvBw.get(port);
                        portRsvBw.put(port, newbw);
                    } else {
                        portRsvBw.put(port, bw);
                    }
                }
            }
        }
        /*
        Iterator it = portRsvBw.keySet().iterator();
        while (it.hasNext()) {
            Port p = (Port) it.next();
            Long bw = portRsvBw.get(p);
            System.out.println("port: "+p.getFQTI()+ " bw: "+bw/1000000+"Mbps");
        }
        */

        DefaultWeightedEdge edge;
        List<Domain> domains = domainDAO.list();
        for (Domain dom : domains) {
            Iterator<?> nodeIt = dom.getNodes().iterator();
            while (nodeIt.hasNext()) {
                Node node = (Node) nodeIt.next();
                if (!node.isValid()) {
                    continue;
                }
                String nodeFQTI = node.getFQTI();
                Double nodeMult = objectsToReweigh.get(nodeFQTI);
                if (nodeMult != null && nodeMult < 0d) {
                    continue;
                } else if (nodeMult == null) {
                    nodeMult = 1d;
                }

                if (!node.getDomain().isLocal()) {
                    nodeMult = 20d;
                }
                g.addVertex(nodeFQTI);
                Iterator<?> portIt = node.getPorts().iterator();
                while (portIt.hasNext()) {
                    Port port = (Port) portIt.next();
                    if (!port.isValid()) {
                        continue;
                    }
                    String portFQTI = port.getFQTI();

                    Double portMult = objectsToReweigh.get(portFQTI);
                    if (portMult != null && portMult < 0d) {
                        continue;
                    } else if (portMult == null) {
                        portMult = 1d;
                    }
                    Long portCapacity = port.getCapacity();
                    Long reservedCapacity = portRsvBw.get(port);
                    if (reservedCapacity == null) {
                        reservedCapacity = 0L;
                    }
                    if (alreadyReserved != null && alreadyReserved.get(portFQTI) != null) {
                        reservedCapacity += alreadyReserved.get(portFQTI);
                    }
                    Long remainingCapacity = portCapacity - reservedCapacity;
                    if (bandwidth > 0L && remainingCapacity < bandwidth) {
//                        System.out.println("port: "+portFQTI+" rsv: "+reservedCapacity+" rem: "+remainingCapacity);
                        continue;
                    } else {
//                        System.out.println("port: "+portFQTI+" in graph");
                    }
                    Double portEdgeCost = 0.1d * portMult;
                    g.addVertex(portFQTI);
                    edge = g.addEdge(nodeFQTI, portFQTI);
                    if (edge != null) {
                        g.setEdgeWeight(edge, portEdgeCost);
                    }
                    edge = g.addEdge(portFQTI, nodeFQTI);
                    if (edge != null) {
                        g.setEdgeWeight(edge, portEdgeCost);
                    }
                    Iterator<?> linkIt = port.getLinks().iterator();
                    while (linkIt.hasNext()) {
                        Link link = (Link) linkIt.next();
                        if (!link.isValid()) {
                            continue;
                        }
                        String linkFQTI = link.getFQTI();
                        Double linkMult = objectsToReweigh.get(linkFQTI);
                        if (linkMult != null && linkMult < 0d) {
                            continue;
                        } else if (linkMult == null) {
                            linkMult = 1d;
                        }
    //                	System.out.println(linkFQTI);
                        g.addVertex(linkFQTI);
                        edge = g.addEdge(linkFQTI, portFQTI);
                        if (edge != null) {
                            g.setEdgeWeight(edge, 0.1d);
                        }
                        edge = g.addEdge(portFQTI, linkFQTI);
                        if (edge != null) {
                            g.setEdgeWeight(edge, 0.1d);
                        }
                        if (link.getRemoteLink() != null) {
                            Link remLink = link.getRemoteLink();
                            String remLinkFQTI = remLink.getFQTI();
                            if (remLink.isValid() && remLinkFQTI != null) {
                                Double edgeWeight = 10d;
                                if (link.getTrafficEngineeringMetric() != null) {
                                    edgeWeight = this.parseTEM(link.getTrafficEngineeringMetric());
                                }
                                // testing
                                edgeWeight = edgeWeight * nodeMult * linkMult;
                                g.addVertex(remLinkFQTI);
                                edge = g.addEdge(linkFQTI, remLinkFQTI);
                                if (edge != null) {
                                    g.setEdgeWeight(edge, edgeWeight);
                                }
                            }
                        }
                    }
                }
            }
        }
        this.log.debug("dbToGraph.start");
        // this.sf.getCurrentSession().getTransaction().commit();
        return g;
    }

    double parseTEM(String trafficEngineeringMetric) {
        double weight = new Double(trafficEngineeringMetric).doubleValue();
        return weight;
    }
}
