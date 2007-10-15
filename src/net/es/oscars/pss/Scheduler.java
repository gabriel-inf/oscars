package net.es.oscars.pss;

import java.util.*;
import java.io.*;
import java.lang.Throwable;
import javax.mail.MessagingException;

import org.apache.log4j.*;
import org.hibernate.*;

import net.es.oscars.Notifier;
import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.ReservationDAO;

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
     * @throws PSSException
     */
    public List<Reservation> pendingReservations(Integer timeInterval) 
            throws PSSException {

        List<Reservation> reservations = null;

        try {
            ReservationDAO dao = new ReservationDAO(this.dbname);
            reservations = dao.pendingReservations(timeInterval);
            for (Reservation resv: reservations) {
                // call PSS to schedule LSP
                String pathSetupMode = resv.getPath().getPathSetupMode();
                if(pathSetupMode.equals("user-xml")){
                    String status = this.pathSetupManager.create(resv);
                    dao.update(resv);
                    this.log.info("pendingReservations: " + resv.toString());
                    String notification = this.pendingReservationMessage(resv);
                    String subject = "Circuit set up";
                    // this.notifier.sendMessage(subject, notification);
                 }
            }
        } catch (PSSException ex) {
            // log and rethrow
            this.log.error("pendingReservations.PSSException: " +
                    ex.getMessage());
            throw new PSSException(ex.getMessage());
        //} catch (javax.mail.MessagingException ex) {
            //throw new BSSException(ex.getMessage());
        }
        return reservations;
    }

    /**
     * Handles tearing down LSP's.
     *
     * @param timeInterval an integer with the time window to check 
     * @return response a list of reservations that have expired
     * @throws PSSException
     */
    public List<Reservation> expiredReservations(Integer timeInterval) 
            throws PSSException {

        List<Reservation> reservations = null;
        String prevStatus = null;
        String newStatus = null;

        try {
            ReservationDAO dao = new ReservationDAO(this.dbname);
            reservations = dao.expiredReservations(timeInterval);
            for (Reservation resv: reservations) {
                // call PSS to tear down LSP
                prevStatus = resv.getStatus();
                String status = this.pathSetupManager.teardown(resv);
                if (status.equals("CANCELLED")) {
                    // set end time to cancel time
                    // useful in case reservation was persistent
                   long millis = System.currentTimeMillis();
                   resv.setEndTime(millis);
                }
                dao.update(resv);
                this.log.info("expiredReservations: " + resv.toString());
                String notification = this.expiredReservationMessage(resv);
                String subject = "Circuit torn down";
                // this.notifier.sendMessage(subject, notification);
            }
        } catch (PSSException ex) {
            // log and rethrow
            this.log.error("expiredReservations.PSSException: " +
                    ex.getMessage());
            throw new PSSException(ex.getMessage());
        //} catch (javax.mail.MessagingException ex) {
            //throw new BSSException(ex.getMessage());
        }
        return reservations;
    }

    /*
     * Notification message methods.  For lack of a better pattern at the
     * moment.  Configuration with unordered properties was not a solution.
     */

    /**
     * Returns a description of the pending reservation suitable for email.
     * @param resv a reservation instance
     * @return a String describing the pending reservation
     */
    public String pendingReservationMessage(Reservation resv) {

        String msg = "Reservation: " + resv.toString() + "\n";
        return msg;
    }

    /**
     * Returns a description of the expired reservation suitable for email.
     * @param resv a reservation instance
     * @return a String describing the expired reservation
     */
    public String expiredReservationMessage(Reservation resv) {

        String msg = "Reservation: " + resv.toString() + "\n";
        return msg;
    }
}
