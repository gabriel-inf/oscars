package net.es.oscars.notify;

import java.util.*;
import org.apache.log4j.*;
import net.es.oscars.PropHandler;
import net.es.oscars.notify.ws.UnacceptableInitialTerminationTimeFault;
import net.es.oscars.notify.ws.UnacceptableTerminationTimeFault;
import net.es.oscars.notify.ws.ResourceNotDestroyedFault;
import net.es.oscars.notify.ws.ResourceUnknownFault;

public class SubscriptionManager{
    private Logger log;
    private long subMaxExpTime;
    private long pubMaxExpTime;
    private String dbname;
    
    //Constants
    public static int INACTIVE_STATUS = 0;
    public static int ACTIVE_STATUS = 1;
    public static int PAUSED_STATUS = 2;
    
    
    public SubscriptionManager(String dbname){
        this.log = Logger.getLogger(this.getClass());
        this.dbname = dbname;
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("notifybroker", true); 
        try{
            this.subMaxExpTime = Long.parseLong(props.getProperty("subscriptions.maxExpireTime"));
        }catch(Exception e){}
        //set to default to one hour if not set
        if(this.subMaxExpTime <= 0){
            this.log.info("Defaulting to 1 hour expiration for subscriptions.");
            this.subMaxExpTime = 3600;
        }
        try{
            this.pubMaxExpTime = Long.parseLong(props.getProperty("publishers.maxExpireTime"));
        }catch(Exception e){}
        //set to default to one hour if not set
        if(this.pubMaxExpTime <= 0){
            this.log.info("Defaulting to 12 hour expiration for publisher registrations.");
            this.pubMaxExpTime = 3600*12;
        }
    }
    
    public Subscription subscribe(Subscription subscription, 
                                  ArrayList<SubscriptionFilter> filters)
                                  throws UnacceptableInitialTerminationTimeFault{
        this.log.info("subscribe.start");
        SubscriptionDAO dao = new SubscriptionDAO(this.dbname);
        SubscriptionFilterDAO filterDAO = new SubscriptionFilterDAO(this.dbname);
        long curTime = System.currentTimeMillis()/1000;
        long expTime = this.checkTermTime(curTime, subscription.getTerminationTime());
        
        /* Save subscription */
        subscription.setReferenceId(this.generateId());
        subscription.setCreatedTime(new Long(curTime));
        subscription.setTerminationTime(new Long(expTime));
        subscription.setStatus(SubscriptionManager.ACTIVE_STATUS);
        dao.create(subscription);
        
        /* Save filters */
        for(SubscriptionFilter filter:filters){
            filter.setSubscription(subscription);
            filterDAO.create(filter);
        }
        
        this.log.info("subscribe.finish");
        
        return subscription;
    }
    
    public Publisher registerPublisher(Publisher publisher) 
                                throws UnacceptableInitialTerminationTimeFault{
        this.log.info("registerPublisher.start");
        PublisherDAO dao = new PublisherDAO(this.dbname);
        long curTime = System.currentTimeMillis()/1000;
        long expTime = this.checkTermTime(curTime, publisher.getTerminationTime());
        
        /* Save subscription */
        publisher.setReferenceId(this.generateId());
        publisher.setCreatedTime(new Long(curTime));
        publisher.setTerminationTime(new Long(expTime));
        publisher.setStatus(SubscriptionManager.ACTIVE_STATUS);
        dao.create(publisher);
        this.log.info("registerPublisher.finish");
        
        return publisher;
    }
    
    public void destroyRegistration(String pubRefId, HashMap<String, String> permissionMap) 
                                throws ResourceUnknownFault, ResourceNotDestroyedFault{
        this.log.info("destroyRegistration.start");
        PublisherDAO dao = new PublisherDAO(this.dbname);
        String modifyLoginConstraint = permissionMap.get("modifyLoginConstraint");
        Publisher publisher = dao.queryByRefId(pubRefId, modifyLoginConstraint, false);
        long curTime = System.currentTimeMillis()/1000;
        
        if(publisher == null){
            this.log.error("Publisher not found: id=" + pubRefId +
                          ", user=" + modifyLoginConstraint);
            throw new ResourceUnknownFault("Publisher " + pubRefId + " not found.");
        }else if(publisher.getTerminationTime() <= curTime){
            throw new ResourceNotDestroyedFault("Registration is already expired.");
        }
        
        if(publisher.getStatus() != SubscriptionManager.ACTIVE_STATUS){
            throw new ResourceNotDestroyedFault("Registration has already been destroyed.");
        }
        
        publisher.setStatus(SubscriptionManager.INACTIVE_STATUS);
        dao.update(publisher);
        this.log.debug("destroyRegistration.end");
    }
    
