package net.es.oscars.notifybroker.ws;

import java.util.*;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.log4j.*;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.oasis_open.docs.wsn.b_2.*;
import org.oasis_open.docs.wsn.br_2.*;
import org.w3.www._2005._08.addressing.*;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.databinding.types.URI;
import org.apache.axis2.databinding.utils.writer.MTOMAwareXMLSerializer;
import net.es.oscars.PropHandler;
import net.es.oscars.rmi.notifybroker.NotifyRmiClient;
import net.es.oscars.rmi.notifybroker.NotifyRmiInterface;
import net.es.oscars.rmi.notifybroker.xface.RmiSubscribeResponse;

/** 
 * NotifyBrokerAdapter provides a translation layer between Axis2 and RMI. 
 * It is intended to provide a gateway for Axis2 into more general core functionality
 * of the notification broker.
 *
 * @author Andrew Lake (alake@internet2.edu)
 */
public class NotifyBrokerAdapter{
    private Logger log;
    private String subscriptionManagerURL;
    private HashMap<String,String> namespaces;
    
    /** Default constructor */
    public NotifyBrokerAdapter(){
        this.log = Logger.getLogger(this.getClass());
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("notify.ws.broker", true); 
        this.subscriptionManagerURL = props.getProperty("url");
        if(this.subscriptionManagerURL == null){
            String localhost = null;
            try{
                localhost = InetAddress.getLocalHost().getHostName();
            }catch(Exception e){
                this.log.error("Please set 'notifybroker.url' in oscars.properties!");
            }
            this.subscriptionManagerURL = "https://" + localhost + ":8443/axis2/services/OSCARSNotify";
        }
        this.log.info("OSCARSNotify.url=" + this.subscriptionManagerURL);
        
        //TODO: Loads namespace prefixes from properties file
        this.namespaces = new HashMap<String,String>();
        this.namespaces.put("idc", "http://oscars.es.net/OSCARS");
        this.namespaces.put("nmwg-ctrlp", "http://ogf.org/schema/network/topology/ctrlPlane/20080828/");
        this.namespaces.put("wsa", "http://www.w3.org/2005/08/addressing");
    }
    
    /**
     * Converts a Notify message into an RMI call
     * 
     * @param holder the Notify message
     * @throws RemoteException
     */
    public void notify(NotificationMessageHolderType holder) throws RemoteException{
        NotifyRmiInterface nbRmiClient = new NotifyRmiClient();
        TopicExpressionType[] topicExprs = {holder.getTopic()};
        EndpointReferenceType producerRef = holder.getProducerReference();
        if(producerRef == null){
            this.log.error("Could not find registration. No producer reference provided.");
            return;
        }
        ReferenceParametersType refParams = producerRef.getReferenceParameters(); 
        if(refParams == null){ 
            this.log.error("Could not find registration. No registration reference provided.");
            return;
        }
        String publisherRegId = refParams.getPublisherRegistrationId();
        if(publisherRegId == null){
            this.log.error("Could not find registration. No registration ID provided.");
            return;
        }
        String publisherUrl = this.parseEPR(producerRef);
        ArrayList<String> topics = null;
        try {
            topics = this.parseTopics(topicExprs);
        } catch (TopicExpressionDialectUnknownFault e) {
            this.log.error(e.getMessage());
        } catch (InvalidTopicExpressionFault e) {
            this.log.error(e.getMessage());
        }
        OMElement[] msg = holder.getMessage().getExtraElement();
        List<Element> jdomMsg = this.axiom2Jdom(msg);
        
        nbRmiClient.notify(publisherUrl, publisherRegId, topics, jdomMsg);
    }
    
