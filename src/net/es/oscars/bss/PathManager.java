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
    private TypeConverter tc;
    private String dbname;
    public String DEFAULT_SWCAP_TYPE;
    public String DEFAULT_ENC_TYPE;

    /** Constructor. */
    public PathManager(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.pceMgr = new PCEManager(dbname);
        this.policyMgr = new PolicyManager(dbname);
        this.tc = new TypeConverter();
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

        try {
            intraPath = this.pceMgr.findPath(pathInfo, resv);
        } catch (PathfinderException ex) {
            throw new BSSException(ex.getMessage());
        }

        if (intraPath == null || intraPath.getPath() == null) {
            throw new BSSException("Pathfinder could not find a path!");
        }

        //Convert any local hops that are still references to link objects
        this.expandLocalHops(pathInfo);
        this.expandLocalHops(intraPath);

        ReservationDAO dao = new ReservationDAO(this.dbname);
        List<Reservation> reservations = dao.overlappingReservations(resv.getStartTime(), resv.getEndTime());
        this.policyMgr.checkOversubscribed(reservations, pathInfo, intraPath.getPath(), resv);

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
                        this.tc.hopToURN(nextExternalHop));
            }
        }
        // convert to form for db
        Path path = this.convertPath(intraPath, pathInfo, nextDomain);
        path.setExplicit(isExplicit);
        return path;
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
            String urn = this.tc.hopToURN(hop);
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
     * Finds the intradomain ingress link and returns a Path containing only
     * that link. This is needed for initially holding the reservation to
     * meet the database schema requirments.
     *
     * @param pathInfo the PathInfo element to analyze
     * @return the Path containing only the ingress link
     */
    public Path buildInitialPath(PathInfo pathInfo) throws BSSException{
        this.log.debug("buildInitialPath.start");
        Layer2Info layer2Info = pathInfo.getLayer2Info();
        Layer3Info layer3Info = pathInfo.getLayer3Info();
        MplsInfo mplsInfo = pathInfo.getMplsInfo();
        Path path = new Path();
        PathElem elem = new PathElem();
        DomainDAO domainDAO = new DomainDAO(this.dbname);
        String ingressLink = null;
        Link link = null;
        this.log.debug("buildInitialPath.pathInfo=" + pathInfo.hashCode());
        PathInfo refOnlyPathInfo = this.tc.createRefPath(pathInfo);

        //Build path containing only the ingress link id
        try{
            ingressLink = this.pceMgr.findIngress(refOnlyPathInfo);
        }catch(PathfinderException e){
            throw new BSSException(e.getMessage());
        }
        this.log.debug("ingress=" + ingressLink);
        link = domainDAO.getFullyQualifiedLink(ingressLink);
        elem.setLink(link);
        path.addPathElem(elem);
        String pathSetupMode = pathInfo.getPathSetupMode();
        if (pathSetupMode == null) {
            pathSetupMode = "timer-automatic";
        }
        if ( pathSetupMode.equals("timer-automatic")  || pathSetupMode.equals("signal-xml")) {
            path.setPathSetupMode(pathSetupMode);
        } else {
                this.log.error("invalid pathSetupMode input: " + pathSetupMode);
                path.setPathSetupMode("timer-automatic");
        }
        //Convert layer2/layer3/mplsInfo to Hibernate beans
        path.setLayer2Data(this.tc.layer2InfoToData(layer2Info));
        path.setLayer3Data(this.tc.layer3InfoToData(layer3Info));
        path.setMplsData(this.tc.mplsInfoToData(mplsInfo));

        this.log.debug("buildInitialPath.end");
        return path;
    }

    /**
     * Converts the intradomain and interdomain paths in Axis2 data structure into
     * database Path class containing both.
     *
     * @param intraPathInfo PathInfo instance (Axis2 type) with filled in info
     * @param interPathInfo PathInfo instance (Axis2 type) with filled in info
     * @param nextDomain Domain instance with information about next domain
     * @return path Path in database format
     */
    public Path convertPath(PathInfo intraPathInfo, PathInfo interPathInfo,
                            Domain nextDomain) throws BSSException {

        Link link = null;
        Link srcLink = null;
        Link destLink = null;
        boolean foundIngress = false;
        PathElem lastElem = null;
        ArrayList<String> linksList = new ArrayList<String>();

        this.log.info("convertPath.start");
        DomainDAO domainDAO = new DomainDAO(this.dbname);
        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);

        Layer2Info layer2Info = null;

        Layer3Info layer3Info = null;
        String pathSetupMode = null;

        if (interPathInfo != null) {
            layer2Info = interPathInfo.getLayer2Info();
            layer3Info = interPathInfo.getLayer3Info();
            pathSetupMode = interPathInfo.getPathSetupMode();
        }

        if (layer2Info != null) {
            srcLink = domainDAO.getFullyQualifiedLink(layer2Info.getSrcEndpoint());
            destLink = domainDAO.getFullyQualifiedLink(layer2Info.getDestEndpoint());
        }

        Path path = new Path();
        path.setNextDomain(nextDomain);

        CtrlPlanePathContent intraPath = null;
        CtrlPlaneHopContent[] hops = new CtrlPlaneHopContent[0];
        if (intraPathInfo != null) {
            intraPath = intraPathInfo.getPath();
            hops = intraPath.getHop();
        }

        List<PathElem> pathElems = new ArrayList<PathElem>();

        // set pathSetupMode, default to timer-automatic
        if (pathSetupMode == null) {
            pathSetupMode = "timer-automatic";
        }
        path.setPathSetupMode(pathSetupMode);

        for (int i = 0; i < hops.length; i++) {
            String hopTopoId = this.tc.hopToURN(hops[i]);
            this.log.debug("convertPath: converting link: "+hopTopoId);
            Hashtable<String, String> parseResults = URNParser.parseTopoIdent(hopTopoId);
            String hopType = parseResults.get("type");
            String domainId = parseResults.get("domainId");

            // can't store non-local addresses
            if (!hopType.equals("link") || !domainDAO.isLocal(domainId)) {
                this.log.debug("convertPath: not a local link");
                continue;
            }

            // check for duplicate hops in our local portion of the path
            String fqti = parseResults.get("fqti");
            if (linksList.contains(fqti)) {
                throw new BSSException("Duplicate hop in path: ["+fqti+"]");
            } else {
                linksList.add(fqti);
            }
            link = domainDAO.getFullyQualifiedLink(hopTopoId);
            if (link == null) {
                this.log.error("Couldn't find link in db for: ["+this.tc.hopToURN(hops[i])+"]");
                throw new BSSException("Couldn't find link in db for: ["+this.tc.hopToURN(hops[i])+"]");
            } else if (!link.isValid()) {
                this.log.error("Link is invalid in db for: ["+this.tc.hopToURN(hops[i])+"]");
                throw new BSSException("Link is invalid in db for: ["+this.tc.hopToURN(hops[i])+"]");
            } else {
                this.log.debug("Found link in db for: ["+this.tc.hopToURN(hops[i])+"]");
            }
            PathElem pathElem = new PathElem();
            if (!foundIngress) {
                if (layer2Info != null) {
                    this.log.debug("convertPath: found first L2 local link, setting to ingress");
                    pathElem.setDescription("ingress");
                    foundIngress = true;
                } else {
                    // layer 3 currently requires a Juniper ingress
                    net.es.oscars.pathfinder.Utils utils =
                        new net.es.oscars.pathfinder.Utils(this.dbname);
                    // assumes one to one relationship
                    Ipaddr ipaddr = ipaddrDAO.queryByParam("linkId", link.getId());
                    if (ipaddr == null) {
                        throw new BSSException("no IP address associated with link " + link.getId());
                    } else if (!ipaddr.isValid()) {
                        throw new BSSException("IP address associated with link " + link.getId() + " is not valid");
                    }
                    try {
                        String ip = utils.getLoopback(ipaddr.getIP(), "Juniper");
                        if (ip != null) {
                            pathElem.setDescription("ingress");
                            foundIngress = true;
                        }
                    } catch (PathfinderException e) {
                        throw new BSSException(e.getMessage());
                    }
                }
            }
            pathElem.setLink(link);

            /* Hold suggested VLAN tags */
            if(layer2Info != null &&
                link.getL2SwitchingCapabilityData() != null) {
                this.setL2LinkDescr(pathElem, hops[i]);
            }
            pathElems.add(pathElem);
            lastElem = pathElem;
        }

        if (!foundIngress) {
            this.log.error("No valid ingress router in path");
            throw new BSSException("No valid ingress router in path");
        }
        if (lastElem == null) {
            this.log.error("No local hops in path");
            throw new BSSException("No local hops in path");
        }
        lastElem.setDescription("egress");
        path.setPathElems(pathElems);

        if (layer2Info != null) {
            Layer2Data dbLayer2Data = new Layer2Data();
            dbLayer2Data.setSrcEndpoint(layer2Info.getSrcEndpoint());
            dbLayer2Data.setDestEndpoint(layer2Info.getDestEndpoint());
            path.setLayer2Data(dbLayer2Data);
        } else if (layer3Info != null) {
            Layer3Data dbLayer3Data = new Layer3Data();
            dbLayer3Data.setSrcHost(layer3Info.getSrcHost());
            dbLayer3Data.setDestHost(layer3Info.getDestHost());
            dbLayer3Data.setSrcIpPort(layer3Info.getSrcIpPort());
            dbLayer3Data.setDestIpPort(layer3Info.getDestIpPort());
            dbLayer3Data.setProtocol(layer3Info.getProtocol());
            dbLayer3Data.setDscp(layer3Info.getDscp());
            path.setLayer3Data(dbLayer3Data);
        }
        MplsInfo mplsInfo = interPathInfo.getMplsInfo();
        if (mplsInfo != null) {
            MPLSData dbMplsData = new MPLSData();
            Long burstLimit = new Long(
                    Integer.valueOf(mplsInfo.getBurstLimit()).longValue());
            dbMplsData.setBurstLimit(burstLimit);
            dbMplsData.setLspClass(mplsInfo.getLspClass());
            path.setMplsData(dbMplsData);
        }
        this.log.debug("convertPath.end");
        return path;
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
        intraPathInfo.setPath(this.tc.pathToCtrlPlane(path, false));
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
            this.policyMgr.checkOversubscribed(active, pathInfo,
                                               intraPath, resv);
        }
        for (CtrlPlaneHopContent hop : hops) {
            CtrlPlaneSwitchingCapabilitySpecificInfo swcapInfo =
                         hop.getLink().getSwitchingCapabilityDescriptors()
                                      .getSwitchingCapabilitySpecificInfo();
            String sug = swcapInfo.getSuggestedVLANRange();
            swcapInfo.setVlanRangeAvailability(sug);
            swcapInfo.setSuggestedVLANRange(null);
        }
        this.tc.mergePathInfo(intraPathInfo, pathInfo, true);
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
            String urn = this.tc.hopToURN(hops[i]);
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
            String hopTopoId = this.tc.hopToURN(hops[i]);
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
            String hopTopoId = this.tc.hopToURN(hops[i]);
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
            String urn = this.tc.hopToURN(hop);
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
    public String endPointSite(Reservation resv, Boolean source) {
        Path path = resv.getPath("intra");
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
