package net.es.oscars.rmi.notifybroker;

import java.util.*;

import java.rmi.*;

import net.es.oscars.PropHandler;
import net.es.oscars.aaa.AuthValue;
import net.es.oscars.notifybroker.NotifyBrokerCore;
import net.es.oscars.notifybroker.SubscriptionManager;
import net.es.oscars.rmi.*;
import net.es.oscars.rmi.notifybroker.xface.*;

import org.apache.axiom.om.OMElement;
import org.apache.log4j.*;
import org.hibernate.Session;

public class NotifyRmiServer extends BaseRmiServer implements NotifyRmiInterface  {
    private Logger log = Logger.getLogger(NotifyRmiServer.class);
    private SubscriptionManager sm;
    private NotifyBrokerCore core;

    /** Static remote object so that GarbageCollector doesn't delete it */
    public static NotifyRmiServer staticObject;

    /**
     * init
     *   By default initializes the RMI server registry to listen on port 1099 (the default)
     *   the RMI server to listen on a random port, and both to listen only on the loopback
     *   interface. These values can be overidden by oscars.properties.
     *   Setting the serverIpaddr to localhost will allow access from remote hosts and
     *   invalidate our security assumptions.
     *
     * @throws remoteException
     */
    public void init() throws RemoteException {
        this.log.debug("NotifyRmiServer.init().start");
        NotifyRmiServer.staticObject = this;
        this.core = NotifyBrokerCore.getInstance();
        this.sm = new SubscriptionManager(this.core.getNotifyDbName());
        
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("rmi.notifybroker", true);
        this.setProperties(props);
        // used for logging in BaseRmiServer.init
        this.setServiceName("NotifyBroker RMI Server");
        this.setRmiServiceName(NotifyRmiInterface.registryName);

        super.init(staticObject);
    }

    /**
     * shutdown
     */
    public void shutdown() {
        super.shutdown(staticObject);
    }
    
    public void notify(String subscriptionId, String publisherId, 
            List<String> topics, OMElement msg) throws RemoteException {
        //this.notifyHandler.Notify(request);
    }

    public RmiSubscribeResponse subscribe(String consumerUrl, Long termTime,
            HashMap<String, List<String>> filters, String user)
            throws RemoteException {
        
        this.log.debug("subscribe.start");
        //Check RMI request and AAA parameters. 
        //Also adds AAA filters for specific event types.
        NBValidator.validateSubscribe(consumerUrl, termTime, filters, user, log);
        
        //Create subscription
        Session sess = this.core.getNotifySession();
        sess.beginTransaction();
        RmiSubscribeResponse response = null;
        try{
            response = this.sm.subscribe(consumerUrl, termTime, filters, user);
        }catch(Exception e){
            sess.getTransaction().rollback();
            this.log.error(e.getMessage());
            e.printStackTrace();
            throw new RemoteException(e.getMessage());
        }
        sess.getTransaction().commit();
        this.log.debug("subscribe.end");
        
        return response;
    }
    
    public Long renew(String subscriptionId, Long terminationTime, String user) 
            throws RemoteException {
        this.log.debug("renew.start");
        //Check RMI request and AAA parameters. 
        AuthValue authValue = NBValidator.validateSubscriptionMod(subscriptionId, user, this.log);
        
        //Create subscription
        Session sess = this.core.getNotifySession();
        sess.beginTransaction();
        Long response = null;
        try{
            response = this.sm.renew(subscriptionId, terminationTime, user, authValue);
        }catch(Exception e){
            sess.getTransaction().rollback();
            this.log.error(e.getMessage());
            e.printStackTrace();
            throw new RemoteException(e.getMessage());
        }
        sess.getTransaction().commit();
        this.log.debug("renew.end");
        
        return response;
    }
    
