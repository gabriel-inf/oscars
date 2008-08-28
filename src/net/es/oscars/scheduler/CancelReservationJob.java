package net.es.oscars.scheduler;

import java.util.*;
import java.net.InetAddress;
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

public class CancelReservationJob  extends ChainingJob  implements Job {
    private Logger log;
    private OSCARSCore core;
    private StateEngine se;
    private long CONFIRM_TIMEOUT = 600;//10min
    private long COMPLETE_TIMEOUT = 600;//10min
    
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String jobName = context.getJobDetail().getFullName();
        this.init();
        this.log.debug("CancelReservationJob.start name:"+jobName);
        TypeConverter tc = this.core.getTypeConverter();
        ReservationManager rm = this.core.getReservationManager();
        String idcURL = this.core.getServiceManager().getIdcURL();
        EventProducer eventProducer = new EventProducer();
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String gri =  dataMap.getString("gri");
        String login = dataMap.getString("login");
        String loginConstraint = dataMap.getString("loginConstraint");
        String institution = dataMap.getString("institution");
        Reservation resv = null;
        this.log.debug("GRI is: "+dataMap.get("gri")+"for job name: "+jobName);
        

        Session bss = core.getBssSession();
        bss.beginTransaction();
        
        /* Get reservation */
        try{
            resv = rm.getConstrainedResv(gri,loginConstraint,institution);
        }catch(BSSException ex){
            bss.getTransaction().rollback();
            this.log.error(ex.getMessage());
            eventProducer.addEvent(OSCARSEvent.RESV_CANCEL_FAILED, login, 
                                   "JOB", "", ex.getMessage());
            return;
        }
        