    /**
     * Converts a Subscribe SOAP message to an RMI call
     * 
     * @param request the Subscribe request
     * @param user the login of the user that sent the Subscribe message
     * @return a Axis2 SubscribeResponse with the subscription ID and termination time
     * @throws TopicExpressionDialectUnknownFault
     * @throws InvalidTopicExpressionFault
     * @throws TopicNotSupportedFault
     * @throws InvalidProducerPropertiesExpressionFault
     * @throws InvalidFilterFault
     * @throws InvalidMessageContentExpressionFault
     * @throws UnacceptableInitialTerminationTimeFault
     * @throws RemoteException
     */
    public SubscribeResponse subscribe(Subscribe request, String user)
        throws TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault,TopicNotSupportedFault,
               InvalidProducerPropertiesExpressionFault,InvalidFilterFault,InvalidMessageContentExpressionFault,
               UnacceptableInitialTerminationTimeFault, RemoteException{
        
        this.log.debug("subscribe.start");
        SubscribeResponse response = null;
        NotifyRmiInterface nbRmiClient = new NotifyRmiClient();
        String consumerUrl = this.parseEPR(request.getConsumerReference());
        long initTermTime = 0L;
        HashMap<String,List<String>> filters = new HashMap<String,List<String>>();
        
        /* NOTE: Requires a filter and topic to specified.
         * Filter and TopicExpression are checked here and not in
         * schema because this is an optional behavior defined in the
         * WS-Notification spec. It also allows for a better error.
         * See http://docs.oasis-open.org/wsn/wsn-ws_base_notification-1.3-spec-os.pdf line 544-545.
         */
        FilterType requestFilter = request.getFilter();
        if(requestFilter == null || requestFilter.getTopicExpression() == null){
            throw new InvalidFilterFault("Invalid filter specified. This " +
                                         "NotificationBroker implementation" +
                                         " requires a filter containing at " +
                                         "least one TopicExpression.");
        }
        QueryExpressionType[] producerPropsFilters = requestFilter.getProducerProperties();
        QueryExpressionType[] messageContentFilters =  requestFilter.getMessageContent();
        TopicExpressionType[] topicFilters = requestFilter.getTopicExpression();
        
        //Add producer filters
        List<String> prodFilterList = new ArrayList<String>();
        if(producerPropsFilters != null){
            for(QueryExpressionType producerPropsFilter : producerPropsFilters){
                if(this.validateQueryExpression(producerPropsFilter, true)){
                    String xpath = producerPropsFilter.getString();
                    prodFilterList.add(xpath);
                }
            }
        }
        filters.put(NotifyRmiClient.FILTER_PRODXPATH, prodFilterList);
        
        //Add message filters
        List<String> msgFilterList = new ArrayList<String>();
        if(messageContentFilters != null){
            for(QueryExpressionType messageContentFilter : messageContentFilters){
                if(this.validateQueryExpression(messageContentFilter, false)){
                    String xpath = messageContentFilter.getString();
                    msgFilterList.add(xpath);    
                }
            }
        }
        filters.put(NotifyRmiClient.FILTER_MSGXPATH, msgFilterList);
        
        //Add topic filters
        filters.put(NotifyRmiClient.FILTER_TOPIC, this.parseTopics(topicFilters));
        
        //Get initial termination time
        try{
            initTermTime = this.parseTermTime(request.getInitialTerminationTime());
        }catch(UnacceptableTerminationTimeFault ex){
            throw new UnacceptableInitialTerminationTimeFault(ex.getMessage());
        }
        
        //send to RMI server
        RmiSubscribeResponse rmiResponse = nbRmiClient.subscribe(consumerUrl, new Long(initTermTime), filters, user);
        response = this.createSubscribeResponse(rmiResponse);
        
        this.log.debug("subscribe.end");
        return response;
    }
    
    /**
     * Converts a Renew SOAP message to an RMI call
     * 
     * @param request the Renew request
     * @param user the login of the user that sent the Renew message
     * @return an axis2 RenewResponse
     * @throws AAAFaultMessage
     * @throws ResourceUnknownFault
     * @throws UnacceptableTerminationTimeFault
     * @throws RemoteException
     */
    public RenewResponse renew(Renew request, String user) 
        throws AAAFaultMessage,  ResourceUnknownFault,
               UnacceptableTerminationTimeFault, RemoteException{
        this.log.debug("renew.start");
        NotifyRmiInterface nbRmiClient = new NotifyRmiClient();
        RenewResponse response = new RenewResponse();
        Long termTime = this.parseTermTime(request.getTerminationTime());
        EndpointReferenceType subRef = request.getSubscriptionReference();
        String subscriptionId = this.verifySubscriptionRef(subRef);
        
        /* Call the rmi component to renew the subscription */
        termTime = nbRmiClient.renew(subscriptionId, termTime, user);
        
        /* Convert creation and termination time to Calendar object */
        GregorianCalendar currCal = new GregorianCalendar();
        GregorianCalendar termCal = new GregorianCalendar();
        currCal.setTimeInMillis(System.currentTimeMillis());
        termCal.setTimeInMillis(termTime * 1000);
        response.setSubscriptionReference(subRef);
        response.setCurrentTime(currCal);
        response.setTerminationTime(termCal);
        
        this.log.debug("renew.end");
        return response;
    }
    
