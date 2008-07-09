package net.es.oscars.bss;

import org.apache.log4j.Logger;
import org.hibernate.*;

import net.es.oscars.oscars.*;
import net.es.oscars.database.*;

public class StateEngine {

    public final static String CREATED = "CREATED";
    public final static String RESERVED = "PENDING";
    public final static String PENDING = "PENDING"; // to be removed..
    public final static String ACTIVE = "ACTIVE";
    public final static String FINISHED = "FINISHED";
    public final static String CANCELLED = "CANCELLED";
    public final static String FAILED = "FAILED";
    public final static String INSETUP = "INSETUP";
    public final static String INTEARDOWN = "INTEARDOWN";
    public final static String INMODIFY = "INMODIFY";

    private String dbname;
    private OSCARSCore core;
    private String status;
    private Logger log;

    public StateEngine() {
        this.dbname = OSCARSCore.getInstance().getBssDbName();
        this.log = Logger.getLogger(this.getClass());
    }

    public synchronized String updateStatus(Reservation resv, String newStatus) throws BSSException {
        if (this.status == null) {
            this.status = resv.getStatus();
        }
        if (newStatus.equals(CREATED)) {
            // always go there
        } else if (newStatus.equals(FAILED)) {
            // always go there
        } else if (newStatus.equals(RESERVED)) {
            if (!status.equals(CREATED)) {
                throw new BSSException("Current status is "+status+"; cannot become RESERVED");
            }
        } else if (newStatus.equals(INMODIFY)) {
            if (!status.equals(RESERVED) && !status.equals(ACTIVE)) {
                throw new BSSException("Current status is "+status+"; cannot modify");
            }
        } else if (newStatus.equals(INSETUP)) {
            if (!status.equals(RESERVED)) {
                throw new BSSException("Current status is "+status+"; cannot setup");
            }
        } else if (newStatus.equals(ACTIVE)) {
            if (!status.equals(INSETUP) && !status.equals(ACTIVE)) {
                throw new BSSException("Current status is "+status+"; cannot make active");
            }
        } else if (newStatus.equals(INTEARDOWN)) {
            if (!status.equals(ACTIVE)) {
                throw new BSSException("Current status is "+status+"; cannot tear down");
            }
        } else if (newStatus.equals(FINISHED)) {
            if (!status.equals(RESERVED) && !status.equals(INTEARDOWN)) {
                throw new BSSException("Current status is "+status+"; cannot finish");
            }
        } else if (newStatus.equals(CANCELLED)) {
            if (!status.equals(RESERVED) && !status.equals(INTEARDOWN)) {
                throw new BSSException("Current status is "+status+"; cannot cancel");
            }
        }
        status = newStatus;
        resv.setStatus(status);
        ReservationDAO resvDAO = new ReservationDAO(this.dbname);
        resvDAO.update(resv);
        return status;
    }



    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }



}
