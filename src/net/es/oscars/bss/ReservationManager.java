package net.es.oscars.bss;

import java.util.*;
import javax.mail.MessagingException;
import java.io.IOException;

import org.hibernate.*;

import net.es.oscars.*;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.bss.topology.*;
import net.es.oscars.pathfinder.*;


/**
 * ReservationManager handles all networking and data access object calls
 * necessary to create, update, delete, and modify reservations.
 *
 * @author David Robertson, Mary Thompson, Jason Lee
 */
public class ReservationManager {
    private Session session;
    private LogWrapper log;
    private Notifier notifier;
    private PathManager pathMgr;
    private Properties traceProps;
    private Properties narbProps;
    private Properties snmpProps;

    /** Constructor. */
    public ReservationManager() {
        PropHandler propHandler = new PropHandler("oscars.properties");
        this.traceProps = propHandler.getPropertyGroup("trace", true);
        this.narbProps = propHandler.getPropertyGroup("narb", true);
        this.snmpProps = propHandler.getPropertyGroup("snmp", true);
        this.log = new LogWrapper(this.getClass());
        this.notifier = new Notifier();
        this.pathMgr = new PathManager();
    }

    public void setSession() {
        this.session = 
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
    }

    /**
     * Inserts a reservation into the database, given a partially filled in
     *     reservation instance and additional parameters.
     *
     * @param resv seservation instance filled in with request params
     * @param login string with login name
     * @param ingressRouterIP string with address of ingress router
     * @param egressRouterIP string with address of egress router
     * @return Domain instance, if any, associated with next domain
     * @throws BSSException
     */
    public Domain create(Reservation resv, String login,
                         String ingressRouterIP, String egressRouterIP) 
            throws  BSSException {

        ParamValidator paramValidator = new ParamValidator();
        List<Reservation> reservations = null;
        List<Path> currentPaths = new ArrayList<Path>();
        Path path = null;
        Long bandwidth = 0L;

        // login is checked in validate so set it here
        resv.setLogin(login);
        this.log.info("create.start", resv.toString());

        // so far just validation for create
        StringBuilder errorMsg =
            paramValidator.validate(resv, ingressRouterIP, egressRouterIP);
        if (errorMsg.length() > 0) {
            throw new BSSException(errorMsg.toString());
        }
        
        String runTrace = this.traceProps.getProperty("runTrace");
        String runNARB = this.narbProps.getProperty("runNARB");
        this.log.debug("runTrace variable is", runTrace);
		this.log.debug("runNARB variable is", runNARB);
		
        ReservationDAO dao = new ReservationDAO();
        dao.setSession(this.session);
        this.pathMgr.setSession(this.session);
        
        /* Determine path calculation to perform */
        if (runTrace != null && runTrace.equals("1")) {
       		 path = this.pathMgr.findPath(resv.getSrcHost(), resv.getDestHost(),
                             ingressRouterIP, egressRouterIP);
        }else if(runNARB != null && runNARB.equals("1")){
        	path = this.pathMgr.findNARBPath(resv.getSrcHost(), resv.getDestHost(),
                             ingressRouterIP, egressRouterIP);
        }else{
        	throw new BSSException("No path computation method configured. Please contact administrator.");
        }
		
        bandwidth = resv.getBandwidth();
        reservations =
            dao.getActiveReservations(resv.getStartTime(), resv.getEndTime());
 
        for (Reservation r: reservations) {
            currentPaths.add(r.getPath());
        }
        this.pathMgr.checkOversubscribed(currentPaths, path, bandwidth);

        // set start of path
        resv.setPath(path);
        resv.setLspClass("4");
        resv.setStatus("PENDING");
        resv.setBurstLimit(10000000L);
        long millis = System.currentTimeMillis();
        resv.setCreatedTime(millis);
        dao.create(resv);
        try {
            String subject = "Reservation has been entered into the system";
            String notification = this.createReservationMessage(resv);
            this.notifier.sendMessage(subject, notification);
        } catch (javax.mail.MessagingException e) {
            this.log.info("create.mail.exception", e.getMessage());
            // throw new BSSException(e.getMessage());
        /* testing activation jar */
        } catch (UnsupportedOperationException e) {
            this.log.info("create.mail.unsupported", e.getMessage());
        }
        
        // finds next domain, if any, to hand to interdomain component
        this.pathMgr.setSession(this.session);
        String runSNMP = this.snmpProps.getProperty("runSNMP");
        Domain nextDomain = null;
        if(runSNMP != null && runSNMP.equals("1")){
         	nextDomain = this.pathMgr.getNextDomain(path);
        }else{
        	nextDomain = this.pathMgr.getNextDomainFromDB();
        }
        
        this.log.info("create.finish reservation tag is ", this.toTag(resv)); 
        if (nextDomain != null) {
        	this.log.info("create.finish next domain is " , nextDomain.getUrl());
        }
        return nextDomain;
    }

