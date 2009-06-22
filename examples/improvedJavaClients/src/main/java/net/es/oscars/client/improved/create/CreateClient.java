package net.es.oscars.client.improved.create;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.es.oscars.client.Client;
import net.es.oscars.client.improved.ConfigHelper;
import net.es.oscars.client.improved.ImprovedClient;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.apache.axis2.AxisFault;

import net.es.oscars.ws.AAAFaultMessage;
import net.es.oscars.ws.BSSFaultMessage;
import net.es.oscars.wsdlTypes.*;

public class CreateClient extends ImprovedClient {

    public static final String DEFAULT_CONFIG_FILE = "create.yaml";
    private List<CreateRequestParams> resvRequests;

    

	public ResCreateContent formRequest(CreateRequestParams params) {
    	
    	ResCreateContent request = new ResCreateContent();

        if (params.getGri() != null && !params.getGri().equals("")) {
            request.setGlobalReservationId(params.getGri());
          }
         
        PathInfo pathInfo = new PathInfo();
        pathInfo.setPathSetupMode(params.getPathSetupMode());

        request.setBandwidth(params.getBandwidth());
        request.setStartTime(params.getStartTime());
        request.setEndTime(params.getEndTime());
        request.setDescription(params.getDescription());
        if (params.getLayer().equals("2")) {
            Layer2Info layer2Info = new Layer2Info();
            layer2Info.setSrcEndpoint(params.getSrc());
            layer2Info.setDestEndpoint(params.getDst());
            VlanTag srcVtag = new VlanTag();
            srcVtag.setString(params.getSrcVlan());
            srcVtag.setTagged(true);
            VlanTag dstVtag = new VlanTag();
            dstVtag.setString(params.getDstVlan());
            dstVtag.setTagged(true);
            layer2Info.setSrcVtag(srcVtag);
            layer2Info.setDestVtag(dstVtag);
            pathInfo.setLayer2Info(layer2Info);
            
        } else if (params.getLayer().equals("3")) {
            Layer3Info layer3Info = new Layer3Info();
            layer3Info.setSrcHost(params.getSrc());
            layer3Info.setDestHost(params.getDst());
            layer3Info.setSrcIpPort(0);
            layer3Info.setDestIpPort(0);
            pathInfo.setLayer3Info(layer3Info);
        } else {
            die ("layer must be 2 or 3");
        }
        if (params.getPath() != null && !params.getPath().isEmpty()) {
            CtrlPlanePathContent path = new CtrlPlanePathContent();
            pathInfo.setPath(path);
            path.setId("userPath");//id doesn't matter in this context
            int i = 0;
            for (String hopStr : params.getPath()) {
                CtrlPlaneHopContent hop = new CtrlPlaneHopContent();
                hop.setLinkIdRef(hopStr);
                hop.setId("hop_"+i++);
                path.addHop(hop);
            }
            pathInfo.setPathType("REQUESTED");
        }
        request.setPathInfo(pathInfo);

        
        return request;
    }
    

    public CreateReply performRequest(ResCreateContent createReq) {
        CreateReply response = null;
        Client oscarsClient = new Client();

        try {
            oscarsClient.setUp(true, wsdlUrl, repoDir);
            response = oscarsClient.createReservation(createReq);
        } catch (AxisFault e) {
            e.printStackTrace();
            die("Error: "+e.getMessage());
        } catch (RemoteException e) {
            e.printStackTrace();
            die("Error: "+e.getMessage());
        } catch (AAAFaultMessage e) {
            e.printStackTrace();
            die("Error: "+e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            die("Error: "+e.getMessage());
        }
        return response;
    }
    

    @SuppressWarnings("unchecked")
    public void configure() {
        if (configFile == null) {
            configFile = DEFAULT_CONFIG_FILE;
        }
        resvRequests = new ArrayList<CreateRequestParams>();

        ConfigHelper cfg = ConfigHelper.getInstance();
        config = cfg.getConfiguration(this.configFile);

        Map create = (Map) config.get("create");
        ArrayList<Map> requests = (ArrayList<Map>) create.get("requests");
        for (Map request : requests) {
            CreateRequestParams params = new CreateRequestParams();
            String gri = (String) request.get("gri");
            String layer = request.get("layer").toString();
            Integer bandwidth = (Integer) request.get("bandwidth");
            String src = (String) request.get("src");
            String dst = (String) request.get("dst");
            String description = (String) request.get("description");
            String srcVlan = (String) request.get("srcvlan");
            String dstVlan = (String) request.get("dstvlan");
            String start_time = (String) request.get("start-time");
            String end_time = (String) request.get("end-time");
            ArrayList<String> path = (ArrayList<String>) request.get("path");
            String pathSetupMode = (String) request.get("path-setup-mode");
            
            if (!layer.equals("2") && !layer.equals("3")) {
                die("Layer must be 2 or 3");
            }
            if (src == null || src.equals("")) {
                die("Source must be specified");
            }
            
            if (dst == null || dst.equals("")) {
                die("Destination must be specified");
            }
            
            if (bandwidth == null) {
                die("bandwidth must be specified");
            }
            
            if (description == null || description.equals("")) {
                die("description must be specified");
            }
            
            HashMap<String, Long> times = this.parseTimes(start_time, end_time);
            
            
            params.setGri(gri);
            params.setLayer(layer);
            params.setBandwidth(bandwidth);
            params.setSrc(src);
            params.setDst(dst);
            params.setSrcVlan(srcVlan);
            params.setDstVlan(dstVlan);
            params.setDescription(description);
            params.setPath(path);
            params.setPathSetupMode(pathSetupMode);
            params.setStartTime(times.get("start"));
            params.setEndTime(times.get("end"));
            resvRequests.add(params);
        }
    }
     
    private void die(String msg) {
        System.err.println("msg");
        System.exit(1);
    }
    
    private HashMap<String, Long> parseTimes(String start_time, String end_time) {
        HashMap<String, Long> result = new HashMap<String, Long>();
        Long startTime = 0L;
        Long endTime = 0L;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (start_time == null || start_time.equals("now") || start_time.equals("")) {
            startTime = System.currentTimeMillis()/1000;
        } else {
            try {
                startTime = df.parse(start_time.trim()).getTime()/1000;
            } catch (java.text.ParseException ex) {
                die("Error parsing start date: "+ex.getMessage());
            }
        }
        if (end_time == null || end_time.equals("")) {
            die("No end time specified.");
        } else if (end_time.startsWith("+")) {
            String[] hm = end_time.substring(1).split("\\:");
            if (hm.length != 2) {
                die("Error parsing end date format");
            } 
            try {
                Integer seconds = Integer.valueOf(hm[0])*3600;
                seconds += Integer.valueOf(hm[1])*60;
                if (seconds < 60) {
                    die("Duration must be > 60 sec");
                }
                endTime = startTime + seconds;
            } catch (NumberFormatException ex) {
                die("Error parsing end date format: "+ex.getMessage());
            }
        } else {
            try {
                endTime = df.parse(end_time.trim()).getTime()/1000;
            } catch (java.text.ParseException ex) {
                die("Error parsing emd date: "+ex.getMessage());
            }
        }
        
        
        result.put("start", startTime);
        result.put("end", endTime);
        return result;
    }

    public List<CreateRequestParams> getResvRequests() {
        return resvRequests;
    }


    public void setResvRequests(List<CreateRequestParams> resvRequests) {
        this.resvRequests = resvRequests;
    }

}
