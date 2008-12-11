package net.es.oscars.bss;

import java.util.*;
import java.net.*;
import org.apache.log4j.*;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwcapContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwitchingCapabilitySpecificInfo;

import net.es.oscars.PropHandler;
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
    public static String DEFAULT_SWCAP_TYPE;
    public static String DEFAULT_ENC_TYPE;

    /** Constructor. */
    public PathManager(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.pceMgr = new PCEManager(dbname);
        this.policyMgr = new PolicyManager(dbname);
        this.dbname = dbname;
        this.initGlobals();
    }

    /** Initializes global variables */
    private void initGlobals() {
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties topoProps = propHandler.getPropertyGroup("topo", true);
        DEFAULT_SWCAP_TYPE = topoProps.getProperty("defaultSwcapType");
        if(DEFAULT_SWCAP_TYPE == null){
            DEFAULT_SWCAP_TYPE = "tdm";
        }
        DEFAULT_ENC_TYPE = topoProps.getProperty("defaultEncodingType");
        if(DEFAULT_ENC_TYPE == null){
            DEFAULT_ENC_TYPE = "sdh/sonet";
        }
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
    public Path getPath(Reservation resv, PathInfo pathInfo)
            throws BSSException {

        boolean isExplicit = (pathInfo.getPath() != null);
        PathInfo intraPath = null;
        
        //TODO: Update for new PCE interface
        /*  try {
            intraPath = this.pceMgr.findLocalPath(resv);
        } catch (PathfinderException ex) {
            throw new BSSException(ex.getMessage());
        } */

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
                        TypeConverter.hopToURN(nextExternalHop));
            }
        }
        // TODO:  return which reservation path
        // Path path = this.convertPath(intraPath, pathInfo, nextDomain);
        // path.setExplicit(isExplicit);
        return null;
    }

    /**
     * Expands any local linkIdRef elements in a given path and
     * converts them to links
     *
     * @param pathInfo the pathInfo containg the path to expand
     * @throws BSSException
     */
    public void expandLocalHops(PathInfo pathInfo) throws BSSException{
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
            String urn = TypeConverter.hopToURN(hop);
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
                swcap.setSwitchingcapType(DEFAULT_SWCAP_TYPE);
                swcap.setEncodingType(DEFAULT_ENC_TYPE);
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
     * Finalizes VLAN tags for reservation.
     *
     * @param resv reservation to be stored in database
     * @param path path associated with Hibernate bean
     * @param pathInfo reservation path information in Axis2 format
     */
    public void finalizeVlanTags(Reservation resv, Path path, PathInfo pathInfo)
            throws BSSException {

        this.log.info("finalizing VLAN tags");
        CtrlPlaneHopContent nextExtHop = this.getNextExternalHop(pathInfo);
        //Retrieve the local path
        PathInfo intraPathInfo = new PathInfo();
        intraPathInfo.setPath(TypeConverter.pathToCtrlPlane(path, false));
        this.expandLocalHops(intraPathInfo);
        CtrlPlanePathContent intraPath = intraPathInfo.getPath();
        CtrlPlaneHopContent[] hops = intraPath.getHop();
        List<PathElem> elems = path.getPathElems();
        String egrSuggestedVLAN = "";
        int ctr = 0;
        for(CtrlPlaneHopContent hop : hops){
            if (ctr >= elems.size()) {
                break;
            }
            PathElem elem = elems.get(ctr);
            Link link = elem.getLink();
            if (link==null || link.getL2SwitchingCapabilityData()==null) {
                ctr++;
                continue;
            }
            hop.getLink().getSwitchingCapabilityDescriptors()
                         .getSwitchingCapabilitySpecificInfo()
                         .setSuggestedVLANRange(elem.getLinkDescr());
            egrSuggestedVLAN = elem.getLinkDescr();
            ctr++;
        }

        /* Find the next hop(if any) and see if it uses the suggested VLAN.
           If not then try to choose another by doing the oversubscription
           check again. */
        String nextVlan = null;
        if (nextExtHop != null && nextExtHop.getLink() != null) {
            nextVlan = nextExtHop.getLink()
                                 .getSwitchingCapabilityDescriptors()
                                 .getSwitchingCapabilitySpecificInfo()
                                 .getVlanRangeAvailability();
        }

        if (nextVlan != null && (!egrSuggestedVLAN.equals(nextVlan))) {
            ReservationDAO dao = new ReservationDAO(this.dbname);
            List<Reservation> active = dao.overlappingReservations(
                                        resv.getStartTime(), resv.getEndTime());
            this.policyMgr.checkOversubscribed(active, resv);
        }
        for (CtrlPlaneHopContent hop : hops) {
            CtrlPlaneSwitchingCapabilitySpecificInfo swcapInfo =
                         hop.getLink().getSwitchingCapabilityDescriptors()
                                      .getSwitchingCapabilitySpecificInfo();
            String sug = swcapInfo.getSuggestedVLANRange();
            swcapInfo.setVlanRangeAvailability(sug);
            swcapInfo.setSuggestedVLANRange(null);
        }
        PathTypeConverter.mergePathInfo(intraPathInfo, pathInfo, true);
        this.convertPathElemList(intraPathInfo, path.getPathElems(), false);
    }

    /**
     * Converts the path in a pathInfo object to a PathElem bean list.
     * This is useful for storing the inter-domain
     * path and updating VLANs when a reservation completes.
     *
     * @param pathInfo the pathInfo element containing the interdomain path
     * @param currPath if path already stored then this is the list
     * @param isInter true if interdomain
     * @return converted path list
     * @throws BSSException
     */
    public List<PathElem> convertPathElemList(PathInfo pathInfo,
                                       List<PathElem> currPath, boolean isInter)
                throws BSSException {

        List<PathElem> newPath = new ArrayList<PathElem>();
        PathElem currPathElem = null;
        CtrlPlanePathContent path = pathInfo.getPath();
        if(path == null){ return null; }
        CtrlPlaneHopContent[] hops = path.getHop();
        HashMap<String, PathElem> savedElems = new HashMap<String, PathElem>();

        for (PathElem pathElem: currPath) {
            Link link = pathElem.getLink();
            if (link != null) {
                savedElems.put(link.getFQTI(), pathElem);
            }
        }

        for (int i = 0; i < hops.length; i++) {
            String urn = TypeConverter.hopToURN(hops[i]);
            Link link = null;
            try {
                link = TopologyUtil.getLink(urn, this.dbname);
            } catch(BSSException e) {
                if (isInter) {
                    //store whatever hops you can
                    continue;
                }
                throw e;
            }
            if (savedElems.containsKey(urn)) {
                currPathElem = savedElems.get(urn);
            } else {
                currPathElem = new PathElem();
                currPathElem.setLink(link);
            }
            if (link.getL2SwitchingCapabilityData() != null &&
                    hops[i].getLink() != null) {
                CtrlPlaneSwitchingCapabilitySpecificInfo swcapInfo =
                                     hops[i].getLink()
                                     .getSwitchingCapabilityDescriptors()
                                     .getSwitchingCapabilitySpecificInfo();
                String vlan = swcapInfo.getVlanRangeAvailability();
                if ("0".equals(vlan)) {
                    vlan = "-" + swcapInfo.getSuggestedVLANRange();
                    swcapInfo.setSuggestedVLANRange("0");
                }
                currPathElem.setLinkDescr(vlan);
            }
            newPath.add(currPathElem);
        }
        return newPath;
    }

    /**
     * Make a copy of either just the internal hops or all the hops on the path
     * depending on the value of the exclude boolean.
     *
     * @param pathInfo a PathInfo instance containing a path
     * @param exclude boolean indicating whether to exclude internal hops
     * @return pathCopy a CtrlPlanePathContent instance with the copied path
     */
    public CtrlPlanePathContent copyPath(PathInfo pathInfo, boolean exclude) {
        boolean edgeFound = false;
        CtrlPlaneHopContent prevHop = null;

        DomainDAO domainDAO = new DomainDAO(this.dbname);
        CtrlPlanePathContent pathCopy = new CtrlPlanePathContent();
        CtrlPlanePathContent ctrlPlanePath = pathInfo.getPath();

        if (ctrlPlanePath == null) {
            return null;
        }
        String pathId = "unimplemented";
        if (ctrlPlanePath.getId() != null && !ctrlPlanePath.getId().equals("")) {
            pathId = ctrlPlanePath.getId();
        }
        pathCopy.setId(pathId);

        CtrlPlaneHopContent[] hops = ctrlPlanePath.getHop();
        for (int i = 0; i < hops.length; i++) {
            CtrlPlaneHopContent hopCopy = new CtrlPlaneHopContent();
            if (!exclude) {
                hopCopy.setId(hops[i].getId());
                hopCopy.setLinkIdRef(hops[i].getLinkIdRef());
                hopCopy.setPortIdRef(hops[i].getPortIdRef());
                hopCopy.setNodeIdRef(hops[i].getNodeIdRef());
                hopCopy.setDomainIdRef(hops[i].getDomainIdRef());
                hopCopy.setLink(hops[i].getLink());
                hopCopy.setPort(hops[i].getPort());
                hopCopy.setNode(hops[i].getNode());
                hopCopy.setDomain(hops[i].getDomain());
                pathCopy.addHop(hopCopy);
                continue;
            }
            String hopTopoId = TypeConverter.hopToURN(hops[i]);
            Hashtable<String, String> parseResults = URNParser.parseTopoIdent(hopTopoId);
            String hopType = parseResults.get("type");
            String domainId = parseResults.get("domainId");

            if (hopType.equals("link") &&  domainDAO.isLocal(domainId)) {
                // add ingress
                if (!edgeFound || i == (hops.length - 1)) {
                    hopCopy.setId(hops[i].getId());
                    hopCopy.setLinkIdRef(hops[i].getLinkIdRef());
                    hopCopy.setPortIdRef(hops[i].getPortIdRef());
                    hopCopy.setNodeIdRef(hops[i].getNodeIdRef());
                    hopCopy.setDomainIdRef(hops[i].getDomainIdRef());
                    hopCopy.setLink(hops[i].getLink());
                    hopCopy.setPort(hops[i].getPort());
                    hopCopy.setNode(hops[i].getNode());
                    hopCopy.setDomain(hops[i].getDomain());
                    pathCopy.addHop(hopCopy);
                    edgeFound = true;
                }
                prevHop = hops[i];
                continue;
            } else if (edgeFound) {
                // add egress
                CtrlPlaneHopContent hopCopy2 = new CtrlPlaneHopContent();
                hopCopy2.setId(prevHop.getId());
                hopCopy2.setLinkIdRef(prevHop.getLinkIdRef());
                hopCopy2.setPortIdRef(prevHop.getPortIdRef());
                hopCopy2.setNodeIdRef(prevHop.getNodeIdRef());
                hopCopy2.setDomainIdRef(prevHop.getDomainIdRef());
                hopCopy2.setLink(prevHop.getLink());
                hopCopy2.setPort(prevHop.getPort());
                hopCopy2.setNode(prevHop.getNode());
                hopCopy2.setDomain(prevHop.getDomain());
                pathCopy.addHop(hopCopy2);
                edgeFound = false;
            }
            hopCopy.setId(hops[i].getId());
            hopCopy.setLinkIdRef(hops[i].getLinkIdRef());
            hopCopy.setPortIdRef(hops[i].getPortIdRef());
            hopCopy.setNodeIdRef(hops[i].getNodeIdRef());
            hopCopy.setDomainIdRef(hops[i].getDomainIdRef());
            hopCopy.setLink(hops[i].getLink());
            hopCopy.setPort(hops[i].getPort());
            hopCopy.setNode(hops[i].getNode());
            hopCopy.setDomain(hops[i].getDomain());
            pathCopy.addHop(hopCopy);
            prevHop = hops[i];
        }
        return pathCopy;
    }

    /**
     * Given a PathInfo instance with the complete path, find the
     * first hop outside the local domain.
     *
     * @param pathInfo PathInfo instance containing path
     * @return hop CtrlPlaneHopContent instance with hop in next domain
     */
    public CtrlPlaneHopContent getNextExternalHop(PathInfo pathInfo) {

        CtrlPlaneHopContent nextHop = null;
        boolean hopFound = false;

        this.log.debug("getNextExternalHop.start");
        DomainDAO domainDAO = new DomainDAO(this.dbname);
        CtrlPlanePathContent ctrlPlanePath = pathInfo.getPath();
        CtrlPlaneHopContent[] hops = ctrlPlanePath.getHop();
        for (int i = 0; i < hops.length; i++) {
            String hopTopoId = TypeConverter.hopToURN(hops[i]);
            Hashtable<String, String> parseResults = URNParser.parseTopoIdent(hopTopoId);
            String hopType = parseResults.get("type");
            String domainId = parseResults.get("domainId");
            if (!hopType.equals("link") || !domainDAO.isLocal(domainId)) {
                if (hopFound) {
                    nextHop = hops[i];
                    break;
                }
            } else {
                hopFound = true;
            }
        }
        this.log.debug("getNextExternalHop.end");
        return nextHop;
    }

    /**
     * Returns the last hop before the current domain
     *
     * @param pathInfo PathInfo instance containing path
     * @return the last hop before the current domain
     * @throws BSSException
     */
     public CtrlPlaneHopContent getPrevExternalHop(PathInfo pathInfo) {
        this.log.debug("getPrevExternalHop.start");
        DomainDAO domainDAO = new DomainDAO(this.dbname);
        CtrlPlanePathContent ctrlPlanePath = pathInfo.getPath();
        CtrlPlaneHopContent[] hops = ctrlPlanePath.getHop();
        CtrlPlaneHopContent prevHop = null;
        for (CtrlPlaneHopContent hop: hops) {
            String urn = TypeConverter.hopToURN(hop);
            Hashtable<String, String> parseResults =
                URNParser.parseTopoIdent(urn);
            String domainId = parseResults.get("domainId");
            if (domainDAO.isLocal(domainId)) {
                break;
            }
            prevHop = hop;
        }
        this.log.debug("getPrevExternalHop.start");
        return prevHop;
     }

    /**
     * Returns IP address associated with host.
     *
     * @param host string with either host name or IP address
     * @return string with IP address
     */
    public String getIpAddress(String host) throws BSSException {
        InetAddress addr = null;
        try {
            addr = InetAddress.getByName(host);
        } catch (UnknownHostException ex) {
            throw new BSSException(ex.getMessage());
        }
        // returns same value if already an IP address
        return addr.getHostAddress();
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
        if ("0".equals(vlanRange)) { //untagged
            pathElem.setLinkDescr("-"+swcapInfo.getSuggestedVLANRange());
            swcapInfo.setSuggestedVLANRange("0");
        } else {
            pathElem.setLinkDescr(swcapInfo.getSuggestedVLANRange());
        }
    }
}
