package net.es.oscars.bss;

import java.util.*;

import org.hibernate.*;

import net.es.oscars.LogWrapper;
import net.es.oscars.database.GenericHibernateDAO;
import net.es.oscars.bss.topology.*;


/**
 * ReservationDAO is the data access object for the oscars.reservations table.
 *
 * @author David Robertson (dwrobertson@lbl.gov), Jason Lee (jrlee@lbl.gov)
 */
public class ReservationDAO extends GenericHibernateDAO<Reservation, Integer> {
    private LogWrapper log;

    public ReservationDAO () {
        this.log = new LogWrapper(this.getClass());
    }

    /**
     * Creates a reservation.
     *
     * @param reservation a reservation to be persisted
     * @throws BSSException
     */
    public void create(Reservation reservation) throws BSSException {
        this.makePersistent(reservation);
    }


    /**
     * Changes the status of a reservation to indicate it is cancelled.
     *
     * @param tag tag a unique id for a reservation across domains
     * @return a string with the new reservation status
     * @throws BSSException
     */
    public String cancel(String tag) throws BSSException {

        // TODO:  ensure unprivileged user can't cancel another's reservation
        String[] strArray = tag.split("-");
        // TODO:  FIX
        Integer id = Integer.valueOf(strArray[1]);
        return this.updateStatus(id, "PRECANCEL");
    }


    /**
     * Finds a reservation, given its tag.
     *
     * @param tag a string uniquely identifying a reservation across domains
     * @param authorized a boolean indicating whether a user can view all
     *     of a reservation's fields
     * @return reservation found, if any
     * @throws BSSException
     */
    public Reservation query(String tag, boolean authorized)
        throws BSSException {

        String[] strArray = tag.split("-");
        // TODO:  FIX
        Integer id = Integer.valueOf(strArray[1]);
        // NOTE:  calling method must call pathDAO.toString() to get path
        // from Reservation bean's pathId
        Reservation reservation = this.findById(id, false);
        return reservation;
    }


    /**
     * Lists all reservations if authorized; otherwise, list only
     *     reservations owned by that particular user.
     *     Note that GenericHibernateDAO has Criteria based methods for
     *     doing lists, but they are less clear to someone used to SQL.
     *
     * @param login a string identifier for a user
     * @param authorized boolean indicating can view all reservations
     * @return a list of reservations.
     * @throws BSSException
     */
    public List<Reservation> list(String login, boolean authorized)
            throws BSSException {
        List<Reservation> reservations = null;

        // must provide user name
        if (login == null) { return null; }
        // if not authorized, can only view individual reservations
        if (!authorized) {
            this.log.info("list", "individual: " + login);
            String hsql = "from Reservation r where r.login = :login " +
                          "order by r.startTime desc";
            reservations =  this.getSession().createQuery(hsql)
                                   .setString("login", login)
                                   .list();
        } else {
            this.log.info("list", "all");
            String hsql = "from Reservation r order by r.startTime desc";
            reservations = this.getSession().createQuery(hsql).list();
        }
        return reservations;
    }


    /**
     * Retrieves the list of all currently pending and active reservations.
     *
     * @param startTime proposed reservation start time
     * @param endTime proposed reservation end time
     * @return list of all pending and active reservations
     * @throws BSSException
     */
    public List<Reservation>
        getActiveReservations(Long startTime, Long endTime)
                 throws BSSException {

        List<Reservation> reservations = null;
        // Get reservations with times overlapping that of the reservationi
        // request.
        String hsql = "from Reservation r " +
                      "where r.endTime >= :endTime and " +
                      "r.startTime <= :startTime and " +
                      "(r.status = 'PENDING' or r.status = 'ACTIVE')";
        reservations = this.getSession().createQuery(hsql)
                                        .setLong("startTime", startTime)
                                        .setLong("endTime", endTime)
                                        .list();
        return reservations;
    }

    /**
     * Updates reservation status.
     *     Used to mark as active, finished, or cancelled.
     *
     * @param id reservation id
     * @param status a string with the proposed new status
     * @return a string containing the new status
     * @throws BSSException
     */
    public String updateStatus(Integer id, String status) 
                  throws BSSException {

        // TODO:  Figure out what this did:  was update method in Perl
        /*
        if (!resv.get("lspStatus")) {
            resv.put("lspStatus", "Successful configuration");
            status = this.updateStatus(resv.tag, status);
        } else { status = this.updateStatus(resv.get("tag"), "failed"); }
        */
        Reservation r = this.findById(id, false);

        /* If the previous state was PRECANCEL, mark it now as CANCELLED.
         If the previous state was PENDING, and it is to be CANCELLED, mark it
         as CANCELLED instead of PRECANCEL.  The latter is used by 
         expiredReservations as one of the conditions to attempt to
         tear down a circuit. */
        String prevStatus = r.getStatus();
        if (prevStatus.equals("PRECANCEL") || (prevStatus.equals("PENDING") &&
                status.equals("PRECANCEL"))) { 
            r.setStatus("CANCELLED");
        } else {
            r.setStatus(status);
        }
        this.makePersistent(r);
        return r.getStatus();
    }

    /** 
     * Finds pending OSCARS reservations.  Calls the PSS to setup a
     *     label-switched path.
     *
     * @param timeInterval an int to add to the current time
     * @return list of pending reservations
     * @throws BSSException
     */
    protected List<Reservation> pendingReservations(int timeInterval)
            throws BSSException { 

        List<Reservation> reservations = null;
        long millis = 0;

        millis = System.currentTimeMillis() + timeInterval * 1000;
        String hsql = "from Reservation where status = :status " +
                      "and startTime < :startTime";
        reservations = this.getSession().createQuery(hsql)
                              .setString("status", "PENDING")
                              .setLong("startTime", millis)
                              .list();
        return reservations;
    }

    /** 
     * Finds expired OSCARS reservations.  Calls the PSS to teear down
     *     the label-switched path.
     *
     * @param timeInterval An int to add to the current time
     * @return list of expired reservations
     * @throws BSSException
     */
    protected List<Reservation> expiredReservations(int timeInterval)
            throws BSSException { 

        List<Reservation> reservations = null;
        long millis = 0;

        millis = System.currentTimeMillis() + timeInterval * 1000;
        String hsql = "from Reservation where (status = 'ACTIVE' and " +
                      "endTime < :endTime) or (status = 'PRECANCEL')";
        reservations = this.getSession().createQuery(hsql)
                              .setLong("endTime", millis)
                              .list();
        return reservations;
    }
}
