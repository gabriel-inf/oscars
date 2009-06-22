package net.es.oscars.client.improved.list;

import static java.util.Arrays.asList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.ibm.wsdl.util.StringUtils;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import net.es.oscars.client.improved.ConfigHelper;
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
                accepts( "i", "interactive mode" );
                accepts( "v", "verbose" );
                accepts( "help", "show extended help then exit" );
                accepts( "p", "soap config filename (config/soap.yaml)" ).withRequiredArg().ofType(String.class);
                accepts( "d", "identifier for soap config (\"default\")" ).withRequiredArg().ofType(String.class);
                accepts( "c", "config filename (config/list.yaml)" ).withRequiredArg().ofType(String.class);
                accepts( "n", "number of results to return" ).withRequiredArg().ofType(String.class);
                accepts( "s", "comma-separated statuses to match" ).withRequiredArg().ofType(String.class);
                accepts( "t", "comma-separated VLAN tags to match" ).withRequiredArg().ofType(String.class);
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

        if (options.has("p")) {
            soapConfigFile = (String) options.valueOf("p");
        }
        if (options.has("d")) {
            soapConfigId = (String) options.valueOf("d");
        }
        if (options.has("c")) {
            configFile = (String) options.valueOf("c");
        }

        HashMap<String, String> cliArgs = new HashMap<String, String>();
        if (options.has("n")) {
            cliArgs.put("numResults", (String) options.valueOf("n"));
        }
        if (options.has("s")) {
            cliArgs.put("statuses", (String) options.valueOf("s"));
        }
        if (options.has("t")) {
            cliArgs.put("vlans", (String) options.valueOf("t"));
        }

        ConfigHelper cfg = ConfigHelper.getInstance();
        Map config = cfg.getConfiguration(configFile);


        HashMap<String, String> userChoices = new HashMap<String, String>();
        if (options.has("i")) {
            userChoices = getUserInput(config, cliArgs);
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

    @SuppressWarnings("unchecked")
    private static HashMap<String, String> getUserInput(Map config, HashMap<String, String> cliArgs) {
        Map filters = (Map) config.get("filters");

        ArrayList<String> cfgStatuses = (ArrayList<String>) filters.get("by-status");
        ArrayList<String> cfgVlans = (ArrayList<String>) filters.get("by-vlan");
        Integer cfgNumResults = (Integer) filters.get("numResults");

        String cfgStatusesStr = org.apache.commons.lang.StringUtils.join(cfgStatuses.toArray(), ", ");
        String cfgVlansStr = org.apache.commons.lang.StringUtils.join(cfgVlans.toArray(), ", ");

        String tmpVlanStr = cfgVlansStr;
        if (cliArgs.get("vlans") != null && !cliArgs.get("vlans").trim().equals("")) {
            tmpVlanStr = cliArgs.get("vlans").trim();
        }

        String tmpStatusStr = cfgStatusesStr;
        if (cliArgs.get("statuses") != null && !cliArgs.get("statuses").trim().equals("")) {
            tmpStatusStr = cliArgs.get("statuses").trim();
        }

        String tmpNumResultsStr = cfgNumResults.toString();
        if (cliArgs.get("numResults") != null && !cliArgs.get("numResults").trim().equals("")) {
            tmpNumResultsStr = cliArgs.get("numResults").trim();
        }

        boolean syntaxOK = false;
        HashMap<String, String> userChoices = new HashMap<String, String>();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while (!syntaxOK) {
            try {
                String strUsrNumResults = ConsoleArgs.getArg(br, "How many to return? ("+tmpNumResultsStr+") ");
                try {
                    Integer.parseInt(strUsrNumResults);
                } catch (NumberFormatException ex) {
                    System.out.println("Invalid number format");
                    continue;
                }
                String strUsrStatuses = ConsoleArgs.getArg(br, "Statuses to return? ("+tmpStatusStr+") ");
                String strUsrVlans 	  = ConsoleArgs.getArg(br, "VLAN tags to look for? ("+tmpVlanStr+") ");
                userChoices.put("numResults", strUsrNumResults);
                userChoices.put("statuses", strUsrStatuses);
                userChoices.put("vlans", strUsrVlans);
                syntaxOK = true;
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        return userChoices;

    }



}
