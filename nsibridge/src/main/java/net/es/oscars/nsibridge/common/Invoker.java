package net.es.oscars.nsibridge.common;

import static java.util.Arrays.asList;

import joptsimple.OptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import net.es.oscars.nsibridge.config.http.HttpConfig;
import net.es.oscars.nsibridge.config.http.HttpConfigProvider;
import net.es.oscars.nsibridge.config.nsa.NsaConfig;
import net.es.oscars.nsibridge.config.nsa.NsaConfigProvider;
import net.es.oscars.nsibridge.config.nsa.StpConfig;
import net.es.oscars.nsibridge.config.SpringContext;
import net.es.oscars.nsibridge.config.oscars.OscarsConfig;
import net.es.oscars.nsibridge.config.oscars.OscarsConfigProvider;
import net.es.oscars.nsibridge.prov.OscarsProxy;
import net.es.oscars.nsibridge.prov.NSAConfigHolder;
import net.es.oscars.nsibridge.soap.impl.ProviderServer;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.task.TaskException;
import net.es.oscars.utils.task.sched.Schedule;
import org.springframework.context.ApplicationContext;

import java.io.File;

public class Invoker implements Runnable {
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


    public static void main(String[] args) throws Exception {
        parseArgs(args);
        Thread thr = new Thread(Invoker.getInstance());
        thr.start();
    }

    public void setContext(String ctx) {
        context = ctx;
    }

    public void run() {

        try {
            ContextConfig.getInstance().setContext(context);
            ContextConfig.getInstance().loadManifest(new File("./config/manifest.yaml"));
        } catch (ConfigException e) {
            e.printStackTrace();
            System.exit(1);
        }



        System.out.print("Initializing Spring... ");
        SpringContext sc = SpringContext.getInstance();
        ApplicationContext ax = sc.initContext("config/beans.xml");

        OscarsConfigProvider op = ax.getBean("oscarsConfigProvider", OscarsConfigProvider.class);
        NsaConfigProvider np = ax.getBean("nsaConfigProvider", NsaConfigProvider.class);
        HttpConfigProvider hp = ax.getBean("httpConfigProvider", HttpConfigProvider.class);




        NsaConfig nc = np.getConfig("local");
        NSAConfigHolder.getInstance().setNsaConfig(nc);

        for (StpConfig stp : NSAConfigHolder.getInstance().getNsaConfig().getStps()) {
            System.out.println("stp :"+stp.getStpId());
        }


        try {
            OscarsConfig oc = op.getConfig("local");
            OscarsProxy.getInstance().setOscarsConfig(oc);
            // OscarsProxy.getInstance().initialize();
        } catch (OSCARSServiceException e) {
            e.printStackTrace();
            System.exit(1);
        }

        PersistenceHolder.getInstance().getEntityManager();

        HttpConfig hc = hp.getConfig("provider");

        try {
            ProviderServer ps = ProviderServer.makeServer(hc);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }


        //JettyContainer jc = JettyContainer.getInstance();
        //jc.setConfig(ConfigManager.getInstance().getJettyConfig("config/jetty.yaml"));

        //jc.startServer();


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
                        ProviderServer.getInstance().stop();
                        System.out.println("Shutdown complete.");
                        Invoker.setKeepRunning(false);
                    }
                }
        );

        while (keepRunning) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(1);

            }
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