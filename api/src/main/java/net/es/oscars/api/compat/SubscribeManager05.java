package net.es.oscars.api.compat;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;

import org.apache.cxf.frontend.ClientProxy;
import org.apache.log4j.Logger;
import org.oasis_open.docs.wsn.b_2.FilterType;
import org.oasis_open.docs.wsn.b_2.QueryExpressionType;
import org.oasis_open.docs.wsn.b_2.Renew;
import org.oasis_open.docs.wsn.b_2.RenewResponse;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.oasis_open.docs.wsn.b_2.SubscribeResponse;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.oasis_open.docs.wsn.b_2.Unsubscribe;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.lookup.soap.gen.LookupRequestContent;
import net.es.oscars.lookup.soap.gen.LookupResponseContent;
import net.es.oscars.lookup.soap.gen.Protocol;
import net.es.oscars.lookup.soap.gen.Relationship;
import net.es.oscars.utils.clients.LookupClient;
import net.es.oscars.utils.clients.NotifyClient05;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ConfigHelper;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.topology.NMWGParserUtil;

/**
 * Handle the creation and renewal of subscriptions at the 0.5 NotificationBroker
 *
 */
public class SubscribeManager05 {
    static private SubscribeManager05 instance = null;
    
    private Logger log = Logger.getLogger(SubscribeManager05.class);
    private HashMap<String, W3CEndpointReference> subscriptionIdMap;
    private HashMap<String, Long> termTimeMap;
    private LookupClient lookupClient;
    private W3CEndpointReference consumerEpr;
    private URL lookupUrl;
    private URL lookupWsdl;
    private URL notify05Wsdl;
    
    final private String PROP_LOOKUP_URL = "lookupUrl";
    final private String PROP_LOOKUP_WSDL = "lookupWsdl";
    final private String WS_TOPIC_FULL = "http://docs.oasis-open.org/wsn/t-1/TopicExpression/Full";
    final private String XPATH_URI = "http://www.w3.org/TR/1999/REC-xpath-19991116";
    final private String OSCARS5_IDC_PROTO = "http://oscars.es.net/OSCARS";
    final private double DEFAULT_TERM_TIME_WINDOW = .2;
    

    static public SubscribeManager05 getInstance() throws ConfigException{
        if(instance == null){
            instance = new SubscribeManager05();
        }
        return instance;
    }
    

    private SubscribeManager05() throws ConfigException{
        ContextConfig cc = ContextConfig.getInstance();
        String configFilename = cc.getFilePath(ConfigDefaults.CONFIG);
        Map config = ConfigHelper.getConfiguration(configFilename);
        
        //get 0.5 wsdl
        try{
            this.notify05Wsdl = new URL("file:" + cc.getFilePath("wsdl-notify-0.5"));
        }catch(Exception e){
            throw new ConfigException("Unable to load notify 0.5 wsdl");
        }
        //get local URL of API service
        this.consumerEpr = (new W3CEndpointReferenceBuilder()).address(this.getPublishTo(config)).build();

        //Create lookup client
        if(config.containsKey(PROP_LOOKUP_URL)){
            try {
                this.lookupUrl = new URL((String) config.get(PROP_LOOKUP_URL));
            } catch (MalformedURLException e) {
                throw new ConfigException(PROP_LOOKUP_URL + " property is an invalid URL");
            }
        }else{
            String lookupConfigFilename = cc.getFilePath(ServiceNames.SVC_LOOKUP,cc.getContext(),
                    ConfigDefaults.CONFIG);
            Map lookupConfig = ConfigHelper.getConfiguration(lookupConfigFilename);
            HashMap<String,Object> soap = (HashMap<String,Object>) lookupConfig.get("soap");
            if (soap == null ){
                throw new ConfigException("soap stanza not found in lookup.yaml");
            }
            try{
                this.lookupUrl = new URL (this.getPublishTo(lookupConfig));
            } catch (MalformedURLException e) {
                throw new ConfigException("lookup publishTo property is an invalid URL");
            }
        }
        
        try{
            if(config.containsKey(PROP_LOOKUP_WSDL)){
                this.lookupWsdl = new URL((String) config.get(PROP_LOOKUP_WSDL));
            }else{
                this.lookupWsdl = new URL(this.lookupUrl + "?wsdl");
            }
        }catch(MalformedURLException e){
            throw new ConfigException(PROP_LOOKUP_WSDL + " property is an invalid URL");
        }
        this.subscriptionIdMap = new HashMap<String, W3CEndpointReference>();
        this.termTimeMap = new HashMap<String, Long>();
    }
    
