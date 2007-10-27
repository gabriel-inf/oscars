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
     * Chooses which sort of configuration to use for circuit setup, and
     * then attempts to set up the circuit.
     *
     * @param resv a reservation instance
     * @return status string with status of circuit set up
     * @throws PSSException
     */
    public String createPath(Reservation resv) throws PSSException {

        String status = null;

        this.log.info("createPath.start");
        String sysDescr = this.getRouterType(resv);
        if (sysDescr.contains("Juniper")) {
            this.log.info("Creating Juniper-style path");
            JnxLSP jnxLSP = new JnxLSP(this.dbname);
            status = jnxLSP.createPath(resv);
        } else if (sysDescr.contains("Cisco")) {
            this.log.info("Creating Cisco-style path");
            LSP lsp = new LSP(this.dbname);
            status = lsp.createPath(resv);
        } else {
            throw new PSSException(
                "unable to perform circuit set up for router of type " +
                 sysDescr);
        }
        return status;
    }

    /**
     * Chooses which sort of configuration to use for circuit refresh, and
     * then attempts to refresh circuit (see if it is still up).
     *
     * @param resv the reservation whose path will be refreshed
     * @return status string with status of refresh (active or failed)
     * @throws PSSException
     */
    public String refreshPath(Reservation resv) throws PSSException {

        String status = null;

        String sysDescr = this.getRouterType(resv);
        if (sysDescr.contains("Juniper")) {
            JnxLSP jnxLSP = new JnxLSP(this.dbname);
            status = jnxLSP.refreshPath(resv);
        } else if (sysDescr.contains("Cisco")) {
            LSP lsp = new LSP(this.dbname);
            status = lsp.refreshPath(resv);
        } else {
            throw new PSSException(
                "unable to perform circuit refresh for router of type " +
                 sysDescr);
        }
        return status;
    }

    /**
     * Chooses which sort of configuration to use for circuit teardown, and
     * then attempts to tear down the circuit.
     *
     * @param resv a reservation instance
     * @return status string with status of tear down
     * @throws PSSException
     */
    public String teardownPath(Reservation resv) throws PSSException {

        String status = null;

        String sysDescr = this.getRouterType(resv);
        if (sysDescr.contains("Juniper")) {
            JnxLSP jnxLSP = new JnxLSP(this.dbname);
            status = jnxLSP.teardownPath(resv);
        } else if (sysDescr.contains("Cisco")) {
            LSP lsp = new LSP(this.dbname);
            status = lsp.teardownPath(resv);
        } else {
            throw new PSSException(
                "unable to perform circuit teardown for router of type " +
                 sysDescr);
        }
        return status;
    }

    /**
     * Determines whether the initial router is a Juniper or Cisco.
     *
     * @param resv the reservation whose path contains the initial router
     * @param sysDescr string with router type, if successful
     * @throws PSSException
     */
    private String getRouterType(Reservation resv) throws PSSException {

        String sysDescr = null;

        Link ingressLink = this.getIngress(resv);
        try {
            // db enforces not-null
            String nodeAddress =
                ingressLink.getPort().getNode().getNodeAddress().getAddress();
            this.log.info("Querying router type using SNMP for node address: ["+nodeAddress+"]");

            SNMP snmp = new SNMP();
            snmp.initializeSession(nodeAddress);
            sysDescr = snmp.queryRouterType();
            snmp.closeSession();

        } catch (IOException e) {
        	this.log.error("Error querying router type using SNMP: ["+e.getMessage()+"]");
            throw new PSSException(e.getMessage());
        }
        if (sysDescr == null) {
            throw new PSSException("Unable to determine router type");
        }
        this.log.info("Got sysdescr: ["+sysDescr+"]");
        return sysDescr;
    }

    /**
     * Gets ingress link given a reservation with an associated path.
     *
     * @param resv the reservation containing the path
     * @return ingressLink ingress link in path, if any
     * @throws PSSException
     */
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
