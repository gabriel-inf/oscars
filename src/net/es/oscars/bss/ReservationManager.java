package net.es.oscars.bss;

import java.rmi.RemoteException;
import java.util.*;
import java.util.regex.Pattern;
import org.apache.log4j.*;

import org.aaaarch.gaaapi.tvs.TokenBuilder;
import org.aaaarch.gaaapi.tvs.TokenKey;

import org.quartz.*;

import net.es.oscars.PropHandler;
import net.es.oscars.rmi.RmiUtils;
import net.es.oscars.rmi.aaa.AaaRmiInterface;
import net.es.oscars.scheduler.*;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.bss.events.OSCARSEvent;
import net.es.oscars.bss.topology.*;
import net.es.oscars.pss.*;


/**
 * ReservationManager handles all networking and data access object calls
 * necessary to create, update, delete, and modify reservations.
 *
 * @author David Robertson, Mary Thompson, Jason Lee
 */
public class ReservationManager {
    private Logger log;
    private OSCARSCore core;
    private StateEngine se;
    private PathManager pathMgr;
    private ReservationLogger rsvLogger;
    private String dbname;
    public String GEN_TOKEN;

    /** Constructor. */
    public ReservationManager(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.rsvLogger = new ReservationLogger(this.log);
        this.dbname = dbname;
        this.core = OSCARSCore.getInstance();
        this.se = this.core.getStateEngine();
        this.pathMgr = this.core.getPathManager();
        this.initGlobals();
    }

    /** Initializes global variables */
    private void initGlobals() {
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties aaaProps = propHandler.getPropertyGroup("aaa", true);
        GEN_TOKEN = aaaProps.getProperty("useSignalTokens");
    }

    public String submitCreate(Reservation resv, String login)
        throws  BSSException {
        this.log.info("submitCreate.start");
        ReservationDAO dao = new ReservationDAO(this.dbname);

        // login is checked in validate so set it here
        resv.setLogin(login);
        // Validate parameters
        ParamValidator paramValidator = new ParamValidator();
        Path path = resv.getPath(PathType.REQUESTED);
        if (path == null || path.getPathElems() == null || path.getPathElems().isEmpty()) {
            this.log.debug("Reservation submitted with empty path");
            this.createInitialPath(path);
        }

        StringBuilder errorMsg = paramValidator.validate(resv, path);
        if (errorMsg.length() > 0) {
            this.log.error(errorMsg.toString());
            throw new BSSException(errorMsg.toString());
        }
        this.log.info("create.validated");

        String gri = resv.getGlobalReservationId();
        // set GRI if none specified
        if (gri == null){
            this.log.info("No GRI specified, generating");
            gri = this.generateGRI();
            resv.setGlobalReservationId(gri);
        } else {
            // this should be the first time we're seeing this GRI
            Reservation tmp = null;
            try{
                tmp = dao.query(resv.getGlobalReservationId());
            }catch(BSSException e){}
            if (tmp != null) {
                throw new BSSException("Reservation with gri: " + gri +
                                       " already exists!");
            }
        }

        long seconds = System.currentTimeMillis()/1000;
        resv.setCreatedTime(seconds);

        this.log.info("Updating state for GRI: "+gri);
        // This will be the ONLY time we set status with setStatus
        resv.setStatus(StateEngine.SUBMITTED);
        resv.setLocalStatus(StateEngine.LOCAL_INIT);

        this.log.info("Saving reservation with GRI: "+gri);
        // Save the reservation so that the client can query for it
        dao.update(resv);

        this.log.info("Updating state engine for GRI: "+gri);
        try {
            // Assume AAA has been performed before this.
            this.core.getStateEngine().updateStatus(resv, StateEngine.ACCEPTED);
        } catch (BSSException ex) {
            this.log.error(ex);
        }


        this.log.info("Scheduling tasks for GRI: "+gri);
        // Now create a CreateReservationJob, put it in the SERIALIZE_RESOURCE_SCHEDULING queue
        // All create / cancel / modify operations will be in this queue.
        Scheduler sched = this.core.getScheduleManager().getScheduler();

        String jobName = "submit-"+resv.hashCode();
        JobDetail jobDetail = new JobDetail(jobName, "SERIALIZE_RESOURCE_SCHEDULING", CreateReservationJob.class);
        this.log.debug("Adding job "+jobName);
        jobDetail.setDurability(true);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("start", true);
        jobDataMap.put("gri", resv.getGlobalReservationId());
        jobDataMap.put("login", login);
        jobDetail.setJobDataMap(jobDataMap);
        try {
            sched.addJob(jobDetail, false);
        } catch (SchedulerException ex) {
            throw new BSSException(ex);
        }
        this.log.info("submitCreate.end");
        return gri;
    }

