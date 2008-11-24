package net.es.oscars.rmi.notify;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.oasis_open.docs.wsn.b_2.Notify;
import org.w3.www._2005._08.addressing.EndpointReferenceType;

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

    public String checkSubscriptionId(String address, EndpointReferenceType msgSubRef) throws RemoteException;
    
    public void Notify(Notify request) throws RemoteException;


}
