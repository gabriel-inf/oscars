package net.es.oscars.bss.policy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;

import net.es.oscars.bss.*;
import net.es.oscars.bss.topology.*;
import net.es.oscars.oscars.TypeConverter;
import net.es.oscars.wsdlTypes.*;

public class VlanFilter implements PolicyFilter {

    public static final String domainScope = "DOMAIN";
    public static final String edgeNodeScope = "EDGENODE";
    public static final String linkScope = "LINK";
    private String scope = null;
    private Logger log;

    public VlanFilter() {
        this.log = Logger.getLogger(this.getClass());
    }

    public void applyFilter(PathInfo pathInfo, CtrlPlaneHopContent[] hops,
                            List<Link> localLinks, Reservation newReservation, 
                            List<Reservation> activeReservations)
        throws BSSException {

        if (this.scope == null) {
            this.scope = edgeNodeScope;
            //throw new BSSException("Scope not set.");
        }
        // TODO: add domain-wide VLAN scope
        if (!this.scope.equals(edgeNodeScope)) {
            throw new BSSException("Unsupported scope.");
        } else {
            if (pathInfo == null) {
                throw new BSSException("PathInfo not provided.");
            } else if (localLinks == null) {
                throw new BSSException("Local links not provided.");
            } else if (localLinks.size() < 2) {
                throw new BSSException("Local links are fewer than 2.");
            }
            Layer2Info layer2Info = pathInfo.getLayer2Info();
            Link ingressLink = localLinks.get(0);
            Link egressLink = localLinks.get(localLinks.size() -1);
            Node ingressNode = ingressLink.getPort().getNode();
            Node egressNode = egressLink.getPort().getNode();

            if (layer2Info == null) {
                throw new BSSException("Layer2Info not provided.");
            } else if (ingressLink == null) {
                throw new BSSException("Could not locate ingress link.");
            } else if (egressLink == null) {
                throw new BSSException("Could not locate egress link.");
            }
            this.processEdgeVtags(ingressLink, egressLink, layer2Info);
            for (Reservation resv: activeReservations) {
                if (!resv.getGlobalReservationId().equals(newReservation.getGlobalReservationId())) {
                    this.examineReservation(resv, newReservation.getLogin(), ingressNode, egressNode, layer2Info);
                } 
            }
        }
    }

    private void examineReservation(Reservation resv, String newLogin,
                 Node newIngressNode, Node newEgressNode, Layer2Info layer2Info)
            throws BSSException {

        this.log.debug("Examining VLANs for reservation " +
                       resv.getGlobalReservationId());
        List<PathElem> pathElems = resv.getPath("intra").getPathElems();
        PathElem ingress = pathElems.get(0);
        PathElem egress = pathElems.get(pathElems.size()-1);
        Node ingressNode = ingress.getLink().getPort().getNode();
        Node egressNode = egress.getLink().getPort().getNode();
        String ingressFQTI = ingress.getLink().getFQTI();
        String egressFQTI = egress.getLink().getFQTI();
        String ingressVlan = ingress.getLinkDescr();
        String egressVlan = egress.getLinkDescr();
        this.log.debug("Ingress VLAN: "+ingressVlan + " at " + ingressFQTI);
        this.log.debug("Egress VLAN: "+egressVlan + " at " + egressFQTI);

        String sameUserGRI = null;
        if (resv.getLogin().equals(newLogin)) {
            sameUserGRI = resv.getGlobalReservationId();
        }
        if (ingressVlan != null && !ingressVlan.trim().equals("")) {
            if (ingressNode.equals(newIngressNode)) {
                this.log.debug("removing from ingress VLANs: "+ingressVlan);
                this.removeVlan(ingressVlan, sameUserGRI, layer2Info);
            } else if (ingressNode.equals(newEgressNode)) {
                this.log.debug("removing from egress VLANs: "+ingressVlan);
                this.removeVlan(ingressVlan, sameUserGRI, layer2Info);
            }
        }
        if (egressVlan != null && !egressVlan.trim().equals("")) {
            if (egressNode.equals(newIngressNode)) {
                this.log.debug("removing from ingress VLANs: "+egressVlan);
                this.removeVlan(egressVlan, sameUserGRI, layer2Info);
            } else if (egressNode.equals(newEgressNode)) {
                this.log.debug("removing from egress VLANs: "+egressVlan);
                this.removeVlan(egressVlan, sameUserGRI, layer2Info);
            }
        }
    }

