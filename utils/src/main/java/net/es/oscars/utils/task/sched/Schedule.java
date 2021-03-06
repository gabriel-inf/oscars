package net.es.oscars.utils.task.sched;

import net.es.oscars.utils.task.TaskException;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Date;
import java.util.Properties;
import java.util.UUID;

public class Schedule {
    private Schedule() {}
    private Scheduler scheduler;
    private static Schedule instance;
    private boolean started;
    private boolean quartzStarted;
    private boolean taskRunnerStarted;
    private WFTicker wfTicker;
    
    public static Schedule getInstance() {
        if (instance == null) {
            instance = new Schedule();
        }
        return instance;
    }


    public void stop() throws TaskException {
        if (!isStarted()) return;
        stopTaskRunner();
        stopQuartz();
        started = false;
    }

    public void start() throws TaskException {
        if (isStarted()) return;
        startQuartz();
        startTaskRunner();
        started = true;
    }

    public void startQuartz() throws TaskException {
        if (quartzStarted) return;
        try {
            /*
            Properties props = new Properties();
            props.put("org.quartz.properties", "config/quartz.properties");
            */

            SchedulerFactory schedFact = new StdSchedulerFactory();

            this.scheduler = schedFact.getScheduler();
            scheduler.start();
            quartzStarted = true;
        } catch (SchedulerException ex) {
            ex.printStackTrace();
            throw new TaskException(ex.getMessage());
        }

    }

    public void startTaskRunner() throws TaskException {
        if (taskRunnerStarted) return;
        try {
            wfTicker = new WFTicker();
            wfTicker.start();
            taskRunnerStarted = true;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TaskException(ex.getMessage());
        }

    }

    public void stopTaskRunner() throws TaskException {
        if (!taskRunnerStarted) return;
        try {
            wfTicker.interrupt();
            taskRunnerStarted = false;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TaskException(ex.getMessage());
        }
    }

    public void stopQuartz() throws  TaskException {
        if (!quartzStarted) return;
        try {
            scheduler.shutdown();
            quartzStarted = false;
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }


    public Scheduler getScheduler() {
        return scheduler;
    }


    public void scheduleSpecificTask(UUID taskId, long delayMillis) throws TaskException {
        long startTime = System.currentTimeMillis() + delayMillis;

        try {
            String groupId = "specTasks";

            UUID trigId = UUID.randomUUID();
            SimpleTrigger nowTrigger = new SimpleTrigger(groupId, trigId.toString(), new Date(startTime));
            UUID jobId = UUID.randomUUID();

            JobDetail jobDetail = new JobDetail(jobId.toString(), groupId, SpecificTaskJob.class);
            jobDetail.getJobDataMap().put("taskId", taskId);

            this.scheduler.scheduleJob(jobDetail, nowTrigger);
        } catch (SchedulerException ex) {
            ex.printStackTrace();
            throw new TaskException(ex.getMessage());
        }

    }


    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public boolean isQuartzStarted() {
        return quartzStarted;
    }

    public void setQuartzStarted(boolean quartzStarted) {
        this.quartzStarted = quartzStarted;
    }

    public boolean isTaskRunnerStarted() {
        return taskRunnerStarted;
    }

    public void setTaskRunnerStarted(boolean taskRunnerStarted) {
        this.taskRunnerStarted = taskRunnerStarted;
    }
}
