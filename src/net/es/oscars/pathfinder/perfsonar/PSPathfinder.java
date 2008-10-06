package net.es.oscars.pathfinder.perfsonar;

import java.util.Hashtable;
import java.util.HashMap;
import java.util.Properties;
import java.util.Iterator;
import java.util.ArrayList;

import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.topology.*;
import net.es.oscars.bss.*;
import net.es.oscars.pathfinder.*;
import net.es.oscars.pathfinder.traceroute.*;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.PropHandler;

import net.es.oscars.bss.topology.URNParser;

import org.jdom.*;

import net.es.oscars.bss.topology.*;
import net.es.oscars.oscars.*;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;

import java.util.List;
import org.apache.log4j.*;

import org.jgrapht.*;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.alg.*;


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
public class PSPathfinder extends Pathfinder implements PCE {
    private Logger log;
    private Domain localDomain;
    private TypeConverter tc;
 
    /**
     * Constructor
     *
     * @param dbname the name of the database to use for reservations and
     * getting the local domain.
     */
    public PSPathfinder(String dbname) {
        super(dbname);
        this.log = Logger.getLogger(this.getClass());

        DomainDAO domDAO = new DomainDAO(dbname);
        this.localDomain = domDAO.getLocalDomain();

        this.tc = OSCARSCore.getInstance().getTypeConverter();
    }

    /**
     * Returns the ingress given a path. Converts hops in path to URNs
     * then passes to superclass
     *
     * @param pathInfo the path from which to extract the needed info
     * @return the ingress linkId of the path
     */
    public String findIngress(PathInfo pathInfo) throws PathfinderException {
        Layer2Info l2Info = pathInfo.getLayer2Info();

        //need to convert to URN
        // if layer2 request then already a URN so pass to super...
        if (l2Info != null) {
            String src = l2Info.getSrcEndpoint();
            String srcURN = this.resolveToFQTI(src);
            CtrlPlaneHopContent[] hops = pathInfo.getPath().getHop();
            for (int i = 0; i < hops.length; i++) {
                hops[i].setLinkIdRef(this.resolveToFQTI(hops[i].getLinkIdRef()));
            }
            pathInfo.getPath().setHop(hops);
            return super.findIngress(srcURN, pathInfo.getPath());

        }

        //...if layer 3 request then pass to TraceroutePathfinder.findIngress
        TraceroutePathfinder tracePF = new TraceroutePathfinder(this.dbname);
        return tracePF.findIngress(pathInfo);
    }

    /**
     * Finds an interdomain path given path information from a
     * createReservation request.
     *
     * @param pathInfo PathInfo instance containing current set of interdomain hops
     * @return a path containing the ingress and egress of the local domain
     * @throws PathfinderException
     */
    public PathInfo findPath(PathInfo pathInfo, Reservation reservation) throws PathfinderException {

        CtrlPlanePathContent interPath = pathInfo.getPath();
        CtrlPlanePathContent intraPath = null;
        Layer2Info layer2Info = pathInfo.getLayer2Info();
        String src = null;
        String dest = null;
        PathInfo intraPathInfo = new PathInfo();

        if(layer2Info != null){
            src = layer2Info.getSrcEndpoint();
            dest = layer2Info.getDestEndpoint();
        }else{
           this.reportError("Layer 2 path information must be provided for " +
            "this IDC.");
        }

        if(interPath == null || interPath.getHop() == null){
            /* Create path with only src and dest in it */
            interPath = new CtrlPlanePathContent();
            interPath.setId("path-" + System.currentTimeMillis());
            CtrlPlaneHopContent srcHop = new CtrlPlaneHopContent();
            CtrlPlaneHopContent destHop = new CtrlPlaneHopContent();

            srcHop.setId("src");
            destHop.setId("dest");
            srcHop.setLinkIdRef(src);
            destHop.setLinkIdRef(dest);
            interPath.addHop(srcHop);
            interPath.addHop(destHop);
            pathInfo.setPathType("loose");
            pathInfo.setPath(interPath);
        }else{
            /* verify the given LIDP is valid */
            this.verifyPath(src, dest, interPath);
        }

        /* build new LIDP from existing LIDP */
        try{
            //Convert to all references in path
            PathInfo refPathInfo = this.tc.createRefPath(pathInfo);
            intraPath = this.buildNewPath(refPathInfo, reservation);
            intraPathInfo.setPath(intraPath);
            //restore any objects in path prior to conversion
            this.tc.mergePathInfo(pathInfo, intraPathInfo, true);
            this.tc.mergePathInfo(pathInfo, refPathInfo, false);
        }catch(BSSException e){
            this.reportError(e.getMessage());
        }

        System.out.println("Path Type: " + pathInfo.getPathType());
        for(int i = 0; i < pathInfo.getPath().getHop().length; i++){
            System.out.println(this.tc.hopToURN(pathInfo.getPath().getHop()[i]));
        }

        /* Remove strict pathType for backward compatibility */
        String interPathType = pathInfo.getPathType();
        if(interPathType != null && interPathType.equals("strict")){
            pathInfo.setPathType(null);
        }

        return intraPathInfo;
    }

