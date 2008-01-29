package net.es.oscars.bss;

import java.util.Map;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlanePathContent;
import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlaneHopContent;

import net.es.oscars.wsdlTypes.*;
import net.es.oscars.bss.topology.PSLookupClient;

/**
 * Class that performs server side validation for reservation parameters.
 */
public class ParamValidator {

    public StringBuilder validate(Reservation resv, PathInfo pathInfo) {

        String explicitPath = null;

        StringBuilder sb = new StringBuilder();
        if (resv == null) {
            return sb.append("Null resv");
        }
        if (resv.getLogin() == null) {
            return sb.append("Null login");
        }
        Layer2Info layer2Info = pathInfo.getLayer2Info();
        if (layer2Info != null) {
            sb.append(this.checkVtag(layer2Info));
            sb.append(this.checkL2Endpoint(layer2Info, false));
            sb.append(this.checkL2Endpoint(layer2Info, true));
        }
        Layer3Info layer3Info = pathInfo.getLayer3Info();
        if (layer3Info != null) {
            sb.append(this.checkSrcHost(layer3Info));
            sb.append(this.checkDestHost(layer3Info));
            sb.append(this.checkDscp(layer3Info));
            sb.append(this.checkProtocol(layer3Info));
            sb.append(this.checkSrcIpPort(layer3Info));
            sb.append(this.checkDestIpPort(layer3Info));
        }
        if ((layer2Info == null) && (layer3Info == null)) {
            return sb.append("Null network layer info");
        }
        MplsInfo mplsInfo = pathInfo.getMplsInfo();
        if (mplsInfo != null) {
            sb.append(this.checkLspClass(mplsInfo));
        }
        CtrlPlanePathContent path = pathInfo.getPath();
        if (path != null) {
            sb.append(this.checkPath(path));
        }
        sb.append(this.checkStartTime(resv));
        sb.append(this.checkEndTime(resv));
        sb.append(this.checkBandwidth(resv));
        sb.append(this.checkLogin(resv));
        sb.append(this.checkStatus(resv));
        sb.append(this.checkDescription(resv));
        return sb;
    }

    /**
     * @param resv A Reservation instance
     */ 
    private String checkStartTime(Reservation resv) {

        // no check for now
        return "";
    }

    /**
     * @param resv A Reservation instance
     */ 
    private String checkEndTime(Reservation resv) {

        // no check for now
        return "";
    }

    /**
     * @param resv A Reservation instance
     */ 
    private String checkBandwidth(Reservation resv) {

        Long bandwidth = resv.getBandwidth();
        if (bandwidth == null) {
            return ("No bandwidth specified" + ".  \n");
        }
        if ((bandwidth < 1000000L) || (bandwidth > 10000000000L)) {
            return("Illegal bandwidth specified: " + bandwidth + " Mbps" +
                    ".  \n");
        }
        return "";
    }

    /**
     * @param resv A Reservation instance
     */ 
    private String checkLogin(Reservation resv) {

        // no check for now
        return "";
    }

    /**
     * @param resv A Reservation instance
     */ 
    private String checkStatus(Reservation resv) {

        // no check for now
        return "";
    }

    /**
     * @param resv A Reservation instance
     */ 
    private String checkDescription(Reservation resv) {

        String defaultDescription = "No description provided.";
        String description = resv.getDescription();
        if (description == null) {
            resv.setDescription(defaultDescription);
        }
        if (description.length() > 80) {
            description = description.substring(0, 79);
            resv.setDescription(description);
            return "";
        }
        return "";
    }

    /**
     * @param path An explicit path containing an ERO (layer 2 or layer 3),
     *     or an ingress and egress (layer 3)
     */ 
    private String checkPath(CtrlPlanePathContent path) {
        // for now, just check length
        CtrlPlaneHopContent[] hops = path.getHop();
        if (hops.length == 1) {
            return("path, if specified, must contain more than one hop");
        }
        return "";
    }

    /**
     * @param layer2Info A Layer2Info instance (layer 2 specific)
     */ 
    private String checkVtag(Layer2Info layer2Info) {
        /* Currently only accepts case where either both
         or neither is specified. If specified, they must have the same value.
         In the future these limits will be relaxed as support for VLAN mapping is added. */
        
        VlanTag srcVtag = layer2Info.getSrcVtag();
        VlanTag destVtag = layer2Info.getDestVtag();
        String vtag = null;
        
        if(srcVtag != null && destVtag != null){
            vtag = srcVtag.getString();
            if(!vtag.equals(destVtag.getString())){
                return "source and destination VLAN tag must be same. VLAN" + 
                " mapping not yet supported";
            }
        }else if(srcVtag != destVtag){
            return "You must specify both the source and destination VLAN " +
                    "tag or neither";
            
        }

        // vlan tag can be either a single integer, a range of integers, or 
        // "any"
        if(vtag == null || vtag.equals("any")){
            return "";
        }
        
        String[] vlanFields = vtag.split("[-,]");
        for (int i=0; i < vlanFields.length; i++) {
            int field = Integer.parseInt(vlanFields[i].trim()); 
            if ((field < 2) || (field > 4094)) {
                return("vlan given, " + field + " is not between 1 and 4094");
            }
        }

        return "";
    }

