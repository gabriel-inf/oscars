package net.es.oscars.pss;

import java.util.*;
import java.io.*;
import java.lang.Throwable;

import org.apache.log4j.*;
import org.hibernate.*;

import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.ReservationDAO;
import net.es.oscars.interdomain.InterdomainException;
import net.es.oscars.notify.*;

/**
 * @author David Robertson (dwrobertson@lbl.gov), Jason Lee (jrlee@lbl.gov)
 *
 * This class is only designed to be invoked from a standalone program
 */
public class Scheduler {
    private Logger log;
    private NotifyInitializer notifier;
    private String dbname;
    private PathSetupManager pathSetupManager;

    public Scheduler(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.pathSetupManager = new PathSetupManager(dbname);
        this.notifier = new NotifyInitializer();
        try {
            this.notifier.init();
        } catch (NotifyException ex) {
            this.log.error("*** COULD NOT INITIALIZE NOTIFIER ***");
            // TODO:  ReservationAdapter, ReservationManager, etc. will
            // have init methods that throw exceptions that will not be
            // ignored it NotifyInitializer cannot be created.  Don't
            // want exceptions in constructor
        }
        this.dbname = dbname;
    }


    /**
     * Handles setting up LSP's.
     *
     * @param timeInterval an integer with the time window to check
     * @return response the list of reservation that are pending
     */
    public List<Reservation> pendingReservations(Integer timeInterval)  {

        List<Reservation> reservations = null;
        boolean success = false;
        boolean badSetup = false;

        ReservationDAO dao = new ReservationDAO(this.dbname);
        reservations = dao.pendingReservations(timeInterval);
        for (Reservation resv: reservations) {
            String subject = "Circuit set up for " +
                             resv.getGlobalReservationId();
            String notification = "Circuit set up for reservation ";
            try {
                // call PSS to schedule LSP
                String pathSetupMode = resv.getPath().getPathSetupMode();
                this.log.info("pendingReservation: " +
                              resv.getGlobalReservationId());
                if (pathSetupMode.equals("timer-automatic")) {
                    // resv set to proper status inside dragon, cisco, or jnx
                    String status = this.pathSetupManager.create(resv, true);
                    success = true;
                }
                subject += " succeeded";
                notification += "succeeded.\n" + resv.toString(this.dbname) + "\n";
            } catch (PSSException ex) {
                // set to FAILED, and log
                resv.setStatus("FAILED");
                badSetup = true;
                subject += " failed";
                notification += "failed with " + ex.getMessage() +
                                "\n" + resv.toString(this.dbname) + "\n";
                this.log.error(notification);
            } catch (InterdomainException ex) {
                // set to FAILED, and log
                resv.setStatus("FAILED");
                badSetup = true;
                subject += " failed";
                notification += "failed with " + ex.getMessage() +
                                "\n" + resv.toString(this.dbname) + "\n";
                this.log.error(notification);
            } catch (Exception ex) {
                // set to FAILED, and log
                resv.setStatus("FAILED");
                badSetup = true;
                subject += " failed";
                notification += "failed with " + ex.getMessage() +
                                "\n" + resv.toString(this.dbname) + "\n";
                this.log.error(notification);
            }
            if (success || badSetup) {
                Map<String,String> messageInfo = new HashMap<String,String>();
                messageInfo.put("subject", subject);
                messageInfo.put("body", notification);
                messageInfo.put("alertLine", resv.getDescription());
                NotifierSource observable = this.notifier.getSource();
                Object obj = (Object) messageInfo;
                observable.eventOccured(obj);
            }
            dao.update(resv);
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
        String prevStatus = null;
        String newStatus = null;

        ReservationDAO dao = new ReservationDAO(this.dbname);
        reservations = dao.expiredReservations(timeInterval);
        for (Reservation resv: reservations) {
            String subject = "Circuit teardown for " +
                             resv.getGlobalReservationId();
            String notification = "Circuit tear down for reservation ";
            try {
                // call PSS to tear down LSP
                prevStatus = resv.getStatus();
                // this should only happen due to a failed interdomain
                // communication
                if (prevStatus.equals("PENDING")) {
                    String errMsg = "pending reservation " +
                        "expired without ever having been set up";
                    throw new PSSException(errMsg);
                }
                this.log.info("expiredReservation: " +
                              resv.getGlobalReservationId());
                String status = this.pathSetupManager.teardown(resv, false);
                if (status.equals("CANCELLED")) {
                    subject += " because of cancellation";
                    notification += "occurred because of cancellation.\n";
                } else {
                    subject += " succeeded";
                    notification += "succeeded.\n";
               }
                notification += resv.toString(this.dbname) + "\n";
            } catch (PSSException ex) {
                // set to FAILED, and log
                resv.setStatus("FAILED");
                subject += " failed";
                notification += "failed with " + ex.getMessage() +
                                "\n" + resv.toString(this.dbname) + "\n";
                this.log.error(notification);
            } catch (Exception ex) {
                // set to FAILED, and log
                resv.setStatus("FAILED");
                subject += " failed";
                notification += "failed with " + ex.getMessage() +
                                "\n" + resv.toString(this.dbname) + "\n";
            }
            Map<String,String> messageInfo = new HashMap<String,String>();
            messageInfo.put("subject", subject);
            messageInfo.put("body", notification);
            messageInfo.put("alertLine", resv.getDescription());
            NotifierSource observable = this.notifier.getSource();
            Object obj = (Object) messageInfo;
            observable.eventOccured(obj);
            dao.update(resv);
        }
        return reservations;
    }

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

        NotifierSource observable = this.notifier.getSource();
        Map<String,String> messageInfo = new HashMap<String,String>();

        ReservationDAO dao = new ReservationDAO(this.dbname);
        String subject = "Expiring OSCARS reservation";
        String body = "";

        // 1 day
        reservations = dao.expiringReservations(days_1, timeInterval);
        for (Reservation resv: reservations) {
            body = "Reservation with GRI: "+resv.getGlobalReservationId()+" expiring in 24 hours.";

            messageInfo.put("subject", subject);
            messageInfo.put("body", body);
            messageInfo.put("alertLine", resv.getDescription());
            Object obj = (Object) messageInfo;
            observable.eventOccured(obj);
        }

        // 7 days
        reservations = dao.expiringReservations(days_7, timeInterval);
        for (Reservation resv: reservations) {
            body = "Reservation with GRI: "+resv.getGlobalReservationId()+" expiring in 7 days.";

            messageInfo.put("subject", subject);
            messageInfo.put("body", body);
            messageInfo.put("alertLine", resv.getDescription());
            Object obj = (Object) messageInfo;
            observable.eventOccured(obj);
        }

        // 30 days
        reservations = dao.expiringReservations(days_30, timeInterval);
        for (Reservation resv: reservations) {
            body = "Reservation with GRI: "+resv.getGlobalReservationId()+" expiring in 30 days.";

            messageInfo.put("subject", subject);
            messageInfo.put("body", body);
            messageInfo.put("alertLine", resv.getDescription());
            Object obj = (Object) messageInfo;
            observable.eventOccured(obj);
        }


    }


}
