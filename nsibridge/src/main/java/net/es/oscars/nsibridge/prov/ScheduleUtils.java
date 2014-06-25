package net.es.oscars.nsibridge.prov;

import net.es.oscars.nsibridge.task.ExpirationMonitor;
import net.es.oscars.nsibridge.task.ProvMonitor;
import net.es.oscars.nsibridge.task.ResvTimeoutMonitor;
import net.es.oscars.utils.task.sched.Schedule;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;

public class ScheduleUtils {

    private static boolean isProvMonitorRunning = false;
    private static boolean isResvTimeoutMonitorRunning = false;
    private static boolean isExpirationMonitorRunning = false;
    private static ProvMonitor provMonitor = null;
    private static ResvTimeoutMonitor resvMonitor = null;
    private static ExpirationMonitor expirationMonitor= null;

    synchronized public static void scheduleProvMonitor() throws SchedulerException {
        if (isProvMonitorRunning) return;

        provMonitor = new ProvMonitor();
        provMonitor.start();
        isProvMonitorRunning = true;
    }
    
    public static void stopProvMonitor() throws SchedulerException {
        if (!isProvMonitorRunning) return;
        provMonitor.interrupt();
        isProvMonitorRunning = false;
    }


    synchronized public static void scheduleResvTimeoutMonitor() throws SchedulerException {
        if (isResvTimeoutMonitorRunning) return;
        resvMonitor = new ResvTimeoutMonitor();
        resvMonitor.start();
        isResvTimeoutMonitorRunning = true;
    }
    public static void stopResvTimeoutMonitor() throws SchedulerException {
        if (!isResvTimeoutMonitorRunning) return;
        Schedule ts = Schedule.getInstance();
        ts.getScheduler().pauseTrigger("ResvMonitorTicker", "ResvMonitorTicker");
        isProvMonitorRunning = false;
    }

    synchronized public static void scheduleExpirationMonitor() throws SchedulerException {
        if (isExpirationMonitorRunning) return;
        expirationMonitor = new ExpirationMonitor();
        expirationMonitor.start();
        isExpirationMonitorRunning = true;
    }
    public static void stopExpirationMonitor() throws SchedulerException {
        if (!isExpirationMonitorRunning) return;
        Schedule ts = Schedule.getInstance();
        ts.getScheduler().pauseTrigger("ExpirationMonitorTicker", "ExpirationMonitorTicker");
        isExpirationMonitorRunning = false;
    }

}
