package net.es.oscars.nsibridge.prov;

import net.es.oscars.nsibridge.task.ProvMonitor;
import net.es.oscars.nsibridge.task.ResvTimeoutMonitor;
import net.es.oscars.utils.task.sched.Schedule;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;

public class ScheduleUtils {

    private static boolean isProvMonitorRunning = false;
    private static boolean isResvTimeoutMonitorRunning = false;

    public static void scheduleProvMonitor() throws SchedulerException {
        if (isProvMonitorRunning) return;

        Schedule ts = Schedule.getInstance();

        SimpleTrigger trigger = new SimpleTrigger("ProvTicker", "ProvTicker");
        trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
        trigger.setRepeatInterval(500);
        JobDetail jobDetail = new JobDetail("ProvTicker", "ProvTicker", ProvMonitor.class);
        ts.getScheduler().scheduleJob(jobDetail, trigger);
        isProvMonitorRunning = true;
    }
    public static void stopProvMonitor() throws SchedulerException {
        if (!isProvMonitorRunning) return;
        Schedule ts = Schedule.getInstance();
        ts.getScheduler().pauseTrigger("ProvTicker", "ProvTicker");


        isProvMonitorRunning = false;
    }


    public static void scheduleResvTimeoutMonitor() throws SchedulerException {
        if (isResvTimeoutMonitorRunning) return;

        Schedule ts = Schedule.getInstance();

        SimpleTrigger trigger = new SimpleTrigger("ResvMonitorTicker", "ResvMonitorTicker");
        trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
        trigger.setRepeatInterval(500);
        JobDetail jobDetail = new JobDetail("ResvMonitorTicker", "ResvMonitorTicker", ResvTimeoutMonitor.class);
        ts.getScheduler().scheduleJob(jobDetail, trigger);
        isProvMonitorRunning = true;
    }
    public static void stopResvTimeoutMonitor() throws SchedulerException {
        if (!isResvTimeoutMonitorRunning) return;
        Schedule ts = Schedule.getInstance();
        ts.getScheduler().pauseTrigger("ResvMonitorTicker", "ResvMonitorTicker");
        isProvMonitorRunning = false;
    }
}
