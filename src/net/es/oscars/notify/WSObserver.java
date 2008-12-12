package net.es.oscars.notify;

import java.io.IOException;
import java.io.File;
import java.net.InetAddress;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import javax.xml.namespace.QName;
import net.es.oscars.client.Client;
import net.es.oscars.notify.OSCARSEvent;
import net.es.oscars.PropHandler;
import net.es.oscars.oscars.*;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.scheduler.*;
import org.apache.axis2.databinding.ADBException;
import org.apache.axis2.databinding.types.URI;
import org.apache.axis2.databinding.types.URI.MalformedURIException;
import org.apache.log4j.*;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMFactory;
import org.oasis_open.docs.wsn.b_2.*;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;
import org.ogf.schema.network.topology.ctrlplane.Path;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import org.jdom.*;
import org.jdom.xpath.*;
import org.jdom.input.*;
import org.jdom.output.*;
import org.quartz.*;
import org.w3.www._2005._08.addressing.*;

/**
 * WSObserver handles sending WS notification messages
 */
public class WSObserver implements Observer {
    private Logger log;
    private OSCARSCore core;
    private String brokerPublisherRegMgrURL;
    private String producerURL;
    private String repo;
    private String axisConfig;
    private boolean initialized;
    private HashMap<String,String> namespaces;
    private HashMap<String,String> prefixes;
    private static String publisherRegistrationId = null;
    private static String brokerConsumerURL = null;
    private static HashMap<String, String> topics = null;

	/* Constants */
    private final String TOPIC_EXPR_FULL = "http://docs.oasis-open.org/wsn/t-1/TopicExpression/Full";
    
    /** Constructor */
    public WSObserver() {
        this.log = Logger.getLogger(this.getClass());
        this.core = OSCARSCore.getInstance();
        /* Set global constants */
        this.namespaces = new HashMap<String,String>();
        this.namespaces.put("idc", "http://oscars.es.net/OSCARS");
        this.namespaces.put("nmwg-ctrlp", "http://ogf.org/schema/network/topology/ctrlPlane/20080828/");
        this.prefixes = new HashMap<String,String>();
        this.prefixes.put("http://oscars.es.net/OSCARS", "idc");
        this.prefixes.put("http://ogf.org/schema/network/topology/ctrlPlane/20080828/", "nmwg-ctrlp");
        /* initialize */
        this.initialize();
    }
    
    /**
     * This method does basic initialization. It will load in topic files
     * and read properties necessary to send messages.
     *
     */
    private boolean initialize(){
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties wsNotifyProps = propHandler.getPropertyGroup("notify.ws", true); 
        Properties idcProps = propHandler.getPropertyGroup("idc", true); 
        this.brokerPublisherRegMgrURL = wsNotifyProps.getProperty("broker.url");
        this.producerURL = idcProps.getProperty("url");
        String registerRetryAttempts = wsNotifyProps.getProperty("broker.registerRetryAttempts");
        //String catalinaHome = System.getProperty("catalina.home");
        String catalinaHome = System.getenv("CATALINA_HOME");
        // check for trailing slash
        System.out.println("catalina.home is: " + catalinaHome);
        if (!catalinaHome.endsWith("/")) {
            catalinaHome += "/";
        }
        String topicNsFile = catalinaHome + "shared/classes/server/idc-topicnamespace.xml";
        String topicSetFile = catalinaHome + "shared/classes/server/idc-topicset.xml";
        /* Set default urls. Lots of logging so its clear what's going on to users. */
        String localhost = null;
        
        try{
            localhost = InetAddress.getLocalHost().getHostName();
        }catch(Exception e){
            this.log.error("Unable to determine localhost.");
        }
        
        /* Set client files */
        this.repo = catalinaHome + "shared/classes/repo/";
        this.axisConfig = this.repo + "axis2-norampart.xml";
        
        /* Load Topics files */
        try{
            this.loadTopics(topicSetFile, topicNsFile);
        }catch(Exception e){
            this.log.error("Error loading topics: " + e);
            e.printStackTrace();
            this.initialized = false;
            return this.initialized;
        }
        
        if(this.brokerPublisherRegMgrURL == null && localhost == null){
            this.log.error("You need to set notify.ws.broker.url in oscars.properties!");
            this.initialized = false;
            return this.initialized;
        }else if(this.brokerPublisherRegMgrURL == null){
            this.brokerPublisherRegMgrURL = "https://" + localhost + ":8443/axis2/services/OSCARSNotify";
            this.log.info("notify.ws.broker not set in oscars.properties. Defaulting to " + this.brokerPublisherRegMgrURL);
        }
        
        if(this.producerURL == null && localhost == null){
            this.log.error("You need to set idc.url in oscars.properties!");
            this.initialized = false;
            return this.initialized;
        }else if(this.producerURL == null){
            this.producerURL = "https://" + localhost + ":8443/axis2/services/OSCARS";
            this.log.info("idc.url not set in oscars.properties. Defaulting to " + this.producerURL);
        }
        
        /* Register publisher */
        //TODO: Move this to ServiceManager
        int registerRetries = 10;//default
        if("unlimited".equals(registerRetryAttempts)){
            registerRetries = -1;
        }else if(registerRetryAttempts != null && 
                 registerRetryAttempts.matches("\\d+")){
            registerRetries = Integer.parseInt(registerRetryAttempts);
        }
        
        Scheduler sched = this.core.getScheduleManager().getScheduler();
        String triggerName = "pubRegTrig-" + idcProps.hashCode();
        String jobName = "pubReg-" + idcProps.hashCode();
        SimpleTrigger trigger = new SimpleTrigger(triggerName, null, 
                                                  new Date(), 
                                                  null, 0, 0L);
        JobDetail jobDetail = new JobDetail(jobName, "REGISTER_PUBLISHER",
                                            RegisterPublisherJob.class);
        JobDataMap dataMap = new JobDataMap();
        dataMap.put("url", this.brokerPublisherRegMgrURL);
        dataMap.put("repo", this.repo);
        dataMap.put("publisher", this.producerURL);
        dataMap.put("retryAttempts", registerRetries);
        jobDetail.setJobDataMap(dataMap);
        try{
            this.log.debug("Adding job " + jobName);
            sched.scheduleJob(jobDetail, trigger);
            this.log.debug("Job added.");
        }catch(SchedulerException ex){
            this.log.error("Scheduler exception: " + ex);
            //don't set uninitialized because job will try again on its own
        }
        
        this.initialized = true;
        return this.initialized;
    }
    