    /**
     * Builds both an intradomain and interdomain path using Topology obtained
     * from a perfSONAR Topology Service as its guide. It grabs the topology,
     * iterates through the path looking for ingress/egress points to the
     * network. Once found, it constructs a graph of the topology and runs
     * Dijkstra's shortest path between the hops. It constructs an intra-domain
     * path as well as an inter-domain path.
     *
     * @param pathInfo the PathInfo element from a createReservation request
     * @param reservation a reservation to ignore when constructing the path
     * @return a path containing the ingress and egress for this domain
     */
    private CtrlPlanePathContent buildNewPath(PathInfo pathInfo, Reservation reservation)
        throws PathfinderException, BSSException {

        PSGenericPathfinder pf;
        try {
            pf = new PSGenericPathfinder();
        } catch (Exception e) {
            throw new PathfinderException("Couldn't initialize Generic perfSONAR pathfinder");
        }

        pf.addDomain(this.localDomain);

        // add in the overlapping reservations to the path.

        ReservationDAO resvDAO = new ReservationDAO(this.dbname);
        Long startTime = reservation.getStartTime();
        Long endTime = reservation.getEndTime();

        ArrayList<Reservation> reservations = new ArrayList<Reservation>(resvDAO.overlappingReservations(startTime, endTime));
        for (Reservation resv : reservations) {
            if (resv.getGlobalReservationId().equals(reservation.getGlobalReservationId())) {
                continue;
            }

            this.log.debug("Found overlapping reservation: "+resv.getGlobalReservationId());

            Double bw = new Double(resv.getBandwidth());
            Path path = resv.getPath();
            PathElem pathElem = path.getPathElem();
            while (pathElem != null) {
                Link link = pathElem.getLink();
                Port port = link.getPort();
                if (pf.getElementBandwidth(port.getFQTI()) > 0) {
                    double newBw = pf.getElementBandwidth(port.getFQTI()) - bw.doubleValue();

                    if (newBw < 0) {
                        newBw = 0;
                    }

                    pf.setElementBandwidth(port.getFQTI(), newBw);
                }
                pathElem = pathElem.getNextElem();
            }
        }

        DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> graph;

        CtrlPlanePathContent newInterPath = new CtrlPlanePathContent();
        CtrlPlanePathContent intraPath = new CtrlPlanePathContent();
        CtrlPlaneHopContent[] currHops = pathInfo.getPath().getHop();
        CtrlPlanePathContent newPath = new CtrlPlanePathContent();

        // Ensure that there is an ingress/egress point for us in the path. If
        // none is specified, we add a hop with our own domain since
        // presumably, we have it because the circuit is supposed to go through
        // our domain.

        boolean foundLocal = false;
        for(int i = 0; i < currHops.length; i++) {
            CtrlPlaneHopContent currHop = currHops[i];
            System.out.println("Current hop: "+this.getHopURN(currHop));

            Hashtable<String, String> currHopURNInfo = URNParser.parseTopoIdent(this.getHopURN(currHop));

            if (currHopURNInfo.get("domainFQID").equals(this.localDomain.getFQTI())) {
                foundLocal = true;
            } else if (i == currHops.length - 1 && foundLocal == false) {
                // There was no hop specifying our ingress point, so we must
                // assume it was between the next to last and last hops.

                CtrlPlaneHopContent newHop = new CtrlPlaneHopContent();

                newHop.setDomainIdRef(this.localDomain.getFQTI());

                newPath.addHop(newHop);
            }

            newPath.addHop(currHop);
        }

        currHops = newPath.getHop();

        CtrlPlaneHopContent prevHop = null;
        String ingressURN = null;
        String egressURN = null;

        for(int i = 0; i < currHops.length; i++) {
            CtrlPlaneHopContent currHop = currHops[i];
            Hashtable<String, String> currHopURNInfo = URNParser.parseTopoIdent(this.getHopURN(currHop));

            System.out.println("Current hop: "+this.getHopURN(currHop));

            if (currHopURNInfo.get("domainFQID").equals(this.localDomain.getFQTI()) == false) {
                if (egressURN != null) {
                    System.out.println("Adding verbatim hop(after egress): "+this.getHopURN(currHop));
                    // we've already found our ingress/egress points
                    newInterPath.addHop(currHop);
                    prevHop = currHop;
                } else if (ingressURN == null) {
                    System.out.println("Adding verbatim hop(before egress): "+this.getHopURN(currHop));
                    // we've not yet found our ingress point
                    newInterPath.addHop(currHop);
                    prevHop = currHop;
                } else {
                    // we've already found our ingress point, so the actual
                    // egress point must be somewhere between the previous hop
                    // and this one. Do a search between the two points, add
                    // all the nodes up through the egress point to the
                    // intradomain path, and add the egress point to the
                    // interdomain path.

                    System.out.println("Finding the egress point");

                    List<String> path = pf.lookupPath(this.getHopURN(prevHop), this.getHopURN(currHop), reservation.getBandwidth());
                    if (path == null) {
                        throw new PathfinderException("There is no known path between "+this.getHopURN(prevHop)+" and "+this.getHopURN(currHop));
                    }

                    System.out.println("Found a path between "+this.getHopURN(prevHop)+" and "+this.getHopURN(currHop));

                    for( String urn : path ) {
                        Hashtable<String, String> currURN = URNParser.parseTopoIdent(urn);

                        // each segment adds its own
                        if (urn.equals(this.getHopURN(prevHop))) {
                            continue;
                        }

                        if (currURN.get("domainFQID").equals(this.localDomain.getFQTI()) == false) {
                            // we've found the hop in the next domain, so our
                            // previous URN was the egress point. Add the
                            // previous URN to the interdomain path and break
                            // out since we're not going to assume we know how
                            // to go anywhere in the next domain. XXX this is
                            // where we'd add in true interdomain path finding.

                            System.out.println("Adding hop to interdomain: "+this.getHopURN(prevHop));

                            egressURN = this.getHopURN(prevHop);
                            newInterPath.addHop(prevHop);

                            // The code assumes that we will give them what we
                            // think is the hop into the next domain. So add
                            // what our current URN is since it will correspond
                            // to the link in the next domain.
                            System.out.println("Adding next hop in next domain: "+urn);
                            CtrlPlaneHopContent hop = new CtrlPlaneHopContent();
                            hop.setLinkIdRef(urn);
                            newInterPath.addHop(hop);

                            // since we can only use a given link once, remove
                            // it from contention for later searches.
                            System.out.println("Removing edge between "+prevHop.getLinkIdRef()+" and "+urn);
                            //pf.setEdgeBandwidth(prevHop.getLinkIdRef(), urn, 0.0);
                            pf.ignoreElement(prevHop.getLinkIdRef());

                            System.out.println("Removing edge between "+urn+" and "+prevHop.getLinkIdRef());
                            //pf.setEdgeBandwidth(urn, prevHop.getLinkIdRef(), 0.0);
                            pf.ignoreElement(urn);

                            // add the current hop as long as it's not the same
                            // as the element we just added.
                            if (currHop.getLinkIdRef() != null && currHop.getLinkIdRef().equals(hop.getLinkIdRef()) == false) {
                                    System.out.println("Adding the given nextHop to interdomain: "+this.getHopURN(currHop));
                                    newInterPath.addHop(currHop);
                            }

                            break;
                        } else {
                            if (TopologyUtil.getURNType(urn) == TopologyUtil.LINK_URN) {
                                CtrlPlaneHopContent hop = new CtrlPlaneHopContent();
                                hop.setLinkIdRef(urn);

                                System.out.println("Adding "+urn+" to intradomain path");
                                intraPath.addHop(hop);

                                System.out.println("Removing edge between "+prevHop.getLinkIdRef()+" and "+urn);
                                //pf.setEdgeBandwidth(prevHop.getLinkIdRef(), urn, 0.0);
                                pf.ignoreElement(prevHop.getLinkIdRef());

                                System.out.println("Removing edge between "+urn+" and "+prevHop.getLinkIdRef());
                                //pf.setEdgeBandwidth(urn, prevHop.getLinkIdRef(), 0.0);
                                pf.ignoreElement(urn);

                                prevHop = hop;
                            }
                        }
                    }
                }
            } else {
                // The current hop is in the domain. We need to do a search
                // from the previous hop to the current, and add all the nodes
                // in between to the intradomain path. If we find the ingress
                // point, we need to add it to the interdomain path.

                if (egressURN != null) {
                    // we've already found and left our domain. An error for
                    // now.
                    throw new PathfinderException("Found multiple instances of "+this.localDomain.getFQTI()+" in the path");
                } else if (prevHop == null) {
                    // this is the first hop in the interdomain path, so add it
                    // as is and set it as our ingress.

                    System.out.println("Found the ingress point: "+this.getHopURN(currHop));

                    ingressURN = this.getHopURN(currHop);
                    newInterPath.addHop(currHop);
                    intraPath.addHop(currHop);
                    prevHop = currHop;
                    continue;
                } else {
                    // do a search between the previous hop and our current
                    // hop. Add all elements in our domain to the intradomain
                    // path. If the ingress point is found, add it to the
                    // interdomain path.

                    System.out.println("Finding the ingress point");

                    List<String> path = pf.lookupPath(this.getHopURN(prevHop), this.getHopURN(currHop), reservation.getBandwidth());
                    if (path == null) {
                        throw new PathfinderException("There is no known path between "+this.getHopURN(prevHop)+" and "+this.getHopURN(currHop));
                    }

                    for( String urn : path ) {
                        Hashtable<String, String> currURN = URNParser.parseTopoIdent(urn);

                        // the souce will either be in a different domain or
                        // will already be in the intradomain path.
                        if (urn.equals(this.getHopURN(prevHop)))
                            continue;

                        // if we were given elements not in our domain and it's
                        // not what the other domain gave us, it's an error.
                        if (currURN.get("domainFQID").equals(this.localDomain.getFQTI()) == false) {
                            if (currURN.get("fqti").equals(this.getHopURN(prevHop)) == false) {
                                throw new PathfinderException("Pathfinding gave us a segment that was not agreed to by a previous domain: "+urn);
                            }
                        } else {
                            if (TopologyUtil.getURNType(urn) == TopologyUtil.LINK_URN) {
                                CtrlPlaneHopContent hop = new CtrlPlaneHopContent();
                                hop.setLinkIdRef(urn);

                                if (ingressURN == null) {
                                    System.out.println("Found our ingress point "+urn+" adding to interdomain path");
                                    // we've found our ingress point. Add it to
                                    // the interdomain path.
                                    newInterPath.addHop(hop);
                                    ingressURN = urn;
                                }

                                System.out.println("Adding "+urn+" to intradomain path");
                                intraPath.addHop(hop);

                                // XXX Currently, we can't reuse a port, so
                                // remove each link we add from contention for
                                // future searches
                                System.out.println("Removing edge between "+prevHop.getLinkIdRef()+" and "+urn);
                                //pf.setEdgeBandwidth(prevHop.getLinkIdRef(), urn, 0.0);
                                pf.ignoreElement(prevHop.getLinkIdRef());

                                System.out.println("Removing edge between "+urn+" and "+prevHop.getLinkIdRef());
                                //pf.setEdgeBandwidth(urn, prevHop.getLinkIdRef(), 0.0);
                                pf.ignoreElement(urn);

                                prevHop = hop;
                            }
                        }
                    }

                    if (i == currHops.length - 1) {
                        // if we're the last hop, the above has found the
                        // ingress point, but there is no egress point. Thus,
                        // we add ourselves to the interdomain path.

                        newInterPath.addHop(currHop);
                    }
                }
            }
        }

        /* Save new interdomain path */
        newInterPath.setId("path-" + System.currentTimeMillis());
        pathInfo.setPath(newInterPath);

        return intraPath;
    }

