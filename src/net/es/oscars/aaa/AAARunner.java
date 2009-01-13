package net.es.oscars.aaa;

import java.io.*;
//import java.rmi.RemoteException;

import net.es.oscars.rmi.aaa.AaaRmiServer;

import org.apache.log4j.*;

/**
 * 
 * @author Evangelos Chaniotakis, ESnet
 * 
 * Main program that starts the AAA RMI server.
 * 
 * It creates a heartbeat file in /tmp/oscarsAaaHeartbeat.txt that
 * it updates every 30 seconds. This file will be monitored by 
 * nagios to be sure the aaaRmi server is still running.
 * 
 *
 */
public class AAARunner {
    private static Logger log = Logger.getLogger(AAARunner.class);

//    private static AaaRmiServer aaaRmiServer = null;


    public static void main (String[] args) {

 
        log.info("*** OSCARS AAA STARTUP ***");

        String fname = "/tmp/oscarsAaaHeartbeat.txt";
        File heartbeatFile = new File(fname);

        if (!heartbeatFile.exists()) {
            createHeartBeat(fname);
        }

        Thread runtimeHookThread = new Thread() {
            public void run() {
                shutdownHook();
            }
        };

        AAACore core = AAACore.getInstance();
        core.init();


        Runtime.getRuntime().addShutdownHook (runtimeHookThread);
        try {
            while (true) {
                long ms = System.currentTimeMillis();
                if (!heartbeatFile.setLastModified(ms)) {
                    log.warn("*** UNABLE TO SET HEARTBEAT ***");
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
              "The aaaRmiServer resets the last modified time of this file on " +
              " each cycle. The last modified time of this file will be " +
              " monitored by nagios to check that the server is " +
              " running normally.");
            out.close();
          } catch (IOException e) {;
              log.error(e);
              System.exit(0);
          } 
    }


    private static void shutdownHook() {
        log.info("*** OSCARS AAA SHUTDOWN beginning ***");
        AAACore core = AAACore.getInstance();
        core.shutdown();
        log.info("*** OSCARS AAA SHUTDOWN ending ***");
    }

}
