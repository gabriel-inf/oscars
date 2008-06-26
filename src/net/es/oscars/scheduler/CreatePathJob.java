package net.es.oscars.scheduler;
import org.apache.log4j.Logger;
import org.quartz.*;

public class CreatePathJob implements Job {
    private Logger log;

    public void execute(JobExecutionContext context) throws JobExecutionException {
        this.log = Logger.getLogger(this.getClass());
        this.log.info("CreatePathJob.start");
        this.log.info("CreatePathJob.end");

    }

}
