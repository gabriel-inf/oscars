package net.es.oscars.lookup.utils;

import java.io.PrintStream;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import net.es.oscars.utils.clients.LookupClient;
import net.es.oscars.common.soap.gen.MessagePropertiesType;
import net.es.oscars.lookup.soap.gen.LookupFaultMessage;
import net.es.oscars.lookup.soap.gen.LookupPortType;
import net.es.oscars.lookup.soap.gen.LookupRequestContent;
import net.es.oscars.lookup.soap.gen.LookupResponseContent;
import net.es.oscars.lookup.soap.gen.Protocol;
import net.es.oscars.lookup.soap.gen.Relationship;
import net.es.oscars.utils.soap.OSCARSServiceException;

public class LookupUtil {
    private LookupPortType client;
    private PrintStream out;
    
    final static private String defaultUrl = "http://localhost:9014/lookup";
    final static private String defaultType = "IDC";
    final static private String defaultRelType = "controls";
    
    static {
        Logger.getRootLogger().setLevel(Level.OFF);
    }
    
    public LookupUtil(String url) throws MalformedURLException, OSCARSServiceException{
        this.client = LookupClient.getClient(url).getPortType();
        this.out = new PrintStream(System.out);
    }
    
    public void lookup(String type, String relationship, String url) throws LookupFaultMessage{
        LookupRequestContent request = new LookupRequestContent();
        request.setMessageProperties(new MessagePropertiesType());
        request.getMessageProperties().setGlobalTransactionId(UUID.randomUUID().toString());
        if(type == null){
            this.out.println("Must specify service type");
            System.exit(1);
        }
        request.setType(type);
        
        if(relationship != null){
            String[] relParts = relationship.split("=", 2);
            if(relParts.length < 2){
                System.out.println("Please specify relationships as <Type>=<RelatedToId>");
                System.exit(1);
            }
            Relationship rel = new Relationship();
            rel.setType(relParts[0]);
            rel.setRelatedTo(relParts[1]);
            request.setHasRelationship(rel);
        }
        
        request.setHasLocation(url);
        
        LookupResponseContent response = this.client.lookup(request);
        System.out.println();
        System.out.println("Type: " + response.getType());
        System.out.println("Protocols:");
        for(Protocol protocol : response.getProtocol()){
            this.out.println("    Type: " + protocol.getType());
            this.out.println("    Location: " + protocol.getLocation());
            this.out.println();
        }
        System.out.println("Relationships:");
        for(Relationship rel : response.getRelationship()){
            System.out.println("    [" + rel.getType() + "] " + 
                    rel.getRelatedTo());
        }
        System.out.println();
    }
    
    public static void main(String[] args){
        String helpMsg = "\nUsage: oscars-lookup [opts] <domain>\n\n";
        helpMsg += "The optional <domain> argument is the name " +
                "of the domain with the IDC you wish to find. Alternatively you may " +
                "use the options below to perform more advanced lookups. The " +
                "default behavior is equivalenet to '-t IDC -r " +
                "controls=urn:ogfnetwork:domain=<domain>'.\n\n";
        
        String url = defaultUrl;
        String type = defaultType;
        OptionParser parser = new OptionParser(){
            {
                acceptsAll(Arrays.asList("h", "help"), "prints this help screen");
                acceptsAll(Arrays.asList("u", "url"), "the URL of the OSCARS lookup module to contact").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("t", "type"), "the type of service to lookup").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("l", "location"), "the URL of the service to lookup").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("relationship", "r"), "lookup a service with the given relationship" +
                "Takes form <Type>=<RelativeId>.").withRequiredArg().ofType(String.class);
            }
        };
        
        try {
            OptionSet opts = parser.parse(args);
            if(opts.has("h")){
                System.out.println(helpMsg);
                try{
                    parser.printHelpOn(System.out);
                }catch(Exception e){}
            }
            List<String> nonOpts = opts.nonOptionArguments();
            if(nonOpts.size() > 0 && (opts.has("r") || opts.has("l"))){
                System.out.println("Do not pass bare argument if specifying -r or -l.");
                System.exit(1);
            }
            
            if(opts.has("u")){
                url = (String)opts.valueOf("u");
            }
            
            if(opts.has("t")){
                type = (String) opts.valueOf("t");
            }
            
            LookupUtil util = new LookupUtil(url);
            if(nonOpts.size() == 1){
                util.lookup(type, defaultRelType + "=urn:ogf:network:domain=" + 
                        nonOpts.get(0), null);
            }else{
                util.lookup(type, (String)opts.valueOf("r"), (String)opts.valueOf("l"));
            }
        }catch(OptionException e){
            System.out.println(e.getMessage());
            System.out.println(helpMsg);
            try{
                parser.printHelpOn(System.out);
            }catch(Exception e1){}
            System.exit(1);
        }catch (MalformedURLException e) {
            System.out.println("Invalid URL provided for OSCARS lookup module");
            System.exit(1);
        } catch (Exception e){
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }
}
