package net.es.oscars.bss.policy;


import java.util.List;
import java.util.Properties;

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
            availIngVlans = combineAvailAndReserved(availIngVlans, ingPE, activeReservations);
            availIngVlans = combineRemote(availIngVlans, ingPE, prevEgrPE);
            log.debug("Available ingress VLANs: "+ availIngVlans);
            
            VlanRange availEgrVlans = combineTopoAndReq(egrPE);
            availEgrVlans = combineAvailAndReserved(availEgrVlans, egrPE, activeReservations);
            availEgrVlans = combineRemote(availEgrVlans, egrPE, nextIngPE);
            log.debug("Available egress VLANs: "+ availEgrVlans);
            
            
            this.decideAndSetVlans(prevEgrPE, ingPE, egrPE, nextIngPE, availIngVlans, availEgrVlans);
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
        
        log.debug("Common VLANs for ingress and egress are: ["+localCommonVlans.toString()+"]");
        
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
        
        if (!localCommonVlans.isEmpty()) {
            log.debug("A common single VLAN exists, deciding..");
            singleVlan = decideVlan(localCommonVlans, suggested);
            log.debug("Common single VLAN is: "+singleVlan);
            this.finalizeVlan(singleVlan, ingPE, prevEgrPE);
            this.finalizeVlan(singleVlan, egrPE, nextIngPE);
        } else {
            log.debug("No common VLANs for edges, deciding ingress...");
            Integer ingVlan = decideVlan(availIngVlans, suggested);
            if (ingVlan == null) {
                throw new BSSException("Could not decide an VLAN for ingress edge!");
            }
            log.debug("Decided inggress VLAN: "+ingVlan+" , deciding egress...");
            Integer egrVlan = decideVlan(availEgrVlans, suggested);
            if (egrVlan == null) {
                throw new BSSException("Could not decide a VLAN for egress edge!");
            }
            log.debug("Decided egress VLAN: "+egrVlan+ " , finalizing VLANs..");
            this.finalizeVlan(ingVlan, ingPE, prevEgrPE);
            this.finalizeVlan(egrVlan, egrPE, nextIngPE);
        }
        log.debug("decideAndSetVlans.end");
    }
    
    private void finalizeVlan(Integer vlanId, PathElem edgePE, PathElem remoteEdgePE) throws BSSException {
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
            edgePE.getPathElemParams().add(pep);
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
                remoteEdgePE.getPathElemParams().add(pep);
            }
        }
    }
    
    private Integer decideVlan(VlanRange availVlans, VlanRange suggestedVlans) {
                
        log.debug("decideVlan.start avail: ["+availVlans+"] sugg: ["+suggestedVlans+"]");
        VlanRange tmp = VlanRange.copy(availVlans);
        if (!suggestedVlans.isEmpty()) {
            tmp = VlanRange.and(tmp, suggestedVlans);
        }
        
        int first = tmp.getFirst();
        if (first == -1) {
            log.error("Could not decide on a VLAN");
            return null;
        } else {
            log.debug("Decided vlan: "+first);
            return first;
        }
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
        
    
    
    
    
    private VlanRange combineAvailAndReserved(VlanRange availVlans, PathElem edgePE, List<Reservation> resvs) throws BSSException {
        for (Reservation resv : resvs) {
            String gri = resv.getGlobalReservationId();
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
