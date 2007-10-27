import java.util.*;
import java.text.DateFormat;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;

import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlanePathContent;
import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlaneHopContent;

import net.es.oscars.oscars.AAAFaultMessage;
import net.es.oscars.oscars.BSSFaultMessage;
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
        } catch (AAAFaultMessage e) {
            System.out.println(
                    "AAAFaultMessage from createReservation");
            System.out.println(e.getFaultMessage().getMsg());
        } catch (BSSFaultMessage e) {
            System.out.println(
                    "BSSFaultMessage from createReservation");
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
            throws AAAFaultMessage, BSSFaultMessage,
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

        // TODO:  better handling in readParams and readProperties
        PathInfo pathInfo = content.getPathInfo();
        CtrlPlanePathContent path = pathInfo.getPath();
        // if prompting for parameters
        BufferedReader br =
                new BufferedReader(new InputStreamReader(System.in));
 
        arg = Args.getArg(br, "Request Type (l2 or l3)", "l3");
        
        if(arg.equals("l2")){
            Layer2Info layer2Info = pathInfo.getLayer2Info();
            
            pathInfo.setLayer3Info(null);
            pathInfo.setMplsInfo(null);
            
            arg = Args.getArg(br, "Source Endpoint", layer2Info.getSrcEndpoint());
            if (arg != null){ layer2Info.setSrcEndpoint(arg); }
            
            arg = Args.getArg(br, "Destination Endpoint", layer2Info.getDestEndpoint());
            if (arg != null){ layer2Info.setDestEndpoint(arg); }
            
            arg = Args.getArg(br, "VLAN Tag (Source and Destination)", layer2Info.getSrcVtag().getString());
            if (arg != null){
                VlanTag srcVtag = layer2Info.getSrcVtag();
                VlanTag destVtag = layer2Info.getDestVtag();
                srcVtag.setString(arg);
                destVtag.setString(arg);
                arg = Args.getArg(br, "Tag source port?", srcVtag.getTagged()+"");
                srcVtag.setTagged(arg.equals("true"));
                arg = Args.getArg(br, "Tag destination port?", destVtag.getTagged()+"");
                destVtag.setTagged(arg.equals("true"));
                layer2Info.setSrcVtag(srcVtag); 
                layer2Info.setDestVtag(destVtag); 
            }
            
        }else if(arg.equals("l3")){
            Layer3Info layer3Info = pathInfo.getLayer3Info();
            MplsInfo mplsInfo = pathInfo.getMplsInfo(); 
            pathInfo.setLayer2Info(null);
            arg = Args.getArg(br, "Source host", layer3Info.getSrcHost());
            if (arg != null) { layer3Info.setSrcHost(arg); }
            arg = Args.getArg(br, "Destination host", layer3Info.getDestHost());
            if (arg != null) { layer3Info.setDestHost(arg); }         
            arg = Args.getArg(br, "Burst limit",
                    Integer.toString(mplsInfo.getBurstLimit()));
            if (arg != null) { mplsInfo.setBurstLimit(Integer.parseInt(arg)); }     
            arg = Args.getArg(br, "Protocol", "");
            if (!arg.equals("")) { layer3Info.setProtocol(arg); }
        }
        
        /* Common parameters */
        arg = Args.getArg(br, "Duration (hours)", "0.06");
            if (arg == null) {
                System.out.println("Duration must be provided");
                System.exit(1);
            }
        this.setTimes(content, arg);
            
        arg = Args.getArg(br, "Bandwidth",
                    Integer.toString(content.getBandwidth()));
            if (arg != null) { content.setBandwidth(Integer.parseInt(arg)); }
        arg = Args.getArg(br, "Description", content.getDescription());
            if (arg != null) { content.setDescription(arg); }
        arg = Args.getArg(br, "Path Setup Mode", pathInfo.getPathSetupMode());
            if (arg != null) { pathInfo.setPathSetupMode(arg); }
            
        arg =
            Args.getLines(br, "Path: if present, ips or topo ids ended by two new lines");
        if (!arg.equals("")) {
            path = new CtrlPlanePathContent();
            path.setId("userPath");//id doesn't matter in this context
            String hops[] = arg.split(" ");
            for (int i = 0; i < hops.length; i++){
               CtrlPlaneHopContent hop = new CtrlPlaneHopContent();
               // these can currently be either topology identifiers
               // or IP addresses
               hop.setId(i + "");
               hop.setLinkIdRef(hops[i]);
               path.addHop(hop);
            }
            pathInfo.setPath(path);
        }

        return content;
    }

    public ResCreateContent readProperties() {
        ResCreateContent content = new ResCreateContent();

        Properties props = new Properties();
        String oscars_home = System.getenv("OSCARS_HOME");
        String propFileName =  "";
        if(oscars_home == null || oscars_home.equals("")){
           propFileName ="reservation.properties"; 
        }else{
            propFileName =  System.getenv("OSCARS_HOME") +
                "/examples/javaClients/reservation.properties";
        }
        System.out.println(propFileName);
        
        try {
            FileInputStream in = new FileInputStream(propFileName);
            props.load(in);
            in.close();
        }
        catch (IOException e) {
            System.out.println(" no default properties file " +
                               propFileName);
        }
   
        PathInfo pathInfo = new PathInfo();
        Layer3Info layer3Info = new Layer3Info();
        Layer2Info layer2Info = new Layer2Info();
        
        MplsInfo mplsInfo = new MplsInfo();
        content.setDescription(props.getProperty("description",""));
        layer2Info.setSrcEndpoint(props.getProperty("sourceEndpoint",""));
        layer2Info.setDestEndpoint(props.getProperty("destEndpoint",""));
        VlanTag srcVtag = new VlanTag();
        srcVtag.setString(props.getProperty("vtag",""));
        srcVtag.setTagged(true);        
        layer2Info.setSrcVtag(srcVtag);
        VlanTag destVtag = new VlanTag();
        destVtag.setString(props.getProperty("vtag",""));
        destVtag.setTagged(true);  
        layer2Info.setDestVtag(destVtag);
        layer3Info.setSrcHost(props.getProperty("sourceHostName",""));
        layer3Info.setDestHost(props.getProperty("destHostName",""));
        // Axis2 bug workaround
        layer3Info.setSrcIpPort(0);
        layer3Info.setDestIpPort(0);
        content.setBandwidth(
                Integer.parseInt(props.getProperty("bandwidth","10")));
        mplsInfo.setBurstLimit(
                Integer.parseInt(props.getProperty("burstLimit","10000")));
        pathInfo.setPathSetupMode(props.getProperty("pathSetupMode",""));
        pathInfo.setLayer3Info(layer3Info);
        pathInfo.setLayer2Info(layer2Info);
        pathInfo.setMplsInfo(mplsInfo);
        content.setPathInfo(pathInfo);
        return content;
    }

    public void outputResponse(CreateReply response) {
        System.out.println("GRI: " + response.getGlobalReservationId());
        System.out.println("Status: " + response.getStatus().toString());
        
        String token = response.getToken();
        if(token != null){
            System.out.println("Token: " + token);
        }

	if ((response.getPathInfo() != null) &&
            (response.getPathInfo().getLayer3Info() != null)) {
            this.outputHops(response.getPathInfo().getPath());
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


