package net.es.oscars.scheduler;
import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.StateEngine;
import net.es.oscars.pss.cisco.LSP;
import net.es.oscars.pss.jnx.JnxLSP;
import net.es.oscars.pss.*;
import net.es.oscars.oscars.*;

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
        
        String status;
        StateEngine stateEngine = new StateEngine();

        
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
            }
        } else if (routerType.equals("cisco")) {
            ciscoLSP = new LSP(bssDbName);
            try {
                ciscoLSP.teardownPath(resv, lspData, direction);
            } catch (PSSException ex) {
                pathWasTornDown = false;
                this.log.error("Could not set up path", ex);
            }
        }

        try {
            status = StateEngine.getStatus(resv);
            this.log.debug("Reservation status was: "+status);
            if (pathWasTornDown) {
                status = stateEngine.updateStatus(resv, newStatus);
            } else {
                status = stateEngine.updateStatus(resv, StateEngine.FAILED);
            }
            this.log.debug("Reservation status now is: "+status);
        } catch (BSSException ex) {
            this.log.error("State engine error", ex);
        }


        bss.getTransaction().commit();

        this.runNextJob(context);
        this.log.debug("VendorTeardownPathJobs.end");

    }

}
