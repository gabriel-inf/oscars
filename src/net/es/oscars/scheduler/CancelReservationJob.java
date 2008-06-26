package net.es.oscars.scheduler;
import org.apache.log4j.Logger;
import org.quartz.*;

public class CancelReservationJob implements Job {
    private Logger log;

    public void execute(JobExecutionContext context) throws JobExecutionException {
        this.log = Logger.getLogger(this.getClass());
        this.log.info("CancelReservationJob.start");
        this.log.info("CancelReservationJob.end");

    }

}
