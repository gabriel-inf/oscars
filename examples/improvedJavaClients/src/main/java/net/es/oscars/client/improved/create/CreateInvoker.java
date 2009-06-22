package net.es.oscars.client.improved.create;

import static java.util.Arrays.asList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.es.oscars.client.improved.ConsoleArgs;
import net.es.oscars.wsdlTypes.CreateReply;
import net.es.oscars.wsdlTypes.ResCreateContent;

public class CreateInvoker {
    public static void main(String[] args) throws Exception {
        String configFile = CreateClient.DEFAULT_CONFIG_FILE;
        String soapConfigFile = CreateClient.DEFAULT_SOAP_CONFIG_FILE;
        String soapConfigId = CreateClient.DEFAULT_SOAP_CONFIG_ID;

        // create a parser
        OptionParser parser = new OptionParser() {
            {
                acceptsAll( asList( "h", "?" ), "show help then exit" );
                accepts( "help", "show extended help then exit" );
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
        CreateClient cl = new CreateClient();
        cl.setSoapConfigFile(soapConfigFile);
        cl.configureSoap(soapConfigId);
        cl.setConfigFile(configFile);
        cl.configure();
        
        
        List<CreateRequestParams> requests = cl.getResvRequests();
        for (CreateRequestParams params : requests) {
        	ResCreateContent createReq = cl.formRequest(params);
        	CreateReply resp = cl.performRequest(createReq);
        	System.out.println("response:");
        	System.out.println("gri: "+resp.getGlobalReservationId());
            System.out.println("status: "+resp.getStatus());
        }

    }
    
    
    private static HashMap<String, String> getUserInput() {
        HashMap<String, String> userChoices = new HashMap<String, String>();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            String numStr = ConsoleArgs.getArg(br, "this does nothing at the moment ");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        return userChoices;

    }

}
