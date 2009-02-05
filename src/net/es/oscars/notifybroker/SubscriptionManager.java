package net.es.oscars.notifybroker;

import java.rmi.RemoteException;
import java.util.*;

import org.apache.log4j.*;
import net.es.oscars.PropHandler;
import net.es.oscars.notifybroker.ws.UnacceptableInitialTerminationTimeFault;
import net.es.oscars.notifybroker.ws.UnacceptableTerminationTimeFault;
import net.es.oscars.notifybroker.ws.ResourceUnknownFault;
import net.es.oscars.notifybroker.db.Publisher;
import net.es.oscars.notifybroker.db.PublisherDAO;
import net.es.oscars.notifybroker.db.Subscription;
import net.es.oscars.notifybroker.db.SubscriptionDAO;
import net.es.oscars.notifybroker.db.SubscriptionFilter;
import net.es.oscars.notifybroker.db.SubscriptionFilterDAO;

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
    }
    
    public Subscription subscribe(Subscription subscription, 
                                  ArrayList<SubscriptionFilter> filters)
                                  throws UnacceptableInitialTerminationTimeFault{
        this.log.debug("subscribe.start");
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
        
        this.log.debug("subscribe.finish");
        
        return subscription;
    }
    
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
        publisher.setStatus(SubscriptionManager.ACTIVE_STATUS);
        dao.create(publisher);
        this.log.debug("registerPublisher.finish");
        
        return publisher.getReferenceId();
    }
    
    public void destroyRegistration(String pubRefId, String user) 
                                throws RemoteException{
        this.log.debug("destroyRegistration.start");
        PublisherDAO dao = new PublisherDAO(this.dbname);
        Publisher publisher = dao.queryByRefId(pubRefId, user, false);
        
        if(publisher == null){
            this.log.error("Publisher not found: id=" + pubRefId +", user=" + user);
            RemoteException re = new RemoteException();
            throw new RemoteException("Publisher " + pubRefId + " not found.");
        }
        
        if(publisher.getStatus() != SubscriptionManager.ACTIVE_STATUS){
            throw new RemoteException("Registration has already been destroyed.");
        }
        
        publisher.setStatus(SubscriptionManager.INACTIVE_STATUS);
        dao.update(publisher);
        this.log.debug("destroyRegistration.end");
    }
    
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
    
    public List<Subscription> findSubscriptions(HashMap<String, ArrayList<String>> permissionMap){
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
    
    public long renew(String subRefId, long termTime, 
                      HashMap<String, String> permissionMap)
                      throws ResourceUnknownFault,
                             UnacceptableTerminationTimeFault{
        this.log.debug("renew.start");
        SubscriptionDAO dao = new SubscriptionDAO(this.dbname);
        String modifyLoginConstraint = permissionMap.get("modifyLoginConstraint");
        Subscription subscription = dao.queryByRefId(subRefId, modifyLoginConstraint);
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
        
        this.log.debug("renew.end");
        
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
    
    public synchronized void updateStatusAll(int newStatus, String subRefId, 
                      String userLogin) throws Exception, ResourceUnknownFault{
        this.log.debug("updateStatusAll.start");
        SubscriptionDAO dao = new SubscriptionDAO(this.dbname);
        List<Subscription> subscriptions = dao.getAllActiveForUser(userLogin);
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