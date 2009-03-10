package net.es.oscars.bss;

import java.util.*;
import org.apache.log4j.Logger;
import org.hibernate.*;

public class StateEngine {

    public final static String SUBMITTED = "SUBMITTED";
    public final static String ACCEPTED = "ACCEPTED";
    public final static String INCREATE = "INCREATE";
    public final static String RESERVED = "PENDING";
    public final static String ACTIVE = "ACTIVE";
    public final static String INSETUP = "INSETUP";
    public final static String INTEARDOWN = "INTEARDOWN";
    public final static String FINISHED = "FINISHED";
    public final static String CANCELLED = "CANCELLED";
    public final static String FAILED = "FAILED";
    public final static String INMODIFY = "INMODIFY";
    
    //local statuses
    public final static int LOCAL_INIT = 0;
    public final static int CONFIRMED = 1;
    public final static int MODIFY_ACTIVE = 2;
    public final static int DOWN_CONFIRMED = 2;
    public final static int UP_CONFIRMED = 4;
    public final static int COMPLETED = 7;
    public final static int NEXT_STATUS = 24; //the next status bits
    public final static int NEXT_STATUS_RESERVED = 0;
    public final static int NEXT_STATUS_CANCEL = 8;
    public final static int NEXT_STATUS_FINISHED = 16;
    public final static int NEXT_STATUS_FAILED = 24;
    
    private String dbname;
    private static HashMap<String, String> statusMap = new HashMap<String, String>();
    private static HashMap<String, Integer> localStatusMap = new HashMap<String, Integer>();
    private Logger log;

    public StateEngine() {
        this.dbname = OSCARSCore.getInstance().getBssDbName();
        this.log = Logger.getLogger(this.getClass());
    }

    public synchronized String updateStatus(Reservation resv, String newStatus) throws BSSException {
        return this.updateStatus(resv, newStatus, true);
    }

    public synchronized String updateStatus(Reservation resv, String newStatus, boolean persist) throws BSSException {
        String gri = resv.getGlobalReservationId();

        // initialize this if this is the first time
        if (StateEngine.statusMap.get(gri) == null) {
            StateEngine.statusMap.put(gri, resv.getStatus());
        }
        String status = StateEngine.statusMap.get(gri);

        StateEngine.canModifyStatus(status, newStatus);

        status = newStatus;
        resv.setStatus(status);
        if (persist) {
            ReservationDAO resvDAO = new ReservationDAO(this.dbname);
            resvDAO.update(resv);
        }
        StateEngine.statusMap.put(gri, status);
        return status;
    }

    // This is intentionally not synchronized, we do not want to block when reading the status
    public static void canUpdateStatus(Reservation resv, String newStatus) throws BSSException {
        String status = StateEngine.getStatus(resv);
        StateEngine.canModifyStatus(status, newStatus);
    }

    // Business / state diagram logic goes here
    public static void canModifyStatus(String status, String newStatus) throws BSSException {
        boolean allowed = true;
        if (newStatus.equals(status)) {
            // no-ops always allowed
        } else if (newStatus.equals(SUBMITTED)) {
            // always allowed, must not abuse..
        } else if (newStatus.equals(FAILED)) {
            // always allowed, must not abuse..
        } else if (newStatus.equals(ACCEPTED)) {
            if (!status.equals(SUBMITTED)) {
                allowed = false;
            }
        } else if (newStatus.equals(INCREATE)) {
            if (!status.equals(ACCEPTED)) {
                allowed = false;
            }
        } else if (newStatus.equals(RESERVED)) {
            if (!status.equals(INCREATE) && !status.equals(INTEARDOWN) && !status.equals(INMODIFY)) {
                allowed = false;
            }
        } else if (newStatus.equals(INMODIFY)) {
            if (!status.equals(RESERVED) && !status.equals(ACTIVE)) {
                allowed = false;
            }
        } else if (newStatus.equals(INSETUP)) {
            if (!status.equals(RESERVED)) {
                allowed = false;
            }
        } else if (newStatus.equals(ACTIVE)) {
            if (!status.equals(INSETUP) && !status.equals(INMODIFY)) {
                allowed = false;
            }
        } else if (newStatus.equals(INTEARDOWN)) {
            if (!status.equals(ACTIVE) && !status.equals(INSETUP)) {
                allowed = false;
            }
        } else if (newStatus.equals(FINISHED)) {
            if (!status.equals(RESERVED) && !status.equals(INTEARDOWN)) {
                allowed = false;
            }
        } else if (newStatus.equals(CANCELLED)) {
            if (!status.equals(ACCEPTED) && !status.equals(RESERVED) && !status.equals(INTEARDOWN)) {
                allowed = false;
            }
        }
        if (!allowed) {
            throw new BSSException("Current status is "+status+"; cannot change to "+newStatus);
        }
    }
    
