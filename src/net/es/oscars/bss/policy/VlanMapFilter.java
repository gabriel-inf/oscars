package net.es.oscars.bss.policy;

import java.util.*;

import org.apache.log4j.Logger;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwcapContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwitchingCapabilitySpecificInfo;

import net.es.oscars.bss.*;
import net.es.oscars.bss.topology.*;
import net.es.oscars.oscars.TypeConverter;
import net.es.oscars.oscars.OSCARSCore;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.PropHandler;

/**
 * VLAN policy filter that allows for VLAN mapping. It also has a property
 * used to determine the 'scope' of VLANs. The scope can be set with the 
 * 'policy.vlanFilter.scope' property in oscars.properties. Valid values are 
 * 'domain', 'node', 'port', or 'link'. The scope determines the level to which 
 * a VLAN must be unique. For example, a scope of 'node' indicates that a VLAN 
 * must be unique to a given node.
 *
 */
public class VlanMapFilter implements PolicyFilter{
    private Logger log;
    private OSCARSCore core;
    private String scope;
    
    public static final String DOMAIN_SCOPE = "domain";
    public static final String NODE_SCOPE = "node";
    public static final String PORT_SCOPE = "port";
    public static final String LINK_SCOPE = "link";

    /** Default constructor */
    public VlanMapFilter(){
        this.log = Logger.getLogger(this.getClass());
        this.core = OSCARSCore.getInstance();
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("policy.vlanFilter", true);
        this.scope = props.getProperty("scope");
        if (this.scope == null) {
            this.scope = NODE_SCOPE;
        } else {
            this.scope = this.scope.toLowerCase();
        }
    }
    
