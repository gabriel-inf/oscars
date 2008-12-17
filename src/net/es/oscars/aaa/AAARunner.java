package net.es.oscars.aaa;

import java.io.*;
import java.rmi.RemoteException;

import net.es.oscars.rmi.aaa.AaaRmiServer;

import org.apache.log4j.*;


public class AAARunner {
    private static Logger log = Logger.getLogger(AAARunner.class);

    private static AaaRmiServer aaaRmiServer = null;


    public static void main (String[] args) {

        log.info("*** OSCARS AAA STARTUP ***");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        String fname = "/tmp/oscarsAaaHeartbeat.txt";
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
        initRMIServer();


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


    /**
     * Initializes the RMIServer module
     */
    private static void initRMIServer() {
        log.info("initRMIServer.start");
        try {
            aaaRmiServer = new AaaRmiServer();
            aaaRmiServer.init();
        } catch (RemoteException ex) {
            log.error("Error initializing AAA RMI server", ex);
            aaaRmiServer.shutdown();
            aaaRmiServer = null;
        }
        log.info("initRMIServer.end");
    }


    private static void shutdownHook() {
        log.info("*** OSCARS AAA SHUTDOWN beginning ***");
        aaaRmiServer.shutdown();
        aaaRmiServer = null;
        log.info("*** OSCARS AAA SHUTDOWN ending ***");
    }

}
