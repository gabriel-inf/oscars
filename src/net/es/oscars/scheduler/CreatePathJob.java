package net.es.oscars.scheduler;
import net.es.oscars.bss.Reservation;

import org.apache.log4j.Logger;
import org.quartz.*;

public class CreatePathJob extends ChainingJob  implements Job {
    private Logger log;

    public void execute(JobExecutionContext context) throws JobExecutionException {
        this.log = Logger.getLogger(this.getClass());
        this.log.info("CreatePathJob.start name:"+context.getJobDetail().getFullName());
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






        try {
            Thread.sleep(10000);
            this.runNextJob(context);
        } catch (InterruptedException ex) {
            this.log.error("Interrupted", ex);
        }

        this.log.info("CreatePathJob.end");

    }

}
