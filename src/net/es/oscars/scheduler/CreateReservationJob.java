package net.es.oscars.scheduler;
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

public class CreateReservationJob extends ChainingJob implements org.quartz.Job {
    private Logger log;
    private OSCARSCore core;
    private StateEngine se;
    
    /**
     * Assigns the job to the start, confirm or complete method
     *
     * @param context the job context
     */
    public void execute(JobExecutionContext context) throws JobExecutionException {
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
        
        /* Perform start, confirm or complete operation */
        try{
            if(dataMap.containsKey("start")){
                this.start(resv, pathInfo);
            }else if(dataMap.containsKey("confirm")){
                this.confirm(resv, pathInfo);
            }else if(dataMap.containsKey("complete")){
                this.complete(resv, pathInfo);
            }else if(dataMap.containsKey("fail")){
                String code = dataMap.getString("errorCode");
                String msg = dataMap.getString("errorMsg");
                String src = dataMap.getString("errorSource");
                this.se.updateStatus(resv, StateEngine.FAILED);
                eventProducer.addEvent(OSCARSEvent.RESV_CREATE_FAILED, login,
                                      src, resv, code, msg);
            }else{
                this.log.error("Unknown createReservation job cannot be executed");
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
                this.se.updateStatus(resv, StateEngine.FAILED);
                eventProducer.addEvent(OSCARSEvent.RESV_CREATE_FAILED, login, 
                                      idcURL, resv, "", ex.getMessage());
                bss.getTransaction().commit();
            }catch(Exception ex2){
                bss.getTransaction().rollback();
                this.log.error(ex2);
            }
        }finally{
            this.runNextJob(context);
        }
        this.log.debug("CreateReservationJob.end name:"+jobName);
    }
    
    /**
     * Processes an initial request
     *
     * @param dataMap the job parameters
     */
    public void start(Reservation resv, PathInfo pathInfo) throws Exception{
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
            if(forwardReply == null){
                this.confirm(resv, pathInfo);      
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
     * @param dataMap the job parameters
     */
    public void confirm(Reservation resv, PathInfo pathInfo) throws BSSException{
        this.log.debug("confirm.start");
        ReservationManager rm = core.getReservationManager();
        EventProducer eventProducer = new EventProducer();
        String gri = resv.getGlobalReservationId();
        String bssDbName = this.core.getBssDbName();
       
        String login = resv.getLogin();
        rm.finalizeResv(resv, pathInfo, true);
        rm.store(resv);
        this.se.updateLocalStatus(resv, 1);
        eventProducer.addEvent(OSCARSEvent.RESV_CREATE_CONFIRMED, login, "JOB", resv, pathInfo);
        
        DomainDAO domainDAO = new DomainDAO(bssDbName);
        //getNextDomain is a misnomer. return hop domain
        Domain firstDomain = domainDAO.getNextDomain(pathInfo.getPath().getHop()[0]);
        if(firstDomain.isLocal()){;
            this.complete(resv, pathInfo);
        }
        this.log.debug("confirm.end");
    }
    
    /**
     * Completes a reservation by updating the path with the 
     * final set of interdomain resources
     *
     * @param dataMap the job parameters
     */
    public void complete(Reservation resv, PathInfo pathInfo) throws BSSException{
        this.log.debug("complete.start");
        ReservationManager rm = core.getReservationManager();
        EventProducer eventProducer = new EventProducer();
        String bssDbName = this.core.getBssDbName();
        String gri = resv.getGlobalReservationId();
        String login = resv.getLogin();
        
        rm.finalizeResv(resv, pathInfo, false);
        rm.store(resv);
        this.se.updateLocalStatus(resv, 0);
        this.se.updateStatus(resv, StateEngine.RESERVED);
        eventProducer.addEvent(OSCARSEvent.RESV_CREATE_COMPLETED, login, "JOB", resv, pathInfo);
        
        // just in case this is an immediate reservation, check pending & add setup actions
        PSSScheduler sched = new PSSScheduler(core.getBssDbName());
        sched.pendingReservations(0);
        
        this.log.debug("complete.end");
    }
}
