package net.es.oscars.bss;

import java.util.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.mail.MessagingException;

import org.apache.log4j.*;

import org.aaaarch.gaaapi.tvs.TokenBuilder;
import org.aaaarch.gaaapi.tvs.TokenKey;

import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlanePathContent;
import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlaneHopContent;

import net.es.oscars.Notifier;
import net.es.oscars.oscars.TypeConverter;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.bss.topology.*;
import net.es.oscars.pathfinder.*;


/**
 * ReservationManager handles all networking and data access object calls
 * necessary to create, update, delete, and modify reservations.
 *
 * @author David Robertson, Mary Thompson, Jason Lee
 */
public class ReservationManager {
    private Logger log;
    private Notifier notifier;
    private PCEManager pceMgr;
    private PolicyManager policyMgr;
    private TypeConverter tc;
    private String dbname;

    /** Constructor. */
    public ReservationManager(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.notifier = new Notifier();
        this.pceMgr = new PCEManager(dbname);
        this.policyMgr = new PolicyManager(dbname);
        this.tc = new TypeConverter();
        this.dbname = dbname;
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
        if(resv.getGlobalReservationId() == null){
            String gri = this.generateGRI();
            resv.setGlobalReservationId(gri);
        }
        
        // so far just validation for create
        StringBuilder errorMsg =
                paramValidator.validate(resv, pathInfo);
        if (errorMsg.length() > 0) {
            throw new BSSException(errorMsg.toString());
        }

        this.log.info("create.validated");
        resv.setStatus("PENDING");
        // save complete copy of original path for forwarding
        // for layer 2
        CtrlPlanePathContent pathCopy = this.copyPath(pathInfo, false);
        // this modifies the path to include internal hops with layer 2,
        // and finds the complete path with traceroute
        Path path = this.getPath(resv, pathInfo);
        resv.setPath(path);
        long millis = System.currentTimeMillis();
        resv.setCreatedTime(millis);
        // if layer 3, forward complete path found by traceroute, minus
        // internal hops
        if (pathInfo.getLayer3Info() != null) {
            pathCopy = this.copyPath(pathInfo, true);
        }else if (pathCopy == null && pathInfo.getLayer2Info() != null) {
            pathCopy = this.copyPath(pathInfo, true);
        }
        
        pathInfo.setPath(pathCopy);
        this.log.info("create.finish"); 
    }

    /**
     * Stores the reservation in the database.
     *
     * @param resv Reservation instance to persist
     */
    public void store(Reservation resv) throws BSSException {
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
            TokenDAO tokenDAO = new TokenDAO("bss");
            tokenDAO.create(resv.getToken());
        }
        try {
            String subject = "Reservation has been entered into the system";
            String msg = "Reservation: " + resv.toString() + "\n";
            this.notifier.sendMessage(subject, msg);
        } catch (javax.mail.MessagingException ex) {
            this.log.info("create.mail.exception: " + ex.getMessage());
            // throw new BSSException(ex.getMessage());
        } catch (UnsupportedOperationException ex) {
            this.log.info("create.mail.unsupported: " + ex.getMessage());
        }
        this.log.info("store.finish");
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
        this.log.info("cancel.finish: " + resv.toString());
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

        this.log.info("finalizeCancel.start");
        String gri = resv.getGlobalReservationId();

