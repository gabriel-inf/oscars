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
            Path requestedPath = resv.getPath(PathType.REQUESTED);
            this.resolveRequestedPath(requestedPath);
            
            //Find interdomain path (layer 2 only)
            if (requestedPath.getLayer2Data() != null) {
                interdomainPaths = this.pceMgr.findInterdomainPath(resv);
                interdomainPath = interdomainPaths.get(0);
                BssUtils.copyPathFields(requestedPath, interdomainPath);
                resv.setPath(interdomainPath);
            }
            
            //Find local path
            localPaths = this.pceMgr.findLocalPath(resv);
            localPath = localPaths.get(0);
            BssUtils.copyPathFields(requestedPath, localPath);
            resv.setPath(localPath);

            // Set up interdomain path with local ingress and egress as
            // placeholder if layer 3
            if (interdomainPath == null) {
                interdomainPath = new Path();
                interdomainPath.setPathType(PathType.INTERDOMAIN);
                List<PathElem> pathElems = new ArrayList<PathElem>();
                List<PathElem> localPathElems = localPath.getPathElems();
                pathElems.add(PathElem.copyPathElem(localPathElems.get(0)));
                pathElems.add(PathElem.copyPathElem(
                    localPathElems.get(localPathElems.size()-1)));
                interdomainPath.setPathElems(pathElems);
                BssUtils.copyPathFields(requestedPath, interdomainPath);
                resv.setPath(interdomainPath);
            }
        } catch (PathfinderException ex) {
            this.log.error(ex.getMessage());
            throw new BSSException(ex.getMessage());
        }
        
        this.checkOversubscription(resv);
        Domain nextDomain = interdomainPath.getNextDomain();
        if (nextDomain != null) {
            this.log.info("create.finish, next domain: " + nextDomain.getUrl());
        } else {
            this.log.info("create.finish, reservation terminates in this domain");
        }
    }
    
    public void checkOversubscription(Reservation resv) throws BSSException{
        ReservationDAO dao = new ReservationDAO(this.dbname);
        List<Reservation> reservations =
            dao.overlappingReservations(resv.getStartTime(), resv.getEndTime());
        this.policyMgr.checkOversubscribed(reservations, resv);
    }
    
    private void resolveRequestedPath(Path requestedPath) throws BSSException {
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
                Link link = null;
                
                //catch exception because inter-domain links are not in our database
                try{
                    link = TopologyUtil.getLink(urn, this.dbname);
                }catch(Exception e){
                    if(urn.startsWith("urn:ogf:network")){
                        continue;
                    }
                }
                
                if (link != null) {
                    pe.setLink(link);
                } else {
                    //Not in database and not a URN so try the lookup service
                    try {
                        PSLookupClient lookupClient = new PSLookupClient();
                        String resolved = lookupClient.lookup(urn);
                        //...again if its an interdomain link it might not be in the DB
                        try{ 
                            link = TopologyUtil.getLink(resolved, this.dbname); 
                        }catch(Exception e){}
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
                if (sugVlanParam == null) {
                    continue;
                }
                
                PathElemParam vlanRange = elem.getPathElemParam(
                        PathElemParamSwcap.L2SC, PathElemParamType.L2SC_VLAN_RANGE);
                if (vlanRange == null) {
                    vlanRange = new PathElemParam();
                    vlanRange.setType(PathElemParamType.L2SC_VLAN_RANGE);
                    vlanRange.setSwcap(PathElemParamSwcap.L2SC);
                    elem.addPathElemParam(vlanRange);
                    this.log.debug("Added another VLAN Range element");
                }
                
                /* Add negative number to suggested VLAN if link is untagged. Save as 
                 * negative so can track which VLAN untagged ports belong to on node.
                 */
                String negative = "0".equals(vlanRange.getValue()) ? "-" : "";
                vlanRange.setValue(negative + sugVlanParam.getValue());
                this.log.debug("VLAN range for " + elem.getUrn() + 
                            " set to " + vlanRange.getValue());
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
     * @return the topology identifier of the endpoint domain
     */
    public String endPointSite(Reservation resv, Boolean source) throws BSSException {
        Path path = resv.getPath(PathType.LOCAL);
        if (path == null){
            this.log.error("path is null");
        }
        List<PathElem> hops = path.getPathElems();
        if (hops == null){
            this.log.error("hops is null");
        }
        PathElem hop = null;
        if (source) {
            hop = hops.get(0);
        } else { // get last hop
            hop = hops.get(hops.size()-1);
        }
        Link endPoint = hop.getLink();
        Link remoteLink = endPoint.getRemoteLink();
        String topologyIdent = "UNKNOWN";
        Domain endDomain = null;
        String FQTI = null;
        if (remoteLink != null) {
            endDomain = remoteLink.getPort().getNode().getDomain();
            FQTI=remoteLink.getFQTI();
            this.log.debug("remote link: " + FQTI + " domain: " + endDomain.getTopologyIdent() );
        } else {
            endDomain = endPoint.getPort().getNode().getDomain();
            FQTI=endPoint.getFQTI();
            this.log.debug("endPoint link is: " + FQTI + " domain: " + endDomain.getTopologyIdent());
        }
        if (endDomain != null ) {
            /*
            Site institution = endDomain.getSite();
            if (institution != null) {
                institutionName = institution.getName();
            }
            */
            topologyIdent = endDomain.getTopologyIdent();
            this.log.debug("toplogyIdent is " + topologyIdent);
        }
        return topologyIdent;
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
