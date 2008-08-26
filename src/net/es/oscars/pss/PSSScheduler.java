package net.es.oscars.pss;

import java.util.*;
import org.apache.log4j.*;

import net.es.oscars.bss.*;
import net.es.oscars.notify.*;
import net.es.oscars.oscars.OSCARSCore;

/**
 * @author David Robertson (dwrobertson@lbl.gov), Jason Lee (jrlee@lbl.gov)
 *
 * This class is only designed to be invoked from a standalone program
 */
public class PSSScheduler {
    private Logger log;
    private String dbname;
    private PathSetupManager pathSetupManager;
    private OSCARSCore core;
    private StateEngine se;
    
    public PSSScheduler(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.pathSetupManager = new PathSetupManager(dbname);
        this.dbname = dbname;
        this.core = OSCARSCore.getInstance();
        this.se = this.core.getStateEngine();
    }


    /**
     * Handles setting up LSP's.
     *
     * @param timeInterval an integer with the time window to check
     * @return response the list of reservation that are pending
     */
    public List<Reservation> pendingReservations(Integer timeInterval)  {

        List<Reservation> reservations = null;
        EventProducer eventProducer = new EventProducer();

        ReservationDAO dao = new ReservationDAO(this.dbname);
        reservations = dao.pendingReservations(timeInterval);
        for (Reservation resv: reservations) {
            try {
                // call PSS to schedule LSP setup(s)
                String pathSetupMode = resv.getPath().getPathSetupMode();
                this.log.info("pendingReservation: " + resv.getGlobalReservationId());
                if (pathSetupMode.equals("timer-automatic")) {
                    // resv must be set to proper status inside PSS
                    //TODO: Throw RESV_PERIOD_STARTED for signal-xml only once
                    eventProducer.addEvent(OSCARSEvent.RESV_PERIOD_STARTED, "", "SCHEDULER", resv);
                    this.pathSetupManager.create(resv, true);
                    eventProducer.addEvent(OSCARSEvent.PATH_SETUP_STARTED, "", "SCHEDULER", resv);
                }
                
            } catch (Exception ex) {
                try {
                    this.se.updateStatus(resv, StateEngine.FAILED);
                } catch (BSSException stateEx) {
                    this.log.error(ex.getMessage());
                } finally {
                    eventProducer.addEvent(OSCARSEvent.PATH_SETUP_FAILED, "", "SCHEDULER", resv, "", ex.getMessage());
                    this.log.error(ex.getMessage());
                }
            }

        }
        return reservations;
    }

    /**
     * Handles tearing down LSP's.
     *
     * @param timeInterval an integer with the time window to check
     * @return response a list of reservations that have expired
     */
    public List<Reservation> expiredReservations(Integer timeInterval) {

        List<Reservation> reservations = null;

        ReservationDAO dao = new ReservationDAO(this.dbname);
        reservations = dao.expiredReservations(timeInterval);
        for (Reservation resv: reservations) {
            EventProducer eventProducer = new EventProducer();

            try {
                // call PSS to schedule LSP teardown(s)
                String status = resv.getStatus();
                this.log.info("expiredReservation: " + resv.getGlobalReservationId());
                eventProducer.addEvent(OSCARSEvent.RESV_PERIOD_FINISHED, "", "SCHEDULER", resv);
                if(status.equals(StateEngine.ACTIVE)){
                    this.pathSetupManager.teardown(resv, StateEngine.FINISHED);
                    eventProducer.addEvent(OSCARSEvent.PATH_TEARDOWN_STARTED, "", "SCHEDULER", resv);
                 }else{
                    //if still in RESERVED state
                    this.se.updateStatus(resv, StateEngine.FINISHED);
                 }
            } catch (Exception ex) {
                try {
                    this.se.updateStatus(resv, StateEngine.FAILED);
                } catch (BSSException stateEx) {
                    this.log.error(ex.getMessage());
                } finally {
                    eventProducer.addEvent(OSCARSEvent.PATH_TEARDOWN_FAILED, "", "SCHEDULER", resv, "", ex.getMessage());
                    this.log.error(ex.getMessage());
                }
            }
            dao.update(resv);
        }
        return reservations;
    }

    /**
     * Sends notifications for reservations that will expire in the future
     *
     * @param timeInterval an integer with the time window to check
     */
    public void expiringReservations(Integer timeInterval) {
        // Check for reservations expiring in:
        // 1 day
        // 7 days
        // 30 days
        // send out a notification each time
        Integer days_1 = 3600*24;
        Integer days_7 = 7 * days_1;
        Integer days_30 = 30 * days_1;

        List<Reservation> reservations = null;
        ReservationDAO dao = new ReservationDAO(this.dbname);
        EventProducer eventProducer = new EventProducer();

        // 1 day
        reservations = dao.expiringReservations(days_1, timeInterval);
        for (Reservation resv: reservations) {
            eventProducer.addEvent(OSCARSEvent.RESV_EXPIRES_IN_1DAY, "", "SCHEDULER", resv);
        }

        // 7 days
        reservations = dao.expiringReservations(days_7, timeInterval);
        for (Reservation resv: reservations) {
            eventProducer.addEvent(OSCARSEvent.RESV_EXPIRES_IN_7DAYS, "", "SCHEDULER", resv);
        }

        // 30 days
        reservations = dao.expiringReservations(days_30, timeInterval);
        for (Reservation resv: reservations) {
            eventProducer.addEvent(OSCARSEvent.RESV_EXPIRES_IN_30DAYS, "", "SCHEDULER", resv);
        }


    }


}
