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
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.ifce.QuerySummarySyncFailed;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.ifce.ServiceException;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.provider.ConnectionProviderPort;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.QuerySummaryConfirmedType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.QuerySummaryResultType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.QueryType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.headers.CommonHeaderType;


public class QueryCLIClient {
    public static void main(String[] args){
        //initialize input variables
        String url = "http://localhost:8500/nsi-v2/ConnectionServiceProvider";
        String[] connectionIds = new String[0];
        String[] gris = new String[0];
        Holder<CommonHeaderType> header = ClientUtil.makeClientHeader();
        boolean recursive = false;
        
        //parse options
        OptionParser parser = new OptionParser(){
            {
                acceptsAll(Arrays.asList("h", "help"), "prints this help screen");
                acceptsAll(Arrays.asList("u", "url"), "the URL of the NSA provider").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("r", "reply-url"), "the URL to which the provider should reply. If not set this will be a synchronous request.").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("i", "connection-id"), "the connection id(s) to query. Separate multiple by commas with no whitespace").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("g", "gri"), "the global reservation ID of the connecion(s) to return. Separate multiple by commas with no whitespace").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("R", "recursive"), "sends a recursive query");
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
                new URL(url);
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
        ConnectionProviderPort client = ClientUtil.createProviderClient(url);
        if(header.value.getReplyTo() == null){
            //send sync request
            QuerySummaryConfirmedType result = null;
            try {
                result = client.querySummarySync(queryReq , header);
            } catch (QuerySummarySyncFailed e) {
                e.printStackTrace();
                System.exit(1);
            }

            //print output
            for(QuerySummaryResultType querySummRes : result.getReservation()){
                System.out.println();
                System.out.println("Connection: " + querySummRes.getConnectionId());
                System.out.println("Global Reservation Id: " + querySummRes.getGlobalReservationId());
                System.out.println("Requester NSA: " + querySummRes.getRequesterNSA());
                System.out.println("Description: " + querySummRes.getDescription());
                System.out.println();
            }
            System.out.println(result.getReservation().size() + " results returned");
        }else if(recursive){
            try {
                client.queryRecursive(queryReq, header);
                System.out.println("Query sent");
            } catch (ServiceException e) {
                e.printStackTrace();
            }
        }else{
            //send async request
            try {
                client.querySummary(queryReq , header);
                System.out.println("Query sent");
            } catch (ServiceException e) {
                e.printStackTrace();
            }
        }

        
    }
}
