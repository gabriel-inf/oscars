package net.es.oscars.notify;

import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.*;
import org.quartz.*;

import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.PathTypeConverter;
import net.es.oscars.oscars.OSCARSCore;
import net.es.oscars.scheduler.NotifyJob;
import net.es.oscars.wsdlTypes.PathInfo;

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
     */
    public void addEvent(String type, String userLogin, String source){
        this.addEvent(type, userLogin, source, null, null, null);
    }
    
    /**
     * Schedules an event notification
     *
     * @param type the type of event.
     * @param userLogin the login of the user that triggered the event
     * @param source the entity that caused the event (API, WBUI, or SCHEDULER)
     * @param errorCode the error code of the event. null if no error.
     * @param errorMessage a message describing an error. null if no error.
     */
    public void addEvent(String type, String userLogin, String source,
            String errorCode, String errorMessage){
        this.addEvent(type, userLogin, source, null, errorCode, errorMessage);
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
                         Reservation resv, String errorCode, 
                         String errorMessage){
        this.addEvent(type, userLogin, source, resv, errorCode, errorMessage);
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