    /**
     * Determine if there is a subscription to the given domain's NotificationBroker. 
     * If there is then return true. If not, then try to create it. Only return false if 
     * creation fails.
     * 
     * @param domainId the ID of the domain to test for a subscription
     * @return true if a subscription exists or was created, false otherwise
     */
    synchronized public boolean hasSubscription(String domainId){
        OSCARSNetLogger netLog = OSCARSNetLogger.getTlogger();
        this.log.info(netLog.start("SubscribeManager05.getSubscription"));
        
        if(subscriptionIdMap.containsKey(NMWGParserUtil.normalizeURN(domainId))){
            return true;
        }
        
        //lookup NB
        String[] lookupResult = this.lookupDomainUrls(domainId);
        String nbUrl = lookupResult[0];
        String idcUrl = lookupResult[1];
        if(nbUrl == null){
            this.log.error(netLog.error("SubscribeManager05.getSubscription", ErrSev.MAJOR, 
                    "Unable to find notificationBroker for " + domainId));
            return false;
        }
        if(idcUrl == null){
            this.log.error(netLog.error("SubscribeManager05.getSubscription", ErrSev.MAJOR, 
                    "Unable to find IDC url for " + domainId));
            return false;
        }
        
        //subscribe
        try {
            NotifyClient05 nbClient = NotifyClient05.getClient(new URL(nbUrl), this.notify05Wsdl);
            ClientProxy.getClient(nbClient.getPortType()).getRequestContext().put("org.apache.cxf.message.Message.ENDPOINT_ADDRESS", nbUrl);

            //clear old subscriptions
            try{
                Unsubscribe unsubscribe = new Unsubscribe();
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance(); 
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document domDoc = db.newDocument();                
                Element subscrIdElem = domDoc.createElementNS("http://oscars.es.net/OSCARS", "subscriptionId");
                subscrIdElem.setTextContent("ALL");
                W3CEndpointReference subRef = (new W3CEndpointReferenceBuilder())
                            .address(nbUrl)
                            .referenceParameter(subscrIdElem)
                            .build();
                unsubscribe.setSubscriptionReference(subRef);
                nbClient.getPortType().unsubscribe(unsubscribe);
            }catch(Exception e){}
            
            //create new subscription
            Subscribe subscribeRequest = new Subscribe();
            subscribeRequest.setConsumerReference(this.consumerEpr);
            FilterType filterType = new FilterType();
            TopicExpressionType topic = new TopicExpressionType();
            topic.setDialect(WS_TOPIC_FULL);
            topic.setValue("idc:IDC");
            filterType.getTopicExpression().add(topic);
            QueryExpressionType producer = new QueryExpressionType();
            producer.setDialect(XPATH_URI);
            producer.setValue("/wsa:Address='" + idcUrl + "'");
            filterType.getProducerProperties().add(producer);
            subscribeRequest.setFilter(filterType);
            SubscribeResponse subResp = nbClient.getPortType().subscribe(subscribeRequest);
            Long termTime = this.calcNextRenewTime(subResp.getTerminationTime().toGregorianCalendar().getTimeInMillis());
            this.updateSubscriptionMaps(domainId,subResp.getSubscriptionReference(), termTime);
        } catch (MalformedURLException e) {
            this.log.error(netLog.error("SubscribeManager05.getSubscription", ErrSev.MAJOR, 
                    "NotificationBroker URL is invalid", nbUrl));
            return false;
        } catch (OSCARSServiceException e) {
            this.log.error(netLog.error("SubscribeManager05.getSubscription", ErrSev.MAJOR, 
                    "Error creating client: " + e.getMessage(), nbUrl));
            return false;
        } catch (Exception e) {
            this.log.error(netLog.error("SubscribeManager05.getSubscription", ErrSev.MAJOR, 
                    e.getMessage(), nbUrl));
            return false;
        }
        
        this.log.info(netLog.end("SubscribeManager05.getSubscription", null, nbUrl));
        return true;
    }
    
