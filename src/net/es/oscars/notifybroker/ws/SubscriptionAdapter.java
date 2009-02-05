package net.es.oscars.notifybroker.ws;

import java.util.*;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import org.apache.log4j.*;
import org.oasis_open.docs.wsn.b_2.*;
import org.oasis_open.docs.wsn.br_2.*;
import org.w3.www._2005._08.addressing.*;
import org.apache.axis2.databinding.ADBException;
import org.apache.axis2.databinding.types.URI;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;

import net.es.oscars.PropHandler;
import net.es.oscars.rmi.notifybroker.NotifyRmiClient;
import net.es.oscars.rmi.notifybroker.NotifyRmiInterface;

/** 
 * SubscriptionAdapter provides a translation layer between Axis2 and Hibernate. 
 * It is intended to provide a gateway for Axis2 into more general core functionality
 * of the notification broker.
 *
 * @author Andrew Lake (alake@internet2.edu)
 */
public class SubscriptionAdapter{
    private Logger log;
    private String subscriptionManagerURL;
    private HashMap<String,String> namespaces;
    private String repo;
    
    /** Default constructor */
    public SubscriptionAdapter(){
        this.log = Logger.getLogger(this.getClass());
        String catalinaHome = System.getProperty("catalina.home");
        // check for trailing slash
        if (!catalinaHome.endsWith("/")) {
            catalinaHome += "/";
        }
        this.repo = catalinaHome + "shared/classes/repo/";
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
     * Creates a new subscription based on the parameters of the request. It also adds
     * entries in its database to make sure the subscriber only gets notifications it is
     * authorized to see. 
     * 
     * @param request the Axis2 object with the Subscribe request information
     * @param userLogin the login of the subscriber
     * @param permissionMap A hash containing certain authorization constraints for the subscriber
     * @return an Axis2 object with the result of the subscription creation
     * @throws InvalidFilterFault
     * @throws InvalidMessageContentExpressionFault
     * @throws InvalidProducerPropertiesExpressionFault
     * @throws InvalidTopicExpressionFault
     * @throws TopicExpressionDialectUnknownFault
     * @throws TopicNotSupportedFault
     * @throws UnacceptableInitialTerminationTimeFault
     */
    public SubscribeResponse subscribe(Subscribe request, String userLogin, HashMap<String,String> permissionMap)
        throws TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault,TopicNotSupportedFault,
               InvalidProducerPropertiesExpressionFault,InvalidFilterFault,InvalidMessageContentExpressionFault,
               UnacceptableInitialTerminationTimeFault{
        this.log.info("subscribe.start");
        this.log.info("subscribe.end");
        return null;
    }
    
    /**
     * Renew a  subscription based on the parameters of the request. It also updates
     * entries in its database to make sure the subscriber only gets notifications it is
     * authorized to see.
     * 
     * @param request the Axis2 object with the Renew request information
     * @param permissionMap a hash containing certain authorization constraints for the renewer
     * @return an Axis2 object with the result of the renewal
     * @throws AAAFaultMessage
     * @throws ResourceUnknownFault
     * @throws UnacceptableInitialTerminationTimeFault
     */
    public RenewResponse renew(Renew request, HashMap<String,String> permissionMap)
                               throws AAAFaultMessage, 
                                      ResourceUnknownFault,
                                      UnacceptableTerminationTimeFault{
        this.log.info("renew.start");
        this.log.info("renew.end");
        return null;
    }
    
     /**
     * Cancel a subscription based on the parameters of the request. 
     * 
     * @param request the Axis2 object with the Unsubscribe request information
     * @param permissionMap a hash containing authorization constraints
     * @return an Axis2 object with the result of the unsubscribe
     * @throws AAAFaultMessage
     * @throws ResourceUnknownFault
     * @throws UnableToDestroySubscriptionFault
     */
    public UnsubscribeResponse unsubscribe(Unsubscribe request, String userLogin,
                               HashMap<String,String> permissionMap)
                               throws AAAFaultMessage, 
                                      ResourceUnknownFault,
                                      UnableToDestroySubscriptionFault{
        this.log.info("unsubscribe.start");
        this.log.info("unsubscribe.end");
        return null;
    }
    
    /**
     * Pause a subscription based on the parameters of the request. Pausing
     * suspends the sending of notifications until the subscription is resumed.
     * 
     * @param request the Axis2 object with the Pause request information
     * @param permissionMap a hash containing authorization constraints
     * @return an Axis2 object with the result of the pause
     * @throws AAAFaultMessage
     * @throws ResourceUnknownFault
     * @throws PauseFailedFault
     */
    public PauseSubscriptionResponse pause(PauseSubscription request, 
                               HashMap<String,String> permissionMap)
                               throws AAAFaultMessage, 
                                      ResourceUnknownFault,
                                      PauseFailedFault{
        this.log.info("pause.start");
        this.log.info("pause.end");
        return null;
    }
    
    /**
     * Resume a paused subscription.
     * 
     * @param request the Axis2 object with the Resume request information
     * @param permissionMap a hash containing authorization constraints
     * @return an Axis2 object with the result of the resume
     * @throws AAAFaultMessage
     * @throws ResourceUnknownFault
     * @throws ResumeFailedFault
     */
    public ResumeSubscriptionResponse resume(ResumeSubscription request, 
                               HashMap<String,String> permissionMap)
                               throws AAAFaultMessage, 
                                      ResourceUnknownFault,
                                      ResumeFailedFault{
        this.log.info("resume.start");
        this.log.info("resume.end");
        return null;
    }
    
    /**
     * Destroy a PublisherRegistration based on the parameters of the request. 
     * 
     * @param request the Axis2 object with the registration to destroy
     * @param permissionMap a hash containing authorization constraints
     * @return an Axis2 object with the result of the destroy
     * @throws ResourceUnknownFault
     * @throws ResourceNotDestroyedFault
     * @throws RemoteException 
     */
    public DestroyRegistrationResponse destroyRegistration(DestroyRegistration request, 
            String login) throws ResourceUnknownFault, ResourceNotDestroyedFault, RemoteException{
        
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
        nbRmiClient.destroyRegistration(pubId, login);
        response.setPublisherRegistrationReference(pubRef);
        
        this.log.debug("destroyRegistration.end");
        
        return response;
    }
    
    /**
     * Registers a publisher in the database using the given registration parameters
     *
     * @param request the registration request
     * @param login the login of the user that sent the registration
     * @return an Axis2 RegisterPublisherResponse with the result of the registration
     * @throws UnacceptableInitialTerminationTimeFault
     * @throws PublisherRegistrationFailedFault
     * @throws RemoteException 
     */
    public RegisterPublisherResponse registerPublisher(RegisterPublisher request, String login)
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
                null, demand, termTime, login);
        response = this.createRegisterPublisherResponse(registrationId);
        this.log.debug("registerPublisher.end");
        
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
            AXIOMXPath axiomXpath = new AXIOMXPath(xpath);
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
}
