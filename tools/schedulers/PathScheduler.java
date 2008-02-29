import java.util.*;
import java.io.*;
import java.lang.Throwable;

import org.apache.log4j.*;
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
    private static final Integer reservationInterval = 0;

    // shutdown hook delay time in seconds
    private static final int shutdownTime = 2;

    // shutdown lock held while pending and expired reservations are handled
    private static Object shutdownLock;

    public static void main (String[] args) {

        Logger log = Logger.getLogger("PathScheduler");
        log.info("*** SCHEDULER STARTUP ***");
        shutdownLock = new Object();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        String fname = "/tmp/schedulerHeartbeat.txt";
        File heartbeatFile = new File(fname);
        if (!heartbeatFile.exists()) {
            try {
              FileWriter outFile = new FileWriter(fname);
              PrintWriter out = new PrintWriter(outFile);
              out.println(
                "The scheduler resets the last modified time of this file on " +
                " each cycle. The last modified time of this file will be " +
                " monitored by nagios to check that the scheduler is " +
                "running normally.");
              out.close();
            } catch (IOException e) {
                e.printStackTrace(pw);
                log.error(sw.toString());
                System.exit(0);
            }
        }
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
                long ms = System.currentTimeMillis();
                if (!heartbeatFile.setLastModified(ms)) {
                    log.error("*** UNABLE TO SET HEARTBEAT ***");
                    System.exit(0);
                }
                // attempt to avoid shutdown until all pending and expired
                // reservations in current cycle are handled
                synchronized(shutdownLock) {
                    checkReservations(sched);
                }
                // sleep for 30 seconds
                Thread.sleep (30000);
            }
        } catch (Exception e) {
            e.printStackTrace(pw);
            log.error(sw.toString());
        }
    }

    private static void checkReservations(Scheduler scheduler) {
        List<Reservation> resList = null;

        Session session =
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        session.beginTransaction();
        // Check for expired reservations first, so reservations aren't
        // set up, and then torn down again if they're already in the past.
        // Look for stuff that *is* expired as of now (0)
        resList = scheduler.expiredReservations(0);
        resList = scheduler.pendingReservations(reservationInterval);

        // look at expiring reservations in the future
        scheduler.expiringReservations(reservationInterval);
        // Any exception is caught and logged in the Scheduler class.
        // It is not rethrown, but the reservation is marked as FAILED
        session.getTransaction().commit();
    }

    private static void shutdownHook() {
        Logger log = Logger.getLogger("PathScheduler");
        log.info("*** SCHEDULER SHUTDOWN beginning ***");
        long t0 = System.currentTimeMillis();
        while (true)
        {
            try {
                Thread.sleep (500);
            } catch (Exception e) {
                break;
            }

            // NOTE that the scheduler may not shut down cleanly if the
            // system goes down, since checkReservations may take a number
            // of seconds.  Use kill -2
            if (System.currentTimeMillis() - t0 > shutdownTime*1000)
                synchronized(shutdownLock) {
                    break;
                }
        }
        log.info("*** SCHEDULER SHUTDOWN ending ***");
    }

    /* close down the standard IO ports we are a daemon */
    private static void closeIO() throws IOException {
        System.out.close();
        System.err.close();
        System.in.close();
    }
}
