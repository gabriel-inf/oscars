package net.es.oscars.nsibridge.client.cli;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.es.oscars.nsibridge.beans.SimpleRequest;
import net.es.oscars.nsibridge.beans.SimpleRequestType;
import net.es.oscars.nsibridge.client.ClientUtil;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.ifce.QuerySummarySyncFailed;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.ifce.ServiceException;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.provider.ConnectionProviderPort;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.QuerySummaryConfirmedType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.QuerySummaryResultType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.QueryType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.headers.CommonHeaderType;

import javax.xml.ws.Holder;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;


public class SimpleCLIClient {
    public static void main(String[] args){
        //initialize input variables
        String url = "http://localhost:8500/nsi-v2/ConnectionServiceProvider";
        String connectionId = null;


        //parse options
        OptionParser parser = new OptionParser(){
            {
                acceptsAll(Arrays.asList("h", "help"), "prints this help screen");
                acceptsAll(Arrays.asList("u", "url"), "the URL of the NSA provider").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("i", "connection-id"), "the connection id (required)").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("o", "operation"), "the operation (required) - one of RESERVE_COMMIT, RESERVE_ABORT, PROVISION, RELEASE, TERMINATE").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("n", "nsa-requester"), "set the NSA requester").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("r", "reply-url"), "the URL to which the provider should reply").withRequiredArg().ofType(String.class);
            }
        };
        SimpleRequestType srt = null;
        Holder<CommonHeaderType> outHolder = ClientUtil.makeClientHeader();
        CommonHeaderType header = new CommonHeaderType();

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
                String replyTo  = (String) opts.valueOf("r");
                new URL(replyTo);
                header.setReplyTo(replyTo);
            }
            
            if(opts.has("i")){
                connectionId = ((String) opts.valueOf("i"));
            } else {
                System.err.println("Undefined connectionId");
                System.exit(1);
            }

            if(opts.has("n")){
                header.setRequesterNSA((String) opts.valueOf("n"));
            }

            if (opts.has("o")) {
                srt = SimpleRequestType.valueOf((String) opts.valueOf("o"));
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
        if (srt == null) {
            System.err.println("Undefined operation");
            System.exit(1);
        }

        ConnectionProviderPort client = ClientUtil.createProviderClient(url);
        try {
            switch (srt) {
                case PROVISION:
                    client.provision(connectionId, header, outHolder);
                    break;
                case RELEASE:
                    client.release(connectionId, header, outHolder);
                    break;
                case RESERVE_COMMIT:
                    client.reserveCommit(connectionId, header, outHolder);
                    break;
                case RESERVE_ABORT:
                    client.reserveAbort(connectionId, header, outHolder);
                    break;
                case TERMINATE:
                    client.terminate(connectionId, header, outHolder);
                    break;
                default:
                    System.err.println("Unsupported operation "+srt);
                    System.exit(1);
            }
        } catch (ServiceException ex) {
            ex.printStackTrace();
            System.exit(1);
        }


        
    }
}
