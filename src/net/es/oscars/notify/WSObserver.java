package net.es.oscars.notify;

import java.io.IOException;
import java.io.File;
import java.net.InetAddress;
import java.util.*;
import javax.xml.namespace.QName;
import net.es.oscars.client.Client;
import net.es.oscars.notify.OSCARSEvent;
import net.es.oscars.PropHandler;
import net.es.oscars.oscars.TypeConverter;
import net.es.oscars.wsdlTypes.*;
import org.apache.axis2.databinding.ADBException;
import org.apache.axis2.databinding.types.URI;
import org.apache.axis2.databinding.types.URI.MalformedURIException;
import org.apache.log4j.*;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMFactory;
import org.oasis_open.docs.wsn.b_2.*;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import org.jdom.*;
import org.jdom.xpath.*;
import org.jdom.input.*;
import org.jdom.output.*;
import org.w3.www._2005._08.addressing.*;

/**
 * WSObserver handles sending WS notification messages
 */
public class WSObserver implements Observer {
    private Logger log;
    private String notificationBroker;
    private String producerURL;
    private String repo;
    private String axisConfig;
    private boolean initialized;
    private HashMap<String, String> topics;
    private HashMap<String,String> namespaces;
    private HashMap<String,String> prefixes;
    
    /* Constants */
    private final String TOPIC_EXPR_FULL = "http://docs.oasis-open.org/wsn/t-1/TopicExpression/Full";
    
    /** Constructor */
    public WSObserver() {
        this.log = Logger.getLogger(this.getClass());
        /* Set global constants */
        this.namespaces = new HashMap<String,String>();
        this.namespaces.put("idc", "http://oscars.es.net/OSCARS");
        this.namespaces.put("nmwg-ctrlp", "http://ogf.org/schema/network/topology/ctrlPlane/20070626/");
        this.prefixes = new HashMap<String,String>();
        this.prefixes.put("http://oscars.es.net/OSCARS", "idc");
        this.prefixes.put("http://ogf.org/schema/network/topology/ctrlPlane/20070626/", "nmwg-ctrlp");
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
        this.notificationBroker = wsNotifyProps.getProperty("broker.url");
        this.producerURL = idcProps.getProperty("url");
        String catalinaHome = System.getProperty("catalina.home");
        // check for trailing slash
        if (!catalinaHome.endsWith("/")) {
            catalinaHome += "/";
        }
        String topicNsFile = catalinaHome + "shared/classes/server/idc-topicnamespace.xml";
        String topicSetFile = catalinaHome + "shared/classes/server/idc-topicset.xml";
        
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
        
        /* If both properties set, then done */
        if(this.notificationBroker != null && this.producerURL != null){
            this.initialized = true;
            return this.initialized;
        }
        
        /* Set default urls. Lots of logging so its clear what's going on to users. */
        String localhost = null;
        try{
            localhost = InetAddress.getLocalHost().getHostName();
            this.initialized = true;
        }catch(Exception e){
            this.initialized = false;
            this.log.warn("Unable to determine localhost.");
        }
        
        if(this.notificationBroker == null && localhost == null){
            this.log.error("You need to set notify.ws.broker.url in oscars.properties!");
        }else if(this.notificationBroker == null){
            this.notificationBroker = "https://" + localhost + ":8443/axis2/services/OSCARSNotify";
            this.log.info("notify.ws.broker not set in oscars.properties. Defaulting to " + this.notificationBroker);
        }
        
        if(this.producerURL == null && localhost == null){
            this.log.error("You need to set idc.url in oscars.properties!");
        }else if(this.producerURL == null){
            this.producerURL = "https://" + localhost + ":8443/axis2/services/OSCARS";
            this.log.info("idc.url not set in oscars.properties. Defaulting to " + this.producerURL);
        }
        
        return this.initialized;
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
        /* if not initialized then try to initialize so user does not need to 
            restart IDC to change URLS in oscars.properties. */
        if((!this.initialized) && (!this.initialize())){
            this.log.error("Can't send web service notifications because of " + 
                           "errors. See previous error messages for details.");
            return;
        }
        // Observer interface requires second argument to be Object
        if(!(arg instanceof OSCARSEvent)){
            this.log.error("[ALERT] Wrong argument passed to WSObserver");
            return;
        }
        
        OSCARSEvent osEvent = (OSCARSEvent) arg;
        OMElement omEvent = null;
        Client client = new Client();
        EndpointReferenceType prodRef = null;
        TopicExpressionType topicExpr = null;
        EventContent event = this.oscarsEventToWSEvent(osEvent);
        NotificationMessageHolderType msgHolder = new NotificationMessageHolderType();
        MessageType msg = new MessageType();
        
        try{
            client.setUpNotify(true, this.notificationBroker, this.repo, this.axisConfig);
            prodRef = client.generateEndpointReference(this.producerURL);
            topicExpr = this.generateTopicExpression(event);
            OMFactory omFactory = (OMFactory) OMAbstractFactory.getOMFactory();
            omEvent = event.getOMElement(Event.MY_QNAME, omFactory);
        }catch(Exception e){
            this.log.error(e);
            return;
        }
        //return if doesn't match any active topics
        if(topicExpr == null){ return; }
        
        
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
        TypeConverter tc = new TypeConverter();
        ResDetails resDetails = tc.hashMaptoResDetails(osEvent.getReservationParams());
        HashMap<String, String[]> map = osEvent.getReservationParams();
        for(String key: map.keySet()){
            String[] val = map.get(key);
            System.out.print(key + ": ");
            if(val == null){
                System.out.println("null");
                continue;
            }
            for(int i=0; i < val.length; i++){
                System.out.println(val[i]);
            }
        }
        System.out.println("ResDetails: " + resDetails);
        event.setId("event-" + event.hashCode());
        event.setType(osEvent.getType());
        event.setTimestamp(osEvent.getTimestamp());
        event.setUserLogin(osEvent.getUserLogin());
        event.setErrorCode(osEvent.getErrorCode());
        event.setErrorMessage(osEvent.getErrorMessage());
        event.setResDetails(resDetails);
        //TODO: Set pathDetailLevel
        //TODO: Set msgDetails
        
        return event;
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
        this.log.info("xpath.start");
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
                this.log.info("XPATH " + topic + " matches event!");
                topicString += (firstMatch ? "" : "|");
                topicString += (topic);
                firstMatch = false;
            }else{
                this.log.info("XPATH " + topic + " does not match event!");
            }
            this.log.debug("Generated topic string: " + topicString);
        }
        if("".equals(topicString)){
            this.log.info("No Topic matches event");
            return null;
        }
        topicExpr.setString(topicString);
         
        this.log.info("xpath.end");
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
        this.log.debug("loadTopics.start");
        this.topics = new HashMap<String, String>();
        Namespace wstop = Namespace.getNamespace("http://docs.oasis-open.org/wsn/t-1");
        
        //Step 1: Figure out which Topics are supported
        //open topicset
        this.log.debug("Loading topic set file " + topicSetFile);
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
                this.log.debug(name + ".completeName=" + completeName);
                this.log.debug(name + ".xpath=" + xpath);
            }
        }
        
        this.log.debug("loadTopics.end");
    }
}
