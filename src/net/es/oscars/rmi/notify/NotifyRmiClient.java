package net.es.oscars.rmi.notify;

import java.rmi.*;
import java.rmi.registry.*;
import java.util.*;

import net.es.oscars.PropHandler;

import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;

public class NotifyRmiClient implements NotifyRmiInterface {
    private Logger log = Logger.getLogger(NotifyRmiClient.class);

    /**
     * The remote object
     */
    private NotifyRmiInterface remote;

    /**
     * True if we have a connection to the RMI registry, false otherwise
     */
    private boolean connected;

    /**
     * Initializes the client and connects to the AAA RMI registry.
     *
     * DO NOT use this yet; currently only here for completeness
     * Will eventually be used by the core to connect to a separate AAA RMI server
     *
     * @throws RemoteException
     */
    public void init() throws RemoteException {
        this.remote = null;
        this.log.debug("NotifyRmiClient.init().start");
        this.connected = true;

        // default rmi registry port
        int rmiPort = NotifyRmiInterface.registryPort;
        // default rmi registry address
        String rmiIpaddr = NotifyRmiInterface.registryAddress;
        // default rmi registry name
        String rmiRegName = NotifyRmiInterface.registryName;


        PropHandler propHandler = new PropHandler("rmi.properties");
        Properties props = propHandler.getPropertyGroup("notify", true);
        if (props.getProperty("registryPort") != null && !props.getProperty("registryPort").equals("")) {
            try {
                rmiPort = Integer.decode(props.getProperty("registryPort"));
            } catch (NumberFormatException e) {
                this.log.warn(e);
            }
        }

        if (props.getProperty("registryAddress") != null && !props.getProperty("registryAddress").equals("")) {
            rmiIpaddr = props.getProperty("registryAddress");
        }

        if (props.getProperty("registryName") != null && !props.getProperty("registryName").equals("")) {
            rmiRegName = props.getProperty("registryName");
        }


        try {
            Registry registry = LocateRegistry.getRegistry(rmiIpaddr, rmiPort);
            this.remote = (NotifyRmiInterface) registry.lookup(rmiRegName);

            this.log.debug("Got remote object \n" + remote.toString());
            this.connected = true;
            this.log.debug("Connected to "+rmiRegName+" server");
        } catch (RemoteException e) {
            this.connected = false;
            this.log.warn("Remote exception from RMI server: trying to access " + this.remote.toString(), e);
            throw e;
        } catch (NotBoundException e) {
            this.connected = false;
            this.log.warn("Trying to access unregistered remote object: ", e);
        } catch (Exception e) {
            this.connected = false;
            this.log.warn("Could not connect", e);
        }
        this.log.debug("AaaRmiClient.init().end");
    }
    
    public void notify(String subscriptionId, String publisherId, 
            List<String> topics, OMElement msg) throws RemoteException {
        String methodName = "notify";
        if (!this.verifyRmiConnection(methodName)) {
            throw new RemoteException("Not connected to RMI server");
        }
        try {
            this.remote.notify(subscriptionId, publisherId, topics, msg);
        } catch (RemoteException e) {
            this.log.warn("Remote exception: " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            this.log.warn("Exception: " + e.getMessage(), e);
            throw new RemoteException(e.getMessage());
        }
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
    /**
     * @param methodName the calling method, for logging
     * @return true if the RMI connection is OK
     */
    private boolean verifyRmiConnection(String methodName) {
        if (this.remote == null) {
            this.log.error(methodName+": Remote object not found");
            return false;
        }
        if (!this.connected) {
            this.log.error(methodName+": Not connected to RMI server");
            return false;
        }
        return true;
    }

    /**
     * @return the remote
     */
    public NotifyRmiInterface getRemote() {
        return remote;
    }

    /**
     * @param remote the remote to set
     */
    public void setRemote(NotifyRmiInterface remote) {
        this.remote = remote;
    }

    /**
     * @return the connected
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * @param connected the connected to set
     */
    public void setConnected(boolean connected) {
        this.connected = connected;
    }


}
