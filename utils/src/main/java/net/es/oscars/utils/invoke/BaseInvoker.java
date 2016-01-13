package net.es.oscars.utils.invoke;

import static java.util.Arrays.asList;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.es.oscars.utils.config.ConfigDefaults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BaseInvoker {
    public static final String MODE_SERVER = "server";
    public static final String MODE_CLIENT = "client";
    public static final String INV_CONTEXT = "context";
    public static final String INV_MODE    = "mode";

    public static Map<String, String> parseArgs(String args[]) throws java.io.IOException {
        String context = ConfigDefaults.CTX_PRODUCTION;
        String mode = MODE_SERVER;

        OptionParser parser = new OptionParser();
        parser.acceptsAll(asList("h", "?"), "show help then exit");

        OptionSpec<String> modeSpec = parser.accepts("mode", "server / client mode").
                withRequiredArg().describedAs("client / server (default)").ofType(String.class);

        OptionSpec<String> contextSpec = parser.accepts("c", "context:UNITTEST,DEVELOPMENT,SDK,PRODUCTION").
                withRequiredArg().ofType(String.class);

        OptionSet options = parser.parse(args);

        // check for help
        if (options.has("?")) {
            parser.printHelpOn(System.out);
            System.exit(0);
        }

        List<String> allowedContexts = new ArrayList<String>();
        allowedContexts.add(ConfigDefaults.CTX_DEVELOPMENT);
        allowedContexts.add(ConfigDefaults.CTX_SDK);
        allowedContexts.add(ConfigDefaults.CTX_TESTING);
        allowedContexts.add(ConfigDefaults.CTX_PRODUCTION);

        if (options.has(contextSpec)) {
            context = options.valueOf(contextSpec);
            if (!allowedContexts.contains(context)) {
                System.out.println("unrecognized CONTEXT value: " + context);
                System.exit(-1);
            }
        }

        if (options.has(modeSpec)) {
            String optVal = options.valueOf(modeSpec);
            if (optVal.equals(MODE_CLIENT) || optVal.equals(MODE_SERVER)) {
                mode = optVal;
            } else {
                parser.printHelpOn(System.out);
                System.exit(1);
            }
        }
        HashMap<String, String> result = new HashMap<String, String>();
        result.put(INV_MODE, mode);
        result.put(INV_CONTEXT, context);
        return result;
    }
}
