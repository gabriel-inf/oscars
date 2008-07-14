package net.es.oscars.pss;

import java.util.Properties;

import org.apache.log4j.*;
import org.quartz.*;

import net.es.oscars.PropHandler;
import net.es.oscars.bss.*;
import net.es.oscars.bss.topology.*;
import net.es.oscars.oscars.OSCARSCore;
import net.es.oscars.pss.jnx.JnxLSP;
import net.es.oscars.pss.cisco.LSP;
import net.es.oscars.scheduler.*;

/**
 * This class decides whether to configure Juniper or Cisco routers,
 * based on an SNMP query.  It is a factory at the method level, rather
 * than the class level.
 *
 * @author David Robertson
 */
public class VendorPSS implements PSS {

    private Logger log;
    private String dbname;
    private boolean allowLSP;
    private static String staticAllowLSP;
    private OSCARSCore core;

    public VendorPSS(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties commonProps = propHandler.getPropertyGroup("pss", true);
        if (staticAllowLSP != null) {
            this.allowLSP = staticAllowLSP.equals("1") ? true : false;
        } else {
            this.allowLSP =
                commonProps.getProperty("allowLSP").equals("1") ? true : false;
        }
        this.dbname = dbname;
        this.core = OSCARSCore.getInstance();
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
        this.log.info("createPath.start");

        try {
            StateEngine.canUpdateStatus(resv, StateEngine.INSETUP);
        } catch (BSSException ex) {
            throw new PSSException(ex);
        }


        String status;
        LSPData lspData = new LSPData(dbname);

        String forwardRouterType = "";
        String reverseRouterType = "";
        boolean doReverse = false;
        // for Juniper circuit set up
//        JnxLSP jnxLSP = null;
        // for Cisco circuit set up
//        LSP ciscoLSP = null;

        Path path = resv.getPath();
        lspData.setPathVars(path.getPathElem());
        String ingressNodeId = lspData.getIngressLink().getPort().getNode().getTopologyIdent();
        String egressNodeId = lspData.getEgressLink().getPort().getNode().getTopologyIdent();


        this.log.info("Getting forward router type");
        String sysDescr = this.getRouterType(lspData.getIngressLink());

        if (sysDescr.contains("Juniper")) {
            this.log.info("Creating Juniper-style forward path");
            forwardRouterType = "jnx";
        } else if (sysDescr.contains("Cisco")) {
            this.log.info("Creating Cisco-style forward path");
            forwardRouterType = "cisco";
        } else {
            throw new PSSException("Unsupported router type " + sysDescr);
        }

        Layer2Data layer2Data = path.getLayer2Data();
        // reverse path setup needed if layer 2
        if (layer2Data != null) {
            doReverse = true;
            sysDescr = this.getRouterType(lspData.getEgressLink());
            if (sysDescr.contains("Juniper")) {
                this.log.info("Creating Juniper-style reverse path");
                reverseRouterType = "jnx";
            } else if (sysDescr.contains("Cisco")) {
                this.log.info("Creating Cisco-style reverse path");
                reverseRouterType = "cisco";
            } else {
                throw new PSSException("Unsupported router type " + sysDescr);
            }
        }


        StateEngine stateEngine = new StateEngine();
        try {
            status = StateEngine.getStatus(resv);
            this.log.debug("Reservation status was: "+status);
            status = stateEngine.updateStatus(resv, StateEngine.INSETUP);
            this.log.debug("Reservation status now is: "+status);
        } catch (BSSException ex) {
            this.log.error("State engine error", ex);
        }


        try {
            String gri = resv.getGlobalReservationId();
            Scheduler sched = this.core.getScheduleManager().getScheduler();

            String fwdJobName = "createpath-forward-"+forwardRouterType+"-"+gri;
            JobDetail fwdJobDetail = new JobDetail(fwdJobName, "SERIALIZE_NODECONFIG_"+ingressNodeId, VendorCreatePathJob.class);
            this.log.debug("Adding job "+fwdJobName);
            fwdJobDetail.setDurability(true);
            JobDataMap fwdJobDataMap = new JobDataMap();
            fwdJobDataMap.put("reservation", resv);
            fwdJobDataMap.put("lspData", lspData);
            fwdJobDataMap.put("direction", "forward");
            fwdJobDataMap.put("routerType", forwardRouterType);
            fwdJobDetail.setJobDataMap(fwdJobDataMap);
            sched.addJob(fwdJobDetail, false);
            if (doReverse) {
                String rvsJobName = "createpath-reverse-"+reverseRouterType+"-"+gri;
                JobDetail rvsJobDetail = new JobDetail(rvsJobName, "SERIALIZE_NODECONFIG_"+egressNodeId, VendorCreatePathJob.class);
                this.log.debug("Adding job "+rvsJobName);
                rvsJobDetail.setDurability(true);
                JobDataMap rvsJobDataMap = new JobDataMap();
                rvsJobDataMap.put("reservation", resv);
                rvsJobDataMap.put("lspData", lspData);
                rvsJobDataMap.put("direction", "reverse");
                rvsJobDataMap.put("routerType", reverseRouterType);
                rvsJobDetail.setJobDataMap(rvsJobDataMap);
                sched.addJob(rvsJobDetail, false);
            }

        } catch (SchedulerException ex) {
            this.log.error("Scheduler exception", ex);
        }

        status = StateEngine.getStatus(resv);


        this.log.info("create.end");
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
        LSPData lspData = new LSPData(dbname);

        Path path = resv.getPath();
        lspData.setPathVars(path.getPathElem());
        String sysDescr = this.getRouterType(lspData.getIngressLink());
        // when Juniper status works this will be sufficient for layer 2
        // since both directions need to be up
        if (sysDescr.contains("Juniper")) {
            JnxLSP jnxLSP = new JnxLSP(this.dbname);
            status = jnxLSP.refreshPath(resv, lspData);
        } else if (sysDescr.contains("Cisco")) {
            LSP lsp = new LSP(this.dbname);
            status = lsp.refreshPath(resv, lspData);
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
    public String teardownPath(Reservation resv, String newStatus) throws PSSException {
        this.log.info("teardownPath.start, reason: "+newStatus);


        try {
            StateEngine.canUpdateStatus(resv, StateEngine.INTEARDOWN);
        } catch (BSSException ex) {
            throw new PSSException(ex);
        }
        String status;
        
        LSPData lspData = new LSPData(dbname);

        String forwardRouterType = "";
        String reverseRouterType = "";
        boolean doReverse = false;
        // for Juniper circuit set up
        JnxLSP jnxLSP = null;
        // for Cisco circuit set up
        LSP ciscoLSP = null;

        Path path = resv.getPath();
        lspData.setPathVars(path.getPathElem());
        String ingressNodeId = lspData.getIngressLink().getPort().getNode().getTopologyIdent();
        String egressNodeId = lspData.getEgressLink().getPort().getNode().getTopologyIdent();


        this.log.info("Getting forward router type");
        String sysDescr = this.getRouterType(lspData.getIngressLink());

        if (sysDescr.contains("Juniper")) {
            this.log.info("Creating Juniper-style forward path");
            forwardRouterType = "jnx";
        } else if (sysDescr.contains("Cisco")) {
            this.log.info("Creating Cisco-style forward path");
            forwardRouterType = "cisco";
        } else {
            throw new PSSException("Unsupported router type " + sysDescr);
        }

        Layer2Data layer2Data = path.getLayer2Data();
        // reverse path setup needed if layer 2
        if (layer2Data != null) {
            doReverse = true;
            sysDescr = this.getRouterType(lspData.getEgressLink());
            if (sysDescr.contains("Juniper")) {
                this.log.info("Creating Juniper-style reverse path");
                reverseRouterType = "jnx";
            } else if (sysDescr.contains("Cisco")) {
                this.log.info("Creating Cisco-style reverse path");
                reverseRouterType = "cisco";
            } else {
                throw new PSSException("Unsupported router type " + sysDescr);
            }
        }
        
        StateEngine stateEngine = new StateEngine();
        try {
            status = StateEngine.getStatus(resv);
            this.log.debug("Reservation status was: "+status);
            status = stateEngine.updateStatus(resv, StateEngine.INTEARDOWN);
            this.log.debug("Reservation status now is: "+status);
        } catch (BSSException ex) {
            this.log.error("State engine error", ex);
        }

        try {
            String gri = resv.getGlobalReservationId();
            Scheduler sched = this.core.getScheduleManager().getScheduler();

            String fwdJobName = "teardownpath-forward-"+forwardRouterType+"-"+gri;
            JobDetail fwdJobDetail = new JobDetail(fwdJobName, "SERIALIZE_NODECONFIG_"+ingressNodeId, VendorTeardownPathJob.class);
            this.log.debug("Adding job "+fwdJobName);
            fwdJobDetail.setDurability(true);
            JobDataMap fwdJobDataMap = new JobDataMap();
            fwdJobDataMap.put("reservation", resv);
            fwdJobDataMap.put("lspData", lspData);
            fwdJobDataMap.put("direction", "forward");
            fwdJobDataMap.put("routerType", forwardRouterType);
            fwdJobDataMap.put("newStatus", newStatus);
            fwdJobDetail.setJobDataMap(fwdJobDataMap);
            sched.addJob(fwdJobDetail, false);
            if (doReverse) {
                String rvsJobName = "teardownpath-reverse-"+reverseRouterType+"-"+gri;
                JobDetail rvsJobDetail = new JobDetail(rvsJobName, "SERIALIZE_NODECONFIG_"+egressNodeId, VendorTeardownPathJob.class);
                this.log.debug("Adding job "+rvsJobName);
                rvsJobDetail.setDurability(true);
                JobDataMap rvsJobDataMap = new JobDataMap();
                rvsJobDataMap.put("reservation", resv);
                rvsJobDataMap.put("lspData", lspData);
                rvsJobDataMap.put("direction", "reverse");
                rvsJobDataMap.put("routerType", reverseRouterType);
                rvsJobDataMap.put("newStatus", newStatus);
                rvsJobDetail.setJobDataMap(rvsJobDataMap);
                sched.addJob(rvsJobDetail, false);
            }

        } catch (SchedulerException ex) {
            this.log.error("Scheduler exception", ex);
        }

        // TODO: replace this with a status check job
        status = StateEngine.getStatus(resv);
        this.log.info("create.end");
        return status;
    }

    /**
     * Allows overriding the allowLSP property.  Primarily for tests.
     * Must be called before this class is instantiated.
     * @param overrideAllowLSP "0" or "1" indicating whether LSP can be set up
     */
    public static void setConfigurable(String overrideAllowLSP) {
        staticAllowLSP = overrideAllowLSP;
    }

    /**
     * Determines whether the initial router is a Juniper or Cisco.
     *
     * @param Link link associated with router
     * @param sysDescr string with router type, if successful
     * @throws PSSException
     */
    private String getRouterType(Link link) throws PSSException {

        String sysDescr = null;
        String nodeAddress = link.getPort().getNode().getNodeAddress().getAddress();

        if (link.getPort().getNode().getNodeAddress() == null) {
            this.log.error("getNodeAddress is null for " + link.getPort().getNode().getId());
            throw new PSSException("No node address associated with node; cannot configure.");
        }

        String errorMsg = "";
        int numTries = 5;
        for (int i = 0; i < numTries; i++) {
            this.log.info("Querying router type using SNMP for node address: ["+nodeAddress+"]");
            try {
                SNMP snmp = new SNMP();
                snmp.initializeSession(nodeAddress);
                sysDescr = snmp.queryRouterType();
                snmp.closeSession();
                i = numTries;
            } catch (Exception ex) {
                errorMsg = ex.getMessage();
                if (i < numTries-1) {
                    this.log.error("Error querying router type using SNMP; ["+errorMsg+"], retrying in 5 sec.");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        this.log.error("Thread interrupted, failing");
                        throw new PSSException("Unable to determine router type");
                    }
                } else {
                    this.log.error("Error querying router type using SNMP; ["+errorMsg+"], failing after "+i+" attempts.");
                }
            }
        }

        if (sysDescr == null) {
            throw new PSSException("Unable to determine router type; error was: "+errorMsg);
        }
        this.log.info("Got sysdescr: ["+sysDescr+"]");
        return sysDescr;
    }
}