     /**
      * Converts an Unsubecribe SOAP request to an RMI call
      * 
      * @param request the Unsubscribe request
      * @param user the login of the user that sent the request
      * @return the axis2 response
      * @throws AAAFaultMessage
      * @throws ResourceUnknownFault
      * @throws UnableToDestroySubscriptionFault
      * @throws RemoteException
      */
    public UnsubscribeResponse unsubscribe(Unsubscribe request, String user)
                        throws AAAFaultMessage, ResourceUnknownFault,
                               UnableToDestroySubscriptionFault, RemoteException{
        this.log.debug("unsubscribe.start");
        NotifyRmiInterface nbRmiClient = new NotifyRmiClient();
        UnsubscribeResponse response = new UnsubscribeResponse();
        EndpointReferenceType subRef = request.getSubscriptionReference();
        String subscriptionId = this.verifySubscriptionRef(subRef);
        
        //Call the rmi component to unsubscribe
        nbRmiClient.unsubscribe(subscriptionId, user);
        response.setSubscriptionReference(subRef);
        
        this.log.debug("unsubscribe.end");
        return response;
    }
    
    /**
     * Converts a pause SOAP message to an RMI call
     * 
     * @param request the Pause request
     * @param user the login of the user that sent the request
     * @return an axis2 response to the call
     * @throws AAAFaultMessage
     * @throws ResourceUnknownFault
     * @throws PauseFailedFault
     * @throws RemoteException
     */
    public PauseSubscriptionResponse pause(PauseSubscription request, String user)
                               throws AAAFaultMessage, ResourceUnknownFault,
                                      PauseFailedFault, RemoteException{
        this.log.debug("pause.start");
        NotifyRmiInterface nbRmiClient = new NotifyRmiClient();
        PauseSubscriptionResponse response = new PauseSubscriptionResponse();
        EndpointReferenceType subRef = request.getSubscriptionReference();
        String subscriptionId = this.verifySubscriptionRef(subRef);
        
        //Call the rmi component to pause
        nbRmiClient.pauseSubscription(subscriptionId, user);
        response.setSubscriptionReference(subRef);
        this.log.debug("pause.end");
        return response;
    }
    
    /**
     * Converts a resume SOAP message to an RMI call
     * 
     * @param request the Resume request
     * @param user the login of the user that sent the request
     * @return the axis2 Resume response
     * @throws AAAFaultMessage
     * @throws ResourceUnknownFault
     * @throws ResumeFailedFault
     * @throws RemoteException
     */
    public ResumeSubscriptionResponse resume(ResumeSubscription request, String user)
            throws AAAFaultMessage, ResourceUnknownFault, ResumeFailedFault, RemoteException{
        this.log.debug("resume.start");
        NotifyRmiInterface nbRmiClient = new NotifyRmiClient();
        ResumeSubscriptionResponse response = new ResumeSubscriptionResponse();
        EndpointReferenceType subRef = request.getSubscriptionReference();
        String subscriptionId = this.verifySubscriptionRef(subRef);
        
        //Call the rmi component to pause
        nbRmiClient.resumeSubscription(subscriptionId, user);
        response.setSubscriptionReference(subRef);
        this.log.debug("resume.end");
        return response;
    }
    
