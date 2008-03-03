package net.es.oscars.bss;

import java.util.*;
import org.apache.log4j.*;

import org.hibernate.*;

import net.es.oscars.database.GenericHibernateDAO;
import net.es.oscars.bss.topology.*;


/**
 * ReservationDAO is the data access object for
 * the bss.reservations table.
 *
 * @author David Robertson (dwrobertson@lbl.gov), Jason Lee (jrlee@lbl.gov)
 */
public class ReservationDAO
    extends GenericHibernateDAO<Reservation, Integer> {

    private Logger log;
    private String dbname;
    private List<Reservation> reservations;

    public ReservationDAO(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.setDatabase(dbname);
        this.dbname = dbname;
        this.reservations = new ArrayList<Reservation>();
    }

    /**
     * Finds a reservation, given its global reservation id.
     *
     * @param gri a string uniquely identifying a reservation across domains
     * @return reservation found, if any
     * @throws BSSException
     */
    public Reservation query(String gri)
        throws BSSException {

        Reservation reservation = this.queryByParam("globalReservationId", gri);
        return reservation;
    }


    /**
     * Lists reservations
     *
     * @param logins a list of user logins. If not null or empty, results will
     * only include reservations submitted by these specific users. If null / empty
     * results will include reservations by all users.
     *
     * @param statuses a list of reservation statuses. If not null or empty,
     * results wil only include reservations with one of these statuses.
     * If null / empty, results will include reservations with any status.
     *
     * @param links a list of links. If not null / empty, results will only
     * include reservations whose path includes at least one of the links.
     * If null / empty, results will include reservations with any path.
     *
     * @param startTime the start of the time window to look in; null for everything before the endTime
     *
     * @param endTime the end of the time window to look in; null for everything after the startTime,
     * leave both start and endTime null to disregard time
     *
     * @return a list of reservations.
     * @throws BSSException
     */
    @SuppressWarnings("unchecked")
    public List<Reservation> list(List<String> logins, List<String> statuses, String description, List<Link> links, Long startTime, Long endTime)
            throws BSSException {
        this.log.info("list.start");
        this.reservations = new ArrayList<Reservation>();
        ArrayList<String> criteria = new ArrayList<String>();
        String loginQ = null;
        if (logins != null && !logins.isEmpty()) {
            loginQ = "r.login IN ("+Utils.join(logins, ",", "'", "'")+") ";
            criteria.add(loginQ);
        }

        String statusQ = null;
        if (statuses != null && !statuses.isEmpty()) {
            statusQ = "r.status IN ("+Utils.join(statuses, ",", "'", "'")+") ";
            criteria.add(statusQ);
        }

        String descriptionQ = null;
        if (description != null) {
            descriptionQ = " (r.description LIKE '%"+description+"%') or (r.globalReservationId LIKE '%"+description+"%') ";
            criteria.add(descriptionQ);
        }

        String startQ = null;
        if (startTime != null) {
            startQ = "(r.endTime >= :startTime) ";
            criteria.add(startQ);
        }
        String endQ = null;
        if (endTime != null) {
            endQ = "(r.startTime <= :endTime) ";
            criteria.add(endQ);
        }

        String hsql = "from Reservation r";
        if (!criteria.isEmpty()) {
            hsql += " where " +Utils.join(criteria, " and ", "(", ")");
        }

        this.log.debug("HSQL is: ["+hsql+"]");

        Query query = this.getSession().createQuery(hsql);
        if (startTime != null) {
            query.setLong("startTime", startTime);
        }
        if (endTime != null) {
            query.setLong("endTime", endTime);
        }


        this.reservations = query.list();

        if (links != null && !links.isEmpty()) {
            ArrayList<Reservation> removeThese = new ArrayList<Reservation>();
            for (Reservation rsv : this.reservations) {
                if (!rsv.getPath().containsAnyOf(links)) {
                    this.log.debug("not returning: " + rsv.getGlobalReservationId());
                    removeThese.add(rsv);
                }
            }
            for (Reservation rsv : removeThese) {
                this.reservations.remove(rsv);
            }
        }

        this.log.info("list.finish");
        return this.reservations;
    }


    /**
     * Retrieves the list of all pending and active reservations that
     * are within the given time interval.
     *
     * @param startTime proposed reservation start time
     * @param endTime proposed reservation end time
     * @return list of all pending and active reservations
     */
    @SuppressWarnings("unchecked")
    public List<Reservation>
            overlappingReservations(Long startTime, Long endTime) {

        this.reservations = null;
        // Get reservations with times overlapping that of the reservation
        // request.
        String hsql = "from Reservation r " +
            "where ((r.startTime <= :startTime and r.endTime >= :startTime) or " +
            "(r.startTime <= :endTime and r.endTime >= :endTime) or " +
            "(r.startTime >= :startTime and r.endTime <= :endTime)) " +
            "and (r.status = 'PENDING' or r.status = 'ACTIVE')";
        this.reservations = this.getSession().createQuery(hsql)
                                        .setLong("startTime", startTime)
                                        .setLong("endTime", endTime)
                                        .list();
        return this.reservations;
    }

    /**
     * Finds pending OSCARS reservations which now should become
     * active.
     *
     * @param timeInterval an int to add to the current time
     * @return list of pending reservations
     */
    @SuppressWarnings("unchecked")
    public List<Reservation> pendingReservations(int timeInterval) {

        this.reservations = null;
        long seconds = 0;

        seconds = System.currentTimeMillis()/1000 + timeInterval;
        String hsql = "from Reservation where status = :status " +
                      "and startTime < :startTime";
        this.reservations = this.getSession().createQuery(hsql)
                              .setString("status", "PENDING")
                              .setLong("startTime", seconds)
                              .list();
        return this.reservations;
    }


    /**
     * Finds current OSCARS reservations will become expired
     * some time in the future
     *
     * @param timeInterval An int to add to the current time
     * @return list of expired reservations
     */
    @SuppressWarnings("unchecked")
    public List<Reservation> expiringReservations(int offset, int interval) {

        this.reservations = null;
        long seconds = 0;

        long periodStart = offset;
        long periodEnd = offset + interval;

        String hsql = "from Reservation where " +
                      "((status = 'ACTIVE' or status= 'PENDING') and " +
                      " (endTime >= :periodStart and endTime <= :periodEnd)) or (status = 'PRECANCEL')";
        this.reservations = this.getSession().createQuery(hsql)
                              .setLong("periodStart", periodStart)
                              .setLong("periodEnd", periodEnd)
                              .list();
        return this.reservations;
    }

    /**
     * Finds current OSCARS reservations which now should be expired.
     *
     * @param timeInterval An int to add to the current time
     * @return list of expired reservations
     */
    @SuppressWarnings("unchecked")
    public List<Reservation> expiredReservations(int timeInterval) {

        this.reservations = null;
        long seconds = 0;

        seconds = System.currentTimeMillis()/1000 + timeInterval;
        String hsql = "from Reservation where " +
                      "((status = 'ACTIVE' or status= 'PENDING') and " +
                      "endTime < :endTime) or (status = 'PRECANCEL')";
        this.reservations = this.getSession().createQuery(hsql)
                              .setLong("endTime", seconds)
                              .list();
        return this.reservations;
    }

    /**
     * Retrieves a list of all reservations with the given status.
     *
     * @param status string with reservation status
     * @return list of all reservations with the given status
     */
    @SuppressWarnings("unchecked")
    public List<Reservation> statusReservations(String status) {

        this.reservations = null;
        // Get reservations with times overlapping that of the reservationi
        // request.
        String hsql = "from Reservation r " +
                      "where r.status = :status";
        this.reservations = this.getSession().createQuery(hsql)
                                        .setString("status", status)
                                        .list();
        return this.reservations;
    }



    /**
     * This function is meant to be called after a list() and should
     * return the number of reservations fitting the search criteria.
     *
     * @return how many reservations are in the result set
     *
     */
    public int resultsNum() {
        if (this.reservations == null) {
            return 0;
        }
        return this.reservations.size();
    }


    /**
     * Retrives reservation given the global reservation ID (GRI) and login
     *
     * @param gri global reservation id of entry to retrieve
     * @param login user's login that made the reservation
     * @return the reservation matching the given parameters
     */
    public Reservation queryByGRIAndLogin(String gri, String login){
        String hsql = "from Reservation r " +
                      "where r.globalReservationId = ? and r.login = ?";
        Reservation resv = (Reservation) this.getSession().createQuery(hsql)
                                        .setString(0, gri)
                                        .setString(1, login)
                                        .uniqueResult();

        return resv;
    }
}
