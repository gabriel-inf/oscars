import java.util.*;
import java.text.DateFormat;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;

import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlanePathContent;
import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlaneHopContent;

import org.apache.axis2.AxisFault;

import net.es.oscars.oscars.AAAFaultMessage;
import net.es.oscars.oscars.BSSFaultMessage;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.client.Client;
import net.es.oscars.PropHandler;


public class CreateReservationClient {
    /**
     * @param args  [0] directory name of the client repository
     *                  contains rampart.mar and axis2.xml
     *              [1] the default url of the service endpoint
     */
    public static void main(String[] args) {


        try {
            CreateReservationClient cl = new CreateReservationClient();
            cl.create(args);
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

    public CreateReply create(String[] args)
            throws AAAFaultMessage, BSSFaultMessage,
                   java.rmi.RemoteException, Exception {

        CreateReply response = null;

        response = this.sendContent(args);
        this.outputResponse(response);
        return response;
    }

    public CreateReply sendContent(String[] args)
        throws AAAFaultMessage, RemoteException, Exception {

        ResCreateContent content = new ResCreateContent();

        Client client = null;
        Properties props = new Properties();
        Layer2Info layer2Info = null;
        Layer3Info layer3Info = null;
        MplsInfo mplsInfo = null;
        String oscars_home = System.getenv("OSCARS_HOME");
        String propFileName = null;
        String url = null;
        String param = null;
        // use the alternate URL in the file
        boolean useAlternateURL = false;
        // which network layer to use
        String layer = null;
        
        for(int i = 0; i < args.length; i++){
            if (args[i].equals("-help") ||
                args[i].equals("-h")) {
                // properties file only optional if running from CLI
                System.out.println("usage from createRes.sh: ./createRes.sh -pf propertiesFile [-alt]");
                System.out.println("-alt chooses an alternate URL for a IDC from the properties file");
                System.exit(0);
            }
            if (args[i].equals("-pf")) {
                propFileName = args[i+1];
            } else if (args[i].equals("-alt")) {
                useAlternateURL = true;
            }
        }
        if (propFileName == null) {
            System.err.println("-pf propertiesFile option must be chosen");
            System.exit(0);
        }

        if (oscars_home != null && !oscars_home.equals("")) {
            propFileName =  System.getenv("OSCARS_HOME") +
                "/examples/javaClients/" + propFileName;
        }
        System.out.println("Properties file: " + propFileName);
        
        try {
            FileInputStream in = new FileInputStream(propFileName);
            props.load(in);
            in.close();
        } catch (IOException e) {
            System.out.println(" Properties file not found: " +
                               propFileName);
        }
        if (!useAlternateURL) {
            url = props.getProperty("url1");
            if (url == null) {
                System.err.println("url1 property is null; exiting");
                System.exit(0);
            }
        } else {
            url = props.getProperty("url2");
            if (url == null) {
                System.err.println("url2 property is null; exiting");
                System.exit(0);
            }
        }
        System.out.println("contacting URL: " + url);
        client = new Client();
        try {
            client.setUp(true, url, "repo");
        } catch (AxisFault e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
        layer = props.getProperty("layer").trim();
        if (layer == null) {
            System.err.println("layer property is null; exiting");
            System.exit(0);
        }
        if (!layer.equals("2") && !layer.equals("3")) {
            System.err.println("layer must be 2 or 3; exiting");
            System.exit(0);
        }
        PathInfo pathInfo = new PathInfo();
        if (layer.equals("2")) {
            layer2Info = new Layer2Info();
        } else {
            layer3Info = new Layer3Info();
        }
        // for layer 2, this will only be used if the router configured is
        // a Juniper
        mplsInfo = new MplsInfo();
        content.setDescription(props.getProperty("description",""));
        param = props.getProperty("duration").trim();
        if (param == null) {
            System.err.println("duration property is null; exiting");
            System.exit(0);
        }
        this.setTimes(content, param);
        if (layer.equals("2")) {
            param = props.getProperty("sourceEndpoint").trim();
            if (param == null) {
                System.err.println("sourceEndpoint property is null; exiting");
                System.exit(0);
            }
            layer2Info.setSrcEndpoint(param);
            param = props.getProperty("destEndpoint").trim();
            if (param == null) {
                System.err.println("destEndpoint property is null; exiting");
                System.exit(0);
            }
            layer2Info.setDestEndpoint(param);
            VlanTag srcVtag = new VlanTag();
            param = props.getProperty("vtag").trim();
            if (param == null) {
                System.err.println("vtag property is null; exiting");
                System.exit(0);
            }
            srcVtag.setString(param);
            srcVtag.setTagged(true);        
            layer2Info.setSrcVtag(srcVtag);
            VlanTag destVtag = new VlanTag();
            // same as srcVtag for now
            destVtag.setString(param);
            destVtag.setTagged(true);  
            layer2Info.setDestVtag(destVtag);
        } else {
            param = props.getProperty("sourceHostName").trim();
            if (param == null) {
                System.err.println("sourceHostName property is null; exiting");
                System.exit(0);
            }
            layer3Info.setSrcHost(param);
            param = props.getProperty("destHostName").trim();
            if (param == null) {
                System.err.println("destHostName property is null; exiting");
                System.exit(0);
            }
            layer3Info.setDestHost(param);
            // Axis2 bug workaround
            layer3Info.setSrcIpPort(0);
            layer3Info.setDestIpPort(0);
        }
        mplsInfo.setBurstLimit(
                Integer.parseInt(props.getProperty("burstLimit","10000000")));
        content.setBandwidth(
                Integer.parseInt(props.getProperty("bandwidth","10")));
        pathInfo.setPathSetupMode(props.getProperty("pathSetupMode", "timer-automatic"));
        if (layer.equals("2")) {
            pathInfo.setLayer2Info(layer2Info);
        } else {
            pathInfo.setLayer3Info(layer3Info);
        }
        pathInfo.setMplsInfo(mplsInfo);
        CtrlPlanePathContent path = new CtrlPlanePathContent();
        path.setId("userPath");//id doesn't matter in this context
        boolean hasEro = false;
        // TODO:  FIX limitation
        for (int i = 0; i < 10 ; i++) {
            String propName = "ero_"+Integer.toString(i);
            String hopId = props.getProperty(propName);
            if (hopId != null) {
                hopId = hopId.trim();
                hasEro = true;
                CtrlPlaneHopContent hop = new CtrlPlaneHopContent();
                hop.setId(i + "");
                hop.setLinkIdRef(hopId);
                path.addHop(hop);
            }
        }
        if (hasEro) {
            pathInfo.setPath(path);
        }
        content.setPathInfo(pathInfo);
        param = props.getProperty("interactive", "0");
        // if interactive, allow to override selected parameters
        if (!param.equals("0")) {
            try {
                this.overrideProperties(content);
            } catch (IOException ioe) {
                System.out.println("IO error reading input");
                System.exit(1);
            }
        }
        // make the call to the server
        CreateReply response = client.createReservation(content);
        return response;
    }

    public void overrideProperties(ResCreateContent content)
            throws IOException {

        String arg = null;

        // if prompting for parameters
        BufferedReader br =
                new BufferedReader(new InputStreamReader(System.in));
 
        PathInfo pathInfo = content.getPathInfo();
        Layer2Info layer2Info = pathInfo.getLayer2Info();
        Layer3Info layer3Info = pathInfo.getLayer3Info();
        if (layer2Info != null) {
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
        } else if (layer3Info != null) {
            arg = Args.getArg(br, "Source host", layer3Info.getSrcHost());
            if (arg != null) { layer3Info.setSrcHost(arg); }
            arg = Args.getArg(br, "Destination host", layer3Info.getDestHost());
            if (arg != null) { layer3Info.setDestHost(arg); }         
            arg = Args.getArg(br, "Protocol", "");
            if (!arg.equals("")) { layer3Info.setProtocol(arg); }
        }
        MplsInfo mplsInfo = pathInfo.getMplsInfo();
        arg = Args.getArg(br, "Burst limit",
                Integer.toString(mplsInfo.getBurstLimit()));
        if (arg != null) { mplsInfo.setBurstLimit(Integer.parseInt(arg)); }     
        
        /* Common parameters */
        // TODO:  get default from properties
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
        // don't allow override of ERO at this time
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
        
        startTime = System.currentTimeMillis()/1000;
        content.setStartTime(startTime);

        double dseconds = Double.valueOf(duration) * 3600.0;
        long seconds = (long)dseconds;
           
        endTime = startTime + seconds;
        content.setEndTime(endTime);
        // format for printing (have to convert back to milliseconds)
        Date date = new Date(startTime*1000);
        System.out.println("Start time: " +
                DateFormat.getDateTimeInstance(
                    DateFormat.LONG, DateFormat.LONG).format(date));
        date = new Date(endTime*1000);
        System.out.println("End time: " + 
                DateFormat.getDateTimeInstance(
                    DateFormat.LONG, DateFormat.LONG).format(date));
    }

    public void outputHops(CtrlPlanePathContent path) {
        System.out.println("Path is:");
        CtrlPlaneHopContent[] hops = path.getHop();
        for (int i = 0; i < hops.length; i++) {
            // What is passed back depends on what layer information is
            // associated with a reservation.  This will be a topology
            // identifier for layer 2, and an IPv4 or IPv6 address for
            // layer 3.
            String hopId = hops[i].getLinkIdRef();
            System.out.println("\t" + hopId);
        }
    }
}


