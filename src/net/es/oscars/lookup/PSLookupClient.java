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
import net.es.oscars.bss.events.WSObserver;


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

    //the default number of gLS URLs to pull from the hints file
    final private static int DEFAULT_GLS_HINTS_COUNT = 2;
    /** Constructor */
    public PSLookupClient() throws LookupException {
        this.log = Logger.getLogger(this.getClass());
        PropHandler propHandler = new PropHandler("oscars.properties");
        this.props = propHandler.getPropertyGroup("lookup", true);
        String[] gLSs = null;
        String[] hLSs = null;

        ArrayList<String> gLSList = new ArrayList<String>();
        ArrayList<String> hLSList = new ArrayList<String>();
        PSLookupClient.configLS(this.props, gLSList, hLSList);
        if(!gLSList.isEmpty()){
            gLSs = gLSList.toArray(new String[gLSList.size()]);
        }
        if(!hLSList.isEmpty()){
            hLSs = hLSList.toArray(new String[hLSList.size()]);
        }

        try {
            if(gLSs != null || hLSs != null){
                this.client = new DCNLookupClient(gLSs, hLSs);
            } else{
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

    public static void configLS(Properties lsProps, List<String> gLSList, List<String> hLSList) throws LookupException{
        String hints = lsProps.getProperty("hints");
        String allHints = lsProps.getProperty("hints.all");

        //Get gLS list from hints file
        if(hints != null){
            String[] hintUrls;
            try {
                hintUrls = edu.internet2.perfsonar.PSLookupClient.getGlobalHints(hints, true);
            } catch (Exception e) {
                throw new LookupException(e.getMessage());
            }
            //only add a few GLS since looking at all of them is 
            //really slow when someone makes a typo. They should
            //all have the same info so should work. 
            int glsCount = DEFAULT_GLS_HINTS_COUNT;
            if("1".equals(allHints)){
                glsCount = hintUrls.length;
            }
            for(int j=0; j < glsCount && j < hintUrls.length; j++){
                if(!gLSList.contains(hintUrls[j])){
                    gLSList.add(hintUrls[j]);
                }
            }
        }

        //Get manual gLS list
        int i = 1;
        while(lsProps.getProperty("global." + i) != null){
            String prop = lsProps.getProperty("global." + i);
            if(!gLSList.contains(prop)){
                gLSList.add(prop);
            }
            i++;
        }

        //Get manual hLS list
        i = 1;
        while(lsProps.getProperty("home." + i) != null){
            String prop= lsProps.getProperty("home." + i);
            if(!hLSList.contains(prop)){
                hLSList.add(prop);
            }
            i++;
        }
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
    public HashMap<String, String> registerIDC(String url, String domainId, String nbURL, HashMap<String, String> serviceKeys, HashMap<String, String> nodeKeys) throws LookupException {
        if(this.client == null){
            this.log.error("Cannot use perfSONAR lookup client " +
                    "because missing required properties. Please set " +
            "lookup.hints, lookup.global.1 or lookup.home.1 in oscars.properties");
            throw new LookupException("Cannot register IDC"+ 
            " because lookup client not intialized");
        }
        HashMap<String, String> keys = this.saveKeys(serviceKeys, nodeKeys);
        try{
            URL urlObj = new URL(url);
            String hostname = urlObj.getHost();
            String[] ids = new String[1];
            //Only register new node if doesn't exist or if exists and you have a lsKey
            Element node = this.client.lookupNode(hostname);
            if(node == null || (node != null && (!nodeKeys.isEmpty()))){
                nodeKeys = this.registerNode(hostname, nodeKeys, ids, true);
                keys = this.saveKeys(serviceKeys, nodeKeys);
            }else{
                this.registerNode(hostname, nodeKeys, ids, false);
            }

            //Only register IDC if doesn't exist or exists and have a key
            String[] existingIDCUrls = null;
            try{
                existingIDCUrls = this.client.lookupIDCUrl(domainId);
            }catch(Exception e){}
            if(serviceKeys.isEmpty() && this.urlMatches(url, existingIDCUrls)){
                return keys;
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
                    reg.setPublisherRel(subscribers);
                }
                HashMap<String,String[]> topicMap = new HashMap<String,String[]>();
                int tc = topics.keySet().size();
                String[] topicList = topics.keySet().toArray(new String[tc]);
                topicMap.put(DCNLookupClient.PARAM_TOPIC, topicList);
                reg.setOptionalParameters(topicMap);
            }
            serviceKeys = this.client.registerService(reg, serviceKeys);
            keys =  this.saveKeys(serviceKeys, nodeKeys);
        }catch(Exception e){
            e.printStackTrace();
            this.log.error(e.getMessage());
        }

        return keys;
    }

    /**
     * Checks if URL is in string list
     * 
     * @param url the URL to match
     * @param existingIDCUrls the set of possible matches
     * @return true if there is a match
     */
    private boolean urlMatches(String url, String[] existingIDCUrls) {
        if(existingIDCUrls == null){
            return false;
        }
        for(String existingIDCUrl : existingIDCUrls){
            if(existingIDCUrl.equals(url)){
                return true;
            }
        }
        return false;
    }

    /**
     * Registers the local IDC with the lookup service
     * 
     * @param nbURL the URL of the service to register
     * @param nodeKeys 
     * @param serviceKeys 
     */
    public HashMap<String, String> registerNB(String nbURL, List<String> pubs, HashMap<String, String> serviceKeys, HashMap<String, String> nodeKeys) throws LookupException {
        if(this.client == null){
            this.log.error("Cannot use perfSONAR lookup client " +
                    "because missing required properties. Please set " +
            "lookup.hints, lookup.global.1 or lookup.home.1 in oscars.properties");
            throw new LookupException("Cannot register IDC"+ 
            " because lookup client not intialized");
        }
        HashMap<String, String> keys = this.saveKeys(serviceKeys, nodeKeys);
        try{
            URL urlObj = new URL(nbURL);
            String hostname = urlObj.getHost();
            String[] ids = new String[1];
            //Only register new node if doesn't exist or if exists and you have a lsKey
            Element node = this.client.lookupNode(hostname);
            if(node == null || (node != null && (!nodeKeys.isEmpty()))){
                nodeKeys = this.registerNode(hostname, nodeKeys, ids, true);
                keys = this.saveKeys(serviceKeys, nodeKeys);
            }else{
                this.registerNode(hostname, nodeKeys, ids, false);
            }

            //Only register IDC if doesn't exist or exists and have a key
            Element existingNB = this.client.lookupNB(nbURL);
            if(existingNB != null && serviceKeys.isEmpty()){
                return keys;
            }

            String serviceName = props.getProperty("reg.nb.name");
            if(serviceName == null){
                serviceName = nbURL;
            }

            String serviceDescr = props.getProperty("reg.nb.description");
            if(serviceDescr == null){
                serviceDescr = nbURL + "'s NB";
            }

            ServiceRegistration reg = new ServiceRegistration(serviceName, ServiceRegistration.NB_TYPE);
            reg.setDescription(serviceDescr);
            reg.setNode(ids[0]);

            if(pubs != null && (!pubs.isEmpty())){
                reg.setSubscriberRel(pubs.toArray(new String[pubs.size()]));
            }

            String[] urls = {nbURL};
            this.loadWSNMessages(reg, urls);
            this.loadWSNBMessages(reg, urls);
            serviceKeys = this.client.registerService(reg, serviceKeys);
            keys = this.saveKeys(serviceKeys, nodeKeys);
        }catch(Exception e){
            e.printStackTrace();
            this.log.error(e.getMessage());
        }
        return keys;
    }

    private HashMap<String,String> saveKeys(HashMap<String,String> serviceKeys, HashMap<String,String> nodeKeys){
        HashMap<String,String> keys = new HashMap<String,String>();
        for(String url : serviceKeys.keySet()){
            String key = serviceKeys.get(url);
            if(nodeKeys.containsKey(url)){
                key += ";" + nodeKeys.get(url);
            }
            keys.put(url, key);
        }
        return keys;
    }

    private HashMap<String,String> registerNode(String hostname, HashMap<String, String> nodeKeys, String[] ids, boolean submit) throws UnknownHostException, LookupException, PSException{
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
        if(!submit){ return null; }
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
        HashMap<String,String> locationInfo = new HashMap<String,String>();
        for(String locField : NodeRegistration.LOCATION_FIELDS){
            String locProp = this.props.getProperty("reg.location." + locField);
            if(locProp != null){
                locationInfo.put(locField, locProp);
            }
        }
        if(!locationInfo.isEmpty()){
            nodeReg.setLocation(locationInfo);
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

    private void loadWSNMessages(ServiceRegistration reg, String[] urls){
        HashMap<String, String[]> wsnMessages = new HashMap<String,String[]>();

        String[] wsnMsgs = {DCNLookupClient.PROTO_WSN + "#Notify", 
                DCNLookupClient.PROTO_WSN + "#Subscribe",
                DCNLookupClient.PROTO_WSN + "#Renew", 
                DCNLookupClient.PROTO_WSN + "#Unsubscribe", 
                DCNLookupClient.PROTO_WSN + "#PauseSubscription", 
                DCNLookupClient.PROTO_WSN + "#ResumeSubscription"};
        wsnMessages.put(DCNLookupClient.PARAM_SUPPORTED_MSG, wsnMsgs);
        reg.setPort(urls, DCNLookupClient.PROTO_WSN, wsnMessages);
    }

    private void loadWSNBMessages(ServiceRegistration reg, String[] urls){
        HashMap<String, String[]> wsnbMessages = new HashMap<String,String[]>();
        String[] wsnMsgs = {DCNLookupClient.PROTO_WSNB + "#RegisterPublisher", 
                DCNLookupClient.PROTO_WSNB + "#DestroyRegistration"};
        wsnbMessages.put(DCNLookupClient.PARAM_SUPPORTED_MSG, wsnMsgs);
        reg.setPort(urls, DCNLookupClient.PROTO_WSNB, wsnbMessages);
    }

    /**
     * @return the client
     */
    public DCNLookupClient getClient() {
        return client;
    }

    /**
     * @param client the client to set
     */
    public void setClient(DCNLookupClient client) {
        this.client = client;
    }
}

