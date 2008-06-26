package net.es.oscars.scheduler;
import org.apache.log4j.Logger;
import org.quartz.*;

public class SimpleJob implements Job {
    private Logger log;

    public void execute(JobExecutionContext context) throws JobExecutionException {
        this.log = Logger.getLogger(this.getClass());
        this.log.info("Executed job name:"+context.getJobDetail().getFullName());

    }

}
