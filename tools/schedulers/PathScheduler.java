import java.util.*;
import java.io.*;
import java.lang.Throwable;

import org.hibernate.*;

import net.es.oscars.database.HibernateUtil;
import net.es.oscars.database.Initializer;
import net.es.oscars.bss.Reservation;
import net.es.oscars.pss.Scheduler;
import net.es.oscars.pss.PSSException;
import net.es.oscars.interdomain.InterdomainException;

/**
 * @author Jason Lee (jrlee@lbl.gov), David Robertson (dwrobertson@lbl.gov)
 */

public class PathScheduler {

    // time in seconds to look into the future for reservations
    private static final Integer reservationInterval = 300;

    // shutdown hook delay time in seconds
    private static final int shutdownTime = 2;     

    public static void main (String[] args) {

        Initializer initializer = new Initializer();
        List<String> dbnames = new ArrayList<String>();
        dbnames.add("bss");
        initializer.initDatabase(dbnames);
        Thread runtimeHookThread = new Thread() {
            public void run() {
                shutdownHook(); 
            }
        };
        Scheduler sched = new Scheduler("bss");
        Runtime.getRuntime().addShutdownHook (runtimeHookThread);
        try {
            while (true) {
                checkReservations(sched);
                // sleep for 30 seconds
                Thread.sleep (30000);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static void checkReservations(Scheduler scheduler) {
        List<Reservation> resList = null;

        Session session =
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        session.beginTransaction();
        try {
            // Check for expired reservations first, so reservations aren't
            // set up, and then torn down again if they're already in the past.
            // Look for stuff that *is* expired as of now (0)
            resList = scheduler.expiredReservations(0);
            resList = scheduler.pendingReservations(reservationInterval);
        } catch (PSSException e) {
            // Exception logged in Scheduler class.
            // Don't do a rollback because want failed status of
            // reservation to be committed.
        } catch (InterdomainException ex ) {
            // ditto
        } catch (Exception ex ) {
            // ditto
        }
        session.getTransaction().commit();
    }

    private static void shutdownHook() {
        long t0 = System.currentTimeMillis();
        while (true) 
        {
            try {
                Thread.sleep (500); 
            } catch (Exception e) {
                break; 
            }

            if (System.currentTimeMillis() - t0 > shutdownTime*1000) 
                break;
        }
    }

    /* close down the standard IO ports we are a daemon */
    private static void closeIO() throws IOException {
        System.out.close();
        System.err.close();
        System.in.close();
    }
}
