package net.es.oscars.nsibridge.common;

import static java.util.Arrays.asList;

import joptsimple.OptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import net.es.oscars.nsibridge.config.HttpConfig;
import net.es.oscars.nsibridge.config.OscarsStubConfig;
import net.es.oscars.nsibridge.config.OscarsStubSecConfig;
import net.es.oscars.nsibridge.config.nsa.NsaConfig;
import net.es.oscars.nsibridge.config.nsa.NsaConfigProvider;
import net.es.oscars.nsibridge.config.nsa.StpConfig;
import net.es.oscars.nsibridge.config.SpringContext;
import net.es.oscars.nsibridge.oscars.OscarsProxy;
import net.es.oscars.nsibridge.prov.ScheduleUtils;
import net.es.oscars.nsibridge.soap.impl.ProviderServer;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.task.TaskException;
import net.es.oscars.utils.task.sched.Schedule;
import net.es.oscars.utils.task.sched.Workflow;
import org.apache.log4j.Logger;
import org.quartz.SchedulerException;
import org.springframework.context.ApplicationContext;

import javax.persistence.EntityManager;
import java.io.File;

public class Invoker implements Runnable {
    private static final Logger log = Logger.getLogger(Invoker.class);
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
            String manifestFile = System.getProperty("nsibridge.manifest");
            if(manifestFile == null || "".equals(manifestFile)){
                manifestFile = "./config/manifest.yaml";
            }else{
                manifestFile = manifestFile.replaceFirst("file:", "");
            }
            ContextConfig.getInstance().loadManifest(new File(manifestFile));
        } catch (ConfigException e) {
            e.printStackTrace();
            Invoker.setKeepRunning(false);
        }



        System.out.println("Initializing Spring... ");
        SpringContext sc = SpringContext.getInstance();
        String beansFile = System.getProperty("nsibridge.beans");
        if(beansFile == null || "".equals(beansFile)){
            beansFile = "config/beans.xml";
        }
        ApplicationContext ax = sc.initContext(beansFile);


        NsaConfigProvider np = ax.getBean("nsaConfigProvider", NsaConfigProvider.class);

        NsaConfig nc = np.getConfig("local");

        for (StpConfig stp : nc.getStps()) {
            // System.out.println("stp :"+stp.getStpId());
        }

        OscarsStubConfig os = ax.getBean("oscarsStubConfig", OscarsStubConfig.class);
        HttpConfig hc = ax.getBean("httpConfig", HttpConfig.class);

        EntityManager em = PersistenceHolder.getEntityManager();


        try {
            ProviderServer ps = ProviderServer.makeServer(hc);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }


        try {
            System.out.print("Connecting to OSCARS...");
            OscarsProxy.getInstance().initialize();
            System.out.println(" connected.");
        } catch (OSCARSServiceException e) {
            System.out.println(" not connected!");
            e.printStackTrace();
            Invoker.setKeepRunning(false);
        }

        Schedule ts = Schedule.getInstance();

        try {
            ts.start();
            ScheduleUtils.scheduleProvMonitor();
            ScheduleUtils.scheduleResvTimeoutMonitor();
        } catch (TaskException e) {
            e.printStackTrace();
            Invoker.setKeepRunning(false);
        } catch (SchedulerException e) {
            e.printStackTrace();
            Invoker.setKeepRunning(false);
        }

        System.out.println("NSI Bridge ready to receive requests.");

        Runtime.getRuntime().addShutdownHook(
                new Thread() {
                    public void run() {
                        System.out.println("Shutting down..");
                        PersistenceHolder.getEntityManager().close();
                        ProviderServer.getInstance().stop();
                        try {
                            Schedule.getInstance().stop();
                        } catch (Exception ex) {
                            log.error(ex);
                        }
                        System.out.println("Shutdown complete.");
                        Invoker.setKeepRunning(false);
                    }
                }
        );

        Workflow wf = Workflow.getInstance();
        while (keepRunning) {
            try {
                Thread.sleep(10);
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