    /**
     * Checks VLANs along a given path taking into consideration VLAN 
     * translation capabilites and the allowed scope of VLANS. It also 
     * chooses a suggestedVLAN for each layer 2 hop.
     *
     * @param pathInfo the inter-domain path
     * @param hops the intra-domain hops
     * @param localLinks a list of the local links in Hibernate bean form
     * @param activeReservations the list of overlapping reservations (in terms of time)
     * @throws BSSException
     */
    public void applyFilter(PathInfo pathInfo, CtrlPlaneHopContent[] hops,
            List<Link> localLinks, Reservation newReservation, 
            List<Reservation> activeReservations) throws BSSException {

        HashMap<String, byte[]> vlanMap = new HashMap<String, byte[]>();
        HashMap<String, Boolean> untagMap = new HashMap<String, Boolean>();
        Layer2Info l2Info = pathInfo.getLayer2Info();
        String src = l2Info.getSrcEndpoint();
        String dest = l2Info.getDestEndpoint();
        VlanTag srcVtag = l2Info.getSrcVtag();
        VlanTag destVtag = l2Info.getDestVtag();
        Link ingrLink = localLinks.get(0);
        Link egrLink = localLinks.get(localLinks.size()-1);
        
        /* Step 1: Initialize each link be ANDing whats in the hop with the 
           VLANS defined in the topology description of the link */
        for(int i = 0; i < localLinks.size(); i++){
            Link link = localLinks.get(i);
            CtrlPlaneHopContent hop = hops[i];
            CtrlPlaneSwcapContent swcap = 
                            hop.getLink().getSwitchingCapabilityDescriptors();
            CtrlPlaneSwitchingCapabilitySpecificInfo swcapInfo = 
                            swcap.getSwitchingCapabilitySpecificInfo();
            L2SwitchingCapabilityData l2scData = 
                            link.getL2SwitchingCapabilityData();
            if(l2scData == null && "l2sc".equals(swcap.getSwitchingcapType())){
                throw new BSSException("Specified layer 2 switching "+
                                       "capability for a non-layer2 link " + 
                                       TypeConverter.hopToURN(hop));
            }else if(l2scData == null){ 
                continue;
            }
            byte[] topoVlans = TypeConverter.rangeStringToMask(l2scData.getVlanRangeAvailability());
            String hopVlanStr = swcapInfo.getVlanRangeAvailability();
            if(vlanMap.containsKey(k(link))){
                byte[] vlanMapMask = vlanMap.get(k(link));
                for(int j = 0; j < topoVlans.length; j++){
                    topoVlans[j] &= vlanMapMask[j];
                }
            }
            
            if(hopVlanStr != null){
                byte[] hopVlans = TypeConverter.rangeStringToMask(hopVlanStr);
                for(int j = 0; j < hopVlans.length; j++){
                    topoVlans[j] &= hopVlans[j];
                }
            }
            String rangeStr = TypeConverter.maskToRangeString(topoVlans);
            if("".equals(rangeStr)){
                throw new BSSException("VLAN(s) " + hopVlanStr + 
                            " not available for hop " + TypeConverter.hopToURN(hop));
            }else if("0".equals(rangeStr)){
                untagMap.put(link.getFQTI(), true);
                vlanMap.put(k(link), TypeConverter.rangeStringToMask(l2scData.getVlanRangeAvailability()));
            }else{
                untagMap.put(link.getFQTI(), false);
                vlanMap.put(k(link), topoVlans);
            }
        }
        
        /* Step 2: If srcVtag or destVtag are specified and one or both are in 
           the local domain then do another AND on the edges */
        if(ingrLink.getFQTI().equals(src) && srcVtag != null 
                && vlanMap.containsKey(k(ingrLink))){
            this.applyEndpointMasks(ingrLink, srcVtag, vlanMap, untagMap);
        }
        if(egrLink.getFQTI().equals(dest) && destVtag != null 
                && vlanMap.containsKey(k(egrLink))){
            this.applyEndpointMasks(egrLink, destVtag, vlanMap, untagMap);
        }
        
        /* Step 3: For each overlapping reservation remove the tags in use */
        for (Reservation resv : activeReservations) {
            if (!resv.getGlobalReservationId().equals(newReservation.getGlobalReservationId())) {
                this.examineReservation(resv, newReservation.getLogin(), vlanMap, untagMap);
            } 
        }
        
        /* Step 4: Remove any VLANs that won't be sent by the remote end of
           the ingress link or can't be sent to the remote side of the egress 
           link */
        CtrlPlaneHopContent[] interHops = pathInfo.getPath().getHop();
        CtrlPlaneHopContent prevInterHop = this.getPrevExternalL2scHop(interHops);
        CtrlPlaneHopContent nextInterHop = this.getNextExternalL2scHop(interHops);
        if(prevInterHop != null && prevInterHop.getLink() != null){
            String prevVlanString = 
                            prevInterHop.getLink()
                                        .getSwitchingCapabilityDescriptors()
                                        .getSwitchingCapabilitySpecificInfo()
                                        .getVlanRangeAvailability();
            prevVlanString = (prevVlanString == null ? "any" : prevVlanString);
            byte[] ingrVlans = vlanMap.get(k(ingrLink));
            byte[] prevVlans = TypeConverter.rangeStringToMask(prevVlanString);
            for(int j = 0; j < ingrVlans.length; j++){
                ingrVlans[j] &= prevVlans[j];
            }
            String remaining = TypeConverter.maskToRangeString(ingrVlans);
            if("".equals(remaining)){
                throw new BSSException("No VLANs available in the range " +
                                       "specified by the previous hop " + 
                                       TypeConverter.hopToURN(prevInterHop));
            }else if("0".equals(remaining)){
                untagMap.put(ingrLink.getFQTI(), true);
            }else{
                vlanMap.put(k(ingrLink), ingrVlans);
            }
        }
        if(nextInterHop != null && nextInterHop.getLink() != null){
            String nextVlanString = 
                            nextInterHop.getLink()
                                        .getSwitchingCapabilityDescriptors()
                                        .getSwitchingCapabilitySpecificInfo()
                                        .getVlanRangeAvailability();
            nextVlanString = (nextVlanString == null ? "any" : nextVlanString);
            byte[] egrVlans = vlanMap.get(k(egrLink));
            byte[] nextVlans = TypeConverter.rangeStringToMask(nextVlanString);
            for(int j = 0; j < egrVlans.length; j++){
                egrVlans[j] &= nextVlans[j];
            }
            String remaining = TypeConverter.maskToRangeString(egrVlans);
            if("".equals(remaining)){
                throw new BSSException("No VLANs available in the range " +
                                       "specified by the next hop " + 
                                       TypeConverter.hopToURN(nextInterHop));
            }else if("0".equals(remaining)){
                untagMap.put(egrLink.getFQTI(), true);
            }else{
                vlanMap.put(k(egrLink), egrVlans);
            }
        }
        
        /* Step 5: Locate links capable of vlan translation and merge vlan
           ranges for links that don't support it */
        ArrayList<CtrlPlaneHopContent[]> segments = new ArrayList<CtrlPlaneHopContent[]>();
        ArrayList<byte[]> segmentMasks = new ArrayList<byte[]>();
        Link prevLink = localLinks.get(0);
        ArrayList<CtrlPlaneHopContent> currSegment = new ArrayList<CtrlPlaneHopContent>();
        currSegment.add(hops[0]);
        byte[] currSegmentMask = vlanMap.get(k(prevLink));
        byte[] globalMask = new byte[currSegmentMask.length];
        for(int i=1;i < currSegmentMask.length; i++){
            globalMask[i] = currSegmentMask[i];
        }
        
        for(int i=1; i < localLinks.size(); i++){
            Link currLink = localLinks.get(i);
            L2SwitchingCapabilityData l2scData = 
                            currLink.getL2SwitchingCapabilityData();
            /* If not a layer 2 link then skip */
            if(l2scData == null || (!vlanMap.containsKey(k(currLink)))){
                continue;
            }
            
            byte[] currHopMask = vlanMap.get(k(currLink));
            /* Update global mask whether translation supported or not */
            for(int j = 0; j < currHopMask.length; j++){
                globalMask[j] &= currHopMask[j];
            }
            
            
            /* If supports VLAN translation and is not the remote end of the 
               previous link then no further filtering needed 
               if untagged make its own segment like translation. This assumes 
               that untagged can only happen on the edges */
            String currHopRangeStr = TypeConverter.maskToRangeString(currHopMask);
            String rangeStr = TypeConverter.maskToRangeString(currSegmentMask);
            if(l2scData.getVlanTranslation() &&
                    (currLink.getRemoteLink() == null || 
                    (!currLink.getRemoteLink().equals(prevLink)))){
                segments.add(currSegment.toArray(new CtrlPlaneHopContent[currSegment.size()]));
                segmentMasks.add(currSegmentMask);
                currSegment = new ArrayList<CtrlPlaneHopContent>();
                currSegment.add(hops[i]);
                currSegmentMask = vlanMap.get(k(currLink));
                prevLink = currLink;
                continue;
            }
            
            for(int j = 0; j < currHopMask.length; j++){
                currSegmentMask[j] &= currHopMask[j];
            }
            if("".equals(rangeStr)){
                throw new BSSException("VLAN(s) not available for segment " +
                                       "starting at hop " + 
                                       currLink.getFQTI());
            }
            currSegment.add(hops[i]);
            prevLink = currLink;
        }
        //add last segment
        segments.add(currSegment.toArray(new CtrlPlaneHopContent[currSegment.size()]));
        segmentMasks.add(currSegmentMask);
        
        //NOTE: ignores suggested for hops beyond the first hop
        byte[] suggested = this.findSuggested(prevInterHop, hops[0]);
        String globalRange = TypeConverter.maskToRangeString(globalMask);
        String globalVlan = null;
        if(suggested.length > 0 && (!"".equals(globalRange))){
            globalVlan = this.chooseVlanTag(globalMask, suggested);
        }else if(!"".equals(globalRange)){
            globalVlan = this.chooseVlanTag(globalMask);
        }
        
        ///update intradomain path
        for(int i = 0; i < segments.size(); i++){
            String vlanRange = TypeConverter.maskToRangeString(segmentMasks.get(i));
            String suggestedVlan = null;
            if(globalVlan != null){
                suggestedVlan = globalVlan;
            }else if(suggested.length > 0){
                suggestedVlan = this.chooseVlanTag(segmentMasks.get(i), suggested);
            }else{
                suggestedVlan = this.chooseVlanTag(segmentMasks.get(i));
            }
            this.log.debug("Suggested VLAN: " + suggestedVlan);
            for(CtrlPlaneHopContent hop: segments.get(i)){
                CtrlPlaneSwitchingCapabilitySpecificInfo swcapInfo = hop.getLink()
                                              .getSwitchingCapabilityDescriptors()
                                              .getSwitchingCapabilitySpecificInfo();
                if(untagMap.containsKey(hop.getLink().getId()) && 
                   untagMap.get(hop.getLink().getId())){
                    swcapInfo.setVlanRangeAvailability("0");
                }else{
                    swcapInfo.setVlanRangeAvailability(vlanRange);
                }
                swcapInfo.setSuggestedVLANRange(suggestedVlan);
                this.log.info(TypeConverter.hopToURN(hop) + "(" + vlanRange + ")");
            }
        }
        
        //update inter-domain path
        PathInfo intraPathInfo = new PathInfo();
        CtrlPlanePathContent intraPath = new CtrlPlanePathContent();
        intraPath.setHop(hops);
        intraPathInfo.setPath(intraPath);
        TypeConverter.mergePathInfo(intraPathInfo, pathInfo,true);
    }
    
