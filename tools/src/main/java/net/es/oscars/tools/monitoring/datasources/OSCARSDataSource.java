package net.es.oscars.tools.monitoring.datasources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;

import net.es.oscars.api.soap.gen.v06.ListReply;
import net.es.oscars.api.soap.gen.v06.ListRequest;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.client.OSCARSClient;
import net.es.oscars.client.OSCARSClientConfig;
import net.es.oscars.client.OSCARSClientException;
import net.es.oscars.common.soap.gen.OSCARSFaultMessage;
import net.es.oscars.utils.topology.NMWGParserUtil;
import net.es.oscars.utils.topology.PathTools;

public class OSCARSDataSource implements DataSource{
    
    private String oscarsUrl;
    private String keystore;
    private String keystoreUser;
    private String keystorePassword;
    
    final public String PROP_OSCARS_URL = "oscarsUrl";
    final public String PROP_KEYSTORE = "keystore";
    final public String PROP_KEYSTORE_USER = "keystoreUser";
    final public String PROP_KEYSTORE_PASSWORD = "keystorePassword";
    
    public void init(Map<String, Object> config) {
        //verify props set
        this.verifyProp(config, PROP_OSCARS_URL);
        this.verifyProp(config, PROP_KEYSTORE);
        this.verifyProp(config, PROP_KEYSTORE_USER);
        this.verifyProp(config, PROP_KEYSTORE_PASSWORD);
        
        //set properties
        this.oscarsUrl = (String)config.get(PROP_OSCARS_URL);
        this.keystore = (String)config.get(PROP_KEYSTORE);
        this.keystoreUser = (String)config.get(PROP_KEYSTORE_USER);
        this.keystorePassword = (String)config.get(PROP_KEYSTORE_PASSWORD);
        
    }

    private void verifyProp(Map<String, Object> config, String propName) {
        if(!config.containsKey(propName) || config.get(propName) == null){
            throw new RuntimeException("Missing required OSCARS data source property " + propName);
        }
    }

    public void retrieve(Map<String, Object> data) {
        try {
            //setup keystores
            OSCARSClientConfig.setClientKeystore(this.keystoreUser, this.keystore, this.keystorePassword);
            OSCARSClientConfig.setSSLKeyStore(this.keystore, this.keystorePassword);
            
            //build client
            OSCARSClient client = new OSCARSClient(this.oscarsUrl);
            
            //Build request that asks for all ACTIVE and RESERVED reservations
            ListRequest request = new ListRequest();
            request.getResStatus().add(OSCARSClient.STATUS_ACTIVE);
            request.getResStatus().add(OSCARSClient.STATUS_RESERVED);
            
            //send request
            ListReply reply = client.listReservations(request);
            
            List<HashMap<String,Object>> resList = new ArrayList<HashMap<String,Object>>();
            //add to data
            for(ResDetails resDetails : reply.getResDetails()){
                HashMap<String,Object> resObj = new HashMap<String,Object>();
                
                //get basic parameters
                String gri = resDetails.getGlobalReservationId();
                resObj.put("id", this.generate_circuit_id(gri));
                resObj.put("name", gri);
                resObj.put("description", resDetails.getDescription());
                if(resDetails.getReservedConstraint() == null){
                    continue;
                }
                resObj.put("capacity", resDetails.getReservedConstraint().getBandwidth() * 1000000);
                resObj.put("start", resDetails.getReservedConstraint().getStartTime());
                resObj.put("end", resDetails.getReservedConstraint().getEndTime());
                
                if(resDetails.getReservedConstraint().getPathInfo() == null){
                    continue;
                }
                if(resDetails.getReservedConstraint().getPathInfo().getPath() == null){
                    continue;
                }
                if(resDetails.getReservedConstraint().getPathInfo().getPath().getHop() == null){
                    continue;
                }
                
                //build path
                ArrayList<String> forwardPath = new ArrayList<String>();
                ArrayList<String> reversePath = new ArrayList<String>();
                for(CtrlPlaneHopContent hop : resDetails.getReservedConstraint().getPathInfo().getPath().getHop()){
                    try{
                        String portId = NMWGParserUtil.getURN(hop, NMWGParserUtil.PORT_TYPE);
                        if(PathTools.isVlanHop(hop)){
                            String vlan = hop.getLink().getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getVlanRangeAvailability();
                            portId += "." + vlan;
                        }
                        forwardPath.add(portId);
                        reversePath.add(0, portId);
                    }catch(Exception e){ }
                }
                
                
                //build segment ids
                ArrayList<String> segmentIds = new ArrayList<String>();
                String forwardSegmentId = this.generate_segment_id(gri, false);
                String reverseSegmentId = this.generate_segment_id(gri, true);
                segmentIds.add(forwardSegmentId);
                segmentIds.add(reverseSegmentId);
                resObj.put("segment_ids", segmentIds);
                
                //build segments
                ArrayList<HashMap<String,Object>> segments = new ArrayList<HashMap<String,Object>>();
                HashMap<String,Object> forwardSegment = new HashMap<String,Object>();
                HashMap<String,Object> reverseSegment = new HashMap<String,Object>();
                forwardSegment.put("id", forwardSegmentId);
                forwardSegment.put("ports", forwardPath);
                reverseSegment.put("id", reverseSegmentId);
                reverseSegment.put("ports", reversePath);
                segments.add(forwardSegment);
                segments.add(reverseSegment);
                resObj.put("segments", segments);
                
                //add reservation to list
                resList.add(resObj);
            }
            data.put("circuits", resList);
        } catch (OSCARSClientException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (OSCARSFaultMessage e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private String generate_circuit_id(String gri){
        String idFromat = "urn:glif:%s:circuit_%s-%s";
        
        String domain = gri.substring(0, gri.lastIndexOf('-'));
        String idNum = gri.substring(gri.lastIndexOf('-')+1);
        
        return String.format(idFromat, domain, domain, idNum);
        
    }
    
    private String generate_segment_id(String gri, boolean reverse){
        String idFromat = "urn:glif:%s:circuit_%s-%s_%s";
        
        String domain = gri.substring(0, gri.lastIndexOf('-'));
        String idNum = gri.substring(gri.lastIndexOf('-')+1);
        
        String direction = "atoz";
        if(reverse){
            direction = "ztoa";
        }
        return String.format(idFromat, domain, domain, idNum, direction);
        
    }
}