    /**
     * Creates the reservation, given a partially filled in reservation
     * instance and additional parameters.
     *
     * @param resv reservation instance modified in place
     * @throws BSSException
     */
    public void create(Reservation resv)
            throws  BSSException {

        this.log.info("create.start");

        this.rsvLogger.redirect(resv.getGlobalReservationId());

        this.pathMgr.calculatePaths(resv);

        //chose resources not put INCREATE
        this.se.updateStatus(resv, StateEngine.INCREATE);
        this.log.info("create.finish");
        this.rsvLogger.stop();
    }

    /**
     * Verifies a RESERVATION_*_CONFIRMED and RESERVATION_*_COMPLETED
     * event is valid and then schedules it for execution.
     *
     * @param gri the GRI of the reservation being confirmed
     * @param pathInfo the confirmed path
     * @param producerDomainID the URN of the domain that produced this event
     * @param reqStatus the required status of the reservation to perform this operation
     * @param confirm true if confirmed event, false if completed event
     * @throws BSSException
     */
    public void submitResvJob(String producerDomainID, String reqStatus,
                              boolean confirm, OSCARSEvent event) throws BSSException{
        ReservationDAO dao = new ReservationDAO(this.dbname);
        Reservation eventResv = event.getReservation();
        String gri = eventResv.getGlobalReservationId();
        Reservation persistedResv = dao.query(gri);
        String op = "complete";
        String targetNeighbor = "previous";
        if(confirm){
            op = "confirm";
            targetNeighbor = "next";
        }

        /* Find job type */
        Class jobClass = null;
        String prefix = "";
        String altStatus = null;
        boolean requirePath = false;
        if(reqStatus.equals(StateEngine.INCREATE)){
            jobClass = CreateReservationJob.class;
            prefix = "createResv";
            requirePath = true;
        }else if(reqStatus.equals(StateEngine.INMODIFY)){
            jobClass = ModifyReservationJob.class;
            prefix = "modifyResv";
        }else if(reqStatus.equals(StateEngine.RESERVED)){
            jobClass = CancelReservationJob.class;
            prefix = "cancelResv";
            altStatus = StateEngine.ACTIVE;
        }else{
            this.log.error("Unknown job type");
            return;
        }

        if(persistedResv == null){
            this.log.error("Reservation " + gri + " not found");
            return;
        }
        String login = persistedResv.getLogin();

        if(requirePath && eventResv.getPath(PathType.INTERDOMAIN) == null){
            this.log.error("Recieved " + op + " event from " + producerDomainID +
                          " with no path element.");
             return;
        }

        String status = StateEngine.getStatus(persistedResv);
        if((!reqStatus.equals(status)) && (!status.equals(altStatus))){
            this.log.debug("Trying to " + op + " a reservation that doesn't" +
                          " have status " + reqStatus);
            return;
        }
        if(confirm){
            int newLocalStatus = StateEngine.getLocalStatus(persistedResv) + 1;
            StateEngine.canUpdateLocalStatus(persistedResv, newLocalStatus);
        }

        Domain neighborDomain = this.endPointDomain(persistedResv, !confirm);
        if(neighborDomain == null || neighborDomain.isLocal()){
            this.log.error("Could not identify " + targetNeighbor +
                           " domain in path.");
            return;
        }else if(!neighborDomain.getTopologyIdent().equals(producerDomainID)){
            this.log.debug("The event is from " + producerDomainID + " not the " +
                           targetNeighbor + " domain " +
                           neighborDomain.getTopologyIdent() + " so discarding");
            return;
        }
        
        /* Submitting a job to the resource scheduling queue
           so there aren't any resource conflicts */
        Scheduler sched = this.core.getScheduleManager().getScheduler();
        String jobName = prefix+"-"+op+"-"+persistedResv.hashCode();
        JobDetail jobDetail = new JobDetail(jobName, "SERIALIZE_RESOURCE_SCHEDULING", jobClass);
        this.log.debug("Adding job "+jobName);
        jobDetail.setDurability(true);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(op, true);
        jobDataMap.put("gri", gri);
        jobDataMap.put("login", login);
        //jobDataMap.put("institution", institution);
        jobDataMap.put("path", eventResv.getPath(PathType.INTERDOMAIN));
        jobDetail.setJobDataMap(jobDataMap);
        try {
            sched.addJob(jobDetail, false);
        } catch (SchedulerException ex) {
            this.log.error("Unable to schedule " + op + " job");
            ex.printStackTrace();
        }
    }

