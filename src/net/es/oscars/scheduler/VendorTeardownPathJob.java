package net.es.oscars.scheduler;
import net.es.oscars.bss.Reservation;
import net.es.oscars.pss.cisco.LSP;
import net.es.oscars.pss.jnx.JnxLSP;
import net.es.oscars.pss.*;
import net.es.oscars.oscars.*;

import org.apache.log4j.Logger;
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
        String gri;
        if (resv != null) {
            gri = (String) resv.getGlobalReservationId();
            this.log.debug("gri is: "+gri);
        } else {
            this.log.error("No reservation!");
            return;
        }
        LSPData lspData = (LSPData) dataMap.get("lspData");
        String direction = (String) dataMap.get("direction");
        String routerType = (String) dataMap.get("routerType");

        LSP ciscoLSP = null;
        JnxLSP jnxLSP = null;
        if (routerType.equals("jnx")) {
            jnxLSP = new JnxLSP(bssDbName);
            try {
                jnxLSP.teardownPath(resv, lspData, direction);
            } catch (PSSException ex) {
                this.log.error("Could not set up path", ex);
            }
        } else if (routerType.equals("cisco")) {
            ciscoLSP = new LSP(bssDbName);
            try {
                ciscoLSP.teardownPath(resv, lspData, direction);
            } catch (PSSException ex) {
                this.log.error("Could not set up path", ex);
            }
        }


        super.execute(context);
        this.log.debug("VendorTeardownPathJobs.end");

    }

}
