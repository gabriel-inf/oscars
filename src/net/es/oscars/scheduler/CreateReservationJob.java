package net.es.oscars.scheduler;

import java.util.Properties;
import java.util.Date;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.quartz.*;
import net.es.oscars.bss.*;
import net.es.oscars.bss.topology.*;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.interdomain.*;
import net.es.oscars.notify.*;
import net.es.oscars.oscars.*;
import net.es.oscars.pss.*;
import net.es.oscars.PropHandler;

public class CreateReservationJob extends ChainingJob implements org.quartz.Job {
    private Logger log;
    private OSCARSCore core;
    private StateEngine se;
    private long CONFIRM_TIMEOUT = 600;//10min
    private long COMPLETE_TIMEOUT = 600;//10min
    
    /**
     * Assigns the job to the start, confirm or complete method
     *
     * @param context the job context
     * @throws JobExecutionException
     */
    public void execute(JobExecutionContext context)
            throws JobExecutionException {

        String jobName = context.getJobDetail().getFullName();
        this.log = Logger.getLogger(this.getClass());
        this.log.debug("CreateReservationJob.start name:"+jobName);
        this.core = OSCARSCore.getInstance();
        this.se = this.core.getStateEngine();
        String idcURL = this.core.getServiceManager().getIdcURL();
        EventProducer eventProducer = new EventProducer();
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String gri =  dataMap.getString("gri");
        PathInfo pathInfo = (PathInfo) dataMap.get("pathInfo");
        this.log.debug("GRI is: "+dataMap.get("gri")+"for job name: "+jobName);
        this.init();

        Session bss = core.getBssSession();
        bss.beginTransaction();
        
        /* Verify reservation exists */
        Reservation resv = null;
        String bssDbName = this.core.getBssDbName();
        ReservationDAO resvDAO = new ReservationDAO(bssDbName);
        try {
            resv = resvDAO.query(gri);
        } catch (BSSException ex) {
            bss.getTransaction().rollback();
            String errMessage = "Could not locate reservation in DB for gri: "+gri;
            this.log.error(errMessage);
            eventProducer.addEvent(OSCARSEvent.RESV_CREATE_FAILED, "", 
                                   "JOB", "", ex.getMessage());
            return;
        }
        String login = resv.getLogin();
        
        /* Perform start, confirm, complete, fail or statusCheck operation */
        try {
            if (dataMap.containsKey("start")) {
                this.start(resv, pathInfo);
            } else if(dataMap.containsKey("confirm")) {
                this.confirm(resv, pathInfo);
            } else if(dataMap.containsKey("complete")) {
                this.complete(resv, pathInfo);
            } else if(dataMap.containsKey("fail")) {
                String code = dataMap.getString("errorCode");
                String msg = dataMap.getString("errorMsg");
                String src = dataMap.getString("errorSource");
                this.se.updateStatus(resv, StateEngine.FAILED);
                this.se.updateLocalStatus(resv, StateEngine.LOCAL_INIT);
                eventProducer.addEvent(OSCARSEvent.RESV_CREATE_FAILED, login,
                                      src, resv, code, msg);
            } else if(dataMap.containsKey("statusCheck")) {
                String status = this.se.getStatus(resv);
                int localStatus = this.se.getLocalStatus(resv);
                if(status.equals(dataMap.getString("status")) && 
                    localStatus == dataMap.getInt("localStatus")){
                    String op = (localStatus == StateEngine.CONFIRMED ? 
                                 OSCARSEvent.RESV_CREATE_COMPLETED : 
                                 OSCARSEvent.RESV_CREATE_CONFIRMED);
                    throw new BSSException("Create reservation timed-out " +
                                           "while waiting for event " +  op);
                }
            } else {
                this.log.error("Unknown createReservation job cannot be executed");
            }
            bss.getTransaction().commit();
        } catch(Exception ex) {
            ex.printStackTrace();
            //Rollback any changes...
            bss.getTransaction().rollback();
            //...then start new transaction to update status
            try {
                bss = core.getBssSession();
                bss.beginTransaction();
                this.se.updateStatus(resv, StateEngine.FAILED);
                this.se.updateLocalStatus(resv, StateEngine.LOCAL_INIT);
                eventProducer.addEvent(OSCARSEvent.RESV_CREATE_FAILED, login, 
                                      idcURL, resv, "", ex.getMessage());
                bss.getTransaction().commit();
            } catch(Exception ex2) {
                bss.getTransaction().rollback();
                this.log.error(ex2);
            }
        } finally { 
            this.runNextJob(context);
        }
        this.log.debug("CreateReservationJob.end name:"+jobName);
    }
    
