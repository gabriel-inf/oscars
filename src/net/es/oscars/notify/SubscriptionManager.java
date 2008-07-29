package net.es.oscars.notify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import org.apache.log4j.*;
import net.es.oscars.PropHandler;
import net.es.oscars.notify.ws.UnacceptableInitialTerminationTimeFault;

public class SubscriptionManager{
    private Logger log;
    private long subMaxExpTime;
    private long pubMaxExpTime;
    private String dbname;
    
    //Constants
    public static int PAUSED_STATUS = 0;
    public static int ACTIVE_STATUS = 1;
    
    
    
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
            this.log.info("Defaulting to 1 hour expiration for subscriptions.");
            this.pubMaxExpTime = 3600;
        }
    }
    
    public Subscription subscribe(Subscription subscription, 
                                  ArrayList<SubscriptionFilter> filters)
                                  throws UnacceptableInitialTerminationTimeFault{
        this.log.info("subscribe.start");
        SubscriptionDAO dao = new SubscriptionDAO(this.dbname);
        SubscriptionFilterDAO filterDAO = new SubscriptionFilterDAO(this.dbname);
        long curTime = System.currentTimeMillis()/1000;
        
        /* calculate initial termination time */
        long maxTime = curTime + this.subMaxExpTime;
        long expTime = subscription.getTerminationTime();
        if(expTime == 0){
            expTime = maxTime;
        }else if(expTime > maxTime){
            throw new UnacceptableInitialTerminationTimeFault("Requested " +
                    "initial termination time is too far in the future. " +
                    "The maximum is " + this.subMaxExpTime + " seconds from the " +
                    "current time.");
        }
        
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
        
        /* calculate initial termination time */
        long maxTime = curTime + this.pubMaxExpTime;
        long expTime = publisher.getTerminationTime();
        if(expTime == 0){
            expTime = maxTime;
        }else if(expTime > maxTime){
            throw new UnacceptableInitialTerminationTimeFault("Requested " +
                    "initial termination time is too far in the future. " +
                    "The maximum is " + this.pubMaxExpTime + " seconds from the " +
                    "current time.");
        }
        
        /* Save subscription */
        publisher.setReferenceId(this.generateId());
        publisher.setCreatedTime(new Long(curTime));
        publisher.setTerminationTime(new Long(expTime));
        publisher.setStatus(SubscriptionManager.ACTIVE_STATUS);
        dao.create(publisher);
        this.log.info("registerPublisher.finish");
        
        return publisher;
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
    
    /**
     * Generates id conforming to RFC 4122 "A Universally Unique IDentifier (UUID) URN Namespace"
     *
     * @return the randomly generated UUID.
     */
    public String generateId(){
        return "urn:uuid:" + UUID.randomUUID().toString();
    }
}