package net.es.oscars.scheduler;
import org.apache.log4j.Logger;
import org.quartz.*;
import java.util.*;

public class ChainingJob {

    private Logger log;


    public void runNextJob(JobExecutionContext context) throws JobExecutionException {
        this.log = Logger.getLogger(this.getClass());
        this.log.debug("chainingJobRunNextJob.start");

        String nextJobGroup = context.getJobDetail().getJobDataMap().getString("nextJobGroup");
        String nextJobName = context.getJobDetail().getJobDataMap().getString("nextJobName");
        this.log.debug("next job should be:"+nextJobGroup+"."+nextJobName);
        Scheduler sched = context.getScheduler();
        try {
            if (nextJobName != null) {
                JobDetail jobDetail = sched.getJobDetail(nextJobName, nextJobGroup);
                if (jobDetail != null) {

                    this.log.debug("next job will be:"+jobDetail.getFullName());
                    String triggerId = "chain-"+this.hashCode();
                    Trigger trigger = new SimpleTrigger(triggerId, "chains", new Date());
                    trigger.setJobName(nextJobName);
                    trigger.setJobGroup(nextJobGroup);

                    // Schedule the trigger
                    sched.scheduleJob(trigger);
                } else {
                    this.log.debug("no job found next");
                }
            }
        } catch (SchedulerException ex) {
            this.log.error("Scheduler error", ex);
        }
        this.log.debug("chainingJobRunNextJob.end");
    }


}
