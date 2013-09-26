package net.es.nsi.cli.core;


import net.es.nsi.cli.client.CLI_ListenerHolder;
import net.es.nsi.cli.client.CliNsiHandler;
import net.es.nsi.cli.cmd.NsiBootstrap;
import net.es.nsi.cli.cmd.NsiCliState;
import net.es.nsi.cli.config.DefaultProfiles;
import net.es.nsi.cli.config.ProviderProfile;
import net.es.nsi.cli.config.RequesterProfile;
import net.es.nsi.cli.config.ResvProfile;
import net.es.nsi.cli.db.DB_Util;
import net.es.oscars.nsibridge.client.cli.CLIListener;
import net.es.oscars.nsibridge.config.SpringContext;
import net.es.oscars.nsibridge.prov.RequesterPortHolder;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.net.URL;

public class Main {
    private static final Logger log = Logger.getLogger(Main.class);
    public static void main(String[] args) throws IOException {

        SpringContext sc = SpringContext.getInstance();
        String beansFile = System.getProperty("nsibridge-cli.beans");
        if (beansFile == null || "".equals(beansFile)){
            beansFile = "config/beans.xml";
        }
        log.info("Initializing Spring from "+beansFile);
        ApplicationContext ax = sc.initContext(beansFile);
        runBackgroundTasks();

        NsiBootstrap.main(args);
    }

    private static void runBackgroundTasks() {
          try {
              System.out.println("\nPreparing CLI...\n");
            // System.out.println("\nPerforming background tasks.\n   Cannot start listener or submit nsi operations yet!\n");
            log.info("Loading data... ");
            DB_Util.getProviderProfiles();
            DB_Util.getResvProfiles();
            DB_Util.getRequesterProfiles();
            DefaultProfiles defs = DB_Util.getDefaults();
            if (defs != null) {
                NsiCliState.getInstance().setDefs(defs);
            }

            SpringContext sc = SpringContext.getInstance();
            ApplicationContext ax = sc.getContext();
            if (defs != null) {

                if (defs.getRequesterProfileName() != null) {
                    RequesterProfile rqp = DB_Util.getRequesterProfile(defs.getRequesterProfileName());
                    if (rqp == null) {
                        rqp = DB_Util.getSpringRequesterProfile(defs.getRequesterProfileName());
                    }
                    NsiCliState.getInstance().setRequesterProfile(rqp);

                    if (rqp != null) {

                        log.info("Starting the default listener... ");
                        CLIListener listener = CLI_ListenerHolder.getInstance().getListeners().get("default");
                        if (listener == null) {
                            listener = new CLIListener(rqp.getUrl(), rqp.getBusConfig(), new CliNsiHandler());
                            CLI_ListenerHolder.getInstance().getListeners().put(defs.getRequesterProfileName(), listener);

                        }
                        listener.start();
                        NsiCliState.getInstance().setListenerStarted(true);
                    }
                }


                log.info("Creating the default client port... ");
                if (defs.getProvProfileName() != null) {
                    ProviderProfile pp = DB_Util.getProviderProfile(defs.getProvProfileName());
                    if (pp == null) {
                        pp = DB_Util.getSpringProvProfile(defs.getProvProfileName());
                    }
                    NsiCliState.getInstance().setProvProfile(pp);
                    if (pp != null) {

                        RequesterPortHolder rph = RequesterPortHolder.getInstance();
                        URL url = new URL(pp.getProviderServer().getUrl());

                        rph.getPort(url);
                        log.info("Default port created.");
                    }

                }

                log.info("Loading default reservation... ");
                if (defs.getResvProfileName() != null) {
                    ResvProfile rp = DB_Util.getResvProfile(defs.getResvProfileName());
                    if (rp == null) {
                        rp = DB_Util.getSpringResvProfile(defs.getResvProfileName());
                    }
                    NsiCliState.getInstance().setResvProfile(rp);
                }

                NsiCliState.getInstance().setListenerStartable(true);
                System.out.println("\nDefaults loaded, CLI ready.\n");
            } else {
                System.out.println("\nNo defaults loaded. See 'defaults help'.\n");

            }
            NsiCliState.getInstance().setNsiAvailable(true);


        } catch (CliInternalException ex) {
            ex.printStackTrace();
            System.exit(1);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

    }
}