package net.es.oscars.bss;

import java.util.*;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.*;
import org.hibernate.*;

import org.aaaarch.gaaapi.tvs.TokenBuilder;
import org.aaaarch.gaaapi.tvs.TokenKey;

import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlanePathContent;
import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlaneHopContent;

import net.es.oscars.PropHandler;
import net.es.oscars.oscars.TypeConverter;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.bss.topology.*;
import net.es.oscars.pathfinder.*;
import net.es.oscars.notify.*;
import net.es.oscars.database.HibernateUtil;



/**
 * ReservationManager handles all networking and data access object calls
 * necessary to create, update, delete, and modify reservations.
 *
 * @author David Robertson, Mary Thompson, Jason Lee
 */
public class ReservationManager {
    private Logger log;
    private NotifyInitializer notifier;
    private PCEManager pceMgr;
    private PolicyManager policyMgr;
    private TypeConverter tc;
    private String dbname;
    private ReservationLogger rsvLogger;

    /** Constructor. */
    public ReservationManager(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.rsvLogger = new ReservationLogger(this.log);
        this.pceMgr = new PCEManager(dbname);
        this.policyMgr = new PolicyManager(dbname);
        this.tc = new TypeConverter();
        this.dbname = dbname;
        this.notifier = new NotifyInitializer();
        try {
            this.notifier.init();
        } catch (NotifyException ex) {
            this.log.error("*** COULD NOT INITIALIZE NOTIFIER ***");
            // TODO:  ReservationAdapter, ReservationManager, etc. will
            // have init methods that throw exceptions that will not be
            // ignored if NotifyInitializer cannot be created.  Don't
            // want exceptions in constructor
        }
    }