    public void unsubscribe(String subscriptionId, String user)
            throws RemoteException {
        this.log.debug("unsubscribe.start");
        //Check RMI request and AAA parameters. 
        AuthValue authValue = NBValidator.validateSubscriptionMod(subscriptionId, user, this.log);
        
        //Create subscription
        Session sess = this.core.getNotifySession();
        sess.beginTransaction();
        try{
            if(subscriptionId.equals("ALL")){
                this.sm.updateStatusAll(SubscriptionManager.INACTIVE_STATUS, subscriptionId, user);
           }else{
               this.sm.updateStatus(SubscriptionManager.INACTIVE_STATUS, subscriptionId, user, authValue);
           }
        }catch(Exception e){
            sess.getTransaction().rollback();
            this.log.error(e.getMessage());
            e.printStackTrace();
            throw new RemoteException(e.getMessage());
        }
        sess.getTransaction().commit();
        this.log.debug("unsubscribe.end");
    }

    public void pauseSubscription(String subscriptionId, String user)
            throws RemoteException {
        this.log.debug("pause.start");
        //Check RMI request and AAA parameters. 
        AuthValue authValue = NBValidator.validateSubscriptionMod(subscriptionId, user, this.log);
        
        //Create subscription
        Session sess = this.core.getNotifySession();
        sess.beginTransaction();
        try{
            this.sm.updateStatus(SubscriptionManager.PAUSED_STATUS, subscriptionId, user, authValue);
        }catch(Exception e){
            sess.getTransaction().rollback();
            this.log.error(e.getMessage());
            e.printStackTrace();
            throw new RemoteException(e.getMessage());
        }
        sess.getTransaction().commit();
        this.log.debug("pause.end");
    }
    
    public void resumeSubscription(String subscriptionId, String user)
            throws RemoteException {
        this.log.debug("resume.start");
        //Check RMI request and AAA parameters. 
        AuthValue authValue = NBValidator.validateSubscriptionMod(subscriptionId, user, this.log);
        
        //Create subscription
        Session sess = this.core.getNotifySession();
        sess.beginTransaction();
        try{
            this.sm.updateStatus(SubscriptionManager.ACTIVE_STATUS, subscriptionId, user, authValue);
        }catch(Exception e){
            sess.getTransaction().rollback();
            this.log.error(e.getMessage());
            e.printStackTrace();
            throw new RemoteException(e.getMessage());
        }
        sess.getTransaction().commit();
        this.log.debug("resume.end");
    }
    
    public String registerPublisher(String publisherUrl, List<String> topics,
            Boolean demand, Long termTime, String user) throws RemoteException {
        
        this.log.debug("registerPublisher.start");
        //Check RMI request and AAA parameters
        NBValidator.validateRegisterPublisher(publisherUrl, topics, 
                demand, termTime, user, this.log);
        
        //Save publisher
        Session sess = this.core.getNotifySession();
        sess.beginTransaction();
        String publisherId = null;
        try{
            publisherId = this.sm.registerPublisher(publisherUrl, demand, termTime, user);
        }catch(Exception e){
            sess.getTransaction().rollback();
            this.log.error(e.getMessage());
            e.printStackTrace();
            throw new RemoteException(e.getMessage());
        }
        sess.getTransaction().commit();
        this.log.debug("registerPublisher.end");
        
        return publisherId;
    }

    public void destroyRegistration(String publisherId, String user)
            throws RemoteException {
        this.log.info("destroyRegistration.start");
        //Check RMI request and AAA parameters
        AuthValue authVal = NBValidator.validateDestroyRegistration(publisherId, user, this.log);
        
        Session sess = this.core.getNotifySession();
        sess.beginTransaction();
        try{
            this.sm.destroyRegistration(publisherId, user, authVal);
        }catch(Exception e){
            sess.getTransaction().rollback();
            this.log.error(e.getMessage());
            throw new RemoteException(e.getMessage());
        }
        sess.getTransaction().commit();
        this.log.info("destroyRegistration.end");
        
    }
}
