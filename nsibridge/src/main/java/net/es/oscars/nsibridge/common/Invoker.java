package net.es.oscars.nsibridge.common;

import static java.util.Arrays.asList;

import joptsimple.OptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import net.es.oscars.nsibridge.beans.config.StpConfig;
import net.es.oscars.nsibridge.prov.OscarsProxy;
import net.es.oscars.nsibridge.prov.NSAConfigHolder;
import net.es.oscars.nsibridge.svc.PersistenceHolder;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.task.TaskException;
import net.es.oscars.utils.task.sched.Schedule;

import java.io.File;

public class Invoker {
    private static boolean keepRunning = true;

    public static boolean isKeepRunning() {
        return keepRunning;
    }

    public static void setKeepRunning(boolean keepRunning) {
        Invoker.keepRunning = keepRunning;
    }


    private static String context = ConfigDefaults.CTX_PRODUCTION;

    private Invoker() {
    }

    private static Invoker instance;
    public static Invoker getInstance() {
        if (instance == null) {
            instance = new Invoker();
        }
        return instance;
    }



    public void setContext(String ctx) {
        context = ctx;
    }

    public static void main(String[] args) throws Exception {
        parseArgs(args);

        try {
            ContextConfig.getInstance().setContext(context);
            ContextConfig.getInstance().loadManifest(new File("./config/manifest.yaml"));
        } catch (ConfigException e) {
            e.printStackTrace();
            System.exit(1);
        }

        NSAConfigHolder.getInstance().setNsaConfig(ConfigManager.getInstance().getNSAConfig("config/nsa.yaml"));
        NSAConfigHolder.getInstance().setStpConfigs(ConfigManager.getInstance().getStpConfig("config/stp.yaml"));
        for (StpConfig stp : NSAConfigHolder.getInstance().getStpConfigs()) {
            System.out.println("stp :"+stp.getStpId());
        }


        try {
            OscarsProxy.getInstance().setOscarsConfig(ConfigManager.getInstance().getOscarsConfig("config/oscars.yaml"));
            OscarsProxy.getInstance().initialize();
        } catch (OSCARSServiceException e) {
            e.printStackTrace();
            System.exit(1);
        }

        JettyContainer jc = JettyContainer.getInstance();
        jc.setConfig(ConfigManager.getInstance().getJettyConfig("config/jetty.yaml"));

        jc.startServer();


        Schedule ts = Schedule.getInstance();

        try {
            ts.start();
        } catch (TaskException e) {
            e.printStackTrace();
            System.exit(1);
        }

        Runtime.getRuntime().addShutdownHook(
                new Thread() {
                    public void run() {
                        System.out.println("Shutting down..");
                        PersistenceHolder.getInstance().getEntityManager().close();
                        System.out.println("Shutdown complete.");
                        Invoker.setKeepRunning(false);
                    }
                }
        );

        while (keepRunning) {
            Thread.sleep(1);
        }


    }

    public static void parseArgs(String args[])  throws java.io.IOException {

        OptionParser parser = new OptionParser();
        parser.acceptsAll( asList( "h", "?" ), "show help then exit" );
        OptionSpec<String> CONTEXT = parser.accepts("c", "context:INITTEST,DEVELOPMENT,SDK,PRODUCTION").withRequiredArg().ofType(String.class);
        OptionSet options = parser.parse( args );

        // check for help
        if ( options.has( "?" ) ) {
            parser.printHelpOn( System.out );
            System.exit(0);
        }
        if (options.has(CONTEXT) ){
            context = options.valueOf(CONTEXT);
            if (!context.equals("UNITTEST") &&
                    !context.equals("SDK") &&
                    !context.equals("DEVELOPMENT") &&
                    !context.equals("PRODUCTION") )
            {
                System.out.println("unrecognized CONTEXT value: " + context);
                System.exit(-1);
            }
        }
    }





}