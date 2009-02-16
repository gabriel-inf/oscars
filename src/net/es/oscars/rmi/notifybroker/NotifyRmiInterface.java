package net.es.oscars.rmi.notifybroker;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;

import net.es.oscars.rmi.notifybroker.xface.*;

import org.jdom.Element;

public interface NotifyRmiInterface extends Remote {
    /**
     * Default registry port
     */
    static int registryPort = 1099;
    
    /**
     * Default registry address
     */
    static String registryHost = "127.0.0.1";
    
    /**
     * Default name in the registry of the notifybroker service
     */
    static String registeredServerName = "net.es.oscars.rmi.notifybroker";
    
    public void notify(String publisherUrl, String publisherRegId, 
            List<String> topics, List<Element> msg) throws RemoteException;

    public RmiSubscribeResponse subscribe(String consumerUrl, Long termTime, 
            HashMap<String,List<String>> filters, String user) throws RemoteException;
    
    public Long renew(String subscriptionId, Long terminationTime, String user) throws RemoteException;
    
    public void unsubscribe(String subscriptionId, String user) throws RemoteException;
    
    public void pauseSubscription(String subscriptionId, String user) throws RemoteException;
    
    public void resumeSubscription(String subscriptionId, String user) throws RemoteException;
    
    public String registerPublisher(String publisherUrl, List<String> topics, 
            Boolean demand, Long termTime, String user) throws RemoteException;
    
    public void destroyRegistration(String publisherId, String user) throws RemoteException;
}
