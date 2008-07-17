package net.es.oscars.scheduler;
import net.es.oscars.bss.*;
import net.es.oscars.pss.cisco.LSP;
import net.es.oscars.pss.jnx.JnxLSP;
import net.es.oscars.pss.*;
import net.es.oscars.oscars.*;
import net.es.oscars.notify.*;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.quartz.*;

public class VendorTeardownPathJob extends ChainingJob  implements Job {
    private Logger log;

    public void execute(JobExecutionContext context) throws JobExecutionException {
        this.log = Logger.getLogger(this.getClass());
        this.log.debug("VendorTeardownPathJob.start name:"+context.getJobDetail().getFullName());

        OSCARSCore core = OSCARSCore.getInstance();

        String bssDbName = core.getBssDbName();

        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        Reservation resv = (Reservation) dataMap.get("reservation");
        LSPData lspData = (LSPData) dataMap.get("lspData");
        String direction = (String) dataMap.get("direction");
        String routerType = (String) dataMap.get("routerType");
        String newStatus = (String) dataMap.get("newStatus");



        String gri;
        if (resv != null) {
            gri = (String) resv.getGlobalReservationId();
            this.log.debug("gri is: "+gri);
        } else {
            this.log.error("No reservation!");
            this.runNextJob(context);
            return;
        }


        try {
            StateEngine.canUpdateStatus(resv, newStatus);
        } catch (BSSException ex) {
            this.log.error(ex);
            this.runNextJob(context);
            return;
        }

        Session bss = core.getBssSession();
        bss.beginTransaction();

        String errString = "";
        boolean pathWasTornDown = true;
        LSP ciscoLSP = null;
        JnxLSP jnxLSP = null;
        if (routerType.equals("jnx")) {
            jnxLSP = new JnxLSP(bssDbName);
            try {
                jnxLSP.teardownPath(resv, lspData, direction);
            } catch (PSSException ex) {
                pathWasTornDown = false;
                this.log.error("Could not set up path", ex);
                errString = ex.getMessage();
            }
        } else if (routerType.equals("cisco")) {
            ciscoLSP = new LSP(bssDbName);
            try {
                ciscoLSP.teardownPath(resv, lspData, direction);
            } catch (PSSException ex) {
                pathWasTornDown = false;
                this.log.error("Could not set up path", ex);
                errString = ex.getMessage();
            }
        }

        EventProducer eventProducer = new EventProducer();
        String status;
        StateEngine stateEngine = new StateEngine();
        try {
            status = StateEngine.getStatus(resv);
            this.log.debug("Reservation status was: "+status);
            if (pathWasTornDown) {
                status = stateEngine.updateStatus(resv, newStatus);
                eventProducer.addEvent(OSCARSEvent.PATH_TEARDOWN_COMPLETED, "", "JOB", resv);
            } else {
                status = stateEngine.updateStatus(resv, StateEngine.FAILED);
                eventProducer.addEvent(OSCARSEvent.PATH_TEARDOWN_FAILED, "", "JOB", resv, "", errString);
            }
            this.log.debug("Reservation status now is: "+status);
        } catch (BSSException ex) {
            this.log.error("State engine error", ex);
            eventProducer.addEvent(OSCARSEvent.PATH_TEARDOWN_FAILED, "", "JOB", resv, "", ex.getMessage());
        }


        bss.getTransaction().commit();

        this.runNextJob(context);
        this.log.debug("VendorTeardownPathJobs.end");

    }

}
