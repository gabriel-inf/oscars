package net.es.oscars.bss;

import java.util.Map;

import net.es.oscars.bss.topology.Interface;

/**
 * Class that performs server side validation for reservation parameters.
 */
public class ParamValidator {

    public StringBuilder validate(Reservation resv, String ingressRouterIP,
                                  String egressRouterIP) {
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
        sb.append(this.checkIngress(resv.getLogin(), ingressRouterIP));
        sb.append(this.checkEgress(resv.getLogin(), egressRouterIP));
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
        } else if ((bandwidth < 1000000L) || (bandwidth > 10000000000L)) {
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
        // no check for now
        return "";
    }

    /**
     * @param resv A Reservation instance
     */ 
    private String checkDestHost(Reservation resv) {
        // no check for now
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
        String defaultDscp = "4";

        String dscp = resv.getDscp();
        if (dscp == null) {
            resv.setDscp(defaultDscp);
        } else {
            Integer intDscp = Integer.parseInt(dscp);
            if ((intDscp < 0) || (intDscp > 63)) {
                return ("Illegal DSCP specified: " + dscp + ".  \n");
            }
        }
        return "";
    }

    /**
     * @param resv A Reservation instance
     */ 
    private String checkProtocol(Reservation resv) {
        String defaultProtocol = "udp";

        String protocol = resv.getProtocol();
        if (protocol == null) {
            resv.setProtocol(defaultProtocol);
        } else {
            //protocol = protocol.toUpperCase();
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
        } else if (description.length() > 80) {
            description = description.substring(0, 79);
            resv.setDescription(description);
            return "";
        }
        return "";
    }

    private String checkIngress(String userName, String ingressRouterIP) {
        if (ingressRouterIP == null) { return ""; }
        if (userName.equals("dtyu@bnl.gov") ||
                userName.equals("wenji@fnal.gov")) {
            if (!ingressRouterIP.equals("chi-sl-sdn1")) {
                return("Only 'chi-sl-sdn1', or a blank value, is permissible in the 'Ingress loopback' field.  \n");
            }
        }
        return "";
    }

    private String checkEgress(String userName, String egressRouterIP) {
        if (egressRouterIP == null) { return ""; }
        if (userName.equals("dtyu@bnl.gov") ||
                userName.equals("wenji@fnal.gov")) {
            if (!egressRouterIP.equals("chi-sl-sdn1")) {
                return("Only 'chi-sl-sdn1', or a blank value, is permissible in the 'Ingress loopback' field.  \n");
            }
        }
        return "";
    }
}
