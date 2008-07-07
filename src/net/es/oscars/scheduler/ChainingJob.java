package net.es.oscars.scheduler;
import org.apache.log4j.Logger;
import org.quartz.*;
import java.util.*;

public class ChainingJob implements Job {

    private Logger log;

    public void execute(JobExecutionContext context) throws JobExecutionException {
        this.log = Logger.getLogger(this.getClass());
        this.log.debug("chainingJobExecute.start");

        String nextJobGroup = context.getJobDetail().getJobDataMap().getString("nextJobGroup");
        String nextJobName = context.getJobDetail().getJobDataMap().getString("nextJobName");
        this.log.debug("next job should be:"+nextJobGroup+"."+nextJobName);
        Scheduler sched = context.getScheduler();
        try {
            /*
            this.log.debug("all jobs");
            for (String jobGroup : sched.getJobGroupNames()) {
                for (String jobName : sched.getJobNames(jobGroup)) {
                    this.log.debug("group: "+jobGroup + " job: "+jobName);
                }
            }
            */

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
        this.log.debug("chainingJobExecute.end");
    }


}
