package net.es.oscars.nsibridge.client.cli;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import javax.xml.ws.Holder;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import net.es.oscars.nsibridge.client.ClientUtil;
import net.es.oscars.nsibridge.client.cli.handlers.QueryHandler;
import net.es.oscars.nsibridge.client.cli.output.QueryOutputter;
import net.es.oscars.nsibridge.client.cli.output.QueryPrettyOutputter;
import net.es.oscars.nsi.soap.gen.nsi_2_0_2013_07.connection.ifce.QuerySummarySyncFailed;
import net.es.oscars.nsi.soap.gen.nsi_2_0_2013_07.connection.ifce.ServiceException;
import net.es.oscars.nsi.soap.gen.nsi_2_0_2013_07.connection.provider.ConnectionProviderPort;
import net.es.oscars.nsi.soap.gen.nsi_2_0_2013_07.connection.types.QuerySummaryConfirmedType;
import net.es.oscars.nsi.soap.gen.nsi_2_0_2013_07.connection.types.QueryType;
import net.es.oscars.nsi.soap.gen.nsi_2_0_2013_07.framework.headers.CommonHeaderType;


/**
 * Client to perform queries to NSI Provider agent
 *
 */
public class QueryCLIClient {
    static final private String FORMAT_PRETTY = "pretty";
    static final private String FORMAT_JSON = "json";
    
    public static void main(String[] args){
        //initialize input variables
        String url = ClientUtil.DEFAULT_URL;
        String clientBusFile = null;
        CLIListener listener = null;
        String[] connectionIds = new String[0];
        String[] gris = new String[0];
        Holder<CommonHeaderType> header = ClientUtil.makeClientHeader();
        boolean recursive = false;
        QueryOutputter outputter = new QueryPrettyOutputter(); 
                
        //parse options
        OptionParser parser = new OptionParser(){
            {
                acceptsAll(Arrays.asList("h", "help"), "prints this help screen");
                acceptsAll(Arrays.asList("u", "url"), "the URL of the NSA provider (default: " + ClientUtil.DEFAULT_URL + ")").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("r", "reply-url"), "the URL to which the provider should reply. If not set this will be a synchronous request.").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("l", "listener"), "Starts listener at reply-url. Parameter is the location of the bus configuration file.").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("f", "bus-file"), "bus file that describes charateristics of HTTP(S) connections").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("i", "connection-id"), "the connection id(s) to query. Separate multiple by commas with no whitespace").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("g", "gri"), "the global reservation ID of the connecion(s) to return. Separate multiple by commas with no whitespace").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("R", "recursive"), "sends a recursive query");
                acceptsAll(Arrays.asList("n", "nsa-requester"), "set the NSA requester (default: " + ClientUtil.DEFAULT_REQUESTER + ")").withRequiredArg().ofType(String.class);
                //acceptsAll(Arrays.asList("output"), "specifies how to format output. Valid options are 'json' or 'pretty'. (default: pretty)").withRequiredArg().ofType(String.class);
            }
        };
        try {
            OptionSet opts = parser.parse(args);
            
            if(opts.has("h")){
                parser.printHelpOn(System.out);
                System.exit(0);
            }
            
            if(opts.has("u")){
                url = (String)opts.valueOf("u");
                new URL(url);
            }
    
            if(opts.has("r")){
                header.value.setReplyTo((String)opts.valueOf("r"));
                new URL(header.value.getReplyTo());
            }
            
            if(opts.has("l")){
                if(!opts.has("r")){
                    System.err.println("Cannot specify -l without -r");
                    System.exit(0);
                }
                listener = new CLIListener(header.value.getReplyTo(), (String)opts.valueOf("l"), new QueryHandler(outputter));
            }
            
            if(opts.has("f")){
                clientBusFile = (String)opts.valueOf("f");
            }
            
            if(opts.has("i")){
                connectionIds = ((String)opts.valueOf("i")).split(",");
            }
            if(opts.has("g")){
                gris = ((String)opts.valueOf("g")).split(",");
            }
            
            if(opts.has("R")){
                recursive = true;
                if(header.value.getReplyTo() == null){
                    System.err.println("Must specify the --reply-url option when doing a recursive query");
                    System.exit(1);
                }
            }
            
            if(opts.has("n")){
                header.value.setRequesterNSA((String)opts.valueOf("n"));
                //make provider same as requester
                header.value.setProviderNSA((String)opts.valueOf("n"));
            }
            
            if(opts.has("output")){
                //TODO: Implement
                String format = ((String)opts.valueOf("f")).toLowerCase();
                if(format.equals(FORMAT_JSON)){
                    
                }else if(format.equals(FORMAT_PRETTY)){
                    outputter = new QueryPrettyOutputter(); 
                }else{
                    System.err.println("Invalid output format: " + format);
                    System.exit(0);
                }
            }
        } catch (OptionException e) {
            System.err.println(e.getMessage());
            try{
                parser.printHelpOn(System.out);
            }catch(IOException e1){}
            System.exit(1);
        } catch (MalformedURLException e) {
            System.err.println("Invalid URL: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        
        //create request
        QueryType queryReq = new QueryType();
        for(String connId : connectionIds){
            queryReq.getConnectionId().add(connId);
        }
        for(String gri : gris){
            queryReq.getGlobalReservationId().add(gri);
        }
        
        //process sync or async request
        ConnectionProviderPort client = ClientUtil.createProviderClient(url, clientBusFile);
        if(header.value.getReplyTo() == null){
            //send sync request
            QuerySummaryConfirmedType result = null;
            try {
                result = client.querySummarySync(queryReq , header);
            } catch (QuerySummarySyncFailed e) {
                e.printStackTrace();
                System.exit(1);
            }
            outputter.outputSummary(result.getReservation());
        }else if(recursive){
            //send recursive request
            if(listener != null){
                listener.start();
            }
            try {
                client.queryRecursive(queryReq, header);
                if(listener == null){
                    System.out.println("Query sent");
                }
            } catch (ServiceException e) {
                e.printStackTrace();
            }
        }else{
            //send async request
            if(listener != null){
                listener.start();
            }
            try {
                client.querySummary(queryReq , header);
                if(listener == null){
                    System.out.println("Query sent");
                }
            } catch (ServiceException e) {
                e.printStackTrace();
            }
        }
    }


}
