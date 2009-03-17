package net.es.oscars.bss.events;

import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.*;
import org.quartz.*;

import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.HashMapTypeConverter;
import net.es.oscars.bss.OSCARSCore;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.ReservationDAO;
import net.es.oscars.scheduler.FireEventJob;

/**
 * EventProducer is used by the entity generating events to schedule
 * notifications. Notifications are sent to a FireEventJob in the core
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
        //set the status message on an error
        if(errorMessage != null && resv != null){
            this.updateResvStatusMsg(errorMessage, resv.getGlobalReservationId());
        }
        OSCARSEvent event = new OSCARSEvent();
        HashMap<String, String[]> resvParams= null;
        /* set userLogin to payload sender if exists
           We should really add a list of requesters like
           in the policy interface but this will have to do 
           for now.*/
        if(resv != null && resv.getPayloadSender() != null){
            userLogin = resv.getPayloadSender();
        }
        try {
            resvParams = HashMapTypeConverter.reservationToHashMap(resv);
        } catch (BSSException e) {
            this.log.error("Unable to convert resv to HashMap for event " + 
                    type + ": " + e.getMessage());
        }
        event.setType(type);
        event.setUserLogin(userLogin);
        event.setSource(source);
        event.setErrorCode(errorCode);
        event.setErrorMessage(errorMessage);
        event.setReservationParams(resvParams);
        this.addEvent(event);
    }

    /**
     * Schedules an event notification
     *
     * @param event the event to schedule
     */
    private void addEvent(OSCARSEvent event) {
        this.log.info("Scheduling notification of event " + event.getType());
        Scheduler sched = this.core.getScheduleManager().getScheduler();
        String jobName = "notify-"+event.hashCode()+System.currentTimeMillis();


        JobDetail jobDetail = new JobDetail(jobName, "NOTIFY", FireEventJob.class);
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
    
    private void updateResvStatusMsg(String errorMessage, String gri){
        if(gri == null){
            return;
        }
        ReservationDAO resvDAO = new ReservationDAO(this.core.getBssDbName());
        //Query the db so modify untainted reservation copy
        Reservation dbResv = null;
        try{
            dbResv = resvDAO.query(gri);
        }catch(BSSException e){
            return;
        }
        dbResv.setStatusMessage(errorMessage);
        resvDAO.update(dbResv);
    }
}