    /**
     * Converts a RegisterPublisher SOAP call to an RMI call
     *
     * @param request the registration request
     * @param user the login of the user that sent the registration
     * @return an Axis2 RegisterPublisherResponse with the result of the registration
     * @throws UnacceptableInitialTerminationTimeFault
     * @throws PublisherRegistrationFailedFault
     * @throws RemoteException 
     */
    public RegisterPublisherResponse registerPublisher(RegisterPublisher request, String user)
                                throws UnacceptableInitialTerminationTimeFault,
                                       PublisherRegistrationFailedFault, RemoteException{
        this.log.debug("registerPublisher.start");
        NotifyRmiInterface nbRmiClient = new NotifyRmiClient();
        RegisterPublisherResponse response = null;
        String publisherUrl = this.parseEPR(request.getPublisherReference());
        Calendar termTimeCal = request.getInitialTerminationTime();
        Long termTime = null;
        boolean demand = request.getDemand();
        
        //TODO:Support demand based publishing
        if(demand){
            throw new PublisherRegistrationFailedFault("Demand publishing is not supported by this implementation.");
        }
        
        if(termTimeCal != null){
            termTime = termTimeCal.getTimeInMillis()/1000L;
        }
        
        String registrationId = nbRmiClient.registerPublisher(publisherUrl, 
                null, demand, termTime, user);
        response = this.createRegisterPublisherResponse(registrationId);
        this.log.debug("registerPublisher.end");
        
        return response;
    }
    
    
    /**
     * Converts a DestroyPublisher SOAP call to an RMI call
     * 
     * @param request the Axis2 object with the registration to destroy
     * @param user the login of the user that sent the request
     * @return an Axis2 object with the result of the destroy
     * @throws ResourceUnknownFault
     * @throws ResourceNotDestroyedFault
     * @throws RemoteException 
     */
    public DestroyRegistrationResponse destroyRegistration(DestroyRegistration request, 
            String user) throws ResourceUnknownFault, ResourceNotDestroyedFault, RemoteException{
        
        this.log.debug("destroyRegistration.start");
        NotifyRmiInterface nbRmiClient = new NotifyRmiClient();
        DestroyRegistrationResponse response = new DestroyRegistrationResponse();
        EndpointReferenceType pubRef = request.getPublisherRegistrationReference();
        String address = this.parseEPR(pubRef);
        ReferenceParametersType refParams = pubRef.getReferenceParameters(); 
        if(refParams == null){ 
            throw new ResourceUnknownFault("Could not find registration." +
                                        "No registration reference provided.");
        }
        String pubId = refParams.getPublisherRegistrationId();
        if(pubId == null){
            throw new ResourceUnknownFault("Could not find registration." +
                                           "No registration ID provided.");
        }
        if(!this.subscriptionManagerURL.equals(address)){
            throw new ResourceUnknownFault("Could not find registration." +
                  "Invalid registration manager address. This notification" +
                  " broker requires you to use address " + 
                   this.subscriptionManagerURL);
        }
        nbRmiClient.destroyRegistration(pubId, user);
        response.setPublisherRegistrationReference(pubRef);
        
        this.log.debug("destroyRegistration.end");
        
        return response;
    }
    
    /**
     * Converts an RMI response to a subscribe call to an Axis2 SubscribeResponse
     * 
     * @param subscription the RMI response to convert
     * @return an Axis2 object that represents the SubscribeResponse
     */
    public SubscribeResponse createSubscribeResponse(RmiSubscribeResponse subscription){
        SubscribeResponse response = new SubscribeResponse();
        
        /* Set subscription reference */
        EndpointReferenceType subRef = new EndpointReferenceType();
        AttributedURIType subAttrUri = new AttributedURIType();
        try{
            URI subRefUri = new URI(this.subscriptionManagerURL);
            subAttrUri.setAnyURI(subRefUri);
        }catch(Exception e){}
        subRef.setAddress(subAttrUri);
        //set ReferenceParameters
        ReferenceParametersType subRefParams = new ReferenceParametersType();
        subRefParams.setSubscriptionId(subscription.getSubscriptionId());
        subRef.setReferenceParameters(subRefParams);
                
        /* Convert creation and termination time to Calendar object */
        GregorianCalendar createCal = new GregorianCalendar();
        GregorianCalendar termCal = new GregorianCalendar();
        createCal.setTimeInMillis(subscription.getCreatedTime() * 1000);
        termCal.setTimeInMillis(subscription.getTerminationTime() * 1000);
        
        response.setSubscriptionReference(subRef);
        response.setCurrentTime(createCal);
        response.setTerminationTime(termCal);
        
        return response;
    }
    /**
     * Creates a RegisterPublisherResponse from an ID
     *
     * @param publisher the Publisher Hibernate bean to convert
     * @return the Axis2 RegisterPublisherResponse converted from the Publisher object
     */
    private RegisterPublisherResponse createRegisterPublisherResponse(String registrationId){
        RegisterPublisherResponse response = new RegisterPublisherResponse();
        
        /* Set publisher reference */
        EndpointReferenceType pubRef = new EndpointReferenceType();
        AttributedURIType pubAttrUri = new AttributedURIType();
        try{
            URI pubRefUri = new URI(this.subscriptionManagerURL);
            pubAttrUri.setAnyURI(pubRefUri);
        }catch(Exception e){}
        pubRef.setAddress(pubAttrUri);
        //set ReferenceParameters
        ReferenceParametersType pubRefParams = new ReferenceParametersType();
        pubRefParams.setPublisherRegistrationId(registrationId);
        pubRef.setReferenceParameters(pubRefParams);
        
        /* Set consumer reference */
        EndpointReferenceType conRef = new EndpointReferenceType();
        AttributedURIType conAttrUri = new AttributedURIType();
        try{
            URI conRefUri = new URI(this.subscriptionManagerURL);
            conAttrUri.setAnyURI(conRefUri);
        }catch(Exception e){}
        conRef.setAddress(conAttrUri);
        
        response.setPublisherRegistrationReference(pubRef);
        response.setConsumerReference(conRef);
        
        return response;
    }
    