    /**
     * Handles a RESERVATION_*_FAILED event
     *
     * @param producerDomainID the URN of the domain that produced this event
     * @param reqStatus the required status of the reservation for this to be valid
     * @throws BSSException
     */
    public void submitFailed(String producerDomainID, String reqStatus, 
            OSCARSEvent event) throws BSSException {
        /* Find job type */
        Class jobClass = null;
        String prefix = "";
        String gri = event.getReservation().getGlobalReservationId();
        if (reqStatus.equals(StateEngine.INCREATE)) {
            jobClass = CreateReservationJob.class;
            prefix = "createResv";
        } else if(reqStatus.equals(StateEngine.INMODIFY)) {
            jobClass = ModifyReservationJob.class;
            prefix = "modifyResv";
        } else if(reqStatus.equals(StateEngine.RESERVED)) {
            jobClass = CancelReservationJob.class;
            prefix = "cancelResv";
        } else {
            this.log.error("Unknown job type");
            return;
        }
        ReservationDAO dao = new ReservationDAO(this.dbname);
        Reservation resv = dao.query(gri);
        if (resv == null) {
            this.log.error("Reservation " + gri + " not found");
            return;
        }
        String status = StateEngine.getStatus(resv);
        if (!reqStatus.equals(status)) {
            this.log.debug("Trying to fail a reservation that doesn't" +
                          " have status " + reqStatus);
            return;
        }
        
        Domain prevDomain = this.endPointDomain(resv, true);
        Domain nextDomain = this.endPointDomain(resv, false);
        Domain neighborDomain = null;
        if (nextDomain == null && prevDomain == null) {
            throw new BSSException("Reservation " + gri +
                                   " is not an interdomain reservation so it" +
                                   " can't be failed by a Notification.");
        } else if(prevDomain != null && prevDomain.getTopologyIdent().equals(producerDomainID)) {
            neighborDomain = prevDomain;
        } else if(nextDomain != null && nextDomain.getTopologyIdent().equals(producerDomainID)) {
            neighborDomain = nextDomain;
        }
        if (neighborDomain == null) {
            this.log.debug("Cannot find notification producer " + producerDomainID +
                           " in the path");
            return;
        }
        
        /* Submitting a job to the resource scheduling queue
           so there aren't any resource conflicts */
        Scheduler sched = this.core.getScheduleManager().getScheduler();
        String jobName = prefix+"-failed-"+resv.hashCode();
        JobDetail jobDetail = new JobDetail(jobName, "SERIALIZE_RESOURCE_SCHEDULING", jobClass);
        this.log.debug("Adding job "+jobName);
        jobDetail.setDurability(true);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("fail", true);
        jobDataMap.put("gri", gri);
        jobDataMap.put("errorSource", event.getSource());
        jobDataMap.put("errorCode", event.getErrorCode());
        jobDataMap.put("errorMsg", event.getErrorMessage());
        jobDetail.setJobDataMap(jobDataMap);
        try {
            sched.addJob(jobDetail, false);
        } catch (SchedulerException ex) {
            this.log.error("Unable to schedule failure job");
            ex.printStackTrace();
        }
    }

    /**
     * Stores the reservation in the database.
     *
     * @param resv Reservation instance to persist
     */
    public void store(Reservation resv) throws BSSException {
        this.rsvLogger.redirect(resv.getGlobalReservationId());
        this.log.info("store.start");
        // store it in the database
        ReservationDAO dao = new ReservationDAO(this.dbname);
        dao.create(resv);
        if (resv.getToken() != null) {
            TokenDAO tokenDAO = new TokenDAO(this.dbname);
            tokenDAO.create(resv.getToken());
        }
        this.log.info("store.finish");
        this.rsvLogger.stop();
    }

    /**
     * Submits a cancel job for execution
     *
     * @param resv the reservation to cancel
     * @param login the login of the sender of the cancel
     * @param institution the sender's institution
     * @throws BSSException
     */
    public void submitCancel(Reservation resv, String loginConstraint,
                        String login, String institution) throws BSSException {
        String status = StateEngine.getStatus(resv);

        //can't cancel a reservation in a terminal state
        if (StateEngine.CANCELLED.equals(status) ||
            StateEngine.FINISHED.equals(status) ||
            StateEngine.FAILED.equals(status)) {
            throw new BSSException("Can't cancel a reservation in the state "
                                   + status);
        }
        /* don't worry about any other states because that will be detected
           when it's actually in the queue */
        Scheduler sched = this.core.getScheduleManager().getScheduler();
        String jobName = "submitCancel-"+resv.hashCode();
        JobDetail jobDetail = new JobDetail(jobName, "SERIALIZE_RESOURCE_SCHEDULING",
                                            CancelReservationJob.class);
        this.log.debug("Adding job "+jobName);
        jobDetail.setDurability(true);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("start", true);
        jobDataMap.put("gri", resv.getGlobalReservationId());
        jobDataMap.put("login", login);
        jobDataMap.put("loginConstraint", loginConstraint);
        jobDataMap.put("institution", institution);
        jobDetail.setJobDataMap(jobDataMap);
        try {
            sched.addJob(jobDetail, false);
        } catch (SchedulerException ex) {
            throw new BSSException(ex);
        }
    }