        String subject = "Reservation successfully cancelled";
        String msg = "Reservation: " + resv.toString() + "\n";
        try {
            this.notifier.sendMessage(subject, msg);
        } catch (javax.mail.MessagingException ex) {
            this.log.info("cancel.mail.exception: " + ex.getMessage());
        } catch (UnsupportedOperationException ex) {
            this.log.info("cancel.mail.unsupported: " + ex.getMessage());
        }
        this.log.info("finalizeCancel.finish: " + resv.toString());
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
        this.log.info("query.finish: " + resv.toString());
        return resv;
    } 

    /**
     * Lists all reservations if allUsers is true; otherwise only lists the
     *     corresponding user's reservations.
     *
     * @param login string with user's login name
     * @param allUsers boolean setting whether can view reservations for all users
     * @return reservations list of reservations
     * @throws BSSException 
     */
    public List<Reservation> list(String login, boolean allUsers)
            throws BSSException {

        List<Reservation> reservations = null;

        this.log.info("list.start, login: " + login);
        ReservationDAO dao = new ReservationDAO(this.dbname);
        reservations = dao.list(login, allUsers);
        this.log.info("list.finish, success");
        return reservations;
    }

    /**
     * Finds path between source and destination, checks to make sure
     * it wouldn't violate policy, and then finds the next domain, if any.
     */
    public Path getPath(Reservation resv, PathInfo pathInfo)
            throws BSSException {

        boolean isExplicit = false;

        try {
            isExplicit = this.pceMgr.findPath(pathInfo);
        } catch (PathfinderException ex) {
            throw new BSSException(ex.getMessage());
        }
        Long bandwidth = resv.getBandwidth();
        ReservationDAO dao = new ReservationDAO(this.dbname);
        List<Reservation> reservations =
                dao.overlappingReservations(resv.getStartTime(),
                                            resv.getEndTime());
        this.policyMgr.checkOversubscribed(reservations, pathInfo,
                                           resv);       
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
        Path path = this.convertPath(pathInfo, nextDomain);
        path.setExplicit(isExplicit);
        return path;
    }

    /**
     * Converts complete path in Axis2 data structure into
     * database path.
     *
     * @param pathInfo PathInfo instance (Axis2 type) with filled in info
     * @param nextDomain domain instance with information about next domain
     * @return path path in database format
     */
    public Path convertPath(PathInfo pathInfo, Domain nextDomain)
            throws BSSException {

        Link link = null;
        Link srcLink = null;
        Link destLink = null;
        String description = null;
        boolean foundIngress = false;
        PathElem lastElem = null;

        this.log.info("convertPath.start");
        DomainDAO domainDAO = new DomainDAO(this.dbname);
        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);
        LinkDAO linkDAO =  new LinkDAO(this.dbname);
        Layer2Info layer2Info = pathInfo.getLayer2Info();
        Layer3Info layer3Info = pathInfo.getLayer3Info();
        String pathSetupMode = pathInfo.getPathSetupMode();
        if (layer2Info != null) {
            srcLink = domainDAO.getFullyQualifiedLink(layer2Info.getSrcEndpoint());
            destLink = domainDAO.getFullyQualifiedLink(layer2Info.getDestEndpoint());
        }
        Path path = new Path();
        path.setNextDomain(nextDomain);
        CtrlPlanePathContent ctrlPlanePath = pathInfo.getPath();
        CtrlPlaneHopContent[] hops = ctrlPlanePath.getHop();
        List<PathElem> pathElems = new ArrayList<PathElem>();
        
        //  finalize information at layer 2 if last domain
        if (nextDomain == null && layer2Info != null) {
            this.chooseVlanTag(layer2Info);
        }
        
        // set pathSetupMode, default to domain
        if (pathSetupMode == null) {
            pathSetupMode = "domain";
        }
        path.setPathSetupMode(pathSetupMode);
        
        for (int i = 0; i < hops.length; i++) {
            String hopTopoId = hops[i].getLinkIdRef();
            Hashtable<String, String> parseResults = TopologyUtil.parseTopoIdent(hopTopoId);
            String hopType = parseResults.get("type");
            String domainId = parseResults.get("domainId");
            
            // can't store non-local addresses
            if (!hopType.equals("link") || !domainDAO.isLocal(domainId)) {
                continue;
            }
            link = domainDAO.getFullyQualifiedLink(hopTopoId);
            if (link == null) {
                this.log.error("Couldn't find link in db for: ["+hops[i].getLinkIdRef()+"]");
            	throw new BSSException("Couldn't find link in db for: ["+hops[i].getLinkIdRef()+"]"); 
            } else {
            	this.log.info("Found link in db for: ["+hops[i].getLinkIdRef()+"]");
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
        MplsInfo mplsInfo = pathInfo.getMplsInfo();
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
     * Make a copy of the path.
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
        CtrlPlaneHopContent hopCopy = null;
        
        if (ctrlPlanePath == null) {
            return null;
        }
        
        pathCopy.setId(ctrlPlanePath.getId());
        
        CtrlPlaneHopContent[] hops = ctrlPlanePath.getHop();
        for (int i = 0; i < hops.length; i++) {
            hopCopy = new CtrlPlaneHopContent();
            if (!exclude) {
                hopCopy.setId(hops[i].getLinkIdRef());
                hopCopy.setLinkIdRef(hops[i].getLinkIdRef());
                pathCopy.addHop(hopCopy);
                continue;
            }

            String hopTopoId = hops[i].getLinkIdRef();
            Hashtable<String, String> parseResults = TopologyUtil.parseTopoIdent(hopTopoId);
            String hopType = parseResults.get("type");
            String domainId = parseResults.get("domainId");
            
            if (hopType.equals("link") &&  domainDAO.isLocal(domainId)) {
                // add ingress
                if (!edgeFound) {
                    hopCopy.setId(hops[i].getLinkIdRef());
                    hopCopy.setLinkIdRef(hops[i].getLinkIdRef());
                    pathCopy.addHop(hopCopy);
                    edgeFound = true;
                }
                prevHop = hops[i];
                continue;
            } else if (edgeFound) {
                // add egress
                hopCopy.setId(prevHop.getLinkIdRef());
                hopCopy.setLinkIdRef(prevHop.getLinkIdRef());
                pathCopy.addHop(hopCopy);
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
            Hashtable<String, String> parseResults = TopologyUtil.parseTopoIdent(hopTopoId);
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
        boolean found = false;
        
        /* Pick first available */
        for(int i = 0; (!found) && i < vtagMask.length; i++){
            for(int j = 0; vtagMask[i] != 0 && j < 8; j++){
                byte tag = (byte)(vtagMask[i] & (1 << (7 - j)));
                if(tag != 0){
                    vtag = (i*8 + j) + "";
                    this.log.info("chose VLAN " + vtag);
                    
                    layer2Info.getSrcVtag().setString(vtag);
                    layer2Info.getDestVtag().setString(vtag);
                    
                    found = true;
                    return;
                    // break;  // no need to break..
                }
            }
        }
    }
    
    /** 
     * Makes final changes to reservation before storage in database
     *
     * @param forwardReply response from forward request
     * @param resv reservation to be stored in database
     * @pathInfo reservation path information
     */
    public void finalizeResv(CreateReply forwardReply, Reservation resv,                                     
                             PathInfo pathInfo) throws BSSException{
        Layer2Info layer2Info = pathInfo.getLayer2Info();
        String pathSetupMode = pathInfo.getPathSetupMode();
        
        //Create token if user signaled
        if(pathSetupMode == null || pathSetupMode.equals("user-xml")){
            this.generateToken(forwardReply, resv);
        }
        
        if(layer2Info != null && forwardReply != null && 
            forwardReply.getPathInfo() != null && 
            forwardReply.getPathInfo().getLayer2Info() != null) {

            this.log.info("setting up vtags");
            DomainDAO domainDAO = new DomainDAO(this.dbname);
            Link srcLink = domainDAO.getFullyQualifiedLink(layer2Info.getSrcEndpoint());
            Link destLink = domainDAO.getFullyQualifiedLink(layer2Info.getDestEndpoint());
            
            VlanTag srcVtag = forwardReply.getPathInfo().getLayer2Info().getSrcVtag();
            VlanTag destVtag = forwardReply.getPathInfo().getLayer2Info().getDestVtag();
            this.log.info("src vtag: " + srcVtag);
            this.log.info("dest vtag: " + destVtag);
            layer2Info.setSrcVtag(srcVtag);
            layer2Info.setDestVtag(destVtag);
            
            /* update VLANs */
            Path path = resv.getPath();
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
        }
    }
    
    /** 
     * Creates a token if the last domain or sets the token returned from
     * the forward reply.
     *
     * @param forwardReply response from forward request
     * @param resv reservation to be stored in database
     */
    private void generateToken(CreateReply forwardReply, Reservation resv)
                    throws BSSException{
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
        }else{
            token.setValue(forwardReply.getToken());
        }
        
        resv.setToken(token);
        token.setReservation(resv);
    }
}
