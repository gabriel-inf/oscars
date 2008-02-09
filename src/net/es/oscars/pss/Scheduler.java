package net.es.oscars.pss;

import java.util.*;
import java.io.*;
import java.lang.Throwable;
import javax.mail.MessagingException;

import org.apache.log4j.*;
import org.hibernate.*;

import net.es.oscars.Notifier;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.ReservationDAO;
import net.es.oscars.interdomain.InterdomainException;

/**
 * @author David Robertson (dwrobertson@lbl.gov), Jason Lee (jrlee@lbl.gov)
 *
 * This class is only designed to be invoked from a standalone program
 */
public class Scheduler {
    private Logger log;
    private Notifier notifier;
    private String dbname;
    private PathSetupManager pathSetupManager;

    public Scheduler(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.pathSetupManager = new PathSetupManager(dbname);
        this.notifier = new Notifier();
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
            try {
                if (success || badSetup) {
                    this.notifier.sendMessage(subject, notification);
                }
            } catch (javax.mail.MessagingException ex) {
                this.log.info("create.mail.exception: " + ex.getMessage());
            } catch (UnsupportedOperationException ex) {
                this.log.info("create.mail.unsupported: " + ex.getMessage());
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
            try {
                this.notifier.sendMessage(subject, notification);
            } catch (javax.mail.MessagingException ex) {
                this.log.info("create.mail.exception: " + ex.getMessage());
            } catch (UnsupportedOperationException ex) {
                this.log.info("create.mail.unsupported: " + ex.getMessage());
            }
            dao.update(resv);
        }
        return reservations;
    }
}
