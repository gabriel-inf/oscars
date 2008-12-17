package net.es.oscars.oscars;

import java.io.*;

import org.apache.log4j.*;


public class OSCARSRunner {
    private static Logger log = Logger.getLogger(OSCARSRunner.class);


    private static OSCARSCore core = null;

    public static void main (String[] args) {

        log.info("*** OSCARS CORE STARTUP ***");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        String fname = "/tmp/oscarsCoreHeartbeat.txt";
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
                    log.fatal("*** OSCARS CORE: UNABLE TO SET HEARTBEAT ***");
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
        log.info("*** OSCARS CORE SHUTDOWN beginning ***");
        core.shutdown();
        log.info("*** OSCARS CORE SHUTDOWN ending ***");
    }

}
