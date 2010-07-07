package net.es.oscars.bss;

import java.util.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import net.es.oscars.lookup.*;
import net.es.oscars.bss.topology.*;

/**
 * Class that performs server side validation for reservation parameters.
 */
public class ParamValidator {
    private Logger log;

    public StringBuilder validate(Reservation resv, Path requestedPath) {
        this.log = Logger.getLogger(this.getClass());

        boolean isLayer2 = false;

        StringBuilder sb = new StringBuilder();
        if (resv == null) {
            return sb.append("Null resv");
        }
        if (resv.getLogin() == null) {
            return sb.append("Null login");
        }
        sb.append(this.checkStartTime(resv));
        sb.append(this.checkEndTime(resv));
        sb.append(this.checkBandwidth(resv));
        sb.append(this.checkLogin(resv));
        sb.append(this.checkStatus(resv));
        sb.append(this.checkDescription(resv));
        if (requestedPath == null) {
            return sb;
        }
        Layer2Data layer2Data = requestedPath.getLayer2Data();
        Layer3Data layer3Data = requestedPath.getLayer3Data();
        try {
            if (requestedPath.isLayer2()) {
                this.log.debug("Validating Layer 2 reservation");
                isLayer2 = true;
                sb.append(this.checkVtag(requestedPath));
                sb.append(this.checkL2Endpoint(layer2Data, false));
                sb.append(this.checkL2Endpoint(layer2Data, true));
            }
            if (requestedPath.isLayer3()) {
                this.log.debug("Validating Layer 3 reservation");
                sb.append(this.checkL3Endpoint(layer3Data, false, requestedPath));
                sb.append(this.checkL3Endpoint(layer3Data, true, requestedPath));
                sb.append(this.checkDscp(layer3Data));
                sb.append(this.checkProtocol(layer3Data));
                sb.append(this.checkSrcIpPort(layer3Data));
                sb.append(this.checkDestIpPort(layer3Data));
            }
        } catch (BSSException ex) {
            return sb.append(ex.getMessage());
        }
        MPLSData mplsData = requestedPath.getMplsData();
        if (mplsData != null) {
            sb.append(this.checkLspClass(mplsData));
        }
        if (!requestedPath.getPathElems().isEmpty()) {
            sb.append(this.checkPath(requestedPath.getPathElems(), isLayer2));
        }
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
     * @param pathElems A path containing an ERO (layer 2 or layer 3),
     *     or an ingress and egress (layer 3)
     * @param isLayer2 true if this is a layer 2 reservation, false otherwise
     */
    private String checkPath(List<PathElem> pathElems, boolean isLayer2){
        // for now, just check length
        if (pathElems.isEmpty()) {
            return "";
        }
        if (pathElems.size() == 1) {
            return("path, if specified, must contain more than one hop");
        }
        if(!isLayer2){
            return "";
        }
        //lookup hops in lookup service if not URNs
        for (PathElem pathElem: pathElems) {
            /* If URN do no further checking */
            String child = pathElem.getUrn();
            // FIXME:  this is not correct
            if (child == null) {
                return "No domain,node,port, or link specified in " +
                       "hop (directly or by reference";
            } else if(child.matches("^urn:ogf:network:.*")) {
                continue;
            }
            /* Lookup name via perfSONAR Lookup Service */
            OSCARSCore core = OSCARSCore.getInstance();
            PSLookupClient lsClient = core.getLookupClient();
            try {
                String urn = lsClient.lookup(child);
                pathElem.setUrn(urn);
            } catch(LookupException e){
                return e.getMessage();
            }
        }

        return "";
    }

    /**
     * @param layer2Data A Layer2Data instance (layer 2 specific)
     */
    private String checkVtag(Path requestedPath) {
        try {
            if (requestedPath == null) {
                return "Null requested path";
            }
            if (requestedPath.getPathElems() == null) {
                return "Null requested path elements";
            }
            if (requestedPath.getPathElems().isEmpty()) {
                return "Empty requested path";
            }
            if (requestedPath.getPathElems().size() < 2) {
                return "Requested path too short";
            }

            // Check all give vlan ranges and suggested vlans
            for(PathElem pathElem : requestedPath.getPathElems()){
                String[] vlanTypes = {PathElemParamType.L2SC_VLAN_RANGE, PathElemParamType.L2SC_SUGGESTED_VLAN};
                for(String vlanType : vlanTypes){
                    PathElemParam vtagParam = pathElem.getPathElemParam(PathElemParamSwcap.L2SC, vlanType);
                    if(vtagParam == null){
                        continue;
                    }
                    // vlan tag can be either a single integer, a range of integers, or
                    // "any"
                    String vtag = vtagParam.getValue();
                    if(vtag == null || vtag.equals("any")){
                        continue;
                    }
                    String[] vlanFields = vtag.split("[-,]");
                    for (int i=0; i < vlanFields.length; i++) {
                        int field = Integer.parseInt(vlanFields[i].trim());
                        if (((field < 2) && field != 0) || (field > 4094)) {
                            return("vlan" + field + " given for " + pathElem.getUrn() + " is not between 2 and 4094");
                        }
                    }
                }
            }
            return "";
        } catch (BSSException ex) {
            this.log.error(ex);
            return ex.getMessage();
        }
    }

    /**
     * @param layer2Data A Layer2Data instance (layer 2 specific)
     */
    private String checkL2Endpoint(Layer2Data layer2Data, boolean isDest) {
        String urn = null;
        String endpoint = null;

        if(isDest){
            endpoint = layer2Data.getDestEndpoint();
        }else{
            endpoint = layer2Data.getSrcEndpoint();
        }

        if (endpoint == null || endpoint.trim().equals("")) {
            return "Empty L2 endpoint";
        }
        endpoint = endpoint.trim();

        String[] parts = endpoint.split("\\s+");
        if (parts.length != 1) {
            return "Only one endpoint allowed for layer 2";
        }

        /* If URN do no further checking */
        if((endpoint.matches("^urn:ogf:network:.*"))){
            return "";
        }

        /* Lookup name via perfSONAR Lookup Service */
        OSCARSCore core = OSCARSCore.getInstance();
        PSLookupClient lsClient = core.getLookupClient();
        try {
            urn = lsClient.lookup(endpoint);
        } catch(LookupException e){
            return e.getMessage();
        }

        /* Print error if no match in LS */
        if(urn == null){
            return "Unable to lookup endpoint " + endpoint + ". Please " +
                "specify a valid URN or a name registered in a " +
                "perfSONAR Lookup Service";
        }

        if(isDest){
            layer2Data.setDestEndpoint(urn);
        }else{
            layer2Data.setSrcEndpoint(urn);
        }
        return "";
    }

    /**
     * @param endpoint A Layer3Data source or destination host specification
     */
    private String checkL3Endpoint(Layer3Data layer3Data, boolean isDest, Path requestedPath) {
        String endpoint = "";

        if (isDest) {
            endpoint = layer3Data.getDestHost();
        } else {
            endpoint = layer3Data.getSrcHost();
        }

        if (endpoint == null || endpoint.trim().equals("")) {
            return("empty endpoint");
        }
        // remove non-significant whitespace
        endpoint = endpoint.trim();
        // replace any commas with whitespace
        endpoint = endpoint.replace(",", " ");
        // break it down into whitespace-separated parts
        String[] hosts = endpoint.split("\\s+");

        // if we have more than one host spec we MUST have been provided an explicit path
        if (hosts.length > 1 ) {
            if (requestedPath.getPathElems().size() < 2) {
                return "no explicit path provided for multi-endpoint L3 request";
            }
        }

        String validatedEndpoint = "";
        for (String host : hosts) {
            String validatedAddress = "";
            String validatedMask = "32";

            String strAddress = host;

            // first check if it's a CIDR style address
            if (host.contains("/")) {
                // check CIDR notation
                String[] parts = host.split("\\/");
                if (parts.length != 2) {
                    return "Invalid CIDR address: "+host;
                }
                strAddress = parts[0];
                String strMask = parts[1];
                try {
                    Integer mask = Integer.valueOf(strMask);
                    if (mask < 8 | mask > 32) {
                        return "mask must be between 8 and 32";
                    } else {
                        validatedMask = mask.toString();
                    }
                } catch (NumberFormatException e) {
                    return "Invalid mask format for CIDR address: "+host;
                }
            }

            InetAddress address = null;
            try {
                // check for validity
                address = InetAddress.getByName(strAddress);
            } catch (UnknownHostException ex) {
                return "Host " + strAddress + " does not exist";
            }
            validatedAddress = address.getHostAddress();
            validatedEndpoint += validatedAddress+"/"+validatedMask+"\n";
        }
        if (isDest) {
            layer3Data.setDestHost(endpoint);
        } else {
            layer3Data.setSrcHost(endpoint);
        }

        return "";
    }


    /**
     * @param layer3Data A Layer3Data instance (layer 3 specific)
     */
    private String checkSrcIpPort(Layer3Data layer3Data) {

        Integer port = layer3Data.getSrcIpPort();
        // depends on client explicitly setting src port to 0
        if (port == 0) { return ""; }
        if (port == null) { return ""; }
        if ((port < 1024) || (port > 65535)) {
            return("Illegal source port specified: " + port + ".  \n");
        }
        return "";
    }

    /**
     * @param layer3Data A Layer3Data instance (layer 3 specific)
     */
    private String checkDestIpPort(Layer3Data layer3Data) {

        Integer port = layer3Data.getDestIpPort();
        if (port == 0) { return ""; }
        if (port == null) { return ""; }
        if ((port < 1024) || (port > 65535)) {
            return ("Illegal destination port specified: " + port + ".  \n");
        }
        return "";
    }

    /**
     * @param layer3Data A Layer3Data instance (layer 3 specific)
     */
    private String checkDscp(Layer3Data layer3Data) {

        String dscp = layer3Data.getDscp();
        if (dscp == null) { return ""; }
        Integer intDscp = Integer.parseInt(dscp);
        if ((intDscp < 0) || (intDscp > 63)) {
            return ("Illegal DSCP specified: " + dscp + ".  \n");
        }
        return "";
    }

    /**
     * @param layer3Data A Layer3Data instance (layer 3 specific)
     */
    private String checkProtocol(Layer3Data layer3Data) {

        String protocol = layer3Data.getProtocol();
        if ((protocol == null) || protocol.equals("")) { return ""; }

        protocol = protocol.toLowerCase();
        if (protocol.equals("udp") || protocol.equals("tcp")) {
            // ensure that what ends up in the db is valid
            // lowercase protocol
            layer3Data.setProtocol(protocol);
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
     * @param mplsData an MPLSData instance containing MPLS-specific data
     */
    private String checkLspClass(MPLSData mplsData) {

        String defaultLspClass = "4";
        if (mplsData.getLspClass() == null) {
            mplsData.setLspClass(defaultLspClass);
        }
        return "";
    }

}
