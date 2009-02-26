package net.es.oscars.scheduler;
import net.es.oscars.bss.OSCARSCore;

import org.apache.log4j.Logger;
import org.quartz.*;
import java.util.*;

public class ChainingJob {

    private Logger log;


    public void runNextJob(JobExecutionContext context) throws JobExecutionException {
        this.log = Logger.getLogger(this.getClass());
        this.log.debug("chainingJobRunNextJob.start");
        ScheduleManager schedMgr = OSCARSCore.getInstance().getScheduleManager();
        
        //Getting the job queue gives this call a lock on the queue
        //any attempts to update the queue will happen after this job executes
        String jobQueueName = context.getJobDetail().getGroup();
        List<String> jobQueue = null;
        try {
            this.log.debug("getting job queue lock...");
            jobQueue = schedMgr.getJobQueue(jobQueueName);
        } catch (InterruptedException e) {
            throw new JobExecutionException(e.getMessage());
        }
        
        //remove self from queue
        if(!jobQueue.isEmpty()){
            jobQueue.remove(0);
        }else{
            this.log.warn("Could not remove self from job queue because its empty");
        }
        
        //get next job from queue
        String nextJobName = null;
        if(!jobQueue.isEmpty()){
            nextJobName = jobQueue.get(0);
        }
        
        this.log.debug("next job should be:"+jobQueueName+"."+nextJobName);
        Scheduler sched = context.getScheduler();
        try {
            if (nextJobName != null) {
                JobDetail jobDetail = sched.getJobDetail(nextJobName, jobQueueName);
                if (jobDetail != null) {

                    this.log.debug("next job will be:"+jobDetail.getFullName());
                    String triggerId = "chain-"+this.hashCode();
                    Trigger trigger = new SimpleTrigger(triggerId, "chains", new Date());
                    trigger.setJobName(nextJobName);
                    trigger.setJobGroup(jobQueueName);

                    // Schedule the trigger
                    sched.scheduleJob(trigger);
                } else {
                    this.log.debug("no job found next");
                }
            }
        } catch (SchedulerException ex) {
            this.log.error("Scheduler error", ex);
        }finally{
            //release lock
            this.log.debug("releasing job queue lock...");
            schedMgr.setJobQueueLock(jobQueueName, false);
        }
        this.log.debug("chainingJobRunNextJob.end");
    }


}
