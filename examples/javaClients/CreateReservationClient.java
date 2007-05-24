import java.util.*;
import java.text.DateFormat;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;

import net.es.oscars.oscars.AAAFaultMessageException;
import net.es.oscars.oscars.BSSFaultMessageException;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.client.Client;
import net.es.oscars.PropHandler;


public class CreateReservationClient extends ExampleClient {
    /**
     * @param args  [0] directory name of the client repository
     *                  contains rampart.mar and axis2.xml
     *              [1] the default url of the service endpoint
     */
    public static void main(String[] args) {


        try {
            CreateReservationClient cl = new CreateReservationClient();
            cl.create(args, true);
        } catch (AAAFaultMessageException e) {
            System.out.println(
                    "AAAFaultMessageException from createReservation");
            System.out.println(e.getFaultMessage().getMsg());
        } catch (BSSFaultMessageException e) {
            System.out.println(
                    "BSSFaultMessageException from createReservation");
            System.out.println(e.getFaultMessage().getMsg());
        } catch (java.rmi.RemoteException e) {
            System.out.println(
                    "RemoteException returned from createReservation");
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(
                    "OSCARSStub threw exception in createReservation");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }    
    }

    public CreateReply create(String[] args, boolean isInteractive)
            throws AAAFaultMessageException, BSSFaultMessageException,
                   java.rmi.RemoteException, Exception {

        ResCreateContent content = null;

        super.init(args, isInteractive);
        try {
            content = this.readParams(isInteractive);
        } catch (IOException ioe) {
            System.out.println("IO error reading input");
            System.exit(1);
        }
        // make the call to the server
        CreateReply response = this.getClient().createReservation(content);
        this.outputResponse(response);
        return response;
    }

    public ResCreateContent readParams(boolean isInteractive)
            throws IOException {

        ResCreateContent content = new ResCreateContent();
        String arg = null;

        // used for defaults for standalone client, and as only input
        // for automated tests
        content = this.readProperties();
        if (!isInteractive) { return content; }

        // if prompting for parameters
        BufferedReader br =
                new BufferedReader(new InputStreamReader(System.in));
        arg = Args.getArg(br, "Source host",content.getSrcHost());
        if (arg != null) { content.setSrcHost(arg); }
        arg = Args.getArg(br, "Destination host", content.getDestHost());
        if (arg != null) { content.setDestHost(arg); }
        arg = Args.getArg(br, "Duration (hours)", "0.06");
        if (arg == null) {
            System.out.println("Duration must be provided");
            System.exit(1);
        }
        this.setTimes(content, arg);
        arg = Args.getArg(br, "Burst limit",
                Integer.toString(content.getBurstLimit()));
        if (arg != null) { content.setBurstLimit(Integer.parseInt(arg)); }
        arg = Args.getArg(br, "Bandwidth",
                Integer.toString(content.getBandwidth()));
        if (arg != null) { content.setBandwidth(Integer.parseInt(arg)); }

        arg = Args.getArg(br, "Route direction",
                content.getCreateRouteDirection());
        if (arg != null) { content.setCreateRouteDirection(arg); }
        arg = Args.getArg(br, "Protocol", "");
        if (arg != null) { content.setProtocol(arg); }
        arg = Args.getArg(br, "Description", content.getDescription());
        if (arg != null) { content.setDescription(arg); }
        arg = Args.getArg(br, "Ingress router", "");
        if (!arg.equals("")) { content.setIngressRouterIP(arg); }
        arg = Args.getArg(br, "Egress router", "");
        if (!arg.equals("")) { content.setEgressRouterIP(arg); }
        arg=Args.getArg(br, "RequestedPath: input dotted ipAddrs separated by spaces", "");
        if (!arg.equals("")) {
            String ipaddr[] = arg.split(" ");
            ExplicitPath explicitPath = new ExplicitPath();
            HopList hopList = new HopList();
            for (int i = 0; i < ipaddr.length; i++){
               Hop hop = new Hop();
               // r ight now this is all traceroutePathfinder accepts
               hop.setLoose(false);
               if ( ipaddr[i].indexOf('.') != -1) {
                   hop.setType("ipv4");
               } else if (ipaddr[i].indexOf(':')!= -1){
                   hop.setType("ipv6");                 
               } else {
                   System.out.println("Path must be either dotted ipv4 addres or : separated ipv6 addresses");
                   System.exit (1);
               }
               hop.setValue(ipaddr[i]);
               hopList.addHop(hop);
            }
            explicitPath.setHops(hopList);
            content.setReqPath(explicitPath);
            arg = Args.getArg(br, "VLAN Tag", "");
            if (!arg.equals("")) { explicitPath.setVtag(arg); }
        }
        return content;
    }

    public ResCreateContent readProperties() {
        ResCreateContent content = new ResCreateContent();

        Properties props = new Properties();
        String propFileName =  System.getenv("OSCARS_HOME") +
        "/examples/javaClients/reservation.properties";
        try {
            FileInputStream in = new FileInputStream(propFileName);
            props.load(in);
            in.close();
        }
        catch (IOException e) {
            System.out.println(" no default properties file " +
                               propFileName);
        }
   
        content.setDescription(props.getProperty("description",""));
        content.setSrcHost(props.getProperty("sourceHostName",""));
        content.setDestHost(props.getProperty("destHostName",""));
        content.setBandwidth(
                Integer.parseInt(props.getProperty("bandwidth","10")));
        content.setCreateRouteDirection(
                props.getProperty("routeDirection","FORWARD"));
        content.setBurstLimit(
                Integer.parseInt(props.getProperty("burstLimit","10000")));
        return content;
    }

    public void outputResponse(CreateReply response) {

        System.out.println("Tag: " + response.getTag());
        System.out.println("Status: " + response.getStatus().toString());
        if (response.getPath() != null){
            this.outputHops(response.getPath());
        }
    }

    public void setTimes(ResCreateContent content, String duration) {
        // all times are communicated to the server in UTC
        Long startTime;
        Long endTime;
        
        startTime = System.currentTimeMillis();
        content.setStartTime(startTime);

        double dseconds = Double.valueOf(duration) * 3600.0;
        long seconds = (long)dseconds;
           
        endTime = startTime + (seconds * 1000);
        content.setEndTime(endTime);
        // format for printing
        Date date = new Date(startTime);
        System.out.println("Start time: " +
                DateFormat.getDateTimeInstance(
                    DateFormat.LONG, DateFormat.LONG).format(date));
        date = new Date(endTime);
        System.out.println("End time: " + 
                DateFormat.getDateTimeInstance(
                    DateFormat.LONG, DateFormat.LONG).format(date));
    }
}