    private void removeVlan(String linkDescr, String sameUserGRI,
                            Layer2Info layer2Info) throws BSSException {
        VlanTag vtag = layer2Info.getSrcVtag();
        String vtagRange = vtag.getString();
        this.log.debug("Ingress vtag: "+vtagRange);
        if (vtagRange == null || vtagRange.equals("")) {
            this.log.error("empty ingress vtagRange");
        }
        byte[] vtagMask = TypeConverter.rangeStringToMask(vtagRange);
        int usedVtag = -1;
        try {
            usedVtag = Integer.parseInt(linkDescr);
        } catch (NumberFormatException ex) {
            this.log.error("Could not parse VLAN tag! String: "+linkDescr);
        }
        if (usedVtag < 0){
            throw new BSSException("No VLAN tags available along path.");
        } else {
            vtagMask[usedVtag/8] &= (byte) ~(1 << (7 - (usedVtag % 8)));
        }


        String newVtagRange = TypeConverter.maskToRangeString(vtagMask); 
        if (newVtagRange.equals("")) {
            if (sameUserGRI != null){
                throw new BSSException("Last VLAN in use by a reservation you previously placed (" + sameUserGRI + ")");
            } else {
                throw new BSSException("VLAN tag unavailable in specified " +
                 "range. If no VLAN range was specified then there are no " +
                 "available vlans along the path.");
            }
        }
        vtag.setString(newVtagRange);
        layer2Info.setSrcVtag(vtag);
        layer2Info.getDestVtag().setString(newVtagRange);
        this.log.debug("new vtagRange: "+newVtagRange);
    }

    private void processEdgeVtags(Link ingressLink, Link egressLink,
                                  Layer2Info layer2Info) throws BSSException {

        L2SwitchingCapabilityData ingressL2scData =
            ingressLink.getL2SwitchingCapabilityData();
        L2SwitchingCapabilityData egressL2scData =
            egressLink.getL2SwitchingCapabilityData();
        if (ingressL2scData == null) {
            throw new BSSException("No L2 switching capability data for ingress link.");
        } else if (egressL2scData == null) {
            throw new BSSException("No L2 switching capability data for egress link.");
        }
        VlanTag srcVtag = layer2Info.getSrcVtag();
        VlanTag destVtag = layer2Info.getDestVtag();
        if (srcVtag == null) {
            srcVtag = new VlanTag();
            srcVtag.setString("2-4094");
            srcVtag.setTagged(true);
            destVtag = new VlanTag();
            srcVtag.setString("2-4094");
            destVtag.setTagged(true);
        } 
        if (srcVtag.getString().equals("any")) {
            srcVtag.setString("2-4094");
            srcVtag.setTagged(true);
        }
        if (destVtag.getString().equals("any")) {
            destVtag.setString("2-4094");
            destVtag.setTagged(true);
        }
        byte[] ingressAvailVtagMask = TypeConverter.rangeStringToMask(ingressL2scData.getVlanRangeAvailability());
        byte[] egressAvailVtagMask = TypeConverter.rangeStringToMask(egressL2scData.getVlanRangeAvailability());

        /* Check if link allows untagged VLAN */
        byte ingressCanBeUntagged = (byte) ((ingressAvailVtagMask[0] & 255) >> 7);
        if((!srcVtag.getTagged()) && ingressCanBeUntagged != 1){
            throw new BSSException("Ingress cannot be untagged");
        }
        /* Check if link allows untagged VLAN */
        byte egressCanBeUntagged = (byte) ((egressAvailVtagMask[0] & 255) >> 7);
        if((!destVtag.getTagged()) && egressCanBeUntagged != 1){
            throw new BSSException("Egress cannot be untagged");
        }
        byte[] vtagMask = TypeConverter.rangeStringToMask(srcVtag.getString());
        for (int i = 0; i < vtagMask.length; i++) {
            vtagMask[i] &= egressAvailVtagMask[i];
        }

        srcVtag.setString(TypeConverter.maskToRangeString(vtagMask));
        destVtag.setString(srcVtag.getString());
        layer2Info.setSrcVtag(srcVtag);
        layer2Info.setDestVtag(destVtag);
        this.log.debug("Source vtag: "+srcVtag.getString());
        this.log.debug("Destination vtag: "+destVtag.getString());

        if (srcVtag.getString().equals("")) {
           throw new BSSException("VLAN not available along the path. " +
                                  "Please try a different VLAN tag.");
        }
    }

    /**
     * @return the scope
     */
    public String getScope() {
        return scope;
    }

    /**
     * @param scope the scope to set
     */
    public void setScope(String scope) {
        this.scope = scope;
    }
}
