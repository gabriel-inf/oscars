package net.es.oscars.nsibridge.common;

import static java.util.Arrays.asList;

import joptsimple.OptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.es.oscars.nsibridge.beans.config.OscarsConfig;
import net.es.oscars.nsibridge.beans.config.JettyConfig;
import net.es.oscars.nsibridge.prov.CoordHolder;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;

import java.io.File;

public class Invoker {

    private static String context = ConfigDefaults.CTX_PRODUCTION;

    private Invoker() {
    }

    private static Invoker instance;
    private static Invoker getInstance() {
        if (instance == null) {
            instance = new Invoker();
        }
        return instance;
    }


    public static void main(String[] args) throws Exception {
        Invoker.getInstance().run(args);
    }

    private void run(String[] args) throws Exception {

        parseArgs( args );

        ContextConfig.getInstance().setContext(context);
        ContextConfig.getInstance().loadManifest(new File("./config/manifest.yaml"));


        CoordHolder.getInstance().setOscarsConfig(this.configureCoord());
        CoordHolder.getInstance().initialize();

        JettyContainer jc = JettyContainer.getInstance();
        jc.setConfig(this.configureJetty());

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



    private OscarsConfig configureCoord() throws ConfigException {

        ConfigManager cm = ConfigManager.getInstance();
        OscarsConfig jc = cm.getCoordConfig("config/oscars.yaml");
        return jc;
    }

    private JettyConfig configureJetty() throws ConfigException {
        ConfigManager cm = ConfigManager.getInstance();
        JettyConfig jc = cm.getJettyConfig("config/jetty.yaml");
        return jc;

    }
}