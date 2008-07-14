package net.es.oscars.notify;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import org.apache.log4j.*;
import org.quartz.*;

import net.es.oscars.bss.Reservation;
import net.es.oscars.oscars.OSCARSCore;
import net.es.oscars.scheduler.NotifyJob;

/**
 * EventProducer is used by the entity generating events to schedule
 * notifications. Notifications are sent to a NotifyJob in the core
 * scheduler from which the notifications are sent out by the
 * configured modules.
 */
public class EventProducer{
    private Logger log;
    private OSCARSCore core;
    /**
     * Constructor that obtains RemoteEventProducer via RMI.
     */
    public EventProducer(){
        this.log = Logger.getLogger(this.getClass());
        this.core = OSCARSCore.getInstance();
    }

    /**
     * Schedules an event notification
     *
     * @param type the type of event.
     * @param userLogin the login of the user that triggered the event
     * @param source the entity that caused the event (API, WBUI, or SCHEDULER)
     * @param resv the reservation affected by this event
     */
    public void addEvent(String type, String userLogin, String source,
            Reservation resv){
        this.addEvent(type, userLogin, source, resv, null, null);
    }

    /**
     * Schedules an event notification
     *
     * @param type the type of event.
     * @param userLogin the login of the user that triggered the event
     * @param source the entity that caused the event (API, WBUI, or SCHEDULER)
     * @param resv the reservation affected by this event
     * @param errorCode the error code of the event. null if no error.
     * @param errorMessage a message describing an error. null if no error.
     */
    public void addEvent(String type, String userLogin, String source,
            Reservation resv, String errorCode, String errorMessage){
        OSCARSEvent event = new OSCARSEvent();
        Reservation resvCopy = null;
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        try {
            resvCopy = resv.copy();
        } catch (Exception e) {
            this.log.info("caught exception");
            e.printStackTrace(pw);
            this.log.info(sw.toString());
        }
        event.setType(type);
        event.setTimestamp(System.currentTimeMillis());
        event.setUserLogin(userLogin);
        event.setSource(source);
        event.setReservation(resvCopy);
        event.setErrorCode(errorCode);
        event.setErrorMessage(errorMessage);
        this.addEvent(event);
    }

    /**
     * Schedules an event notification
     *
     * @param event the event to schedule
     */
    public void addEvent(OSCARSEvent event) {
        this.log.info("Scheduling notifcation of event " + event.getType());
        Scheduler sched = this.core.getScheduleManager().getScheduler();
        String jobName = "notify-"+event.hashCode();


        JobDetail jobDetail = new JobDetail(jobName, "NOTIFY", NotifyJob.class);
        JobDataMap jobDataMap = new JobDataMap();

        this.log.debug("Adding job "+jobDetail.getFullName());
        jobDataMap.put("event", event);
        jobDetail.setJobDataMap(jobDataMap);

        // this is how you schedule a job for immediate execution
        String triggerId = "notify-"+event.hashCode();
        Trigger trigger = new SimpleTrigger(triggerId, "NOTIFY", new Date());


        //TODO:Handle exception better
        try {
            sched.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException ex) {
            this.log.error(ex);
        }
    }
}
