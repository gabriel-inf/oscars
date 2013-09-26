package net.es.nsi.cli.core;


import net.es.nsi.cli.client.CLI_ListenerHolder;
import net.es.nsi.cli.client.CliNsiHandler;
import net.es.nsi.cli.cmd.NsiBootstrap;
import net.es.nsi.cli.cmd.NsiCliState;
import net.es.nsi.cli.config.DefaultProfiles;
import net.es.nsi.cli.config.ProviderProfile;
import net.es.nsi.cli.config.RequesterProfile;
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
        Thread th = new Thread() {
            public void run() {
                try {
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

                    if (defs.getRequesterProfileName() != null) {
                        RequesterProfile rp = DB_Util.getRequesterProfile(defs.getRequesterProfileName());
                        if (rp == null) {
                            rp = DB_Util.getSpringRequesterProfile(defs.getRequesterProfileName());
                        }
                        if (rp != null) {
                            log.info("Starting the default listener... ");
                            CLIListener listener = CLI_ListenerHolder.getInstance().getListeners().get("default");
                            if (listener == null) {
                                listener = new CLIListener(rp.getUrl(), rp.getBusConfig(), new CliNsiHandler());
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

                        RequesterPortHolder rph = RequesterPortHolder.getInstance();
                        URL url = new URL(pp.getProviderServer().getUrl());

                        rph.getPort(url);
                        log.info("Default port created.");
                    }


                    NsiCliState.getInstance().setListenerStartable(true);

                    NsiCliState.getInstance().setNsiAvailable(true);
                    System.out.println("\nBackground tasks complete, all operations available.\n");
                } catch (CliInternalException ex) {
                    ex.printStackTrace();
                    System.exit(1);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.exit(1);
                }

            }
        };
        th.start();

    }
}