    /**
     * Called by scheduler to iterate through domains and renew subscriptions
     * set to expire.
     */
    synchronized public void renewAll(){
        OSCARSNetLogger netLog = OSCARSNetLogger.getTlogger();
        this.log.debug(netLog.start("SubscribeManager05.renewAll"));
        
        for(String domainId : this.termTimeMap.keySet()){
            long currentTime = System.currentTimeMillis();
            if(currentTime < this.termTimeMap.get(domainId)){
                continue;
            }
            
            //Time to renew...
            String[] lookupResult = this.lookupDomainUrls(domainId);
            String nbUrl = lookupResult[0];
            if(nbUrl == null){
                continue;
            }
            this.sendRenew(domainId, nbUrl);
        }
        
        this.log.debug(netLog.end("SubscribeManager05.renewAll"));
    }
    
    /**
     * Sends a renew request. If the renewal fails then it removes the subscription from 
     * the list. The subscription will need to be recreated the next time a request 
     * tries to contact the specified domain.
     * 
     * @param domainId the domain id of the subscription being renewed
     * @param nbUrl the URL of the 0.5 NotificationBroker to contact
     */
    public void sendRenew(String domainId, String nbUrl){
        OSCARSNetLogger netLog = OSCARSNetLogger.getTlogger();
        HashMap<String, String> netLogProps = new HashMap<String, String>();
        netLogProps.put("domain", domainId);
        this.log.info(netLog.start("SubscribeManager05.sendRenew"));
        try {
            NotifyClient05 nbClient = NotifyClient05.getClient(new URL(nbUrl), new URL(nbUrl+"?wsdl"));
            Renew renewRequest = new Renew();
            renewRequest.setSubscriptionReference(this.subscriptionIdMap.get(domainId));
            RenewResponse renewResp = nbClient.getPortType().renew(renewRequest);
            Long termTime = this.calcNextRenewTime(renewResp.getTerminationTime().toGregorianCalendar().getTimeInMillis());
            //set new termination time
            this.updateSubscriptionMaps(domainId, this.subscriptionIdMap.get(domainId), termTime);
        } catch (Exception e) {
            this.updateSubscriptionMaps(domainId, null, null);
            this.log.info(netLog.error("SubscribeManager05.sendRenew", ErrSev.MAJOR, 
                    e.getMessage(), nbUrl, netLogProps));
            return;
        } 
        this.log.info(netLog.end("SubscribeManager05.sendRenew", null, nbUrl, netLogProps));
    }
    
    /**
     * Updates the maps that track subscription ids and termination times. If one of the values
     * is null then they will both be deleted. 
     *  
     * @param domainId the Id of teh domain that needs updating
     * @param subRef the new subscription reference.
     * @param termTime the new expiration time
     */
    synchronized private void updateSubscriptionMaps(String domainId, W3CEndpointReference subRef, Long termTime){
        String normalizedDomainId = NMWGParserUtil.normalizeURN(domainId);
        if(subRef == null || termTime == null){
            this.subscriptionIdMap.remove(normalizedDomainId);
            this.termTimeMap.remove(normalizedDomainId);
        }else{
            this.subscriptionIdMap.put(normalizedDomainId, subRef);
            this.termTimeMap.put(normalizedDomainId, termTime);
        }
    }
    
