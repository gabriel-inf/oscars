package net.es.oscars.bss.policy;

import java.util.*;

import org.apache.log4j.Logger;

import net.es.oscars.bss.*;
import net.es.oscars.bss.topology.*;
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
     * translation capabilities and the allowed scope of VLANS. It also
     * chooses a suggestedVLAN for each layer 2 hop.
     *
     * @param newReservation reservation containing the inter-domain and local paths
     * @param activeReservations the list of overlapping reservations (in terms of time)
     * @throws BSSException
     */
    public void applyFilter(Reservation newReservation,
            List<Reservation> activeReservations) throws BSSException {

        HashMap<String, byte[]> vlanMap = new HashMap<String, byte[]>();
        HashMap<String, Boolean> untagMap = new HashMap<String, Boolean>();
        Path localPath = newReservation.getPath(PathType.LOCAL);
        Path interPath = newReservation.getPath(PathType.INTERDOMAIN);
        List<PathElem> localPathElems = localPath.getPathElems();
        List<PathElem> interPathElems = interPath.getPathElems();
        Link ingrLink = localPathElems.get(0).getLink();
        Link egrLink = localPathElems.get(localPathElems.size() - 1).getLink();

        /* Step 1: Initialize each link be ANDing what is in the hop with the
           VLANS defined in the topology description of the link */
        for(PathElem pathElem: localPathElems){
            Link link = pathElem.getLink();
            L2SwitchingCapabilityData l2scData =
                            link.getL2SwitchingCapabilityData();
            if(l2scData == null){
                /* if not a layer 2 link then don't need to check VLANs */
                continue;
            }
            byte[] topoVlans = VlanMapFilter.rangeStringToMask(l2scData.getVlanRangeAvailability());
           // String hopVlanStr = pathElemParams.
            if(vlanMap.containsKey(k(link))){
                byte[] vlanMapMask = vlanMap.get(k(link));
                for(int j = 0; j < topoVlans.length; j++){
                    topoVlans[j] &= vlanMapMask[j];
                }
            }

            String hopVlanStr = null;
            PathElemParam vlanRangeParam = pathElem.getPathElemParam(PathElemParamSwcap.L2SC, PathElemParamType.L2SC_VLAN_RANGE);
            if(vlanRangeParam != null){
                hopVlanStr = vlanRangeParam.getValue();
                byte[] hopVlans = VlanMapFilter.rangeStringToMask(hopVlanStr);
                for(int j = 0; j < hopVlans.length; j++){
                    topoVlans[j] &= hopVlans[j];
                }
            }
            String rangeStr = VlanMapFilter.maskToRangeString(topoVlans);
            this.log.debug("Init VLANs for " + pathElem.getUrn() + ": " + rangeStr);
            if("".equals(rangeStr)){
                throw new BSSException("VLAN(s) " + hopVlanStr +
                            " not available for hop " + pathElem.getUrn());
            }else if("0".equals(rangeStr)){
                this.log.debug("adding " + link.getFQTI() + " to UntagMap");
                untagMap.put(link.getFQTI(), true);
                vlanMap.put(k(link), VlanMapFilter.rangeStringToMask(l2scData.getVlanRangeAvailability()));
            }else{
                untagMap.put(link.getFQTI(), false);
                vlanMap.put(k(link), topoVlans);
            }
        }

        /* Step 2: For each overlapping reservation remove the tags in use */
        for (Reservation resv : activeReservations) {
            if (!resv.getGlobalReservationId().equals(newReservation.getGlobalReservationId())) {
                this.examineReservation(resv, newReservation.getLogin(), vlanMap, untagMap);
            }
        }

        /* Step 3: Remove any VLANs that won't be sent by the remote end of
           the ingress link or can't be sent to the remote side of the egress
           link */
        PathElem prevInterPathElem = this.getPrevExternalL2scHop(interPathElems);
        PathElem nextInterPathElem = this.getNextExternalL2scHop(interPathElems);
        if(prevInterPathElem != null && prevInterPathElem.getLink() != null){
            String prevVlanString = prevInterPathElem.getPathElemParam(PathElemParamSwcap.L2SC, PathElemParamType.L2SC_VLAN_RANGE).getValue();
            prevVlanString = (prevVlanString == null ? "any" : prevVlanString);
            byte[] ingrVlans = vlanMap.get(k(ingrLink));
            byte[] prevVlans = VlanMapFilter.rangeStringToMask(prevVlanString);
            for(int j = 0; j < ingrVlans.length; j++){
                ingrVlans[j] &= prevVlans[j];
            }
            String remaining = VlanMapFilter.maskToRangeString(ingrVlans);
            if("".equals(remaining)){
                throw new BSSException("No VLANs available in the range " +
                                       "specified by the previous hop " +
                                       prevInterPathElem.getUrn());
            }else if("0".equals(remaining)){
                untagMap.put(ingrLink.getFQTI(), true);
            }else{
                vlanMap.put(k(ingrLink), ingrVlans);
            }
        }
        if(nextInterPathElem != null && nextInterPathElem.getLink() != null){
            String nextVlanString = nextInterPathElem.getPathElemParam(PathElemParamSwcap.L2SC, PathElemParamType.L2SC_VLAN_RANGE).getValue();
            nextVlanString = (nextVlanString == null ? "any" : nextVlanString);
            byte[] egrVlans = vlanMap.get(k(egrLink));
            byte[] nextVlans = VlanMapFilter.rangeStringToMask(nextVlanString);
            for(int j = 0; j < egrVlans.length; j++){
                egrVlans[j] &= nextVlans[j];
            }
            String remaining = VlanMapFilter.maskToRangeString(egrVlans);
            if("".equals(remaining)){
                throw new BSSException("No VLANs available in the range " +
                                       "specified by the next hop " +
                                       nextInterPathElem.getUrn());
            }else if("0".equals(remaining)){
                untagMap.put(egrLink.getFQTI(), true);
            }else{
                vlanMap.put(k(egrLink), egrVlans);
            }
        }

        /* Step 4: Locate links capable of vlan translation and merge vlan
           ranges for links that don't support it */
        ArrayList<List<PathElem>> segments = new ArrayList<List<PathElem>>();
        ArrayList<byte[]> segmentMasks = new ArrayList<byte[]>();
        Link prevLink = ingrLink;
        ArrayList<PathElem> currSegment = new ArrayList<PathElem>();
        currSegment.add(localPathElems.get(0));
        byte[] currSegmentMask = vlanMap.get(k(prevLink));
        byte[] globalMask = new byte[currSegmentMask.length];
        for(int i=1;i < currSegmentMask.length; i++){
            globalMask[i] = currSegmentMask[i];
        }

        for(int i=1; i < localPathElems.size(); i++){
            Link currLink = localPathElems.get(i).getLink();
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
            String rangeStr = VlanMapFilter.maskToRangeString(currSegmentMask);
            if(l2scData.getVlanTranslation() &&
                    (currLink.getRemoteLink() == null ||
                    (!currLink.getRemoteLink().equals(prevLink)))){
                segments.add(currSegment);
                segmentMasks.add(currSegmentMask);
                currSegment = new ArrayList<PathElem>();
                currSegment.add(localPathElems.get(i));
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
            currSegment.add(localPathElems.get(i));
            prevLink = currLink;
        }
        //add last segment
        segments.add(currSegment);
        segmentMasks.add(currSegmentMask);

        //NOTE: ignores suggested for hops beyond the first hop
        byte[] suggested = this.findSuggested(prevInterPathElem, localPathElems.get(0));
        String globalRange = VlanMapFilter.maskToRangeString(globalMask);
        String globalVlan = null;
        if(suggested.length > 0 && (!"".equals(globalRange))){
            globalVlan = this.chooseVlanTag(globalMask, suggested);
        }else if(!"".equals(globalRange)){
            globalVlan = this.chooseVlanTag(globalMask);
        }

        ///update intradomain path
        for(int i = 0; i < segments.size(); i++){
            String vlanRange = VlanMapFilter.maskToRangeString(segmentMasks.get(i));
            String suggestedVlan = null;
            if(globalVlan != null){
                suggestedVlan = globalVlan;
            }else if(suggested.length > 0){
                suggestedVlan = this.chooseVlanTag(segmentMasks.get(i), suggested);
            }else{
                suggestedVlan = this.chooseVlanTag(segmentMasks.get(i));
            }
            this.log.debug("Suggested VLAN: " + suggestedVlan);
            for(PathElem pathElem: segments.get(i)){
                //If not a layer 2 link than skip it to prevent bad PathElemParam creation
                if(pathElem.getLink() == null || pathElem.getLink().getL2SwitchingCapabilityData() == null){
                    continue;
                }
                PathElemParam peVlanRange = pathElem.getPathElemParam(PathElemParamSwcap.L2SC, PathElemParamType.L2SC_VLAN_RANGE);
                if (peVlanRange == null) {
                    peVlanRange = new PathElemParam();
                    peVlanRange.setSwcap(PathElemParamSwcap.L2SC);
                    peVlanRange.setType(PathElemParamType.L2SC_VLAN_RANGE);
                    pathElem.getPathElemParams().add(peVlanRange);
                }
                PathElemParam peSugVlan = pathElem.getPathElemParam(PathElemParamSwcap.L2SC, PathElemParamType.L2SC_SUGGESTED_VLAN);

                if(untagMap.containsKey(pathElem.getLink().getFQTI()) &&
                        untagMap.get(pathElem.getLink().getFQTI())){
                    peVlanRange.setValue("0");
                    this.log.debug("Set untagged VLAN for " + 
                            pathElem.getLink().getFQTI());
                }else{
                    peVlanRange.setValue(vlanRange);
                }

                if(peSugVlan == null){
                    peSugVlan= new PathElemParam();
                    peSugVlan.setSwcap(PathElemParamSwcap.L2SC);
                    peSugVlan.setType(PathElemParamType.L2SC_SUGGESTED_VLAN);
                    pathElem.addPathElemParam(peSugVlan);
                }
                peSugVlan.setValue(suggestedVlan);
                this.log.info(pathElem.getUrn() + "(" + peVlanRange.getValue() + ")");
            }
        }

        /* Update inter-domain path with VLAN ranges. Only need to update the ingress
         * and egress hops. THIS IS AN ASSUMPTION THAT LINK IDS IN INTERDOMAIN AND LOCAL 
         * PATH LOOK THE SAME.*/
        PathElem ingrPathElem = localPathElems.get(0);
        PathElem egrPathElem = localPathElems.get(localPathElems.size() - 1);
        for(PathElem interPathElem : interPathElems){
            if(ingrPathElem.getUrn().equals(interPathElem.getUrn())){
                PathElem.copyPathElemParams(interPathElem, ingrPathElem, PathElemParamSwcap.L2SC);
            }else if(egrPathElem.getUrn().equals(interPathElem.getUrn())){
                PathElem.copyPathElemParams(interPathElem, egrPathElem, PathElemParamSwcap.L2SC);
                break;
            }
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
        Path resvPath = resv.getPath(PathType.LOCAL);
        List<PathElem> pathElems = resvPath.getPathElems();
        for (PathElem pathElem: pathElems) {
            Link link = pathElem.getLink();
            if (link == null) {
                continue;
            }
            String fqti = link.getFQTI();
            L2SwitchingCapabilityData l2scData = link.getL2SwitchingCapabilityData();
            if (l2scData == null || !vlanMap.containsKey(k(link))) {
                continue;
            }
            
            /* Check VLAN range first. If VLAN not a single integer then the 
             * reservation is likely still getting processed.Try the suggestedVlan
             * instead since that field indicates the value being held for reservations
             * INCREATE.
             */
            PathElemParam pep =
                pathElem.getPathElemParam(PathElemParamSwcap.L2SC,
                                          PathElemParamType.L2SC_VLAN_RANGE);
            String linkDescr = null;
            try{
                linkDescr = pep.getValue();
                Integer.parseInt(linkDescr);
            }catch(Exception e){
                pep =
                    pathElem.getPathElemParam(PathElemParamSwcap.L2SC,
                                              PathElemParamType.L2SC_SUGGESTED_VLAN);
                if(pep != null){
                    linkDescr = pep.getValue();
                }
            }
            //No VLAN range or suggested VLAN fields. This should not happen
            if(linkDescr == null){
                this.log.debug("Skipping link with no VLAN range or suggested VLAN");
                continue;
            }
            
            /* Now that we have the vlan for the link apply to the map */
            byte[] resvMask = VlanMapFilter.rangeStringToMask(linkDescr);
            boolean resvUntagged = false;
            try {
                resvUntagged = (Integer.parseInt(linkDescr) <= 0);
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
            String remainingVlans = VlanMapFilter.maskToRangeString(vlanMask);
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
     * @param interPathElems the inter-domain hops to search
     * @return the last l2sc hop before the current domain
     * @throws BSSException
     */
     public PathElem getPrevExternalL2scHop(List<PathElem> interPathElems)
                                                        throws BSSException{
        DomainDAO domainDAO = new DomainDAO(this.core.getBssDbName());
        PathElem prevInterHop = null;
        for(PathElem interPathElem : interPathElems){
            PathElemParam vlanRangeParam = interPathElem.getPathElemParam(PathElemParamSwcap.L2SC, PathElemParamType.L2SC_VLAN_RANGE);
            Hashtable<String, String> parseResults = URNParser.parseTopoIdent(interPathElem.getUrn());
            String domainId = parseResults.get("domainId");
            if(domainDAO.isLocal(domainId)){
                break;
            }else if(vlanRangeParam == null){
                continue;
            }
            prevInterHop = interPathElem;
        }

        return prevInterHop;
     }

     /**
     * Returns the next l2sc hop past the current domain
     *
     * @param interPathElems the inter-domain hops to search
     * @return the next l2sc hop past the current domain
     * @throws BSSException
     */
     public PathElem getNextExternalL2scHop(List<PathElem> interPathElems)
                                                        throws BSSException{

        DomainDAO domainDAO = new DomainDAO(this.core.getBssDbName());
        PathElem nextHop = null;
        boolean hopFound = false;

        for (int i = 0; i < interPathElems.size(); i++) {
            String hopTopoId =interPathElems.get(i).getUrn();
            Hashtable<String, String> parseResults = URNParser.parseTopoIdent(hopTopoId);
            String hopType = parseResults.get("type");
            String domainId = parseResults.get("domainId");
            if (!hopType.equals("link") || !domainDAO.isLocal(domainId)) {
                if (hopFound) {
                    nextHop = interPathElems.get(i);
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
     * @param prevInterPathElem the the last l2sc hop in the previous domain
     * @param currPathElem the first hop in the current domain
     * @return a byte mask of the suggested vlans. 0 length if none found.
     * @throws BSSException
     */
    private byte[] findSuggested(PathElem prevInterPathElem, PathElem currPathElem) throws BSSException{
        //choose a suggested VLAN
        byte[] suggested = new byte[0];
        String sugVlan = null;
        if(prevInterPathElem != null){
            PathElemParam sugParam = prevInterPathElem.getPathElemParam(PathElemParamSwcap.L2SC, PathElemParamType.L2SC_SUGGESTED_VLAN);
            if(sugParam != null){
                sugVlan = sugParam.getValue();
            }
        }
        if(sugVlan == null && currPathElem != null){
            PathElemParam sugParam = currPathElem.getPathElemParam(PathElemParamSwcap.L2SC, PathElemParamType.L2SC_SUGGESTED_VLAN);
            if(sugVlan != null){
                sugVlan = sugParam.getValue();
            }
        }
        if(sugVlan != null){
            suggested = VlanMapFilter.rangeStringToMask(sugVlan);
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
        String remaining = VlanMapFilter.maskToRangeString(suggested);
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

    /**
     * Converts a string to a bit mask. The range should take the form
     * "x,y" for discontinuous ranges and "x-y" for continuous ranges.
     * These formats can be concatenated to specify many subranges
     * (i.e 600,3000-3001).
     *
     * @param range the range string to be converted
     * @return a bit mask with values in given range set to 1
     * @throws BSSException
     */
    public static byte[] rangeStringToMask(String range) throws BSSException{
        byte[] mask = new byte[512];

        if (range.trim().equals("any")) {
            for (int i = 0; i < 512; i++) {
                mask[i] = (byte) 255;
            }
            return mask;
        }

        range = range.replaceAll("\\s", "");
        String[] rangeList = range.split(",");
        try {

            for(int i = 0; i < rangeList.length; i++){
                String[] rangeEnds = rangeList[i].split("-");
                if (rangeEnds.length == 1){
                    int tag = Integer.parseInt(rangeEnds[0].trim());
                    if(tag < 4096){
                        mask[tag/8] = (byte)(1 << (7 - (tag % 8)));
                    }
                } else if(rangeEnds.length == 2 && "".equals(rangeEnds[0])){
                    int tag = Integer.parseInt(rangeEnds[1].trim());
                    if(tag < 4096){
                        mask[tag/8] = (byte)(1 << (7 - (tag % 8)));
                    }
                } else if(rangeEnds.length == 2){
                    int startTag = Integer.parseInt(rangeEnds[0].trim());
                    int endTag = Integer.parseInt(rangeEnds[1].trim());
                    if (startTag < 4096 && endTag < 4096){
                        for(int j = startTag; j <= endTag; j++){
                            mask[j/8] |= (1 << (7 - (j % 8)));
                        }
                    }
                }else {
                    throw new BSSException("Invalid VLAN range specified");
                }
            }
        } catch (NumberFormatException ex) {
            throw new BSSException("Invalid VLAN range format	\n"+ ex.getMessage());
        }

        /* for(int k = 0; k < mask.length; k++){
            System.out.println(k + ": " + (byte)(mask[k] & 255));
        } */

        return mask;
    }

    /**
     * Converts given mask to a range string. The range takes the form
     * "x,y" for discontinuous ranges and "x-y" for continuous ranges.
     * These formats can be concatenated to specify many subranges
     * (i.e 600,3000-3001).
     *
     * @param mask the bit mask to be converted
     * @return a range string representing the given bit mask
     */
    public static String maskToRangeString(byte[] mask){
        int start = -2;//far away from 0
        String range = "";
        // a check to see whether the mask is all 1s
        boolean allowsAny = true;

        for(int i = 0; i < mask.length; i++){
            for(int j = 0; j < 8; j++){
                int tag = i*8 + j;
                if((mask[i] & (int)Math.pow(2, (7-j))) > 0){
                    if(start == -2){
                        start = tag;
                    }
                }else if(start != -2){
                    allowsAny = false;
                    if(!range.equals("")){
                        range += ",";
                    }
                    range += start;
                    if(start != (tag -1)){
                        range += "-" + (tag-1);
                    }
                    start = -2;
                }
            }
        }
        // if we never hit the allowsAny=false bit that means that
        // either the mask is all ones or all zeros
        // if start != -2 it's all ones, so return 0-4096
        if (allowsAny && start != -2) {
            range = "0-4096";
        }

        return range;
    }

}
