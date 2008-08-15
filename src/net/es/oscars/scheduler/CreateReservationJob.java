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
        Session bss = core.getBssSession();
        bss.beginTransaction();
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        this.log.debug("GRI is: "+dataMap.get("gri")+"for job name: "+jobName);
        try{
            if(dataMap.containsKey("start")){
                this.start(dataMap);
            }else if(dataMap.containsKey("confirm")){
                this.confirm(dataMap);
            }else if(dataMap.containsKey("complete")){
                //this.confirm(context);
            }else{
                this.log.error("Unknown createReservation job cannot be executed");
            }
            bss.getTransaction().commit();
        }catch(Exception e){
            e.printStackTrace();
            bss.getTransaction().rollback();
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
    public void start(JobDataMap dataMap) throws Exception{
        this.log.debug("start.start");
        Forwarder forwarder = core.getForwarder();
        ReservationManager rm = core.getReservationManager();
        EventProducer eventProducer = new EventProducer();
        String bssDbName = this.core.getBssDbName();
        
        PathInfo pathInfo = (PathInfo) dataMap.get("pathInfo");
        String login = (String) dataMap.get("login");
        
        ReservationDAO resvDAO = new ReservationDAO(bssDbName);
        String gri = (String) dataMap.get("gri");
        Reservation resv = null;
        try {
            resv = resvDAO.query(gri);
        } catch (BSSException ex) {
            String errMessage = "Could not locate reservation in DB for gri: "+gri;
            this.log.error(errMessage);
            eventProducer.addEvent(OSCARSEvent.RESV_CREATE_FAILED, login, "JOB", "", errMessage);
            return;
        }
        eventProducer.addEvent(OSCARSEvent.RESV_CREATE_STARTED, login, "JOB", resv);

        try {
            StateEngine.canUpdateStatus(resv, StateEngine.INCREATE);
        } catch (BSSException ex) {
            this.log.error(ex);
            eventProducer.addEvent(OSCARSEvent.RESV_CREATE_FAILED, login, "JOB", "", ex.getMessage());
            return;
        }

        boolean wasReserved = true;
        Exception error = null;

        try {
            rm.create(resv, pathInfo);
            TypeConverter tc = core.getTypeConverter();
            tc.ensureLocalIds(pathInfo);

            // FIXME: why does this sometimes get unset?
            pathInfo.getPath().setId("unimplemented");

            // checks whether next domain should be contacted, forwards to
            // the next domain if necessary, and handles the response
            // **** IMPORTANT ****
            // FIXME: this bit right here makes CreateReservationJobs kinda slow
            // COUNTER-FIXME: Is this really that slow since the next domain is returning immediately?
            CreateReply forwardReply = forwarder.create(resv, pathInfo);
            rm.finalizeResv(resv, pathInfo, null);
            rm.store(resv);
            if(forwardReply == null){
                dataMap.put("lastDomain", true);
                this.confirm(dataMap);      
            }
        } catch (BSSException ex) {
            this.log.error(ex);
            error = (Exception) ex;
            wasReserved = false;
        } catch (InterdomainException ex) {
            this.log.error(ex);
            error = (Exception) ex;;
            wasReserved = false;
        } catch (Exception ex) {
            this.log.error(ex);
            error = (Exception) ex;
            wasReserved = false;
        } finally {
            forwarder.cleanUp();
            if (error != null) {
                this.se.updateStatus(resv, StateEngine.FAILED);
                eventProducer.addEvent(OSCARSEvent.RESV_CREATE_FAILED, login, "JOB", resv, "", error.getMessage());
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
    public void confirm(JobDataMap dataMap) throws BSSException{
        this.log.debug("confirm.start");
        ReservationManager rm = core.getReservationManager();
        EventProducer eventProducer = new EventProducer();
        PathInfo pathInfo = (PathInfo) dataMap.get("pathInfo");
        String gri = (String) dataMap.get("gri");
        String bssDbName = this.core.getBssDbName();
        ReservationDAO resvDAO = new ReservationDAO(bssDbName);
        Reservation resv = null;
        try {
            resv = resvDAO.query(gri);
        } catch (BSSException ex) {
            String errMessage = "Could not locate reservation in DB for gri: "+gri;
            this.log.error(errMessage);
            //TODO: Set login
            eventProducer.addEvent(OSCARSEvent.RESV_CREATE_FAILED, "", "JOB", "", errMessage);
            return;
        }
        String login = resv.getLogin();
        
        if(!dataMap.containsKey("lastDomain")){
            //TODO: Second option should be NULL or finalize needs to be split-up
            rm.finalizeResv(resv, pathInfo, pathInfo);
            rm.store(resv);
        }
        this.se.updateLocalStatus(resv, 1);
        eventProducer.addEvent(OSCARSEvent.RESV_CREATE_CONFIRMED, login, "JOB", resv, pathInfo);
        
        DomainDAO domainDAO = new DomainDAO(bssDbName);
        //getNextDomain is a misnomer. return hop domain
        Domain firstDomain = domainDAO.getNextDomain(pathInfo.getPath().getHop()[0]);
        if(firstDomain.isLocal()){
            dataMap.put("firstDomain", true);
            this.complete(dataMap);
        }
        this.log.debug("confirm.end");
    }
    
    /**
     * Completes a reservation by updating the path with the 
     * final set of interdomain resources
     *
     * @param dataMap the job parameters
     */
    public void complete(JobDataMap dataMap) throws BSSException{
        this.log.debug("complete.start");
        ReservationManager rm = core.getReservationManager();
        EventProducer eventProducer = new EventProducer();
        String bssDbName = this.core.getBssDbName();
        ReservationDAO resvDAO = new ReservationDAO(bssDbName);
        String gri = (String) dataMap.get("gri");
        PathInfo pathInfo = (PathInfo) dataMap.get("pathInfo");
        Reservation resv = null;
        try {
            resv = resvDAO.query(gri);
        } catch (BSSException ex) {
            String errMessage = "Could not locate reservation in DB for gri: "+gri;
            this.log.error(errMessage);
            //TODO: Set login
            eventProducer.addEvent(OSCARSEvent.RESV_CREATE_FAILED, "", "JOB", "", ex.getMessage());
            return;
        }
        String login = resv.getLogin();
        
        if(!dataMap.containsKey("firstDomain")){
            //TODO: whatever it is I need to do here
        }
        
        try{
            this.se.updateLocalStatus(resv, 0);
            this.se.updateStatus(resv, StateEngine.RESERVED);
        }catch(BSSException ex){
            this.se.updateStatus(resv, StateEngine.FAILED);
            eventProducer.addEvent(OSCARSEvent.RESV_CREATE_FAILED, login, "JOB", resv, "", ex.getMessage());
            this.log.debug("createReservation.complete failed: " + ex.getMessage());
            return;
        }
        eventProducer.addEvent(OSCARSEvent.RESV_CREATE_COMPLETED, login, "JOB", resv, pathInfo);
        
        // just in case this is an immediate reservation, check pending & add setup actions
        PSSScheduler sched = new PSSScheduler(core.getBssDbName());
        sched.pendingReservations(0);
        
        this.log.debug("complete.end");
    }
}