    /**
     * Sets the publisherRegistrationId to use in the ProducerReference of all
     * Notify messages and empties any queued notifications. If set to null 
     * then it will be interpreted as no PublisherRegistration exists and that
     * Notify messages can't be sent. While null, notification messages will either 
     * be queued or discarded depending on the value of notify.ws.queueOnFailure.
     *
     * @param id the publisherRegistrationId to set
     */
    public synchronized static void registered(String id, String url){
        WSObserver.publisherRegistrationId = id;
        WSObserver.brokerConsumerURL = url;
    }

    /**
     * Observer method called whenever a change occurs. It accepts an 
     * Observable object and an net.es.oscars.notify.OSCARSEvent object as
     * arguments.
     *
     * @param obj the observable object
     * @param arg the event that ocurred
     */
    public void update (Observable obj, Object arg) {
         // Observer interface requires second argument to be Object
        if(!(arg instanceof OSCARSEvent)){
            this.log.error("[ALERT] Wrong argument passed to WSObserver");
            return;
        }
        OSCARSEvent osEvent = (OSCARSEvent) arg;
        
        /* if not initialized then try to initialize so user does not need to 
            restart IDC to change URLS in oscars.properties. */
        if((!this.initialized) && (!this.initialize())){
            this.log.error("Can't send web service notifications because of " + 
                           "errors. See previous error messages for details.");
            return;
        }
       
        //if not registered with broker, then retry later
        if(this.publisherRegistrationId == null){
            this.log.debug("Not registered so exiting!");
            return;
        }
        
        OMElement omEvent = null;
        Client client = new Client();
        EndpointReferenceType prodRef = null;
        TopicExpressionType topicExpr = null;
        EventContent event = this.oscarsEventToWSEvent(osEvent);
        NotificationMessageHolderType msgHolder = new NotificationMessageHolderType();
        MessageType msg = new MessageType();
        
        try{
            client.setUpNotify(true, this.brokerConsumerURL, this.repo, this.axisConfig);
            prodRef = client.generateEndpointReference(this.producerURL);
            topicExpr = this.generateTopicExpression(event);
            OMFactory omFactory = (OMFactory) OMAbstractFactory.getOMFactory();
            omEvent = event.getOMElement(Event.MY_QNAME, omFactory);
        }catch(Exception e){
            this.log.error(e);
            client.cleanUp();
            return;
        }
        //return if doesn't match any active topics
        if(topicExpr == null){ return; }
        /* Set publisherRegistrationId
           Note: Empty string means broker does not require ID
         */
        if(!"".equals(publisherRegistrationId)){
            ReferenceParametersType refParams = new ReferenceParametersType();
            refParams.setPublisherRegistrationId(publisherRegistrationId);
            prodRef.setReferenceParameters(refParams); 
        }
        
        msg.addExtraElement(omEvent);
        msgHolder.setTopic(topicExpr);
        msgHolder.setProducerReference(prodRef);
        msgHolder.setMessage(msg);
        
        //send notifcation
        try{
            client.notify(msgHolder);
        }catch(Exception e){
            this.log.error(e);
            e.printStackTrace();
        }finally{
            client.cleanUp();
        }
    }
    