    /**
     * Applies the srcVtag or destVtag filter to the ingress or egress link
     *
     * @param endLink the link being checked
     * @param endVtag the user-specified VlanTag to chech
     * @param vlanMap the current map of vlans 
     */
    private void applyEndpointMasks(Link endLink, VlanTag endVtag, 
                            HashMap<String, byte[]> vlanMap,
                            HashMap<String, Boolean> untagMap) throws BSSException{
        if(endVtag.getTagged()){
            byte[] endVtagMask = TypeConverter.rangeStringToMask(endVtag.getString());
            byte[] vlanMapMask = vlanMap.get(k(endLink));
            for(int j = 0; j < vlanMapMask.length; j++){
                vlanMapMask[j] &= endVtagMask[j];
            }
            String rangeStr = TypeConverter.maskToRangeString(vlanMapMask);
            if("".equals(rangeStr)){
                throw new BSSException("VLAN not available for edge link " + 
                                       endLink.getFQTI());
            }
            vlanMap.put(k(endLink), vlanMapMask);
        }else{
            byte[] vlanMapMask = vlanMap.get(k(endLink));
            byte[] endVtagMask = TypeConverter.rangeStringToMask("0");
            //check if untagged allowed
            endVtagMask[0] &= vlanMapMask[0];
            String rangeStr = TypeConverter.maskToRangeString(endVtagMask);
            if("".equals(rangeStr)){
                throw new BSSException("Link " + endLink.getFQTI() + 
                                       " cannot be untagged");
            }
            untagMap.put(endLink.getFQTI(), true);
            //vlanMap.put(k(endLink), endVtagMask);
        }
    }
    
