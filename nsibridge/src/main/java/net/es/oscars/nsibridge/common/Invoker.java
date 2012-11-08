package net.es.oscars.nsibridge.common;

import static java.util.Arrays.asList;

import joptsimple.OptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.es.oscars.nsibridge.beans.config.OscarsConfig;
import net.es.oscars.nsibridge.beans.config.JettyConfig;
import net.es.oscars.nsibridge.beans.config.StpConfig;
import net.es.oscars.nsibridge.prov.CoordHolder;
import net.es.oscars.nsibridge.prov.NSAConfigHolder;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;

import java.io.File;

public class Invoker {

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
        parseArgs( args );

        Invoker.getInstance().run(context);
    }

    public void run(String ctx) throws Exception {


        ContextConfig.getInstance().setContext(ctx);
        ContextConfig.getInstance().loadManifest(new File("./config/manifest.yaml"));

        NSAConfigHolder.getInstance().setNsaConfig(ConfigManager.getInstance().getNSAConfig("config/nsa.yaml"));
        NSAConfigHolder.getInstance().setStpConfigs(ConfigManager.getInstance().getStpConfig("config/stp.yaml"));
        for (StpConfig stp : NSAConfigHolder.getInstance().getStpConfigs()) {
            System.out.println("stp :"+stp.getStpId());
        }


        CoordHolder.getInstance().setOscarsConfig(ConfigManager.getInstance().getOscarsConfig("config/oscars.yaml"));
        CoordHolder.getInstance().initialize();

        JettyContainer jc = JettyContainer.getInstance();
        jc.setConfig(ConfigManager.getInstance().getJettyConfig("config/jetty.yaml"));

        jc.startServer();

        while (true) {
            /*
            for (NSAAPI nsa : NSAFactory.getInstance().getNSAs()) {
                nsa.tick();
            }
            Thread.sleep(500);
              */
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