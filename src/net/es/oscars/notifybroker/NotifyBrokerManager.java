package net.es.oscars.notifybroker;

import java.rmi.RemoteException;
import java.util.*;

import org.apache.log4j.*;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.*;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;

import net.es.oscars.PropHandler;
import net.es.oscars.aaa.AuthValue;
import net.es.oscars.notifybroker.ws.UnacceptableInitialTerminationTimeFault;
import net.es.oscars.notifybroker.ws.UnacceptableTerminationTimeFault;
import net.es.oscars.notifybroker.ws.ResourceUnknownFault;
import net.es.oscars.notifybroker.db.Publisher;
import net.es.oscars.notifybroker.db.PublisherDAO;
import net.es.oscars.notifybroker.db.Subscription;
import net.es.oscars.notifybroker.db.SubscriptionDAO;
import net.es.oscars.notifybroker.db.SubscriptionFilter;
import net.es.oscars.notifybroker.db.SubscriptionFilterDAO;
import net.es.oscars.notifybroker.jdom.ProducerReference;
import net.es.oscars.notifybroker.jobs.ProcessNotifyJob;
import net.es.oscars.notifybroker.jobs.SendNotifyJob;
import net.es.oscars.rmi.notifybroker.xface.*;

/**
 * Core class for managing publishers subscriptions and notifications. Class
 * adds/remove publishers to the database. It creates, modifies and deletes
 * subscriptions from the database. It also matches subscriptions to incoming
 * notifications and schedules the delivery of those notifications. 
 * 
 * @author Andrew Lake (alake@internet2.edu)
 */
public class NotifyBrokerManager{
    private Logger log;
    private long subMaxExpTime;
    private long pubMaxExpTime;
    private String dbname;
    private HashMap<String,String> namespaces;
    
    public static int INACTIVE_STATUS = 0;
    public static int ACTIVE_STATUS = 1;
    public static int PAUSED_STATUS = 2;
    
    
    /**
     * Loads properties from oscars.properties and initializes variables.
     * 
     * @param dbname the location of the 'notify' database
     */
    public NotifyBrokerManager(String dbname){
        this.log = Logger.getLogger(this.getClass());
        this.dbname = dbname;
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("notifybroker", true); 
        try{
            this.subMaxExpTime = Long.parseLong(props.getProperty("subscriptions.maxExpireTime"));
        }catch(Exception e){}
        //set to default to one hour if not set
        if(this.subMaxExpTime <= 0){
            this.log.debug("Defaulting to 1 hour expiration for subscriptions.");
            this.subMaxExpTime = 3600;
        }
        try{
            this.pubMaxExpTime = Long.parseLong(props.getProperty("publishers.maxExpireTime"));
        }catch(Exception e){}
        //set to default to one hour if not set
        if(this.pubMaxExpTime <= 0){
            this.log.debug("Defaulting to 12 hour expiration for publisher registrations.");
            this.pubMaxExpTime = 3600*12;
        }
        
        //TODO: Loads namespace prefixes from properties file
        this.namespaces = new HashMap<String,String>();
        this.namespaces.put("idc", "http://oscars.es.net/OSCARS");
        this.namespaces.put("nmwg-ctrlp", "http://ogf.org/schema/network/topology/ctrlPlane/20080828/");
        this.namespaces.put("wsa", "http://www.w3.org/2005/08/addressing");
    }
    
    /**
     * Adds a ProcessNotifyJob to the scheduler so that the caller is free to return
     * an HTTP 200 response to publisher of this notifcation without waiting for
     * all the send messages to be sent to subscribers.
     * 
     * @param publisherUrl the URL of the service that published the notification
     * @param publisherRegId the registration ID of the publisher
     * @param topics the topics to which the notification belongs
     * @param msg the JDOM elements from the Message of the notification
     * @param pepMap the list of policy filters that must match a subscription
     */
    public void schedProcessNotify(String publisherUrl, String publisherRegId, 
            List<String> topics, List<Element> msg, HashMap<String, List<String>> pepMap){ 
        this.log.debug("schedProcessNotify.start");
        String jobKey = msg.hashCode() + System.currentTimeMillis() + "";
        NotifyBrokerCore core = NotifyBrokerCore.getInstance();
        Scheduler sched = core.getScheduler();
        String triggerName = "processNotifyTrig-" + jobKey;
        String jobName = "processNotify-" + jobKey;
        SimpleTrigger trigger = new SimpleTrigger(triggerName, null, 
                                                  new Date(), null, 0, 0L);
        JobDetail jobDetail = new JobDetail(jobName, "PROCESS_NOTIFY",
                                            ProcessNotifyJob.class);
        JobDataMap dataMap = new JobDataMap();
        dataMap.put("permissionMap", pepMap);
        dataMap.put("publisherUrl", publisherUrl);
        dataMap.put("topics", topics);
        dataMap.put("message", msg);
        jobDetail.setJobDataMap(dataMap);   
        try{
            this.log.debug("Adding job " + jobName);
            sched.scheduleJob(jobDetail, trigger);
            this.log.debug("Job added.");
        }catch(SchedulerException ex){
            this.log.error("Scheduler exception: " + ex);    
        }
        this.log.debug("schedProcessNotify.end");
    }
    