    /**
     * Removes VLANs from potential list that are already in use
     *
     * @param resv the existing reservation using a VLAN
     * @param newLogin the login of the user creating the new reservation
     * @param vlanMap the map of vlans already in use
     */
    public void examineReservation(Reservation resv, String newLogin, 
                            HashMap<String, byte[]> vlanMap,
                            HashMap<String, Boolean> untagMap) throws BSSException{
        Path resvPath = resv.getPath("intra");
        List<PathElem> pathElems = resvPath.getPathElems();
        for (PathElem pathElem: pathElems) {
            Link link = pathElem.getLink();
            if (link == null) {
                continue;
            }
            String fqti = link.getFQTI();
            L2SwitchingCapabilityData l2scData = link.getL2SwitchingCapabilityData();
            if (l2scData == null || !vlanMap.containsKey(k(link)) || 
                    pathElem.getLinkDescr() == null) {
                continue;
            }
            byte[] resvMask = TypeConverter.rangeStringToMask(pathElem.getLinkDescr());
            boolean resvUntagged = false;
            try {
                resvUntagged = (Integer.parseInt(pathElem.getLinkDescr()) < 0);
            } catch (Exception e){}
            //NOTE: Even if untagMap contains false, it can be used to match links in path
            if (resvUntagged && untagMap.containsKey(fqti) &&
                newLogin.equals(resv.getLogin())) {
                throw new BSSException("Untagged VLAN already on " + fqti +
                                        " from a reservation you previously " +
                                        "placed (" +  resv.getGlobalReservationId() + ")");
            } else if (resvUntagged && untagMap.containsKey(fqti)) {
                throw new BSSException("VLAN tag unavailable in specified " +
                 "range. If no VLAN range was specified then there are no " +
                 "available vlans along the path.");
            }   
            
            byte[] vlanMask = vlanMap.get(k(link));
            String vlanMaskStr = TypeConverter.maskToRangeString(vlanMask);
            if (untagMap.containsKey(fqti) && untagMap.get(fqti)
                    && newLogin.equals(resv.getLogin())) {
                throw new BSSException("Cannot set untagged because there is " +
                                       "another VLAN on " + fqti + 
                                       " from a reservation you " +
                                       "previously placed (" + 
                                       resv.getGlobalReservationId() + ")");
            } else if(untagMap.containsKey(fqti) && untagMap.get(fqti)) {
                throw new BSSException("VLAN tag unavailable in specified " +
                 "range. If no VLAN range was specified then there are no " +
                 "available vlans along the path.");
            } 
            //only add if not in use
            for (int i=0; i < vlanMask.length; i++) {
                int newTags = 0;
                for (int j = 0; j < 8; j++) {
                    if ((resvMask[i] & (int)Math.pow(2, (7-j))) == 0 &&
                       (vlanMask[i] & (int)Math.pow(2, (7-j))) > 0) {
                        newTags += (int)Math.pow(2, (7-j));
                    }
                }
                vlanMask[i] =(byte) newTags;
            }
            String remainingVlans = TypeConverter.maskToRangeString(vlanMask);
            if ("".equals(remainingVlans) && newLogin.equals(resv.getLogin())) {
                throw new BSSException("Last VLAN in use by a reservation " +
                                       "you previously placed (" + 
                                       resv.getGlobalReservationId() + ")");
            } else if("".equals(remainingVlans)) {
                throw new BSSException("VLAN tag unavailable in specified " +
                 "range. If no VLAN range was specified then there are no " +
                 "available vlans along the path.");
            }
            vlanMap.put(k(link), vlanMask);
        }
    }
    
