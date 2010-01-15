package net.es.oscars.bss.policy;

import java.util.*;

import org.apache.log4j.Logger;

import net.es.oscars.bss.*;
import net.es.oscars.bss.topology.*;
import net.es.oscars.PropHandler;

/**
 * VLAN policy filter for EoMPLS-like data planes.
 * Here, VLANs only "exist" at the edges of reservations
 * Scoping of VLANs can be per-node or per-port, set by 
 * the "scope" variable in oscars.properties. Default
 * behavior is per-port. 
 *
 */
public class EoMplsVlanMapFilter extends VlanMapFilter implements PolicyFilter{
    private Logger log;
    private OSCARSCore core;
    private String scope;
    
    public static final String NODE_SCOPE = "node";
    public static final String PORT_SCOPE = "port";

    /** Default constructor */
    public EoMplsVlanMapFilter(){
        this.log = Logger.getLogger(this.getClass());
        this.core = OSCARSCore.getInstance();
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("policy.vlanFilter", true);
        this.scope = props.getProperty("scope");
        if (this.scope == null) {
            this.scope = PORT_SCOPE;
        } else {
            this.scope = this.scope.toLowerCase();
        }
        this.log.debug("scope: "+this.scope);
    }

    /**
     * Checks VLANs along a given path 
     *
     * @param newReservation reservation containing the inter-domain and local paths
     * @param activeReservations the list of overlapping reservations (in terms of time)
     * @throws BSSException
     */
    public void applyFilter(Reservation newReservation,
            List<Reservation> activeReservations) throws BSSException {
        Path localPath = newReservation.getPath(PathType.LOCAL);
        Path interPath = newReservation.getPath(PathType.INTERDOMAIN);
        List<PathElem> localPathElems = localPath.getPathElems();
        List<PathElem> interPathElems = interPath.getPathElems();
        
        PathElem ingPE = localPathElems.get(0);
        PathElem egrPE = localPathElems.get(localPathElems.size() - 1);
        
        PathElem prevEgrPE = this.getPrevExternalL2scHop(interPathElems);
        PathElem nextIngPE = this.getNextExternalL2scHop(interPathElems);
        
        byte[] availIngVlans = combineTopoAndReq(ingPE);
        availIngVlans = combineAvailAndReserved(availIngVlans, ingPE, activeReservations);
        availIngVlans = combineRemote(availIngVlans, ingPE, prevEgrPE);
        
        byte[] availEgrVlans = combineTopoAndReq(egrPE);
        availEgrVlans = combineAvailAndReserved(availEgrVlans, egrPE, activeReservations);
        availEgrVlans = combineRemote(availEgrVlans, egrPE, nextIngPE);
        
        
        this.decideAndSetVlans(prevEgrPE, ingPE, egrPE, nextIngPE, availIngVlans, availEgrVlans);
    }
    
    
    
    private void decideAndSetVlans(PathElem prevEgrPE, PathElem ingPE, PathElem egrPE, PathElem nextIngPE, 
                            byte[] availIngVlans, byte[] availEgrVlans) throws BSSException {
        // find the common subset of vlans between our ingress
        // and egress. just AND the two masks
        byte[] localCommonVlans = availIngVlans.clone();
        for (int i = 0; i < availIngVlans.length; i++) {
            localCommonVlans[i] &= availEgrVlans[i];
        }
        
        String localCommonVlanStr = VlanMapFilter.maskToRangeString(localCommonVlans);
        log.debug("Common VLANs for ingress and egress are: ["+localCommonVlanStr+"]");
        
        // first: if previous egress was not null that means
        // we're not the first domain. so we probably have received 
        // a suggested VLAN. 
        Integer singleVlan = null;
        byte[] suggested = null;
        if (prevEgrPE != null) {
            String sugVlan = "";
            PathElemParam pep = prevEgrPE.getPathElemParam(PathElemParamSwcap.L2SC, PathElemParamType.L2SC_SUGGESTED_VLAN);
            if(pep != null){
                sugVlan = pep.getValue().trim();
            }
            if (sugVlan != null && !sugVlan.equals("")) {
                suggested = VlanMapFilter.rangeStringToMask(sugVlan);
            } else {
                log.warn("Null/empty suggested VLAN for edge: "+prevEgrPE.getUrn());
            }
        } else {
            log.debug("No previous domain, so no suggested VLANs");
        }
        
        singleVlan = decideVlan(localCommonVlans, suggested);
        
        
        if (singleVlan == null) {
            this.finalizeVlan(singleVlan, ingPE, prevEgrPE);
            this.finalizeVlan(singleVlan, egrPE, nextIngPE);
        } else {
            Integer ingVlan = decideVlan(availIngVlans, suggested);
            if (ingVlan == null) {
                throw new BSSException("Could not decide an VLAN for ingress edge!");
            } else {
                this.finalizeVlan(ingVlan, ingPE, prevEgrPE);
            }
            Integer egrVlan = decideVlan(availEgrVlans, suggested);
            if (egrVlan == null) {
                throw new BSSException("Could not decide a VLAN for egress edge!");
            } else {
                this.finalizeVlan(egrVlan, egrPE, nextIngPE);
            }
        }
    }
    