        /* Perform start, confirm, complete, fail or statusCheck operation */
        try{
            if(dataMap.containsKey("start")){
                this.start(resv, login);
            }else if(dataMap.containsKey("confirm")){
                this.confirm(resv, login, true);
            }else if(dataMap.containsKey("complete")){
                this.complete(resv, login);
            }else if(dataMap.containsKey("fail")){
                this.fail(resv, login, dataMap);
            }else if(dataMap.containsKey("statusCheck")){
               String status = this.se.getStatus(resv);
                int localStatus = this.se.getLocalStatus(resv);
                if(status.equals(dataMap.getString("status")) && 
                    localStatus == dataMap.getInt("localStatus")){
                    String op = ((localStatus & StateEngine.CONFIRMED) == StateEngine.CONFIRMED ? 
                                 OSCARSEvent.RESV_CANCEL_COMPLETED : 
                                 OSCARSEvent.RESV_CANCEL_CONFIRMED);
                    throw new BSSException("Modify reservation timed-out " +
                                           "while waiting for event " +  op);
                }
            }else{
                this.log.error("Unknown modifyReservation job cannot be executed");
            }
            bss.getTransaction().commit();
        }catch(Exception ex){
            ex.printStackTrace();
            //Rollback any changes...
            eventProducer.addEvent(OSCARSEvent.RESV_CANCEL_FAILED, login, 
                                   idcURL, resv, "", ex.getMessage());
            bss.getTransaction().rollback();
            try{
                bss = core.getBssSession();
                bss.beginTransaction();
                this.se.updateLocalStatus(resv, StateEngine.LOCAL_INIT);
                bss.getTransaction().commit();
            }catch(BSSException e){
                bss.getTransaction().rollback();
                this.log.error(e);
            }
            
        }finally{
            this.runNextJob(context);
        }
        this.log.info("CancelReservationJob.end");
    }
    
    /**
     * Reads in timeout properties
     */
     public void init(){
        this.log = Logger.getLogger(this.getClass());
        this.core = OSCARSCore.getInstance();
        this.se = this.core.getStateEngine();
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("timeout", true);
        String defaultTimeoutStr = props.getProperty("default");
        String confirmTimeoutStr = props.getProperty("cancelResv.confirm");
        String completeTimeoutStr = props.getProperty("cancelResv.complete");
        int defaultTimeout = 0;
        if(defaultTimeoutStr != null){
            try{
                defaultTimeout = Integer.parseInt(defaultTimeoutStr);
            }catch(Exception e){
                this.log.error("Default timeout.default property invalid. " +
                               "Defaulting to another value for timeout.");
            }
        }
        
        if(confirmTimeoutStr != null){
            try{
                CONFIRM_TIMEOUT = Integer.parseInt(confirmTimeoutStr);
            }catch(Exception e){
                this.log.error("timeout.cancelResv.confirm property invalid." +
                               "Defaulting to another value for timeout.");
            }
        }else if(defaultTimeout > 0){
            CONFIRM_TIMEOUT = defaultTimeout;
        }
        
        if(completeTimeoutStr != null){
            try{
                COMPLETE_TIMEOUT = Integer.parseInt(completeTimeoutStr);
            }catch(Exception e){
                this.log.error("timeout.cancelResv.complete property invalid." +
                               "Defaulting to another value for timeout.");
            }
        }else if(defaultTimeout > 0){
            COMPLETE_TIMEOUT = defaultTimeout;
        }
     }
     
    public void start(Reservation resv, String login) throws BSSException, InterdomainException{
        this.log.debug("start.start");
        EventProducer eventProducer = new EventProducer();
        String status = this.se.getStatus(resv);
        int localStatus = this.se.getLocalStatus(resv);
        Forwarder forwarder = null;
        String remoteStatus = null;
        
        if(!(StateEngine.RESERVED.equals(status) || 
                StateEngine.ACTIVE.equals(status)) ||
                StateEngine.ACCEPTED.equals(status)){
            throw new BSSException("Cannot cancel a reservation with status "+ 
                                    status);
        }
        if((localStatus & StateEngine.NEXT_STATUS_CANCEL) == StateEngine.NEXT_STATUS_CANCEL){
            //no-op
            this.log.debug("Already in cancel so skipping");
            return;
        }
        
        this.se.updateLocalStatus(resv, StateEngine.NEXT_STATUS_CANCEL);
        eventProducer.addEvent(OSCARSEvent.RESV_CANCEL_STARTED, login, "JOB", resv);
        InterdomainException interException = null;
        try {
            forwarder = this.core.getForwarder();
            remoteStatus = forwarder.cancel(resv);
        } catch (InterdomainException e) {
            interException = e;
            eventProducer.addEvent(OSCARSEvent.RESV_CANCEL_FAILED, login, "JOB", resv, "", e.getMessage());
        } finally {
            forwarder.cleanUp();
            if(interException != null){
                throw interException;
            }
        }
        
        //if last domain in path
        if(remoteStatus == null){
            this.confirm(resv, login, true);
        }else{
            this.scheduleStatusCheck(COMPLETE_TIMEOUT, resv);
        }
        this.log.debug("start.end");
    }
    
    public void confirm(Reservation resv, String login, boolean doCancel) throws BSSException{
        this.log.debug("confirm.start");
        EventProducer eventProducer = new EventProducer();
        if(doCancel){
            ReservationManager rm = this.core.getReservationManager();
            rm.cancel(resv);
            String status = this.se.getStatus(resv);
            if(status.equals(StateEngine.CANCELLED)){
                //if was cancelled then complete immediately
                eventProducer.addEvent(OSCARSEvent.RESV_CANCEL_CONFIRMED, 
                                      login, "JOB", resv);
                eventProducer.addEvent(OSCARSEvent.RESV_CANCEL_COMPLETED, 
                                       login, "JOB", resv);
                return;
            }else if(status.equals(StateEngine.INTEARDOWN)){
                //wait for teardown before confirming cancel
                return;
            }
        }
        
        this.se.updateLocalStatus(resv, StateEngine.NEXT_STATUS_CANCEL+StateEngine.CONFIRMED);
        eventProducer.addEvent(OSCARSEvent.RESV_CANCEL_CONFIRMED, 
                               login, "JOB", resv);
        
        if(this.isFirstDomain(resv)){
            this.complete(resv, login);
        }else{
            this.scheduleStatusCheck(COMPLETE_TIMEOUT, resv);
        }
        this.log.debug("confirm.end");
    }
    
    public void complete(Reservation resv, String login) throws BSSException{
        this.log.debug("complete.start");
        EventProducer eventProducer = new EventProducer();
        this.se.updateStatus(resv, StateEngine.CANCELLED);
        this.se.updateLocalStatus(resv, StateEngine.LOCAL_INIT);
        eventProducer.addEvent(OSCARSEvent.RESV_CANCEL_COMPLETED, login, "JOB",
                               resv);
        this.log.debug("complete.end");
    }
    
    public void fail(Reservation resv, String login, JobDataMap dataMap)
                     throws BSSException{
        this.log.debug("fail.start");
        EventProducer eventProducer = new EventProducer();
        String code = dataMap.getString("errorCode");
        String msg = dataMap.getString("errorMsg");
        String src = dataMap.getString("errorSource");
        int localStatus = this.se.getLocalStatus(resv);
        if((localStatus & StateEngine.CONFIRMED) == StateEngine.CONFIRMED){
            //set to failed if already been confirmed
            this.se.updateStatus(resv, StateEngine.FAILED);
        }
        this.se.updateLocalStatus(resv, StateEngine.LOCAL_INIT);
        eventProducer.addEvent(OSCARSEvent.RESV_CANCEL_FAILED, login,
                              src, resv, code, msg);
        this.log.debug("fail.end");
    }
   
    public boolean isFirstDomain(Reservation resv) throws BSSException{
        /* Not guaranteed interdomain path so try finding src */
        Path path = resv.getPath();
        String src = "";
        String bssDbName = this.core.getBssDbName();
        
        if(path.getLayer2Data() != null){
            src = path.getLayer2Data().getSrcEndpoint();
        }else if(path.getLayer3Data() != null){
            src = path.getLayer3Data().getSrcHost();
            try{
                src = InetAddress.getByName(src).getHostAddress();
            }catch(Exception e){
                e.printStackTrace();
                throw new BSSException(e);
            }
            IpaddrDAO ipaddrDAO = new IpaddrDAO(bssDbName);
            Ipaddr ip = ipaddrDAO.queryByParam("IP", src);
            src = ip.getLink().getFQTI();
        }else{
            throw new BSSException("Cannot find src endpoint for reservation");
        }
        DomainDAO domainDAO = new DomainDAO(bssDbName);
        Hashtable<String, String> parseResults = URNParser.parseTopoIdent(src);
        String srcDomainId = parseResults.get("domainId");
        return domainDAO.isLocal(srcDomainId);
    }
    
    /**
     * Schedules job to check if a request timed out
     *
     * @param resv the reservation to be check
     */
     public void scheduleStatusCheck(long timeout, Reservation resv){
        Scheduler sched = this.core.getScheduleManager().getScheduler();
        String triggerName = "cancelResvTimeoutTrig-" + resv.hashCode();
        String jobName = "cancelResvTimeoutJob-" + resv.hashCode();
        long time = System.currentTimeMillis() + timeout*1000;
        Date date = new Date(time);
        SimpleTrigger trigger = new SimpleTrigger(triggerName, null, 
                                                  date, null, 0, 0L);
        JobDetail jobDetail = new JobDetail(jobName, "REQ_TIMEOUT", 
                                            CancelReservationJob.class);
        JobDataMap dataMap = new JobDataMap();
        dataMap.put("statusCheck", true);
        dataMap.put("gri", resv.getGlobalReservationId());
        dataMap.put("status", this.se.getStatus(resv));
        dataMap.put("localStatus", this.se.getLocalStatus(resv));
        jobDetail.setJobDataMap(dataMap);
        try{
            this.log.debug("Adding job " + jobName);
            sched.scheduleJob(jobDetail, trigger);
            this.log.debug("Job added.");
        }catch(SchedulerException ex){
            this.log.error("Scheduler exception: " + ex);    
        }
     }
}
