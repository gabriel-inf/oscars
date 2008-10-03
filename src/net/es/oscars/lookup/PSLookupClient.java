package net.es.oscars.lookup;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.*;

import org.apache.log4j.*;
import org.jdom.Element;

import edu.internet2.perfsonar.NodeRegistration;
import edu.internet2.perfsonar.PSException;
import edu.internet2.perfsonar.ServiceRegistration;
import edu.internet2.perfsonar.dcn.*;
import net.es.oscars.PropHandler;
import net.es.oscars.bss.topology.Domain;
import net.es.oscars.bss.topology.DomainDAO;
import net.es.oscars.bss.topology.DomainService;
import net.es.oscars.bss.topology.DomainServiceDAO;
import net.es.oscars.notify.WSObserver;
import net.es.oscars.oscars.OSCARSCore;


/**
 * Class used to retrieve the URNs associated with a given hostname
 * via the perfSONAR Lookup Service
 *
 * @author Andrew Lake
 */
public class PSLookupClient {
    private Logger log;
    private Properties props;
    private DCNLookupClient client;
    
    /** Constructor */
    public PSLookupClient() throws LookupException {
        this.log = Logger.getLogger(this.getClass());
        PropHandler propHandler = new PropHandler("oscars.properties");
        this.props = propHandler.getPropertyGroup("lookup", true);
        String hints = this.props.getProperty("hints");
        String[] gLSs = null;
        String[] hLSs = null;
        
        int i = 1;
        ArrayList<String> gLSList = new ArrayList<String>();
        while(this.props.getProperty("global." + i) != null){
            gLSList.add(this.props.getProperty("global." + i));
            i++;
        }
        if(!gLSList.isEmpty()){
            String [] temp = new String[1];
            gLSList.toArray(temp);
            gLSs = temp;
        }

        i = 1;
        ArrayList<String> hLSList = new ArrayList<String>();
        while(this.props.getProperty("home." + i) != null){
            hLSList.add(this.props.getProperty("home." + i));
            i++;
        }
        if(!hLSList.isEmpty()){
            String [] temp = new String[1];
            hLSList.toArray(temp);
            hLSs = temp;
        }

        try {
            if(gLSs != null || hLSs != null){
                this.client = new DCNLookupClient(gLSs, hLSs);
            }else if(hints != null){
                this.client = new DCNLookupClient(hints);
            }else{
                throw new LookupException("Cannot initialize perfSONAR lookup client " +
                    "because missing required properties. Please set " +
                    "lookup.hints, lookup.global.1 or lookup.home.1 in oscars.properties");
            }
        } catch (Exception e) {
            this.client = null;
        }

        if (this.client == null) {
            throw new LookupException("Cannot initialize perfSONAR lookup client");
        }

        String useGlobals = this.props.getProperty("useGlobal");
        if(useGlobals != null){
            this.client.setUseGlobalLS(("1".equals(useGlobals) || "true".equals(useGlobals)));
        }
        this.client.setRetryOnKeyNotFound(true);
    }

    /**
     * Retrieves the URN for a given hostname from the perfSONAR
     * Lookup Service. The URL of the service is defined in oscars.properties.
     *
     * @param hostname a String containing the hostname of the URN to lookup
     * @return String of URN found. null if URN is not found by Lookup Service.
     * throws LookupException
     */
    public String lookup(String hostname) throws LookupException {
        if(this.client == null){
            this.log.error("Cannot use perfSONAR lookup client " +
                "because missing required properties. Please set " +
                "lookup.hints, lookup.global.1 or lookup.home.1 in oscars.properties");
            throw new LookupException("Cannot lookup " + hostname + 
                    " because lookup client not intialized");
        }
        String urn = null;
        try{
            urn =this.client.lookupHost(hostname);
        }catch(Exception e){
            throw new LookupException(e.getMessage());
        }
        return urn;
    }
    
    /**
     * Registers the local IDC with the lookup service
     * 
     * @param url the URL of the service to register
     * @param nodeKeys 
     * @param serviceKeys 
     */
    public void registerIDC(String url, String domainId, String nbURL, HashMap<String, String> serviceKeys, HashMap<String, String> nodeKeys) throws LookupException {
        if(this.client == null){
            this.log.error("Cannot use perfSONAR lookup client " +
                "because missing required properties. Please set " +
                "lookup.hints, lookup.global.1 or lookup.home.1 in oscars.properties");
            throw new LookupException("Cannot register IDC"+ 
                    " because lookup client not intialized");
        }
        try{
        	URL urlObj = new URL(url);
        	String hostname = urlObj.getHost();
        	String[] ids = new String[1];
        	//Only register new node if doesn't exist or if exists and you have a lsKey
        	Element node = this.client.lookupNode(hostname);
        	if(node == null || (node != null && (!nodeKeys.isEmpty()))){
        		nodeKeys = this.registerNode(hostname, nodeKeys, ids);
        		this.saveKeys(serviceKeys, nodeKeys);
        	}
        	
        	//Only register IDC if doesn't exist or exists and have a key
        	String existingIDCUrl = this.lookup(domainId);
        	if(existingIDCUrl != null && nodeKeys.isEmpty() && existingIDCUrl.equals(url)){
        		return;
        	}
        	
            String serviceName = props.getProperty("reg.idc.name");
            if(serviceName == null){
            	serviceName = url;
            }
            
            String serviceDescr = props.getProperty("reg.idc.description");
            if(serviceDescr == null){
            	serviceDescr = domainId + "'s IDC";
            }
            ServiceRegistration reg = new ServiceRegistration(serviceName, ServiceRegistration.IDC_TYPE);
            reg.setDescription(serviceDescr);
            reg.setNode(ids[0]);
            String[] domainIds = {domainId};
            reg.setControls(domainIds);
            
            String[] urls = {url};
            this.loadIDCMessages(reg, urls);
            /* Non-null topics indicate that it's publishing */
            HashMap<String,String> topics = WSObserver.getTopics();
            if(topics != null){
            	this.loadNotifyMessage(reg, urls);
            	if(nbURL != null){
                	String[] subscribers = {nbURL};
                	reg.setSubscribers(subscribers);
                }
            	HashMap<String,String[]> topicMap = new HashMap<String,String[]>();
            	int tc = topics.keySet().size();
            	String[] topicList = topics.keySet().toArray(new String[tc]);
            	topicMap.put(DCNLookupClient.PARAM_TOPIC, topicList);
            	reg.setOptionalParameters(topicMap);
            }
            serviceKeys = this.client.registerService(reg, serviceKeys);
            this.saveKeys(serviceKeys, nodeKeys);
        }catch(Exception e){
        	e.printStackTrace();
            throw new LookupException(e.getMessage());
        }
    }
    