    /**
     * @param layer2Info A Layer2Info instance (layer 2 specific)
     */ 
    private String checkL2Endpoint(Layer2Info layer2Info, boolean isDest) {
        String urn = null;
        String endpoint = null;
        
        if(isDest){
            endpoint = layer2Info.getDestEndpoint();
        }else{
            endpoint = layer2Info.getSrcEndpoint();
        }   
        
        /* If URN do no further checking */
        if((endpoint.matches("^urn:ogf:network:.*"))){
            return "";
        }
        
        /* Lookup name via perfSONAR Lookup Service */
        PSLookupClient lsClient = new PSLookupClient();
        try{
            urn = lsClient.lookup(endpoint);
        }catch(BSSException e){
            return e.getMessage();
        }
        
        /* Print error if no match in LS */
        if(urn == null){
            return "Unable to lookup endpoint " + endpoint + ". Please " +
                "specify a valid URN or a name registered in a " + 
                "perfSONAR Lookup Service";
        }
        
        if(isDest){
            layer2Info.setDestEndpoint(urn);
        }else{
            layer2Info.setSrcEndpoint(urn);
        }
        return "";
    }

    /**
     * @param layer3Info A Layer3Info instance (layer 3 specific)
     */ 
    private String checkSrcHost(Layer3Info layer3Info) {

        // Check to make sure host exists
        InetAddress srcAddress = null;
        try {
            srcAddress = InetAddress.getByName( layer3Info.getSrcHost() );
        } catch (UnknownHostException ex) {
            return "Source host " + layer3Info.getSrcHost() + " does not exist";
        }
        layer3Info.setSrcHost(srcAddress.getHostAddress());
        return "";
    }

    /**
     * @param layer3Info A Layer3Info instance (layer 3 specific)
     */ 
    private String checkDestHost(Layer3Info layer3Info) {

        InetAddress destAddress = null;
        try {
            destAddress = InetAddress.getByName(layer3Info.getDestHost() );
        } catch (UnknownHostException ex) {
            return "Destination host " + layer3Info.getDestHost() +
                   " does not exist";
        }
        layer3Info.setDestHost(destAddress.getHostAddress());
        return "";
    }

    /**
     * @param layer3Info A Layer3Info instance (layer 3 specific)
     */ 
    private String checkSrcIpPort(Layer3Info layer3Info) {

        Integer port = layer3Info.getSrcIpPort();
        // depends on client explicitly setting src port to 0
        if (port == 0) { return ""; }
        if (port == null) { return ""; }
        if ((port < 1024) || (port > 65535)) {
            return("Illegal source port specified: " + port + ".  \n");
        }
        return "";
    }

    /**
     * @param layer3Info A Layer3Info instance (layer 3 specific)
     */ 
    private String checkDestIpPort(Layer3Info layer3Info) {

        Integer port = layer3Info.getDestIpPort();
        if (port == 0) { return ""; }
        if (port == null) { return ""; }
        if ((port < 1024) || (port > 65535)) {
            return ("Illegal destination port specified: " + port + ".  \n");
        }
        return "";
    }

    /**
     * @param layer3Info A Layer3Info instance (layer 3 specific)
     */ 
    private String checkDscp(Layer3Info layer3Info) {

        String dscp = layer3Info.getDscp();
        if (dscp == null) { return ""; }
        Integer intDscp = Integer.parseInt(dscp);
        if ((intDscp < 0) || (intDscp > 63)) {
            return ("Illegal DSCP specified: " + dscp + ".  \n");
        }
        return "";
    }

    /**
     * @param layer3Info A Layer3Info instance (layer 3 specific)
     */ 
    private String checkProtocol(Layer3Info layer3Info) {

        String protocol = layer3Info.getProtocol();
        if ((protocol == null) || protocol.equals("")) { return ""; }

        protocol = protocol.toLowerCase();
        if (protocol.equals("udp") || protocol.equals("tcp")) {
            // ensure that what ends up in the db is valid 
            // lowercase protocol
            layer3Info.setProtocol(protocol);
            return "";
        }
        try {
            Integer intProt = Integer.parseInt(protocol);
            if ((intProt < 0) || (intProt > 255)) {
                return ("Illegal protocol specified: " + protocol + ".  \n");
            }
        } catch (Exception e1) {
            return ("Illegal protocol specified: " + protocol + ".  \n");          	
        }
        return "";
    }

    /**
     * @param mplsInfo an MplsInfo instance containing MPLS-specific data
     */ 
    private String checkLspClass(MplsInfo mplsInfo) {

        String defaultLspClass = "4";
        if (mplsInfo.getLspClass() == null) {
            mplsInfo.setLspClass(defaultLspClass);
        }
        return "";
    }

}