    private void finalizeVlan(Integer vlanId, PathElem edgePE, PathElem remoteEdgePE) {
        
    }
    
    private Integer decideVlan(byte[] availVlans, byte[] suggestedVlans) {
        String sugStr = VlanMapFilter.maskToRangeString(suggestedVlans);
        String avStr = VlanMapFilter.maskToRangeString(availVlans);
                
        log.debug("decideVlan.start avail: ["+avStr+"] sugg: ["+sugStr+"]");
        byte[] tmpVlans = availVlans.clone();
        if (suggestedVlans != null) {
            for (int i = 0; i < suggestedVlans.length; i++) {
                tmpVlans[i] &= suggestedVlans[i];
            }
        }
        
        for (int i = 0; i < tmpVlans.length; i++) {
            if (tmpVlans[i] > 0) {
                Integer vlanId = (8*i + (int) tmpVlans[i]);
                log.debug("decideVlan: decided :"+vlanId);
                return vlanId;
            }
        }
        return null;
    }
    
    
    private byte[] combineRemote(byte[] availVlans, PathElem edgePE, PathElem remotePE) throws BSSException {
        String inVlanString = VlanMapFilter.maskToRangeString(availVlans);
        if (remotePE == null) {
            log.debug("No remote PE for edge: "+edgePE.getUrn());
            return availVlans;
        }
        PathElemParam remotePEP = remotePE.getPathElemParam(PathElemParamSwcap.L2SC, PathElemParamType.L2SC_VLAN_RANGE);
        if (remotePEP == null) {
            log.debug("No VLAN availability info for remote edge: "+remotePE.getUrn()+", assuming 'any'");
            return availVlans;
        }
        String remoteVlanString = remotePEP.getValue();
        byte[] remoteVlans = VlanMapFilter.rangeStringToMask(remoteVlanString);
        for(int j = 0; j < availVlans.length; j++){
            availVlans[j] &= remoteVlans[j];
        }
        String vlanString = VlanMapFilter.maskToRangeString(availVlans);
        if("".equals(vlanString)){
            throw new BSSException("No usable VLANs at edge "+edgePE.getUrn()+" . Remote edge: " +
                                   remotePE.getUrn()+" only accepts: ["+remoteVlanString+"] and we needed ["+inVlanString+"]");
        }

        return availVlans;
    }
        
    
    
    
    
    private byte[] combineAvailAndReserved(byte[] availVlans, PathElem edgePE, List<Reservation> resvs) throws BSSException {
        for (Reservation resv : resvs) {
            Path localPath = resv.getPath(PathType.LOCAL);
            List<PathElem> localPathElems = localPath.getPathElems();
            PathElem ingPE = localPathElems.get(0);
            PathElem egrPE = localPathElems.get(localPathElems.size() - 1);
            PathElem pe = null;
            
            if (scope.equals(PORT_SCOPE)) {
                if (ingPE.getLink().equals(edgePE.getLink().getPort())) {
                    pe = ingPE;
                } else if (egrPE.getLink().equals(edgePE.getLink().getPort())) {
                    pe = egrPE;
                } else {
                    continue;
                }
            } else if (scope.equals(NODE_SCOPE)) {
                if (ingPE.getLink().equals(edgePE.getLink().getPort().getNode())) {
                    pe = ingPE;
                } else if (egrPE.getLink().equals(edgePE.getLink().getPort().getNode())) {
                    pe = egrPE;
                } else {
                    continue;
                }
            }
            String vlanString = this.getVlanForOverlappingPE(pe);
            byte[] resvMask = VlanMapFilter.rangeStringToMask(vlanString);
            availVlans = VlanMapFilter.subtractMask(availVlans, resvMask);
        }
        
        return availVlans;
    }
    
    

    
    