    /**
     * Returns the last l2sc hop before the current domain
     *
     * @param interHops the inter-domain hops to search
     * @return the last l2sc hop before the current domain
     * @throws BSSException
     */
     public CtrlPlaneHopContent getPrevExternalL2scHop(CtrlPlaneHopContent[] interHops) 
                                                        throws BSSException{
        DomainDAO domainDAO = new DomainDAO(this.core.getBssDbName());
        CtrlPlaneHopContent prevInterHop = null;
        for(CtrlPlaneHopContent interHop : interHops){
            CtrlPlaneLinkContent interLink = interHop.getLink();
            if(interLink == null){
                throw new BSSException("Received hop from previous domain " +
                                       "that is not a link: " + 
                                       TypeConverter.hopToURN(interHop));
            }
            CtrlPlaneSwcapContent swcap = 
                            interLink.getSwitchingCapabilityDescriptors();
            String urn = TypeConverter.hopToURN(interHop);
            Hashtable<String, String> parseResults = URNParser.parseTopoIdent(urn);
            String domainId = parseResults.get("domainId");
            if(domainDAO.isLocal(domainId)){
                break;
            }else if(!"l2sc".equals(swcap.getSwitchingcapType())){
                continue;
            }
            prevInterHop = interHop;
        }
        
        return prevInterHop;
     }
     
     /**
     * Returns the next l2sc hop past the current domain
     *
     * @param interHops the inter-domain hops to search
     * @return the next l2sc hop past the current domain
     * @throws BSSException
     */
     public CtrlPlaneHopContent getNextExternalL2scHop(CtrlPlaneHopContent[] interHops) 
                                                        throws BSSException{
        
        DomainDAO domainDAO = new DomainDAO(this.core.getBssDbName());
        CtrlPlaneHopContent nextHop = null;
        boolean hopFound = false;

        for (int i = 0; i < interHops.length; i++) {
            String hopTopoId = TypeConverter.hopToURN(interHops[i]);
            Hashtable<String, String> parseResults = URNParser.parseTopoIdent(hopTopoId);
            String hopType = parseResults.get("type");
            String domainId = parseResults.get("domainId");
            if (!hopType.equals("link") || !domainDAO.isLocal(domainId)) {
                if (hopFound) {
                    nextHop = interHops[i];
                    break;
                }
            } else {
                hopFound = true;
            }
        }
        return nextHop;
    }
    