    /**
     * Forwards notifications to appropriate subscribers.
     * 
     * @param publisherUrl the URL of the service that published the notification
     * @param topics the topics to which the notification belongs
     * @param msg the JDOM elements from the Message of the notification
     * @param permissionMap the list of policy filters that must match a subscription
     * @throws RemoteException
     */
    public void notify(String publisherUrl, List<String> topics,
                       List<Element> msg,
                       HashMap<String,List<String>> permissionMap) throws RemoteException{
        this.log.debug("notify.start");
        ArrayList<String> parentTopics = new ArrayList<String>();
        List<Subscription> authSubscriptions = null;
        
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
        
        //Convert publisher URL to JDOM type so can run XPATH
        ProducerReference prodRef = new ProducerReference();
        prodRef.setAddress(publisherUrl);
        
        /* find all subscriptions that match this topic, have the necessary
           authorizations on the Notification resource, and match user XPATH */
        authSubscriptions = this.findSubscriptions(permissionMap);
        for(Subscription authSubscription : authSubscriptions){ 
            //apply subscriber specified XPATH filters
            this.log.debug("Applying filters for " + authSubscription.getReferenceId());
            Set filters = authSubscription.getFilters();
            Iterator i = filters.iterator();
            String matches = null;
            while(i.hasNext() && matches == null){
                SubscriptionFilter filter = (SubscriptionFilter) i.next();
                String type = filter.getType();
                try{
                    if("PRODXPATH".equals(type)){
                        this.log.debug("Found producer filter: " + filter.getValue());
                        XPath xpath = XPath.newInstance(filter.getValue());
                        for(String nsPrefix : this.namespaces.keySet()){
                            xpath.addNamespace(nsPrefix, this.namespaces.get(nsPrefix));
                        }
                        matches = xpath.valueOf(prodRef.getJdom());
                        this.log.debug(matches != null ? "Filter matches." : "No Match");
                    }else if("MSGXPATH".equals(type)){
                        this.log.debug("Found message filter: " + filter.getValue());
                        XPath xpath = XPath.newInstance(filter.getValue());
                        for(String nsPrefix : this.namespaces.keySet()){
                            xpath.addNamespace(nsPrefix, this.namespaces.get(nsPrefix));
                        }
                        matches = xpath.valueOf(msg);
                        this.log.debug(matches != null ? "Filter matches." : "No Match");
                    }
                }catch(JDOMException e){
                    this.log.error(e);
                    e.printStackTrace();
                    matches = null;
                    break;
                }
            }
            if(matches != null){
                this.schedSendNotify(publisherUrl, topics, msg, authSubscription);
            } 
        }
        this.log.info("notify.end");
    }
    
    /**
     * Adds a SendNotifyJob to the scheduler so that notification sending
     * is threaded rather than doing each serially. This is desirable because
     * if one send action is taking a long amount of time it will not hold-up 
     * other messages from sending.
     *
     * @param publisherUrl the URL of the service that published the notification
     * @param topics the topics to which the notification belongs
     * @param msg the JDOM elements from the Message of the notification
     * @param subscription the matching subscription that will receive the notification
     */
    public void schedSendNotify(String publisherUrl, List<String> topics, 
            List<Element> msg, Subscription subscription){
        this.log.debug("schedSendNotify.start");
        NotifyBrokerCore core = NotifyBrokerCore.getInstance();
        Scheduler sched = core.getScheduler();
        String jobKey = subscription.hashCode() + "" + System.currentTimeMillis();
        String triggerName = "sendNotifyTrig-" + jobKey;
        String jobName = "sendNotify-" + jobKey;
        SimpleTrigger trigger = new SimpleTrigger(triggerName, null, 
                                                  new Date(), null, 0, 0L);
        JobDetail jobDetail = new JobDetail(jobName, "SEND_NOTIFY",
                                            SendNotifyJob.class);
        JobDataMap dataMap = new JobDataMap();
        dataMap.put("url", subscription.getUrl());
        dataMap.put("subRefId", subscription.getReferenceId());
        dataMap.put("message", msg);
        dataMap.put("publisherUrl", publisherUrl);
        dataMap.put("topics", topics);
        jobDetail.setJobDataMap(dataMap);
        
        try{
            this.log.debug("Adding job " + jobName);
            sched.scheduleJob(jobDetail, trigger);
            this.log.debug("Job added.");
        }catch(SchedulerException ex){
            this.log.error("Scheduler exception: " + ex);    
        }
        
        this.log.debug("schedSendNotify.end: subscription=" + 
                       subscription.getReferenceId());
    }
    
