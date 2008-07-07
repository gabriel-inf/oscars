package net.es.oscars.scheduler;
import org.apache.log4j.Logger;
import org.quartz.*;
import net.es.oscars.oscars.*;

public class ProcessQueueJob implements Job {
    private Logger log;

    public void execute(JobExecutionContext context) throws JobExecutionException {
        this.log = Logger.getLogger(this.getClass());

        this.log.debug("processQueueJob.start");
        this.log = Logger.getLogger(this.getClass());
        OSCARSCore core = OSCARSCore.getInstance();
        ScheduleManager schedMgr = core.getScheduleManager();
        schedMgr.processQueue();
        this.log.debug("processQueueJob.end");

    }

}