    /**
     * Creates the reservation, given a partially filled in reservation
     * instance and additional parameters.
     *
     * @param resv reservation instance modified in place
     * @param login string with login name
     * @param pathInfo contains either layer 2 or layer 3 info
     * @throws BSSException
     */
    public void create(Reservation resv, String login, PathInfo pathInfo)
            throws  BSSException {

        ParamValidator paramValidator = new ParamValidator();

        this.log.info("create.start");
        // login is checked in validate so set it here
        resv.setLogin(login);

        //set GRI if none specified
        if (resv.getGlobalReservationId() == null){
            String gri = this.generateGRI();
            resv.setGlobalReservationId(gri);
        } else {
            ReservationDAO dao = new ReservationDAO(this.dbname);
            Reservation tmp = dao.query(resv.getGlobalReservationId());
            if (tmp != null) {
                throw new BSSException("Reservation with gri: "+resv.getGlobalReservationId()+" already exists!");
            }

        }
        this.rsvLogger.redirect(resv.getGlobalReservationId());

        // so far just validation for create
        StringBuilder errorMsg =
                paramValidator.validate(resv, pathInfo);
        if (errorMsg.length() > 0) {
            throw new BSSException(errorMsg.toString());
        }

        this.log.info("create.validated");
        resv.setStatus("PENDING");
        CtrlPlanePathContent pathCopy = null;

        // this modifies the path to include internal hops with layer 2,
        // and finds the complete path with traceroute
        Path path = this.getPath(resv, pathInfo);
        resv.setPath(path);
        long seconds = System.currentTimeMillis()/1000;
        resv.setCreatedTime(seconds);

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
        this.log.info("create.finish");
        this.rsvLogger.stop();
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
     * @return reservation with cancellation status
     * @throws BSSException
     */
    public Reservation cancel(String gri, String login, boolean allUsers)
            throws BSSException {

        this.rsvLogger.redirect(gri);
        ReservationDAO dao = new ReservationDAO(this.dbname);
        String newStatus = null;

        this.log.info("cancel.start: " + gri + " login: " + login + " allUsers is " + allUsers);

        Reservation resv = dao.query(gri);
        if (resv == null) {
            throw new BSSException(
                "No current reservation with GRI: " + gri);
        }
        if (!allUsers) {
            if  (!resv.getLogin().equals(login)) {
                throw new BSSException ("cancel reservation: permission denied");
            }
        }
        String prevStatus = resv.getStatus();
        if (prevStatus.equals("FINISHED")) {
            throw new BSSException(
               "Trying to cancel a finished reservation");
        }
        if (prevStatus.equals("FAILED")) {
            throw new BSSException(
               "Trying to cancel a failed reservation");
        }
        if (prevStatus.equals("CANCELLED")) {
            throw new BSSException(
               "Trying to cancel an already cancelled reservation");
        }
        if (prevStatus.equals("ACTIVE")) {
            newStatus = "PRECANCEL";
        } else {
            newStatus = "CANCELLED";
        }
        // note that this is not persisted until any forward domains
        // are also contacted
        resv.setStatus(newStatus);
        this.log.info("cancel.finish: " + resv.getGlobalReservationId());
        this.rsvLogger.stop();
        return resv;
    }

    /**
     * Handles final status of cancellation after possible forwarding.
     * Assume that permission exists or we would not have got this far
     *
     * @param resv Reservation instance
     * @param status string with final cancel status
     * @throws BSSException
     */
    public void finalizeCancel(Reservation resv, String status)
            throws BSSException {

        this.rsvLogger.redirect(resv.getGlobalReservationId());
        this.log.info("finalizeCancel.start");
        String gri = resv.getGlobalReservationId();

        Map<String,String> messageInfo = new HashMap<String,String>();
        messageInfo.put("subject",
                "Reservation " + resv.getGlobalReservationId() + " cancelled");
        messageInfo.put("body",
            "Reservation cancelled.\n" + resv.toString(this.dbname));
        messageInfo.put("alertLine", resv.getDescription());
        NotifierSource observable = this.notifier.getSource();
        Object obj = (Object) messageInfo;
        observable.eventOccured(obj);
        this.log.info("finalizeCancel.finish: " +
                      resv.getGlobalReservationId());
        this.rsvLogger.stop();
    }


    /**
     * Given a reservation GRI, queries the database and returns the
     *     corresponding reservation instance.
     *
     * @param gri string with reservation GRI
     * @param login string with login name of the caller
     * @param allUsers boolean indicating user can view reservations for all users
     * @return resv corresponding reservation instance, if any
     * @throws BSSException
     */
    public Reservation query(String gri, String login, boolean allUsers)
            throws BSSException {

        Reservation resv = null;

        this.log.info("query.start: " + gri + " login: " + login + " allUsers is " + allUsers);
        ReservationDAO dao = new ReservationDAO(this.dbname);
        resv = dao.query(gri);
        if (resv == null) {
            throw new BSSException("Reservation not found: " + gri);
        }
        if (!allUsers) {
            this.log.debug("reservation login is " + resv.getLogin());
            if  (!resv.getLogin().equals(login)) {
                throw new BSSException ("query reservation: permission denied");
            }
        }
        this.log.info("query.finish: " + resv.getGlobalReservationId());
        return resv;
    }



    /**
     * Modifies the reservation, given a partially filled in reservation
     * instance and additional parameters.
     *
     * @param resv reservation instance modified in place
     * @param login string with login name
     * @param pathInfo contains either layer 2 or layer 3 info
     * @throws BSSException
     */
    public Reservation modify(Reservation resv, String login, PathInfo pathInfo)
            throws  BSSException {
        this.log.info("modify.start");

        // need to set this
        resv.setLogin(login);

        ParamValidator paramValidator = new ParamValidator();

        StringBuilder errorMsg =
            paramValidator.validate(resv, pathInfo);
        if (errorMsg.length() > 0) {
            throw new BSSException(errorMsg.toString());
        }


        ReservationDAO resvDAO = new ReservationDAO(this.dbname);

        Reservation persistentResv = resvDAO.query(resv.getGlobalReservationId());
        if (persistentResv == null) {
            throw new BSSException("Could not locate reservation to modify, GRI: "+resv.getGlobalReservationId());
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


        // this will throw an exception if modification isn't possible
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
     * @param pathInfo contains either layer 2 or layer 3 info
     * @throws BSSException
     */
    public Reservation finalizeModifyResv(ModifyResReply forwardReply, Reservation resv, PathInfo pathInfo)
            throws  BSSException {

        this.log.info("finalizeModify.start");
        ReservationDAO resvDAO = new ReservationDAO(this.dbname);

        Reservation persistentResv = resvDAO.query(resv.getGlobalReservationId());
        if (persistentResv == null) {
            throw new BSSException("Could not locate reservation to finalize modify, GRI: "+resv.getGlobalReservationId());
        }
        // this will throw an exception if modification isn't possible
        Path path = this.getPath(resv, pathInfo);

        // TODO: check if the new path is different from the old path..

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
     *
     * @param loginIds a list of user logins. If not null or empty, results will
     * only include reservations submitted by these specific users.
     * If null / empty results will include reservations by all users.
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
     * @return reservations list of reservations
     * @throws BSSException
     */
    public List<Reservation> list(String login, List<String> loginIds,
        List<String> statuses, String description, List<Link> links,
        List<String> vlanTags,  Long startTime, Long endTime)
                throws BSSException {

        List<Reservation> reservations = null;

        this.log.info("list.start, login: " + login);

        ReservationDAO dao = new ReservationDAO(this.dbname);
        reservations = dao.list(loginIds, statuses, description, links,
                                vlanTags, startTime, endTime);
        this.log.info("list.finish, success");

        return reservations;
    }

    /**
     * Finds path between source and destination, checks to make sure
     * it wouldn't violate policy, and then finds the next domain, if any.
     *
     * @param resv partially filled in resvervation, use startTime, endTime, bandWidth,
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

        Long bandwidth = resv.getBandwidth();
        ReservationDAO dao = new ReservationDAO(this.dbname);
        List<Reservation> reservations = dao.overlappingReservations(resv.getStartTime(), resv.getEndTime());
        this.policyMgr.checkOversubscribed(reservations, pathInfo,
                                           intraPath.getPath(), resv);
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
                        nextExternalHop.getId());
            }
        }
        // convert to form for db
        Path path = this.convertPath(intraPath, pathInfo, nextDomain);
        path.setExplicit(isExplicit);
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
        String description = null;
        boolean foundIngress = false;
        PathElem lastElem = null;

        ArrayList<String> linksList = new ArrayList<String>();

        this.log.info("convertPath.start");
        DomainDAO domainDAO = new DomainDAO(this.dbname);
        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);
        LinkDAO linkDAO =  new LinkDAO(this.dbname);
        Layer2Info layer2Info = interPathInfo.getLayer2Info();
        Layer3Info layer3Info = interPathInfo.getLayer3Info();
        String pathSetupMode = interPathInfo.getPathSetupMode();
        if (layer2Info != null) {
            srcLink = domainDAO.getFullyQualifiedLink(layer2Info.getSrcEndpoint());
            destLink = domainDAO.getFullyQualifiedLink(layer2Info.getDestEndpoint());
        }
        Path path = new Path();
        path.setNextDomain(nextDomain);
        CtrlPlanePathContent intraPath = intraPathInfo.getPath();
        CtrlPlaneHopContent[] hops = intraPath.getHop();
        List<PathElem> pathElems = new ArrayList<PathElem>();

        //  finalize information at layer 2 if last domain
        if (nextDomain == null && layer2Info != null) {
            this.chooseVlanTag(layer2Info);
        }

        // set pathSetupMode, default to timer-automatic
        if (pathSetupMode == null) {
            pathSetupMode = "timer-automatic";
        }
        path.setPathSetupMode(pathSetupMode);

        for (int i = 0; i < hops.length; i++) {
            String hopTopoId = hops[i].getLinkIdRef();
            Hashtable<String, String> parseResults = URNParser.parseTopoIdent(hopTopoId);
            String hopType = parseResults.get("type");
            String domainId = parseResults.get("domainId");


            // can't store non-local addresses
            if (!hopType.equals("link") || !domainDAO.isLocal(domainId)) {
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
                this.log.error("Couldn't find link in db for: ["+hops[i].getLinkIdRef()+"]");
                throw new BSSException("Couldn't find link in db for: ["+hops[i].getLinkIdRef()+"]");
            } else {
                this.log.debug("Found link in db for: ["+hops[i].getLinkIdRef()+"]");
            }
            PathElem pathElem = new PathElem();
            if (!foundIngress) {
                if (layer2Info != null) {
                    pathElem.setDescription("ingress");
                    foundIngress = true;
                } else {
                    // layer 3 currently requires a Juniper ingress
                    net.es.oscars.pathfinder.Utils utils =
                        new net.es.oscars.pathfinder.Utils(this.dbname);
                    // assumes one to one relationship
                    Ipaddr ipaddr =
                        ipaddrDAO.queryByParam("linkId", link.getId());
                    if (ipaddr == null) {
                        throw new BSSException(
                            "no IP address associated with link " +
                             link.getId());
                    } else if (!ipaddr.isValid()) {
                        throw new BSSException(
                                "IP address associated with link " +
                                 link.getId() + " is not valid");
                    }
                    try {
                        String ip =
                            utils.getLoopback(ipaddr.getIP(), "Juniper");
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
            //set VLAN tag if layer2 request and l2sc link
            //TODO: Set differently if VLAN mapping supported
            if(layer2Info != null &&
                link.getL2SwitchingCapabilityData() != null &&
                nextDomain == null){
                this.setL2LinkDescr(pathElem, srcLink, destLink, link,  layer2Info);
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
     * for reporting prurpose so there are not quite as many requirements as
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
            String urn = hops[i].getLinkIdRef();
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
                hopCopy.setId(hops[i].getLinkIdRef());
                hopCopy.setLinkIdRef(hops[i].getLinkIdRef());
                pathCopy.addHop(hopCopy);
                continue;
            }

            String hopTopoId = hops[i].getLinkIdRef();
            Hashtable<String, String> parseResults = URNParser.parseTopoIdent(hopTopoId);
            String hopType = parseResults.get("type");
            String domainId = parseResults.get("domainId");

            if (hopType.equals("link") &&  domainDAO.isLocal(domainId)) {
                // add ingress
                if (!edgeFound || i == (hops.length - 1)) {
                    hopCopy.setId(hops[i].getLinkIdRef());
                    hopCopy.setLinkIdRef(hops[i].getLinkIdRef());
                    pathCopy.addHop(hopCopy);
                    edgeFound = true;
                }
                prevHop = hops[i];
                continue;
            } else if (edgeFound) {
                // add egress
                CtrlPlaneHopContent hopCopy2 = new CtrlPlaneHopContent();
                hopCopy2.setId(prevHop.getLinkIdRef());
                hopCopy2.setLinkIdRef(prevHop.getLinkIdRef());
                pathCopy.addHop(hopCopy2);
                edgeFound = false;
            }
            hopCopy.setId(hops[i].getLinkIdRef());
            hopCopy.setLinkIdRef(hops[i].getLinkIdRef());
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

            String hopTopoId = hops[i].getLinkIdRef();
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
     *  Generates the next Gobal Rresource Identifier, created from the local
     *  Domain's topology identifier and the next unused index in the IdSequence table.
     *
     * @return the GRI.
     * @throws BSSException
     */
    public String generateGRI() throws BSSException{
        DomainDAO domainDAO = new DomainDAO(this.dbname);
        IdSequenceDAO idDAO = new IdSequenceDAO(this.dbname);
        Domain localDomain = domainDAO.getLocalDomain();
        int id = idDAO.getNewId();
        String gri = null;

        if(localDomain != null){
            gri = localDomain.getTopologyIdent() + "-" + id;
            this.log.info("GRI: " + gri);
        }else{
            throw new BSSException("Unable to generate GRI");
        }

        return gri;
    }

    /**
     * Sets VLAN tag on a pathElemenet.
     *
     * @param pathElem path element to be updated
     * @param srcLink source link of this request
     * @param destLink destination link of this request
     * @param link current link to be checked
     * @param layer2Info of the request
     */
     public void setL2LinkDescr(PathElem pathElem, Link srcLink, Link destLink,
                                    Link link, Layer2Info layer2Info){
        VlanTag srcVtag = layer2Info.getSrcVtag();
        VlanTag destVtag = layer2Info.getDestVtag();
        int srcTagged = srcVtag.getTagged() ? 1 : -1;
        int destTagged = destVtag.getTagged() ? 1 : -1;

        if (link == srcLink) {
            int vtagValue = Integer.parseInt(srcVtag.getString());
            pathElem.setLinkDescr((vtagValue * srcTagged) + "");
            this.log.info("linkId: " + link.getId() + ", srcLink: " + vtagValue * srcTagged);
        } else if(link == destLink) {
            int vtagValue = Integer.parseInt(destVtag.getString());
            pathElem.setLinkDescr((vtagValue * destTagged) + "");
            this.log.info("linkId: " + link.getId() + ", destLink: " + vtagValue * srcTagged);
        } else {
            pathElem.setLinkDescr(srcVtag.getString());
            this.log.info("linkId: " + link.getId() + ", internalLink: " + srcVtag.getString());
        }
     }

    /**
     * Picks a VLAN tag from a range of VLANS
     *
     * @param layer2Info the layer2Info containing the VLAN range
     * @throws BSSException
     */
    public void chooseVlanTag(Layer2Info layer2Info) throws BSSException{
        String vtag = layer2Info.getSrcVtag().getString();
        byte[] vtagMask = this.tc.rangeStringToMask(vtag);

        /* Pick first available */
        for(int i = 0;i < vtagMask.length; i++){
            for(int j = 0; vtagMask[i] != 0 && j < 8; j++){
                byte tag = (byte)(vtagMask[i] & (1 << (7 - j)));
                if(tag != 0){
                    vtag = (i*8 + j) + "";
                    this.log.info("chose VLAN " + vtag);

                    layer2Info.getSrcVtag().setString(vtag);
                    layer2Info.getDestVtag().setString(vtag);

                    return;
                }
            }
        }
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
    public void finalizeResv(CreateReply forwardReply, Reservation resv,
                             PathInfo pathInfo) throws BSSException{
        Layer2Info layer2Info = pathInfo.getLayer2Info();
        String pathSetupMode = pathInfo.getPathSetupMode();
        Path path = resv.getPath();

        // if user signaled and last domain create token, otherwise store
        // token returned in forwardReply
        if(pathSetupMode == null || pathSetupMode.equals("signal-xml")){
            this.generateToken(forwardReply, resv);
        }

        if(layer2Info != null && forwardReply != null &&
            forwardReply.getPathInfo() != null &&
            forwardReply.getPathInfo().getLayer2Info() != null) {

            this.log.info("setting up vtags");
            DomainDAO domainDAO = new DomainDAO(this.dbname);
            Link srcLink = domainDAO.getFullyQualifiedLink(layer2Info.getSrcEndpoint());
            Link destLink = domainDAO.getFullyQualifiedLink(layer2Info.getDestEndpoint());

            PathInfo fwdPathInfo = forwardReply.getPathInfo();
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
                    this.setL2LinkDescr(pathElem, srcLink, destLink,
                                            pathElem.getLink(), layer2Info);
                } else {
                    this.log.info("no switching capability data for: " + pathElem.getLink().getTopologyIdent());
                }
                pathElem = pathElem.getNextElem();
            }

            /* Store interdomain path */
            try{
                PathElem interPathElem = this.convertInterPath(fwdPathInfo);
                path.setInterPathElem(interPathElem);
            }catch(BSSException e){
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
            if (forwardReply == null) {
                this.log.info("because no forwardReply");
            } else if (forwardReply.getPathInfo() == null) {
                this.log.info("because no forwardReply.getPathInfo");
            } else if (forwardReply.getPathInfo().getLayer2Info() == null) {
                this.log.info("because no forwardReply.getPathInfo().getLayer2Info()");
            } else {
                this.log.info("unknown error");
            }

            /* Store version of path in terms of interdomain hops. Still want to do
               this even if a local path because the intradomain version may be private.
               For current implementations the intradomain URNs and interdomain URNs will
               be the same but that won't be true in the future so we need two versions
               of every path*/
            try{

                PathElem interPathElem = this.convertInterPath(pathInfo);
                path.setInterPathElem(interPathElem);
            }catch(BSSException e){
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
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("aaa", true);
        String doTokenGen = props.getProperty("useSignalTokens");
        if(doTokenGen != null && doTokenGen.equals("0")){
            return;
        }
        Token token = new Token();

        if(forwardReply == null){
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
        }else if(forwardReply.getToken() == null){
            return;
        }else{
            token.setValue(forwardReply.getToken());
        }

        resv.setToken(token);
        token.setReservation(resv);
    }
}
