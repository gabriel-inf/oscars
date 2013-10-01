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
        String beansFile = System.getProperty("nsi-cli.beans");
        if (beansFile == null || "".equals(beansFile)){
            beansFile = "config/beans.xml";
        }
        log.info("Initializing Spring from "+beansFile);
        ApplicationContext ax = sc.initContext(beansFile);
        prepare(true);

        NsiBootstrap.main(args);
    }

    private static void prepare(boolean full) {


        try {
              // System.out.println("\nPerforming background tasks.\n   Cannot start listener or submit nsi operations yet!\n");
            log.info("Loading data... ");
            DB_Util.getProviderProfiles();
            DB_Util.getResvProfiles();
            DB_Util.getRequesterProfiles();

            DefaultProfiles defs = DB_Util.getDefaults();

            if (defs != null) {
                NsiCliState.getInstance().setDefs(defs);
            } else {
                log.info("No defaults, initializing with 'default' beans from beans.xml");
                defs = new DefaultProfiles();
                defs.setProvProfileName("default");
                defs.setRequesterProfileName("default");
                defs.setResvProfileName("default");
                defs.setId(null);
                DB_Util.save(defs);
                defs = DB_Util.getDefaults();

                injectDefaultProfiles();
                NsiCliState.getInstance().setDefs(defs);
            }
            if (defs == null) {
                System.err.println("could not load or initialize defaults");
                System.exit(1);
            }

            String name;
            name = defs.getRequesterProfileName();
            if (name == null || name.isEmpty()) {
                System.err.println("null / empty default requester profile name!");
                System.exit(1);
            }

            RequesterProfile rqp = DB_Util.getRequesterProfile(name);
            if (rqp == null) {
                System.err.println("could not load requester profile from DB for name:" +name);
                System.exit(1);
            }

            NsiCliState.getInstance().setRequesterProfile(rqp);

            if (full) {

                log.info("Starting listener... ");
                CLIListener listener = CLI_ListenerHolder.getInstance().getListeners().get("default");
                if (listener == null) {
                    listener = new CLIListener(rqp.getUrl(), rqp.getBusConfig(), new CliNsiHandler());
                    CLI_ListenerHolder.getInstance().getListeners().put(defs.getRequesterProfileName(), listener);

                }
                listener.start();
                NsiCliState.getInstance().setListenerStarted(true);
                log.info("listener started.");
            }


            name = defs.getProvProfileName();
            if (name == null || name.isEmpty()) {
                System.err.println("null / empty default requester profile name!");
                System.exit(1);
            }

            ProviderProfile pp = DB_Util.getProviderProfile(name);
            if (pp == null) {
                System.err.println("could not load provider profile from DB for name:" +name);
                System.exit(1);
            }

            NsiCliState.getInstance().setProvProfile(pp);
            if (full) {
                log.info("Creating the default client port... ");
                RequesterPortHolder rph = RequesterPortHolder.getInstance();
                URL url = new URL(pp.getProviderServer().getUrl());

                rph.getPort(url);
                log.info("Default port created.");
            }

            log.info("Loading default reservation... ");
            name = defs.getResvProfileName();
            if (name == null || name.isEmpty()) {
                System.err.println("null / empty default reservation profile name!");
                System.exit(1);
            }

            ResvProfile rp = DB_Util.getResvProfile(defs.getResvProfileName());
            if (rp == null) {
                System.err.println("could not load reservation profile from DB for name:" +name);
                System.exit(1);
            }
            NsiCliState.getInstance().setResvProfile(rp);

            NsiCliState.getInstance().setListenerStartable(true);

            NsiCliState.getInstance().setNsiAvailable(true);


        } catch (CliInternalException ex) {
            ex.printStackTrace();
            System.exit(1);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

    }

    private static void injectDefaultProfiles() {
        ProviderProfile pp = DB_Util.getSpringProvProfile("default");
        if (pp == null) {
            System.err.println("no ProviderProfile 'default' bean");
            System.exit(1);
        }
        NsiCliState.getInstance().setProvProfile(pp);
        DB_Util.save(pp);


        RequesterProfile rqp = DB_Util.getSpringRequesterProfile("default");
        if (rqp == null) {
            System.err.println("no RequesterProfile 'default' bean");
            System.exit(1);
        }
        NsiCliState.getInstance().setRequesterProfile(rqp);
        DB_Util.save(rqp);


        ResvProfile rp = DB_Util.getSpringResvProfile("default");
        if (rp == null) {
            System.err.println("no ResvProfile 'default' bean");
            System.exit(1);
        }
        NsiCliState.getInstance().setResvProfile(rp);
        DB_Util.save(rp);



    }

}