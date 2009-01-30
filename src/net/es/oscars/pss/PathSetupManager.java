package net.es.oscars.pss;

import java.util.*;
import org.quartz.*;
import org.apache.log4j.*;
import net.es.oscars.PropHandler;
import net.es.oscars.bss.*;
import net.es.oscars.bss.events.EventProducer;
import net.es.oscars.bss.events.OSCARSEvent;
import net.es.oscars.bss.topology.*;
import net.es.oscars.interdomain.*;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.scheduler.*;

import net.es.oscars.oscars.OSCARSCore;

/**
 * PathSetupManager handles all direct interaction with the PSS module.
 * It contains the factory to create the PSS and makes the necessary method
 * calls.
 *
 * @author Andrew Lake (alake@internet2.edu)
 */
public class PathSetupManager{
    private Logger log;
    private String dbname;
    private Properties props;
    private PSS pss;
    private ReservationLogger rsvLogger;
    private OSCARSCore core;
    
    private long SETUP_CONFIRM_TIMEOUT = 600;//10min
    private long TEARDOWN_CONFIRM_TIMEOUT = 600;//10min
    
    /** Constructor. */
    public PathSetupManager(String dbname) {
        PropHandler propHandler = new PropHandler("oscars.properties");
        PSSFactory pssFactory = new PSSFactory();
        this.core = OSCARSCore.getInstance();
        this.props = propHandler.getPropertyGroup("pss", true);
        this.pss = pssFactory.createPSS(this.props.getProperty("method"), dbname);
        this.log = Logger.getLogger(this.getClass());
        this.rsvLogger = new ReservationLogger(this.log);
        this.dbname = dbname;      
        //Init timeout globals
        Properties timeProps = propHandler.getPropertyGroup("timeout", true);
        String defaultTimeoutStr = timeProps.getProperty("default");
        String createTimeoutStr = timeProps.getProperty("createPath.confirm");
        String teardownTimeoutStr = timeProps.getProperty("teardownPath.confirm");
        
        long defaultTimeout = 0;
        if(defaultTimeoutStr != null){
            try{
                defaultTimeout = Long.parseLong(defaultTimeoutStr);
            }catch(Exception e){
                this.log.error("Default timeout.default property invalid. " +
                               "Defaulting to another value for timeout.");
            }
        }
        
        if(createTimeoutStr != null){
            try{
                SETUP_CONFIRM_TIMEOUT = Long.parseLong(createTimeoutStr);
            }catch(Exception e){
                this.log.error("timeout.createPath.confirm property invalid." +
                               "Defaulting to another value for timeout.");
            }
        }else if(defaultTimeout > 0){
            SETUP_CONFIRM_TIMEOUT = defaultTimeout;
        }
        
        if(teardownTimeoutStr != null){
            try{
                TEARDOWN_CONFIRM_TIMEOUT = Long.parseLong(teardownTimeoutStr);
            }catch(Exception e){
                this.log.error("timeout.teardownPath.confirm property invalid." +
                               "Defaulting to another value for timeout.");
            }
        }else if(defaultTimeout > 0){
            TEARDOWN_CONFIRM_TIMEOUT = defaultTimeout;
        }
    }