    public static void canUpdateLocalStatus(Reservation resv, int newLocalStatus) throws BSSException{
        boolean allowed = true;
        String status = StateEngine.getStatus(resv);
        int localStatus = resv.getLocalStatus();
        if(StateEngine.LOCAL_INIT == newLocalStatus){
            //always allow reset
            allowed = true;
        }else if(status.equals(INCREATE) ){
            if((localStatus ^ newLocalStatus) != 1){
                allowed = false;
            }
        }else if(status.equals(INMODIFY)){
            //bits: confirmed
            if(newLocalStatus > (MODIFY_ACTIVE + CONFIRMED) || 
               (localStatus == CONFIRMED && newLocalStatus > CONFIRMED) ||
               (localStatus > CONFIRMED && newLocalStatus == CONFIRMED)){
              allowed = false;   
            }
        }else if(status.equals(INSETUP)){
            //bits: upstream, downstream, local
            if(newLocalStatus > COMPLETED || newLocalStatus < localStatus){
                allowed = false;
            }
        }else if(status.equals(RESERVED) || status.equals(ACTIVE)){
            //bits: cancel,empty,empty,confirm
            if((newLocalStatus & 6) != 0){
                allowed = false;
            }
        }else if(status.equals(INTEARDOWN)){
            //bits: newStatus, newStatus,upstream, downstream, local
            //newStatus: 00=RESERVED,01=CANCELLED, 10=FINISHED, 11=FAILED
            if(newLocalStatus > (NEXT_STATUS_FAILED + COMPLETED) || 
               newLocalStatus < localStatus){
                allowed = false;
            }
        }else if(newLocalStatus != 0){
            allowed = false;
        }
        
        if (!allowed) {
            throw new BSSException("Current local status is "+localStatus+";" +
                                   "cannot change to "+newLocalStatus);
        }
    }
    
    public synchronized int updateLocalStatus(Reservation resv, int newLocalStatus) throws BSSException {
        return this.updateLocalStatus(resv, newLocalStatus, true);
    }

    public synchronized int updateLocalStatus(Reservation resv, int newLocalStatus, boolean persist) throws BSSException {
        String gri = resv.getGlobalReservationId();

        // initialize this if this is the first time
        if (StateEngine.localStatusMap.get(gri) == null) {
            StateEngine.localStatusMap.put(gri, resv.getLocalStatus());
        }
        int status = StateEngine.localStatusMap.get(gri);

        StateEngine.canUpdateLocalStatus(resv, newLocalStatus);

        status = newLocalStatus;
        resv.setLocalStatus(status);
        if (persist) {
            ReservationDAO resvDAO = new ReservationDAO(this.dbname);
            resvDAO.update(resv);
        }
        StateEngine.localStatusMap.put(gri, status);
        return status;
    }
    
    /**
     * @return the local status
     */
    public static int getLocalStatus(Reservation resv) {
        String gri = resv.getGlobalReservationId();
        int status = 0;
        // if the state engine has not been initialized, return what is in the Reservation object
        if (StateEngine.localStatusMap.get(gri) != null) {
            status = StateEngine.localStatusMap.get(gri);
        } else {
            status = resv.getLocalStatus();
        }

        return status;
    }

    /**
     * @return the status
     */
    public static String getStatus(Reservation resv) {
        String gri = resv.getGlobalReservationId();
        String status = null;
        // if the state engine has not been initialized, return what is in the Reservation object
        if (StateEngine.statusMap.get(gri) != null) {
            status = StateEngine.statusMap.get(gri);
        } else {
            status = resv.getStatus();
        }

        return status;
    }
    
    /**
     * Utility function for verifying the status is up-to-date before committing a 
     * Hibernate session. The statusMap is synchronized so will always be the most 
     * accurate representation of the status. During interdomain path setup this call should 
     * ALWAYS be used to commit the transaction. There is a time between when a path 
     * receives a notification and makes a change where a race condition exists without this.
     * 
     * @param resv the reservation to save
     * @param bss the Hibernate session to commit
     */
    public synchronized void safeHibernateCommit(Reservation resv, Session bss){
        //sets the reservation status to the value of the status map
        resv.setStatus(StateEngine.getStatus(resv));
        //sets the reservation local status to the value of the local status map
        resv.setLocalStatus(StateEngine.getLocalStatus(resv));
        bss.beginTransaction().commit();
    }

}