    /**
     * Given a reservation GRI, cancels the corresponding reservation.
     * (Can only cancel a pending or active reservation.)
     *
     * @param resv the reservation to cancel
     * @throws BSSException
     */
    public void cancel(Reservation resv)
            throws BSSException {
        String gri = resv.getGlobalReservationId();
        this.rsvLogger.redirect(gri);
        // See if we can cancel at all; this logic is DIFFERENT from the StateEngine
        String status = StateEngine.getStatus(resv);
        if (status.equals(StateEngine.CANCELLED)) {
            // a no-op; no need to complain though
            return;
        }
        String newStatus = "";
        if(status.equals(StateEngine.ACCEPTED)){
            //hasn't been forwarded yet so cancel immediately
            newStatus = StateEngine.CANCELLED;
        } else if (status.equals(StateEngine.RESERVED)) {
            //wait for complete before cancelled
            newStatus = StateEngine.RESERVED;
        } else if (status.equals(StateEngine.ACTIVE)) {
            newStatus = StateEngine.INTEARDOWN;
        } else {
            throw new BSSException("Cannot cancel with status: "+status);
        }

        // remove all pending jobs in the scheduler related to this reservation
        /* NOTE: I DON'T THINK WE NEED THIS AS STATE ENGINE SHOULD PREVENT ANY
                 ERRORS. IF WE DO END UP NEEDING THIS IT MUST BE MODIFED TO NOT
                 DELETE THE CURRENT JOB
        */
        //this.core.getScheduleManager().processCancel(resv, newStatus);
        if (newStatus.equals(StateEngine.INTEARDOWN)) {
            // add the teardown jobs
            try {
                core.getPathSetupManager().teardown(resv, StateEngine.CANCELLED);
            } catch (PSSException ex) {
                this.log.error(ex);
                throw new BSSException(ex);
            }
        } else if(newStatus.equals(StateEngine.CANCELLED)) {
            this.se.updateStatus(resv, newStatus);
            this.se.updateLocalStatus(resv, StateEngine.LOCAL_INIT);
        }
        this.log.info("cancel.finish: " + resv.getGlobalReservationId());
        this.rsvLogger.stop();
    }

    /**
     * Given a reservation GRI, queries the database and returns the
     *     corresponding reservation instance.
     *
     * @param gri string with reservation GRI
     * @param login string with login name of the caller,
     *          if null any user's reservation may be queried
     *          if set, only that user's reservation may be queried
     * @param institution string with institution of caller
     *          if null reservations from any site may be queried
     *          if set only reservations starting or ending at that site may be queried
     * @return resv corresponding reservation instance, if any
     * @throws BSSException
     */
    public Reservation query(String gri, String login, String institution)
            throws BSSException {

        this.log.info("query.start: " + gri + " login: " + login + " institution: " + institution);
        Reservation resv = getConstrainedResv(gri, login, institution,  null);
        this.log.info("query.finish: " + resv.getGlobalReservationId());
        return resv;
    }

