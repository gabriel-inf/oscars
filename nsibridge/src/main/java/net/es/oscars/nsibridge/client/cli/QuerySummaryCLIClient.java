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
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.provider.ConnectionProviderPort;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.QuerySummaryConfirmedType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.QuerySummaryResultType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.QueryType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.headers.CommonHeaderType;


public class QuerySummaryCLIClient {
    public static void main(String[] args){
        //initialize input variables
        String url = "http://localhost:8500/nsi-v2/ConnectionServiceProvider";
        String[] connectionIds = new String[0];
        String[] gris = new String[0];
        
        //parse options
        OptionParser parser = new OptionParser(){
            {
                acceptsAll(Arrays.asList("h", "help"), "prints this help screen");
                acceptsAll(Arrays.asList("u", "url"), "the URL of the NSA provider").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("i", "connection-id"), "the connection id(s) to query. Separate multiple by commas with no whitespace").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("g", "gri"), "the global reservation ID of the connecion(s) to return. Separate multiple by commas with no whitespace").withRequiredArg().ofType(String.class);
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
            
            if(opts.has("i")){
                connectionIds = ((String)opts.valueOf("i")).split(",");
            }
            if(opts.has("g")){
                gris = ((String)opts.valueOf("g")).split(",");
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
        
        //create client
        ConnectionProviderPort client = ClientUtil.createProviderClient(url);
        
        //Build request
        Holder<CommonHeaderType> header = ClientUtil.makeClientHeader();
        QueryType querySummarySync = new QueryType();
        for(String connId : connectionIds){
            querySummarySync.getConnectionId().add(connId);
        }
        for(String gri : gris){
            querySummarySync.getGlobalReservationId().add(gri);
        }
        
        //send request;
        QuerySummaryConfirmedType result = null;
        try {
            result = client.querySummarySync(querySummarySync , header);
        } catch (QuerySummarySyncFailed e) {
            e.printStackTrace();
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
    }
}
