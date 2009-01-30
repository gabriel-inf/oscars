package net.es.oscars.ws;

import java.io.*;

import org.apache.log4j.*;

/**
 * 
 * @author Evangelos Chaniotakis @ ESNet
 *
 *  Main program of the RMI core OSCARS server.
 *  
 *  Creates a heartbeat file in /tmp/oscarsCoreHeartbeat.txt that it updates every 30 secs.
 *  A nagios server can monitor the heartbeat file to check that the core is still running.
 *  
 *  Creates an instance of the oscarsCore and initializes it. Includes
 *  initializing a coreRMI repository and server, a scheduler and Hibernate.
 */

public class OSCARSRunner {
    private static Logger log = Logger.getLogger(OSCARSRunner.class);
    private static OSCARSCore core = null;
    
    public static void main (String[] args) {

        log.info("*** OSCARS CORE STARTUP ***");

        String fname = "/tmp/oscarsCoreHeartbeat.txt";
        File heartbeatFile = new File(fname);

        if (!heartbeatFile.exists()) {
            createHeartBeat(fname);
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
               "The scheduler resets the last modified time of this file on " +
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
        log.info("*** OSCARS CORE SHUTDOWN beginning ***");
        core.shutdown();
        log.info("*** OSCARS CORE SHUTDOWN ending ***");
    }

}