    /**
     * Creates a subscription entry in the database
     * 
     * @param consumerUrl the URL where matching notifications are sent
     * @param termTime the suggested time the subscription will expire
     * @param filters the matching subscription filters
     * @param user the login of the user creating the subscription
     * @return an RMI response with the subscription ID and termination time
     * @throws UnacceptableInitialTerminationTimeFault
     */
    public RmiSubscribeResponse subscribe(String consumerUrl, Long termTime, 
            HashMap<String,List<String>> filters, String user)
            throws UnacceptableInitialTerminationTimeFault{
        this.log.debug("subscribe.start");
        RmiSubscribeResponse response = new RmiSubscribeResponse();
        SubscriptionDAO dao = new SubscriptionDAO(this.dbname);
        SubscriptionFilterDAO filterDAO = new SubscriptionFilterDAO(this.dbname);
        if(termTime == null){ termTime = 0L; }
        long curTime = System.currentTimeMillis()/1000;
        long expTime = this.checkTermTime(curTime, termTime);
        Subscription subscription = new Subscription();
        
        /* Save subscription */
        subscription.setReferenceId(this.generateId());
        subscription.setUrl(consumerUrl);
        subscription.setUserLogin(user);
        subscription.setCreatedTime(new Long(curTime));
        subscription.setTerminationTime(new Long(expTime));
        subscription.setStatus(NotifyBrokerManager.ACTIVE_STATUS);
        dao.create(subscription);
        
        /* Save filters */
        for(String filterType : filters.keySet()){
            for(String filterValue : filters.get(filterType)){
                SubscriptionFilter filter = new SubscriptionFilter();
                filter.setType(filterType);
                filter.setValue(filterValue);
                filter.setSubscription(subscription);
                filterDAO.create(filter);
            }
        }
        
        /* Create return type */
        response.setSubscriptionId(subscription.getReferenceId());
        response.setTerminationTime(expTime);
        response.setCreatedTime(curTime);
        
        this.log.debug("subscribe.finish");
        
        return response;
    }
    
    /**
     * Adds a publisher to the database
     * 
     * @param publisherUrl the URL that identifies the publisher
     * @param demand true if this is a demand publisher (see WSN spec)
     * @param termTime the suggested termination time of the publisher registration
     * @param user the login of the user registering the publisher
     * @return the publisher registration ID
     * @throws RemoteException
     */
    public String registerPublisher(String publisherUrl, Boolean demand,
            Long termTime, String user) throws RemoteException{
        this.log.debug("registerPublisher.start");
        PublisherDAO dao = new PublisherDAO(this.dbname);
        Publisher publisher = new Publisher();
        long curTime = System.currentTimeMillis()/1000;
        long expTime = curTime + this.pubMaxExpTime;
        
        /* Save registration */
        publisher.setUserLogin(user);
        publisher.setUrl(publisherUrl);
        publisher.setDemand(false);
        publisher.setReferenceId(this.generateId());
        publisher.setCreatedTime(curTime);
        if(termTime != null && termTime <= expTime && termTime >= curTime){
            publisher.setTerminationTime(termTime);
        }else if(termTime != null){
            throw new RemoteException("Requested termination must be " +
                    "greater than the current time and less than " + 
                    this.pubMaxExpTime/3600 + " hours in the future." );
        }else{
            publisher.setTerminationTime(expTime);
        }
        publisher.setStatus(NotifyBrokerManager.ACTIVE_STATUS);
        dao.create(publisher);
        this.log.debug("registerPublisher.finish");
        
        return publisher.getReferenceId();
    }
    
