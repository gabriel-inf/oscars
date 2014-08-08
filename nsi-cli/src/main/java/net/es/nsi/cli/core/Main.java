package net.es.nsi.cli.core;


import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.es.nsi.cli.cmd.NsiBootstrap;
import net.es.nsi.cli.cmd.NsiCliState;
import net.es.nsi.cli.config.*;
import net.es.nsi.cli.db.DB_Util;
import net.es.nsi.client.types.NsiCallbackHandler;
import net.es.nsi.client.util.ListenerHolder;
import net.es.nsi.client.util.NsiRequesterPort;
import net.es.nsi.client.util.NsiRequesterPortListener;
import net.es.nsi.client.util.RequesterPortHolder;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.net.URL;

import static java.util.Arrays.asList;

public class Main {
    private static final Logger log = Logger.getLogger(Main.class);

    public static void main(String[] args) throws IOException {

        CliSpringContext sc = CliSpringContext.getInstance();
        String beansFile = System.getProperty("nsi-cli.beans");
        if (beansFile == null || "".equals(beansFile)){
            beansFile = "config/beans.xml";
        }
        log.info("Initializing Spring from "+beansFile);
        ApplicationContext ax = sc.initContext(beansFile);
        parseArgs(args);
        prepare(true);

        NsiBootstrap.main(args);
    }

    private static void prepare(boolean receivecallbacks) {


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

            CliRequesterProfile rqp = DB_Util.getRequesterProfile(name);
            if (rqp == null) {
                System.err.println("could not load requester profile from DB for name:" +name);
                System.exit(1);
            }

            NsiCliState.getInstance().setRequesterProfile(rqp);

            if (receivecallbacks) {

                log.info("Starting listener... ");
                NsiRequesterPortListener listener = ListenerHolder.getInstance().getListeners().get("default");
                boolean listenerError = false;
                if (listener == null) {
                    NsiRequesterPort port = new NsiRequesterPort();
                    NsiCallbackHandler handler = NsiCliState.getInstance().getNewState();
                    port.setCallbackHandler(handler);
                    try {
                        listener = new NsiRequesterPortListener(rqp.getUrl(), rqp.getBusConfig(), port);
                        ListenerHolder.getInstance().getListeners().put(defs.getRequesterProfileName(), listener);
                    } catch (Exception ex) {
                        log.error("could not start listener!");
                        listenerError = true;
                    }
                }
                if (!listenerError) {
                    listener.start();
                    NsiCliState.getInstance().setListenerStarted(true);
                    log.info("listener started.");
                }
            }


            name = defs.getProvProfileName();
            if (name == null || name.isEmpty()) {
                System.err.println("null / empty default provider profile name!");
                System.exit(1);
            }
            log.debug("got provider profile name "+name);

            CliProviderProfile pp = DB_Util.getProviderProfile(name);
            if (pp == null) {
                System.err.println("could not load provider profile from DB for name:" +name);
                System.exit(1);
            }
            log.debug("loaded provider profile for provider profile "+name);

            CliProviderServer ps = pp.getProviderServer();
            if (ps == null) {
                System.err.println("could not load provider server from DB for name:" +name);
                System.exit(1);
            }
            log.debug("loaded provider server for provider profile "+name);


            if (ps.getUrl() == null) {
                System.err.println("null URL for provider server:" +name);
                System.exit(1);
            }
            log.debug("got provider server URL: "+ps.getUrl());

            NsiCliState.getInstance().setProvProfile(pp);

            if (receivecallbacks) {
                log.info("Creating the default client port... ");
                RequesterPortHolder rph = RequesterPortHolder.getInstance();
                URL url = new URL(ps.getUrl());

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
        CliProviderProfile pp = DB_Util.getSpringProvProfile("default");
        if (pp == null) {
            System.err.println("no ProviderProfile 'default' bean");
            System.exit(1);
        }
        NsiCliState.getInstance().setProvProfile(pp);
        DB_Util.save(pp);


        CliRequesterProfile rqp = DB_Util.getSpringRequesterProfile("default");
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



    public static String[] parseArgs(String args[])  throws java.io.IOException {

        OptionParser parser = new OptionParser();
        parser.acceptsAll( asList("h", "?"), "show help then exit" );
        parser.accepts("d", "turn on debugging");
        parser.accepts("cmdfile" , "/path/to/script" ).withRequiredArg().describedAs("path to script file to non-interactively execute then exit").ofType(String.class);
        try {
            OptionSet options = parser.parse( args );
            // check for help
            if ( options.has( "?" ) || options.has("h")) {
                parser.printHelpOn( System.out );
                System.exit(0);
            }

        } catch (OptionException exception) {
            System.err.println("Error parsing command-line parameters: "+exception.getMessage());
            parser.printHelpOn(System.err);
            System.exit(1);
        }

        return args;

    }


}