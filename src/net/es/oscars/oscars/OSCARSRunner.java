package net.es.oscars.oscars;

import java.util.*;
import java.io.*;
import java.lang.Throwable;

import org.apache.log4j.*;


public class OSCARSRunner {
    private static Logger log = Logger.getLogger(OSCARSRunner.class);

    // time in seconds to look into the future for reservations
    private static final Integer reservationInterval = 0;


    // shutdown lock held while pending and expired reservations are handled
    private static Object shutdownLock;
    
    private static OSCARSCore core = null;

    public static void main (String[] args) {

        log.info("*** OSCARS STARTUP ***");
        
        shutdownLock = new Object();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        String fname = "/tmp/oscarsHeartbeat.txt";
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
        
        Thread runtimeHookThread = new Thread() {
            public void run() {
                shutdownHook();
            }
        };
        
        core = OSCARSCore.init();

        Runtime.getRuntime().addShutdownHook (runtimeHookThread);
        try {
            while (true) {
                long ms = System.currentTimeMillis();
                if (!heartbeatFile.setLastModified(ms)) {
                    log.fatal("*** UNABLE TO SET HEARTBEAT ***");
                    System.exit(0);
                }
                Thread.sleep (30000);
            }
        } catch (Exception e) {
            e.printStackTrace(pw);
            log.error(sw.toString());
        }
    }
    
    private static void shutdownHook() {
        log.info("*** OSCARS SHUTDOWN beginning ***");
        long t0 = System.currentTimeMillis();
        core.shutdown();
        log.info("*** SCHEDULER SHUTDOWN ending ***");
    }

    /* close down the standard IO ports we are a daemon */
    private static void closeIO() throws IOException {
        System.out.close();
        System.err.close();
        System.in.close();
    }
}