    /**
     * Sets a publisher as INACTIVE in the database
     * 
     * @param pubRefId the publisher registration ID
     * @param user the login of the user requesting the destruction
     * @param authVal the auth value of the requester
     * @throws RemoteException
     */
    public void destroyRegistration(String pubRefId, String user, AuthValue authVal) 
                                throws RemoteException{
        this.log.debug("destroyRegistration.start");
        PublisherDAO dao = new PublisherDAO(this.dbname);
        String modifyConstraint = null;
        if(authVal.equals(AuthValue.SELFONLY)){
            modifyConstraint = user;
        }
        Publisher publisher = dao.queryByRefId(pubRefId, modifyConstraint, false);
        
        if(publisher == null){
            this.log.error("Publisher not found: id=" + pubRefId +", user=" + 
                    user + ", modifyConstraint=" + modifyConstraint);
            throw new RemoteException("Publisher " + pubRefId + " not found.");
        }
        
        if(publisher.getStatus() != NotifyBrokerManager.ACTIVE_STATUS){
            throw new RemoteException("Registration has already been destroyed.");
        }
        
        publisher.setStatus(NotifyBrokerManager.INACTIVE_STATUS);
        dao.update(publisher);
        this.log.debug("destroyRegistration.end");
    }
    
    /**
     * Returns the publisher given a publisher registration ID
     * 
     * @param pubRefId the publisher registration ID
     * @throws ResourceUnknownFault
     */
    public void queryPublisher(String pubRefId) 
                                throws ResourceUnknownFault{
        this.log.debug("queryPublisher.start");
        PublisherDAO dao = new PublisherDAO(this.dbname);
        Publisher publisher = dao.queryByRefId(pubRefId, null, true);
        if(publisher == null){
            this.log.error("Publisher registration not found: id=" + pubRefId);
            throw new ResourceUnknownFault("PublisherRegistration " + 
                                            pubRefId + " not found.");
        }
        this.log.debug("queryPublisher.end");
    }
    
    /**
     * Finds all subscriptions that match a particular set of policy filters
     * 
     * @param permissionMap the set of policy filters to match
     * @return the list of matching subscriptions
     */
    public List<Subscription> findSubscriptions(HashMap<String, List<String>> permissionMap){
        this.log.debug("findSubscriptions.start");
        
        SubscriptionDAO dao = new SubscriptionDAO(this.dbname);
        List<Subscription> subscriptions = dao.getAuthorizedSubscriptions(permissionMap);
        if(subscriptions == null || subscriptions.isEmpty()){
            this.log.debug("No matching subscriptions found");
        }else{
            this.log.debug("Matching subscriptions found");
            for(Subscription subscription : subscriptions){
                this.log.debug("Matched subscription: " + subscription.getReferenceId());
            }
        }
        
        return subscriptions;
    }
    
    /**
     * Extends the termination time of a subscription in the database.
     * 
     * @param subRefId the subscription ID
     * @param termTime the suggested new termination time
     * @param user the user renewing the subscription
     * @param authVal the Auth value of the user on subscriptions
     * @return the new termination time
     * @throws RemoteException
     * @throws UnacceptableTerminationTimeFault
     */
    public Long renew(String subRefId, Long termTime,String user, AuthValue authVal)
                      throws RemoteException,
                             UnacceptableTerminationTimeFault{
        this.log.debug("renew.start");
        SubscriptionDAO dao = new SubscriptionDAO(this.dbname);
        String modifyConstraint = null;
        if(authVal.equals(AuthValue.SELFONLY)){
            modifyConstraint = user;
        }
        Subscription subscription = dao.queryByRefId(subRefId, modifyConstraint);
        if(termTime == null){ termTime = 0L; }
        long curTime = System.currentTimeMillis()/1000;
        long expTime = 0L;
        
        // make sure matching subscription found
        if(subscription == null){
            this.log.error("Subscription not found: id=" + subRefId +
                          ", user=" + user + ", modifyConstraint=" + 
                          modifyConstraint);
            throw new RemoteException("Subscription " + subRefId + " not found.");
        }
        
        if(subscription.getStatus() != NotifyBrokerManager.ACTIVE_STATUS){
            throw new RemoteException("Subscription " + subRefId + " cannot " +
                        "be renewed because it has already been unsubscribed");
        }
        
        //check time
        try{
            expTime = this.checkTermTime(curTime, termTime);
            subscription.setTerminationTime(expTime);
            dao.update(subscription);
        }catch(UnacceptableInitialTerminationTimeFault ex){
            throw new UnacceptableTerminationTimeFault(ex.getMessage());
        }
        this.log.debug("renew.end");
        
        return subscription.getTerminationTime();
    }
    
