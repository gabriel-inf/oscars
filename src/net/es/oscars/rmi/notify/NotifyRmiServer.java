package net.es.oscars.rmi.notify;

import java.util.*;

import java.rmi.*;
import java.rmi.registry.*;
import net.es.oscars.PropertyLoader;
import net.es.oscars.rmi.*;

import org.apache.axiom.om.OMElement;
import org.apache.log4j.*;

public class NotifyRmiServer  extends BaseRmiServer implements NotifyRmiInterface  {
    private Logger log = Logger.getLogger(NotifyRmiServer.class);
    private Registry registry;

    /** Static remote object so that GarbageCollector doesn't delete it */
    public static NotifyRmiServer staticObject;

    private NotifyRmiHandler notifyHandler;

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
        
        Properties props = PropertyLoader.loadProperties("rmi.properties","notify",true);
        this.setProperties(props);
        // used for logging in BaseRmiServer.init
        this.setServiceName("Notify RMI Server");

        super.init(staticObject);
        this.initHandlers();
    }

    /**
     * shutdown
     */
    public void shutdown() {
        super.shutdown(staticObject);
    }

    public void initHandlers() {
        this.notifyHandler = new NotifyRmiHandler();
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
        // TODO Auto-generated method stub
        return null;
    }

    public void destroyRegistration(String publisherId, String user)
            throws RemoteException {
        // TODO Auto-generated method stub
    }
}
