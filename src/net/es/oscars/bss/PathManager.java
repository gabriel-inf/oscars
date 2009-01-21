package net.es.oscars.bss;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.*;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwcapContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwitchingCapabilitySpecificInfo;

import net.es.oscars.oscars.*;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.bss.topology.*;
import net.es.oscars.bss.policy.*;
import net.es.oscars.pathfinder.*;

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
        //FIXME: START
        //
        // All this looks unnecessary:


        /*  try {
            intraPath = this.pceMgr.findLocalPath(resv);
        } catch (PathfinderException ex) {
            throw new BSSException(ex.getMessage());
        } */

        /*
        if (intraPath == null || intraPath.getPath() == null) {
            throw new BSSException("Pathfinder could not find a path!");
        }

        //Convert any local hops that are still references to link objects
        this.expandLocalHops(pathInfo);
        this.expandLocalHops(intraPath);

        ReservationDAO dao = new ReservationDAO(this.dbname);
        List<Reservation> reservations = dao.overlappingReservations(resv.getStartTime(), resv.getEndTime());
        this.policyMgr.checkOversubscribed(reservations, resv);

        Domain nextDomain = null;
        DomainDAO domainDAO = new DomainDAO(this.dbname);
        // get next external hop (first past egress) from the complete path
        CtrlPlaneHopContent nextExternalHop = this.getNextExternalHop(pathInfo);
        if (nextExternalHop != null){
            nextDomain = domainDAO.getNextDomain(nextExternalHop);
            if (nextDomain != null) {
                this.log.info("create.finish, next domain: " +
                          nextDomain.getUrl());
            } else {
                this.log.warn(
                        "Can't find domain url for nextExternalHop. Hop is: " +
                        WSDLTypeConverter.hopToURN(nextExternalHop));
            }
        }
        // TODO:  return which reservation path
        // Path path = this.convertPath(intraPath, pathInfo, nextDomain);
        // path.setExplicit(isExplicit);
        return null;


        // FIXME: END
         */

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
     * @param pathInfo - input pathInfo,includes either layer2 or layer3 path
     *                  information, may also include explicit path hops.
     * @return a Path structure with the intradomain path hops, nextDomain, and
     *                  whether the path hops were explicitly set by the user.
     */
    public void calculatePaths(Reservation resv)
            throws BSSException {

        List<Path> interdomainPaths = null;
        List<Path> localPaths = null;
        Path interdomainPath = null;
        Path localPath = null;
        try {

            interdomainPaths = this.pceMgr.findInterdomainPath(resv);
            localPaths = this.pceMgr.findInterdomainPath(resv);

            interdomainPath = interdomainPaths.get(0);
            localPath = localPaths.get(0);

            // FIXME: is addPath the method to use? maybe we want to replace the set of
            // paths during modify
            resv.addPath(localPath);
            resv.addPath(interdomainPath);

        } catch (PathfinderException ex) {
            this.log.error(ex.getMessage());
            throw new BSSException(ex.getMessage());
        }

        ReservationDAO dao = new ReservationDAO(this.dbname);
        List<Reservation> reservations = dao.overlappingReservations(resv.getStartTime(), resv.getEndTime());
        this.policyMgr.checkOversubscribed(reservations, resv);


        Domain nextDomain = interdomainPath.getNextDomain();
        if (nextDomain != null) {
            this.log.info("create.finish, next domain: " + nextDomain.getUrl());
        } else {
            this.log.info("create.finish, reservation terminates in this domain");
        }

    }

    /**
     * FIXME: likely not necessary / should be rethought
     *
     * Expands any local linkIdRef elements in a given path and
     * converts them to links
     *
     *
     * @deprecated
     * @param pathInfo the pathInfo containg the path to expand
     * @throws BSSException
     */
    private void expandLocalHops(PathInfo pathInfo) throws BSSException{
        CtrlPlanePathContent path = pathInfo.getPath();
        CtrlPlanePathContent expandedPath = new CtrlPlanePathContent();
        DomainDAO domainDAO = new DomainDAO(this.dbname);
        if(path == null){
            throw new BSSException("Cannot expand path because no path given");
        }
        CtrlPlaneHopContent[] hops = path.getHop();
        if(hops == null || hops.length == 0 ){
            throw new BSSException("Cannot expand path because no hops given");
        }

        for(CtrlPlaneHopContent hop : hops){
            String urn = WSDLTypeConverter.hopToURN(hop);
            //if not link then add to path
            if(urn == null){
                throw new BSSException("Cannot expand path because " +
                                   "contains invalid hop.");
            }
            Hashtable<String, String> parseResults = URNParser.parseTopoIdent(urn);
            String hopType = parseResults.get("type");
            String domainId = parseResults.get("domainId");
            boolean isLocal = domainDAO.isLocal(domainId);
            if ((!isLocal) || (isLocal && hop.getLink() != null)) {
                expandedPath.addHop(hop);
                continue;
            } else if(isLocal && (!"link".equals(hopType))) {
                throw new BSSException("Cannot expand hop because it contains " +
                                   "a hop that's not a link: " + urn);
            }
            Link dbLink = domainDAO.getFullyQualifiedLink(urn);
            CtrlPlaneHopContent expandedHop = new CtrlPlaneHopContent();
            CtrlPlaneLinkContent link = new CtrlPlaneLinkContent();
            L2SwitchingCapabilityData l2scData = dbLink.getL2SwitchingCapabilityData();
            CtrlPlaneSwcapContent swcap = new CtrlPlaneSwcapContent();
            CtrlPlaneSwitchingCapabilitySpecificInfo swcapInfo = new CtrlPlaneSwitchingCapabilitySpecificInfo();
            if (l2scData != null) {
                swcapInfo.setInterfaceMTU(l2scData.getInterfaceMTU());
                swcapInfo.setVlanRangeAvailability(l2scData.getVlanRangeAvailability());
                swcap.setSwitchingcapType("l2sc");
                swcap.setEncodingType("ethernet");
            } else {
                //TODO: What does esnet use for internal links?
                swcapInfo.setCapability("unimplemented");
                swcap.setSwitchingcapType(L2SwitchingCapType.DEFAULT_SWCAP_TYPE);
                swcap.setEncodingType(L2SwitchingCapType.DEFAULT_ENC_TYPE);
            }
            swcap.setSwitchingCapabilitySpecificInfo(swcapInfo);
            link.setId(urn);
            link.setTrafficEngineeringMetric(dbLink.getTrafficEngineeringMetric());
            link.setSwitchingCapabilityDescriptors(swcap);

            expandedHop.setId(hop.getId());
            expandedHop.setLink(link);
            expandedPath.setId(path.getId());
            expandedPath.addHop(expandedHop);
        }
        pathInfo.setPath(expandedPath);
    }

    /**
     *  s VLAN tags for reservation.
     *
     * @param resv reservation to be stored in database
     * @param path path associated with Hibernate bean
     * @param pathInfo reservation path information in Axis2 format
     */
    public void finalizeVlanTags(Reservation resv, Path interdomainPath)
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
        
        //Find ingress pathElem of next domain (if exists)
        for(PathElem interPathElem : interdomainPath.getPathElems()){
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
                vlanRange.setValue(sugVlanParam.getValue());
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
     * Sets VLAN tag on a pathElemenet.
     *
     * @param pathElem path element to be updated
     * @param hop the hop with the link information
     */
    public void setL2LinkDescr(PathElem pathElem, CtrlPlaneHopContent hop) {
        CtrlPlaneLinkContent link = hop.getLink();
        if (link == null) {
            return;
        }
        CtrlPlaneSwitchingCapabilitySpecificInfo swcapInfo =
                                    link.getSwitchingCapabilityDescriptors()
                                        .getSwitchingCapabilitySpecificInfo();
        String vlanRange = swcapInfo.getVlanRangeAvailability();
        PathElemParam pep = new PathElemParam();
        // TODO
        if ("0".equals(vlanRange)) { //untagged
            // pathElem.setLinkDescr("-"+swcapInfo.getSuggestedVLANRange());
            swcapInfo.setSuggestedVLANRange("0");
        } else {
            // pathElem.setLinkDescr(swcapInfo.getSuggestedVLANRange());
        }
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
