package net.es.oscars.notify.ws;

import org.apache.log4j.*;
import org.oasis_open.docs.wsn.b_2.*;
import org.w3.www._2005._08.addressing.*;
import org.apache.axis2.databinding.types.URI;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.notify.*;
import java.util.GregorianCalendar;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.HashMap;

public class SubscriptionAdapter{
    private Logger log;
    private HashMap<String,Boolean> VALID_TOPICS;
    private String MANAGER_URL;
    
    public SubscriptionAdapter(){
        this.log = Logger.getLogger(this.getClass());
        //TODO: Load from properties file
        this.VALID_TOPICS = new HashMap<String,Boolean>();
        this.VALID_TOPICS.put("idc:DEBUG", new Boolean(true));
        this.VALID_TOPICS.put("idc:INFO", new Boolean(true));
        this.VALID_TOPICS.put("idc:ERROR", new Boolean(true));
        this.VALID_TOPICS.put("idc:FATAL", new Boolean(true));
        this.MANAGER_URL =
            "http://anna-lab3.internet2.edu:8080/axis2/services/OSCARSNotify";
    }
    
    public SubscribeResponse subscribe(Subscribe request, String userLogin)
            throws TopicExpressionDialectUnknownFault,
                   InvalidTopicExpressionFault,
                   TopicNotSupportedFault{
        SubscriptionManager sm = new SubscriptionManager();
        Subscription subscription = this.axis2Subscription(request, userLogin);
        ArrayList<SubscriptionFilter> filters = this.axis2Filters(request);
        SubscribeResponse response = null;
        
        subscription = sm.subscribe(subscription, filters);
        response = this.subscription2Axis(subscription);
        
        return response;
    }
    
    private Subscription axis2Subscription(Subscribe request, String userLogin){
        Subscription subscription = new Subscription();
        AttributedURIType consumerAddress = 
            request.getConsumerReference().getAddress();
        URI consumerUri = consumerAddress.getAnyURI();
        
        subscription.setUrl(consumerUri.toString());
        subscription.setUserLogin(userLogin);
        
        return subscription;
    }
    
    private ArrayList<SubscriptionFilter> axis2Filters(Subscribe request) 
            throws TopicExpressionDialectUnknownFault,
                   InvalidTopicExpressionFault,
                   TopicNotSupportedFault{
        ArrayList<SubscriptionFilter> filters = 
            new ArrayList<SubscriptionFilter>();
        FilterType filter = request.getFilter();
        
        if(filter == null){
            filters.add(new SubscriptionFilter("TOPIC", "ALL"));
            return filters;
        }
        
        TopicExpressionType topicExpr = filter.getTopicExpression();
        if(topicExpr == null){
            filters.add(new SubscriptionFilter("TOPIC", "ALL"));
        }else{
            String[] topics = this.parseTopic(topicExpr);
            for(String topic:topics){
                filters.add(new SubscriptionFilter("TOPIC", topic));
            }
        }
        
        QueryExpressionType msgFilter = filter.getMessageContent();
        if(msgFilter == null){
            return filters;
        }
        
        ArrayList<SubscriptionFilter> eventSubFilters = 
            this.parseMessageFilter(msgFilter);
        if(eventSubFilters != null){
            filters.addAll(eventSubFilters);
        }
        
        return filters;
    }
    
    private String[] parseTopic(TopicExpressionType topicExpr) 
            throws TopicExpressionDialectUnknownFault,
                   InvalidTopicExpressionFault,
                   TopicNotSupportedFault{
        URI dialect = topicExpr.getDialect();
        String topicString = topicExpr.getString();
        
        //check dialect
        //TODO: more descriptive fault
        if(!dialect.toString().equals(
            "http://docs.oasis-open.org/wsn/t-1/TopicExpression/Simple")){
            throw new TopicExpressionDialectUnknownFault();
        }
        
        if(topicString == null){
            throw new InvalidTopicExpressionFault();
        }
        topicString = topicString.replaceAll("\\s","");
        String[] topics = topicString.split(",");
        for(String topic:topics){
            if(!this.VALID_TOPICS.containsKey(topic)){
                throw new TopicNotSupportedFault();
            }
        }
        return topics;
    }
    
    private  ArrayList<SubscriptionFilter> parseMessageFilter(
            QueryExpressionType msgFilter){
        ArrayList<SubscriptionFilter> filters = 
            new ArrayList<SubscriptionFilter>();
            
        String[] pathDetailLevel = msgFilter.getPathDetailLevel();
        filters = this.addStringFilter("PATHDETAIL", pathDetailLevel, filters);
        
        String[] userLogin = msgFilter.getUserLogin();
        filters = this.addStringFilter("USERLOGIN", userLogin, filters);
        
        String[] errorCode = msgFilter.getErrorCode();
        filters = this.addStringFilter("ERRORCODE", errorCode, filters);
        
        String[] gri = msgFilter.getGlobalReservationId();
        filters = this.addStringFilter("GRI", gri, filters);
        
        String[] resStatus = msgFilter.getResStatus();
        filters = this.addStringFilter("RESSTATUS", resStatus, filters);
        
        filters = this.addLongFilter("STARTTIME", msgFilter.getStartTime(), filters);
        filters = this.addLongFilter("ENDTIME", msgFilter.getEndTime(), filters);
        
        String description = msgFilter.getDescription();
        filters = this.addStringFilter("DESCRIPTION", description, filters);
        
        String[] linkId = msgFilter.getLinkId();
        filters = this.addStringFilter("LINK", linkId, filters);
        
        int[] vlanTag = msgFilter.getVlanTag();
        for(int i=0; vlanTag != null && i < vlanTag.length; i++){
            filters = this.addStringFilter("VLAN", vlanTag[i] + "", filters);
        }
        
        return filters;
    } 
    
    private ArrayList<SubscriptionFilter> addStringFilter(String type, 
            String value, ArrayList<SubscriptionFilter> filters){
        if(value != null){
            filters.add(new SubscriptionFilter(type, value));
        }
        return filters;
    }
    
    private ArrayList<SubscriptionFilter> addStringFilter(String type, 
            String[] value, ArrayList<SubscriptionFilter> filters){
        for(int i=0; value != null && i < value.length; i++){
            filters = this.addStringFilter(type, value[i], filters);
        }
        return filters;
    }
    
    private ArrayList<SubscriptionFilter> addLongFilter(String type, 
            long value, ArrayList<SubscriptionFilter> filters){
        if(value != 0){
            filters.add(new SubscriptionFilter(type, value + ""));
        }
        return filters;
    }
    
    private SubscribeResponse subscription2Axis(Subscription subscription){
        SubscribeResponse response = new SubscribeResponse();
        
        /* Set subscription reference */
		EndpointReferenceType subRef = new EndpointReferenceType();
        AttributedURIType subAttrUri = new AttributedURIType();
        try{
            URI subRefUri = new URI(this.MANAGER_URL);
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
}