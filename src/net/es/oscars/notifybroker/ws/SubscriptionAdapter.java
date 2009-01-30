package net.es.oscars.notifybroker.ws;

import java.util.*;
import java.net.InetAddress;
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
import org.hibernate.*;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import org.quartz.JobDetail;
import org.quartz.JobDataMap;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import net.es.oscars.client.Client;
import net.es.oscars.PropHandler;
import net.es.oscars.notifybroker.OSCARSNotifyCore;
import net.es.oscars.notifybroker.SubscriptionManager;
import net.es.oscars.notifybroker.db.Publisher;
import net.es.oscars.notifybroker.db.Subscription;
import net.es.oscars.notifybroker.db.SubscriptionFilter;
import net.es.oscars.notifybroker.jobs.ProcessNotifyJob;
import net.es.oscars.notifybroker.jobs.SendNotifyJob;
import net.es.oscars.notifybroker.policy.NotifyPEP;

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
    private String dbname;
    private String repo;
    private OSCARSNotifyCore core;
    private SubscriptionManager sm;
    
    /** Default constructor */
    public SubscriptionAdapter(String dbname){
        this.log = Logger.getLogger(this.getClass());
        this.core = OSCARSNotifyCore.getInstance();
        this.dbname = dbname;
        String catalinaHome = System.getProperty("catalina.home");
        // check for trailing slash
        if (!catalinaHome.endsWith("/")) {
            catalinaHome += "/";
        }
        this.repo = catalinaHome + "shared/classes/repo/";
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("notify.ws.broker", true); 
        this.subscriptionManagerURL = props.getProperty("url");
        this.sm = new SubscriptionManager(this.dbname);
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
        Subscription subscription = this.axis2Subscription(request, userLogin);
        SubscribeResponse response = null;
        ArrayList<SubscriptionFilter> filters = new ArrayList<SubscriptionFilter>();
        FilterType requestFilter = request.getFilter();
        QueryExpressionType[] producerPropsFilters = requestFilter.getProducerProperties();
        QueryExpressionType[] messageContentFilters =  requestFilter.getMessageContent();
        TopicExpressionType[] topicFilters = requestFilter.getTopicExpression();
        
        /* Add filters */
        for(String permission : permissionMap.keySet()){
            filters.add(new SubscriptionFilter(permission, permissionMap.get(permission)));
        }

        /* TODO: Add constraint on which producers can be seen. It should be 
           handled in a similar way as above where a producer or list of 
           producers is passed in a HashMap */
        producerPropsFilters = (producerPropsFilters == null) ? new QueryExpressionType[0] : producerPropsFilters;
        for(QueryExpressionType producerPropsFilter : producerPropsFilters){
            if(this.validateQueryExpression(producerPropsFilter, true)){
                String xpath = producerPropsFilter.getString();
                filters.add(new SubscriptionFilter("PRODXPATH", xpath));    
            }
        }
        
        messageContentFilters = (messageContentFilters == null) ? new QueryExpressionType[0] : messageContentFilters;
        for(QueryExpressionType messageContentFilter : messageContentFilters){
            if(this.validateQueryExpression(messageContentFilter, false)){
                String xpath = messageContentFilter.getString();
                filters.add(new SubscriptionFilter("MSGXPATH", xpath));    
            }
        }
        
        ArrayList<String> topics = this.parseTopics(topicFilters);
        for(String topic : topics){
             filters.add(new SubscriptionFilter("TOPIC", topic.trim()));
        }
        
        Session sess = this.core.getNotifySession();
        sess.beginTransaction();
        try{
            subscription = this.sm.subscribe(subscription, filters);
            response = this.subscription2Axis(subscription);
        }catch(UnacceptableInitialTerminationTimeFault e){
            sess.getTransaction().rollback();
            throw e;
        }
        sess.getTransaction().commit();
        
        this.log.info("subscribe.end");
        return response;
    }
    
    /**
     * Adds a ProcessNotifyJob to the scheduler so that the caller is free to return
     * an HTTP 200 response to publisher of this notifcation without waiting for
     * all the send messages to be sent to subscribers.
     *
     * @param holder the notification message to send
     * @param permissionMap a map of the permissions required to view this notification
     */
    public void schedProcessNotify(NotificationMessageHolderType holder,
                            HashMap<String, ArrayList<String>> permissionMap){ 
        this.log.info("schedProcessNotify.start");
        Scheduler sched = this.core.getScheduler();
        String triggerName = "processNotifyTrig-" + holder.hashCode();
        String jobName = "processNotify-" + holder.hashCode();
        SimpleTrigger trigger = new SimpleTrigger(triggerName, null, 
                                                  new Date(), null, 0, 0L);
        JobDetail jobDetail = new JobDetail(jobName, "PROCESS_NOTIFY",
                                            ProcessNotifyJob.class);
        JobDataMap dataMap = new JobDataMap();
        dataMap.put("permissionMap", permissionMap);
        dataMap.put("message", holder);
        jobDetail.setJobDataMap(dataMap);   
        try{
            this.log.debug("Adding job " + jobName);
            sched.scheduleJob(jobDetail, trigger);
            this.log.debug("Job added.");
        }catch(SchedulerException ex){
            this.log.error("Scheduler exception: " + ex);    
        }
        this.log.info("schedProcessNotify.end");
    }
    
    /**
     * Forwards notfications to appropriate subscribers.
     *
     * @param holder the notification message to send
     * @param permissionMap a map of the permissions required to view this notification
     */
    public void notify(NotificationMessageHolderType holder,
                       HashMap<String, ArrayList<String>> permissionMap,
                       ArrayList<NotifyPEP> notifyPEPs)
                       throws ADBException,
                              InvalidTopicExpressionFault,
                              JaxenException,
                              TopicExpressionDialectUnknownFault{
        this.log.info("notify.start");
        TopicExpressionType topicExpr = holder.getTopic();
        TopicExpressionType[] topicExprs = {topicExpr};
        ArrayList<String>topics = this.parseTopics(topicExprs);
        ArrayList<String> parentTopics = new ArrayList<String>();
        List<Subscription> authSubscriptions = null;
        EndpointReferenceType producerRef = holder.getProducerReference();
        MessageType message = holder.getMessage();
        SimpleNamespaceContext nsContext= new SimpleNamespaceContext(this.namespaces);
        OMFactory omFactory = (OMFactory) OMAbstractFactory.getOMFactory();
        OMElement omProducerRef = null;
        OMElement omMessage = message.getOMElement(NotificationMessage.MY_QNAME, omFactory);
        if(producerRef != null){
            omProducerRef = producerRef.getOMElement(NotificationMessage.MY_QNAME, omFactory);
        }
        
        //add all parent topics
        for(String topic : topics){
            String[] topicParts = topic.split("\\/");
            String topicString = "";
            for(int i = 0; i < (topicParts.length - 1); i++){
                topicString += topicParts[i];
                parentTopics.add(topicString);
                topicString += "/";
            }
        }
        topics.addAll(parentTopics);
        permissionMap.put("TOPIC", topics);
        
        /* find all subscriptions that match this topic, have the necessary
           authorizations on the Notification resource, and match user XPATH */
        authSubscriptions = this.sm.findSubscriptions(permissionMap);
        for(Subscription authSubscription : authSubscriptions){ 
            //apply subscriber specified XPATH filters
            this.log.debug("Applying filters for " + authSubscription.getReferenceId());
            Set filters = authSubscription.getFilters();
            Iterator i = filters.iterator();
            boolean matches = true;
            while(i.hasNext() && matches){
                SubscriptionFilter filter = (SubscriptionFilter) i.next();
                String type = filter.getType();
                try{
                    if("PRODXPATH".equals(type) && omProducerRef != null){
                        this.log.debug("Found producer filter: " + filter.getValue());
                        AXIOMXPath xpath = new AXIOMXPath(filter.getValue());
                        xpath.setNamespaceContext(nsContext);
                        matches = xpath.booleanValueOf(omProducerRef);
                        this.log.debug(matches ? "Filter matches." : "No Match");
                    }else if("MSGXPATH".equals(type)){
                        this.log.debug("Found message filter: " + filter.getValue());
                        AXIOMXPath xpath = new AXIOMXPath(filter.getValue());
                        xpath.setNamespaceContext(nsContext);
                        matches = xpath.booleanValueOf(omMessage);
                        this.log.debug(matches ? "Filter matches." : "No Match");
                    }
                }catch(JaxenException e){
                    this.log.error(e);
                    e.printStackTrace();
                    matches = false;
                    break;
                }
            }
            if(matches){
                this.schedSendNotify(holder, authSubscription);
            } 
        }
        this.log.info("notify.end");
    }
    
    /**
     * Adds a SendNotifyJob to the scheduler so that notification sending
     * is threaded rather than doing each serially. This is desirable because
     * one send action is taking a long amount of time it will not hold-up 
     * other messages from sending.
     *
     * @param holder the message to send
     * @param subscription a subscription containing information needed to send the notification
     */
    public void schedSendNotify(NotificationMessageHolderType holder,
                                Subscription subscription){
        this.log.info("schedSendNotify.start");
        NotificationMessageHolderType holderCopy = this.copyHolder(holder);
        Scheduler sched = this.core.getScheduler();
        String triggerName = "sendNotifyTrig-" + holderCopy.hashCode() +
                             ":" + subscription.hashCode();
        String jobName = "sendNotify-" + holderCopy.hashCode() +
                         ":" + subscription.hashCode();
        SimpleTrigger trigger = new SimpleTrigger(triggerName, null, 
                                                  new Date(), null, 0, 0L);
        JobDetail jobDetail = new JobDetail(jobName, "SEND_NOTIFY",
                                            SendNotifyJob.class);
        JobDataMap dataMap = new JobDataMap();
        dataMap.put("url", subscription.getUrl());
        dataMap.put("subRefId", subscription.getReferenceId());
        dataMap.put("message", holderCopy);
        jobDetail.setJobDataMap(dataMap);
        
        try{
            this.log.debug("Adding job " + jobName);
            sched.scheduleJob(jobDetail, trigger);
            this.log.debug("Job added.");
        }catch(SchedulerException ex){
            this.log.error("Scheduler exception: " + ex);    
        }
        
        this.log.info("schedSendNotify.end: subscription=" + 
                       subscription.getReferenceId());
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
        RenewResponse response = new RenewResponse();
        long termTime = this.parseTermTime(request.getTerminationTime());
        long newTermTime = 0L;
        EndpointReferenceType subRef = request.getSubscriptionReference();
        String address = this.parseEPR(subRef);
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
        if(!this.subscriptionManagerURL.equals(address)){
            throw new ResourceUnknownFault("Could not find subscription." +
                  "Invalid subcription manager address. This notification" +
                  " broker requires you to use address " + 
                   this.subscriptionManagerURL);
        }

        Session sess = this.core.getNotifySession();
        sess.beginTransaction();
        try{
            newTermTime = this.sm.renew(subscriptionId, termTime, permissionMap);
        }catch(UnacceptableTerminationTimeFault e){
            sess.getTransaction().rollback();
            throw e;
        }catch(ResourceUnknownFault e){
            sess.getTransaction().rollback();
            throw e;
        }
        
        sess.getTransaction().commit();
        
        /* Convert creation and termination time to Calendar object */
		GregorianCalendar currCal = new GregorianCalendar();
		GregorianCalendar termCal = new GregorianCalendar();
		currCal.setTimeInMillis(System.currentTimeMillis());
		termCal.setTimeInMillis(newTermTime * 1000);
		
        response.setSubscriptionReference(subRef);
        response.setCurrentTime(currCal);
        response.setTerminationTime(termCal);
        
        this.log.info("renew.end");
        return response;
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
        UnsubscribeResponse response = new UnsubscribeResponse();
        Subscription subscription = null;
        EndpointReferenceType subRef = request.getSubscriptionReference();
        String address = this.parseEPR(subRef);
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
        if(!this.subscriptionManagerURL.equals(address)){
            throw new ResourceUnknownFault("Could not find subscription." +
                  "Invalid subcription manager address. This notification" +
                  " broker requires you to use address " + 
                   this.subscriptionManagerURL);
        }

        Session sess = this.core.getNotifySession();
        sess.beginTransaction();
        try{
            if(subscriptionId.equals("ALL")){
                 this.sm.updateStatusAll(SubscriptionManager.INACTIVE_STATUS, subscriptionId, userLogin);
            }else{
                this.sm.updateStatus(SubscriptionManager.INACTIVE_STATUS, subscriptionId, permissionMap);
            }
        }catch(ResourceUnknownFault e){
            sess.getTransaction().rollback();
            throw e;
        }catch(Exception e){
            sess.getTransaction().rollback();
            throw new UnableToDestroySubscriptionFault(e.getMessage());
        }
        sess.getTransaction().commit();
		
        response.setSubscriptionReference(subRef);
        this.log.info("unsubscribe.end");
        return response;
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
        PauseSubscriptionResponse response = new PauseSubscriptionResponse();
        EndpointReferenceType subRef = request.getSubscriptionReference();
        String address = this.parseEPR(subRef);
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
        if(!this.subscriptionManagerURL.equals(address)){
            throw new ResourceUnknownFault("Could not find subscription." +
                  "Invalid subcription manager address. This notification" +
                  " broker requires you to use address " + 
                   this.subscriptionManagerURL);
        }

        Session sess = this.core.getNotifySession();
        sess.beginTransaction();
        try{
            this.sm.updateStatus(SubscriptionManager.PAUSED_STATUS, subscriptionId, permissionMap);
        }catch(ResourceUnknownFault e){
            sess.getTransaction().rollback();
            throw e;
        }catch(Exception e){
            sess.getTransaction().rollback();
            throw new PauseFailedFault(e.getMessage());
        }
        sess.getTransaction().commit();
		
        response.setSubscriptionReference(subRef);
        this.log.info("pause.end");
        return response;
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
        ResumeSubscriptionResponse response = new ResumeSubscriptionResponse();
        EndpointReferenceType subRef = request.getSubscriptionReference();
        String address = this.parseEPR(subRef);
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
        if(!this.subscriptionManagerURL.equals(address)){
            throw new ResourceUnknownFault("Could not find subscription." +
                  "Invalid subcription manager address. This notification" +
                  " broker requires you to use address " + 
                   this.subscriptionManagerURL);
        }

        Session sess = this.core.getNotifySession();
        sess.beginTransaction();
        try{
            this.sm.updateStatus(SubscriptionManager.ACTIVE_STATUS, subscriptionId, permissionMap);
        }catch(ResourceUnknownFault e){
            sess.getTransaction().rollback();
            throw e;
        }catch(Exception e){
            sess.getTransaction().rollback();
            throw new ResumeFailedFault(e.getMessage());
        }
        sess.getTransaction().commit();
		
        response.setSubscriptionReference(subRef);
        this.log.info("resume.end");
        return response;
    }
    
    /**
     * Destroy a PublisherRegistration based on the parameters of the request. 
     * 
     * @param request the Axis2 object with the registration to destroy
     * @param permissionMap a hash containing authorization constraints
     * @return an Axis2 object with the result of the destroy
     * @throws ResourceUnknownFault
     * @throws ResourceNotDestroyedFault
     */
    public DestroyRegistrationResponse destroyRegistration(DestroyRegistration request, 
                               HashMap<String,String> permissionMap)
                               throws ResourceUnknownFault,
                                      ResourceNotDestroyedFault{
        this.log.info("destroyRegistration.start");
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

        Session sess = this.core.getNotifySession();
        sess.beginTransaction();
        try{
            this.sm.destroyRegistration(pubId, permissionMap);
        }catch(ResourceUnknownFault e){
            sess.getTransaction().rollback();
            throw e;
        }catch(ResourceNotDestroyedFault e){
            sess.getTransaction().rollback();
            throw e;
        }catch(Exception e){
            sess.getTransaction().rollback();
            throw new ResourceNotDestroyedFault(e.getMessage());
        }
        sess.getTransaction().commit();
		
        response.setPublisherRegistrationReference(pubRef);
        this.log.info("destroyRegistration.end");
        return response;
    }
    
    /**
     * Validates a publisher given a registration ID
     * 
     * @param epr the endpoint reference containing the publisherRegistrationId
     * @return true if PublisherRegistration is valid, false otherwise
     * @throws ResourceUnknownFault
     */
    public boolean validatePublisherRegistration(EndpointReferenceType epr){
        this.log.info("validatePublisherRegistration.start");
        if(epr == null){
            this.log.error("Could not find registration. No producer reference provided.");
            return false;
        }
        ReferenceParametersType refParams = epr.getReferenceParameters(); 
        if(refParams == null){ 
            this.log.error("Could not find registration. No registration reference provided.");
            return false;
        }
        String pubId = refParams.getPublisherRegistrationId();
        if(pubId == null){
            this.log.error("Could not find registration. No registration ID provided.");
            return false;
        }

        Session sess = this.core.getNotifySession();
        sess.beginTransaction();
        try{
            this.sm.queryPublisher(pubId);
        }catch(ResourceUnknownFault e){
            sess.getTransaction().rollback();
            this.log.error(e.getMessage());
            return false;
        }catch(Exception e){
            sess.getTransaction().rollback();
            this.log.error(e.getMessage());
            e.printStackTrace();
            return false;
        }
        sess.getTransaction().commit();
        this.log.info("validatePublisherRegistration.end");
        return true;
    }
    
    /**
     * Sends a Notify message to a subscriber
     *
     * @param holder the message to send
     * @param url a URL indication where to send the message
     * @param subRefId the ID of the subscription to which the message belongs
     */
    public void sendNotify(NotificationMessageHolderType holder, String url, String subRefId){
        this.log.debug("sendNotify.start");
        Client client = new Client();
        
        try{
            EndpointReferenceType subRef = client.generateEndpointReference(
                   this.subscriptionManagerURL, subRefId);
            holder.setSubscriptionReference(subRef);
            client.setUpNotify(true, url, this.repo, this.repo + "axis2-norampart.xml");
            client.notify(holder);
        }catch(Exception e){
            this.log.info("Error sending notification: " + e);
        }finally{
            client.cleanUp();
        }
        this.log.debug("sendNotify.end");
    }
    
    /**
     * Registers a publisher in the database using the given registration parameters
     *
     * @param request the registration request
     * @param login the login of the user that sent the registration
     * @return an Axis2 RegisterPublisherResponse withthe result of the registration
     * @throws UnacceptableInitialTerminationTimeFault
     * @throws PublisherRegistrationFailedFault
     */
    public RegisterPublisherResponse registerPublisher(RegisterPublisher request, String login)
                                throws UnacceptableInitialTerminationTimeFault,
                                       PublisherRegistrationFailedFault{
        this.log.info("registerPublisher.start");
        RegisterPublisherResponse response = null;
        Publisher publisher = new Publisher();
        String publisherAddress = this.parseEPR(request.getPublisherReference());
        Calendar initTermTime = request.getInitialTerminationTime();
        boolean demand = request.getDemand();
        
        publisher.setUserLogin(login);
        publisher.setUrl(publisherAddress);
        if(initTermTime != null){
            publisher.setTerminationTime(initTermTime.getTimeInMillis()/1000L);
        }else{
            publisher.setTerminationTime(0L);
        }
        
        //TODO:Support demand based publishing
        if(demand){
            throw new PublisherRegistrationFailedFault("Demand publishing is not supported by this implementation.");
        }
        publisher.setDemand(demand);
        
        Session sess = this.core.getNotifySession();
        sess.beginTransaction();
        try{
            publisher = this.sm.registerPublisher(publisher);
            response = this.publisher2Axis(publisher);
        }catch(UnacceptableInitialTerminationTimeFault e){
            sess.getTransaction().rollback();
            throw e;
        }
        this.log.info("registerPublisher.end");
        sess.getTransaction().commit();
        
        return response;
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
        String consumerAddress = this.parseEPR(request.getConsumerReference());
        long initTermTime = 0L;
        try{
            initTermTime = this.parseTermTime(request.getInitialTerminationTime());
        }catch(UnacceptableTerminationTimeFault ex){
            throw new UnacceptableInitialTerminationTimeFault(ex.getMessage());
        }
        
        subscription.setUrl(consumerAddress);
        subscription.setUserLogin(userLogin);
        subscription.setTerminationTime(initTermTime);
        
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
     * Converts a complete Publisher Hibernate bean to an Axis2 object
     *
     * @param publisher the Publisher Hibernate bean to convert
     * @return the Axis2 RegisterPublisherResponse converted from the Publisher object
     */
    private RegisterPublisherResponse publisher2Axis(Publisher publisher){
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
        pubRefParams.setPublisherRegistrationId(publisher.getReferenceId());
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
    
    private NotificationMessageHolderType copyHolder(NotificationMessageHolderType holder){
        NotificationMessageHolderType holderCopy = new NotificationMessageHolderType();
        holderCopy.setTopic(holder.getTopic());
        holderCopy.setProducerReference(holder.getProducerReference());
        holderCopy.setMessage(holder.getMessage());
        
        return holderCopy;
    }
}
