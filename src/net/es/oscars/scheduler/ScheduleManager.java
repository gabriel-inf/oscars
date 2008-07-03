package net.es.oscars.scheduler;

import net.es.oscars.oscars.*;
import net.es.oscars.pss.PSSScheduler;

import java.util.*;

import org.apache.log4j.Logger;
import org.hibernate.Session;
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

            this.queueScheduledActions();


            String[] queueNames = this.scheduler.getJobGroupNames();

            for (String queueName : queueNames) {
                if (queueName.startsWith("SERIALIZE_")) {
                    this.serializeQueue(queueName);
                }
            }

            this.scheduler.start();

        } catch (SchedulerException ex) {
            this.log.error("Scheduler exception", ex);
        }

        this.log.info("processQueue.end");
    }


    public void serializeQueue(String queueName) throws SchedulerException {

        String unqueuedJobsGroupName = queueName;
        String queuedJobsGroupName = "QUEUED_"+queueName;

        String[] unQueuedJobNames = this.scheduler.getJobNames(unqueuedJobsGroupName);
        String[] queuedJobNames = this.scheduler.getJobNames(queuedJobsGroupName);
        String jobToSchedule = null;
        JobDetail lastJobInQueue = null;
        if (queuedJobNames != null) {
            for (String queuedJobName : queuedJobNames) {
                this.log.debug("Queued job name: "+queuedJobName);
                // the LAST in the queue chain would have null nextJobGroup, nextJobName
                JobDetail jobDetail = this.scheduler.getJobDetail(queuedJobName, queuedJobsGroupName);
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
                JobDetail jobDetail = this.scheduler.getJobDetail(unQueuedJobName, unqueuedJobsGroupName);
                if (lastJobInQueue != null && previousUnqueuedJob == null) {
                    this.log.debug("Next job to added to queue: "+unQueuedJobName);
                    jobToSchedule = unQueuedJobName;
                    jobDetail.setGroup(queuedJobsGroupName);
                    jobDetail.setDurability(false);
                    this.scheduler.deleteJob(unQueuedJobName, unqueuedJobsGroupName);
                    this.scheduler.addJob(jobDetail, true);
                    lastJobInQueue.getJobDataMap().put("nextJobGroup", queuedJobsGroupName);
                    lastJobInQueue.getJobDataMap().put("nextJobName", unQueuedJobName);
                } else if (previousUnqueuedJob == null) {
                    this.log.debug("Next job to be first in queue: "+unQueuedJobName);
                    jobToSchedule = unQueuedJobName;
                    jobDetail.setGroup(queuedJobsGroupName);
                    jobDetail.setDurability(false);
                    this.scheduler.deleteJob(unQueuedJobName, unqueuedJobsGroupName);
                    this.scheduler.addJob(jobDetail, true);
                } else {
                    this.log.debug("Adding job to queue: "+unQueuedJobName);
                    previousUnqueuedJob.getJobDataMap().put("nextJobGroup", queuedJobsGroupName);
                    previousUnqueuedJob.getJobDataMap().put("nextJobName", unQueuedJobName);
                    this.scheduler.addJob(previousUnqueuedJob, true);
                    jobDetail.setGroup(queuedJobsGroupName);
                    jobDetail.setDurability(false);
                    this.scheduler.deleteJob(unQueuedJobName, unqueuedJobsGroupName);
                    this.scheduler.addJob(jobDetail, true);
                }
                previousUnqueuedJob = jobDetail;
            }

            if (jobToSchedule != null) {
                Trigger trigger = new SimpleTrigger("immediate", queuedJobsGroupName, new Date());
                trigger.setJobName(jobToSchedule);
                trigger.setJobGroup(queuedJobsGroupName);
                this.scheduler.scheduleJob(trigger);
            }
        }    }



    public void queueScheduledActions() {
        this.log.debug("queueScheduledActions.start");
        Session session = core.getBssSession();
        session.beginTransaction();
        PSSScheduler sched = new PSSScheduler(core.getBssDbName());
        sched.pendingReservations(0);
        sched.expiredReservations(0);
        sched.expiringReservations(0);
        session.getTransaction().commit();
        this.log.debug("queueScheduledActions.end");
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
