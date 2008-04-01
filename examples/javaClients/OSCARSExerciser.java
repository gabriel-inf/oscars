import net.es.oscars.client.Client;
import net.es.oscars.wsdlTypes.*;
import org.ogf.schema.network.topology.ctrlplane._20070626.*;
import net.es.oscars.PropHandler;
import java.util.*;
import java.io.*;
import org.apache.axis2.AxisFault;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import net.es.oscars.oscars.AAAFaultMessage;
import net.es.oscars.oscars.BSSFaultMessage;

/**
 * OSCARSExerciser is a class that issues a series of requests to OSCARS based
 * on parameters set in a properties file. It is capable of issuing the 
 * following types of requests: createReservation, cancelReservation, 
 * createPath and teardownPath.
 **/
public class OSCARSExerciser extends Client{
    private Properties props;
    private String url;
    private int start;
    private int end;
    
    /**
     * Initializes the exerciser client
     * 
     * @param propFileName the path to the properties file with test requests
     * @param altURL 
     *      boolean indicating whether the alternate URL in the properties file
     *      should be used
     * @param start the number of the first test to conduct (i.e. resv5 => 5)
     * @param end the number of the last test to conduct (i.e. resv5 => 5)
     */
    public OSCARSExerciser(String propFileName, boolean altURL, int start, int end){
        super();
        this.props = new Properties();
        try{
            FileInputStream in = new FileInputStream(propFileName);
            props.load(in);
            in.close();
        }catch (IOException e){
            System.err.println(" Properties file not found: " + propFileName);
            System.exit(1);
        }
        
        /* Set the url */
        if(altURL){
           this.url = this.props.getProperty("url2"); 
        }else{
           this.url = this.props.getProperty("url1"); 
        }
        
        /* Verify URL set */
        if(url == null){
            System.err.println("Please specify a URL in the properties file");
            System.exit(1);
        }
        
        /* Setup client */
        try{
            System.out.println("Testing IDC at "+ url);
            this.setUp(true, url, "repo");
        }catch (AxisFault e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
        
        this.start = start;
        this.end = end;
    }
    
    /**
     *  Conducts tests specified in the properties file given
     */
    public void exercise(){
        this.log.info("exercise.start");
        for(int i = this.start; i <= this.end; i++){
            String resv = "resv" + i;
            String layer = this.props.getProperty(resv + ".layer");
            if(layer == null){
                break;
            }
            
            //send create reservation
            this.log.info(resv + ".start");
            System.out.print("Test " + i + ": createReservation......");
            try{
                this.log.info(resv + ".createReservation.start");
                ResCreateContent content = this.propertiesToCreateResv(resv, layer);
                CreateReply response = this.createReservation(content);
                this.log.info(resv + ".response\n" +
                    this.responseToString(response));
                System.out.println(response.getStatus());
                this.log.info(resv + ".createReservation.end");
                
                //wait for setup
                String wait = this.props.getProperty(resv + ".waitForSetup");
                if(wait != null && wait.equals("1")){
                    this.log.info("waiting for path setup");
                    this.pollResv(response, content.getStartTime(),"PENDING");
                }
                
                //find next task
                ArrayList<String[]> tasks = this.scheduleResvTaks(resv);
                
                //do tasks
                this.performResvTasks(response.getGlobalReservationId(), tasks);
                this.log.info(resv + ".end");
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
    
    /**
     * Determines the order that requests such as cancel, createPath, and 
     * teardownPath will be issued
     *
     * @param resv the name of the test for which to schedule tasks
     * @returns a sorted list of tasks indicating the task type and time
     */
    private ArrayList<String[]> scheduleResvTaks(String resv){
        ArrayList<String[]> tasks = new ArrayList<String[]>();
        String[] propNames = {"cancelTime", "createPathTime", "teardownPathTime"};
        for(String propName : propNames){
            String propVal = this.props.getProperty(resv + "." + propName);
            
            if(propVal != null){  
                String[] entry = new String[2];
                entry[0] = propName;
                entry[1] =propVal;
                if(tasks.isEmpty()){  
                    tasks.add(entry);
                    continue;
                }
                
                long newLong = Long.parseLong(entry[1]);
                boolean added = false;
                for(int i = 0; i < tasks.size(); i++){
                    String[] curr = tasks.get(i);
                    long currLong = Long.parseLong(curr[1]);
                    
                    if(newLong < currLong){
                        tasks.add(i, entry);
                        added = true;
                        break;
                    }
                }
                
                if(!added){
                    tasks.add(entry);
                }
            }
        }
        
        return tasks;
    }   
    
    /**
     * Performs the tasks in a given task list
     *
     * @param gri the GRI of the reservation being tested
     * @param tasks the list oftasks to conduct
     * @throws Exception
     */
    private void performResvTasks(String gri, ArrayList<String[]> tasks) 
            throws Exception{
        long totalTime = 0;
        for(String[] task : tasks){
            String type = task[0];
            long time = Long.parseLong(task[1])*1000;
            long sleepTime = time - totalTime;
            totalTime = time;
            System.out.print("Sleeping for " + sleepTime/1000 + 
                    " seconds before next operation...");
            Thread.sleep(sleepTime);
            System.out.println("AWAKE");
            
            if(type.equals("cancelTime")){
                this.log.info(gri + ".cancel.start");
                System.out.print("Cancelling " + gri + "......");
                GlobalReservationId cancelReq = new GlobalReservationId();
                cancelReq.setGri(gri);
                String response = this.cancelReservation(cancelReq);
                System.out.println(response);
                 this.log.info(gri + ".cancel.status." + response);
                this.log.info(gri + ".cancel.end");
            }else if(type.equals("createPathTime")){
                this.log.info(gri + ".createPath.start");
                System.out.print("Sending createPath......");
                CreatePathContent createRequest = new CreatePathContent();
                createRequest.setGlobalReservationId(gri);
                CreatePathResponseContent createResponse = this.createPath(createRequest);
                System.out.println(createResponse.getStatus());
                this.log.info(gri + ".createPath.status." + createResponse.getStatus());
                this.log.info(gri + ".createPath.end");
            }else if(type.equals("teardownPathTime")){
                this.log.info(gri + ".teardownPath.start");
                System.out.print("Sending teardownPath......");
                TeardownPathContent teardownRequest = new TeardownPathContent();
                teardownRequest.setGlobalReservationId(gri);
                TeardownPathResponseContent teardownResponse = this.teardownPath(teardownRequest);
                System.out.println(teardownResponse.getStatus());
                 this.log.info(gri + ".teardownPath.status." + teardownResponse.getStatus());
                this.log.info(gri + ".teardownPath.end");
            }else{
                System.out.println("Skipping unrecognized operation.");
            }
            
        }
    }
    
    /**
     * Checks whether a reservation has changed from given status
     *
     * @param response the reply from a createReservation request
     * @param startTime the start time of the reservation
     * @param waitStatus the status that needs to change
     * @throws Exception
     */
    private void pollResv(CreateReply response, Long startTime, 
                            String waitStatus) throws Exception{
        String pathSetupMode = response.getPathInfo().getPathSetupMode();
        long sleepTime = startTime.longValue()*1000 - System.currentTimeMillis();
        GlobalReservationId queryRequest = new GlobalReservationId();
        String gri = response.getGlobalReservationId();
        
        queryRequest.setGri(gri);
        
        if(pathSetupMode != null && (!pathSetupMode.equals("timer-automatic"))){
            return;
        }
        System.out.print("Sleeping until start time......");
        if(sleepTime > 0){
            Thread.sleep(sleepTime + 10000);
        }
        
        
        for(int i = 0; i < 10; i++){
            System.out.println("AWAKE");
            this.log.info(gri + ".query.start");
            System.out.print("Checking reservation status......");
            ResDetails details = this.queryReservation(queryRequest);
            String status = details.getStatus();
            this.log.info(gri + ".query.status." + status);
            System.out.println(status);
            this.log.info(gri + ".query.end");
            if(!status.equals(waitStatus)){
                return;
            }
            
            System.out.print("Sleeping for 10 more seconds......");
            Thread.sleep(10000);
        }
        
        throw new Exception("Circuit not automatically setup by scheduler");
    }
    
    /**
     * Converts a properties file to a createReservation request
     *
     * @param resv the label of the test to conduct
     * @param layer the layer element of the test
     * @return a ResCreateContent element with the parameters of the test
     */
    private ResCreateContent propertiesToCreateResv(String resv, String layer){
        ResCreateContent content = new ResCreateContent();
        Layer2Info layer2Info = null;
        Layer3Info layer3Info = null;
        MplsInfo mplsInfo = null;
        String param = null;
        
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
        content.setDescription(this.props.getProperty(resv + ".description",""));
        String start_time = this.props.getProperty(resv + ".start_time");
        String end_time = this.props.getProperty(resv + ".end_time");
        String duration = this.props.getProperty(resv + ".duration");
        this.setTimes(content, start_time, end_time, duration);

        if (layer.equals("2")) {
            param = this.props.getProperty(resv + ".sourceEndpoint").trim();
            if (param == null) {
                System.err.println("sourceEndpoint property is null; exiting");
                System.exit(0);
            }
            layer2Info.setSrcEndpoint(param);
            param = this.props.getProperty(resv + ".destEndpoint").trim();
            if (param == null) {
                System.err.println("destEndpoint property is null; exiting");
                System.exit(0);
            }
            layer2Info.setDestEndpoint(param);
            VlanTag srcVtag = new VlanTag();
            param = this.props.getProperty(resv + ".vtag").trim();
            if (param == null) {
                System.err.println("vtag property is null; exiting");
                System.exit(0);
            }
            srcVtag.setString(param);
            param = this.props.getProperty(resv + ".tagSrc");
            if(param != null){
                srcVtag.setTagged(param.equals("1"));
            }else{
                srcVtag.setTagged(true);
            }
            layer2Info.setSrcVtag(srcVtag);
            VlanTag destVtag = new VlanTag();
            param = this.props.getProperty(resv + ".vtag").trim();
            // same as srcVtag for now
            destVtag.setString(param);
            param = this.props.getProperty(resv + ".tagDest");
            if(param != null){
                destVtag.setTagged(param.equals("1"));
            }else{
                destVtag.setTagged(true);
            }
            layer2Info.setDestVtag(destVtag);
        } else {
            param = this.props.getProperty(resv + ".sourceHostName").trim();
            if (param == null) {
                System.err.println("sourceHostName property is null; exiting");
                System.exit(0);
            }
            layer3Info.setSrcHost(param);
            param = this.props.getProperty(resv + ".destHostName").trim();
            if (param == null) {
                System.err.println("destHostName property is null; exiting");
                System.exit(0);
            }
            layer3Info.setDestHost(param);
            // Axis2 bug workaround
            layer3Info.setSrcIpPort(0);
            layer3Info.setDestIpPort(0);
        }
        param = this.props.getProperty(resv + ".burstLimit","10000000");
        if(param != null){
            mplsInfo.setBurstLimit(Integer.parseInt(param));
            pathInfo.setMplsInfo(mplsInfo);
        }
        content.setBandwidth(
                Integer.parseInt(this.props.getProperty(resv + ".bandwidth","10")));
        pathInfo.setPathSetupMode(this.props.getProperty(resv + ".pathSetupMode", "timer-automatic"));
        if (layer.equals("2")) {
            pathInfo.setLayer2Info(layer2Info);
        } else {
            pathInfo.setLayer3Info(layer3Info);
        }
        CtrlPlanePathContent path = new CtrlPlanePathContent();
        path.setId("userPath");//id doesn't matter in this context
        boolean hasEro = false;
        int i= 0;
        String propName = "ero_"+Integer.toString(i);
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
            }else{
                hop.setLinkIdRef(hopId);
            }
            path.addHop(hop);
            i++;
            propName = "ero_"+Integer.toString(i);
            hopId = this.props.getProperty(propName);
        }
        if (hasEro) {
            pathInfo.setPath(path);
            pathInfo.setPathType(this.props.getProperty(resv + ".pathType"));
        }
        content.setPathInfo(pathInfo);

        String gri = this.props.getProperty(resv + ".gri", null);
        if (gri != null && !gri.equals("")) {
            content.setGlobalReservationId(gri);
        }

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
                    output += "    " + hop.getLinkIdRef() + "\n";
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
        OSCARSExerciser exerciser = null;
        String pf = null;
        boolean alt = false;
        int state = 0;
        int start = 1;
        int end = 4095;
        
        /* Read command-line arguments */
        for(String arg:args){
            if(state == 1){
                pf = arg;
                state  = 0;
            }else if(state == 2){
                try{
                    start = Integer.parseInt(arg);
                }catch(Exception e){
                    System.err.println("The -start parameter must be a number");
                    System.exit(1);
                }
                state  = 0;
            }else if(state == 3){
                try{
                    end = Integer.parseInt(arg);
                }catch(Exception e){
                    System.err.println("The -end parameter must be a number");
                    System.exit(1);
                }
                state  = 0;
            }else if(arg.equals("-pf")){
                state = 1;
            }else if(arg.equals("-start")){
                state = 2;
            }else if(arg.equals("-end")){
                state = 3;
            }else if(arg.equals("-alt")){
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
        exerciser = new OSCARSExerciser(pf, alt, start, end);
        exerciser.exercise();
    }
}











































































































































