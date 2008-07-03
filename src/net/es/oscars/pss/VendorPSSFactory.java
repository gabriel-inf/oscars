package net.es.oscars.pss;

import java.io.*;
import java.util.Properties;
import java.util.List;

import org.apache.log4j.*;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import net.es.oscars.PropHandler;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.topology.*;
import net.es.oscars.oscars.OSCARSCore;
import net.es.oscars.pss.jnx.JnxLSP;
import net.es.oscars.pss.cisco.LSP;
import net.es.oscars.scheduler.CreatePathJob;
import net.es.oscars.scheduler.TeardownPathJob;

/**
 * This class decides whether to configure Juniper or Cisco routers,
 * based on an SNMP query.  It is a factory at the method level, rather
 * than the class level.
 *
 * @author David Robertson
 */
public class VendorPSSFactory implements PSS {

    private Logger log;
    private String dbname;
    private LSPData lspData;
    private boolean allowLSP;
    private static String staticAllowLSP;
    private OSCARSCore core;

    public VendorPSSFactory(String dbname) {
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
        this.lspData = new LSPData(dbname);
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

        try {
            String gri = resv.getGlobalReservationId();
            Scheduler sched = this.core.getScheduleManager().getScheduler();
            String jobName = "pathsetup-"+gri;
            JobDetail jobDetail = new JobDetail(jobName, "SERIALIZE_CREATE", CreatePathJob.class);
            this.log.debug("Adding job "+jobName);
            jobDetail.setDurability(true);
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("reservation", resv);
            jobDetail.setJobDataMap(jobDataMap);
            sched.addJob(jobDetail, false);

        } catch (SchedulerException ex) {
            this.log.error("Scheduler exception", ex);
        }


        // for Juniper circuit set up
        JnxLSP jnxLSP = null;
        // for Cisco circuit set up
        LSP ciscoLSP = null;

        this.log.info("createPath.start");
        Path path = resv.getPath();
        this.lspData.setPathVars(path.getPathElem());
        this.log.info("to getRouterType");
        String sysDescr = this.getRouterType(this.lspData.getIngressLink());
        if (sysDescr.contains("Juniper")) {
            this.log.info("Creating Juniper-style forward path");
            jnxLSP = new JnxLSP(this.dbname);
            jnxLSP.createPath(resv, this.lspData, "forward");
        } else if (sysDescr.contains("Cisco")) {
            this.log.info("Creating Cisco-style forward path");
            ciscoLSP = new LSP(this.dbname);
            ciscoLSP.createPath(resv, this.lspData, "forward");
        } else {
            throw new PSSException(
                "unable to perform forward circuit set up for router of type " +
                 sysDescr);
        }
        Layer2Data layer2Data = path.getLayer2Data();
        // set up reverse path if layer 2
        if (layer2Data != null) {
            sysDescr = this.getRouterType(this.lspData.getEgressLink());
            if (sysDescr.contains("Juniper")) {
                this.log.info("Creating Juniper-style reverse path");
                if (jnxLSP == null) {
                    jnxLSP = new JnxLSP(this.dbname);
                }
                jnxLSP.createPath(resv, this.lspData, "reverse");
            } else if (sysDescr.contains("Cisco")) {
                this.log.info("Creating Cisco-style reverse path");
                if (ciscoLSP == null) {
                    ciscoLSP = new LSP(this.dbname);
                }
                ciscoLSP.createPath(resv, this.lspData, "reverse");
            } else {
                throw new PSSException(
                    "couldn't do reverse circuit set up for router of type " +
                     sysDescr);
            }
        }
        // don't attempt to get circuit status if not configuring
        if (!this.allowLSP) {
            return resv.getStatus();
        }
        boolean active = false;
        // both sides need to be up for a circuit to show up as UP
        // currently only Cisco status gathering works
        if (ciscoLSP != null) {
            active = ciscoLSP.statusLSP();
            if (!active) {
                // try one more time a minute later
                try {
                    Thread.sleep(60000);
                } catch (Exception ex) {
                    throw new PSSException(ex.getMessage());
                }
                active = ciscoLSP.statusLSP();
            }
        } else {
            //active = jnxLSP.statusLSP();
            active = true;
        }
        if (!active) {
            resv.setStatus("FAILED");
            throw new PSSException("circuit setup for " +
                                   resv.getGlobalReservationId() + " failed");
        }
        this.log.info("create.end");
        return resv.getStatus();
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

        Path path = resv.getPath();
        this.lspData.setPathVars(path.getPathElem());
        String sysDescr = this.getRouterType(this.lspData.getIngressLink());
        // when Juniper status works this will be sufficient for layer 2
        // since both directions need to be up
        if (sysDescr.contains("Juniper")) {
            JnxLSP jnxLSP = new JnxLSP(this.dbname);
            status = jnxLSP.refreshPath(resv, this.lspData);
        } else if (sysDescr.contains("Cisco")) {
            LSP lsp = new LSP(this.dbname);
            status = lsp.refreshPath(resv, this.lspData);
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
        try {
            String gri = resv.getGlobalReservationId();
            Scheduler sched = this.core.getScheduleManager().getScheduler();
            String jobName = "teardown-"+gri;
            JobDetail jobDetail = new JobDetail(jobName, "SERIALIZE_TEARDOWN", TeardownPathJob.class);
            this.log.debug("Adding job "+jobName);
            jobDetail.setDurability(true);
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("reservation", resv);
            jobDetail.setJobDataMap(jobDataMap);
            sched.addJob(jobDetail, false);

        } catch (SchedulerException ex) {
            this.log.error("Scheduler exception", ex);
        }


        // for Juniper circuit set up
        JnxLSP jnxLSP = null;
        // for Cisco circuit set up
        LSP ciscoLSP = null;

        Path path = resv.getPath();
        this.lspData.setPathVars(path.getPathElem());
        String sysDescr = this.getRouterType(this.lspData.getIngressLink());
        if (sysDescr.contains("Juniper")) {
            jnxLSP = new JnxLSP(this.dbname);
            jnxLSP.teardownPath(resv, this.lspData, "forward");
        } else if (sysDescr.contains("Cisco")) {
            ciscoLSP = new LSP(this.dbname);
            ciscoLSP.teardownPath(resv, this.lspData, "forward");
        } else {
            throw new PSSException(
                "unable to perform circuit teardown for router of type " +
                 sysDescr);
        }
        Layer2Data layer2Data = path.getLayer2Data();
        // tear down reverse path if layer 2
        if (layer2Data != null) {
            sysDescr = this.getRouterType(this.lspData.getEgressLink());
            if (sysDescr.contains("Juniper")) {
                jnxLSP = new JnxLSP(this.dbname);
                jnxLSP.teardownPath(resv, this.lspData, "reverse");
            } else if (sysDescr.contains("Cisco")) {
                ciscoLSP = new LSP(this.dbname);
                ciscoLSP.teardownPath(resv, this.lspData, "reverse");
            } else {
                throw new PSSException(
                    "unable to perform circuit teardown for router of type " +
                     sysDescr);
            }
        }
        // don't attempt to get circuit status if not configuring
        if (!this.allowLSP) {
            return resv.getStatus();
        }
        boolean active = false;
        // currently only Cisco status gathering works
        if (ciscoLSP != null) {
            active = ciscoLSP.statusLSP();
        } else {
            //active = jnxLSP.statusLSP();
            active = false;
        }
        if (active) {
            resv.setStatus("FAILED");
            throw new PSSException("circuit teardown for " +
                                   resv.getGlobalReservationId() + " failed");
        }
        return resv.getStatus();
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