    /**
     * Creates path by contacting PSS module
     *
     * @param resv reservation to be created
     * @param doForward forward teardown to next domain if true
     * @return the status returned by the the create operation
     * @throws PSSException
     */
    public String create(Reservation resv, boolean doForward) throws PSSException,
                    InterdomainException {
        this.rsvLogger.redirect(resv.getGlobalReservationId());
        this.log.info("create.start");
        String status = null;
        String gri = resv.getGlobalReservationId();
        StateEngine se = this.core.getStateEngine();
        
        
        /* Check reservation */
        if(this.pss == null){
            this.log.error("PSS is null");
            throw new PSSException("Path setup not currently supported");
        }
        
        /* Create path */
        try{
            se.updateStatus(resv, StateEngine.INSETUP);
            /* Get next domain */
            Domain nextDomain = resv.getPath(PathType.LOCAL).getNextDomain();
            if(nextDomain == null){
                this.updateCreateStatus(2, resv);
            }else{
                this.scheduleStatusCheck(SETUP_CONFIRM_TIMEOUT, resv, "setup", false);
            }
            
            /* Get previous domain */
            // INTERDOMAIN
            PathElem firstElem= resv.getPath(PathType.INTERDOMAIN).getPathElems().get(0);
            Domain firstDomain = null;
            if(firstElem != null && firstElem.getLink() != null){
                firstDomain = firstElem.getLink().getPort().getNode().getDomain();
            }
            if(firstDomain == null || firstDomain.isLocal()){
                this.updateCreateStatus(4, resv);
            }else{
                this.scheduleStatusCheck(SETUP_CONFIRM_TIMEOUT, resv, "setup", true);
            }
            status = this.pss.createPath(resv);
        } catch(Exception e) {
            this.log.error("Error setting up local path for reservation, gri: [" +
                gri + "]");
            e.printStackTrace();
            throw new PSSException("Path cannot be created, error setting up path.");
        }
        this.log.info("create.end");
        this.rsvLogger.stop();
        return status;
    }

    /**
     * Refreshes path by contacting PSS module
     *
     * @param resv reservation to be refreshed
     * @param doForward forward teardown to next domain if true
     * @return the status returned by the the refresh operation
     * @throws PSSException
     */
    public String refresh(Reservation resv, boolean doForward) throws PSSException,
                    InterdomainException{
        String status = null;
        boolean stillActive = false;
        String errorMsg = null;
        String gri = resv.getGlobalReservationId();
        Forwarder forwarder = new Forwarder();
        RefreshPathResponseContent forwardReply = null;
        this.rsvLogger.redirect(resv.getGlobalReservationId());

        /* Check reservation */
        if(this.pss == null){
            throw new PSSException("Path setup not currently supported");
        }

        /* Refresh path in this domain */
        try{
            status = this.pss.refreshPath(resv);
            stillActive = true;
        }catch(Exception e){
            this.log.error("Reservation " + gri + " path failure. " +
                "Sending teardownPath. Reason: " + e.getMessage());
            errorMsg = "A path failure has occurred. The path is no " +
                "longer active. Reason: " + e.getMessage();
        }

        /* Forward to next domain */
        if(stillActive && doForward){
            InterdomainException interException = null;
            try{
                forwardReply = forwarder.refreshPath(resv);
            }catch(InterdomainException e){
                interException = e;
            }finally{
                forwarder.cleanUp();
                if(interException != null){
                    throw interException;
                }
            }
        }else if(doForward){
            //this.forwardTeardown(resv, errorMsg);
        }

        /* Print forwarding status */
        if(forwardReply == null){
            this.log.info("last domain in signalling path");
        }else if(!forwardReply.getStatus().equals("ACTIVE")){
            String errMsg = "forwardReply returned an unrecognized status: " +
                forwardReply.getStatus();
            this.log.error(errMsg);
            throw new PSSException(errMsg);
        }

        this.rsvLogger.stop();
        return status;
    }

