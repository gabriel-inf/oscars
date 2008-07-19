package net.es.oscars.scheduler;
import net.es.oscars.bss.*;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.pss.cisco.LSP;
import net.es.oscars.pss.jnx.JnxLSP;
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


        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        Reservation resv = (Reservation) dataMap.get("reservation");
        LSPData lspData = (LSPData) dataMap.get("lspData");
        String direction = (String) dataMap.get("direction");
        String routerType = (String) dataMap.get("routerType");


        String gri;
        if (resv != null) {
            gri = (String) resv.getGlobalReservationId();
            this.log.debug("GRI is: "+gri+ " for job name: "+jobName);
        } else {
            this.log.error("No reservation!");
            this.runNextJob(context);
            return;
        }

        try {
            StateEngine.canUpdateStatus(resv, StateEngine.ACTIVE);
        } catch (BSSException ex) {
            this.log.error(ex);
            this.runNextJob(context);
            return;
        }


        String errString = "";
        boolean pathWasSetup = true;
        LSP ciscoLSP = null;
        JnxLSP jnxLSP = null;
        if (routerType.equals("jnx")) {
            jnxLSP = new JnxLSP(bssDbName);
            try {
                jnxLSP.createPath(resv, lspData, direction);
            } catch (PSSException ex) {
                this.log.error("Could not set up path!", ex);
                errString = ex.getMessage();
                pathWasSetup = false;
            }
        } else if (routerType.equals("cisco")) {
            ciscoLSP = new LSP(bssDbName);
            try {
                ciscoLSP.createPath(resv, lspData, direction);
            } catch (PSSException ex) {
                this.log.error("Could not set up path!", ex);
                errString = ex.getMessage();
                pathWasSetup = false;
            }
        }

        EventProducer eventProducer = new EventProducer();
        String status;
        StateEngine stateEngine = new StateEngine();
        try {
            status = StateEngine.getStatus(resv);
            this.log.debug("Reservation status was: "+status);
            if (!pathWasSetup) {
                status = stateEngine.updateStatus(resv, StateEngine.FAILED);
                eventProducer.addEvent(OSCARSEvent.PATH_SETUP_FAILED, "", "JOB", resv, "", errString);
            } else {
                try {
                    Scheduler sched = core.getScheduleManager().getScheduler();
                    JobDetail jd = sched.getJobDetail("MaintainStatus", "STATUS");
                    HashMap<String, HashMap<String, String>> checklist = (HashMap<String, HashMap<String, String>>) jd.getJobDataMap().get("checklist");
                    HashMap<String, String> properties = checklist.get(gri);
                    if (properties == null) {
                        properties = new HashMap<String, String>();
                        checklist.put(gri, properties);
                        jd.getJobDataMap().put("checklist", checklist);
                    }
                    properties.put("desiredStatus", StateEngine.ACTIVE);
                    properties.put("operation", "PATH_SETUP");
                    if (direction.equals("forward")) {
                        properties.put("ingressNodeId", lspData.getIngressLink().getPort().getNode().getTopologyIdent());
                        properties.put("ingressVlan", lspData.getVlanTag());
                        properties.put("ingressVendor", routerType);
                    } else if (direction.equals("reverse")) {
                        properties.put("egressNodeId", lspData.getEgressLink().getPort().getNode().getTopologyIdent());
                        properties.put("egressVlan", lspData.getVlanTag());
                        properties.put("egressVendor", routerType);
                    }
                } catch (SchedulerException ex) {
                    this.log.error(ex);
                }
            }
            this.log.debug("Reservation status now is: "+status);
        } catch (BSSException ex) {
            this.log.error("State engine error", ex);
            eventProducer.addEvent(OSCARSEvent.PATH_SETUP_FAILED, "", "JOB", resv, "", ex.getMessage());
        }

        bss.getTransaction().commit();

        this.runNextJob(context);

        this.log.debug("VendorCreatePathJob.end name: "+jobName);

    }

}
