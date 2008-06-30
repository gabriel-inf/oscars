package net.es.oscars.scheduler;
import org.apache.log4j.Logger;
import org.quartz.*;
import net.es.oscars.bss.*;

public class CreateReservationJob extends ChainingJob implements org.quartz.Job {
    private Logger log;


    public void execute(JobExecutionContext context) throws JobExecutionException {
        this.log = Logger.getLogger(this.getClass());
        this.log.info("CreateReservationJob.start name:"+context.getJobDetail().getFullName());
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        Reservation resv = (Reservation) dataMap.get("reservation");
        String gri;
        if (resv != null) {
            gri = (String) resv.getGlobalReservationId();
        } else {
            gri = "unknown gri!";
        }
        this.log.debug("gri is: "+gri);
        try {
            Thread.sleep(10000);
            super.execute(context);
        } catch (InterruptedException ex) {
            this.log.error("Interrupted", ex);
        }


        this.log.info("CreateReservationJob.end");
    }

}