    /**
     * Parse a TopicExpression to make sure it is in a known Dialect.
     * It also splits topics into multiple topics. Currently this method
     * supports the SimpleTopic, ConcreteTopic, and FullTopic(partially)
     * specifications. 
     *
     * @param topicFilters the TopicExpression to parse
     * @return an array of strings containing each Topic
     * @throws InvalidTopicExpressionFault
     * @throws TopicExpressionDialectUnknownFault
     */
    public ArrayList<String> parseTopics(TopicExpressionType[] topicFilters) 
            throws TopicExpressionDialectUnknownFault,
                   InvalidTopicExpressionFault{
        if(topicFilters == null || topicFilters.length < 1){
            return new ArrayList<String>(0);
        }
        
        ArrayList<String> topics = new ArrayList<String>();
        for(TopicExpressionType topicFilter : topicFilters){
            if(topicFilter == null){
                continue;
            }
            String dialect = topicFilter.getDialect().toString();
            String topicString = topicFilter.getString();
            
            //check dialect
            if(WSNotifyConstants.XPATH_URI.equals(dialect)){
                 throw new TopicExpressionDialectUnknownFault("The XPath Topic " +
                            "Expression dialect is not supported at this time.");
            }else if(!(WSNotifyConstants.WS_TOPIC_SIMPLE.equals(dialect) || 
                    WSNotifyConstants.WS_TOPIC_CONCRETE.equals(dialect) || 
                    WSNotifyConstants.WS_TOPIC_FULL.equals(dialect))){
                throw new TopicExpressionDialectUnknownFault("Unknown Topic dialect '" + dialect + "'");
            }
            
            if(topicString == null || "".equals(topicString)){
                throw new InvalidTopicExpressionFault("Empty topic expression given.");
            }
            String[] topicTokens = topicString.split("\\|");
            for(String topicToken : topicTokens){
                topics.add(topicToken);
            }
            /* NOTE: Currently the notification broker is neutral as to the 
               type of topics is sends/receives so there is no check to see 
               if a topic is supported. This provides the greatest flexibility
               but doesn't allow the broker to return an error if it knows it
               can never send a notification for a particular topic. As we gain
               more experience we can revisit this fact */
        }
        
        return topics;
    }
    
    /**
     * Validates a QueryExpression as those used in the ProducerProperties and
     * MessageContent sections of a Subscribe Filter. Checks to see it is in the 
     * XPath dialect and that a valid XPath expression was provided.
     *
     * @param query the QueryExpression to validate
     * @param isProdProps true if this is a ProducerProperties element. Helps determine what exception to throw.
     * @return true if valid, false if no query exists. Throws an exception otherwise.
     * @throws InvalidFilterFault
     * @throws InvalidMessageContentExpressionFault
     * @throws InvalidProducerPropertiesExpressionFault
     */
    private boolean validateQueryExpression(QueryExpressionType query, boolean isProdProps)
                throws InvalidFilterFault, InvalidProducerPropertiesExpressionFault, 
                       InvalidMessageContentExpressionFault{
        if(query == null){
            return false;
        }
        
        String dialect = query.getDialect().toString();
        if(!WSNotifyConstants.XPATH_URI.equals(dialect)){
            throw new InvalidFilterFault("Filter dialect '" + dialect +
                                         "'is not supported by this service.");
        }
        
        String xpath = query.getString();
        try{
            XPath.newInstance(xpath);
        }catch(Exception e){
            String err = "Invalid expression: " + e;
            if(isProdProps){
                throw new InvalidProducerPropertiesExpressionFault(err);
            }
            throw new InvalidMessageContentExpressionFault(err);
        }
        
        return true;
    }
    