    /**
     * Reads in timeout properties
     */
     public void init() {
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("timeout", true);
        String defaultTimeoutStr = props.getProperty("default");
        String confirmTimeoutStr = props.getProperty("createResv.confirm");
        String completeTimeoutStr = props.getProperty("createResv.complete");
        int defaultTimeout = 0;
        if (defaultTimeoutStr != null) {
            try {
                defaultTimeout = Integer.parseInt(defaultTimeoutStr);
            } catch(Exception e){
                this.log.error("Default timeout.default property invalid. " +
                               "Defaulting to another value for timeout.");
            }
        }
        if (confirmTimeoutStr != null) {
            try {
                CONFIRM_TIMEOUT = Integer.parseInt(confirmTimeoutStr);
            } catch(Exception e){
                this.log.error("timeout.createResv.confirm property invalid." +
                               "Defaulting to another value for timeout.");
            }
        } else if(defaultTimeout > 0) {
            CONFIRM_TIMEOUT = defaultTimeout;
        }
        if (completeTimeoutStr != null) {
            try {
                COMPLETE_TIMEOUT = Integer.parseInt(completeTimeoutStr);
            } catch(Exception e) {
                this.log.error("timeout.createResv.complete property invalid." +
                               "Defaulting to another value for timeout.");
            }
        } else if(defaultTimeout > 0) {
            COMPLETE_TIMEOUT = defaultTimeout;
        }
     }
     
     
    /**
     * Processes an initial request
     *
     * @param resv partially filled in Reserrvation instance
     * @param pathInfo requested path info
     * @throws Exception
     */
    public void start(Reservation resv, PathInfo pathInfo) throws Exception {
        this.log.debug("start.start");
        Forwarder forwarder = core.getForwarder();
        ReservationManager rm = core.getReservationManager();
        EventProducer eventProducer = new EventProducer();
        String login = resv.getLogin();
        Exception error = null;     
        
        eventProducer.addEvent(OSCARSEvent.RESV_CREATE_STARTED, login, "JOB", 
                               resv, pathInfo);
        try {
            StateEngine.canUpdateStatus(resv, StateEngine.INCREATE);
            rm.create(resv, pathInfo);
            TypeConverter tc = core.getTypeConverter();
            tc.ensureLocalIds(pathInfo);

            // FIXME: why does this sometimes get unset?
            pathInfo.getPath().setId(resv.getGlobalReservationId());

            /* checks whether next domain should be contacted, forwards to
               the next domain if necessary, and handles the response */
            CreateReply forwardReply = forwarder.create(resv, pathInfo);
            rm.finalizeResv(resv, pathInfo, false);
            rm.store(resv);
            if (forwardReply == null) {
                this.confirm(resv, pathInfo);      
            } else {
                this.scheduleStatusCheck(CONFIRM_TIMEOUT, resv);
            }
        } catch (Exception ex) {
            error = (Exception) ex;
        } finally {
            forwarder.cleanUp();
            if(error != null){
                throw error;
            }
        }
        this.log.debug("start.end");
    }
    
