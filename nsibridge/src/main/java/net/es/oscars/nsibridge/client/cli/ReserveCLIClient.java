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
import net.es.oscars.nsibridge.client.cli.handlers.ReserveHandler;
import net.es.oscars.nsibridge.client.cli.output.ReserveOutputter;
import net.es.oscars.nsibridge.client.cli.output.ReservePrettyOutputter;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.provider.ConnectionProviderPort;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.ReservationRequestCriteriaType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.ScheduleType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.headers.CommonHeaderType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.types.TypeValuePairType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.services.point2point.EthernetVlanType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.services.point2point.ObjectFactory;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.services.types.DirectionalityType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.services.types.StpType;

/**
 * Client to create or modify a reservation
 */
public class ReserveCLIClient {
    final static long DEFAULT_DURATION = 15*60*1000;//15 minutes
    final static String DEFAULT_SERVICE_TYPE = "http://services.ogf.org/nsi/2013/07/descriptions/EVTS.A-GOLE";
    
    public static void main(String[] args){
      //initialize input variables
        String url = ClientUtil.DEFAULT_URL;
        String clientBusFile = null;
        CLIListener listener = null;
        Holder<String> connectionId = new Holder<String>();
        String globalReservationId = null;
        String description = null;
        Holder<CommonHeaderType> header = ClientUtil.makeClientHeader();
        ReservationRequestCriteriaType criteria = new ReservationRequestCriteriaType();
        ScheduleType schedule = new ScheduleType();
        criteria.setSchedule(schedule);
        ObjectFactory objFactory = new ObjectFactory();
        EthernetVlanType evType = new EthernetVlanType();
        criteria.getAny().add(objFactory.createEvts(evType));
        StpType sourceSTP = new StpType();
        StpType destSTP = new StpType();
        evType.setSourceSTP(sourceSTP);
        evType.setDestSTP(destSTP);
        criteria.setServiceType(DEFAULT_SERVICE_TYPE);
        ReserveOutputter outputter = new ReservePrettyOutputter();
        
        //parse options
        OptionParser parser = new OptionParser(){
            {
                acceptsAll(Arrays.asList("h", "help"), "prints this help screen");
                acceptsAll(Arrays.asList("u", "url"), "the URL of the NSA provider (default: " + ClientUtil.DEFAULT_URL + ")").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("r", "reply-url"), "the URL to which the provider should reply").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("l", "listener"), "Starts listener at reply-url. Should be given location of the bus configuration file.").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("f", "bus-file"), "bus file that describes charateristics of HTTP(S) connections").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("src-net"), "required. network portion of source STP").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("src-local"), "required. local portion of source STP").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("src-label"), "Comma separated list of source STP labels with label key separated by colon from value (e.g. VLAN:100,MYLABEL:2)").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("src-vlan"), "VLAN to assign to the source STP").withRequiredArg().ofType(Integer.class);
                acceptsAll(Arrays.asList("dst-net"), "required. network portion of destination STP").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("dst-local"), "required. local portion of destination STP").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("dst-label"), "Comma separated list of destination STP labels with label key separated by colon from value (e.g. VLAN:100,MYLABEL:2)").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("dst-vlan"), "VLAN to assign to the destination STP").withRequiredArg().ofType(Integer.class);
                acceptsAll(Arrays.asList("b", "begin"), "start time of reservation as Unix timestamp (default: the current time)").withRequiredArg().ofType(Long.class);
                acceptsAll(Arrays.asList("e", "end"), "end time of reservation as Unix timestamp (default: 15 minutes after the start time)").withRequiredArg().ofType(Long.class);
                acceptsAll(Arrays.asList("t", "service-type"), "the service type of the request").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("v", "version"), "version of the connection").withRequiredArg().ofType(Integer.class);
                acceptsAll(Arrays.asList("c", "capacity"), "capacity of the connection in Mbps").withRequiredArg().ofType(Long.class);
                acceptsAll(Arrays.asList("p", "description"), "a description of the reservation").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("i", "connection-id"), "the connection ID to assign").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("g", "gri"), "the global reservation ID to assign").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("n", "nsa-requester"), "set the NSA requester (default: " + ClientUtil.DEFAULT_REQUESTER + ")").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("direction"), "directionality (Bidirectional or unidirectional) of the connection").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("asymmetric"), "indicates path is asymmetric (symmetric if not set)");
