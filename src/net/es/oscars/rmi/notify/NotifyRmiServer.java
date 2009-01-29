package net.es.oscars.rmi.notify;

import java.util.*;

import java.rmi.*;
import java.rmi.registry.*;
import net.es.oscars.PropertyLoader;
import net.es.oscars.rmi.*;

import org.apache.log4j.*;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.w3.www._2005._08.addressing.EndpointReferenceType;

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
        this.notifyHandler 	= new NotifyRmiHandler();
    }
    
    public String checkSubscriptionId(String address, EndpointReferenceType msgSubRef) throws RemoteException {
    	return this.notifyHandler.checkSubscriptionId(address, msgSubRef);
    }
    
    public void Notify(Notify request) throws RemoteException {
    	this.notifyHandler.Notify(request);
    }


}
