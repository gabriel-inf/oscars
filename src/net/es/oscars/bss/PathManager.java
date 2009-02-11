package net.es.oscars.bss;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.*;


import net.es.oscars.bss.topology.*;
import net.es.oscars.bss.policy.*;
import net.es.oscars.pathfinder.*;
import net.es.oscars.lookup.*;

/**
 * PathManager performs path manipulation on behalf of the reservation manager.
 *
 * @author Andrew Lake, David Robertson, Evangelos Chaniotakis
 */
public class PathManager {
    private Logger log;
    private PCEManager pceMgr;
    private PolicyManager policyMgr;
    private String dbname;

    /** Constructor. */
    public PathManager(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.pceMgr = new PCEManager(dbname);
        this.policyMgr = new PolicyManager(dbname);
        this.dbname = dbname;
        L2SwitchingCapType.initGlobals();
    }

    /**
     * Finds path between source and destination, checks to make sure
     * it wouldn't violate policy, and then finds the next domain, if any.
     *
     * @param resv partially filled in reservation, use startTime, endTime, bandWidth,
     *                GRI
     */
    public void calculatePaths(Reservation resv)
            throws BSSException {

        List<Path> interdomainPaths = null;
        List<Path> localPaths = null;
        Path interdomainPath = null;
        Path localPath = null;
        try {
            //Find requested path
            this.resolveRequestedPath(resv);
            Path requestedPath = resv.getPath(PathType.REQUESTED);
            
            //Find interdomain path
            interdomainPaths = this.pceMgr.findInterdomainPath(resv);
            interdomainPath = interdomainPaths.get(0);
            interdomainPath.setPathSetupMode(requestedPath.getPathSetupMode());
            resv.setPath(interdomainPath);
            
            //Find local path
            localPaths = this.pceMgr.findLocalPath(resv);
            localPath = localPaths.get(0);
            localPath.setPathSetupMode(requestedPath.getPathSetupMode());
            resv.setPath(localPath);
            // FIXME: is setPath the method to use? maybe we want to replace the set of
            // paths during modify
        } catch (PathfinderException ex) {
            this.log.error(ex.getMessage());
            throw new BSSException(ex.getMessage());
        }
        ReservationDAO dao = new ReservationDAO(this.dbname);
        List<Reservation> reservations =
            dao.overlappingReservations(resv.getStartTime(), resv.getEndTime());
        this.policyMgr.checkOversubscribed(reservations, resv);

        Domain nextDomain = interdomainPath.getNextDomain();
        if (nextDomain != null) {
            this.log.info("create.finish, next domain: " + nextDomain.getUrl());
        } else {
            this.log.info("create.finish, reservation terminates in this domain");
        }
    }

    private void resolveRequestedPath(Reservation resv) throws BSSException {
        Path requestedPath = resv.getPath(PathType.REQUESTED);
        String errMsg = "";
        if (requestedPath == null) {
            errMsg = "No requested path set!";
            this.log.error(errMsg);
            throw new BSSException(errMsg);
        }
        for (PathElem pe : requestedPath.getPathElems()) {
            if (pe.getLink() == null) {
                if (pe.getUrn() == null) {
                    errMsg = "No link or URN set for a pathelem!";
                    this.log.error(errMsg);
                    throw new BSSException(errMsg);
                }
                String urn = pe.getUrn();
                Link link = TopologyUtil.getLink(urn, this.dbname);
                if (link != null) {
                    pe.setLink(link);
                } else {
                    try {
                        PSLookupClient lookupClient = new PSLookupClient();
                        String resolved = lookupClient.lookup(urn);
                        link = TopologyUtil.getLink(resolved, this.dbname);
                        if (link != null) {
                            pe.setLink(link);
                        }
                    } catch (LookupException ex) {
                        this.log.error(ex);
                        throw new BSSException(ex.getMessage());
                    }
                }
            }
        }
    }