//                acceptsAll(Arrays.asList("ero"), "List of hops in ero separated by commas. Must include source and destination.");
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
                listener = new CLIListener(header.value.getReplyTo(), (String)opts.valueOf("l"), new ReserveHandler(outputter));
            }
            
            if(opts.has("f")){
                clientBusFile = (String)opts.valueOf("f");
            }
            
            if(opts.has("p")){
                description = (String)opts.valueOf("p");
            }
            
            if(opts.has("i")){
                connectionId.value = (String)opts.valueOf("i");
            }
            
            if(opts.has("g")){
                globalReservationId = (String)opts.valueOf("g");
            }
            
            if(opts.has("src-net")){
                sourceSTP.setNetworkId((String)opts.valueOf("src-net"));
            }else{
                System.err.println("Missing required option src-net");
                System.exit(1);
            }
            
            if(opts.has("src-local")){
                sourceSTP.setLocalId((String)opts.valueOf("src-local"));
            }else{
                System.err.println("Missing required option src-local");
                System.exit(1);
            }
            
            if(opts.has("src-labels")){
                String[] labels = ((String)opts.valueOf("src-labels")).split(",");
                for(String label : labels){
                    String[] lblParts = label.split(":");
                    if(lblParts.length != 2){
                        System.err.println("Invalid label " + label);
                        System.exit(1);
                    }
                    TypeValuePairType tvpLabel = new TypeValuePairType();
                    tvpLabel.setType(lblParts[0]);
                    tvpLabel.getValue().add(lblParts[1]);
                    sourceSTP.getLabels().getAttribute().add(tvpLabel );
                }
            }
            
            if(opts.has("src-vlan")){
                evType.setSourceVLAN((Integer)opts.valueOf("src-vlan"));
            }
            
            if(opts.has("dst-net")){
                destSTP.setNetworkId((String)opts.valueOf("dst-net"));
            }else{
                System.err.println("Missing required option dst-net");
                System.exit(1);
            }
            
            if(opts.has("dst-local")){
                destSTP.setLocalId((String)opts.valueOf("dst-local"));
            }else{
                System.err.println("Missing required option dst-local");
                System.exit(1);
            }
            
            if(opts.has("dst-labels")){
                String[] labels = ((String)opts.valueOf("dst-labels")).split(",");
                for(String label : labels){
                    String[] lblParts = label.split(":");
                    if(lblParts.length != 2){
                        System.err.println("Invalid label " + label);
                        System.exit(1);
                    }
                    TypeValuePairType tvpLabel = new TypeValuePairType();
                    tvpLabel.setType(lblParts[0]);
                    tvpLabel.getValue().add(lblParts[1]);
                    destSTP.getLabels().getAttribute().add(tvpLabel );
                }
            }
            
            if(opts.has("dst-vlan")){
                evType.setDestVLAN((Integer)opts.valueOf("dst-vlan"));
            }
            
            if(opts.has("b")){
                schedule.setStartTime(ClientUtil.unixtimeToXMLGregCal(((Long)opts.valueOf("b")) * 1000L));
            }else{
                schedule.setStartTime(ClientUtil.unixtimeToXMLGregCal(System.currentTimeMillis()));
            }
            
            if(opts.has("e")){
                schedule.setEndTime(ClientUtil.unixtimeToXMLGregCal(((Long)opts.valueOf("e")) * 1000L));
            }else{
                schedule.setEndTime(ClientUtil.unixtimeToXMLGregCal(System.currentTimeMillis() + DEFAULT_DURATION));
            }
            
            if(opts.has("c")){
                evType.setCapacity((Long)opts.valueOf("c"));
            }
            
            if(opts.has("direction")){
                try{
                    evType.setDirectionality(DirectionalityType.valueOf((String)opts.valueOf("direction")));
                }catch(Exception e){
                    System.err.println("Invalid direction: " + opts.valueOf("direction"));
                    System.exit(1);
                }
            }
            
            if(opts.has("asymmetric")){
                evType.setSymmetricPath(false);
            }else{
                evType.setSymmetricPath(true);
            }
            
            if(opts.has("t")){
                criteria.setServiceType((String)opts.valueOf("t"));
            }
            
            if(opts.has("v")){
                criteria.setVersion((Integer)opts.valueOf("v"));
            } else {
                criteria.setVersion(0);
            }
            
            if(opts.has("n")){
                header.value.setRequesterNSA((String)opts.valueOf("n"));
               //make provider same as requester
                header.value.setProviderNSA((String)opts.valueOf("n"));
            }
            
            //create listener
            if(listener != null){
                listener.start();
            }
            
            //create client
            ConnectionProviderPort client = ClientUtil.createProviderClient(url, clientBusFile);
            
            //Send request
            client.reserve(connectionId, globalReservationId, description, criteria, header.value, new Holder<CommonHeaderType>());
            outputter.outputResponse(connectionId.value);
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
    }
}
