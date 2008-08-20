package net.es.oscars.bss;

import java.util.*;
import java.net.*;
import org.apache.log4j.*;

import org.aaaarch.gaaapi.tvs.TokenBuilder;
import org.aaaarch.gaaapi.tvs.TokenKey;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwcapContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwitchingCapabilitySpecificInfo;
import org.quartz.*;

import net.es.oscars.PropHandler;
import net.es.oscars.oscars.*;
import net.es.oscars.scheduler.*;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.bss.topology.*;
import net.es.oscars.bss.policy.*;
import net.es.oscars.pathfinder.*;
import net.es.oscars.pss.PSSException;
import net.es.oscars.notify.*;

/**
 * ReservationManager handles all networking and data access object calls
 * necessary to create, update, delete, and modify reservations.
 *
 * @author David Robertson, Mary Thompson, Jason Lee
 */
public class ReservationManager {
    private Logger log;
    private PCEManager pceMgr;
    private PolicyManager policyMgr;
    private TypeConverter tc;
    private StateEngine se;
    private String dbname;
    private ReservationLogger rsvLogger;
    private OSCARSCore core;
    public String GEN_TOKEN;
    public String DEFAULT_SWCAP_TYPE;
    public String DEFAULT_ENC_TYPE;
    
    /** Constructor. */
    public ReservationManager(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.rsvLogger = new ReservationLogger(this.log);
        this.pceMgr = new PCEManager(dbname);
        this.policyMgr = new PolicyManager(dbname);
        this.tc = new TypeConverter();
        this.dbname = dbname;
        this.core = OSCARSCore.getInstance();
        this.se = this.core.getStateEngine();
        this.initGlobals();
    }
    
    /** Initializes global variables */
    private void initGlobals(){
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties topoProps = propHandler.getPropertyGroup("topo", true);
        Properties aaaProps = propHandler.getPropertyGroup("aaa", true);
        GEN_TOKEN = aaaProps.getProperty("useSignalTokens");
        DEFAULT_SWCAP_TYPE = topoProps.getProperty("defaultSwcapType");
        if(DEFAULT_SWCAP_TYPE == null){
            DEFAULT_SWCAP_TYPE = "tdm";
        }
        DEFAULT_ENC_TYPE = topoProps.getProperty("defaultEncodingType");
        if(DEFAULT_ENC_TYPE == null){
            DEFAULT_ENC_TYPE = "sdh/sonet";
        }
    }

    public void submitCreate(Reservation resv, String login, PathInfo pathInfo)
        throws  BSSException {
        this.log.info("submitCreate.start");
        ReservationDAO dao = new ReservationDAO(this.dbname);

        // login is checked in validate so set it here
        resv.setLogin(login);
        // Validate parameters
        ParamValidator paramValidator = new ParamValidator();
        StringBuilder errorMsg = paramValidator.validate(resv, pathInfo);
        if (errorMsg.length() > 0) {
            throw new BSSException(errorMsg.toString());
        }
        this.log.info("create.validated");

        // set GRI if none specified
        if (resv.getGlobalReservationId() == null){
            String gri = this.generateGRI();
            resv.setGlobalReservationId(gri);
        } else {
            // this should be the first time we're seeing this GRI
            Reservation tmp = dao.query(resv.getGlobalReservationId());
            if (tmp != null) {
                throw new BSSException("Reservation with gri: "+resv.getGlobalReservationId()+" already exists!");
            }
        }

        long seconds = System.currentTimeMillis()/1000;
        resv.setCreatedTime(seconds);
        Path tempPath = this.buildInitialPath(pathInfo);
        resv.setPath(tempPath);

        // This will be the ONLY time we set status with setStatus
        resv.setStatus(StateEngine.SUBMITTED);
        resv.setLocalStatus(0);
        try {
            // Assume AAA has been performed before this.
            this.se.updateStatus(resv, StateEngine.ACCEPTED);
        } catch (BSSException ex) {
            this.log.error(ex);
        }

        // Save the reservation so that the client can query for it
        dao.update(resv);

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
        jobDataMap.put("pathInfo", pathInfo);
        jobDetail.setJobDataMap(jobDataMap);
        try {
            sched.addJob(jobDetail, false);
        } catch (SchedulerException ex) {
            throw new BSSException(ex);
        }
        this.log.info("submitCreate.end");
    }

    /**
     * Creates the reservation, given a partially filled in reservation
     * instance and additional parameters.
     *
     * @param resv reservation instance modified in place
     * @param pathInfo contains either layer 2 or layer 3 info
     * @throws BSSException
     */
    public void create(Reservation resv, PathInfo pathInfo)
            throws  BSSException {

        this.log.info("create.start");

        this.rsvLogger.redirect(resv.getGlobalReservationId());
        CtrlPlanePathContent pathCopy = null;

        // this modifies the path to include internal hops with layer 2,
        // and finds the complete path with traceroute
        Path path = this.getPath(resv, pathInfo);
        resv.setPath(path);

        // if layer 3, forward complete path found by traceroute, minus
        // internal hops
        // NOTE: This should really be done in internal pathfinders
        // since interdomain URNS of path may not always just be the same
        // as the URNS of the local path minus internal hops
        if (pathInfo.getLayer3Info() != null) {
            pathCopy = this.copyPath(pathInfo, true);
        }else if (pathCopy == null && pathInfo.getLayer2Info() != null) {
            pathCopy = this.copyPath(pathInfo, true);
        }
        pathInfo.setPath(pathCopy);
        //chose resources not put INCREATE
        this.se.updateStatus(resv, StateEngine.INCREATE);
        this.log.info("create.finish");
        this.rsvLogger.stop();
    }
    
