package net.es.oscars.scheduler;
import net.es.oscars.bss.*;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.pss.cisco.LSP;
import net.es.oscars.pss.jnx.JnxLSP;
import net.es.oscars.pss.*;
import net.es.oscars.oscars.*;

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

        this.log.debug("vendorCreatePath thread: "+Thread.currentThread().getName());

        OSCARSCore core = OSCARSCore.getInstance();
        String bssDbName = core.getBssDbName();

        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        Reservation resv = (Reservation) dataMap.get("reservation");

        String gri;
        if (resv != null) {
            gri = (String) resv.getGlobalReservationId();
            this.log.debug("GRI is: "+gri+ " for job name: "+jobName);
        } else {
            this.log.error("No reservation!");
            return;
        }

        // Need to get our own Hibernate session since this is a new thread
        Session bss = core.getBssSession();
        bss.beginTransaction();

        String status;
        StateEngine stateEngine = new StateEngine();
        try {
            status = StateEngine.getStatus(resv);
            this.log.debug("Reservation status was: "+status);
            status = stateEngine.updateStatus(resv, StateEngine.ACTIVE);
            this.log.debug("Reservation status now is: "+status);
        } catch (BSSException ex) {
            this.log.error("State engine error", ex);
        }



        LSPData lspData = (LSPData) dataMap.get("lspData");
        String direction = (String) dataMap.get("direction");
        String routerType = (String) dataMap.get("routerType");

        LSP ciscoLSP = null;
        JnxLSP jnxLSP = null;
        if (routerType.equals("jnx")) {
            jnxLSP = new JnxLSP(bssDbName);
            try {
                jnxLSP.createPath(resv, lspData, direction);
            } catch (PSSException ex) {
                this.log.error("Could not set up path!", ex);
            }
        } else if (routerType.equals("cisco")) {
            ciscoLSP = new LSP(bssDbName);
            try {
                ciscoLSP.createPath(resv, lspData, direction);
            } catch (PSSException ex) {
                this.log.error("Could not set up path!", ex);
            }
        }


        super.execute(context);
        try {
            Thread.sleep(10);
        } catch (Exception ex) {
        }



        bss.getTransaction().commit();

        this.log.debug("VendorCreatePathJob.end name: "+jobName);

    }

}
