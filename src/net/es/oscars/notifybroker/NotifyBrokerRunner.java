package net.es.oscars.notifybroker;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.log4j.Logger;

public class NotifyBrokerRunner {
    private static Logger log = Logger.getLogger(NotifyBrokerRunner.class);
    private static NotifyBrokerCore core = null;
    
    public static void main (String[] args) {

        log.info("*** NOTIFYBROKER CORE STARTUP ***");

        String fname = "/tmp/notifyBrokerCoreHeartbeat.txt";
        File heartbeatFile = new File(fname);

        if (!heartbeatFile.exists()) {
            createHeartBeat(fname);
        }

        Thread runtimeHookThread = new Thread() {
            public void run() {
                shutdownHook();
            }
        };
        
        core = NotifyBrokerCore.init();

        Runtime.getRuntime().addShutdownHook (runtimeHookThread);
        try {
            while (true) {
                long ms = System.currentTimeMillis();
                if (!heartbeatFile.setLastModified(ms)) {
                    log.warn("*** ATTEMPT TO CREATE NEW HEARTBEAT ***");
                    createHeartBeat(fname);
                }
                Thread.sleep (30000);
            }
        } catch (Exception e) {
            log.error(e);
        }
    }

    public static void createHeartBeat(String fname) {
        try {
            FileWriter outFile = new FileWriter(fname);
            PrintWriter out = new PrintWriter(outFile);
            out.println(
               "The NotifyBroker resets the last modified time of this file on " +
               " each cycle. The last modified time of this file will be " +
               " monitored by nagios to check that the scheduler is " +
               " running normally.");
            out.close();
          } catch (IOException e) {;
              log.error(e);
              System.exit(0);
          } 
    }
    private static void shutdownHook() {
        log.info("*** NOTIFYBROKER CORE SHUTDOWN beginning ***");
        core.shutdown();
        log.info("*** NOTIFYBROKER CORE SHUTDOWN ending ***");
    }
}
