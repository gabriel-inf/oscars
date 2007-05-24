package net.es.oscars.bss;

import java.util.Map;
import java.net.InetAddress;
import java.net.UnknownHostException;

import net.es.oscars.bss.topology.Interface;

/**
 * Class that performs server side validation for reservation parameters.
 */
public class ParamValidator {

    public StringBuilder validate(Reservation resv, String ingressRouter,
                                  String egressRouter) {

        StringBuilder sb = new StringBuilder();
        if (resv == null) 
            return sb.append("Null resv");

        if (resv.getLogin() == null)
            return sb.append("Null login");

        sb.append(this.checkStartTime(resv));
        sb.append(this.checkEndTime(resv));
        sb.append(this.checkBandwidth(resv));
        sb.append(this.checkLogin(resv));
        sb.append(this.checkStatus(resv));
        sb.append(this.checkSrcHost(resv));
        sb.append(this.checkDestHost(resv));
        sb.append(this.checkLspClass(resv));
        sb.append(this.checkDscp(resv));
        sb.append(this.checkProtocol(resv));
        sb.append(this.checkDescription(resv));
        sb.append(this.checkIngress(resv, ingressRouter));
        sb.append(this.checkEgress(resv, egressRouter));
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
        if ((bandwidth < 1000000L) || (bandwidth > 5000000000L)) {
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
    private String checkSrcHost(Reservation resv) {

        // Check to make sure host exists
        InetAddress srcAddress = null;
        try {
            srcAddress = InetAddress.getByName( resv.getSrcHost() );
        } catch (UnknownHostException ex) {
            return "Source host " + resv.getSrcHost() + " does not exist";
        }
        resv.setSrcHost(srcAddress.getHostAddress());
        return "";
    }

    /**
     * @param resv A Reservation instance
     */ 
    private String checkDestHost(Reservation resv) {

        InetAddress destAddress = null;
        try {
            destAddress = InetAddress.getByName( resv.getDestHost() );
        } catch (UnknownHostException ex) {
            return "Destination host " + resv.getDestHost() + " does not exist";
        }
        resv.setDestHost(destAddress.getHostAddress());
        return "";
    }

    /**
     * @param resv A Reservation instance
     */ 
    private String checkSrcPort(Reservation resv) {

        Integer port = resv.getSrcPort();
        if (port == null) { return ""; }
        if ((port < 1024) || (port > 65535)) {
            return("Illegal source port specified: " + port + ".  \n");
        }
        return "";
    }

    /**
     * @param resv A Reservation instance
     */ 
    private String checkDestPort(Reservation resv) {

        Integer port = resv.getDestPort();
        if (port == null) { return ""; }
        if ((port < 1024) || (port > 65535)) {
            return ("Illegal destination port specified: " + port + ".  \n");
        }
        return "";
    }

    /**
     * @param resv A Reservation instance
     */ 
    private String checkLspClass(Reservation resv) {

        String defaultLspClass = "4";
        if (resv.getLspClass() == null) {
            resv.setLspClass(defaultLspClass);
        }
        return "";
    }

    /**
     * @param resv A Reservation instance
     */ 
    private String checkDscp(Reservation resv) {

        String dscp = resv.getDscp();
        if (dscp == null) { return ""; }
        Integer intDscp = Integer.parseInt(dscp);
        if ((intDscp < 0) || (intDscp > 63)) {
            return ("Illegal DSCP specified: " + dscp + ".  \n");
        }
        return "";
    }

    /**
     * @param resv A Reservation instance
     */ 
    private String checkProtocol(Reservation resv) {

        String protocol = resv.getProtocol();
        if ((protocol == null) || protocol.equals("")) { return ""; }

        protocol = protocol.toLowerCase();
        if (protocol.equals("udp") || protocol.equals("tcp")) {
            // ensure that what ends up in the db is valid 
            // lowercase protocol
            resv.setProtocol(protocol);
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

    private String checkIngress(Reservation resv, String ingressRouter) {

        if (ingressRouter == null) { return ""; }
        String userName = resv.getLogin();
        if (userName.equals("dtyu@bnl.gov") ||
                userName.equals("wenji@fnal.gov")) {
            if (!ingressRouter.equals("chi-sl-sdn1")) {
                return("Only 'chi-sl-sdn1', or a blank value, is permissible in the 'Ingress loopback' field.  \n");
            }
        }
        // check to make sure host exists
        InetAddress addr = null;
        try {
            addr = InetAddress.getByName(ingressRouter);
        } catch (UnknownHostException ex) {
            return "Ingress router " + ingressRouter + " does not exist";
        }
        return "";
    }

    private String checkEgress(Reservation resv, String egressRouter) {

        if (egressRouter == null) { return ""; }
        String userName = resv.getLogin();
        if (userName.equals("dtyu@bnl.gov") ||
                userName.equals("wenji@fnal.gov")) {
            if (!egressRouter.equals("chi-sl-sdn1")) {
                return("Only 'chi-sl-sdn1', or a blank value, is permissible in the 'Egress loopback' field.  \n");
            }
        }
        // check to make sure host exists
        InetAddress addr = null;
        try {
            addr = InetAddress.getByName(egressRouter);
        } catch (UnknownHostException ex) {
            return "Egress router " + egressRouter + " does not exist";
        }
        return "";
    }
}