    /**
     * Converts an OSCAREvent object to an EventContent type that can be passed in Axis2.
     * 
     * @param osEvent the OSCARSEvent to convert
     * @returns a converted EventContent object
     */
    private EventContent oscarsEventToWSEvent(OSCARSEvent osEvent){
        EventContent event = new EventContent();
        HashMap<String, String[]> map = osEvent.getReservationParams();
        ResDetails resDetails =
            WSDLTypeConverter.hashMapToResDetails(osEvent.getReservationParams());
        LocalDetails localDetails = this.getLocalDetails(map.get("intradomainPath"), 
                                                         map.get("intradomainHopInfo"));
        Path path = new Path();
        
        for(String key: map.keySet()){
            String[] val = map.get(key);
            //System.out.print(key + ": ");
            if(val == null){
                //System.out.println("null");
                continue;
            }
            /* for(int i=0; i < val.length; i++){
                System.out.println(val[i]);
            } */
        }
        System.out.println("ResDetails: " + resDetails);
        event.setId("event-" + event.hashCode());
        event.setType(osEvent.getType());
        event.setTimestamp(osEvent.getTimestamp());
        event.setUserLogin(osEvent.getUserLogin());
        event.setErrorCode(osEvent.getErrorCode());
        event.setErrorMessage(osEvent.getErrorMessage());
        if(osEvent.getErrorCode() != null && osEvent.getSource() != null){
            try{
                String errSrc = osEvent.getSource();
                //test if URL
                new URL(errSrc);
                event.setErrorSource(errSrc);
            }catch(Exception e){}
        }
        event.setResDetails(resDetails);
        event.setLocalDetails(localDetails);
        //TODO: Set msgDetails
        
        return event;
    }
    
    /**
     * Creates a LocalDetails element containing the local path
     *
     * @param path an array of hops to add to the local path
     * @param hopInfo an array of details about each hop in the local path
     * @return a Axis2 LocalDetails object containing the local path
     */
    private LocalDetails getLocalDetails(String[] path, String[] hopInfo){
        if(path == null || path.length < 1){ return null; }
        LocalDetails localDetails = new LocalDetails();
        OMFactory omFactory = (OMFactory) OMAbstractFactory.getOMFactory();
        OMElement omPath = null;
        
        //Build path
        
        CtrlPlanePathContent wsPath =
            WSDLTypeConverter.arrayToCtrlPlanePath(path, hopInfo);
        wsPath.setId("localPath");
        try{
            omPath = wsPath.getOMElement(Path.MY_QNAME, omFactory);
         }catch(ADBException e){
            this.log.error(e + "Ignoring error setting local details.");
            return null;
        }
        localDetails.addExtraElement(omPath);
        return localDetails;
    }
    
    /**
     * Matches an event to a topic then returns a TopicExpression
     *
     * @param event the event to classify as belonging to a topic
     * @return a topic expression for the given event
     */
    private TopicExpressionType generateTopicExpression(EventContent event) 
                    throws MalformedURIException, ADBException, JaxenException{
        TopicExpressionType topicExpr = new TopicExpressionType();
        URI topicDialect = new URI(TOPIC_EXPR_FULL);
        topicExpr.setDialect(topicDialect);

        String topicString = "";
        boolean firstMatch = true;
        for(String topic : this.topics.keySet()){
            String xpath = this.topics.get(topic);
            /* Prepare message for parsing by adding outer element to appease axis2 */
            OMFactory omFactory = (OMFactory) OMAbstractFactory.getOMFactory();
            QName rootQname = new QName("EventWrapper", this.namespaces.get("idc"), Event.MY_QNAME.getPrefix());
            OMElement omRoot = omFactory.createOMElement(rootQname);
            OMElement omEvent = event.getOMElement(Event.MY_QNAME, omFactory);
            omRoot.addChild(omEvent);
            AXIOMXPath xpathExpression = new AXIOMXPath (xpath); 
            SimpleNamespaceContext nsContext = new SimpleNamespaceContext(this.namespaces);
            xpathExpression.setNamespaceContext(nsContext);
            if(xpathExpression.booleanValueOf(omRoot)){
                topicString += (firstMatch ? "" : "|");
                topicString += (topic);
                firstMatch = false;
            }
            this.log.debug("Generated topic string: " + topicString);
        }
        if("".equals(topicString)){
            this.log.info("No Topic matches event");
            return null;
        }
        topicExpr.setString(topicString);
         
        return topicExpr;
    }
    