    /**
     * Updates the status of a subscription. Used by Unsubscribe, Pause, and 
     * Resume to change the state of a subscription.
     * 
     * @param newStatus the new status of the subscription
     * @param subRefId the id of the subscription to update
     * @param user the user requesting the update
     * @param authVal the Auth value of the user requesting the update
     * @throws Exception
     * @throws ResourceUnknownFault
     */
    public synchronized void updateStatus(int newStatus, String subRefId, 
                      String user, AuthValue authVal) 
                      throws Exception, ResourceUnknownFault{
        this.log.debug("updateStatus.start");
        SubscriptionDAO dao = new SubscriptionDAO(this.dbname);
        String modifyLoginConstraint = null;
        if(authVal.equals(AuthValue.SELFONLY)){
            modifyLoginConstraint = user;
        }
        Subscription subscription = dao.queryByRefId(subRefId, modifyLoginConstraint);
        long curTime = System.currentTimeMillis()/1000;
        if(subscription == null){
            this.log.error("Subscription not found: id=" + subRefId +
                          ", user=" + modifyLoginConstraint);
            throw new ResourceUnknownFault("Subscription " + subRefId + " not found.");
        }else if(subscription.getTerminationTime() <= curTime){
            throw new Exception("Subscription has expired and cannot be modified.");
        }
        int curStatus = subscription.getStatus();
        
        if(newStatus == NotifyBrokerManager.ACTIVE_STATUS && curStatus != PAUSED_STATUS){
            throw new Exception("Trying to resume a subscription that is not paused");
        }else if(newStatus == NotifyBrokerManager.PAUSED_STATUS && curStatus != ACTIVE_STATUS){
            throw new Exception("Trying to pause a subscription that is not active");
        }else if(newStatus == NotifyBrokerManager.INACTIVE_STATUS && curStatus == INACTIVE_STATUS){
            //Unsubscribe any state that is not already unsubscribed
            throw new Exception("Trying to unsubscribe a reservation " +
                                        "that is already cancelled");
        }
        subscription.setStatus(newStatus);
        dao.update(subscription);
        this.log.debug("updateStatus.end=" + newStatus);
    }
    
    /**
     * Updates the status of multiple subscriptions at one time. Useful for canceling all subscriptions.
     * 
     * @param newStatus the new status of the subscriptions
     * @param subRefId the subscription ID
     * @param user the login of the user requesting the change
     * @throws Exception
     * @throws ResourceUnknownFault
     */
    public synchronized void updateStatusAll(int newStatus, String subRefId, 
                      String user) throws Exception, ResourceUnknownFault{
        this.log.debug("updateStatusAll.start");
        SubscriptionDAO dao = new SubscriptionDAO(this.dbname);
        List<Subscription> subscriptions = dao.getAllActiveForUser(user);
        if(subscriptions == null){
            //if not found just exit
            return;
        }
        for(Subscription subscription : subscriptions){
            subscription.setStatus(newStatus);
            dao.update(subscription);
        }
        this.log.debug("updateStatusAll.end=" + newStatus);
    }
    
    /**
     * Checks a suggested termination against the allowed time
     * 
     * @param curTime the current time
     * @param expTime the suggested termination time
     * @return the suggested termination time if less than maximum allowed. if 0 given then set to max time.
     * @throws UnacceptableInitialTerminationTimeFault
     */
    private long checkTermTime(long curTime, long expTime) 
                                throws UnacceptableInitialTerminationTimeFault{
        /* calculate initial termination time */
        long maxTime = curTime + this.subMaxExpTime;
        if(expTime == 0){
            expTime = maxTime;
        }else if(expTime > maxTime){
            throw new UnacceptableInitialTerminationTimeFault("Requested " +
                    "termination time is too far in the future. " +
                    "The maximum is " + this.subMaxExpTime + " seconds from the " +
                    "current time.");
        }
        
        return expTime;
    }
    
    /**
     * Generates id conforming to RFC 4122 "A Universally Unique IDentifier (UUID) URN Namespace"
     *
     * @return the randomly generated UUID.
     */
    public String generateId(){
        return "urn:uuid:" + UUID.randomUUID().toString();
    }
}