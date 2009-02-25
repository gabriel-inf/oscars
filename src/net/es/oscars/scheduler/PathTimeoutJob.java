package net.es.oscars.scheduler;

import java.util.Properties;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.quartz.*;
import net.es.oscars.bss.*;
import net.es.oscars.bss.events.EventProducer;
import net.es.oscars.bss.events.OSCARSEvent;
import net.es.oscars.pss.*;
import net.es.oscars.PropHandler;

public class PathTimeoutJob implements org.quartz.Job {
    private Logger log;
    private OSCARSCore core;
    private StateEngine se;
    private int SETUP_RETRIES = 10;
    private int TEARDOWN_RETRIES = 10;
    private long SETUP_RETRY_WAIT = 30;
    private long TEARDOWN_RETRY_WAIT = 30;
    
    
    /**
     * Assigns the job to the start, confirm or complete method
     *
     * @param context the job context
     */
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String jobName = context.getJobDetail().getFullName();
        this.log = Logger.getLogger(this.getClass());
        this.log.debug("PathTimeoutJob.start name:"+jobName);
        this.core = OSCARSCore.getInstance();
        this.se = this.core.getStateEngine();
        PathSetupManager pm = this.core.getPathSetupManager();
        String idcURL = this.core.getServiceManager().getIdcURL();
        EventProducer eventProducer = new EventProducer();
        
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String gri =  dataMap.getString("gri");
        String login = dataMap.getString("login");
        String targStatus = dataMap.getString("targStatus");
        int newLocalStatus = dataMap.getInt("newLocalStatus");
        int retries = dataMap.getInt("retries");
        String op = dataMap.getString("op");
        boolean upstream = dataMap.getBoolean("upstream");
        
        Reservation resv = null;
        this.log.debug("GRI is: "+dataMap.get("gri")+"for job name: "+jobName);
        this.init();
        
        String failedEvent = null;
        String eventType = null;
        long retryWait = SETUP_RETRY_WAIT;
        if(op.equals("setup")){
            failedEvent = OSCARSEvent.PATH_SETUP_FAILED;
            eventType = (upstream ? OSCARSEvent.UP_PATH_SETUP_CONFIRMED : 
                                   OSCARSEvent.DOWN_PATH_SETUP_CONFIRMED);
            retries = (retries == -1 ? SETUP_RETRIES : retries);
        }else if(op.equals("teardown")){
            failedEvent = OSCARSEvent.PATH_TEARDOWN_FAILED;
            eventType = (upstream ? OSCARSEvent.UP_PATH_TEARDOWN_CONFIRMED : 
                                   OSCARSEvent.DOWN_PATH_TEARDOWN_CONFIRMED);
            retries = (retries == -1 ? TEARDOWN_RETRIES : retries);
            retryWait = TEARDOWN_RETRY_WAIT;
        }else{
            return;
        }
        
        Session bss = core.getBssSession();
        bss.beginTransaction();
        
        /* Get reservation */
        try{
            ReservationDAO dao = new ReservationDAO(this.core.getBssDbName());
            resv = dao.query(gri);
        }catch(BSSException ex){
            bss.getTransaction().rollback();
            this.log.error(ex.getMessage());
            return;
        }
        
        String status = StateEngine.getStatus(resv);
        //if in a final state then return because nothing can be done
        if(StateEngine.FINISHED.equals(status) || 
                StateEngine.CANCELLED.equals(status) || 
                StateEngine.FAILED.equals(status)){
            return;
        }
        
        try{
            if(dataMap.containsKey("retry")){
                /* try updating the status if a confirmed event was sent from
                   another domain whose clock is slightly earlier than the 
                   local clock */
                if(targStatus.equals(status) && op.equals("setup")){
                    pm.updateCreateStatus(newLocalStatus, resv);
                }else if(targStatus.equals(status) && op.equals("teardown")){
                    pm.updateTeardownStatus(newLocalStatus, resv);
                }else if(retries >= 1){
                    retries--;
                    pm.scheduleUpdateAttempt(retryWait, gri, login, targStatus,
                                        newLocalStatus, op, upstream, retries);
                }else{
                    throw new BSSException("Unable to update path after numerous attempts.");
                }
            }else if(dataMap.containsKey("statusCheck")){
                /* timeout a reservation if its not updated by 
                   the time this job runs */
                int localStatus = StateEngine.getLocalStatus(resv);
                int bit = (upstream ? StateEngine.UP_CONFIRMED : StateEngine.DOWN_CONFIRMED);
                if(status.equals(dataMap.getString("status")) && 
                    (localStatus & bit) != 1){
                    throw new BSSException("Timeout while waiting for event "+
                                           eventType);
                }
            }else{
                this.log.error("Unknown path timeout job cannot be executed");
            }
            bss.getTransaction().commit();
        }catch(Exception ex){
            ex.printStackTrace();
            //Rollback any changes...
            bss.getTransaction().rollback();
            //...then start new transaction to update status
            try{
                bss = core.getBssSession();
                bss.beginTransaction();
                se.updateStatus(resv, StateEngine.FAILED);
                se.updateLocalStatus(resv, StateEngine.LOCAL_INIT);
                eventProducer.addEvent(failedEvent, login, idcURL, 
                                       resv, "", ex.getMessage());
                bss.getTransaction().commit();
            }catch(Exception ex2){
                bss.getTransaction().rollback();
                this.log.error(ex2);
            }
        }
        this.log.debug("PathTimeoutJob.end name:"+jobName);
    }
    
    /**
     * Reads in timeout properties
     */
     public void init(){
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("timeout", true);
        String createRetries = props.getProperty("createPath.retryAttempts");
        String createRetryWait = props.getProperty("createPath.retryWait");
        String teardownRetries = props.getProperty("teardownPath.retryAttempts");
        String teardownRetryWait = props.getProperty("teardownPath.retryWait");
        
        if(createRetries != null){
            try{
                SETUP_RETRIES = Integer.parseInt(createRetries);
            }catch(Exception e){
                this.log.error("timeout.createPath.retryAttempts property invalid." +
                               "Defaulting to another value for timeout.");
            }
        }
        
        if(createRetryWait != null){
            try{
                SETUP_RETRY_WAIT = Long.parseLong(createRetryWait);
            }catch(Exception e){
                this.log.error("timeout.createPath.retryWait property invalid." +
                               "Defaulting to another value for timeout.");
            }
        }
        
        if(teardownRetries != null){
            try{
                TEARDOWN_RETRIES = Integer.parseInt(teardownRetries);
            }catch(Exception e){
                this.log.error("timeout.teardownPath.retryAttempts property invalid." +
                               "Defaulting to another value for timeout.");
            }
        }
        
        if(teardownRetryWait != null){
            try{
                TEARDOWN_RETRY_WAIT = Long.parseLong(teardownRetryWait);
            }catch(Exception e){
                this.log.error("timeout.teardownPath.retryWait property invalid." +
                               "Defaulting to another value for timeout.");
            }
        }
     }

}