    /**
     *  Finalizes VLAN tags for reservation.
     *
     * @param resv reservation to be stored in database
     * @param pathFromDownstream the interdomain path received from downstream
     */
    public void finalizeVlanTags(Reservation resv, Path pathFromDownstream)
            throws BSSException {

        this.log.info("finalizing VLAN tags");

        PathElem nextExtPathElem = null;
        String egrSugVlan = null;
        boolean localFound = false;
        String nextExtVlan = null;
        List<PathElem> interLocalSegment = new ArrayList<PathElem>();
        Path localPath = resv.getPath(PathType.LOCAL);
        List<List<PathElem>> pathsToUpdate = new ArrayList<List<PathElem>>();
        pathsToUpdate.add(interLocalSegment);
        pathsToUpdate.add(localPath.getPathElems());
        
        /* Find local path segment of inter-domain path so can update when VLAN chosen */
        for(PathElem interPathElem : resv.getPath(PathType.INTERDOMAIN).getPathElems()){
            String domainUrn = URNParser.parseTopoIdent(interPathElem.getUrn()).get("domainFQID");
            Domain domain = null;
            try{
                domain = TopologyUtil.getDomain(domainUrn, this.dbname);
            }catch(BSSException e){
                continue;
            }
            if(domain.isLocal()){
                localFound = true;
                interLocalSegment.add(interPathElem);
            }else if(localFound){
                break;
            }
        }
        
        /* Examine next domain path and determine what VLAN was choses */
        localFound = false;
        if (pathFromDownstream != null) {

            //Find ingress pathElem of next domain (if exists)
            for (PathElem interPathElem : pathFromDownstream.getPathElems()){
                String domainUrn = URNParser.parseTopoIdent(interPathElem.getUrn()).get("domainFQID");
                Domain domain = null;
                try{
                    domain = TopologyUtil.getDomain(domainUrn, this.dbname);
                }catch(BSSException e){
                    continue;
                }
                if(domain.isLocal()){
                    localFound = true;
                    interLocalSegment.add(interPathElem);
                    egrSugVlan = interPathElem.getPathElemParam(PathElemParamSwcap.L2SC,
                            PathElemParamType.L2SC_SUGGESTED_VLAN).getValue();
                }else if(localFound && (!domain.isLocal())){
                    nextExtPathElem = interPathElem;
                    break;
                }
            }
        }

        /* Find the next hop(if any) and see if it uses the suggested VLAN.
        If not then try to choose another by doing the oversubscription
        check again. */
        if(nextExtPathElem != null){
            nextExtVlan = nextExtPathElem.getPathElemParam(PathElemParamSwcap.L2SC,
                    PathElemParamType.L2SC_VLAN_RANGE).getValue();
        }
        if(nextExtVlan != null && (!nextExtVlan.equals(egrSugVlan))){
            ReservationDAO dao = new ReservationDAO(this.dbname);
            List<Reservation> active = dao.overlappingReservations(
                                        resv.getStartTime(), resv.getEndTime());
            this.policyMgr.checkOversubscribed(active, resv);
        }

        /* Set the local VLAN range on each hop to the suggested VLAN
           for both interdomain and local path */
        for(List<PathElem> pathToUpdate : pathsToUpdate){
            for(PathElem elem : pathToUpdate){
                PathElemParam sugVlanParam = elem.getPathElemParam(
                        PathElemParamSwcap.L2SC, PathElemParamType.L2SC_SUGGESTED_VLAN);
                PathElemParam vlanRange = elem.getPathElemParam(
                        PathElemParamSwcap.L2SC, PathElemParamType.L2SC_VLAN_RANGE);
                if (vlanRange == null) {
                    vlanRange = new PathElemParam();
                    vlanRange.setType(PathElemParamType.L2SC_VLAN_RANGE);
                    vlanRange.setSwcap(PathElemParamSwcap.L2SC);
                    elem.addPathElemParam(vlanRange);
                    this.log.debug("Added another VLAN Range element");
                }

                if (sugVlanParam != null) {
                    vlanRange.setValue(sugVlanParam.getValue());
                    this.log.debug("VLAN range for " + elem.getUrn() + 
                            " set to " + vlanRange.getValue());
                }
            }
        }
    }

    /**
     * Returns the name of the institution of the end point of the reservation
     *
     * @param resv Reservation for which we want to find an end point
     *             institution
     * @param source - true returns the source , false returns the destination
     * @return institution String name of the end point
     */
    public String endPointSite(Reservation resv, Boolean source) throws BSSException {
        Path path = resv.getPath(PathType.LOCAL);
        List<PathElem> hops = path.getPathElems();
        PathElem hop = null;
        if (source) {
            hop = hops.get(0);
        } else { // get last hop
            hop = hops.get(hops.size()-1);
        }
        Link endPoint = hop.getLink();
        Link remoteLink = endPoint.getRemoteLink();
        String institutionName = "UNKNOWN";
        Domain endDomain = null;
        // String FQTI = null;
        if (remoteLink != null) {
            endDomain = remoteLink.getPort().getNode().getDomain();
            // FQTI=remoteLink.getFQTI();
            // this.log.debug("remote link: " + FQTI + " domain: " + endDomain.getTopologyIdent() );
        } else {
            endDomain = endPoint.getPort().getNode().getDomain();
            // FQTI=endPoint.getFQTI();
            // this.log.debug("endPoint link is: " + FQTI + " domain: " + endDomain.getTopologyIdent());
        }
        if (endDomain != null ) {
            Site institution = endDomain.getSite();
            if (institution != null) {
                institutionName = institution.getName();
            }
        }
        return institutionName;
    }

    /**
     * Matches if any hop in the path has a topology identifier that at
     * least partially matches a link id.
     *
     * @param path path to check
     * @param patterns map from linkIds to compiled Patterns
     */
    public boolean matches(Path path, Map<String, Pattern> patterns) {

        List<PathElem> pathElems = path.getPathElems();
        StringBuilder sb = new StringBuilder();
        for (PathElem pathElem: pathElems) {
            String topologyIdent = pathElem.getLink().getFQTI();
            sb.append(topologyIdent);
            String localIdent = URNParser.abbreviate(topologyIdent);
            sb.append(localIdent);
        }
        for (Pattern pattern: patterns.values()) {
            Matcher matcher = pattern.matcher(sb.toString());
            if (matcher.matches()) {
                return true;
            }
        }
        return false;
    }
}
