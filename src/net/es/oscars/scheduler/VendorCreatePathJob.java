package net.es.oscars.scheduler;
import net.es.oscars.bss.*;
import net.es.oscars.bss.topology.Path;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.pss.vendor.cisco.LSP;
import net.es.oscars.pss.vendor.jnx.JnxLSP;
import net.es.oscars.pss.*;
import net.es.oscars.oscars.*;
import net.es.oscars.notify.*;

import org.apache.log4j.Logger;
import org.hibernate.*;
import org.quartz.*;
import java.util.*;

public class VendorCreatePathJob extends ChainingJob  implements Job {
    private Logger log;

    public void execute(JobExecutionContext context) throws JobExecutionException {
        this.log = Logger.getLogger(this.getClass());
        String jobName = context.getJobDetail().getFullName();
        this.log.debug("VendorCreatePathJob.start name: "+jobName);

        OSCARSCore core = OSCARSCore.getInstance();
        String bssDbName = core.getBssDbName();
        // Need to get our own Hibernate session since this is a new thread
        Session bss = core.getBssSession();
        bss.beginTransaction();

        // prepare common objects
        ReservationDAO resvDAO = new ReservationDAO(bssDbName);
        EventProducer eventProducer = new EventProducer();
        String status;
        StateEngine stateEngine = core.getStateEngine();

        // Get reservation info from DB
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String direction = (String) dataMap.get("direction");
        String ingressRouterType = (String) dataMap.get("ingressRouterType");
        String egressRouterType = (String) dataMap.get("egressRouterType");
        this.log.debug("ingress router type: " + ingressRouterType);
        this.log.debug("egress router type: " + egressRouterType);
        String gri = (String) dataMap.get("gri");
        Reservation resv = null;
        try {
            resv = resvDAO.query(gri);
        } catch (BSSException ex) {
            this.log.error("Could not locate reservation in DB for gri: "+gri);
            this.runNextJob(context);
            return;
        }

        // Prepare LSP data from path
        LSPData lspData = new LSPData(bssDbName);
        Path path = resv.getPath("intra");
        try {
            lspData.setPathVars(path.getPathElems());
        } catch (PSSException ex) {
            this.log.error(ex);
            try {
                status = stateEngine.updateStatus(resv, StateEngine.FAILED);
            } catch (BSSException bssex) {
                this.log.error("State engine error", bssex);
                eventProducer.addEvent(OSCARSEvent.PATH_SETUP_FAILED, "", "JOB", resv, "", bssex.getMessage()+"\n"+ex.getMessage());
            }
            eventProducer.addEvent(OSCARSEvent.PATH_SETUP_FAILED, "", "JOB", resv, "", ex.getMessage());
            this.runNextJob(context);
            return;
        }

        // If something has failed up to here, go to the next job
        try {
            StateEngine.canUpdateStatus(resv, StateEngine.ACTIVE);
        } catch (BSSException ex) {
            this.log.error(ex);
            this.runNextJob(context);
            return;
        }

        // Try and set up the path
        String errString = "";
        boolean pathWasSetup = true;
        LSP ciscoLSP = null;
        JnxLSP jnxLSP = null;
        if (ingressRouterType.equals("jnx")) {
            jnxLSP = new JnxLSP(bssDbName);
            try {
                jnxLSP.createPath(resv, lspData, direction);
            } catch (PSSException ex) {
                this.log.error("Could not set up path!", ex);
                errString = ex.getMessage();
                pathWasSetup = false;
            }
        } else if (ingressRouterType.equals("cisco")) {
            ciscoLSP = new LSP(bssDbName);
            try {
                ciscoLSP.createPath(resv, lspData, direction);
            } catch (PSSException ex) {
                this.log.error("Could not set up path!", ex);
                errString = ex.getMessage();
                pathWasSetup = false;
            }
        }

        // All done with configuring routers, now update resv status
        try {
            status = StateEngine.getStatus(resv);
            // path was not set up, fail.
            if (!pathWasSetup) {
                status = stateEngine.updateStatus(resv, StateEngine.FAILED);
                eventProducer.addEvent(OSCARSEvent.PATH_SETUP_FAILED, "", "JOB", resv, "", errString);
            } else {
                String syncedStatus = VendorStatusSemaphore.syncSetupCheck(gri, "PATH_SETUP", direction);
                // make sure both path setup operations have completed
                if (syncedStatus.equals("PATH_SETUP_BOTH")) {
                    ArrayList<String> directions = new ArrayList<String>();
                    directions.add("forward");
                    directions.add("reverse");
                    // add the reservation to the status checklist
                    for (String dir : directions) {
                        HashMap<String, String> params = new HashMap<String, String>();
                        params.put("desiredStatus", StateEngine.ACTIVE);
                        params.put("operation", "PATH_SETUP");
                        params.put("description", resv.getDescription());
                        if (dir.equals("forward")) {
                            this.log.debug("setting forward status check params");
                            params.put("ingressNodeId", lspData.getIngressLink().getPort().getNode().getTopologyIdent());
                            params.put("ingressVlan", lspData.getVlanTag());
                            // depends on which finished first
                            if (direction.equals("forward")) {
                                params.put("ingressVendor", ingressRouterType);
                            } else {
                                this.log.info("switching ingress vendor to " + egressRouterType);
                                params.put("ingressVendor", egressRouterType);
                            }
                        } else if (dir.equals("reverse")) {
                            this.log.debug("setting reverse status check params");
                            params.put("egressNodeId", lspData.getEgressLink().getPort().getNode().getTopologyIdent());
                            params.put("egressVlan", lspData.getVlanTag());
                            if (direction.equals("forward")) {
                                params.put("egressVendor", egressRouterType);
                            } else {
                                this.log.info("switching egress vendor to " + ingressRouterType);
                                params.put("egressVendor", ingressRouterType);
                            }
                        }
                        VendorMaintainStatusJob.addToCheckList(gri, params);
                    }
                }
            }
        } catch (BSSException ex) {
            this.log.error("State engine error", ex);
            eventProducer.addEvent(OSCARSEvent.PATH_SETUP_FAILED, "", "JOB", resv, "", ex.getMessage());
        }
        bss.getTransaction().commit();
        this.runNextJob(context);
        this.log.debug("VendorCreatePathJob.end name: "+jobName);
    }
}
