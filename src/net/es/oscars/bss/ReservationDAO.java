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
    
    private final static String DEFAULT_LIST_SORT = "startTime desc";
    private final static String[] VALID_SORT_FIELDS = {
        "id", "startTime", "endTime", "createdTime", "bandwidth", "login", 
        "payloadSender", "status", "description", "globalReservationId"
    };
    
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
        if (reservation == null) {
            throw new BSSException ("No reservation matches requested gri: " + gri);
        }
        return reservation;
    }

    /**
     * Lists reservations
     *
     * @param numRequested int with the number of reservations to return
     * @param resOffset int with the offset into the list 
     *
     * @param logins a list of user logins. If not null or empty, results will
     * only include reservations submitted by these specific users. If null / empty
     * results will include reservations by all users.
     *
     * @param statuses a list of reservation statuses. If not null or empty,
     * results wil only include reservations with one of these statuses.
     * If null / empty, results will include reservations with any status.
     *
     * @param vlanTags a list of VLAN tags.  If not null or empty,
     * results will only include reservations where (currently) the first link
     * in the path has a VLAN tag from the list (or ranges in the list).  If
     * null / empty, results will include reservations with any associated
     * VLAN.
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
    public List<Reservation> list(int numRequested, int resOffset,
            List<String> logins, List<String> statuses, String description,
            List<String> vlanTags, Long startTime, Long endTime, String sortBy)
                throws BSSException {

        this.log.debug("list.start");
        this.reservations = new ArrayList<Reservation>();
        ArrayList<String> criteria = new ArrayList<String>();
        String loginQ = null;
        if (logins != null && !logins.isEmpty()) {
            loginQ = "r.login IN ("+BssUtils.join(logins, ",", "'", "'")+") ";
            criteria.add(loginQ);
        }

        String statusQ = null;
        if (statuses != null && !statuses.isEmpty()) {
            statusQ = "r.status IN ("+BssUtils.join(statuses, ",", "'", "'")+") ";
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
            hsql += " where " +BssUtils.join(criteria, " and ", "(", ")");
        }
        hsql += " order by r." + this.checkSortBy(sortBy);

        this.log.debug("HSQL is: ["+hsql+"]");

        Query query = this.getSession().createQuery(hsql);
        // if zero, get everything (needed for browser)
        if (numRequested > 0) {
            query.setMaxResults(numRequested);
            query.setFirstResult(resOffset);
        }
        if (startTime != null) {
            query.setLong("startTime", startTime);
        }
        if (endTime != null) {
            query.setLong("endTime", endTime);
        }

        this.reservations = query.list();
        this.log.debug("done with Hibernate query");

        if (vlanTags != null && !vlanTags.isEmpty() &&
            !vlanTags.contains("any")) {
            ArrayList<Reservation> removeThese = new ArrayList<Reservation>();
            for (Reservation rsv : this.reservations) {
                if (!this.containsVlan(rsv, vlanTags)) {
                    removeThese.add(rsv);
                }
            }
            for (Reservation rsv : removeThese) {
                this.reservations.remove(rsv);
            }
        }
        this.log.debug("list.finish");
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

        ArrayList<String> states = new ArrayList<String>();
        states.add(StateEngine.RESERVED);
        states.add(StateEngine.INCREATE);
        states.add(StateEngine.ACTIVE);
        states.add(StateEngine.INMODIFY);
        states.add(StateEngine.INSETUP);
        states.add(StateEngine.INTEARDOWN);
        StringBuilder sb = new StringBuilder();
        Iterator<String> iter = states.iterator();
        if (iter.hasNext()) {
            String status = "'"+iter.next()+"'";
            sb.append(status);
            while (iter.hasNext()) {
                status = "'"+iter.next()+"'";
                sb.append(",");
                sb.append(status);
            }
        }
        String stateClause = sb.toString();

        // Get reservations with times overlapping that of the reservation
        // request.
        String hsql = "from Reservation r " +
            "where ((r.startTime <= :startTime and r.endTime >= :startTime) or " +
            "(r.startTime <= :endTime and r.endTime >= :endTime) or " +
            "(r.startTime >= :startTime and r.endTime <= :endTime)) " +
            "and (r.status IN (" + stateClause + "))";
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
                      "and localStatus=0 " +
                      "and startTime < :startTime " +
                      "and endTime > :now "+
                      " order by startTime ";
        this.reservations = this.getSession().createQuery(hsql)
                              .setString("status", StateEngine.RESERVED)
                              .setLong("startTime", seconds)
                              .setLong("now", seconds)
                              .list();
        return this.reservations;
    }


    /**
     * Finds current OSCARS reservations that will expire
     * during the specified time interval
     *
     * @param offset start time of the interval in seconds since 12AM, Jan 1, 1970.
     * @param interval the length of the interval to check.
     * @return list of expired reservations
     */
    @SuppressWarnings("unchecked")
    public List<Reservation> expiringReservations(int offset, int interval) {

        this.reservations = null;
        long seconds = 0;


        ArrayList<String> states = new ArrayList<String>();
        states.add(StateEngine.FAILED);
        states.add(StateEngine.CANCELLED);
        states.add(StateEngine.FINISHED);
        StringBuilder sb = new StringBuilder();
        Iterator<String> iter = states.iterator();
        if (iter.hasNext()) {
            String status = "'"+iter.next()+"'";
            sb.append(status);
            while (iter.hasNext()) {
                status = "'"+iter.next()+"'";
                sb.append(",");
                sb.append(status);
            }
        }
        String stateClause = sb.toString();

        long periodStart = offset;
        long periodEnd = offset + interval;

        String hsql = "from Reservation where " +
                      " ( (status NOT IN (:stateClause)) and localStatus=0 and" +
                      "   (endTime >= :periodStart and endTime <= :periodEnd) )" +
                      " order by endTime";

        this.reservations = this.getSession().createQuery(hsql)
                              .setString("stateClause", stateClause)
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
                      "endTime <= :endTime) or (status = 'PRECANCEL')";
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

    /**
     * Checks to see whether first element in path contains a VLAN in the
     * list of tags or ranges specified.
     *
     * @param rsv Reservation to check
     * @param vlanTags list of tags or tag ranges to check
     *
     * @return whether first element in path has a matching VLAN
     */
    private boolean containsVlan(Reservation rsv, List<String> vlanTags) throws BSSException {
        int checkVtag = -1;
        int minVtag = 100000;
        int maxVtag = -1;
        List<String> tagStrs = BssUtils.getVlanTags(rsv.getPath(PathType.LOCAL));
        if (tagStrs.isEmpty()) {
            return false;
        }
        String tagStr = tagStrs.get(0);
        // no associated VLAN
        if (tagStr == null) {
            return false;
        }
        int resvVtag = Math.abs(Integer.parseInt(tagStr));
        for (String v: vlanTags) {
            String[] range = v.split("-");
            // single number
            if (range.length == 1) {
                try {
                    checkVtag = Integer.parseInt(range[0]);
                } catch (NumberFormatException ex) {
                    continue;
                }
                if (checkVtag == resvVtag) {
                    return true;
                }
            } else if (range.length == 2) {
                try {
                    minVtag = Integer.parseInt(range[0]);
                    maxVtag = Integer.parseInt(range[1]);
                } catch (NumberFormatException ex) {
                    continue;
                }
                if ((resvVtag >= minVtag) && (resvVtag <= maxVtag)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Verifies that the sortBY field of a list request is valud. If not then
     * uses a default value.
     * 
     * @param sortBy the sortBy value to verify
     * @return the input value if valid, otherwise a default value
     */
    private String checkSortBy(String sortBy){
        if(sortBy == null){
            return DEFAULT_LIST_SORT;
        }
        sortBy = sortBy.trim();
        
        //check parts: should be form 'field asc|desc'
        String[] sortByParts = sortBy.split(" ");
        if(sortByParts.length == 0 || sortByParts.length > 2){
            return DEFAULT_LIST_SORT;
        }
        
        //check if valid field
        boolean isValidField = false;
        for(String validFieldName : VALID_SORT_FIELDS){
            if(validFieldName.equals(sortByParts[0])){
                isValidField = true;
                break;
            }
        }
        if(!isValidField){
            return DEFAULT_LIST_SORT;
        }
        
        //check for asc/desc
        if(sortByParts.length == 2 && 
                !("asc".equals(sortByParts[1]) || "desc".equals(sortByParts[1]))){
            return DEFAULT_LIST_SORT;
        }
        
        return sortBy;
    }
}
