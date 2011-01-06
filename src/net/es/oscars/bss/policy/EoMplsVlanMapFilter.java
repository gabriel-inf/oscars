package net.es.oscars.bss.policy;


import java.util.*;

import org.apache.log4j.Logger;

import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.VlanRange;
import net.es.oscars.bss.topology.L2SwitchingCapabilityData;
import net.es.oscars.bss.topology.Link;
import net.es.oscars.bss.topology.Path;
import net.es.oscars.bss.topology.PathElem;
import net.es.oscars.bss.topology.PathElemParam;
import net.es.oscars.bss.topology.PathElemParamSwcap;
import net.es.oscars.bss.topology.PathElemParamType;
import net.es.oscars.bss.topology.PathType;
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
    private String scope;
    
    public static final String NODE_SCOPE = "node";
    public static final String PORT_SCOPE = "port";

    /** Default constructor */
    public EoMplsVlanMapFilter(){
        this.log = Logger.getLogger(this.getClass());
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
        log.debug("applyFilter.start");
        
        Path localPath;
        try {
            localPath = newReservation.getPath(PathType.LOCAL);
            Path interPath = newReservation.getPath(PathType.INTERDOMAIN);
            List<PathElem> localPathElems = localPath.getPathElems();
            List<PathElem> interPathElems = interPath.getPathElems();
            
            PathElem ingPE = localPathElems.get(0);
            PathElem egrPE = localPathElems.get(localPathElems.size() - 1);
            
            PathElem prevEgrPE = this.getPrevExternalL2scHop(interPathElems);
            PathElem nextIngPE = this.getNextExternalL2scHop(interPathElems);
            
            VlanRange availIngVlans = combineTopoAndReq(ingPE);
            availIngVlans = combineAvailAndReserved(newReservation, availIngVlans, ingPE, activeReservations);
            availIngVlans = combineRemote(availIngVlans, ingPE, prevEgrPE);
            log.debug("Available ingress VLANs: "+ availIngVlans);
            
            VlanRange availEgrVlans = combineTopoAndReq(egrPE);
            availEgrVlans = combineAvailAndReserved(newReservation, availEgrVlans, egrPE, activeReservations);
            availEgrVlans = combineRemote(availEgrVlans, egrPE, nextIngPE);
            log.debug("Available egress VLANs: "+ availEgrVlans);
            
            
            this.decideAndSetVlans(prevEgrPE, ingPE, egrPE, nextIngPE, availIngVlans, availEgrVlans);
            
            //copy vlan decision to interdomain path
            for(PathElem interPathElem : interPathElems){
                if(ingPE.getUrn().equals(interPathElem.getUrn())){
                    PathElem.copyPathElemParams(interPathElem, ingPE, PathElemParamSwcap.L2SC);
                }else if(egrPE.getUrn().equals(interPathElem.getUrn())){
                    PathElem.copyPathElemParams(interPathElem, egrPE, PathElemParamSwcap.L2SC);
                    break;
                }
                //prevEgrPE ? nextIngPE ?
            }
        } catch (BSSException e) {
            this.log.error(e);
            throw e;
        }
        log.debug("applyFilter.end");
    }
    
    
    private void decideAndSetVlans(PathElem prevEgrPE, PathElem ingPE, PathElem egrPE, PathElem nextIngPE, 
            VlanRange availIngVlans, VlanRange availEgrVlans) throws BSSException {
        log.debug("decideAndSetVlans.start");
        // find the common subset of vlans between our ingress
        // and egress. just AND the two masks
        VlanRange localCommonVlans = VlanRange.and(availIngVlans, availEgrVlans);
               
        // first: if previous egress was not null that means
        // we're not the first domain. so we probably have received 
        // a suggested VLAN. 
        Integer singleVlan = null;
        VlanRange suggested = new VlanRange();
        if (prevEgrPE != null) {
            String sugVlanString = "";
            PathElemParam pep = prevEgrPE.getPathElemParam(PathElemParamSwcap.L2SC, PathElemParamType.L2SC_SUGGESTED_VLAN);
            if(pep != null){
                sugVlanString = pep.getValue().trim();
            }
            suggested = new VlanRange(sugVlanString);
            if (suggested.isEmpty()) {
                log.warn("Null/empty suggested VLAN for previous domain egress: "+prevEgrPE.getUrn());
            }
        } else {
            log.debug("No previous domain, so no suggested VLANs");
        }
        
        // If the domain can do VLAN translation at both ingress and egress,
        // we should always take advantage of the translation to give next domain bigger range.
        if ((ingPE.getLink().getL2SwitchingCapabilityData() != null
                && ingPE.getLink().getL2SwitchingCapabilityData().getVlanTranslation())
			&& (egrPE.getLink().getL2SwitchingCapabilityData() != null
                && egrPE.getLink().getL2SwitchingCapabilityData().getVlanTranslation())) {
            Integer ingVlan = decideVlan(availIngVlans, suggested);
            if (ingVlan == null) {
                throw new BSSException("Could not decide a VLAN for ingress edge!");
            }
            log.debug("Decided ingress VLANs: "+availIngVlans.toString()+" (suggestedVlan: "+ingVlan+") , deciding egress...");
            Integer egrVlan = decideVlan(availEgrVlans, suggested);
            if (egrVlan == null) {
                throw new BSSException("Could not decide a VLAN for egress edge!");
            }
            log.debug("Decided egress VLANs: "+availEgrVlans.toString()+" (suggestedVlan: "+egrVlan+ " , finalizing VLANs..");
            // If this is last domain, finalize a single VLAN for both ingress and egress
            if (nextIngPE == null) {
                this.finalizeVlan(egrVlan, egrPE, nextIngPE);
                // finalize a single ingess VLAN, even this is not the first domain
                // if there is common vlan avaialble, use same egrVlan for ingress
                if (availIngVlans.getMap()[egrVlan])
                    this.finalizeVlan(egrVlan, ingPE, prevEgrPE, false);
                else { // otherwise, pick ingVlan
                    this.finalizeVlan(ingVlan, ingPE, prevEgrPE, false);
                    // special handling for single domain case
                    // if picked egress vlan cannot be made at ingress, try override ingress and egress with a common vlan
                    if (prevEgrPE == null) {
                        singleVlan = decideVlan(localCommonVlans, suggested);
                        if (singleVlan != null) {
                            this.finalizeVlan(singleVlan, ingPE, prevEgrPE, false);
                            this.finalizeVlan(singleVlan, egrPE, nextIngPE, false);
                        }
						//otherwise, still use the ingVlan that is diff than the egrVlan
                    }
                }
            } else {
                // otherwise, use range for egress interdomain link.
                this.finalizeVlanRange(egrVlan, availEgrVlans, egrPE, nextIngPE);
                // use range for ingress interdomain link, even this is the first domain
                // if there is common vlan avaialble, use same egrVlan for ingress
                if (availIngVlans.getMap()[egrVlan]) 
                    this.finalizeVlanRange(egrVlan, availIngVlans, ingPE, prevEgrPE, false);
                else // otherwise, pick ingVlan
                    this.finalizeVlanRange(ingVlan, availIngVlans, ingPE, prevEgrPE, false);
            }
        } else if (!localCommonVlans.isEmpty()) {
            // If the domain cannot do translation, try find a common/continous VLAN range for ingress and egress.
            log.debug("Common VLANs for ingress and egress are: ["+localCommonVlans.toString()+"]");
            log.debug("A common single VLAN exists, deciding..");
            singleVlan = decideVlan(localCommonVlans, suggested);
            log.debug("Common single VLAN is: "+singleVlan);
            // If this is last domain, finalize a single VLAN 
            if (nextIngPE == null) {
                this.finalizeVlan(singleVlan, ingPE, prevEgrPE);
                // finalize a single VLAN, even this is the first domain
                this.finalizeVlan(singleVlan, egrPE, nextIngPE);
            } else {
                //otherwise, use range for egress interdomain link.
                this.finalizeVlanRange(singleVlan, localCommonVlans, egrPE, nextIngPE);			
                // use range for ingress interdomain link, even this is the first domain
                this.finalizeVlanRange(singleVlan, localCommonVlans, ingPE, prevEgrPE);
            }
        } else { 
            // Otherwise, fail the path.
            log.debug("No common VLANs or translation.");
            throw new BSSException("VLAN(s) not available for local segment!");
        }
        log.debug("decideAndSetVlans.end");
    }
    
    private void finalizeVlan(Integer vlanId, PathElem edgePE, PathElem remoteEdgePE, boolean updateRemoteSuggestedVlan) throws BSSException {
        log.debug("finalizeVlan.start");
        
        if (edgePE == null) {	
            throw new BSSException("Internal error: Local edge path element is null!");
        } 
        PathElemParam pep;
        pep = edgePE.getPathElemParam(PathElemParamSwcap.L2SC, PathElemParamType.L2SC_VLAN_RANGE);
        if (pep != null) {
            pep.setValue(vlanId.toString());
        } else {
            pep = new PathElemParam();
            pep.setSwcap(PathElemParamSwcap.L2SC);
            pep.setType(PathElemParamType.L2SC_VLAN_RANGE);
            pep.setValue(vlanId.toString());
            edgePE.addPathElemParam(pep);
        }
        
        pep = edgePE.getPathElemParam(PathElemParamSwcap.L2SC, PathElemParamType.L2SC_SUGGESTED_VLAN);
        if (pep != null) {
            pep.setValue(vlanId.toString());
        } else {
            pep = new PathElemParam();
            pep.setSwcap(PathElemParamSwcap.L2SC);
            pep.setType(PathElemParamType.L2SC_SUGGESTED_VLAN);
            pep.setValue(vlanId.toString());
            edgePE.addPathElemParam(pep);
        }

        
        if (remoteEdgePE != null) {
            pep = remoteEdgePE.getPathElemParam(PathElemParamSwcap.L2SC, PathElemParamType.L2SC_VLAN_RANGE);
            if (pep != null) {
                pep.setValue(vlanId.toString());
            } else {
                pep = new PathElemParam();
                pep.setSwcap(PathElemParamSwcap.L2SC);
                pep.setType(PathElemParamType.L2SC_VLAN_RANGE);
                pep.setValue(vlanId.toString());
                remoteEdgePE.addPathElemParam(pep);
            }
            if (updateRemoteSuggestedVlan) {
                pep = remoteEdgePE.getPathElemParam(PathElemParamSwcap.L2SC, PathElemParamType.L2SC_SUGGESTED_VLAN);
                if (pep != null) {
                    pep.setValue(vlanId.toString());
                } else { 
                    pep = new PathElemParam();
                    pep.setSwcap(PathElemParamSwcap.L2SC);
                    pep.setType(PathElemParamType.L2SC_SUGGESTED_VLAN);
                    pep.setValue(vlanId.toString());
                    remoteEdgePE.addPathElemParam(pep);
                }
            }
        }   
    }

	
    private void finalizeVlan(Integer vlanId, PathElem edgePE, PathElem remoteEdgePE) throws BSSException {
        finalizeVlan(vlanId, edgePE, remoteEdgePE, true);
    }

	
    private void finalizeVlanRange(Integer suggested, VlanRange vlans, PathElem edgePE, PathElem remoteEdgePE, boolean updateRemoteSuggestedVlan)  throws BSSException {
        log.debug("finalizeVlanRange.start");
        
        if (edgePE == null) {	
            throw new BSSException("Internal error: Local edge path element is null!");
        } 
        PathElemParam pep;
        pep = edgePE.getPathElemParam(PathElemParamSwcap.L2SC, PathElemParamType.L2SC_VLAN_RANGE);
        if (pep != null) {
            pep.setValue(vlans.toString());
        } else {
            pep = new PathElemParam();
            pep.setSwcap(PathElemParamSwcap.L2SC);
            pep.setType(PathElemParamType.L2SC_VLAN_RANGE);
            pep.setValue(vlans.toString());
            edgePE.addPathElemParam(pep);
        }
        
        pep = edgePE.getPathElemParam(PathElemParamSwcap.L2SC, PathElemParamType.L2SC_SUGGESTED_VLAN);
        if (pep != null) {
            pep.setValue(suggested.toString());
        } else {
            pep = new PathElemParam();
            pep.setSwcap(PathElemParamSwcap.L2SC);
            pep.setType(PathElemParamType.L2SC_SUGGESTED_VLAN);
            pep.setValue(suggested.toString());
            edgePE.addPathElemParam(pep);
        }
		
        
        if (remoteEdgePE != null) {
            pep = remoteEdgePE.getPathElemParam(PathElemParamSwcap.L2SC, PathElemParamType.L2SC_VLAN_RANGE);
            if (pep != null) {
                pep.setValue(vlans.toString());
            } else {
                pep = new PathElemParam();
                pep.setSwcap(PathElemParamSwcap.L2SC);
                pep.setType(PathElemParamType.L2SC_VLAN_RANGE);
                pep.setValue(vlans.toString());
                remoteEdgePE.addPathElemParam(pep);
            }
            if (updateRemoteSuggestedVlan) {
                pep = remoteEdgePE.getPathElemParam(PathElemParamSwcap.L2SC, PathElemParamType.L2SC_SUGGESTED_VLAN);
                if (pep != null) {
                    pep.setValue(suggested.toString());
                } else {
                    pep = new PathElemParam();
                    pep.setSwcap(PathElemParamSwcap.L2SC);
                    pep.setType(PathElemParamType.L2SC_SUGGESTED_VLAN);
                    pep.setValue(suggested.toString());
                    remoteEdgePE.addPathElemParam(pep);
                }
            }
        }
    }
	
	
    private void finalizeVlanRange(Integer suggested, VlanRange vlans, PathElem edgePE, PathElem remoteEdgePE)  throws BSSException {
        finalizeVlanRange(suggested, vlans, edgePE, remoteEdgePE, true);
    }

	
    private Integer decideVlan(VlanRange availVlans, VlanRange suggestedVlans) {
                
        log.debug("decideVlan.start avail: ["+availVlans+"] sugg: ["+suggestedVlans+"]");
        VlanRange vlanRange = VlanRange.copy(availVlans);
        VlanRange suggRange = VlanRange.and(vlanRange, suggestedVlans);
        if (!suggRange.isEmpty()) {
            vlanRange = suggRange;
        }

        boolean[] map = vlanRange.getMap();
        ArrayList<Integer> vlanPool = new ArrayList<Integer>();
        for (int i=0; i < map.length; i++) {
            if (map[i])
                vlanPool.add(i);
        }
        int index = 0;
        if (vlanPool.size() > 1) {
            Random rand = new Random();
            index = rand.nextInt(vlanPool.size()-1);
        } else if (vlanPool.size() == 0) {
            log.error("Could not decide on a VLAN");
            return null;
        } 

        log.debug("Picking vlan: "+vlanPool.get(index));
        return vlanPool.get(index);
    }
    
    
    private VlanRange combineRemote(VlanRange availVlans, PathElem edgePE, PathElem remotePE) throws BSSException {
        String inVlanString = availVlans.toString();
        if (remotePE == null) {
            log.debug("No remote PE for edge: "+edgePE.getUrn()+" , result "+availVlans.toString());
            return availVlans;
        }
        PathElemParam remotePEP = remotePE.getPathElemParam(PathElemParamSwcap.L2SC, PathElemParamType.L2SC_VLAN_RANGE);
        if (remotePEP == null) {
            log.debug("No VLAN availability info for remote edge: "+remotePE.getUrn()+" , assuming 'any', result: "+availVlans.toString());
            return availVlans;
        }
        String remoteVlanString = remotePEP.getValue();
        VlanRange remoteVlans = new VlanRange(remoteVlanString);
        availVlans = VlanRange.and(availVlans, remoteVlans);
        
        
        if(availVlans.isEmpty()){
            throw new BSSException("No usable VLANs at edge "+edgePE.getUrn()+" . Remote edge: " +
                                   remotePE.getUrn()+" only accepts: ["+remoteVlanString+"] and we needed ["+inVlanString+"]");
        }

        return availVlans;
    }
        
    
    
    
    
    private VlanRange combineAvailAndReserved(Reservation newReservation, VlanRange availVlans, 
            PathElem edgePE, List<Reservation> resvs) throws BSSException {
		String newGri = newReservation.getGlobalReservationId();
        for (Reservation resv : resvs) {
            String gri = resv.getGlobalReservationId();
			//ignore the 'self' reservation
			if (gri.equals(newGri))
				continue;
            Path localPath = resv.getPath(PathType.LOCAL);
            List<PathElem> localPathElems = localPath.getPathElems();
            PathElem ingPE = localPathElems.get(0);
            PathElem egrPE = localPathElems.get(localPathElems.size() - 1);
            PathElem pe = null;
            if (ingPE == null || ingPE.getLink() == null || ingPE.getLink().getPort() == null) {
                // avoid NPE
                log.debug("incomplete ingress info for "+gri+" "+ingPE.getUrn());
            } else if (egrPE == null || egrPE.getLink() == null || egrPE.getLink().getPort() == null) {
                // avoid NPE
                log.debug("incomplete egress info for "+gri+" "+ingPE.getUrn());
            } else {
                if (scope.equals(PORT_SCOPE)) {
                    if (ingPE.getLink().getPort().equals(edgePE.getLink().getPort())) {
                        pe = ingPE;
                    } else if (egrPE.getLink().getPort().equals(edgePE.getLink().getPort())) {
                        pe = egrPE;
                    } else {
                        continue;
                    }
                } else if (scope.equals(NODE_SCOPE)) {
                    if (ingPE.getLink().getPort().getNode().equals(edgePE.getLink().getPort().getNode())) {
                        pe = ingPE;
                    } else if (egrPE.getLink().getPort().getNode().equals(edgePE.getLink().getPort().getNode())) {
                        pe = egrPE;
                    } else {
                        continue;
                    }
                }
                String vlanString = this.getVlanForOverlappingPE(pe);
                VlanRange resvVlans = new VlanRange(vlanString);
                log.debug("vlan range: ["+resvVlans+"] reserved by "+gri+" at "+pe.getUrn());
                availVlans = VlanRange.subtract(availVlans, resvVlans);
            }
        }
        
        log.debug("usable vlans when all resvs removed: "+availVlans);
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
    
    
    
    
    private VlanRange combineTopoAndReq(PathElem edgePE) throws BSSException {
        
        
        VlanRange availVlans = new VlanRange();
        
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
        VlanRange edgeTopoVlans = new VlanRange(edgeVlanAvail);
        
        // the available VLANs are initially the ones configured on the topology
        availVlans = edgeTopoVlans;
        
        // this is what the user has requested at that edge
        PathElemParam reqVlanRangeParam = edgePE.getPathElemParam(PathElemParamSwcap.L2SC, PathElemParamType.L2SC_VLAN_RANGE);
        String reqVlanString = "";
        if (reqVlanRangeParam != null) {
            reqVlanString = reqVlanRangeParam.getValue().trim();
        }
        log.debug(edgePE.getUrn()+" requested VLANs: ["+reqVlanString+"]");
        VlanRange reqVlans = new VlanRange(reqVlanString);
        
        // if nothing requested, everything on the topology is available
        if (reqVlans.isEmpty()){
            return availVlans;
        }
        
        // disallow untagged interfaces
        // TODO: make this configurable 
        if (reqVlans.getMap()[0]) {
            throw new BSSException("untagged not allowed: "+edgeLink.getFQTI());
        }
        
        
        
        // AND the topology and requested VLANs
        availVlans = VlanRange.and(edgeTopoVlans, reqVlans);
        
        this.log.debug(edgePE.getUrn() + " : available VLANs by req and topo: [" + availVlans+"]");
        if(availVlans.isEmpty()){
            throw new BSSException("None of VLAN(s): [" + reqVlanString + "] are available at edge: " + edgePE.getUrn());
        }
        return availVlans;

        
        
    }


}