    /**
     * Modifies the reservation, given a partially filled in reservation
     * instance and additional parameters.
     *
     * @param resv reservation instance modified in place
     * @param loginConstraint string with login name
     *          if null any user's reservation may be modified
     *          if set, only that user's reservation may be modified
     * @param login string with login name of requester
     * @param institution string with institution of caller
     *          if null reservations from any site may be modified
     *          if set only reservations starting or ending at that site may be modified
     * @throws BSSException
     */
    public Reservation submitModify(Reservation resv, String loginConstraint,
                       String login, String institution) throws  BSSException {

        this.log.info("modify.start: login: " +  loginConstraint +
                      " institution: " +  institution );
        String gri = resv.getGlobalReservationId();
        Reservation persistentResv =
            this.getConstrainedResv(gri, loginConstraint, institution, null);
        // need to set this before validation
        // leave it the same, do not set to current user
        resv.setLogin(persistentResv.getLogin());
        resv.setCreatedTime(persistentResv.getCreatedTime());

        ParamValidator paramValidator = new ParamValidator();
        StringBuilder errorMsg = paramValidator.validate(resv, null);
        if (errorMsg.length() > 0) {
            throw new BSSException(errorMsg.toString());
        }

        if(StateEngine.INMODIFY.equals(StateEngine.getStatus(persistentResv))){
            throw new BSSException("Cannot modify reservation because it is " +
                                    "already being modified.");
        }
        StateEngine.canUpdateStatus(persistentResv, StateEngine.INMODIFY);
        if (resv.getStartTime() > resv.getEndTime()) {
            throw new BSSException("Cannot modify: start time after end time!");
        }

        //Schedule job
        HashMap<String, String[]> resvMap = HashMapTypeConverter.reservationToHashMap(resv);
        Scheduler sched = this.core.getScheduleManager().getScheduler();
        String jobName = "submitModify-"+resv.hashCode();
        JobDetail jobDetail = new JobDetail(jobName, "SERIALIZE_RESOURCE_SCHEDULING", ModifyReservationJob.class);
        this.log.debug("Adding job "+jobName);
        jobDetail.setDurability(true);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("start", true);
        jobDataMap.put("gri", resv.getGlobalReservationId());
        jobDataMap.put("login", login);
        jobDataMap.put("loginConstraint", loginConstraint);
        jobDataMap.put("institution", institution);
        jobDataMap.put("resvMap", resvMap);
        jobDetail.setJobDataMap(jobDataMap);
        try {
            sched.addJob(jobDetail, false);
        } catch (SchedulerException ex) {
            throw new BSSException(ex);
        }
        int localStatus = 0;
        if (StateEngine.getStatus(persistentResv).equals(StateEngine.ACTIVE)) {
            localStatus = StateEngine.MODIFY_ACTIVE;
        }
        this.se.updateStatus(persistentResv, StateEngine.INMODIFY);
        this.se.updateLocalStatus(persistentResv, localStatus);
        return persistentResv;
    }

    /**
     * Modifies the reservation, given a partially filled in reservation
     * instance and additional parameters.
     *
     * @param resv reservation instance modified in place
     * @param persistentResv
     * @throws BSSException
     */
    public void modify(Reservation resv, Reservation persistentResv)
                              throws BSSException {
        Long now = System.currentTimeMillis()/1000;
        if (persistentResv.getStatus().equals(StateEngine.ACTIVE)) {
            // we will silently not allow the user to modify the start time
            resv.setStartTime(persistentResv.getStartTime());
            // can't end before current time
            if (resv.getEndTime() < now) {
                resv.setEndTime(now);
            }
        } else if (persistentResv.getStatus().equals(StateEngine.RESERVED)) {
            if (resv.getEndTime() <= now) {
                throw new BSSException("Cannot modify: reservation ends before current time.");
            }
            // can't start before current time
            if (resv.getStartTime() < now) {
                resv.setStartTime(now);
            }
        }

        // this will throw an exception if modification isn't possible
        // Note that for now, the paths are not allowed to change and any arguments
        // are ignored.
        resv.setPath(persistentResv.getPath(PathType.INTERDOMAIN));
        resv.setPath(persistentResv.getPath(PathType.LOCAL));
        
        //Just check oversubscription since we can't recalculate the path
        this.pathMgr.checkOversubscription(resv);
        this.log.info("modify.finish");
    }

    /**
     * Modifies the reservation, given a partially filled in reservation
     * instance and additional parameters.
     *
     * @param resv reservation instance modified in place
     * @throws BSSException
     */
    public Reservation finalizeModifyResv(Reservation resv)
            throws BSSException {

        this.log.info("finalizeModify.start");
        ReservationDAO resvDAO = new ReservationDAO(this.dbname);

        Reservation persistentResv = resvDAO.query(resv.getGlobalReservationId());
        if (persistentResv == null) {
            throw new BSSException("Could not locate reservation to finalize modify, GRI: "+resv.getGlobalReservationId());
        }

        // OK, if we got so far, just change the times
        persistentResv.setStartTime(resv.getStartTime());
        persistentResv.setEndTime(resv.getEndTime());
        resvDAO.update(persistentResv);
        resvDAO.flush();

        this.log.info("finalizeModify.finish");
        return persistentResv;
    }