    private void saveKeys(HashMap<String, String> serviceKeys,
			HashMap<String, String> nodeKeys) {
    	//NOTE: it won't save the node key if this is the first time saving
    	OSCARSCore core = OSCARSCore.getInstance();
    	DomainDAO domainDAO = new DomainDAO(core.getBssDbName());
    	DomainServiceDAO dsDAO = new DomainServiceDAO(core.getBssDbName());
    	Domain localDomain = domainDAO.getLocalDomain();
		for(String url : serviceKeys.keySet()){
			DomainService ds = dsDAO.queryByParam("url", url);
			if(ds == null || !(ds.getType().equals("LS") && ds.getDomain().equals(localDomain))){
				ds = new DomainService();
				ds.setDomain(domainDAO.getLocalDomain());
				ds.setType("LS");
				ds.setUrl(url);
			}
			String key = serviceKeys.get(url);
			if(nodeKeys.containsKey(url)){
				key += ";" + nodeKeys.get(url);
			}
			ds.setServiceKey(key);
			dsDAO.update(ds);
		}
		
	}

	private HashMap<String,String> registerNode(String hostname, HashMap<String, String> nodeKeys, String[] ids) throws UnknownHostException, LookupException, PSException{
    	String id = "";
    	InetAddress[] nodeIPs = InetAddress.getAllByName(hostname);
    	String name = null;
    	for(InetAddress nodeIP : nodeIPs){
    		if(name == null){
    			name = nodeIP.getCanonicalHostName();
    		}
        	String ip = nodeIP.getHostAddress();
        	if(name == null || ip == null){
        		 this.log.debug("Cannot register address"+ 
                 " because name and/or ip is null");
        		 continue;
        	}else if(ip.equals(name)){
        		name = null;
        	}else{
        		break;
        	}
    	}
    	if(name == null){
    		id = nodeIPs[0].getHostAddress();
    	}else if (name.indexOf('.') > -1){
    		int dotIndex = name.indexOf('.');
    		id += "urn:ogf:network:domain=" + name.substring(dotIndex+1);
    		id += ":node=" + name.substring(0, dotIndex);
    	}
    	ids[0] = id;
    	
    	NodeRegistration nodeReg = new NodeRegistration(id);
    	if(name != null){
    		nodeReg.setName(name, "dns");
    	}
    	for(InetAddress nodeIP : nodeIPs){
    		boolean isIPv6 = nodeIP.getClass().getName().equals("java.net.Inet6Address");
    		String ip = nodeIP.getHostAddress();
    		if(ip == null){ continue; }
    		nodeReg.setL3Address(ip, isIPv6);
    	}
    	
    	return this.client.registerNode(nodeReg, nodeKeys);
    }
    
    private void loadIDCMessages(ServiceRegistration reg, String[] urls){
    	HashMap<String, String[]> idcMessages = new HashMap<String,String[]>();
    	
    	String[] idcMsgs = {DCNLookupClient.PROTO_OSCARS + "#createReservation", 
    					    DCNLookupClient.PROTO_OSCARS + "#modifyReservation",
    					    DCNLookupClient.PROTO_OSCARS + "#cancelReservation", 
    					    DCNLookupClient.PROTO_OSCARS + "#createPath", 
    					    DCNLookupClient.PROTO_OSCARS + "#refreshPath", 
    					    DCNLookupClient.PROTO_OSCARS + "#teardownPath", 
    					    DCNLookupClient.PROTO_OSCARS + "#queryReservation", 
    					    DCNLookupClient.PROTO_OSCARS + "#listReservation",
    					    DCNLookupClient.PROTO_OSCARS + "#getNetworkTopology"};
    	idcMessages.put(DCNLookupClient.PARAM_SUPPORTED_MSG, idcMsgs);
    	reg.setPort(urls, DCNLookupClient.PROTO_OSCARS, idcMessages);
    	
    }
    
    private void loadNotifyMessage(ServiceRegistration reg, String[] urls){
    	HashMap<String, String[]> wsnMessages = new HashMap<String,String[]>();
    	String[] wsnMsgs = { DCNLookupClient.PROTO_OSCARS + "#Notify"};
    	wsnMessages.put(DCNLookupClient.PARAM_SUPPORTED_MSG, wsnMsgs);
    	reg.setPort(urls, DCNLookupClient.PROTO_WSN, wsnMessages);
    }
}

