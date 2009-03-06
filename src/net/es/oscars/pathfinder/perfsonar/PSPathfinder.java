package net.es.oscars.pathfinder.perfsonar;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Properties;
import java.util.ArrayList;

import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.topology.*;
import net.es.oscars.bss.*;
import net.es.oscars.lookup.LookupException;
import net.es.oscars.lookup.PSLookupClient;
import net.es.oscars.pathfinder.*;
import net.es.oscars.PropHandler;

import net.es.oscars.pathfinder.perfsonar.util.*;

import net.es.oscars.bss.topology.URNParser;

import java.util.List;

import org.apache.commons.httpclient.HttpException;
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
        this.log.debug("localDomain=" + this.localDomain);
        
        if (PSPathfinder.psdf == null) {
            String[] gLSs = null;
            String[] hLSs = null;
            String[] TSs = null;
            ArrayList<String> gLSList = new ArrayList<String>();
            ArrayList<String> hLSList = new ArrayList<String>();
            String[] sections = { "topology", "lookup" };

            for ( String section : sections) {

                this.log.debug("Handling section: "+section);

                PropHandler propHandler = new PropHandler("oscars.properties");
                Properties props = propHandler.getPropertyGroup(section, true);
                
                //Set home and global lookup service
                try {
                    PSLookupClient.configLS(props, gLSList, hLSList);
                } catch (LookupException e) {
                    this.log.error(e.getMessage());
                }

                if (TSs == null) {
                    int i = 1;
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
            
            if(!gLSList.isEmpty()){
                gLSs = gLSList.toArray(new String[gLSList.size()]);
            }
            if(!hLSList.isEmpty()){
                hLSs = gLSList.toArray(new String[hLSList.size()]);
            }
            
            try {
                if(gLSs != null || hLSs != null || TSs != null){
                    PSPathfinder.psdf = new PerfSONARDomainFinder(gLSs, hLSs, TSs);
                }else{
                    this.log.warn("No lookup service information specified, using defaults");
                    PSPathfinder.psdf = new PerfSONARDomainFinder("http://www.perfsonar.net/gls.root.hints");
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
        if (PSPathfinder.psdf == null) {
            this.log.error("The perfSONAR pathfinder is not properly configured.");
        }
        
        ArrayList<Path> results = new ArrayList<Path>();
        try {
            Path localPath = this.buildNewPath(resv, PathType.INTERDOMAIN);
            localPath.setPathType(PathType.LOCAL);
            results.add(localPath);
        } catch (BSSException e) {
            throw new PathfinderException(e.getMessage());
        }

        for(Path p : results){
            for(PathElem e : p.getPathElems()){
                String vlan = "";
                try {
                    PathElemParam peParam = e.getPathElemParam(PathElemParamSwcap.L2SC, PathElemParamType.L2SC_VLAN_RANGE);
                    if(peParam != null){ vlan =" --VLAN=" + peParam.getValue();}
                } catch (Exception e1) {}
                this.log.debug("Hop) " + e.getUrn() + vlan);
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
        if (PSPathfinder.psdf == null) {
            this.log.error("The perfSONAR pathfinder is not properly configured.");
        }
        
        ArrayList<Path> results = new ArrayList<Path>();
        try {
            Path interdomainPath = this.buildNewPath(resv, PathType.REQUESTED);
            interdomainPath.setPathType(PathType.INTERDOMAIN);
            results.add(interdomainPath);
        } catch (BSSException e) {
            throw new PathfinderException(e.getMessage());
        }
        
        /* Print path and set next domain */
        for(Path p : results){
            boolean localFound = false;
            boolean nextFound = false;
            for(PathElem e : p.getPathElems()){
                String vlan = "";
                Hashtable<String,String> urnInfo = URNParser.parseTopoIdent(e.getUrn());
                if((!nextFound) && localDomain.getTopologyIdent().equals(urnInfo.get("domainId"))){
                    localFound = true;
                }else if((!nextFound) && localFound){
                    DomainDAO domainDAO = new DomainDAO(OSCARSCore.getInstance().getBssDbName());
                    p.setNextDomain(domainDAO.fromTopologyIdent(urnInfo.get("domainId")));
                    this.log.debug("Found next domain: " + urnInfo.get("domainId"));
                    nextFound = true;
                }
                
                try {
                    PathElemParam peParam = e.getPathElemParam(PathElemParamSwcap.L2SC, PathElemParamType.L2SC_VLAN_RANGE);
                    if(peParam != null){ vlan =" --VLAN=" + peParam.getValue();}
                } catch (Exception e1) {}
                this.log.debug("Hop) " + e.getUrn() + vlan);
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
     * @param resv the reservation to ignore when constructing the path
     * @para inputPathType the type of path to use as the input.
     * @return a path of type INTERDOMAIN or LOCAL depending on the input information
     */
    private Path buildNewPath(Reservation resv, String inputPathType)
        throws PathfinderException, BSSException {
        
        //if an interdomain path is already calculated then just calculate local path
        final boolean LOCALPATH = PathType.INTERDOMAIN.equals(inputPathType);
        
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
        Path tmpPath = new Path();
        List<PathElem> reqHops = resv.getPath(inputPathType).getPathElems();
        boolean foundLocal = false;
        for(int i = 0; i < reqHops.size(); i++) {
            PathElem currHop = PathElem.copyPathElem(reqHops.get(i));
            this.log.debug("Current hop: "+currHop.getUrn());
            Hashtable<String, String> currHopURNInfo = URNParser.parseTopoIdent(currHop.getUrn());
            if (currHopURNInfo.get("domainFQID").equals(this.localDomain.getFQTI())) {
                foundLocal = true;
            }
            tmpPath.addPathElem(currHop);
        }
        
        /* If didn't find ingress insert domain ID as second to last hop */
        if(!foundLocal){
           List<PathElem> tmpHops = tmpPath.getPathElems();
           PathElem lastElem = tmpHops.get(tmpHops.size()-1);
           PathElem ingElem = PathElem.copyPathElem(lastElem);
           ingElem.setLink(null);
           ingElem.setUrn(this.localDomain.getFQTI());
           tmpHops.add(tmpHops.size()-2, ingElem);
        }
        
        List<PathElem> tmpHops = tmpPath.getPathElems();
        Path newPath = new Path();
        newPath.setDirection(PathDirection.BIDIRECTIONAL);
        newPath.setPathType(LOCALPATH ? PathType.LOCAL : PathType.INTERDOMAIN);
        
        PathElem prevHop = null;
        String ingressURN = null;
        String egressURN = null;
        
        for(int i = 0; i < tmpHops.size(); i++) {
            PathElem currHop = PathElem.copyPathElem(tmpHops.get(i));
            Hashtable<String, String> currHopURNInfo = URNParser.parseTopoIdent(currHop.getUrn());

            this.log.debug("Current hop: "+ currHop.getUrn());

            if (currHopURNInfo.get("domainFQID").equals(this.localDomain.getFQTI()) == false) {
                if (egressURN != null) {
                    this.log.debug("Adding verbatim hop(after egress): "+ currHop.getUrn());
                    // we've already found our ingress/egress points
                    newPath.addPathElem(currHop);
                    prevHop = currHop;
                } else if (ingressURN == null) {
                    if(LOCALPATH){ continue; }
                    this.log.debug("Adding verbatim hop(before ingress): "+ currHop.getUrn());
                    // we've not yet found our ingress point
                    newPath.addPathElem(currHop);
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
                            
                            //...if its a local path though skip adding these hops
                            if(LOCALPATH){ continue; }
                            
                            this.log.debug("Adding hop to interdomain: "+prevHop.getUrn());

                            egressURN = prevHop.getUrn();
                            newPath.addPathElem(prevHop);

                            // The code assumes that we will give them what we
                            // think is the hop into the next domain. So add
                            // what our current URN is since it will correspond
                            // to the link in the next domain.
                            this.log.debug("Adding next hop in next domain: "+urn);
                            PathElem hop = null;
                            if(urn.equals(currHop.getUrn())){
                                hop = currHop;
                            }else{
                                hop= new PathElem();
                                hop.setUrn(urn);
                                try{
                                    hop.setLink(TopologyUtil.getLink(urn, this.dbname));
                                }catch(BSSException e){}
                            }
                            newPath.addPathElem(hop);

                            // since we can only use a given link once, remove
                            // it from contention for later searches.
                           // this.log.debug("Removing edge between "+prevHop.getUrn()+" and "+urn);
                            //pf.setEdgeBandwidth(prevHop.getLinkIdRef(), urn, 0.0);
                            pf.ignoreElement(prevHop.getUrn());

                           // this.log.debug("Removing edge between "+urn+" and "+prevHop.getUrn());
                            //pf.setEdgeBandwidth(urn, prevHop.getLinkIdRef(), 0.0);
                            pf.ignoreElement(urn);

                            // add the current hop as long as it's not the same
                            // as the element we just added.
                            if (currHop.getUrn() != null && currHop.getUrn().equals(hop.getUrn()) == false) {
                                    this.log.debug("Adding the given nextHop to interdomain: "+ currHop.getUrn());
                                    newPath.addPathElem(currHop);
                            }

                            break;
                        } else {
                            if (TopologyUtil.getURNType(urn) == TopologyUtil.LINK_URN) {
                                PathElem hop = null;
                                if(urn.equals(currHop.getUrn())){
                                    hop = currHop;
                                }else{
                                    hop= new PathElem();
                                    hop.setUrn(urn);
                                    try{
                                        hop.setLink(TopologyUtil.getLink(urn, this.dbname));
                                    }catch(BSSException e){}
                                }
                                if(LOCALPATH){
                                    this.log.debug("Adding "+urn+" to intradomain path");
                                    newPath.addPathElem(hop);
                                }
                                //this.log.debug("Removing edge between "+prevHop.getUrn()+" and "+urn);
                                //pf.setEdgeBandwidth(prevHop.getLinkIdRef(), urn, 0.0);
                                pf.ignoreElement(prevHop.getUrn());

                               // this.log.debug("Removing edge between "+urn+" and "+prevHop.getUrn());
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
                    newPath.addPathElem(currHop);
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

                        // the source will either be in a different domain or
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
                                PathElem hop = null;
                                if(urn.equals(currHop.getUrn())){
                                    hop = currHop;
                                }else{
                                    hop= new PathElem();
                                    hop.setUrn(urn);
                                    try{
                                        hop.setLink(TopologyUtil.getLink(urn, this.dbname));
                                    }catch(BSSException e){}
                                }
                                if(ingressURN == null && !LOCALPATH){
                                    // we've found our ingress point. Add it to
                                    // the interdomain path.
                                    this.log.debug("Adding ingress " + urn + " to interdomain path");
                                    newPath.addPathElem(hop);
                                }else if(LOCALPATH){
                                    this.log.debug("Adding "+urn+" to intradomain path");
                                    newPath.addPathElem(hop);
                                }
                                if (ingressURN == null) {
                                    this.log.debug("Found our ingress point "+urn);
                                    
                                    ingressURN = urn;
                                }
                                
                                // XXX Currently, we can't reuse a port, so
                                // remove each link we add from contention for
                                // future searches
                               // this.log.debug("Removing edge between "+prevHop.getUrn()+" and "+urn);
                                //pf.setEdgeBandwidth(prevHop.getLinkIdRef(), urn, 0.0);
                                pf.ignoreElement(prevHop.getUrn());

                               // this.log.debug("Removing edge between "+urn+" and "+prevHop.getUrn());
                                //pf.setEdgeBandwidth(urn, prevHop.getLinkIdRef(), 0.0);
                                pf.ignoreElement(urn);

                                prevHop = hop;
                            }
                        }
                    }

                    if ((i == tmpHops.size() - 1) && !LOCALPATH) {
                        // if we're the last hop, the above has found the
                        // ingress point, but there is no egress point. Thus,
                        // we add ourselves to the interdomain path.
                        newPath.addPathElem(currHop);
                    }
                }
            }
        }

        return newPath;
    }
}