    /**
     * Loads Topics from an XML file. The file must follow the WS-Topics standard
     * listed by OASIS (http://docs.oasis-open.org/wsn/wsn-ws_topics-1.3-spec-os.pdf).
     * It does NOT support the option "Extension Topics" listed in section 6.1 of the
     * WS-Topics specification. This is intended to allow for lightweight activation/deactivation
     * of notifications about certain topics.
     *
     * @param topicSetFile file that contains supported topics (TopicSet)
     * @param topicNamesapce file that describes all possible topics (TopicNamespace)
     */
    private void loadTopics(String topicSetFile, String topicNSFile) 
                                            throws IOException, JDOMException{
        this.topics = new HashMap<String, String>();
        Namespace wstop = Namespace.getNamespace("http://docs.oasis-open.org/wsn/t-1");
        
        //Step 1: Figure out which Topics are supported
        //open topicset
        SAXBuilder builder = new SAXBuilder(false);
        Document topicSetDoc = builder.build(new File(topicSetFile));
        Element topicSetRoot = topicSetDoc.getRootElement();
        ArrayList<Element> topicSetElems = new ArrayList<Element>();
        ArrayList<String> topicSetParents = new ArrayList<String>();
        HashMap<String, Boolean> supportedTopics = new HashMap<String, Boolean>();
        //initialize topic set
        topicSetElems.addAll(0, topicSetRoot.getChildren());
        //load this.topics with name,xpath entries
        while(!topicSetElems.isEmpty()){
            Element currElem = topicSetElems.get(0);
            String name = currElem.getName();
            List children = currElem.getChildren();
            //check if working way back up tree
            if((!topicSetParents.isEmpty()) && name.equals(topicSetParents.get(0))){
                topicSetElems.remove(0);
                topicSetParents.remove(0);
                continue;
            }
            String namespaceURI = currElem.getNamespaceURI();
            String prefix = this.prefixes.get(namespaceURI);
            String isTopic = currElem.getAttributeValue("topic", wstop);
            if("true".equals(isTopic)){
                String completeName = prefix + ":";
                for(int i = (topicSetParents.size() - 1); i >= 0; i--){
                    completeName += (topicSetParents.get(i) + "/");
                }
                completeName += name;
                supportedTopics.put(completeName, true);
                this.log.debug("TopicSet includes topic " + completeName);
            }
            if(children.isEmpty()){
                topicSetElems.remove(0);
            }else{
                topicSetParents.add(0, name);
                topicSetElems.addAll(0, children);
            }
        }
        
        //Step 2: Find supported topics in topic namespace
        //load file
        this.log.debug("Loading topic namespace file " + topicNSFile);
        Document topicNSDoc = builder.build(new File(topicNSFile));
        Element topicNSRoot = topicNSDoc.getRootElement();
        String targetNamespace = topicNSRoot.getAttributeValue("targetNamespace");
        String prefix = this.prefixes.get(targetNamespace);
        ArrayList<Element> topicNSElems = new ArrayList<Element>();
        ArrayList<String> parents = new ArrayList<String>();
        topicNSElems.addAll(0, topicNSRoot.getChildren("Topic", wstop));
        while(!topicNSElems.isEmpty()){
            Element currElem = topicNSElems.get(0);
            String completeName = prefix + ":";
            String name = currElem.getAttributeValue("name", wstop);
            if(name == null){
                this.log.error("idc-topicnamespace.xml contains a Topic without a 'name' attribute.");
                topicNSElems.remove(0);
                continue;
            }
            
            List children = currElem.getChildren("Topic", wstop);
            //check if working way back up tree
            if((!parents.isEmpty()) && name.equals(parents.get(0))){
                topicNSElems.remove(0);
                parents.remove(0);
                continue;
            }
            
            for(int i = (parents.size() - 1); i >= 0; i--){
                completeName += (parents.get(i) + "/");
            }
            completeName += name;
            if(!supportedTopics.containsKey(completeName)){
                topicNSElems.remove(0);
                this.log.debug("Ignoring disabled topic " + completeName);
                continue;
            }
            
            if(children.isEmpty()){
                topicNSElems.remove(0);
            }else{
                parents.add(0, name);
                topicNSElems.addAll(0, children);
            }
            
            Element msgPattern = currElem.getChild("MessagePattern", wstop);
            if(msgPattern == null){
                continue;
            }
            String dialect = msgPattern.getAttributeValue("Dialect", wstop);
            if("http://www.w3.org/TR/1999/REC-xpath-19991116".equals(dialect)){
                String xpath = msgPattern.getText();
                this.topics.put(completeName, xpath);
            }
        }
    }
    
    public static HashMap<String, String> getTopics() {
		return topics;
	}
}
