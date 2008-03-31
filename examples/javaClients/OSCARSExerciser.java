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

public class OSCARSExerciser extends Client{
    private Properties props;
    private String url;
    private int start;
    private int end;
    
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
    
    public void exercise(){
        for(int i = this.start; i <= this.end; i++){
            String resv = "resv" + i;
            String layer = this.props.getProperty(resv + ".layer");
            if(layer == null){
                break;
            }
            
            //send create reservation
            System.out.print("Test " + i + ": createReservation......");
            try{
                ResCreateContent content = this.propertiesToCreateResv(resv, layer);
                CreateReply response = this.createReservation(content);
                System.out.println(response.getStatus());
                
                //wait for setup
                String wait = this.props.getProperty(resv + ".waitForSetup");
                if(wait != null && wait.equals("1")){
                    this.pollResv(response, content.getStartTime(),"PENDING");
                }
                
                //find next task
                ArrayList<String[]> tasks = this.scheduleResvTaks(resv);
                
                //do tasks
                this.performResvTasks(response.getGlobalReservationId(), tasks);
            }catch (AAAFaultMessage e) {
                System.out.println("FAILED");
                //System.out.println(e.getFaultMessage().getMsg());
            }catch (BSSFaultMessage e) {
                System.out.println("FAILED");
                //System.out.println(e.getFaultMessage().getMsg());
            }catch (java.rmi.RemoteException e) {
                System.out.println("FAILED");
                //System.out.println(e.getMessage());
            }catch (Exception e) {
                System.out.println("FAILED");
                System.out.println(e.getMessage());
            }
        }
        
        System.out.println("Exercises complete");
    }
    
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
    
    private void performResvTasks(String gri, ArrayList<String[]> tasks) throws Exception{
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
                System.out.print("Cancelling " + gri + "......");
                GlobalReservationId cancelReq = new GlobalReservationId();
                cancelReq.setGri(gri);
                String response = this.cancelReservation(cancelReq);
                System.out.println(response);
            }else if(type.equals("createPathTime")){
                System.out.print("Sending createPath......");
                CreatePathContent createRequest = new CreatePathContent();
                createRequest.setGlobalReservationId(gri);
                CreatePathResponseContent createResponse = this.createPath(createRequest);
                System.out.println(createResponse.getStatus());
            }else if(type.equals("teardownPathTime")){
                System.out.print("Sending teardownPath......");
                TeardownPathContent teardownRequest = new TeardownPathContent();
                teardownRequest.setGlobalReservationId(gri);
                TeardownPathResponseContent teardownResponse = this.teardownPath(teardownRequest);
                System.out.println(teardownResponse.getStatus());
            }else{
                System.out.println("Skipping unrecognized operation.");
            }
            
        }
    }
    
    private void pollResv(CreateReply response, Long startTime, String waitStatus) throws Exception{
        String pathSetupMode = response.getPathInfo().getPathSetupMode();
        long sleepTime = startTime.longValue()*1000 - System.currentTimeMillis();
        GlobalReservationId queryRequest = new GlobalReservationId();
        
        queryRequest.setGri(response.getGlobalReservationId());
        
        if(pathSetupMode != null && (!pathSetupMode.equals("timer-automatic"))){
            return;
        }
        System.out.print("Sleeping until start time......");
        if(sleepTime > 0){
            Thread.sleep(sleepTime + 10000);
        }
        
        
        for(int i = 0; i < 10; i++){
            System.out.println("AWAKE");
            System.out.print("Checking reservation status......");
            ResDetails details = this.queryReservation(queryRequest);
            String status = details.getStatus();
            System.out.println(status);
            if(!status.equals(waitStatus)){
                return;
            }
            
            System.out.print("Sleeping for 10 more seconds......");
            Thread.sleep(10000);
        }
        
        throw new Exception("Circuit not automatically setup by scheduler");
    }
    
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
            srcVtag.setTagged(true);
            layer2Info.setSrcVtag(srcVtag);
            VlanTag destVtag = new VlanTag();
            // same as srcVtag for now
            destVtag.setString(param);
            destVtag.setTagged(true);
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
        mplsInfo.setBurstLimit(
                Integer.parseInt(this.props.getProperty(resv + ".burstLimit","10000000")));
        content.setBandwidth(
                Integer.parseInt(this.props.getProperty(resv + ".bandwidth","10")));
        pathInfo.setPathSetupMode(this.props.getProperty(resv + ".pathSetupMode", "timer-automatic"));
        if (layer.equals("2")) {
            pathInfo.setLayer2Info(layer2Info);
        } else {
            pathInfo.setLayer3Info(layer3Info);
        }
        pathInfo.setMplsInfo(mplsInfo);
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
    
    public void setTimes(ResCreateContent content, String start_time, String end_time, String duration) {
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
        //System.out.println("Start time: " +
       //         DateFormat.getDateTimeInstance(
        //            DateFormat.LONG, DateFormat.LONG).format(date));
        date = new Date(endTime*1000);
        //System.out.println("End time: " +
           //     DateFormat.getDateTimeInstance(
               //     DateFormat.LONG, DateFormat.LONG).format(date));
    }
    
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
        exerciser = new OSCARSExerciser(pf, alt, start, end);
        exerciser.exercise();
    }
}











































































































