   /**
     * @param numRequested int with the number of reservations to return
     * @param resOffset int with the offset into the list
     *
     * @param login String with user's login name
     *          if null any user's reservation may be listed
     *          if set, only that user's reservation may be listed
     *
     * @param institution String with name of user's institution
     *          if null reservations from any site may be listed
     *          if set only reservations starting or ending at that site may be listed
     *
     * @param statuses a list of reservation statuses. If not null or empty,
     * results will only include reservations with one of these statuses.
     * If null / empty, results will include reservations with any status.
     *
     * @param linkIds a list of link id's. If not null / empty, results will
     * only include reservations whose path includes at least one of the links.
     * If null / empty, results will include reservations with any path.
     *
     * @param vlanTags a list of VLAN tags.  If not null or empty,
     * results will only include reservations where (currently) the first link
     * in the path has a VLAN tag from the list (or ranges in the list).  If
     * null / empty, results will include reservations with any associated
     * VLAN.
     *
     * @param startTime the start of the time window to look in; null for
     * everything before the endTime
     *
     * @param endTime the end of the time window to look in; null for everything after the startTime,
     * leave both start and endTime null to disregard time
     *
     * @return reservations list of reservations that user is allowed to see
     * @throws BSSException
     */
    public List<Reservation> list(int numRequested, int resOffset,
            String login, String institution,
            List<String> statuses, String description, List<String> linkIds,
            List<String> vlanTags,  Long startTime, Long endTime)
                throws BSSException {

        List<Reservation> reservations = null;
        List<String> loginIds = new ArrayList<String>();
        List<Reservation> authResv = new LinkedList<Reservation>();
        this.log.info("list.start, login: " + login + " institution: " + institution);

        if (login != null){
            // get only reservations that belong to this user
            loginIds.add(login);
        }
        ReservationDAO dao = new ReservationDAO(this.dbname);
        reservations = dao.list(numRequested, resOffset, loginIds, statuses,
                                description, vlanTags, startTime, endTime);
        if (linkIds != null && !linkIds.isEmpty()) {
            Map<String, Pattern> patterns = new HashMap<String,Pattern>();
            for (String id: linkIds) {
                patterns.put(id, Pattern.compile(".*" + id + ".*"));
            }
            ArrayList<Reservation> removeThese = new ArrayList<Reservation>();
            for (Reservation rsv : reservations) {
                Path localPath = rsv.getPath(PathType.LOCAL);
                Path interdomainPath = rsv.getPath(PathType.INTERDOMAIN);
                boolean matches = false;
                if ((localPath != null) &&
                    this.pathMgr.matches(localPath, patterns)) {
                    matches = true;
                } else if ((interdomainPath != null) &&
                    this.pathMgr.matches(interdomainPath, patterns)) {
                        matches =true;
                }
                if (!matches) {
                    this.log.debug("not returning: " +
                                    rsv.getGlobalReservationId());
                    removeThese.add(rsv);
                }
            }
            for (Reservation rsv : removeThese) {
                reservations.remove(rsv);
            }
        }
        if (institution == null) {
            // the dao.list selected only the allowed reservations
            this.log.info("list.finish, success");
            return reservations;
        } else {
            // keep reservations that start or terminate at institution
            // or belong to this user
            this.log.debug("Checking " + reservations.size() + " reservations for site");

            for (Reservation resv : reservations) {
                if (checkInstitution( resv, institution)) {
                    authResv.add(resv);
                }
            }
            this.log.info("list.finish, success");
            return authResv;
        }
    }

    /**
     *  Generates the next Global Resource Identifier, created from the local
     *  Domain's topology identifier and the next unused index in the IdSequence table.
     *
     * @return the GRI.
     * @throws BSSException
     */
    public String generateGRI() throws BSSException {
        DomainDAO domainDAO = new DomainDAO(this.dbname);
        IdSequenceDAO idDAO = new IdSequenceDAO(this.dbname);
        Domain localDomain = domainDAO.getLocalDomain();
        int id = idDAO.getNewId();
        String gri = null;

        if (localDomain != null) {
            gri = localDomain.getTopologyIdent() + "-" + id;
            this.log.info("GRI: " + gri);
        } else {
            throw new BSSException("Unable to generate GRI");
        }
        return gri;
    }

