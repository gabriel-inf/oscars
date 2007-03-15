import java.util.*;
import java.io.*;
import java.lang.Throwable;

import org.hibernate.*;

import net.es.oscars.database.HibernateUtil;
import net.es.oscars.database.Initializer;
import net.es.oscars.bss.*;

/**
 * @author Jason Lee (jrlee@lbl.gov), David Robertson (dwrobertson@lbl.gov)
 */

public class LSPScheduler {

    // time in seconds to look into the future for reservations
    private static final Integer reservationInterval = 300;

    // shutdown hook delay time in seconds
    private static final int shutdownTime = 2;     

    private static int debug = 0;

    public static void main (String[] args) {
       System.err.println ("Start of LSPScheduler");

       debug = 1;

       Initializer initializer = new Initializer();
       initializer.initDatabase();
       Thread runtimeHookThread = new Thread() {
            public void run() {
                shutdownHook(); 
            }
       };

       Scheduler sched = new Scheduler();

        //if (!debug)
        //    closeIO();
        Runtime.getRuntime().addShutdownHook (runtimeHookThread);

        try {
            while (true) {
                // make this 30 sec
                Thread.sleep (30000);
                //XXX: do something
                CheckReservations( sched );
            }
        } catch (Throwable t) {
            System.out.println("Error: " + t.toString());
            t.printStackTrace();
        }
    }

    private static void CheckReservations(Scheduler s) {
        List<Reservation> resList = null;

        Session session =
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        session.beginTransaction();
        try {
            // Check for expired reservations 1st, that way we don't
            // setup reservations and then tear them down again if they're
            // in the past.
            System.out.println("Checking for expired reserverations: ");
            resList = s.expiredReservations(0);
            for (Reservation r: resList ) {
                System.out.println ("Tore down: " + r.toString());
            }
            /* Look for stuff that *is* exprired as of now (0) */
            resList = s.pendingReservations(reservationInterval);

            System.out.println("Checking for new reserverations: ");
            for (Reservation r: resList ) {
                System.out.println ("Setting up: " + r.toString());
            }

        } catch (BSSException e) {
            session.getTransaction().rollback();
            System.out.println("BSSError: " + e.getMessage());
        } catch (Exception e ) {
            // catch everything elese and keep marching on
            System.out.println("Exception: " + e.getMessage());
        }

        session.getTransaction().commit();
    }

    private static void shutdownHook() {
        System.out.println("ShutdownHook started");
        long t0 = System.currentTimeMillis();
        while (true) 
        {
            try {
             Thread.sleep (500); 
            } catch (Exception e) {
                System.out.println("Exception: "+e.toString());
                break; 
            }

            if (System.currentTimeMillis() - t0 > shutdownTime*1000) 
                break;
            System.out.println("shutdown"); 
        }
        System.out.println("ShutdownHook completed"); 
    }

    /* close down the standard IO ports we are a daemon */
    private static void closeIO() throws IOException {
        System.out.close();
        System.err.close();
        System.in.close();
    }
}
