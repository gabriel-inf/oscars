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
    private long maxExpTime;
    
    //Constants
    public static int PAUSED_STATUS = 0;
    public static int ACTIVE_STATUS = 1;
    
    final private String DBNAME = "notify";
    
    public SubscriptionManager(){
        this.log = Logger.getLogger(this.getClass());
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("notifybroker", true); 
        try{
            this.maxExpTime = Long.parseLong(props.getProperty("subscriptions.maxExpireTime"));
        }catch(Exception e){}
        //set to default to one hour if not set
        if(this.maxExpTime <= 0){
            this.log.info("Defaulting to 1 hour expiration for subscriptions.");
            this.maxExpTime = 3600;
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
        long maxTime = curTime + this.maxExpTime;
        long expTime = subscription.getTerminationTime();
        if(expTime == 0){
            expTime = maxTime;
        }else if(expTime > maxTime){
            throw new UnacceptableInitialTerminationTimeFault("Requested " +
                    "initial termination time is too far in the future. " +
                    "The maximum is " + this.maxExpTime + " seconds from the " +
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
    
    public String generateId(){
        return UUID.randomUUID().toString();
    }
}