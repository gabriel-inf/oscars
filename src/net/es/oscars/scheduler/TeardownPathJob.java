package net.es.oscars.scheduler;
import net.es.oscars.bss.Reservation;

import org.apache.log4j.Logger;
import org.quartz.*;

public class TeardownPathJob extends ChainingJob implements Job {
    private Logger log;

    public void execute(JobExecutionContext context) throws JobExecutionException {
        this.log = Logger.getLogger(this.getClass());
        this.log.info("TeardownPathJob.start name:"+context.getJobDetail().getFullName());
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        Reservation resv = (Reservation) dataMap.get("reservation");
        String gri;
        if (resv != null) {
            gri = (String) resv.getGlobalReservationId();
        } else {
            gri = "unknown gri!";
        }
        this.log.debug("gri is: "+gri);
        super.execute(context);
        this.log.info("TeardownPathJob.end");
    }

}
