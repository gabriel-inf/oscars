import net.es.oscars.client.Client;
import net.es.oscars.wsdlTypes.*;
import org.ogf.schema.network.topology.ctrlplane.*;
import net.es.oscars.PropHandler;
import java.util.*;
import java.io.*;
import org.apache.axis2.AxisFault;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import net.es.oscars.ws.AAAFaultMessage;
import net.es.oscars.ws.BSSFaultMessage;

/**
 * OSCARSRandExerciser is a class that issues a series of randomized
 * create reservation requests to OSCARS based on parameters set in a
 * properties file.
 * It is meant to str
 **/
public class OSCARSRandExerciser extends Client{
    private Properties props;
    private String url;
    private int num;

    private int waittime = 1000;
    private HashMap<Integer, String> bandwidths;
    private HashMap<Integer, String> srcendpoints;
    private HashMap<Integer, String> dstendpoints;

    /**
     * Initializes the exerciser client
     *
     * @param propFileName the path to the properties file with test requests
     * @param altURL
     *      boolean indicating whether the alternate URL in the properties file
     *      should be used
     * @param num the number of the reservations to send
     */
    public OSCARSRandExerciser(String propFileName, boolean altURL, int num){
        super();
        this.props = new Properties();
        this.bandwidths = new HashMap<Integer, String>();
        this.srcendpoints = new HashMap<Integer, String>();
        this.dstendpoints = new HashMap<Integer, String>();

        try{
            FileInputStream in = new FileInputStream(propFileName);
            props.load(in);
            in.close();
        }catch (IOException e){
            System.err.println(" Properties file not found: " + propFileName);
            System.exit(1);
        }

        /* Set the url */
        if (altURL){
           this.url = this.props.getProperty("url2");
        }else{
           this.url = this.props.getProperty("url1");
        }


        /* Verify URL set */
        if(url == null){
            System.err.println("Please specify a URL in the properties file");
            System.exit(1);
        }

        this.waittime = Integer.parseInt(this.props.getProperty("waittime"));

        /* Setup client */
        try{
            System.out.println("Testing IDC at "+ url);
            this.setUp(true, url, "repo");
        }catch (AxisFault e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

        this.num = num;
    }

    /**
     *  Conducts tests specified in the properties file given
     */
    public void exercise(){
        this.log.info("exercise.start");
        for (int i = 0; i < 20; i++) {
            String param = this.props.getProperty("resv.bandwidth_"+i);
            if (param == null) {
                i = 100;
            } else {
                this.bandwidths.put(i, param.trim());
                System.out.println(param);
            }
        }
        for (int i = 0; i < 100; i++) {
            String param = this.props.getProperty("resv.srcendpoint_"+i);
            if (param == null) {
                i = 100;
            } else {
                this.srcendpoints.put(i, param.trim());
                System.out.println(param);
            }
        }
        for (int i = 0; i < 100; i++) {
            String param = this.props.getProperty("resv.dstendpoint_"+i);
            if (param == null) {
                i = 100;
            } else {
                this.dstendpoints.put(i, param.trim());
                System.out.println(param);
            }
        }

        for(int i = 1 ; i <= this.num; i++){

            System.out.print("Test " + i + ": createReservation......");
            try{
                this.log.info("createReservation.start");
                ResCreateContent content = this.makeRandCreateResv();
                CreateReply response = this.createReservation(content);
                this.log.info(i + ".response\n" + this.responseToString(response));
                System.out.println(response.getStatus());
                this.log.info("createReservation.end");
                Thread.sleep(waittime);

            }catch (AAAFaultMessage e) {
                System.out.println("FAILED");
                this.log.error(e.getFaultMessage().getMsg());
            }catch (BSSFaultMessage e) {
                System.out.println("FAILED");
                this.log.error(e.getFaultMessage().getMsg());
            }catch (java.rmi.RemoteException e) {
                System.out.println("FAILED");
                this.log.error(e.getMessage());
            }catch (Exception e) {
                System.out.println("FAILED");
                this.log.error(e.getMessage());
            }
        }
        this.log.info("exercise.end");
        System.out.println("Exercises complete");
    }

    private ResCreateContent makeRandCreateResv(){
        ResCreateContent content = new ResCreateContent();
        Layer2Info layer2Info = null;
        Layer3Info layer3Info = null;
        MplsInfo mplsInfo = null;
        String param = null;
        String layer = this.props.getProperty("resv.layer");

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

        int randSrc = -1;
        int randDst = -1;
        int randBw  = -1;

        Random rand = new Random();
        int randInt = rand.nextInt();
        if (randInt < 0) randInt = -randInt;

        randBw = randInt % this.bandwidths.size();
        String bandwidth = this.bandwidths.get(randBw);


        randInt = rand.nextInt();
        if (randInt < 0) randInt = -randInt;

        randSrc = randInt % this.srcendpoints.size();
        String srcEndpoint = this.srcendpoints.get(randSrc);

        String dstEndpoint = "";

        while (randDst == -1 || dstEndpoint.equals(srcEndpoint)) {
            randInt = rand.nextInt();
            if (randInt < 0) randInt = -randInt;
            randDst = randInt % this.dstendpoints.size();
            dstEndpoint = this.dstendpoints.get(randDst);
        }

        System.out.println("bw: "+bandwidth);
        System.out.println("src: "+srcEndpoint);
        System.out.println("dst: "+dstEndpoint);

        // for layer 2, this will only be used if the router configured is
        // a Juniper
        mplsInfo = new MplsInfo();
        content.setDescription(this.props.getProperty("resv.description",""));
        String start_time = this.props.getProperty("resv.start_time");
        String end_time = this.props.getProperty("resv.end_time");
        String duration = this.props.getProperty("resv.duration");
        this.setTimes(content, start_time, end_time, duration);

        if (layer.equals("2")) {
            layer2Info.setSrcEndpoint(srcEndpoint);
            layer2Info.setDestEndpoint(dstEndpoint);
            VlanTag srcVtag = new VlanTag();
            srcVtag.setString("any");
            srcVtag.setTagged(true);
            layer2Info.setSrcVtag(srcVtag);
            VlanTag destVtag = new VlanTag();
            destVtag.setString("any");
            destVtag.setTagged(true);
            layer2Info.setDestVtag(destVtag);
        } else {
            layer3Info.setSrcHost(srcEndpoint);
            layer3Info.setDestHost(srcEndpoint);
            // Axis2 bug workaround
            layer3Info.setSrcIpPort(0);
            layer3Info.setDestIpPort(0);
        }
        param = this.props.getProperty("resv.burstLimit","10000000");
        if(param != null){
            mplsInfo.setBurstLimit(Integer.parseInt(param));
            pathInfo.setMplsInfo(mplsInfo);
        }
        content.setBandwidth(Integer.parseInt(bandwidth));
        pathInfo.setPathSetupMode(this.props.getProperty("resv.pathSetupMode", "timer-automatic"));
        if (layer.equals("2")) {
            pathInfo.setLayer2Info(layer2Info);
        } else {
            pathInfo.setLayer3Info(layer3Info);
        }
        CtrlPlanePathContent path = new CtrlPlanePathContent();
        path.setId("userPath");//id doesn't matter in this context
        boolean hasEro = false;
        int i= 1;
        String propName = "resv.ero_" + i;
        String useHopRefs = this.props.getProperty("resv.useHopRefs");
        String hopId = this.props.getProperty(propName);
        while (hopId != null) {
            hopId = hopId.trim();
            int hopType = hopId.split(":").length;
            hasEro = true;
            CtrlPlaneHopContent hop = new CtrlPlaneHopContent();
            hop.setId(i + "");
            if(hopType == 4){
                hop.setDomainIdRef(hopId);
            }else if(hopType == 5){
                hop.setNodeIdRef(hopId);
            }else if(hopType == 6){
                hop.setPortIdRef(hopId);
            }else if("false".equals(useHopRefs)){
                CtrlPlaneLinkContent link = new CtrlPlaneLinkContent();
                link.setId(hopId);
                link.setTrafficEngineeringMetric("10");
                CtrlPlaneSwcapContent swcap = new CtrlPlaneSwcapContent();
                swcap.setSwitchingcapType("l2sc");
                swcap.setEncodingType("ethernet");
                CtrlPlaneSwitchingCapabilitySpecificInfo swcapInfo = new CtrlPlaneSwitchingCapabilitySpecificInfo();
                swcapInfo.setInterfaceMTU(9000);
                String vlans = this.props.getProperty("resv.ero_vtags_"+ i).trim();
                swcapInfo.setVlanRangeAvailability(vlans);
                String sugVlans = this.props.getProperty("resv.ero_sug_vtags_"+ i);
                if(sugVlans != null){
                    swcapInfo.setSuggestedVLANRange(sugVlans.trim());
                }
                swcap.setSwitchingCapabilitySpecificInfo(swcapInfo);
                link.setSwitchingCapabilityDescriptors(swcap);
                hop.setLink(link);
            }else{
                hop.setLinkIdRef(hopId);
            }
            path.addHop(hop);
            i++;
            propName = "resv.ero_" + i;
            hopId = this.props.getProperty(propName);
        }
        if (hasEro) {
            pathInfo.setPath(path);
            pathInfo.setPathType(this.props.getProperty("resv.pathType"));
        }
        content.setPathInfo(pathInfo);

        return content;
    }

    /**
     * Sets the time of the reservation request. Borrowed this from
     * createReservationClient.
     *
     * @param content the request for which to set the times
     * @param start_time the start of the reservation
     * @param end_time the end of the reservation
     * @param duration the duration of the reservation
     */
    public void setTimes(ResCreateContent content, String start_time,
                            String end_time, String duration) {
        // all times are communicated to the server in UTC
        Long startTime = null;
        Long endTime = null;

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (start_time == null || start_time.trim().equals("")) {
            startTime = System.currentTimeMillis()/1000;
        } else {
            try {
                startTime = df.parse(start_time.trim()).getTime()/1000;
            } catch (java.text.ParseException ex) {
                System.out.println("Error parsing start date: "+ex.getMessage());
                System.exit(1);
            }
        }

        if (duration != null && !duration.trim().equals("")) {
            double dseconds = Double.valueOf(duration.trim()) * 3600.0;
            long seconds = (long)dseconds;
            endTime = startTime + seconds;

        } else if (end_time == null || end_time.trim().equals("")) {
            endTime = startTime + 600;
        } else {
            try {
                endTime = df.parse(end_time.trim()).getTime()/1000;
            } catch (java.text.ParseException ex) {
                System.out.println("Error parsing end date: "+ex.getMessage());
                System.exit(1);
            }
        }

        content.setStartTime(startTime);
        content.setEndTime(endTime);
        // format for printing (have to convert back to milliseconds)
        Date date = new Date(startTime*1000);
        date = new Date(endTime*1000);
    }

    /**
     * Converts a CreateReply to a string that can be printed in the logs
     *
     * @param response the CreateReply to be converted
     */
    private String responseToString(CreateReply response){
        PathInfo pathInfo = response.getPathInfo();
        String output = "GRI: " + response.getGlobalReservationId() + "\n";
        output += "Token: " + response.getToken() + "\n";
        output += "Status: " + response.getStatus() + "\n";

        if(pathInfo != null){
            Layer2Info l2Info = pathInfo.getLayer2Info();
            MplsInfo mplsInfo = pathInfo.getMplsInfo();
            Layer3Info l3Info = pathInfo.getLayer3Info();
            CtrlPlanePathContent path = pathInfo.getPath();
            CtrlPlaneHopContent[] hops = (path == null ? null : path.getHop());

            output += "Path Setup Mode: " + pathInfo.getPathSetupMode() + "\n";
            output += "Path Type: " + pathInfo.getPathType() + "\n";
            if(l2Info != null){
                VlanTag srcVtag = l2Info.getSrcVtag();
                VlanTag destVtag = l2Info.getDestVtag();
                output += "L2 Source: " + l2Info.getSrcEndpoint() + "\n";
                output += "L2 Dest: " + l2Info.getDestEndpoint() + "\n";
                if(srcVtag != null){
                    output += "L2 Source VLAN Tag: " + srcVtag.getString() +
                        "(tagged=" + srcVtag.getTagged() + ")\n";
                }
                if(destVtag != null){
                    output += "L2 Dest VLAN Tag: " + destVtag.getString() +
                        "(tagged=" + destVtag.getTagged() + ")\n";
                }
            }

            if(l3Info != null){
                output += "L3 Source: " + l3Info.getSrcHost() + "\n";
                output += "L3 Dest: " + l3Info.getDestHost() + "\n";
                output += "Protocol: " + l3Info.getProtocol() + "\n";
                output += "Src L4 Port: " + l3Info.getSrcIpPort() + "\n";
                output += "Dest L4 Port: " + l3Info.getDestIpPort() + "\n";
                output += "DSCP: " + l3Info.getDscp() + "\n";
            }

            if(mplsInfo != null){
                output += "Burst Limit: " + mplsInfo.getBurstLimit() + "\n";
                output += "LSP Class: " + mplsInfo.getLspClass() + "\n";
            }

            if(hops != null){
                output += "Path:\n";
                for(CtrlPlaneHopContent hop : hops){
                    if( hop.getLinkIdRef() != null){
                        output += "    " + hop.getLinkIdRef() + " (REF)\n";
                    }else if( hop.getPortIdRef() != null){
                        output += "    " + hop.getPortIdRef() + " (REF)\n";
                    }else if( hop.getNodeIdRef() != null){
                        output += "    " + hop.getNodeIdRef() + " (REF)\n";
                    }else if( hop.getDomainIdRef() != null){
                        output += "    " + hop.getDomainIdRef() + " (REF)\n";
                    }else if( hop.getLink() != null){
                        String id = hop.getLink().getId();
                        CtrlPlaneSwcapContent swcap = hop.getLink().getSwitchingCapabilityDescriptors();
                        CtrlPlaneSwitchingCapabilitySpecificInfo swcapInfo = swcap.getSwitchingCapabilitySpecificInfo();
                        output += "    " + id;
                        output += ", " + swcap.getEncodingType();
                        if("ethernet".equals(swcap.getEncodingType())){
                            output += ", " + swcapInfo.getVlanRangeAvailability();
                        }
                        output += "\n";
                    }else if( hop.getPort() != null){
                        output += "    " + hop.getPort().getId() + "\n";
                    }else if( hop.getNode() != null){
                        output += "    " + hop.getNode().getId() + "\n";
                    }else if( hop.getDomain() != null){
                        output += "    " + hop.getDomain().getId() + "\n";
                    }else{
                        output += "    EMPTY HOP!\n";
                    }
                }
            }
        }

        return output;
    }

    /**
     *  @param args
     *      -pf required. the path to the properties file
     *      -start optional. the first test to conduct (i.e. resv5 => 5)
     *      -end optional. the last test to conduct (i.e. resv5 => 5)
     *      -alt optional. use the alternate url
     *
     */
    public static void main(String[] args){
        OSCARSRandExerciser exerciser = null;
        String pf = null;
        boolean alt = false;
        int state = 0;
        int num = 0;

        /* Read command-line arguments */
        for(String arg:args){
            if (state == 1) {
                pf = arg;
                state = 0;
            } else if (state == 2) {
                try{
                    num = Integer.parseInt(arg);
                }catch(Exception e){
                    System.err.println("The -num parameter must be a number");
                    System.exit(1);
                }
                state = 0;
            } else if (arg.equals("-pf")) {
                state = 1;
            } else if (arg.equals("-num")) {
                state = 2;
            } else if (arg.equals("-alt")) {
                alt = true;
            }
        }

        /* Check properties file and args set correctly */
        if(pf == null){
            System.err.println("-pf option must be provided");
            System.exit(1);
        }
        if(state != 0){
            System.err.println("Invalid parameter");
            System.exit(1);
        }

        /* Exercise */
        System.out.println("NOTE: You may suppress log ouput by modifying " +
                                "conf/log4j.properties");
        exerciser = new OSCARSRandExerciser(pf, alt, num);
        exerciser.exercise();
    }
}











































































































































