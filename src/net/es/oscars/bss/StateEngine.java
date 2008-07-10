package net.es.oscars.bss;

import java.util.*;
import org.apache.log4j.Logger;
import org.hibernate.*;

import net.es.oscars.oscars.*;
import net.es.oscars.database.*;

public class StateEngine {

    public final static String SUBMITTED = "SUBMITTED";
    public final static String ACCEPTED = "ACCEPTED";
    public final static String RESERVED = "PENDING";
    public final static String PENDING = "PENDING"; // to be removed..
    public final static String ACTIVE = "ACTIVE";
    public final static String INSETUP = "INSETUP";
    public final static String INTEARDOWN = "INTEARDOWN";
    public final static String FINISHED = "FINISHED";
    public final static String CANCELLED = "CANCELLED";
    public final static String FAILED = "FAILED";
    public final static String INMODIFY = "INMODIFY";

    private String dbname;
    private static HashMap<String, String> statusMap = new HashMap<String, String>();
    private Logger log;

    public StateEngine() {
        this.dbname = OSCARSCore.getInstance().getBssDbName();
        this.log = Logger.getLogger(this.getClass());
    }

    public synchronized String updateStatus(Reservation resv, String newStatus) throws BSSException {
        String gri = resv.getGlobalReservationId();

        // initialize this if this is the first time
        if (StateEngine.statusMap.get(gri) == null) {
            StateEngine.statusMap.put(gri, resv.getStatus());
        }
        String status = StateEngine.statusMap.get(gri);

        StateEngine.canModifyStatus(status, newStatus);

        status = newStatus;
        resv.setStatus(status);
        ReservationDAO resvDAO = new ReservationDAO(this.dbname);
        resvDAO.update(resv);
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
        } else if (newStatus.equals(RESERVED)) {
            if (!status.equals(ACCEPTED) && !status.equals(INTEARDOWN) && !status.equals(INMODIFY)) {
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
            if (!status.equals(ACTIVE)) {
            	allowed = false;
            }
        } else if (newStatus.equals(FINISHED)) {
            if (!status.equals(RESERVED) && !status.equals(INTEARDOWN)) {
            	allowed = false;
            }
        } else if (newStatus.equals(CANCELLED)) {
            if (!status.equals(RESERVED) && !status.equals(INTEARDOWN)) {
            	allowed = false;
            }
        }
    	if (!allowed) {
    		throw new BSSException("Current status is "+status+"; cannot change to "+newStatus);
    	}
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



}