    /**
     * Given a domainId returns the notificationBroker URL and the idc URL
     * 
     * @param domainId the domain for which to find the IDC and NotificationBroker URL
     * @return String array whose first element is the NotificationBroker URL and the 
     *          second is the IDC URL. If either is null that means that it was not found.
     */
    public String[] lookupDomainUrls(String domainId){
        OSCARSNetLogger netLog = OSCARSNetLogger.getTlogger();
        this.log.info(netLog.start("SubscribeManager05.lookupDomainUrls"));
        
       //lookup NB
        String nbUrl = null;
        String idcUrl = null;
        if(this.lookupClient == null){
            try {
                this.lookupClient = LookupClient.getClient(this.lookupUrl, this.lookupWsdl);
            } catch(Exception e) {
                this.log.error(netLog.error("SubscribeManager05.lookupDomainUrls", ErrSev.MAJOR, e.getMessage()));
                String[] result = new String[2];
                result[0] = null;
                result[1] = null;
                return result;
            }
        }

        //make sure it's a URN because this is how its stuffed in the perfSONAR LS
        String domainURN = NMWGParserUtil.TOPO_ID_PREFIX + ":domain=" + NMWGParserUtil.normalizeURN(domainId);
        LookupRequestContent lookupRequest = new LookupRequestContent();
        lookupRequest.setType("IDC");
        Relationship rel = new Relationship();
        rel.setRelatedTo(domainURN);
        rel.setType("controls");
        lookupRequest.setHasRelationship(rel);
        try {
            LookupResponseContent lookupResult = this.lookupClient.getPortType().lookup(lookupRequest);
            for(Relationship resultRel : lookupResult.getRelationship()){
                if("publisher".equals(resultRel.getType())){
                    nbUrl = resultRel.getRelatedTo();
                    break;
                }
            }
            for(Protocol proto : lookupResult.getProtocol()){
                if(OSCARS5_IDC_PROTO.equals(proto.getType())){
                    idcUrl = proto.getLocation();
                    break;
                }
            }
        } catch (Exception e) {
            this.log.error(netLog.error("SubscribeManager05.lookupDomainUrls", ErrSev.MAJOR, e.getMessage()));
        }
        
        String[] result = new String[2];
        result[0] = nbUrl;
        result[1] = idcUrl;
        
        this.log.info(netLog.end("SubscribeManager05.lookupDomainUrls"));
        return result;
    }
    
    /**
     * Extract the soap/publishTo option from a yaml file.
     * 
     * @param config the config to search
     * @return the value of the publishTo parameter
     * @throws ConfigException
     */
    private String getPublishTo(Map config) throws ConfigException{
        HashMap<String,Object> soap = (HashMap<String,Object>) config.get("soap");
        if (soap == null ){
            throw new ConfigException("soap stanza not found in lookup.yaml");
        }
        return ((String)soap.get("publishTo"));
    }
    
    /**
     * Determine the next time a subscription needs to be renewed. Its 
     * based on the returned expiration time, minus some window so subscription 
     * does not expire before it can be renewed.
     * 
     * @param termTime the expiration of the subscription
     * @return
     */
    private Long calcNextRenewTime(long termTime){
        long currentTime = System.currentTimeMillis();
        return currentTime + (long)((1.0-DEFAULT_TERM_TIME_WINDOW) * 
                (double)(termTime - currentTime)); 
    }
    
/*    public static void main(String[] args){
        try {
            ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_API);
            cc.setContext("DEVELOPMENT");
            cc.setServiceName(ServiceNames.SVC_API);
            cc.loadManifest(ServiceNames.SVC_API, ConfigDefaults.MANIFEST);
            cc.setLog4j();
            OSCARSSoapHandler05 handler = new OSCARSSoapHandler05();
            SubscribeManager05 subMgr = SubscribeManager05.getInstance();
            subMgr.hasSubscription("dev.es.net");
            while(true){
                System.out.println("sleeping...");
                Thread.sleep(10000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
*/
}
