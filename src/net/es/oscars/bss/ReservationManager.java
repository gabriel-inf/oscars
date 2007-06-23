package net.es.oscars.bss;

import java.util.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.mail.MessagingException;

import org.apache.log4j.*;

import net.es.oscars.Notifier;
import net.es.oscars.oscars.TypeConverter;
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
    private DomainDAO domainDAO;
    private PolicyManager policyMgr;
    private TypeConverter tc;
    private String dbname;

    /** Constructor. */
    public ReservationManager(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.notifier = new Notifier();
        this.pceMgr = new PCEManager(dbname);
        this.domainDAO = new DomainDAO(dbname);
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
     * @param ingressNodeIP string with address of ingress node
     * @param egressNodeIP string with address of egress node
     * @return string with URL of next domain manager, if any
     * @throws BSSException
     */
    public String create(Reservation resv, String login,
                         String ingressNode, String egressNode)
            throws  BSSException {

        ParamValidator paramValidator = new ParamValidator();
        String ingressNodeIP = null;
        String egressNodeIP = null;
        CommonPath commonPath = resv.getCommonPath();

        this.log.info("create.start");
        // login is checked in validate so set it here
        resv.setLogin(login);

        // so far just validation for create
        StringBuilder errorMsg =
                paramValidator.validate(resv, ingressNode, egressNode);
        if (errorMsg.length() > 0) {
            throw new BSSException(errorMsg.toString());
        }

        this.log.info("create.validated");
        resv.setLspClass("4");
        // this may actually be optional
        if (resv.getDscp() == null) {
            resv.setDscp("4");
        }
        resv.setStatus("PENDING");
        resv.setBurstLimit(10000000L);
        if (ingressNode != null) {
            ingressNodeIP = this.getIpAddress(ingressNode);
        }
        if (egressNode != null) {
            egressNodeIP = this.getIpAddress(egressNode);
        }
        Path path = this.getPath(resv, ingressNodeIP, egressNodeIP, commonPath);
        // check to make sure the path is not already in the database
        resv.setPath(this.checkPath(path));
        long millis = System.currentTimeMillis();
        resv.setCreatedTime(millis);
        this.log.info("create.finish"); 
        return resv.getCommonPath().getUrl();
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
     * Given a reservation tag, cancels the corresponding reservation.
     * (Can only cancel a pending or active reservation.)
     *
     * @param tag string with reservation tag
     * @param login  string with login name of user
     * @return reservation with cancellation status
     * @throws BSSException 
     */
    public Reservation cancel(String tag, String login, boolean allUsers) throws BSSException {

        ReservationDAO dao = new ReservationDAO(this.dbname);
        String newStatus = null;
        
        this.log.info("cancel.start: " + tag + " login: " + login + " allUsers is " + allUsers);

        Reservation resv = dao.query(tag);
        if (resv == null) {
            throw new BSSException(
                "No current reservation with tag: " + tag);
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
     * @param status string with final cancel status (TODO:  use)
     * @throws BSSException
     */
    public void finalizeCancel(Reservation resv, String status) 
            throws BSSException {

        this.log.info("finalizeCancel.start");
        String tag = this.tc.getReservationTag(resv);

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
     * Given a reservation tag, queries the database and returns the
     *     corresponding reservation instance.
     *
     * @param tag string with reservation tag
     * @param login string with login name of the caller
     * @param allUsers boolean indicating user can view reservations for all users
     * @return resv corresponding reservation instance, if any
     * @throws BSSException 
     */
    public Reservation query(String tag, String login, boolean allUsers)
            throws BSSException {

        Reservation resv = null;

        this.log.info("query.start: " + tag + " login: " + login + " allUsers is " + allUsers);
        ReservationDAO dao = new ReservationDAO(this.dbname);
        resv = dao.query(tag);
        if (resv == null) {
            throw new BSSException("Reservation not found: " + tag);
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
     * @return reservations list of rseervations
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
    public Path getPath(Reservation resv, String ingressNodeIP,
                        String egressNodeIP, CommonPath commonPath)
            throws BSSException {

        if (commonPath == null) {
            throw new BSSException("No path provided to reservation manager");
        }

        try {
            this.pceMgr.findPath(resv.getSrcHost(), resv.getDestHost(),
                                 ingressNodeIP, egressNodeIP,
                                 commonPath);
        } catch (PathfinderException ex) {
            throw new BSSException(ex.getMessage());
        }
        // only occurs if testing only a portion of the system
        if (commonPath.getElems() == null) { return null; }

        Long bandwidth = resv.getBandwidth();
        ReservationDAO dao = new ReservationDAO(this.dbname);
        List<Reservation> reservations =
                dao.overlappingReservations(resv.getStartTime(),
                                            resv.getEndTime());
        this.policyMgr.checkOversubscribed(reservations, commonPath,
                                           bandwidth);
        String url = null;
        Domain nextDomain = null;
        // get next external hop (first past egress) from the complete path
        String nextExternalHop = this.getNextExternalHop(commonPath);
        if (nextExternalHop != null){
            nextDomain = this.domainDAO.getNextDomain(nextExternalHop);
            if (nextDomain != null) {
                this.log.info("create.finish, next domain: " +
                          nextDomain.getUrl());
                url = nextDomain.getUrl();
            } else {
                this.log.warn(
                        "Can't find domain url for nextExternalHop. Hop is: " +
                        nextExternalHop);
            }
        }
        commonPath.setUrl(url);
        // needed for upstream classes
        resv.setCommonPath(commonPath);
        // convert to form for db
        Path path = this.convertPath(commonPath, nextDomain);
        return path;
    }

    /**
     * Checks to see if the path found for a reservation already exists in
     * the database.  If so, this returns the path from the database.  If
     * not the reservation's found path is returned unchanged.
     *
     * @param path Path instance found by pathfinder component
     * @return either path from the database, or the found path
     */
    public Path checkPath(Path path) {

        Path existingPath = null;
        boolean samePath = false;

        this.log.info("checkPath.start");
        PathDAO pathDAO = new PathDAO(this.dbname);
        PathElem pathElem = path.getPathElem();
        // since cascading, these will be complete paths starting with
        // the given one's IP
        List<Path> testPaths =
                pathDAO.getPaths(pathElem.getIpaddr().getIP());
        Utils utils = new Utils(this.dbname);
        // check each path to see if it is the same
        for (Path testp: testPaths) {
            samePath = utils.isDuplicate(path, testp);
            // may be small number of duplicates from before, but not a
            // problem
            if (samePath) {
                existingPath = testp;
                break;
            }
        }
        if (existingPath == null) {
            this.log.info("checkPath.finish, new path");
            return path;
        } else {
            this.log.info("checkPath.finish, old path");
            return existingPath;
        }
    }
    
    /**
     * Converts complete path in common format (no db keys) into
     * local path, in form suitable for database manipulation.
     *
     * @param commonPath path in common format
     * @param nextDomain domain instance with information about next domain
     * @return path path in db format
     */
    public Path convertPath(CommonPath commonPath, Domain nextDomain) {

        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);
        Ipaddr ipaddr = null;
        String description = null;
        List<PathElem> pathElems = new ArrayList<PathElem>();

        Path path = new Path();
        if (commonPath.getVlanTag() != null) {
            Vlan vlan = new Vlan();
            // TODO:  port association
            vlan.setVlanTag(commonPath.getVlanTag());
            path.setVlan(vlan);
        }
        path.setExplicit(commonPath.isExplicit());
        path.setNextDomain(nextDomain);
        List<CommonPathElem> commonElems = commonPath.getElems();
        // this is the complete path
        for (int i = 0; i < commonElems.size(); i++) {
            CommonPathElem commonPathElem = commonElems.get(i);
            description = commonPathElem.getDescription();
            // can't store non-local addresses
            if (description == null) {
                continue;
            }
            PathElem pathElem = new PathElem();
            pathElem.setLoose(commonPathElem.isLoose());
            pathElem.setDescription(description);
            ipaddr = ipaddrDAO.getIpaddr(commonPathElem.getIP(), true);
            pathElem.setIpaddr(ipaddr);
            pathElems.add(pathElem);
        }
        for (int i = 0; i < pathElems.size()-1; i++) {
            pathElems.get(i).setNextElem(pathElems.get(i+1));
        }
        // set start to first element
        path.setPathElem(pathElems.get(0));
        return path;
    }

    /**
     * Given a CommonPath instance with the complete path, find the
     * first hop outside the local domain.
     *
     * @param path CommonPath instance containing complete path
     * @return ip string with IP of next hop
     */
    public String getNextExternalHop(CommonPath path) {

        String nextHop = null;
        String description = null;
        boolean hopFound = false;

        this.log.info("getNextExternalHop.start");
        List<CommonPathElem> pathElems = path.getElems();
        for (int i = 0; i < pathElems.size(); i++) {
            CommonPathElem pathElem = pathElems.get(i);
            description = pathElem.getDescription();
            // if not local
            if (description == null) {
                if (hopFound) {
                    nextHop = pathElem.getIP();
                    break;
                }
            } else {
                hopFound = true;
            }
        }
        this.log.info("getNextExternalHop.end");
        return nextHop;
    }

    /**
     * Returns IP address associated with host.
     *
     * @param string with either host name or IP address
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
}