    private String getVlanForOverlappingPE(PathElem pe) throws BSSException {
        /* Check VLAN range first. If VLAN not a single integer then the 
         * reservation is likely still getting processed.Try the suggestedVlan
         * instead since that field indicates the value being held for reservations
         * INCREATE.
         */
        PathElemParam pep = pe.getPathElemParam(PathElemParamSwcap.L2SC,
                                      PathElemParamType.L2SC_VLAN_RANGE);
        String vlanString = null;
        try {
            vlanString = pep.getValue();
            Integer.parseInt(vlanString);
        } catch(Exception e){
            pep = pe.getPathElemParam(PathElemParamSwcap.L2SC,
                                          PathElemParamType.L2SC_SUGGESTED_VLAN);
            if(pep != null){
                vlanString = pep.getValue();
            }
        }
        //No VLAN range or suggested VLAN fields. This should not happen
        if (vlanString == null){
            this.log.debug("Skipping PathElem with neither VLAN range nor suggested VLAN");
        }    
        return vlanString;
    }
    
    private byte[] combineTopoAndReq(PathElem edgePE) throws BSSException {
        byte[] availVlans = new byte[512];
        
        Link edgeLink = edgePE.getLink();
        if (edgeLink == null) {
            throw new BSSException("Unknown edge link: "+edgePE.getUrn());
        }
        L2SwitchingCapabilityData edgeL2Cap = edgeLink.getL2SwitchingCapabilityData();
        if (edgeL2Cap == null) {
            throw new BSSException("No L2 capability data at edge: "+edgeLink.getFQTI());
        }
        String edgeVlanAvail = edgeL2Cap.getVlanRangeAvailability();
        if (edgeVlanAvail == null) {
            throw new BSSException("No VLAN range availaibility data at edge: "+edgeLink.getFQTI());
        }
        
        log.debug(edgePE.getUrn()+" topology VLANs: ["+edgeVlanAvail+"]");
        byte[] edgeTopoVlans = VlanMapFilter.rangeStringToMask(edgeVlanAvail);
        
        // the available VLANs are initially the ones configured on the topology
        availVlans = edgeTopoVlans.clone();
        
        // this is what the user has requested at that edge
        PathElemParam reqVlanRangeParam = edgePE.getPathElemParam(PathElemParamSwcap.L2SC, PathElemParamType.L2SC_VLAN_RANGE);
        String reqVlanString = "";
        if (reqVlanRangeParam != null) {
            reqVlanString = reqVlanRangeParam.getValue().trim();
        }
        log.debug(edgePE.getUrn()+" requested VLANs: ["+reqVlanString+"]");
        // if nothing requested, everything on the topology is available
        if (reqVlanString.equals("")){
            return availVlans;
        }
        
        
        
        // disallow untagged interfaces
        // TODO: make this configurable 
        if (reqVlanString.equals("0")) {
            throw new BSSException("untagged not allowed: "+edgeLink.getFQTI());
        }
        
        // AND the topology and requested VLANs
        byte[] reqVlans = VlanMapFilter.rangeStringToMask(reqVlanString);
        for(int j = 0; j < 512; j++){
            availVlans[j] = edgeTopoVlans[j];
            edgeTopoVlans[j] &= reqVlans[j];
        }
        
        // log and return; throw exception if none are available
        String availStr = VlanMapFilter.maskToRangeString(availVlans);
        this.log.debug(edgePE.getUrn() + " : available VLANs by req and topo: [" + availStr+"]");
        if("".equals(availStr)){
            throw new BSSException("None of VLAN(s): [" + reqVlanString + "] are available at edge: " + edgePE.getUrn());
        }
        return availVlans;

        
        
    }


}
