package net.es.oscars.notify.ws;

import java.util.*;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import org.apache.log4j.*;
import org.oasis_open.docs.wsn.b_2.*;
import org.w3.www._2005._08.addressing.*;
import org.apache.axis2.databinding.types.URI;
import org.apache.axiom.om.xpath.AXIOMXPath;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.notify.*;
import net.es.oscars.client.Client;
import net.es.oscars.PropHandler;

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
    
    /** Default constructor */
    public SubscriptionAdapter(){
        this.log = Logger.getLogger(this.getClass());
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("notifybroker", true); 
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
    }
    
    /**
     * Creates a new subscription based on the parameters of the request. It also adds
     * entries in its database to make sure the subscriber only gets notifications it is
     * authorized to see. 
     * 
     * @param request the Axis2 object with the Subscribe request information
     * @userLogin the login of the subscriber
     * @permissionMap A hash containing certain authorization constraints for the subscriber
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
        SubscriptionManager sm = new SubscriptionManager();
        Subscription subscription = this.axis2Subscription(request, userLogin);
        SubscribeResponse response = null;
        ArrayList<SubscriptionFilter> filters = new ArrayList<SubscriptionFilter>();
        FilterType requestFilter = request.getFilter();
        QueryExpressionType producerPropsFilter = null;
        QueryExpressionType messageContentFilter = null;
        TopicExpressionType topicFilter = null;
        if(requestFilter != null){
            producerPropsFilter = requestFilter.getProducerProperties();
            messageContentFilter = requestFilter.getMessageContent();
            topicFilter = requestFilter.getTopicExpression();
        }
        
        /* Add filters */
        if(permissionMap.containsKey("loginConstraint")){
            String constraint = permissionMap.get("loginConstraint");
            filters.add(new SubscriptionFilter("USERLOGIN", constraint));
        }else{
            filters.add(new SubscriptionFilter("USERLOGIN", "ALL"));
        }
        
        if(permissionMap.containsKey("institution")){
            String constraint = permissionMap.get("institution");
            filters.add(new SubscriptionFilter("INSTITUTION", constraint));
        }
        
        /* TODO: Add constraint on which producers can be seen. It should be 
           handled in a similar way as above where a producer or list of 
           producers is passed in a HashMap */
        
        if(this.validateQueryExpression(producerPropsFilter, true)){
            String xpath = producerPropsFilter.getString();
            filters.add(new SubscriptionFilter("PRODXPATH", xpath));    
        }
        
        if(this.validateQueryExpression(messageContentFilter, false)){
            String xpath = messageContentFilter.getString();
            filters.add(new SubscriptionFilter("MSGXPATH", xpath));    
        }
        
        String[] topics = this.parseTopics(topicFilter);
        boolean explicitTopics = false;
        for(String topic : topics){
             filters.add(new SubscriptionFilter("TOPIC", topic.trim()));
             explicitTopics = true;
        }
        if(!explicitTopics){
             filters.add(new SubscriptionFilter("TOPIC", "ALL"));
        }
        
        subscription = sm.subscribe(subscription, filters);
        response = this.subscription2Axis(subscription);
        
        this.log.info("subscribe.end");
        return response;
    }
    
    /**
     * Forwards notfications to appropriate subscribers.
     *
     * @param request the notify message to forward
     */
    public void notify(Notify request){
        SubscriptionManager sm = new SubscriptionManager();
        this.log.info("notify.start");
        System.out.println("This should really do something");
        this.log.info("notify.end");
    }
    
    /** 
     * Converts and Axis2 Subscribe object to a Subsciption Hibernate bean
     *
     * @param the Subscribe object to convert
     * @param userLogin the login of the subscriber that sent the request
     * @return the Hibernate Bean generate from the original request
     */
    private Subscription axis2Subscription(Subscribe request, String userLogin)
                                throws UnacceptableInitialTerminationTimeFault{
        Subscription subscription = new Subscription();
        AttributedURIType consumerAddress = 
            request.getConsumerReference().getAddress();
        URI consumerUri = consumerAddress.getAnyURI();
        String initTermTime = request.getInitialTerminationTime();
        
        subscription.setUrl(consumerUri.toString());
        subscription.setUserLogin(userLogin);
        
        /* Parsing initial termination time since Axis2 does not like unions */
        if(initTermTime == null){
            this.log.debug("initTermTime=default");
            subscription.setTerminationTime(0L);
        }else if(initTermTime.startsWith("P")){
            //duration
            this.log.debug("initTermTime=xsd:duration");
            try{
                DatatypeFactory dtFactory = DatatypeFactory.newInstance();
                Duration dur = dtFactory.newDuration(initTermTime);
                GregorianCalendar cal = new GregorianCalendar();
                dur.addTo(cal);
                subscription.setTerminationTime(cal.getTimeInMillis()/1000L);
            }catch(Exception e){
                throw new UnacceptableInitialTerminationTimeFault("InitialTerminationTime " +
                    "appears to be an invalid xsd:duration value.");
            }
        }else{
            //datetime or invalid
            this.log.debug("initTermTime=xsd:datetime");
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
            try{
                Date date = df.parse(initTermTime);
                subscription.setTerminationTime(date.getTime()/1000L);
            }catch(Exception e){
                throw new UnacceptableInitialTerminationTimeFault("InitialTerminationTime " +
                    "must be of type xsd:datetime or xsd:duration.");
            }
        }
        
        return subscription;
    }
    
    /**
     * Converts a complete Subscription Hibernate bean to an Axis2 object
     *
     * @param subscription the Subscription Hibernate bean to convert
     * @return the Axis2 SubscribeResponse converted from the Subscribe object
     */
    private SubscribeResponse subscription2Axis(Subscription subscription){
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
        subRefParams.setSubscriptionId(subscription.getReferenceId());
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
     * Parse a TopicExpression to make sure it is in a known Dialect.
     * It also splits topics into multiple topics. Currently this method
     * supports the SimpleTopic, ConcreteTopic, and FullTopic(partially)
     * specifications. 
     *
     * @param topicFilter the TopicExpression to parse
     * @return an array of strings containing each Topic
     * @throws InvalidTopicExpressionFault
     * @throws TopicExpressionDialectUnknownFault
     */
    private String[] parseTopics(TopicExpressionType topicFilter) 
            throws TopicExpressionDialectUnknownFault,
                   InvalidTopicExpressionFault{
        if(topicFilter == null){
            return new String[0];
        }
        String dialect = topicFilter.getDialect().toString();
        String topicString = topicFilter.getString();
        
        //check dialect
        if(Client.XPATH_URI.equals(dialect)){
             throw new TopicExpressionDialectUnknownFault("The XPath Topic " +
                        "Expression dialect is not supported at this time.");
        }else if(!(Client.WS_TOPIC_SIMPLE.equals(dialect) || 
                   Client.WS_TOPIC_CONCRETE.equals(dialect) || 
                   Client.WS_TOPIC_FULL.equals(dialect))){
            throw new TopicExpressionDialectUnknownFault("Unknown Topic dialect '" + dialect + "'");
        }
        
        if(topicString == null || "".equals(topicString)){
            throw new InvalidTopicExpressionFault("Empty topic expression given.");
        }
        String[] topics = topicString.split("\\|");
        /* NOTE: Currently the notification broker is neutral as to the 
           type of topics is sends/receives so there is no check to see 
           if a topic is supported. This provides the greatest flexibility
           but doesn't allow the broker to return an error if it knows it
           can never send a notification for a particular topic. As we gain
           more experience we can revisit this fact */
           
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
        if(!Client.XPATH_URI.equals(dialect)){
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
}