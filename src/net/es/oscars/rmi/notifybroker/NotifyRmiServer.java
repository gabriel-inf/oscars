package net.es.oscars.rmi.notifybroker;

import java.util.*;

import java.rmi.*;
import java.rmi.registry.*;

import net.es.oscars.PropHandler;
import net.es.oscars.notifybroker.NotifyBrokerCore;
import net.es.oscars.notifybroker.SubscriptionManager;
import net.es.oscars.rmi.*;

import org.apache.axiom.om.OMElement;
import org.apache.log4j.*;
import org.hibernate.Session;

public class NotifyRmiServer  extends BaseRmiServer implements NotifyRmiInterface  {
    private Logger log = Logger.getLogger(NotifyRmiServer.class);
    private SubscriptionManager sm;
    private Registry registry;
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

    public String subscribe(String consumerUrl, Long termTime,
            HashMap<String, String> filters, String user)
            throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }
    
    public Long renew(String subscriptionId, Long terminationTime, String user) 
            throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }
    
    public void unsubscribe(String subscriptionId, String user)
            throws RemoteException {
        // TODO Auto-generated method stub
        
    }

    public void pauseSubscription(String subscriptionId, String user)
            throws RemoteException {
        // TODO Auto-generated method stub
        
    }
    
    public void resumeSubscription(String subscriptionId, String user)
            throws RemoteException {
        // TODO Auto-generated method stub
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
        NBValidator.validateDestroyRegistration(publisherId, user, this.log);
        
        Session sess = this.core.getNotifySession();
        sess.beginTransaction();
        try{
            this.sm.destroyRegistration(publisherId, user);
        }catch(Exception e){
            sess.getTransaction().rollback();
            this.log.error(e.getMessage());
            throw new RemoteException(e.getMessage());
        }
        sess.getTransaction().commit();
        this.log.info("destroyRegistration.end");
        
    }
}