    /**
     * Confirms a reservation by choosing the final set of resources based 
     * on what other domains return
     *
     * @param resv partially completed reservation instance
     * @param pathInfo PathInfo instance containing path related information
     * @throws BSSException
     */
    public void confirm(Reservation resv, PathInfo pathInfo)
            throws BSSException {

        this.log.debug("confirm.start");
        ReservationManager rm = core.getReservationManager();
        EventProducer eventProducer = new EventProducer();
        String gri = resv.getGlobalReservationId();
        String bssDbName = this.core.getBssDbName();
       
        String login = resv.getLogin();
        rm.finalizeResv(resv, pathInfo, true);
        rm.store(resv);
        this.se.updateLocalStatus(resv, StateEngine.CONFIRMED);
        eventProducer.addEvent(OSCARSEvent.RESV_CREATE_CONFIRMED, login, "JOB", resv, pathInfo);
        
        DomainDAO domainDAO = new DomainDAO(bssDbName);
        //getNextDomain is a misnomer. return hop domain
        Domain firstDomain = domainDAO.getNextDomain(pathInfo.getPath().getHop()[0]);
        if (firstDomain.isLocal()) {
            this.complete(resv, pathInfo);
        } else {
            this.scheduleStatusCheck(COMPLETE_TIMEOUT, resv);
        }
        this.log.debug("confirm.end");
    }
    
    /**
     * Completes a reservation by updating the path with the 
     * final set of interdomain resources
     *
     * @param resv reservation instance to be completed
     * @param pathInfo PathInfo instance containing path related information
     * @throws BSSException
     */
    public void complete(Reservation resv, PathInfo pathInfo)
            throws BSSException {

        this.log.debug("complete.start");
        ReservationManager rm = core.getReservationManager();
        EventProducer eventProducer = new EventProducer();
        String bssDbName = this.core.getBssDbName();
        String gri = resv.getGlobalReservationId();
        String login = resv.getLogin();
        
        rm.finalizeResv(resv, pathInfo, false);
        rm.store(resv);
        this.se.updateLocalStatus(resv, StateEngine.LOCAL_INIT);
        this.se.updateStatus(resv, StateEngine.RESERVED);
        eventProducer.addEvent(OSCARSEvent.RESV_CREATE_COMPLETED, login, "JOB", resv, pathInfo);
        
        // just in case this is an immediate reservation, check pending & add setup actions
        PSSScheduler sched = new PSSScheduler(core.getBssDbName());
        sched.pendingReservations(0);
        this.log.debug("complete.end");
    }
    
    /**
     * Schedules a job to check if a request timed out
     *
     * @param timeout long with time in seconds before timeout
     * @param resv the reservation to be check
     */
    public void scheduleStatusCheck(long timeout, Reservation resv) {
        Scheduler sched = this.core.getScheduleManager().getScheduler();
        String triggerName = "createResvTimeoutTrig-" + resv.hashCode();
        String jobName = "createResvTimeoutJob-" + resv.hashCode();
        long time = System.currentTimeMillis() + timeout*1000;
        Date date = new Date(time);
        SimpleTrigger trigger = new SimpleTrigger(triggerName, null, 
                                                  date, null, 0, 0L);
        JobDetail jobDetail = new JobDetail(jobName, "REQ_TIMEOUT", 
                                            CreateReservationJob.class);
        JobDataMap dataMap = new JobDataMap();
        dataMap.put("statusCheck", true);
        dataMap.put("gri", resv.getGlobalReservationId());
        dataMap.put("status", this.se.getStatus(resv));
        dataMap.put("localStatus", this.se.getLocalStatus(resv));
        jobDetail.setJobDataMap(dataMap);
        try {
            this.log.debug("Adding job " + jobName);
            sched.scheduleJob(jobDetail, trigger);
            this.log.debug("Job added.");
        } catch(SchedulerException ex) {
            this.log.error("Scheduler exception: " + ex);    
        }
    }
}