    /**
     * Teardown path by contacting PSS module
     *
     * @param resv reservation to be torn down
     * @param newStatus the status to apply when finished
     * @return the status returned by the the teardown operation
     * @throws PSSException
     */
    public String teardown(Reservation resv, String newStatus) 
                            throws PSSException{
        this.rsvLogger.redirect(resv.getGlobalReservationId());
        StateEngine se = this.core.getStateEngine();
        int newStatusBits = 0;
        /* Check reservation */
        if(this.pss == null){
            throw new PSSException("Path teardown not currently supported");
        }
        
        /* Prepare upper local status bits */
        if(StateEngine.CANCELLED.equals(newStatus)){
            newStatusBits = 8;
        }else if(StateEngine.FINISHED.equals(newStatus)){
            newStatusBits = 16;
        }else if(StateEngine.FAILED.equals(newStatus)){
            newStatusBits = 24;
        }
        
        
        /* Teardown path in this domain */
        try{
            se.updateStatus(resv, StateEngine.INTEARDOWN);
            se.updateLocalStatus(resv, newStatusBits);
            /* Get next domain */
            Domain nextDomain = resv.getPath(PathType.LOCAL).getNextDomain();
            if(nextDomain == null){
                this.updateTeardownStatus(2, resv);
            }else{
                this.scheduleStatusCheck(TEARDOWN_CONFIRM_TIMEOUT, resv, "teardown", false);
            }
            
            /* Get previous domain */
            // INTERDOMAIN
            PathElem firstElem= resv.getPath(PathType.INTERDOMAIN).getPathElems().get(0);
            Domain firstDomain = null;
            if(firstElem != null && firstElem.getLink() != null){
                firstDomain = firstElem.getLink().getPort().getNode().getDomain();
            }
            if(firstDomain == null || firstDomain.isLocal()){
                this.updateTeardownStatus(4, resv);
            }else{
                this.scheduleStatusCheck(TEARDOWN_CONFIRM_TIMEOUT, resv, "teardown", true);
            }
        }catch(BSSException e){
            throw new PSSException(e);
        }
        
        /* Teardown */
        String status = this.pss.teardownPath(resv, newStatus);
        
        this.rsvLogger.stop();
        return status;
    }
    
    /**
     * Handles the notifications of an upstream or downstream PATH_*_CONFIRMED
     * event.
     *
     * @param gri the gri of the affected reservation
     * @param producerID the producer of the notification
     * @param targStatus INSETUP or INTEARDOWN
     * @param upstream true if supposed to be from upstream neighbor
     * @throws BSSException
     *
     */
    public void handleEvent(String gri, String producerID, String targStatus,
                            boolean upstream) throws BSSException{
        ReservationDAO dao = new ReservationDAO(this.dbname);
        Reservation resv = dao.query(gri);
        ReservationManager rm = this.core.getReservationManager();
        StateEngine se = this.core.getStateEngine();
        int newLocalStatus = upstream ? StateEngine.UP_CONFIRMED : StateEngine.DOWN_CONFIRMED;
        String op = "setup";
        if(targStatus.equals(StateEngine.INTEARDOWN)){
            op = "teardown";
        }
        String targetNeighbor = "downstream";
        if(upstream){
            targetNeighbor = "upstream";
        }
        if(resv == null){
            this.log.error("Reservation " + gri + " not found");
            return;
        }
        String login = resv.getLogin();
        
        Domain neighborDomain = rm.endPointDomain(resv, upstream);
        if(neighborDomain == null || neighborDomain.isLocal()){
            this.log.error("Could not identify " + targetNeighbor + 
                           " domain in path.");
            return;
        }else if(!neighborDomain.getTopologyIdent().equals(producerID)){
            this.log.debug("The event is from " + producerID + " not the " +
                           targetNeighbor + " domain " + 
                           neighborDomain.getTopologyIdent() + " so discarding");
            return;
        }
        
        /* Get the institution */
        Site site = neighborDomain.getSite();
        if(site == null){
            this.log.error("No site associated with domain " +  
                           neighborDomain.getTopologyIdent() + ". Please specify" +
                           " institution associated with domain in your " +
                           "bss.sites table.");
            return;
        }
        
        String institution = site.getName();
        if(institution == null){
            this.log.error("No institution associated with domain " +  
                           neighborDomain.getTopologyIdent() + ". Please specify" +
                           " institution associated with domain in your " +
                           "aaa.institution and bss.sites table.");
            return;
        }
        
        //check if in cancel state
        int localStatus = se.getLocalStatus(resv);
        if((localStatus & StateEngine.NEXT_STATUS) == StateEngine.NEXT_STATUS_CANCEL){
            //ignore and wait for cancel event
            return;
        }
        
        this.scheduleUpdateAttempt(0,gri,login, targStatus, newLocalStatus,
                                   op,upstream,-1);
    }
    
