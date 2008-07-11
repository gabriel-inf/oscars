package net.es.oscars.notify;

import java.util.ArrayList;
import java.util.UUID;
import org.apache.log4j.*;
import org.hibernate.*;
import net.es.oscars.database.HibernateUtil;

public class SubscriptionManager{
    private Logger log;
    private long EXP_INTERVAL;
    
    //Constants
    public static int PAUSED_STATUS = 0;
    public static int ACTIVE_STATUS = 1;
    
    final private String DBNAME = "notify";
    
    public SubscriptionManager(){
        this.log = Logger.getLogger(this.getClass());
        //TODO: LOAD FROM PROPERTIES FILE
        this.EXP_INTERVAL = 3600; //1 hour
    }
    
    public Subscription subscribe(Subscription subscription, 
                                  ArrayList<SubscriptionFilter> filters){
        this.log.info("subscribe.start");
        Session sess = HibernateUtil.getSessionFactory(DBNAME).getCurrentSession();
        sess.beginTransaction();
        SubscriptionDAO dao = new SubscriptionDAO(DBNAME);
        SubscriptionFilterDAO filterDAO = new SubscriptionFilterDAO(DBNAME);
        long curTime = System.currentTimeMillis()/1000;
        long expTime = curTime + EXP_INTERVAL;
        
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