    public void queryPublisher(String pubRefId) 
                                throws ResourceUnknownFault{
        this.log.info("queryPublisher.start");
        PublisherDAO dao = new PublisherDAO(this.dbname);
        Publisher publisher = dao.queryByRefId(pubRefId, null, true);
        if(publisher == null){
            this.log.error("Publisher registration not found: id=" + pubRefId);
            throw new ResourceUnknownFault("PublisherRegistration " + 
                                            pubRefId + " not found.");
        }
        this.log.debug("queryPublisher.end");
    }
    
    public List<Subscription> findSubscriptions(HashMap<String, ArrayList<String>> permissionMap){
        this.log.info("findSubscriptions.start");
        
        SubscriptionDAO dao = new SubscriptionDAO(this.dbname);
        List<Subscription> subscriptions = dao.getAuthorizedSubscriptions(permissionMap);
        if(subscriptions == null || subscriptions.isEmpty()){
            this.log.info("No matching subscriptions found");
        }else{
            this.log.info("Matching subscriptions found");
            for(Subscription subscription : subscriptions){
                this.log.debug("Matched subscription: " + subscription.getReferenceId());
            }
        }
        
        return subscriptions;
    }
    
    public long renew(String subRefId, long termTime, 
                      HashMap<String, String> permissionMap)
                      throws ResourceUnknownFault,
                             UnacceptableTerminationTimeFault{
        this.log.info("renew.start");
        SubscriptionDAO dao = new SubscriptionDAO(this.dbname);
        String modifyLoginConstraint = permissionMap.get("modifyLoginConstraint");
        String loginConstraint = permissionMap.get("loginConstraint");
        Subscription subscription = dao.queryByRefId(subRefId, modifyLoginConstraint);
        SubscriptionFilterDAO filterDAO = new SubscriptionFilterDAO(this.dbname);
        long curTime = System.currentTimeMillis()/1000;
        long expTime = 0L;
        
        // make sure matching subscription found
        if(subscription == null){
            this.log.error("Subscription not found: id=" + subRefId +
                          ", user=" + modifyLoginConstraint);
            throw new ResourceUnknownFault("Subscription " + subRefId + " not found.");
        }
        
        //check time
        try{
            expTime = this.checkTermTime(curTime, termTime);
            subscription.setTerminationTime(expTime);
            dao.update(subscription);
        }catch(UnacceptableInitialTerminationTimeFault ex){
            throw new UnacceptableTerminationTimeFault(ex.getMessage());
        }
        
        //TODO: update filters
        
        this.log.info("renew.end");
        
        return expTime;
    }
    
    public synchronized void updateStatus(int newStatus, String subRefId, 
                      HashMap<String, String> permissionMap) 
                      throws Exception, ResourceUnknownFault{
        this.log.debug("updateStatus.start");
        SubscriptionDAO dao = new SubscriptionDAO(this.dbname);
        String modifyLoginConstraint = permissionMap.get("modifyLoginConstraint");
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
        
        if(newStatus == SubscriptionManager.ACTIVE_STATUS && curStatus != PAUSED_STATUS){
            throw new Exception("Trying to resume a subscription that is not paused");
        }else if(newStatus == SubscriptionManager.PAUSED_STATUS && curStatus != ACTIVE_STATUS){
            throw new Exception("Trying to pause a subscription that is not active");
        }else if(newStatus == SubscriptionManager.INACTIVE_STATUS && curStatus == INACTIVE_STATUS){
            //Unsubscribe any state that is not already unsubscribed
            throw new Exception("Trying to unsubscribe a reservation " +
                                        "that is already cancelled");
        }
        subscription.setStatus(newStatus);
        dao.update(subscription);
        this.log.debug("updateStatus.end=" + newStatus);
    }
    
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