    /**
     * Handles a PATH_SETUP_FAILED event. Currently it automatically schedules 
     * the path for teardown but more advanced handling may be possible in the 
     * future.
     *
     * @param gri the reservation that failed
     * @param producerID the domain the sent this event
     * @param errorSrc the source of the error
     * @param errorCode the error code
     * @param errorMsg the error message
     * @param failedType PATH_SETUP_FAILED or PATH_TEARDOWN_FAILED
     * @throws BSSException
     **/
     public void handleFailed(String gri, String producerID, String errorSrc, 
                        String errorCode, String errorMsg, String failedType) 
                        throws BSSException{
        
        EventProducer eventProducer = new EventProducer();
        ReservationDAO dao = new ReservationDAO(this.dbname);
        ReservationManager rm = this.core.getReservationManager();
        Reservation resv = dao.query(gri);
        if(resv == null){
            this.log.error("Reservation " + gri + " not found");
            return;
        }
        String login = resv.getLogin();
        
        Domain prevDomain = rm.endPointDomain(resv, true);
        Domain nextDomain = rm.endPointDomain(resv, false);
        Domain neighborDomain = null;
        if(nextDomain == null && prevDomain == null){
            throw new BSSException("Reservation " + gri + 
                                   " is not an interdomain reservation so it" +
                                   " can't be failed by a Notification.");
        }else if(prevDomain != null && prevDomain.getTopologyIdent().equals(producerID)){
            neighborDomain = prevDomain;
        }else if(nextDomain != null && nextDomain.getTopologyIdent().equals(producerID)){
            neighborDomain = nextDomain;
        }
        if(neighborDomain == null){
            this.log.debug("Cannot find notification producer " + producerID +
                           " in the path");
            return;
        }
        
        /* Get the institution */
        Site site = neighborDomain.getSite();
        if(site == null){
            this.log.error("No site associated with domain " +  
                           neighborDomain.getTopologyIdent() + ". Please specify" +
                           " institution associated with domain in your " +
                           "bss.sites table.");
            return;
        }
        
        String institution = site.getName();
        if(institution == null){
            this.log.error("No institution associated with domain " +  
                           neighborDomain.getTopologyIdent() + ". Please specify" +
                           " institution associated with domain in your " +
                           "aaa.institution and bss.sites table.");
            return;
        }

		if(StateEngine.getStatus(resv).equals(StateEngine.FAILED)){
        	this.log.warn("Reservation " + gri + " already failed so skipping.");
        	return;
        }
        
        eventProducer.addEvent(failedType, login, errorSrc, resv, errorCode, errorMsg);
        /** Teardown the reservation. It may be in the queue so detecting
            status won't do much good. Just count on PSS to put in queue 
            and properly check if setup and set to failed */
        try{
            this.teardown(resv, StateEngine.FAILED);
        }catch(PSSException e){
            e.printStackTrace();
            this.log.error("Path "+gri+" was not removed after failure.");
        }
    }
    