    /**
     *  Utility function that extracts the address from an EndpointReference
     *
     * @param epr the Endpoint Reference to parse
     * @return the address of the parsed EndpointReference
     */
    private String parseEPR(EndpointReferenceType epr){
        AttributedURIType address = epr.getAddress();
        URI uri = address.getAnyURI();
        return uri.toString();
    }
    
    /**
     *  Utility function that extracts an xsd:datetime or xsd:duration from a string
     *
     * @param termTime the string to parse
     * @return a timestamp in seconds equivalent to the given string
     * @throws UnacceptableInitialTerminationTimeFault
     */
    private long parseTermTime(String termTime) 
                                throws UnacceptableTerminationTimeFault{
        /* Parsing initial termination time since Axis2 does not like unions */
        long timestamp = 0L;
        if(termTime == null){
            this.log.debug("termTime=default");
        }else if(termTime.startsWith("P")){
            //duration
            this.log.debug("termTime=xsd:duration");
            try{
                DatatypeFactory dtFactory = DatatypeFactory.newInstance();
                Duration dur = dtFactory.newDuration(termTime);
                GregorianCalendar cal = new GregorianCalendar();
                dur.addTo(cal);
                timestamp = (cal.getTimeInMillis()/1000L);
            }catch(Exception e){
                throw new UnacceptableTerminationTimeFault("InitialTerminationTime " +
                    "appears to be an invalid xsd:duration value.");
            }
        }else{
            //datetime or invalid
            this.log.debug("termTime=xsd:datetime");
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
            try{
                Date date = df.parse(termTime);
                timestamp = (date.getTime()/1000L);
            }catch(Exception e){
                throw new UnacceptableTerminationTimeFault("InitialTerminationTime " +
                    "must be of type xsd:datetime or xsd:duration.");
            }
        }
        
        return timestamp;
    }
    
    /**
     * Verify a subscription ID has all the required values and maps to subscription manager URL
     * 
     * @param subRef the SubscriptioNReference to verify
     * @return the subscription ID
     * @throws ResourceUnknownFault
     */
    private String verifySubscriptionRef(EndpointReferenceType subRef) throws ResourceUnknownFault{
        
        /* Find subscription ID */
        ReferenceParametersType refParams = subRef.getReferenceParameters(); 
        if(refParams == null){ 
            throw new ResourceUnknownFault("Could not find subscription." +
                                        "No subscription reference provided.");
        }
        String subscriptionId = refParams.getSubscriptionId();
        if(subscriptionId == null){
            throw new ResourceUnknownFault("Could not find subscription." +
                                           "No subscription ID provided.");
        }
        
        /* Verify they want to be talking to this NB */
        String address = this.parseEPR(subRef);
        if(!this.subscriptionManagerURL.equals(address)){
            throw new ResourceUnknownFault("Could not find subscription." +
                  "Invalid subcription manager address. This notification" +
                  " broker requires you to use address " + 
                   this.subscriptionManagerURL);
        }
        
        return subscriptionId;
    }
    
    /**
     * Converts an Axis2 OMElement to a JDOM element.
     * 
     * @param omElems the axis2 elements to converts
     * @return a list of Elements for each OMElement given
     * @throws RemoteException
     */
    public List<Element> axiom2Jdom(OMElement[] omElems) throws RemoteException{
        List<Element> jdomElems = new ArrayList<Element>();
        for(OMElement omElem : omElems){
            try {
                Element jdomElem = null;
                StringWriter sw = new StringWriter();
                XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
                MTOMAwareXMLSerializer mtom = new MTOMAwareXMLSerializer(writer);
                omElem.serialize(writer);
                mtom.flush();
                SAXBuilder builder = new SAXBuilder();
                StringReader sr = new StringReader(sw.toString());
                Document doc = builder.build(sr);
                jdomElem = doc.getRootElement();
                jdomElems.add(jdomElem);
            } catch (Exception e) {
                this.log.error(e.getMessage());
                throw new RemoteException("Unable to convert Notify message to JDOM object");
            }
        }
        
        return jdomElems;
    }
}