    /**
     *
     * FIXME: this needs to be looked at
     *
     * Makes final changes to reservation before storage in database.
     * Stores token and vlan tags if they are returned by forwardReply
     * Stores the interdomain path elements returned by forwardReply
     *
     * @param resv reservation to be stored in database
     */
    public void finalizeResv(Reservation resv, boolean confirm, Path pathFromNeighbor)
            throws BSSException {

        /* if user signaled and last domain create token, otherwise store
           token returned in confirm message */
        Path interdomainPath = resv.getPath(PathType.INTERDOMAIN);
        List<PathElem> interPathElems = interdomainPath.getPathElems();
        
        if (confirm) {
            this.pathMgr.finalizeVlanTags(resv, pathFromNeighbor);
        }
        
        //If local reservation then nothing left to do
        if(pathFromNeighbor == null){
            return;
        }
        
        //Path from neighbor should always be the same size or bigger
        List<PathElem> neighborElems = pathFromNeighbor.getPathElems();
        if(neighborElems.size() < interPathElems.size()){
            throw new BSSException("Confirmed path from neighbor contains " +
                        "less hops than path in database");
        }else if(neighborElems.size() != interPathElems.size() && (!confirm)){
            throw new BSSException("Completed path and store path do not " +
                            "have a equal number of hops");
        }
        
        //update hops before the path (but never need to add any)
        for(int i = 0; i < neighborElems.size(); i++){
            PathElem neighborElem = neighborElems.get(i);
            if(i > interPathElems.size()){
                interPathElems.add(neighborElem);
                continue;
            }
            
            PathElem interPathElem = interPathElems.get(i);
            //use startsWith to support domain/node/port/linkId
            if(neighborElem.getUrn().startsWith(interPathElem.getUrn())){
                interPathElem.setUrn(neighborElem.getUrn());
                PathElem.copyPathElemParams(interPathElem, neighborElem, null);
            }else{
                //more hops but this one does not match -add and continue
                interPathElems.add(i, neighborElem);
            }
        }
    }

    /**
     * Creates a token if this is the last domain otherwise sets the token returned
     * from the forward reply.
     *
     * @param forwardReply response from forward request
     * @param resv reservation to be stored in database
     */
    private void generateToken(CreateReply forwardReply, Reservation resv)
                    throws BSSException{
        if (GEN_TOKEN != null && GEN_TOKEN.equals("0")) {
            return;
        }
        Token token = new Token();

        if (forwardReply == null) {
            //Generate token
            String gri = resv.getGlobalReservationId();
            byte[] tokenKey = null;
            String tokenValue = null;
            try {
                tokenKey = TokenKey.generateTokenKey(gri);
                tokenValue = TokenBuilder.getXMLTokenValue(gri, tokenKey);
                this.log.info("token=" + tokenValue);
            } catch (Exception e) {
                this.log.error("Token building error: " + e.getMessage());
                 throw new BSSException("Token building error: " +
                    e.getMessage());
            }
            token.setValue(tokenValue);
        } else if(forwardReply.getToken() == null) {
            return;
        } else {
            token.setValue(forwardReply.getToken());
        }
        resv.setToken(token);
        token.setReservation(resv);
    }

    /**
     * Checks to see if the reservation either starts or terminates at the given institution
     * @param resv Reservation to be checked
     * @param institution String with institution of caller
     * @return Boolean allowed
     */

    public Boolean checkInstitution(Reservation resv, String institution) throws BSSException {
        Domain startDomain = this.endPointDomain(resv,true);
        Domain endDomain = this.endPointDomain(resv,false);
        String sourceTopoId = startDomain.getTopologyIdent();
        String destTopoId  = endDomain.getTopologyIdent();
        try {
            AaaRmiInterface aaaRmiClient =
                RmiUtils.getAaaRmiClient("checkInstitution", log);
            this.log.debug("checkInstitution: sourceTopoId is " + sourceTopoId
                    + "destinationSite is " + destTopoId);
            return aaaRmiClient.checkDomainAccess(null,institution, startDomain.getTopologyIdent(),
                    endDomain.getTopologyIdent());
        } catch ( RemoteException e){
            throw new BSSException ("Remote exception in checkInstitution from checkDomainAccess");
        }
    }

    /**
     * Returns Domain of the institution of the an end point of the reservation
     *
     * @param resv Reservation for which we want to find an end point institution
     * @param source - true returns the source , false returns the destination
     * @return institution String name of the end point
     */
    public Domain endPointDomain(Reservation resv, Boolean source) throws BSSException {
        Path path = resv.getPath(PathType.LOCAL);
        if (path == null ){
            this.log.info ( "no LOCAL path found, trying requested path" );
            path = resv.getPath(PathType.REQUESTED);
            if (path == null) {
                this.log.warn ("neither LOCAL or REQUESTED path found in endPointDomain " + resv.getGlobalReservationId());
                throw new BSSException("no path found for reservation in endPointDomain: " +resv.getGlobalReservationId());
            }
        }
        List<PathElem> hops = path.getPathElems();
        PathElem hop = null;
        if (source) {
            hop = hops.get(0);
        } else { // get last hop
            hop = hops.get(hops.size()-1);
        }
        if (hop ==  null) {
            this.log.warn("no hops found for source " + source + "path id " + path.getId());
            throw new BSSException("no first or last hop found in endPointDomain");
        }
        Link endPoint = hop.getLink();
        if (endPoint ==  null) {
            this.log.warn("no link found for hop source " + source + "hop id " + hop.getId());
            throw new BSSException("no first or last link found in endPointDomain");
        }
        Link remoteLink = endPoint.getRemoteLink();
        Domain endDomain = null;
        if (remoteLink != null) {
            endDomain = remoteLink.getPort().getNode().getDomain();
        } else {
            endDomain = endPoint.getPort().getNode().getDomain();
        }
        return endDomain;
    }