    /**
     * Given a reservation tag, cancels the corresponding reservation.
     *
     * @param tag string with reservation tag
     * @param login  string with login name of user
     * @return reply string with cancellation status
     * @throws BSSException 
     */
    public String cancel(String tag, String login) throws BSSException {
        ReservationDAO dao = null;

        String reply = null;
        this.log.info("cancel.start", tag);
   
        try {
	        dao = new ReservationDAO();
	        dao.setSession(this.session);
	        reply = dao.cancel(tag);
	
	        this.log.info("cancel.finish", "tag: " + tag + ", status: " + reply);
        } catch (Exception eIn) {
        	BSSException eOut = new BSSException("Reservation not found: "+ tag);
        	//eOut.fillInStackTrace();
        	throw eOut;       	
        }
        Reservation resv = dao.query(tag, false);
        String subject = "Reservation successfully cancelled";
        String notification = this.cancelReservationMessage(resv);
        try {
            this.notifier.sendMessage(subject, notification);
        } catch (javax.mail.MessagingException e) {
            this.log.info("cancel.mail.exception", e.getMessage());
            // throw new BSSException(e.getMessage());
        /* testing activation jar */
        } catch (UnsupportedOperationException e) {
            this.log.info("cancel.mail.unsupported", e.getMessage());
        }
        return reply;
    }

    /**
     * Given a reservation tag, queries the database and returns the
     *     corresponding reservation instance.
     *
     * @param tag string with reservation tag
     * @param authorized boolean indicating user can view all reservations
     * @return resv corresponding reservation instance, if any
     * @throws BSSException 
     */
    public Reservation query(String tag, boolean authorized)
            throws BSSException {

        Reservation resv = null;

        this.log.info("query.start", tag);
        ReservationDAO dao = new ReservationDAO();
        dao.setSession(this.session);
        try {
            resv = dao.query(tag, authorized);
           this.log.info("query.finish" , this.toTag(resv));
           return resv;
        } catch (Exception eIn) {
        	BSSException eOut = new BSSException("Reservation not found: "+ tag);
        	eOut.fillInStackTrace();
        	throw eOut;
        }
    } 

    /**
     * Lists all reservations if authorized; otherwise only lists the
     *     corresponding user's reservations.
     *
     * @return reservations list of rseervations
     * @param login string with user's login name
     * @param authorized boolean setting whether can view all reservations
     * @throws BSSException 
     */
    public List<Reservation> list(String login, boolean authorized)
            throws BSSException {

        List<Reservation> reservations = null;

        this.log.info("list.start", "login: " + login);
        ReservationDAO dao = new ReservationDAO();
        dao.setSession(this.session);
        reservations = dao.list(login, authorized);
        this.log.info("list.finish", "success");
        return reservations;
    }

    /**
     * Given a reservation instance, returns a string representation of its
     *     associated path.
     *
     * @param resv a reservation instance
     * @param retType a string, either "ip" or "host"
     * @return string representation of the path
     */
    public String pathToString(Reservation resv, String retType) {
        if (resv.getPath() == null) { return ""; }
        Path path = resv.getPath();
        this.pathMgr.setSession(this.session);
        return this.pathMgr.pathToString(path, retType);
    }

    /**
     * Given a reservation instance, constructs a tag which is unique across
     *     domains.
     *
     * @param reservation a reservation instance
     * @return tag string with unique tag
     */
    public String toTag(Reservation reservation) {
        long millis = 0;

        if (reservation.getStartTime() == null) { return ""; }
        DomainDAO domainDAO = new DomainDAO();
        domainDAO.setSession(this.session);
        Domain domain = domainDAO.getLocalDomain();
        Calendar startTime = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        millis = reservation.getStartTime();
        startTime.setTimeInMillis(millis);
        // months start at 0 in Calendar
        int month = startTime.get(Calendar.MONTH) + 1;
        String tag = domain.getAbbrev() + "-" + reservation.getLogin() + "-" +
            startTime.get(Calendar.YEAR) + "-" +
            this.fixedLengthTime(month) + "-" +
            this.fixedLengthTime(startTime.get(Calendar.DAY_OF_MONTH)) + "-" +
            reservation.getId();
        return tag;
    }

    /**
     * If given an int whose string length is less than 2, prepends a "0".
     *
     * @param dint int, for example representing month or day
     * @return fixedLength fixed length string of length 2
     */
    public String fixedLengthTime(int dint) {
        String fixedLength = null;

        if (dint < 10) { fixedLength = "0" + dint; }
        else { fixedLength = "" + dint; }
        return fixedLength;
    }

    /*
     * Notification message methods.  For lack of a better pattern at the
     * moment.  Configuration with unordered properties was not a solution.
     */

    /**
     * Returns a description of the created reservation suitable for email.
     * @param reservation a reservation instance
     * @return a String describing the created reservation
     */
    public String createReservationMessage(Reservation resv) {
        String msg = "Reservation tag: " + this.toTag(resv) + "\n";
        msg += "Status: " + resv.getStatus() + "\n";
        return msg;
    }

    /**
     * Returns a description of the cancelled reservation suitable for email.
     * @param reservation a reservation instance
     * @return a String describing the cancelled reservation
     */
    public String cancelReservationMessage(Reservation resv) {
        String msg = "Reservation tag: " + this.toTag(resv) + "\n";
        return msg;
    }
}