    /**
     * Verifies a given path is valid
     *
     * @param src a createReservation request's given source URN
     * @param dest a createReservation request's given destination URN
     * @param interPath a createReservation request's given path
     * @throws PathfinderException
     */
    private void verifyPath(String src, String dest,
        CtrlPlanePathContent interPath) throws PathfinderException{

        String srcURN = this.resolveToFQTI(src);
        String destURN = this.resolveToFQTI(dest);

        CtrlPlaneHopContent[] hops = interPath.getHop();
        String firstHop = this.tc.hopToURN(hops[0], "link");
        String lastHop = this.tc.hopToURN(hops[hops.length - 1], "link");

        if(firstHop == null || lastHop == null){
            this.reportError("The first and last hop of the given path must " +
                "be a link or link ID reference.");
        }else if(!firstHop.equals(srcURN)){
            this.reportError("The first hop of the path must be the same as " +
            "the source. The source given was " + src + " and the first hop " +
            "of the provided path is " + firstHop);
        }else if(!lastHop.equals(destURN)){
            this.reportError("The last hop of the path must be the same as " +
            "the destination. The destination given was " + dest + " and the" +
            "last hop of the provided path is " + lastHop);
        }

    }

    /**
     * Reports an error
     *
     * @param msq the message to report
     * @throws PathfinderException
     */
    private void reportError(String msg) throws PathfinderException{
        //this.log.error(msg);
        throw new PathfinderException(msg);
    }

    /**
     * Returns the idRef for the given hop
     *
     * @param hop the hop from which to take the id ref
     * @return the hop's idref or null if it doesn't have one
     */
    private String getHopURN(CtrlPlaneHopContent hop) {
        if (hop.getDomainIdRef() != null) {
            return hop.getDomainIdRef();
        } else if (hop.getNodeIdRef() != null) {
            return hop.getNodeIdRef();
        } else if (hop.getPortIdRef() != null) {
            return hop.getPortIdRef();
        } else if (hop.getLinkIdRef() != null) {
            return hop.getLinkIdRef();
        }

        return null;
    }
}
