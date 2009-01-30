package net.es.oscars.rmi.notifybroker;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;

import org.apache.axiom.om.OMElement;

public interface NotifyRmiInterface extends Remote {
    /**
     * Default registry port
     */
    static int registryPort = 1099;
    
    /**
     * Default registry address
     */
    static String registryAddress = "127.0.0.1";
    
    /**
     * Default registry name
     */
    static String registryName = "NotifyRMIServer";

    public void init() throws RemoteException;
    
    public void notify(String subscriptionId, String publisherId, 
            List<String> topics, OMElement msg) throws RemoteException;

    public String subscribe(String consumerUrl, Long termTime, 
            HashMap<String,String> filters, String user) throws RemoteException;
    
    public Long renew(String subscriptionId, Long terminationTime, String user) throws RemoteException;
    
    public void unsubscribe(String subscriptionId, String user) throws RemoteException;
    
    public void pauseSubscription(String subscriptionId, String user) throws RemoteException;
    
    public void resumeSubscription(String subscriptionId, String user) throws RemoteException;
    
    public String registerPublisher(String publisherUrl, List<String> topics, 
            Boolean demand, Long termTime, String user) throws RemoteException;
    
    public void destroyRegistration(String publisherId, String user) throws RemoteException;
}