    /**
     *  finds the reservations that matches all the constraints
     *
     *  @param gri String global reservation Id identifies the reservation
     *  @param loginConstraint reservation must be owned by this login
     *  @param institutionConstraint reservation must belong to this institution
     *  @param tokenValue authorization token
     *
     *  @return Reservation - a reservation that meets the constraint,
     *  @throws PSSException if no such reservation exists
     *
     */
   public Reservation getConstrainedResv(String gri,
               String loginConstraint,
               String institutionConstraint,
               String tokenValue)  throws BSSException {

       ReservationDAO resvDAO = new ReservationDAO(this.dbname);
       Reservation resv = null;
       Reservation myResv = null;

       // check  token first, it is sufficient to authorize request
       if (tokenValue != null){
           resv = this.validateToken(tokenValue, gri);
       }
       /* loginConstraint must be set whenever institutionConstraint is set to avoid
        * a race condition in getting and setting the path links during createReservation
        */
       if ((loginConstraint == null) && ( institutionConstraint != null)){
           this.log.error("loginConstraint must be set when institutionConstraint is set");
           throw new BSSException("invalid arguments to queryReservation: loginConstraint must be set when institutionConstraint is set");
       }
       
       try {
           resv = resvDAO.query(gri);
       } catch ( BSSException e ){
           this.log.info("No reservation matches gri: " + gri);
           throw new BSSException("No reservations match request");
       }
       
       //user with no constraints so return the reservation
       if (loginConstraint == null ) {
           return resv;
       }
       
       myResv = resvDAO.queryByGRIAndLogin(gri, loginConstraint);
       if (myResv == null) {
           if (institutionConstraint == null) {
               this.log.info("No reservation matches gri: " + gri + " login: " +
                       loginConstraint);
               throw new BSSException("No reservations match request");
           } else {
               //check the institution
               if (!this.checkInstitution(resv, institutionConstraint)) {
                   this.log.info("No reservations matched gri: " + gri + " login: " +
                           loginConstraint + " institution: " + institutionConstraint);
                   throw new BSSException("No reservations match request");
               }
           }
       }
       if (myResv != null) {
           return myResv;
       } else {
           return resv;
       }
   }

   private void createInitialPath(Path path) throws BSSException {
       this.log.info("createInitialPath.start");
       String errMsg = "";
       String src = "";
       String dst = "";
       if (path.getLayer2Data() != null) {
           src = path.getLayer2Data().getSrcEndpoint();
           dst = path.getLayer2Data().getDestEndpoint();
       } else if (path.getLayer3Data() != null) {
           src = path.getLayer3Data().getSrcHost();
           dst = path.getLayer3Data().getDestHost();
       } else {
           errMsg = "Path has neither L2 nor L3 data attached";
           this.log.error(errMsg);
           throw new BSSException(errMsg);
       }
       if (path.getPathElems() == null) {
           ArrayList<PathElem> pes = new ArrayList<PathElem>();
           path.setPathElems(pes);
       } else if (!path.getPathElems().isEmpty()) {
           errMsg = "createInitialPath called when requested path was not empty";
           this.log.error(errMsg);
           throw new BSSException(errMsg);
       }
       PathElem srcpe = new PathElem();
       srcpe.setUrn(src);
       srcpe.setSeqNumber(0);
       PathElem dstpe = new PathElem();
       dstpe.setUrn(dst);
       dstpe.setSeqNumber(1);
       path.getPathElems().add(srcpe);
       path.getPathElems().add(dstpe);
       this.log.info("createInitialPath.end");
   }

    /**
     * Retrieves a reservation by token. throws an exception if not found
     *
     * @param tokenValue the value of the token to use in lookup
     * @return the matching reservation
     * @throws PSSException
     */
    private Reservation validateToken(String tokenValue, String gri)
            throws BSSException{

        TokenDAO tokenDAO = new TokenDAO(this.dbname);
        Token token = tokenDAO.fromValue(tokenValue);
        Reservation resv = null;

        /* Check token value */
        this.log.info("token received");
        if (token == null) {
            this.log.error("unrecognized token");
            throw new BSSException("Unrecognized token specified.");
        }

        /* Check GRI */
        resv = token.getReservation();
        if (!resv.getGlobalReservationId().equals(gri)) {
            this.log.error("token and GRI do not match");
            throw new BSSException("Token and GRI do not match");
        }
        this.log.info("token matched");

        return token.getReservation();

    }

}
