package net.es.oscars.notify;

import java.util.ArrayList;
import java.util.Properties;
import java.util.UUID;
import org.apache.log4j.*;
import org.hibernate.*;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.PropHandler;
import net.es.oscars.notify.ws.UnacceptableInitialTerminationTimeFault;

public class SubscriptionManager{
    private Logger log;
    private long subMaxExpTime;
    private long pubMaxExpTime;
     
    //Constants
    public static int PAUSED_STATUS = 0;
    public static int ACTIVE_STATUS = 1;
    
    final private String DBNAME = "notify";
    
    public SubscriptionManager(){
        this.log = Logger.getLogger(this.getClass());
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
        Session sess = HibernateUtil.getSessionFactory(DBNAME).getCurrentSession();
        sess.beginTransaction();
        SubscriptionDAO dao = new SubscriptionDAO(DBNAME);
        SubscriptionFilterDAO filterDAO = new SubscriptionFilterDAO(DBNAME);
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
        
        sess.getTransaction().commit();
        this.log.info("subscribe.finish");
        
        return subscription;
    }
    
    public Publisher registerPublisher(Publisher publisher) 
                                throws UnacceptableInitialTerminationTimeFault{
        this.log.info("registerPublisher.start");
        Session sess = HibernateUtil.getSessionFactory(DBNAME).getCurrentSession();
        this.log.info("registerPublisher.1");
        sess.beginTransaction();
        this.log.info("registerPublisher.2");
        PublisherDAO dao = new PublisherDAO(DBNAME);
        this.log.info("registerPublisher.3");
        long curTime = System.currentTimeMillis()/1000;
        this.log.info("registerPublisher.4");
        
        /* calculate initial termination time */
        long maxTime = curTime + this.pubMaxExpTime;
        this.log.info("registerPublisher.5");
        long expTime = publisher.getTerminationTime();
        this.log.info("registerPublisher.6");
        if(expTime == 0){
            expTime = maxTime;
            this.log.info("registerPublisher.7");
        }else if(expTime > maxTime){
            this.log.info("registerPublisher.8");
            throw new UnacceptableInitialTerminationTimeFault("Requested " +
                    "initial termination time is too far in the future. " +
                    "The maximum is " + this.pubMaxExpTime + " seconds from the " +
                    "current time.");
        }
        this.log.info("registerPublisher.9");
        /* Save subscription */
        publisher.setReferenceId(this.generateId());
        this.log.info("registerPublisher.10");
        publisher.setCreatedTime(new Long(curTime));
        this.log.info("registerPublisher.11");
        publisher.setTerminationTime(new Long(expTime));
        this.log.info("registerPublisher.12");
        publisher.setStatus(SubscriptionManager.ACTIVE_STATUS);
        this.log.info("registerPublisher.13");
        dao.create(publisher);
        this.log.info("registerPublisher.14");
        sess.getTransaction().commit();
        this.log.info("registerPublisher.finish");
        
        return publisher;
    }
    
    public String generateId(){
        return UUID.randomUUID().toString();
    }
}