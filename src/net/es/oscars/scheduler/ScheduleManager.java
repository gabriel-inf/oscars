package net.es.oscars.scheduler;

import net.es.oscars.oscars.*;

import java.util.*;

import org.apache.log4j.Logger;
import org.quartz.*;
import org.quartz.impl.*;



public class ScheduleManager {
    private OSCARSCore core;
    private Scheduler scheduler;
    private Logger log;

    private static ScheduleManager instance;
    public static ScheduleManager getInstance() {
        if (ScheduleManager.instance == null) {
            ScheduleManager.instance = new ScheduleManager();
        }
        return ScheduleManager.instance;
    }


    private ScheduleManager() {
        this.log = Logger.getLogger(this.getClass());
        this.log.info("scheduler.start");
        this.core = OSCARSCore.getInstance();

        try {
            SchedulerFactory schedFact = new StdSchedulerFactory();
            this.scheduler = schedFact.getScheduler();

            JobDetail jobDetail = new JobDetail("Process Queue", "queue", ProcessQueueJob.class);
            Trigger trigger = TriggerUtils.makeMinutelyTrigger(1);

            trigger.setStartTime(new Date());
            trigger.setName("SimpleTrigger");

            this.scheduler.scheduleJob(jobDetail, trigger);
            this.scheduler.start();

        } catch (SchedulerException ex) {
            this.log.error("Scheduler exception", ex);
        }

    }


    @SuppressWarnings("unchecked")
    public void processQueue() {
        this.log.info("processQueue.start");
        try {
            this.scheduler.standby();

            // first wait for currently running jobs in the queue to complete
            boolean isQueueRunning = true;
            while (isQueueRunning) {
                isQueueRunning = false;
                List<JobExecutionContext> currentlyRunningJobs = (List<JobExecutionContext>) this.scheduler.getCurrentlyExecutingJobs();
                for (JobExecutionContext context : currentlyRunningJobs) {
                    String groupName = context.getJobDetail().getGroup();
                    String jobName = context.getJobDetail().getName();
                    this.log.debug("Currenty running job: "+groupName+"."+jobName);
                    if (groupName.equals("QUEUED")) {
                        isQueueRunning = true;
                    }
                }
                if (isQueueRunning) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        this.log.error("Job queueing interrupted", ex);
                        return;
                    }
                }
            }


            String[] unQueuedJobNames = this.scheduler.getJobNames("UNQUEUED");
            String[] queuedJobNames = this.scheduler.getJobNames("QUEUED");
            String jobToSchedule = null;
            JobDetail lastJobInQueue = null;
            if (queuedJobNames != null) {
                for (String queuedJobName : queuedJobNames) {
                    this.log.debug("Queued job name: "+queuedJobName);
                    // the LAST in the queue chain would have null nextJobGroup, nextJobName
                    JobDetail jobDetail = this.scheduler.getJobDetail(queuedJobName, "QUEUED");
                    String nextJobName = (String) jobDetail.getJobDataMap().get("nextJobName");
                    if (nextJobName == null) {
                        lastJobInQueue = jobDetail;
                        this.log.debug("Last job in existing queue: "+queuedJobName);
                        break;
                    }
                }
            }

            JobDetail previousUnqueuedJob = null;

            if (unQueuedJobNames != null) {
                for (String unQueuedJobName : unQueuedJobNames) {
                    this.log.debug("Unqueued job name: "+unQueuedJobName);
                    JobDetail jobDetail = this.scheduler.getJobDetail(unQueuedJobName, "UNQUEUED");
                    if (lastJobInQueue != null && previousUnqueuedJob == null) {
                        this.log.debug("Next job to added to queue: "+unQueuedJobName);
                        jobToSchedule = unQueuedJobName;
                        jobDetail.setGroup("QUEUED");
                        jobDetail.setDurability(false);
                        this.scheduler.deleteJob(unQueuedJobName, "UNQUEUED");
                        this.scheduler.addJob(jobDetail, true);
                        lastJobInQueue.getJobDataMap().put("nextJobGroup", "QUEUED");
                        lastJobInQueue.getJobDataMap().put("nextJobName", unQueuedJobName);
                    } else if (previousUnqueuedJob == null) {
                        this.log.debug("Next job to be first in queue: "+unQueuedJobName);
                        jobToSchedule = unQueuedJobName;
                        jobDetail.setGroup("QUEUED");
                        jobDetail.setDurability(false);
                        this.scheduler.deleteJob(unQueuedJobName, "UNQUEUED");
                        this.scheduler.addJob(jobDetail, true);
                    } else {
                        this.log.debug("Adding job to queue: "+unQueuedJobName);
                        previousUnqueuedJob.getJobDataMap().put("nextJobGroup", "QUEUED");
                        previousUnqueuedJob.getJobDataMap().put("nextJobName", unQueuedJobName);
                        this.scheduler.addJob(previousUnqueuedJob, true);
                        jobDetail.setGroup("QUEUED");
                        jobDetail.setDurability(false);
                        this.scheduler.deleteJob(unQueuedJobName, "UNQUEUED");
                        this.scheduler.addJob(jobDetail, true);
                    }
                    previousUnqueuedJob = jobDetail;
                }

                if (jobToSchedule != null) {
                    Trigger trigger = new SimpleTrigger("immediate", "group1", new Date());
                    trigger.setJobName(jobToSchedule);
                    trigger.setJobGroup("QUEUED");
                    this.scheduler.scheduleJob(trigger);
                }
            }

            this.scheduler.start();

        } catch (SchedulerException ex) {
            this.log.error("Scheduler exception", ex);
        }

        this.log.info("processQueue.end");
    }


    public void addJobToQueue() throws SchedulerException {
    }

    /**
     * @return the scheduler
     */
    public Scheduler getScheduler() {
        return scheduler;
    }

    /**
     * @param scheduler the scheduler to set
     */
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }



}
