package net.es.oscars.pathfinder.perfsonar;

import java.util.Hashtable;
import java.util.Properties;
import java.util.ArrayList;

import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.topology.*;
import net.es.oscars.bss.*;
import net.es.oscars.pathfinder.*;
import net.es.oscars.PropHandler;

import net.es.oscars.pathfinder.perfsonar.util.*;

import net.es.oscars.bss.topology.URNParser;

import java.util.List;
import org.apache.log4j.*;

/**
 * PSPathfinder finds the route through the domain and toward the destination
 * by consulting a perfSONAR Topology service. The pathfinder uses the
 * perfSONAR Information Services to lookup topologies from other domains and
 * does a search through those domains to find the path needed to get to the
 * destination. It then returns the intradomain path and the hop in the next
 * domain along the path.
 *
 * @author Aaron Brown (aaron@internet2.edu), Andrew Lake (alake@internet2.edu)
 */
public class PSPathfinder extends Pathfinder implements LocalPCE, InterdomainPCE {
    private Logger log;
    private Domain localDomain;
    static private PerfSONARDomainFinder psdf = null;

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

        if (this.psdf == null) {
            String[] gLSs = null;
            String[] hLSs = null;
            String[] TSs = null;
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
            }

            try {
                if(gLSs != null || hLSs != null || TSs != null){
                    this.psdf = new PerfSONARDomainFinder(gLSs, hLSs, TSs);
                }else if(hints != null){
                    this.psdf = new PerfSONARDomainFinder(hints);
                }else{
                    this.log.warn("No lookup service information specified, using defaults");
                    this.psdf = new PerfSONARDomainFinder("http://www.perfsonar.net/gls.root.hints");
                }
            } catch(Exception e) {
                this.log.error(e.getMessage());
            }
        }
    }

    /**
     * Finds a local path given path information from a Reservation
     *
     * @param resv Reservation instance containing request information
     * @return a list of Paths containing the local path calculated
     * @throws PathfinderException
     */
    public List<Path> findLocalPath(Reservation resv) throws PathfinderException {
        if (this.psdf == null) {
            this.log.error("The perfSONAR pathfinder is not properly configured.");
        }
        
        ArrayList<Path> results = new ArrayList<Path>();
        try {
            results.add(this.buildNewPath(resv, PathType.INTERDOMAIN));
        } catch (BSSException e) {
            throw new PathfinderException(e.getMessage());
        }

        for(Path p : results){
            for(PathElem e : p.getPathElems()){
                this.log.debug(e.getUrn());
            }
        }
        
        return results;
    }
    
    /**
     * Finds an interdomain path given path information from a Reservation
     *
     * @param resv Reservation instance containing current set of parameters
     * @return a list of Paths containing the calculated interdomain path
     * @throws PathfinderException
     */
    public List<Path> findInterdomainPath(Reservation resv) throws PathfinderException {
        if (this.psdf == null) {
            this.log.error("The perfSONAR pathfinder is not properly configured.");
        }
        
        ArrayList<Path> results = new ArrayList<Path>();
        try {
            results.add(this.buildNewPath(resv, PathType.REQUESTED));
        } catch (BSSException e) {
            throw new PathfinderException(e.getMessage());
        }

        for(Path p : results){
            for(PathElem e : p.getPathElems()){
                this.log.debug(e.getUrn());
            }
        }
        
        return results;
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
     * @param resv a reservation to ignore when constructing the path
     * @return a path containing the ingress and egress for this domain
     */
    private Path buildNewPath(Reservation resv, String pathType)
        throws PathfinderException, BSSException {

        GenericPathfinder pf;
        try {
            pf = new GenericPathfinder();
        } catch (Exception e) {
            throw new PathfinderException("Couldn't initialize Generic perfSONAR pathfinder");
        }

        pf.addDomainFinder(psdf);

        pf.addDomain(this.localDomain);

        // add in the overlapping reservations to the path.

        ReservationDAO resvDAO = new ReservationDAO(this.dbname);
        Long startTime = resv.getStartTime();
        Long endTime = resv.getEndTime();

        ArrayList<Reservation> reservations = new ArrayList<Reservation>(resvDAO.overlappingReservations(startTime, endTime));
        for (Reservation overResv : reservations) {
            if (resv.getGlobalReservationId().equals(overResv.getGlobalReservationId())) {
                continue;
            }

            this.log.debug("Found overlapping reservation: "+overResv.getGlobalReservationId());

            Double bw = new Double(overResv.getBandwidth());
            Path path = overResv.getPath(PathType.LOCAL);
            List<PathElem> pathElems = path.getPathElems();
            for (PathElem pathElem: pathElems) {
                Link link = pathElem.getLink();
                Port port = link.getPort();
                if (pf.getElementBandwidth(port.getFQTI()) > 0) {
                    double newBw = pf.getElementBandwidth(port.getFQTI()) - bw.doubleValue();

                    if (newBw < 0) {
                        newBw = 0;
                    }

                    pf.setElementBandwidth(port.getFQTI(), newBw);
                }
            }
        }

        // Ensure that there is an ingress/egress point for us in the path. If
        // none is specified, we add a hop with our own domain since
        // presumably, we have it because the circuit is supposed to go through
        // our domain.
        Path newPath = new Path();
        List<PathElem> reqHops = resv.getPath(pathType).getPathElems();
        int sequenceNum = 0;
        boolean foundLocal = false;
        for(int i = 0; i < reqHops.size(); i++) {
            PathElem currHop = PathElem.copyPathElem(reqHops.get(i));
            this.log.debug("Current hop: "+currHop.getUrn());
            Hashtable<String, String> currHopURNInfo = URNParser.parseTopoIdent(currHop.getUrn());
            if (currHopURNInfo.get("domainFQID").equals(this.localDomain.getFQTI())) {
                foundLocal = true;
            }
            currHop.setSeqNumber(++sequenceNum);
            newPath.addPathElem(currHop);
        }
        
        /* If didn't find ingress insert domain ID as second to last hop */
        if(!foundLocal){
           List<PathElem> newHops = newPath.getPathElems();
           PathElem lastElem = newHops.get(newHops.size()-1);
           PathElem ingElem = PathElem.copyPathElem(lastElem);
           ingElem.setLink(null);
           ingElem.setUrn(this.localDomain.getFQTI());
           newHops.add(newHops.size()-2, ingElem);
           lastElem.setSeqNumber(newHops.size());
        }
        
        List<PathElem> newHops = newPath.getPathElems();
        Path newInterPath = new Path();
        newInterPath.setDirection(PathDirection.BIDIRECTIONAL);
        newInterPath.setPathType(PathType.INTERDOMAIN);
        newInterPath.setExplicit(false);
        
        PathElem prevHop = null;
        String ingressURN = null;
        String egressURN = null;
        sequenceNum = 0;
        
        for(int i = 0; i < newHops.size(); i++) {
            PathElem currHop = PathElem.copyPathElem(newHops.get(i));
            Hashtable<String, String> currHopURNInfo = URNParser.parseTopoIdent(currHop.getUrn());

            this.log.debug("Current hop: "+ currHop.getUrn());

            if (currHopURNInfo.get("domainFQID").equals(this.localDomain.getFQTI()) == false) {
                if (egressURN != null) {
                    this.log.debug("Adding verbatim hop(after egress): "+ currHop.getUrn());
                    // we've already found our ingress/egress points
                    currHop.setSeqNumber(++sequenceNum);
                    newInterPath.addPathElem(currHop);
                    prevHop = currHop;
                } else if (ingressURN == null) {
                    this.log.debug("Adding verbatim hop(before egress): "+ currHop.getUrn());
                    // we've not yet found our ingress point
                    currHop.setSeqNumber(++sequenceNum);
                    newInterPath.addPathElem(currHop);
                    prevHop = currHop;
                } else {
                    // we've already found our ingress point, so the actual
                    // egress point must be somewhere between the previous hop
                    // and this one. Do a search between the two points, add
                    // all the nodes up through the egress point to the
                    // intradomain path, and add the egress point to the
                    // interdomain path.

                    this.log.debug("Finding the egress point");

                    List<String> path = pf.lookupPath(prevHop.getUrn(), currHop.getUrn(), resv.getBandwidth());
                    if (path == null) {
                        throw new PathfinderException("There is no known path between "+prevHop.getUrn()+" and "+currHop.getUrn());
                    }

                    this.log.debug("Found a path between "+ prevHop.getUrn()+" and "+ currHop.getUrn());

                    for( String urn : path ) {
                        Hashtable<String, String> currURN = URNParser.parseTopoIdent(urn);

                        // each segment adds its own
                        if (urn.equals(prevHop.getUrn())) {
                            continue;
                        }

                        if (currURN.get("domainFQID").equals(this.localDomain.getFQTI()) == false) {
                            // we've found the hop in the next domain, so our
                            // previous URN was the egress point. Add the
                            // previous URN to the interdomain path and break
                            // out since we're not going to assume we know how
                            // to go anywhere in the next domain. XXX this is
                            // where we'd add in true interdomain path finding.

                            this.log.debug("Adding hop to interdomain: "+prevHop.getUrn());

                            egressURN = prevHop.getUrn();
                            prevHop.setSeqNumber(++sequenceNum);
                            newInterPath.addPathElem(prevHop);

                            // The code assumes that we will give them what we
                            // think is the hop into the next domain. So add
                            // what our current URN is since it will correspond
                            // to the link in the next domain.
                            this.log.debug("Adding next hop in next domain: "+urn);
                            PathElem hop = new PathElem();
                            hop.setUrn(urn);
                            try{
                                hop.setLink(TopologyUtil.getLink(urn, this.dbname));
                            }catch(BSSException e){}
                            hop.setSeqNumber(++sequenceNum);
                            newInterPath.addPathElem(hop);

                            // since we can only use a given link once, remove
                            // it from contention for later searches.
                            this.log.debug("Removing edge between "+prevHop.getUrn()+" and "+urn);
                            //pf.setEdgeBandwidth(prevHop.getLinkIdRef(), urn, 0.0);
                            pf.ignoreElement(prevHop.getUrn());

                            this.log.debug("Removing edge between "+urn+" and "+prevHop.getUrn());
                            //pf.setEdgeBandwidth(urn, prevHop.getLinkIdRef(), 0.0);
                            pf.ignoreElement(urn);

                            // add the current hop as long as it's not the same
                            // as the element we just added.
                            if (currHop.getUrn() != null && currHop.getUrn().equals(hop.getUrn()) == false) {
                                    this.log.debug("Adding the given nextHop to interdomain: "+ currHop.getUrn());
                                    newInterPath.addPathElem(currHop);
                            }

                            break;
                        } else {
                            if (TopologyUtil.getURNType(urn) == TopologyUtil.LINK_URN) {
                                PathElem hop = new PathElem();
                                hop.setUrn(urn);
                                try{
                                    hop.setLink(TopologyUtil.getLink(urn, this.dbname));
                                }catch(BSSException e){}
                                hop.setSeqNumber(++sequenceNum);

                                this.log.debug("Removing edge between "+prevHop.getUrn()+" and "+urn);
                                //pf.setEdgeBandwidth(prevHop.getLinkIdRef(), urn, 0.0);
                                pf.ignoreElement(prevHop.getUrn());

                                this.log.debug("Removing edge between "+urn+" and "+prevHop.getUrn());
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

                    this.log.debug("Found the ingress point: "+ currHop.getUrn());

                    ingressURN = currHop.getUrn();
                    newInterPath.addPathElem(currHop);
                    prevHop = currHop;
                    continue;
                } else {
                    // do a search between the previous hop and our current
                    // hop. Add all elements in our domain to the intradomain
                    // path. If the ingress point is found, add it to the
                    // interdomain path.

                    this.log.debug("Finding the ingress point");

                    List<String> path = pf.lookupPath(prevHop.getUrn(), currHop.getUrn(), resv.getBandwidth());
                    if (path == null) {
                        throw new PathfinderException("There is no known path between "+prevHop.getUrn()+" and "+ currHop.getUrn());
                    }

                    for( String urn : path ) {
                        Hashtable<String, String> currURN = URNParser.parseTopoIdent(urn);

                        // the souce will either be in a different domain or
                        // will already be in the intradomain path.
                        if (urn.equals(prevHop.getUrn()))
                            continue;

                        // if we were given elements not in our domain and it's
                        // not what the other domain gave us, it's an error.
                        if (currURN.get("domainFQID").equals(this.localDomain.getFQTI()) == false) {
                            if (currURN.get("fqti").equals(prevHop.getUrn()) == false) {
                                throw new PathfinderException("Pathfinding gave us a segment that was not agreed to by a previous domain: "+urn);
                            }
                        } else {
                            if (TopologyUtil.getURNType(urn) == TopologyUtil.LINK_URN) {
                                PathElem hop = new PathElem();
                                hop.setUrn(urn);
                                try{
                                    hop.setLink(TopologyUtil.getLink(urn, this.dbname));
                                }catch(BSSException e){}
                                hop.setSeqNumber(++sequenceNum);

                                if (ingressURN == null) {
                                    this.log.debug("Found our ingress point "+urn+" adding to interdomain path");
                                    // we've found our ingress point. Add it to
                                    // the interdomain path.
                                    newInterPath.addPathElem(hop);
                                    ingressURN = urn;
                                }
                                
                                // XXX Currently, we can't reuse a port, so
                                // remove each link we add from contention for
                                // future searches
                                this.log.debug("Removing edge between "+prevHop.getUrn()+" and "+urn);
                                //pf.setEdgeBandwidth(prevHop.getLinkIdRef(), urn, 0.0);
                                pf.ignoreElement(prevHop.getUrn());

                                this.log.debug("Removing edge between "+urn+" and "+prevHop.getUrn());
                                //pf.setEdgeBandwidth(urn, prevHop.getLinkIdRef(), 0.0);
                                pf.ignoreElement(urn);

                                prevHop = hop;
                            }
                        }
                    }

                    if (i == newHops.size() - 1) {
                        // if we're the last hop, the above has found the
                        // ingress point, but there is no egress point. Thus,
                        // we add ourselves to the interdomain path.

                        newInterPath.addPathElem(currHop);
                    }
                }
            }
        }

        /* Save new interdomain path */
        return newInterPath;
    }
}
