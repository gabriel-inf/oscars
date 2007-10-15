package net.es.oscars.pss;

import java.io.*;

import org.apache.log4j.*;

import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.topology.*;
import net.es.oscars.pss.jnx.JnxLSP;
import net.es.oscars.pss.cisco.LSP;

/**
 * This class decides whether to configure Juniper or Cisco routers,
 * based on an SNMP query.
 *
 * @author David Robertson
 */
public class PSSChooser implements PSS {

    private Logger log;
    private String dbname;

    public PSSChooser(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.dbname = dbname;
    }

    /**
     * Chooses which sort of configuration for circuit setup
     *
     * @param resv a reservation instance
     * @throws PSSException
     */
    public String createPath(Reservation resv) throws PSSException {
        String sysDescr = null;

        Link ingressLink = this.getIngress(resv);
        try {
        	
            // String nodeName = ingressLink.getPort().getNode().getTopologyIdent();
            Port ingressPort = ingressLink.getPort();
            if (ingressPort == null) {
            	throw new PSSException("Could not find port for ingress link");
            }
            
            Node ingressNode = ingressPort.getNode();
            if (ingressNode == null) {
            	throw new PSSException("Could not find node for ingress link");
            }
            NodeAddress ingressNodeAddress = ingressNode.getNodeAddress();
            if (ingressNodeAddress == null) {
            	throw new PSSException("Could not find node address for ingress link");
            }
            String nodeAddress = ingressNodeAddress.getAddress(); 

            this.log.info("Querying router type using SNMP for node address: ["+nodeAddress+"]");

            SNMP snmp = new SNMP();
            snmp.initializeSession(nodeAddress);
            sysDescr = snmp.queryRouterType();
            snmp.closeSession();
            this.log.info("Got sysdescr: ["+sysDescr+"]");

        } catch (IOException e) {
        	this.log.error("Error querying router type using SNMP: ["+e.getMessage()+"]");
            throw new PSSException(e.getMessage());
        }
        if (sysDescr.contains("Juniper")) {
            this.log.info("Creating Juniper-style path");
            JnxLSP jnxLSP = new JnxLSP(this.dbname);
            return jnxLSP.createPath(resv);
        } else if (sysDescr.contains("Cisco")) {
            this.log.info("Creating Cisco-style path");
            LSP lsp = new LSP(this.dbname);
            return lsp.createPath(resv);
        }
        // TODO:  better status
        resv.setStatus("CANCELLED");
        return "CANCELLED";
    }

    /**
     * Verifies LSP is still active: TODO
     *
     * @param resv the reservation whose path will be refreshed
     * @throws PSSException
     */
    public String refreshPath(Reservation resv) throws PSSException {
        return "ACTIVE";
    }

    /**
     * Formats reservation parameters for circuit teardown.
     *
     * @param resv a reservation instance
     * @throws PSSException
     */
    public String teardownPath(Reservation resv) throws PSSException {
        String sysDescr = null;

        Link ingressLink = this.getIngress(resv);
        try {
            SNMP snmp = new SNMP();
            snmp.initializeSession(
                            ingressLink.getPort().getNode().getTopologyIdent());
            sysDescr = snmp.queryRouterType();
            snmp.closeSession();
        } catch (IOException e) {
            throw new PSSException(e.getMessage());
        }
        if (sysDescr.contains("Juniper")) {
            JnxLSP jnxLSP = new JnxLSP(this.dbname);
            return jnxLSP.teardownPath(resv);
        } else if (sysDescr.contains("Cisco")) {
            LSP lsp = new LSP(this.dbname);
            return lsp.teardownPath(resv);
        }
        // TODO:  better status
        resv.setStatus("CANCELLED");
        return "CANCELLED";
    }

    private Link getIngress(Reservation resv) throws PSSException {
        Link ingressLink = null;

        Path path = resv.getPath();
        if (path == null) {
            throw new PSSException("Could not find path!");
        }
        PathElem pathElem = path.getPathElem();
        if (pathElem == null) {
            throw new PSSException("Could not find first path element!");
        }
        // find ingress IP
        while (pathElem != null) {
            if (pathElem.getDescription() != null) {
                if (pathElem.getDescription().equals("ingress")) {
                    ingressLink = pathElem.getLink();
                    break;
                }
            }
            pathElem = pathElem.getNextElem();
        }
        if (ingressLink == null) {
        	this.log.error("Could not find ingress link!");
            throw new PSSException("Could not find ingress link!");
        }
        return ingressLink;
    }
}
