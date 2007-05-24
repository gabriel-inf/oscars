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

    public ReservationDAO(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.setDatabase(dbname);
        this.dbname = dbname;
    }

    /**
     * Finds a reservation, given its tag.
     *
     * @param tag a string uniquely identifying a reservation across domains
     * @param authorized a boolean indicating whether a user can view all
     *     of a reservation's fields:  TODO:  use
     * @return reservation found, if any
     * @throws BSSException
     */
    public Reservation query(String tag, boolean authorized)
        throws BSSException {

        String[] strArray = tag.split("-");
        // TODO:  FIX
        Integer id = Integer.valueOf(strArray[1]);
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
            this.log.info("list, individual: " + login);
            String hsql = "from Reservation r where r.login = :login " +
                          "order by r.startTime desc";
            reservations =  this.getSession().createQuery(hsql)
                                   .setString("login", login)
                                   .list();
        } else {
            this.log.info("list, all");
            String hsql = "from Reservation r order by r.startTime desc";
            reservations = this.getSession().createQuery(hsql).list();
        }
        return reservations;
    }


    /**
     * Retrieves the list of all pending and active reservations that
     * are within the given time interval.
     *
     * @param startTime proposed reservation start time
     * @param endTime proposed reservation end time
     * @return list of all pending and active reservations
     */
    public List<Reservation>
            overlappingReservations(Long startTime, Long endTime) {

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
     * Finds pending OSCARS reservations which now should become
     * active.
     *
     * @param timeInterval an int to add to the current time
     * @return list of pending reservations
     */
    public List<Reservation> pendingReservations(int timeInterval) {

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
     * Finds current OSCARS reservations which now should be expired.
     *
     * @param timeInterval An int to add to the current time
     * @return list of expired reservations
     */
    public List<Reservation> expiredReservations(int timeInterval) {

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

    /**
     * Retrieves a list of all reservations with the given status.
     *
     * @param status string with reservation status
     * @return list of all reservations with the given status
     */
    public List<Reservation> statusReservations(String status) {

        List<Reservation> reservations = null;
        // Get reservations with times overlapping that of the reservationi
        // request.
        String hsql = "from Reservation r " +
                      "where r.status = :status";
        reservations = this.getSession().createQuery(hsql)
                                        .setString("status", status)
                                        .list();
        return reservations;
    }
}
