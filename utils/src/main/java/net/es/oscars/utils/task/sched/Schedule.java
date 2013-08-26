package net.es.oscars.utils.task.sched;

import net.es.oscars.utils.task.TaskException;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.impl.StdSchedulerFactory;

import java.util.UUID;

public class Schedule {
    private Schedule() {}
    private Scheduler scheduler;
    private static Schedule instance;
    public static Schedule getInstance() {
        if (instance == null) {
            instance = new Schedule();
        }
        return instance;
    }



    public void start() throws TaskException {
        this.startQuartz();
        this.startTaskRunner();
    }

    public void startQuartz() throws TaskException {
        try {
            SchedulerFactory schedFact = new StdSchedulerFactory();
            this.scheduler = schedFact.getScheduler();
            scheduler.start();
        } catch (SchedulerException ex) {
            ex.printStackTrace();
            throw new TaskException(ex.getMessage());
        }

    }

    public void startTaskRunner() throws TaskException {
        try {
            // look at the queue every second
            SimpleTrigger inspectorTrigger = new SimpleTrigger("WFTicker", "WFTicker");
            inspectorTrigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
            inspectorTrigger.setRepeatInterval(100);
            JobDetail inspectorJobDetail = new JobDetail("WFTicker", "WFTicker", WFTicker.class);
            this.scheduler.scheduleJob(inspectorJobDetail, inspectorTrigger);
        } catch (SchedulerException ex) {
            ex.printStackTrace();
            throw new TaskException(ex.getMessage());
        }

    }

    public void stopTaskRunner() throws TaskException {
        try {
            this.scheduler.pauseTrigger("WFTicker", "WFTicker");
        } catch (SchedulerException ex) {
            ex.printStackTrace();
            throw new TaskException(ex.getMessage());
        }
    }



    public void stop() {
        try {
            scheduler.shutdown();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }


    public Scheduler getScheduler() {
        return scheduler;
    }


    public void scheduleSpecificTask(UUID taskId) throws TaskException {
        try {
            String groupId = "specTasks";

            UUID trigId = UUID.randomUUID();
            SimpleTrigger nowTrigger = new SimpleTrigger(groupId, trigId.toString());
            UUID jobId = UUID.randomUUID();

            JobDetail jobDetail = new JobDetail(jobId.toString(), groupId, SpecificTaskJob.class);
            jobDetail.getJobDataMap().put("taskId", taskId);

            this.scheduler.scheduleJob(jobDetail, nowTrigger);
        } catch (SchedulerException ex) {
            ex.printStackTrace();
            throw new TaskException(ex.getMessage());
        }

    }
}