    /**
     * Finds the suggestedVlans if any by examining the remote side of the 
     * ingress link and by examining the current hop. Defaults to the remote 
     * side of the ingress link since the previous domain should be holding 
     * any VLANs it suggests. Returns a 0 length array if no suggestedVLANs.
     *
     * @param prevInterHop the the last l2sc hop in the previous domain
     * @param currHop the first hop in the current domain
     * @return a byte mask of the suggested vlans. 0 length if none found.
     * @throws BSSException
     */
    private byte[] findSuggested(CtrlPlaneHopContent prevInterHop, 
                            CtrlPlaneHopContent currHop) throws BSSException{
        //choose a suggested VLAN
        byte[] suggested = new byte[0];
        boolean useSuggested = false;
        String sugVlan = null;
        if(prevInterHop != null){
            sugVlan = prevInterHop.getLink()
                                  .getSwitchingCapabilityDescriptors()
                                  .getSwitchingCapabilitySpecificInfo()
                                   .getSuggestedVLANRange();
            
        }
        if(sugVlan == null && currHop != null){
            sugVlan = currHop.getLink()
                             .getSwitchingCapabilityDescriptors()
                             .getSwitchingCapabilitySpecificInfo()
                             .getSuggestedVLANRange();
        }
        if(sugVlan != null){
            suggested = TypeConverter.rangeStringToMask(sugVlan);
        }
        
        return suggested;
    }
    
    /**
     * Return the key(k) of vlanMap based on the scope defined in 
     * oscars.properties
     *
     * @param link the link from which to extract the key
     * @return the key for this link based on the scope
     * @throws BSSException
     */
    private String k(Link link) throws BSSException{
        String key = null;
        if(this.scope.equals(DOMAIN_SCOPE)){
            Domain d = link.getPort().getNode().getDomain();
            key = d.getFQTI();
        }else if(this.scope.equals(NODE_SCOPE)){
            Node n = link.getPort().getNode();
            key = n.getFQTI();
        }else if(this.scope.equals(PORT_SCOPE)){
            Port p = link.getPort();
            key = p.getFQTI();
        }else if(this.scope.equals(LINK_SCOPE)){
            key = link.getFQTI();
        }else{
            this.log.error("Invalid VLAN filter '" + this.scope + "'. " +
                           "Please check the value of " +
                           "policy.vlanFilter.scope in oscars.properties. " +
                           "Valid values are domain, node port or link.");
            throw new BSSException("OSCARS configuration error for VLAN " +
                                   "filter. Please contact administrator");
        }
        
        return key;
    }

    /**
     * Picks a VLAN tag from a range of VLANS given a suggested VLAN Range
     *
     * @param mask the range to choose from
     * @param suggested a suggested range to try first
     * @return the chosen VLAN as a string
     * @throws BSSException
     */
    public String chooseVlanTag(byte[] mask, byte[] suggested)
            throws BSSException {

        // Never pick untagged
        mask[0] &= (byte) 127;
        suggested[0] &= (byte) 127;
        //Try suggested
        for (int i=0; i < suggested.length; i++) {
            suggested[i] &= mask[i];
        }
        String remaining = TypeConverter.maskToRangeString(suggested);
        if (!"".equals(remaining)) {
            mask = suggested;
        }
        return this.chooseVlanTag(mask);
    }

    /**
     * Picks a VLAN tag from a range of VLANS
     *
     * @param mask the range to choose from
     * @return the chosen VLAN as a string
     * @throws BSSException
     */
    public String chooseVlanTag(byte[] mask) throws BSSException{
        // Never pick untagged
        mask[0] &= (byte) 127;

        // pick one
        ArrayList<Integer> vlanPool = new ArrayList<Integer>();
        for (int i=0; i < mask.length; i++) {
            for (int j = 0; j < 8; j++) {
                int tag = i*8 + j;
                if ((mask[i] & (int)Math.pow(2, (7-j))) > 0) {
                    vlanPool.add(tag);
                }
            }
        }
        int index = 0;
        if (vlanPool.size() > 1) {
            Random rand = new Random();
            index = rand.nextInt(vlanPool.size()-1);
        } else if (vlanPool.size() == 0) {
            return null;
        }
        return vlanPool.get(index).toString();
    }
}