    /**
     * Verifies a RESERATION_CREATE_CONFIRMED event is valid and then 
     * schedules it for execution.
     *
     * @param gri the GRI of the reservation being confirmed
     * @param pathInfo the confirmed path
     * @param producerID the URN of the domain that produced this event
     */
    public void submitCreateConfirm(String gri, PathInfo pathInfo, 
                                    String producerID) throws BSSException{
        ReservationDAO dao = new ReservationDAO(this.dbname);
        Reservation resv = dao.query(gri);
        if(resv == null){
            this.log.error("Reservation " + gri + " not found");
            return;
        }
        if(pathInfo.getPath() == null){
            this.log.error("Recieved confirm event from " + producerID + 
                          " with no path element.");
             return;
        }else if(pathInfo.getPath().getHop() == null){
            this.log.error("Recieved confirm event from " + producerID + 
                          " with a path element containing no hops");
             return;
        }
        
        CtrlPlaneHopContent nextHop = this.getNextExternalHop(pathInfo);
        if(nextHop == null){
            this.log.error("Recieved confirm event from " + producerID + 
                          " for a reservation with no hop past the local" +
                          " domain");
             return;
        }
        
        DomainDAO domainDAO = new DomainDAO(this.dbname);
        Domain nextDomain = domainDAO.getNextDomain(nextHop);
        if(nextDomain == null){
            this.log.error("The next hop in the given path is an unknown neighbor.");
            return;
        }else if(!nextDomain.getTopologyIdent().equals(producerID)){
            this.log.debug("The event is from " + producerID + " not the " +
                           "next domain " + nextDomain.getTopologyIdent() + 
                           " so discarding");
            return;
        }
        
        String status = this.se.getStatus(resv);
        if(!StateEngine.INCREATE.equals(status)){
            this.log.info("Trying to confirm a reservation that doesn't" + 
                          " have status " + StateEngine.INCREATE);
        }
        this.se.canUpdateLocalStatus(resv, 1);
        
        /* Submitting a job to the resource scheduling queue 
           so there aren't any resource conflicts */
        Scheduler sched = this.core.getScheduleManager().getScheduler();
        String jobName = "confirm-"+resv.hashCode();
        JobDetail jobDetail = new JobDetail(jobName, "SERIALIZE_RESOURCE_SCHEDULING", CreateReservationJob.class);
        this.log.debug("Adding job "+jobName);
        jobDetail.setDurability(true);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("confirm", true);
        jobDataMap.put("gri", gri);
        jobDataMap.put("pathInfo", pathInfo);
        jobDataMap.put("nextDomain", producerID);
        jobDetail.setJobDataMap(jobDataMap);
        try {
            sched.addJob(jobDetail, false);
        } catch (SchedulerException ex) {
            this.log.error("Unable to schedule confirm job");
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
        // one-to-one associations are in wrong direction to do cascading save
        // direction is so that all the various options don't have to have
        // foreign keys in the paths table
        if (resv.getPath().getLayer2Data() != null) {
            Layer2DataDAO layer2DataDAO = new Layer2DataDAO(this.dbname);
            layer2DataDAO.create(resv.getPath().getLayer2Data());
        } else if (resv.getPath().getLayer3Data() != null) {
            Layer3DataDAO layer3DataDAO = new Layer3DataDAO(this.dbname);
            layer3DataDAO.create(resv.getPath().getLayer3Data());
        }
        if (resv.getPath().getMplsData() != null) {
            MPLSDataDAO mplsDataDAO = new MPLSDataDAO(this.dbname);
            mplsDataDAO.create(resv.getPath().getMplsData());
        }
        if(resv.getToken() != null){
            TokenDAO tokenDAO = new TokenDAO(this.dbname);
            tokenDAO.create(resv.getToken());
        }
        this.log.info("store.finish");
        this.rsvLogger.stop();
    }

    /**
     * Given a reservation GRI, cancels the corresponding reservation.
     * (Can only cancel a pending or active reservation.)
     *
     * @param gri string with reservation global reservation id
     * @param login  string with login name of user
     *          if null any user's reservation may be canceled
     *          if set, only that user's reservation may canceled
     * @param institution string with institution of caller
     *          if null reservations from any site may be canceled
     *          if set only reservations starting or ending at that site may be canceled
     *
     * @return reservation with cancellation status
     * @throws BSSException
     */

    public Reservation cancel(String gri, String login, String institution)
            throws BSSException {

        this.rsvLogger.redirect(gri);
        this.log.info("cancel.start: " + gri + " login: " + login + " institution: " + institution );
        Reservation resv = getConstrainedResv(gri,login,institution);

        // See if we can cancel at all; this logic is DIFFERENT from the StateEngine
        String status = StateEngine.getStatus(resv);

        if (status.equals(StateEngine.CANCELLED)) {
            // a no-op; no need to complain though
            return resv;
        }

        String newStatus = "";
        if (status.equals(StateEngine.RESERVED) || status.equals(StateEngine.ACCEPTED)) {
            newStatus = StateEngine.CANCELLED;
        } else if (status.equals(StateEngine.ACTIVE)) {
            newStatus = StateEngine.INTEARDOWN;
        } else {
            throw new BSSException("Cannot cancel with status: "+status);
        }

        // remove all pending jobs in the scheduler related to this reservation
        this.core.getScheduleManager().processCancel(resv, newStatus);

        this.se.updateStatus(resv, newStatus);
        if (newStatus.equals(StateEngine.INTEARDOWN)) {
            // add the teardown jobs
            try {
                core.getPathSetupManager().teardown(resv, StateEngine.CANCELLED, true);
            } catch (PSSException ex) {
                this.log.error(ex);
                throw new BSSException(ex);
            }
        }
        this.log.info("cancel.finish: " + resv.getGlobalReservationId());
        this.rsvLogger.stop();
        return resv;
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
        Reservation resv = getConstrainedResv(gri,login,institution);
        this.log.info("query.finish: " + resv.getGlobalReservationId());
        return resv;
    }

    /**
     * Modifies the reservation, given a partially filled in reservation
     * instance and additional parameters.
     *
     * @param resv reservation instance modified in place
     * @param login string with login name
     *          if null any user's reservation may be modified
     *          if set, only that user's reservation may be modified
     * @param institution string with institution of caller
     *          if null reservations from any site may be modified
     *          if set only reservations starting or ending at that site may be modified
     * @param pathInfo contains either layer 2 or layer 3 info
     * @throws BSSException
     */
    public Reservation modify(Reservation resv, String login, String institution,
             PathInfo pathInfo)
            throws  BSSException {

        this.log.info("modify.start: login: " +  login + " institution: " +  institution ) ;

        String gri = resv.getGlobalReservationId();
        Reservation persistentResv = getConstrainedResv(gri,login,institution);
        // need to set this before validation
        // leave it the same, do not set to current user
        resv.setLogin(persistentResv.getLogin());

        ParamValidator paramValidator = new ParamValidator();

        StringBuilder errorMsg = paramValidator.validate(resv, pathInfo);
        if (errorMsg.length() > 0) {
            throw new BSSException(errorMsg.toString());
        }

        // handle times
        Long now = System.currentTimeMillis()/1000;

        if (persistentResv.getStatus().equals("FAILED")) {
            throw new BSSException("Cannot modify: reservation has already failed.");
        } else if (persistentResv.getStatus().equals("FINISHED")) {
            throw new BSSException("Cannot modify: reservation has already finished.");
        } else if (persistentResv.getStatus().equals("CANCELLED")) {
            throw new BSSException("Cannot modify: reservation has been cancelled.");
        } else if (persistentResv.getStatus().equals("INVALIDATED")) {
            throw new BSSException("Cannot modify: reservation has been invalidated.");
        } else if (persistentResv.getStatus().equals("ACTIVE")) {
            // we will silently not allow the user to modify the start time
            resv.setStartTime(persistentResv.getStartTime());
            // can't end before current time
            if (resv.getEndTime() < now) {
                resv.setEndTime(now);
            }
            // can't end before start
            if (resv.getStartTime() > resv.getEndTime()) {
                throw new BSSException("Cannot modify: end time before start.");
            }
        } else if (persistentResv.getStatus().equals("PENDING")) {
            if (resv.getEndTime() <= now) {
                throw new BSSException("Cannot modify: reservation ends before current time.");
            }
            // can't start before current time
            if (resv.getStartTime() < now) {
                resv.setStartTime(now);
            }
            if (resv.getStartTime() > resv.getEndTime()) {
                throw new BSSException("Cannot modify: start time after end time!");
            }
        }

        // If pathInfo is null that means we should keep the stored path
        if (pathInfo == null) {
            pathInfo = tc.getPathInfo(persistentResv);
            if (pathInfo == null) {
                throw new BSSException("No path provided or stored in DB for reservation "+resv.getGlobalReservationId());
            }
            pathInfo = tc.toLocalPathInfo(pathInfo);
        }
        // this will throw an exception if modification isn't possible
        this.log.info("PI SRC: " + pathInfo.getLayer2Info().getSrcEndpoint());
        Path path = this.getPath(resv, pathInfo);
        this.log.info("modify.finish");
        return persistentResv;
    }

    /**
     * Modifies the reservation, given a partially filled in reservation
     * instance and additional parameters.
     *
     * @param forwardReply response from the forwarded modify message
     * @param resv reservation instance modified in place
     * @throws BSSException
     */
    public Reservation finalizeModifyResv(ModifyResReply forwardReply, Reservation resv)
            throws  BSSException {

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
     * @param links a list of links. If not null / empty, results will only
     * include reservations whose path includes at least one of the links.
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
    public List<Reservation> list(String login, String institution,
        List<String> statuses, String description, List<Link> links,
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
        reservations = dao.list(loginIds, statuses, description, links,
                                vlanTags, startTime, endTime);

        if (institution == null){
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
     * Finds path between source and destination, checks to make sure
     * it wouldn't violate policy, and then finds the next domain, if any.
     *
     * @param resv partially filled in reservation, use startTime, endTime, bandWidth,
     * 	           GRI
     * @param pathInfo - input pathInfo,includes either layer2 or layer3 path
     *                  information, may also include explicit path hops.
     * @return a Path structure with the intradomain path hops, nextDomain, and
     *                  whether the path hops were explicitly set by the user.
     */
    public Path getPath(Reservation resv, PathInfo pathInfo)
            throws BSSException {

        boolean isExplicit = (pathInfo.getPath() != null);
        PathInfo intraPath = null;

        try {
            intraPath = this.pceMgr.findPath(pathInfo, resv);
        } catch (PathfinderException ex) {
            throw new BSSException(ex.getMessage());
        }

        if (intraPath == null || intraPath.getPath() == null) {
            throw new BSSException("Pathfinder could not find a path!");
        }
        
        //Convert any local hops that are still references to link objects
        this.log.info("PRE-EXPAND");
        for(CtrlPlaneHopContent hop : pathInfo.getPath().getHop()){
            if(hop.getLink() == null){ continue; }
            String vlans = hop.getLink().getSwitchingCapabilityDescriptors()
                                        .getSwitchingCapabilitySpecificInfo()
                                        .getVlanRangeAvailability();
            this.log.info(this.tc.hopToURN(hop) + "(" + vlans + ")");
        }
        this.expandLocalHops(pathInfo);
         this.log.info("POST-EXPAND");
         for(CtrlPlaneHopContent hop : pathInfo.getPath().getHop()){
            if(hop.getLink() == null){ continue; }
            String vlans = hop.getLink().getSwitchingCapabilityDescriptors()
                                        .getSwitchingCapabilitySpecificInfo()
                                        .getVlanRangeAvailability();
            this.log.info(this.tc.hopToURN(hop) + "(" + vlans + ")");
        }
        this.expandLocalHops(intraPath);
        
        ReservationDAO dao = new ReservationDAO(this.dbname);
        List<Reservation> reservations = dao.overlappingReservations(resv.getStartTime(), resv.getEndTime());
        this.policyMgr.checkOversubscribed(reservations, pathInfo, intraPath.getPath(), resv);
        
        Domain nextDomain = null;
        DomainDAO domainDAO = new DomainDAO(this.dbname);
        // get next external hop (first past egress) from the complete path
        CtrlPlaneHopContent nextExternalHop = this.getNextExternalHop(pathInfo);
        if (nextExternalHop != null){
            nextDomain = domainDAO.getNextDomain(nextExternalHop);
            if (nextDomain != null) {
                this.log.info("create.finish, next domain: " +
                          nextDomain.getUrl());
            } else {
                this.log.warn(
                        "Can't find domain url for nextExternalHop. Hop is: " +
                        this.tc.hopToURN(nextExternalHop));
            }
        }
        // convert to form for db
        Path path = this.convertPath(intraPath, pathInfo, nextDomain);
        path.setExplicit(isExplicit);
        return path;
    }
    
    /**
     * Expands any local linkIdRef elements in a given path and 
     * converts them to links
     *
     * @param pathInfo the pathInfo containg the path to expand
     * @throws BSSException
     */
    public void expandLocalHops(PathInfo pathInfo) throws BSSException{
        CtrlPlanePathContent path = pathInfo.getPath();
        CtrlPlanePathContent expandedPath = new CtrlPlanePathContent();
        DomainDAO domainDAO = new DomainDAO(this.dbname);
        if(path == null){
            throw new BSSException("Cannot expand path because no path given");
        }
        CtrlPlaneHopContent[] hops = path.getHop();
        if(hops == null || hops.length == 0 ){
            throw new BSSException("Cannot expand path because no hops given");
        }
        
        for(CtrlPlaneHopContent hop : hops){
            String urn = this.tc.hopToURN(hop);
            //if not link then add to path
            if(urn == null){
                throw new BSSException("Cannot expand path because " +
                                   "contains invalid hop.");
            }
            Hashtable<String, String> parseResults = URNParser.parseTopoIdent(urn);
            String hopType = parseResults.get("type");
            String domainId = parseResults.get("domainId");
            boolean isLocal = domainDAO.isLocal(domainId);
            if((!isLocal) || (isLocal && hop.getLink() != null)){
                expandedPath.addHop(hop);
                continue;
            }else if(isLocal && (!"link".equals(hopType))){
                throw new BSSException("Cannot expand hop because it contains " +
                                   "a hop that's not a link: " + urn);
            }
            
            Link dbLink = domainDAO.getFullyQualifiedLink(urn);
            CtrlPlaneHopContent expandedHop = new CtrlPlaneHopContent();
            CtrlPlaneLinkContent link = new CtrlPlaneLinkContent();
            L2SwitchingCapabilityData l2scData = dbLink.getL2SwitchingCapabilityData();
            CtrlPlaneSwcapContent swcap = new CtrlPlaneSwcapContent();
            CtrlPlaneSwitchingCapabilitySpecificInfo swcapInfo = new CtrlPlaneSwitchingCapabilitySpecificInfo();
            if(l2scData != null){
                swcapInfo.setInterfaceMTU(l2scData.getInterfaceMTU());
                swcapInfo.setVlanRangeAvailability(l2scData.getVlanRangeAvailability());
                swcap.setSwitchingcapType("l2sc");
                swcap.setEncodingType("ethernet");
            }else{
                //TODO: What does esnet use for internal links?
                swcapInfo.setCapability("unimplemented");
                swcap.setSwitchingcapType(DEFAULT_SWCAP_TYPE);
                swcap.setEncodingType(DEFAULT_ENC_TYPE);
            }
            swcap.setSwitchingCapabilitySpecificInfo(swcapInfo);
            link.setId(urn);
            link.setTrafficEngineeringMetric(dbLink.getTrafficEngineeringMetric());
            link.setSwitchingCapabilityDescriptors(swcap);
                
            expandedHop.setId(hop.getId());
            expandedHop.setLink(link);
            expandedPath.setId(path.getId());
            expandedPath.addHop(expandedHop);
            
        }
        
        pathInfo.setPath(expandedPath);
    }

    /**
     * Finds the intradomain ingress link and returns a Path containing only
     * that link. This is needed for initially holding the reservation to
     * meet the database schema requirments.
     *
     * @param pathInfo the PathInfo element to analyze
     * @return the Path containing only the ingress link
     */
    public Path buildInitialPath(PathInfo pathInfo) throws BSSException{
        this.log.debug("buildInitialPath.start");
        Layer2Info layer2Info = pathInfo.getLayer2Info();
        Layer3Info layer3Info = pathInfo.getLayer3Info();
        MplsInfo mplsInfo = pathInfo.getMplsInfo();
        Path path = new Path();
        PathElem elem = new PathElem();
        DomainDAO domainDAO = new DomainDAO(this.dbname);
        String ingressLink = null;
        Link link = null;
        this.log.debug("buildInitialPath.pathInfo=" + pathInfo.hashCode());
        PathInfo refOnlyPathInfo = this.tc.createRefPath(pathInfo);
        
        //Build path containing only the ingress link id
        try{
            ingressLink = this.pceMgr.findIngress(refOnlyPathInfo);
        }catch(PathfinderException e){
            throw new BSSException(e.getMessage());
        }
        this.log.debug("ingress=" + ingressLink);
        link = domainDAO.getFullyQualifiedLink(ingressLink);
        elem.setLink(link);
        path.setPathElem(elem);
        String pathSetupMode = pathInfo.getPathSetupMode();
        if (pathSetupMode == null) {
            pathSetupMode = "timer-automatic";
        } 
        if ( pathSetupMode.equals("timer-automatic")  || pathSetupMode.equals("signal-xml")) {
            path.setPathSetupMode(pathSetupMode);
        } else {
                this.log.error("invalid pathSetupMode input: " + pathSetupMode);
                path.setPathSetupMode("timer-automatic");
        }

        //Convert layer2/layer3/mplsInfo to Hibernate beans
        path.setLayer2Data(this.tc.layer2InfoToData(layer2Info));
        path.setLayer3Data(this.tc.layer3InfoToData(layer3Info));
        path.setMplsData(this.tc.mplsInfoToData(mplsInfo));

        this.log.debug("buildInitialPath.end");
        return path;
    }

    /**
     * Converts the intradomain and interdomain paths in Axis2 data structure into
     * database Path class containing both.
     *
     * @param intraPathInfo PathInfo instance (Axis2 type) with filled in info
     * @param interPathInfo PathInfo instance (Axis2 type) with filled in info
     * @param nextDomain Domain instance with information about next domain
     * @return path Path in database format
     */
    public Path convertPath(PathInfo intraPathInfo, PathInfo interPathInfo,
        Domain nextDomain) throws BSSException {

        Link link = null;
        Link srcLink = null;
        Link destLink = null;
        boolean foundIngress = false;
        PathElem lastElem = null;
        ArrayList<String> linksList = new ArrayList<String>();

        this.log.info("convertPath.start");
        DomainDAO domainDAO = new DomainDAO(this.dbname);
        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);

        Layer2Info layer2Info = null;

        Layer3Info layer3Info = null;
        String pathSetupMode = null;

        if (interPathInfo != null) {
            layer2Info = interPathInfo.getLayer2Info();
            layer3Info = interPathInfo.getLayer3Info();
            pathSetupMode = interPathInfo.getPathSetupMode();
        }

        if (layer2Info != null) {
            srcLink = domainDAO.getFullyQualifiedLink(layer2Info.getSrcEndpoint());
            destLink = domainDAO.getFullyQualifiedLink(layer2Info.getDestEndpoint());
        }

        Path path = new Path();
        path.setNextDomain(nextDomain);

        CtrlPlanePathContent intraPath = null;
        CtrlPlaneHopContent[] hops = new CtrlPlaneHopContent[0];
        if (intraPathInfo != null) {
            intraPath = intraPathInfo.getPath();
            hops = intraPath.getHop();
        }

        List<PathElem> pathElems = new ArrayList<PathElem>();
        
        // set pathSetupMode, default to timer-automatic
        if (pathSetupMode == null) {
            pathSetupMode = "timer-automatic";
        }
        path.setPathSetupMode(pathSetupMode);

        for (int i = 0; i < hops.length; i++) {
            String hopTopoId = this.tc.hopToURN(hops[i]);
            this.log.debug("convertPath: converting link: "+hopTopoId);
            Hashtable<String, String> parseResults = URNParser.parseTopoIdent(hopTopoId);
            String hopType = parseResults.get("type");
            String domainId = parseResults.get("domainId");

            // can't store non-local addresses
            if (!hopType.equals("link") || !domainDAO.isLocal(domainId)) {
                this.log.debug("convertPath: not a local link");
                continue;
            }

            // check for duplicate hops in our local portion of the path
            String fqti = parseResults.get("fqti");
            if (linksList.contains(fqti)) {
                throw new BSSException("Duplicate hop in path: ["+fqti+"]");
            } else {
                linksList.add(fqti);
            }
            link = domainDAO.getFullyQualifiedLink(hopTopoId);
            if (link == null) {
                this.log.error("Couldn't find link in db for: ["+this.tc.hopToURN(hops[i])+"]");
                throw new BSSException("Couldn't find link in db for: ["+this.tc.hopToURN(hops[i])+"]");
            } else if (!link.isValid()) {
                this.log.error("Link is invalid in db for: ["+this.tc.hopToURN(hops[i])+"]");
                throw new BSSException("Link is invalid in db for: ["+this.tc.hopToURN(hops[i])+"]");
            } else {
                this.log.debug("Found link in db for: ["+this.tc.hopToURN(hops[i])+"]");
            }
            PathElem pathElem = new PathElem();
            if (!foundIngress) {
                if (layer2Info != null) {
                    this.log.debug("convertPath: found first L2 local link, setting to ingress");
                    pathElem.setDescription("ingress");
                    foundIngress = true;
                } else {
                    // layer 3 currently requires a Juniper ingress
                    net.es.oscars.pathfinder.Utils utils =
                        new net.es.oscars.pathfinder.Utils(this.dbname);
                    // assumes one to one relationship
                    Ipaddr ipaddr = ipaddrDAO.queryByParam("linkId", link.getId());
                    if (ipaddr == null) {
                        throw new BSSException("no IP address associated with link " + link.getId());
                    } else if (!ipaddr.isValid()) {
                        throw new BSSException("IP address associated with link " + link.getId() + " is not valid");
                    }
                    try {
                        String ip = utils.getLoopback(ipaddr.getIP(), "Juniper");
                        if (ip != null) {
                            pathElem.setDescription("ingress");
                            foundIngress = true;
                        }
                    } catch (PathfinderException e) {
                        throw new BSSException(e.getMessage());
                    }
                }
            }
            pathElem.setLink(link);

            if(layer2Info != null &&
                link.getL2SwitchingCapabilityData() != null &&
                nextDomain == null) {
                this.setL2LinkDescr(pathElem, hops[i]);
            } else if (nextDomain != null) {
                this.log.info("next domain is NOT NULL, not setting up VLAN tags now");
            } else if (link.getL2SwitchingCapabilityData() == null) {
                this.log.info("L2 switching capability data is NULL, can't set up VLAN tags ");
            } else if (layer2Info == null) {
                this.log.info("layer 2 info is NULL");
            }
            pathElems.add(pathElem);
            lastElem = pathElem;
        }

        if (!foundIngress) {
            this.log.error("No valid ingress router in path");
            throw new BSSException("No valid ingress router in path");
        }
        if (lastElem == null) {
            this.log.error("No local hops in path");
            throw new BSSException("No local hops in path");
        }

        lastElem.setDescription("egress");
        // set start to first element
        path.setPathElem(pathElems.get(0));

        // set the next element for each element
        for (int i = 0; i < pathElems.size()-1; i++) {
            pathElems.get(i).setNextElem(pathElems.get(i+1));
        }

        if (layer2Info != null) {
            Layer2Data dbLayer2Data = new Layer2Data();
            dbLayer2Data.setSrcEndpoint(layer2Info.getSrcEndpoint());
            dbLayer2Data.setDestEndpoint(layer2Info.getDestEndpoint());
            path.setLayer2Data(dbLayer2Data);
        } else if (layer3Info != null) {
            Layer3Data dbLayer3Data = new Layer3Data();
            dbLayer3Data.setSrcHost(layer3Info.getSrcHost());
            dbLayer3Data.setDestHost(layer3Info.getDestHost());
            dbLayer3Data.setSrcIpPort(layer3Info.getSrcIpPort());
            dbLayer3Data.setDestIpPort(layer3Info.getDestIpPort());
            dbLayer3Data.setProtocol(layer3Info.getProtocol());
            dbLayer3Data.setDscp(layer3Info.getDscp());
            path.setLayer3Data(dbLayer3Data);
        }
        MplsInfo mplsInfo = interPathInfo.getMplsInfo();
        if (mplsInfo != null) {
            MPLSData dbMplsData = new MPLSData();
            Long burstLimit = new Long(
                    Integer.valueOf(mplsInfo.getBurstLimit()).longValue());
            dbMplsData.setBurstLimit(burstLimit);
            dbMplsData.setLspClass(mplsInfo.getLspClass());
            path.setMplsData(dbMplsData);
        }
        this.log.debug("convertPath.end");
        return path;
    }

    /**
     * Stores the interdomain path. The interdomain path is stored primarily
     * for reporting purpose so there are not quite as many requirements as
     * for the intradomain path.
     *
     * @param pathInfo the pathInfo element containing the interdomain path
     * @return first PathElem of interdomain path
     * @throws BSSException
     */
    public PathElem convertInterPath(PathInfo pathInfo) throws BSSException{
        CtrlPlanePathContent path = pathInfo.getPath();
        if(path == null){ return null; }
        CtrlPlaneHopContent[] hops = path.getHop();
        PathElem prevPathElem = null;
        PathElem currPathElem = null;

        for(int i = (hops.length - 1); i >= 0; i--){
            String urn = this.tc.hopToURN(hops[i]);
            Link link = TopologyUtil.getLink(urn, this.dbname);
            currPathElem = new PathElem();
            currPathElem.setLink(link);
            currPathElem.setNextElem(prevPathElem);
            prevPathElem = currPathElem;
        }
        return currPathElem;
    }

    /**
     * Make a copy of either just the internal hops or all the hops on the path
     * depending on the value of the exclude boolean.
     *
     * @param pathInfo a PathInfo instance containing a path
     * @param exclude boolean indicating whether to exclude internal hops
     * @return pathCopy a CtrlPlanePathContent instance with the copied path
     */
    public CtrlPlanePathContent copyPath(PathInfo pathInfo, boolean exclude) {
        boolean edgeFound = false;
        CtrlPlaneHopContent prevHop = null;

        DomainDAO domainDAO = new DomainDAO(this.dbname);
        CtrlPlanePathContent pathCopy = new CtrlPlanePathContent();
        CtrlPlanePathContent ctrlPlanePath = pathInfo.getPath();

        if (ctrlPlanePath == null) {
            return null;
        }
        String pathId = "unimplemented";
        if (ctrlPlanePath.getId() != null && !ctrlPlanePath.getId().equals("")) {
            pathId = ctrlPlanePath.getId();
        }
        pathCopy.setId(pathId);

        CtrlPlaneHopContent[] hops = ctrlPlanePath.getHop();
        for (int i = 0; i < hops.length; i++) {
            CtrlPlaneHopContent hopCopy = new CtrlPlaneHopContent();
            if (!exclude) {
                hopCopy.setId(hops[i].getId());
                hopCopy.setLinkIdRef(hops[i].getLinkIdRef());
                hopCopy.setPortIdRef(hops[i].getPortIdRef());
                hopCopy.setNodeIdRef(hops[i].getNodeIdRef());
                hopCopy.setDomainIdRef(hops[i].getDomainIdRef());
                hopCopy.setLink(hops[i].getLink());
                hopCopy.setPort(hops[i].getPort());
                hopCopy.setNode(hops[i].getNode());
                hopCopy.setDomain(hops[i].getDomain());
                pathCopy.addHop(hopCopy);
                continue;
            }
            String hopTopoId = this.tc.hopToURN(hops[i]);
            Hashtable<String, String> parseResults = URNParser.parseTopoIdent(hopTopoId);
            String hopType = parseResults.get("type");
            String domainId = parseResults.get("domainId");

            if (hopType.equals("link") &&  domainDAO.isLocal(domainId)) {
                // add ingress
                if (!edgeFound || i == (hops.length - 1)) {
                    hopCopy.setId(hops[i].getId());
                    hopCopy.setLinkIdRef(hops[i].getLinkIdRef());
                    hopCopy.setPortIdRef(hops[i].getPortIdRef());
                    hopCopy.setNodeIdRef(hops[i].getNodeIdRef());
                    hopCopy.setDomainIdRef(hops[i].getDomainIdRef());
                    hopCopy.setLink(hops[i].getLink());
                    hopCopy.setPort(hops[i].getPort());
                    hopCopy.setNode(hops[i].getNode());
                    hopCopy.setDomain(hops[i].getDomain());
                    pathCopy.addHop(hopCopy);
                    edgeFound = true;
                }
                prevHop = hops[i];
                continue;
            } else if (edgeFound) {
                // add egress
                CtrlPlaneHopContent hopCopy2 = new CtrlPlaneHopContent();
                hopCopy2.setId(prevHop.getId());
                hopCopy2.setLinkIdRef(prevHop.getLinkIdRef());
                hopCopy2.setPortIdRef(prevHop.getPortIdRef());
                hopCopy2.setNodeIdRef(prevHop.getNodeIdRef());
                hopCopy2.setDomainIdRef(prevHop.getDomainIdRef());
                hopCopy2.setLink(prevHop.getLink());
                hopCopy2.setPort(prevHop.getPort());
                hopCopy2.setNode(prevHop.getNode());
                hopCopy2.setDomain(prevHop.getDomain());
                pathCopy.addHop(hopCopy2);
                edgeFound = false;
            }
            hopCopy.setId(hops[i].getId());
            hopCopy.setLinkIdRef(hops[i].getLinkIdRef());
            hopCopy.setPortIdRef(hops[i].getPortIdRef());
            hopCopy.setNodeIdRef(hops[i].getNodeIdRef());
            hopCopy.setDomainIdRef(hops[i].getDomainIdRef());
            hopCopy.setLink(hops[i].getLink());
            hopCopy.setPort(hops[i].getPort());
            hopCopy.setNode(hops[i].getNode());
            hopCopy.setDomain(hops[i].getDomain());
            pathCopy.addHop(hopCopy);
            prevHop = hops[i];
        }
        return pathCopy;
    }

    /**
     * Given a PathInfo instance with the complete path, find the
     * first hop outside the local domain.
     *
     * @param pathInfo PathInfo instance containing path
     * @return hop CtrlPlaneHopContent instance with hop in next domain
     */
    public CtrlPlaneHopContent getNextExternalHop(PathInfo pathInfo) {

        CtrlPlaneHopContent nextHop = null;
        boolean hopFound = false;

        this.log.debug("getNextExternalHop.start");
        DomainDAO domainDAO = new DomainDAO(this.dbname);
        CtrlPlanePathContent ctrlPlanePath = pathInfo.getPath();
        CtrlPlaneHopContent[] hops = ctrlPlanePath.getHop();
        for (int i = 0; i < hops.length; i++) {
            String hopTopoId = this.tc.hopToURN(hops[i]);
            Hashtable<String, String> parseResults = URNParser.parseTopoIdent(hopTopoId);
            String hopType = parseResults.get("type");
            String domainId = parseResults.get("domainId");
            if (!hopType.equals("link") || !domainDAO.isLocal(domainId)) {
                if (hopFound) {
                    nextHop = hops[i];
                    break;
                }
            } else {
                hopFound = true;
            }
        }
        this.log.debug("getNextExternalHop.end");
        return nextHop;
    }

    /**
     * Returns IP address associated with host.
     *
     * @param host string with either host name or IP address
     * @return string with IP address
     */
    public String getIpAddress(String host) throws BSSException {
        InetAddress addr = null;
        try {
            addr = InetAddress.getByName(host);
        } catch (UnknownHostException ex) {
            throw new BSSException(ex.getMessage());
        }
        // returns same value if already an IP address
        return addr.getHostAddress();
    }

    /**
     * Returns the name of the institution of the an end point of the reservation
     *
     * @param resv Reservation for which we want to find an end point institution
     * @param boolean source: true returns the source , false returns the destination
     * @return institution String name of the end point
     */
    public String endPointSite(Reservation resv, Boolean source) {
        Path path = resv.getPath();
        PathElem hop = path.getPathElem();
        if (!source) { // get last hop
            while (hop.getNextElem() != null) {
                hop = hop.getNextElem();
            }
        }
        Link endPoint = hop.getLink();
        Link remoteLink = endPoint.getRemoteLink();
        String institutionName = "UNKNOWN";
        Domain endDomain = null;
        // String FQTI = null;
        if (remoteLink != null) {
            endDomain = remoteLink.getPort().getNode().getDomain();
            // FQTI=remoteLink.getFQTI();
            // this.log.debug("remote link: " + FQTI + " domain: " + endDomain.getTopologyIdent() );
        } else {
            endDomain = endPoint.getPort().getNode().getDomain();
            // FQTI=endPoint.getFQTI();
            // this.log.debug("endPoint link is: " + FQTI + " domain: " + endDomain.getTopologyIdent());
        }

        if (endDomain != null ){
            Site institution = endDomain.getSite();
            if (institution != null) {
                institutionName = institution.getName();
            }
        }
        return institutionName;
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
     * Sets VLAN tag on a pathElemenet.
     *
     * @param pathElem path element to be updated
     * @param hop the hop with the link information
     */
    public void setL2LinkDescr(PathElem pathElem, CtrlPlaneHopContent hop){
        CtrlPlaneLinkContent link = hop.getLink();
        if(link == null){
            return;
        }
        CtrlPlaneSwitchingCapabilitySpecificInfo swcapInfo = 
                                    link.getSwitchingCapabilityDescriptors()
                                        .getSwitchingCapabilitySpecificInfo();
        String vlanRange = swcapInfo.getVlanRangeAvailability();
        if("0".equals(vlanRange)){//untagged
            pathElem.setLinkDescr("0");
        }else{
            pathElem.setLinkDescr(swcapInfo.getSuggestedVLANRange());
        }
    }
    
    /**
     * Picks a VLAN tag from a range of VLANS given a suggested VLAN Range
     *
     * @param mask the range to choose from
     * @param suggested a suggested range to try first
     * @return the chosen VLAN as a string
     * @throws BSSException
     */
    public String chooseVlanTag(byte[] mask, byte[] suggested) throws BSSException{
        //Never pick untagged
        mask[0] &= (byte) 127;
        suggested[0] &= (byte) 127;
        //Try suggested
        for(int i=0; i < suggested.length; i++){
            suggested[i] &= mask[i];
        }
        String remaining = this.tc.maskToRangeString(suggested);
        if(!"".equals(remaining)){
            mask = suggested;
        }
        return this.chooseVlanTag(mask);
    }
    
    /**
     * Picks a VLAN tag from a range of VLANS
     *
     * @param mask the range to choose from
     * @return the chosen VLAN as a string
     * @throws BSSException
     */
    public String chooseVlanTag(byte[] mask) throws BSSException{
        //Never pick untagged
        mask[0] &= (byte) 127;
        
        //pick one
        ArrayList<Integer> vlanPool = new ArrayList<Integer>();
        for(int i=0; i < mask.length; i++){
            for(int j = 0; j < 8; j++){
                int tag = i*8 + j;
                if((mask[i] & (int)Math.pow(2, (7-j))) > 0){
                    vlanPool.add(tag);
                }
            }
        }
        
        int index = 0;
        if(vlanPool.size() > 1){
            Random rand = new Random();
            index = rand.nextInt(vlanPool.size()-1);
        }
        
        return vlanPool.get(index).toString();
    }

    /**
     * Makes final changes to reservation before storage in database.
     * Stores token and vlan tags if they are returned by forwardReply
     * Stores the interdomain path elements returned by forwardReply
     *
     * @param forwardReply response from forward request
     * @param resv reservation to be stored in database
     * @param pathInfo reservation path information
     */
    public void finalizeResv(Reservation resv, PathInfo pathInfo, 
                            PathInfo fwdPathInfo) throws BSSException{
        Layer2Info layer2Info = pathInfo.getLayer2Info();
        String pathSetupMode = pathInfo.getPathSetupMode();
        Path path = resv.getPath();

        // if user signaled and last domain create token, otherwise store
        // token returned in forwardReply
        /* if (pathSetupMode == null || pathSetupMode.equals("signal-xml")) {
            this.generateToken(forwardReply, resv);
        } */
        if (layer2Info != null && fwdPathInfo != null && 
            fwdPathInfo.getLayer2Info() != null) {

            this.log.info("setting up vtags");
            DomainDAO domainDAO = new DomainDAO(this.dbname);
            Link srcLink = domainDAO.getFullyQualifiedLink(layer2Info.getSrcEndpoint());
            Link destLink = domainDAO.getFullyQualifiedLink(layer2Info.getDestEndpoint());
            VlanTag srcVtag = fwdPathInfo.getLayer2Info().getSrcVtag();
            VlanTag destVtag = fwdPathInfo.getLayer2Info().getDestVtag();
            this.log.info("src vtag: " + srcVtag);
            this.log.info("dest vtag: " + destVtag);
            layer2Info.setSrcVtag(srcVtag);
            layer2Info.setDestVtag(destVtag);

            /* update VLANs in intradomain path */
            PathElem pathElem = path.getPathElem();
            while (pathElem != null) {
                if (pathElem.getLink().getL2SwitchingCapabilityData() != null){
                    this.log.info(pathElem.getLink().getTopologyIdent());
                    //TODO: Support VLAN mapping
                    //this.setL2LinkDescr(pathElem, srcLink, destLink,
                    //                       pathElem.getLink(), layer2Info);
                } else {
                    this.log.info("no switching capability data for: " + pathElem.getLink().getTopologyIdent());
                }
                pathElem = pathElem.getNextElem();
            }

            /* Store interdomain path */
            try {
                PathElem interPathElem = this.convertInterPath(fwdPathInfo);
                path.setInterPathElem(interPathElem);
            } catch(BSSException e) {
                /* Catch error when try to store path with links not in the
                   database. Perhaps in the future this will be an error but
                   until everyone shares topology we can relax this requirement
                 */
                this.log.info("Unable to store interdomain path. " +
                    e.getMessage());
            }
        } else {
            this.log.info("not setting up vtags");
            if (layer2Info == null) {
                this.log.info("because no layer 2 info");
            }
            if (fwdPathInfo == null) {
                this.log.info("because no fwdPathInfo");
            } else if (fwdPathInfo.getLayer2Info() == null) {
                this.log.info("because no fwdPathInfo.getLayer2Info()");
            } else {
                this.log.info("unknown error");
            }
            /* Store version of path in terms of interdomain hops. Still want to do
               this even if a local path because the intradomain version may be private.
               For current implementations the intradomain URNs and interdomain URNs will
               be the same but that won't be true in the future so we need two versions
               of every path*/
            try {
                PathElem interPathElem = this.convertInterPath(pathInfo);
                path.setInterPathElem(interPathElem);
            } catch(BSSException e) {
                /* Catch error when try to store path with links not in the
                   database. Perhaps in the future this will be an error but
                   until everyone shares topology we can relax this requirement
                 */
                this.log.info("Unable to store interdomain path. " +
                    e.getMessage());
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
            try{
                tokenKey = TokenKey.generateTokenKey(gri);
                tokenValue = TokenBuilder.getXMLTokenValue(gri, tokenKey);
                this.log.info("token=" + tokenValue);
            }catch(Exception e){
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

    public Boolean checkInstitution(Reservation resv, String institution) {
        // get the site associated the source of the reservation
        String sourceSite = this.endPointSite(resv, true);
        // this.log.debug("checkInstitution: sourceSite is " + sourceSite);
        if (sourceSite.equals(institution)) {
            return true;
        } else {
            // get the site associated the destination of the reservation
            String destSite = this.endPointSite(resv,false);
            // this.log.debug("checkInstitution: destinationSite is " + destSite);
            if (destSite.equals(institution)){
                return true;
            }
        }
        return false;
    }

    /**
     *  finds the reservations that matches all the constraints
     *
     *  @param gri String global reservation Id identifies the reservation
     *  @param String loginConstraint = reservation must be owned by this login
     *  @param String institutionConstraint - reservation must belong to this institution
     *
     *  @return Reservation - a reservation that meets the constraint,
     *  @throws PSSException if no such reservation exists
     *
     */
   private Reservation getConstrainedResv(String gri, String loginConstraint,
               String institutionConstraint)  throws BSSException {

       ReservationDAO resvDAO = new ReservationDAO(this.dbname);
       Reservation resv = null;

       if (loginConstraint == null ) {
           try {
              resv = resvDAO.query(gri);
           } catch ( BSSException e ){
               this.log.error("No reservation matches gri: " + gri);
                  throw new BSSException("No reservations match request");
           }
           if (institutionConstraint != null) {
              //check the institution
              if (!this.checkInstitution(resv, institutionConstraint)) {
                  resv = null;
              }
           }
       }
       else  {
           resv = resvDAO.queryByGRIAndLogin(gri, loginConstraint);
       }
       if (resv == null) {
       this.log.error("No reservation matches gri: " + gri + " login: " +
           loginConstraint + " institution: " + institutionConstraint);
           throw new BSSException("No reservations match request");
       }
       return resv;
   }
}
