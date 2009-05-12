package net.es.oscars.client.improved.list;

import static java.util.Arrays.asList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import net.es.oscars.client.improved.ConsoleArgs;
import net.es.oscars.wsdlTypes.ListReply;
import net.es.oscars.wsdlTypes.ResDetails;


public class ListInvoker {

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        String configFile = ListClient.DEFAULT_CONFIG_FILE;
        String soapConfigFile = ListClient.DEFAULT_SOAP_CONFIG_FILE;
        String soapConfigId = ListClient.DEFAULT_SOAP_CONFIG_ID;

        // create a parser
        OptionParser parser = new OptionParser() {
            {
                acceptsAll( asList( "h", "?" ), "show help then exit" );
                accepts( "help", "show extended help then exit" );
                accepts( "i", "interactive mode" );
                accepts( "scfg", "soap config filename (config/soap.yaml)" ).withRequiredArg().ofType(String.class);
                accepts( "scgfid", "soap config identifier (\"default\")" ).withRequiredArg().ofType(String.class);
                accepts( "lcfg", "list config filename (config/list.yaml)" ).withRequiredArg().ofType(String.class);
                accepts( "num", "how many to return" ).withRequiredArg().ofType(Integer.class);
                accepts( "st", "comma-separated statuses to match" ).withRequiredArg().ofType(String.class);
                accepts( "vl", "comma-separated VLANs to match" ).withRequiredArg().ofType(String.class);
            }
        };

        OptionSet options = parser.parse( args );

        // check for help
        if ( options.has( "?" ) || options.has("h")) {
            parser.printHelpOn( System.out );
            System.exit(0);
        }
        if (options.has("help")) {
            System.out.println("More help");
            System.exit(0);
        }

        if (options.has("scfg")) {
            soapConfigFile = (String) options.valueOf("scfg");
        }
        if (options.has("scfgid")) {
            soapConfigId = (String) options.valueOf("scfgid");
        }
        if (options.has("lcfg")) {
            configFile = (String) options.valueOf("lcfg");
        }

        if (options.has("i")) {
            HashMap<String, String> interactiveOpts;
            interactiveOpts = getUserInput();
        }


        List<ListOutputterInterface> outputters =
                ListOutputterFactory.getConfiguredOutputters(configFile);

        ListClient cl = new ListClient();
        cl.setSoapConfigFile(soapConfigFile);
        cl.configureSoap(soapConfigId);
        cl.setConfigFile(configFile);
        cl.configure();

        ListReply listResp = cl.performRequest(cl.formRequest());
        ResDetails[] resvs = cl.filterResvs(listResp.getResDetails());
        for (ListOutputterInterface outputter : outputters) {
            outputter.output(resvs);
        }
    }

    private static HashMap<String, String> getUserInput() {
        HashMap<String, String> userChoices = new HashMap<String, String>();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            String numStr = ConsoleArgs.getArg(br, "How many to return? ");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        return userChoices;

    }



}