    /**
     * Checks the interdomain status of path setup and changes status to ACTIVE
     * if upstream, downstream and local path setup status
     *
     * @param newLocalStatus the new amount to increase the local status field
     * @param resv the reservation to update
     */
     synchronized public void updateCreateStatus(int newLocalStatus, Reservation resv) throws BSSException{
        StateEngine se = this.core.getStateEngine();
        int localStatus = se.getLocalStatus(resv);
        if((newLocalStatus & localStatus) == 1){
            throw new BSSException("Already set local status bit " + newLocalStatus);
        }
        this.log.debug("create.newLocalStatus=" + newLocalStatus);
        this.log.debug("create.oldLocalStatus=" + localStatus);
        se.updateLocalStatus(resv, localStatus + newLocalStatus);
        localStatus = se.getLocalStatus(resv);
        String login = resv.getLogin();
        EventProducer eventProducer = new EventProducer();
        
        //local path setup done
        if(newLocalStatus == StateEngine.CONFIRMED){
            eventProducer.addEvent(OSCARSEvent.PATH_SETUP_CONFIRMED, login, "JOB", resv);
        }
        
        //downstream path setup done
        //3 = StateEngine.DOWN_CONFIRMED + StateEngine.CONFIRMED
        if(newLocalStatus <= StateEngine.DOWN_CONFIRMED && (localStatus & 3) ==3){
            eventProducer.addEvent(OSCARSEvent.DOWN_PATH_SETUP_CONFIRMED, login, "JOB", resv);
        }
        
        //upstream path setup done
        //5 = StateEngine.UP_CONFIRMED + StateEngine.CONFIRMED
        if((newLocalStatus == StateEngine.CONFIRMED || newLocalStatus == StateEngine.UP_CONFIRMED) && ((localStatus & 5) == 5)){
            eventProducer.addEvent(OSCARSEvent.UP_PATH_SETUP_CONFIRMED, login, "JOB", resv);
        }
        
        //everything complete
        if((localStatus & StateEngine.COMPLETED)  == StateEngine.COMPLETED){
            se.updateStatus(resv, StateEngine.ACTIVE);
            se.updateLocalStatus(resv, 0);
            eventProducer.addEvent(OSCARSEvent.PATH_SETUP_COMPLETED, login, "JOB", resv);
        }
     }
     
     /**
     * Checks the interdomain status of path teardowm and changes status to newStatus
     * if upstream, downstream and local path teardown complete
     *
     * @param newLocalStatus the new amount to increase the local status field
     * @param resv the reservation to update
     */
     synchronized public void updateTeardownStatus(int newLocalStatus, Reservation resv) throws BSSException{
        StateEngine se = this.core.getStateEngine();
        int localStatus = se.getLocalStatus(resv);
        if((newLocalStatus & localStatus) == 1){
            throw new BSSException("Already set local status bit " + newLocalStatus);
        }
        if(!se.getStatus(resv).equals(StateEngine.FAILED)){
            se.updateLocalStatus(resv, localStatus + newLocalStatus);
        }
        localStatus = se.getLocalStatus(resv);
        String login = resv.getLogin();
        EventProducer eventProducer = new EventProducer();
        int newStatusBits = (localStatus >> 3) & 255;
        String newStatus = StateEngine.RESERVED;
        
        if(newStatusBits == 1){
            newStatus = StateEngine.CANCELLED;
        }else if(newStatusBits == 2){
            newStatus = StateEngine.FINISHED;
        }else if(newStatusBits == 3){
            newStatus = StateEngine.FAILED;
        }
        
        //local path setup done
        if(newLocalStatus == StateEngine.CONFIRMED){
            eventProducer.addEvent(OSCARSEvent.PATH_TEARDOWN_CONFIRMED, login, "JOB", resv);
        }
        
        //downstream path setup done
        //3 = StateEngine.DOWN_CONFIRMED + StateEngine.CONFIRMED
        if(newLocalStatus <= StateEngine.DOWN_CONFIRMED && (localStatus & 3) ==3){
            eventProducer.addEvent(OSCARSEvent.DOWN_PATH_TEARDOWN_CONFIRMED, login, "JOB", resv);
        }
        
        //upstream path setup done
        //5 = StateEngine.UP_CONFIRMED + StateEngine.CONFIRMED
        if((newLocalStatus == StateEngine.CONFIRMED || newLocalStatus ==  StateEngine.UP_CONFIRMED) && ((localStatus & 5) == 5)){
            eventProducer.addEvent(OSCARSEvent.UP_PATH_TEARDOWN_CONFIRMED, login, "JOB", resv);
        }
        
        if(StateEngine.CANCELLED.equals(newStatus) && 
                newLocalStatus == StateEngine.CONFIRMED){
            se.updateStatus(resv, StateEngine.RESERVED);
            CancelReservationJob crj = new CancelReservationJob();
            crj.init();
            crj.confirm(resv,login,false);
        } else if((localStatus & StateEngine.COMPLETED) == StateEngine.COMPLETED){
            //everything complete
            se.updateStatus(resv, newStatus);
            se.updateLocalStatus(resv, StateEngine.LOCAL_INIT);
            eventProducer.addEvent(OSCARSEvent.PATH_TEARDOWN_COMPLETED, login, "JOB", resv);
        } else if (StateEngine.FAILED.equals(newStatus)){
            se.updateStatus(resv, newStatus);
            se.updateLocalStatus(resv, StateEngine.LOCAL_INIT);
         }
        
        
     }
     
     /**
     * Schedules a job to check if path can be updated 
     *
     * @param wait the time to wait before attempting
     * @param gri the reservation too update
     * @param login the login of the reservation owner
     * @param targStatus the state the reservation must be in for a notification to be handled
     * @param newLocalStatus the new local state to apply to the reservation
     * @param op setup or teardown
     * @param upstream true if from upstream
     * @param retries the number of times to retry
     */
     public void scheduleUpdateAttempt(long wait, String gri, 
                               String login,  String targStatus, 
                               int newLocalStatus, String op, 
                               boolean upstream, int retries){
        Scheduler sched = this.core.getScheduleManager().getScheduler();
        long currTime = System.currentTimeMillis();
        String triggerName = "pathRetryTrig-" + gri.hashCode()+currTime;
        String jobName = "pathRetryJob-" + gri.hashCode()+currTime;
        long time = currTime + wait*1000;
        Date date = new Date(time);
        SimpleTrigger trigger = new SimpleTrigger(triggerName, null, 
                                                  date, null, 0, 0L);
        JobDetail jobDetail = new JobDetail(jobName, "PATH_RETRY", 
                                            PathTimeoutJob.class);
        JobDataMap dataMap = new JobDataMap();
        dataMap.put("retry", true);
        dataMap.put("gri", gri);
        dataMap.put("login", login);
        dataMap.put("targStatus", targStatus);
        dataMap.put("newLocalStatus", newLocalStatus);
        dataMap.put("op", op);
        dataMap.put("upstream", upstream);
        dataMap.put("retries", retries);
        jobDetail.setJobDataMap(dataMap);
        
        try{
            this.log.debug("Adding job " + jobName);
            sched.scheduleJob(jobDetail, trigger);
            this.log.debug("Job added.");
        }catch(SchedulerException ex){
            this.log.error("Scheduler exception: " + ex);    
        }
     }
     
     /**
     * Schedules a job to check if a request timed out
     *
     * @param timeout the time to wait
     * @param resv the reservation to be check
     * @param op setup or teardown
     * @param upstream true if applies to upstream event
     */
     public void scheduleStatusCheck(long timeout, Reservation resv, String op, boolean upstream){
        Scheduler sched = this.core.getScheduleManager().getScheduler();
        StateEngine se = this.core.getStateEngine();
        String prefix = op + (upstream?"-up-":"-down-");
        String triggerName = prefix + "pathTimeoutTrig-" + resv.hashCode();
        String jobName = prefix + "pathTimeoutJob-" + resv.hashCode();
        long time = System.currentTimeMillis() + timeout*1000;
        Date date = new Date(time);
        SimpleTrigger trigger = new SimpleTrigger(triggerName, null, 
                                                  date, null, 0, 0L);
        JobDetail jobDetail = new JobDetail(jobName, "PATH_TIMEOUT", 
                                            PathTimeoutJob.class);
        JobDataMap dataMap = new JobDataMap();
        dataMap.put("statusCheck", true);
        dataMap.put("gri", resv.getGlobalReservationId());
        dataMap.put("status", se.getStatus(resv));
        dataMap.put("newLocalStatus", 0);//unused
        dataMap.put("retries", 0);//unused
        dataMap.put("op", op);
        dataMap.put("upstream", upstream);
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
