package net.es.oscars.rmi.notifybroker;

import java.rmi.*;
import java.rmi.registry.*;
import java.util.*;

import net.es.oscars.PropHandler;
import net.es.oscars.rmi.notifybroker.xface.*;

import org.apache.log4j.Logger;
import org.jdom.Element;

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
    
    final public static String FILTER_PRODXPATH = "PRODXPATH";
    final public static String FILTER_MSGXPATH = "MSGXPATH";
    final public static String FILTER_TOPIC = "TOPIC";
    
    /**
     * Initializes the client and connects to the AAA RMI registry.
     *
     * @throws RemoteException
     */
    public NotifyRmiClient() throws RemoteException {
        this.remote = null;
        this.log.debug("NotifyRmiClient.init().start");
        this.connected = true;

        // default rmi registry port
        int rmiPort = NotifyRmiInterface.registryPort;
        // default rmi registry address
        String rmiIpaddr = NotifyRmiInterface.registryHost;
        // default rmi registry name
        String rmiRegName = NotifyRmiInterface.registeredServerName;

        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("rmi.notifybroker", true);
        if (props.getProperty("registryPort") != null && !props.getProperty("registryPort").equals("")) {
            try {
                rmiPort = Integer.decode(props.getProperty("registryPort"));
            } catch (NumberFormatException e) {
                this.log.warn(e);
            }
        }

        if (props.getProperty("registryHost") != null && !props.getProperty("registryHost").equals("")) {
            rmiIpaddr = props.getProperty("registryHost");
        }

        if (props.getProperty("registeredServerName") != null && !props.getProperty("registeredServerName").equals("")) {
            rmiRegName = props.getProperty("registeredServerName");
        }

        try {
            Registry registry = LocateRegistry.getRegistry(rmiIpaddr, rmiPort);
            this.remote = (NotifyRmiInterface) registry.lookup(rmiRegName);
            this.log.debug("Got remote object \n" + remote.toString());
            this.connected = true;
            this.log.debug("Connected to "+rmiRegName+" server");
        } catch (RemoteException e) {
            this.connected = false;
            this.log.error("Remote exception from NotifyBroker RMI server:\n" + e);
            throw new RemoteException("Error while server tried connecting to" +
            		" RMI Server. Please contact server administrator");
        } catch (NotBoundException e) {
            this.connected = false;
            this.log.error("Trying to access unregistered remote object: ", e);
        } catch (Exception e) {
            this.connected = false;
            this.log.error("Could not connect", e);
        }
        this.log.debug("NotifyRmiClient.init().end");
    }
    
    public void notify(String publisherUrl, String publisherRegId, 
            List<String> topics, List<Element> msg) throws RemoteException {
        String methodName = "notify";
        if (!this.verifyRmiConnection(methodName)) {
            throw new RemoteException("Not connected to RMI server");
        }
        try {
            this.remote.notify(publisherUrl, publisherRegId, topics, msg);
        } catch (RemoteException e) {
            this.log.error("Remote exception: " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            this.log.error("Exception: " + e.getMessage(), e);
            throw new RemoteException(e.getMessage());
        }
    }
    
    public RmiSubscribeResponse subscribe(String consumerUrl, Long termTime,
            HashMap<String, List<String>> filters, String user)
            throws RemoteException {
        this.log.debug("subscribe.start");
        String methodName = "Subscribe";
        this.verifyRmiConnection(methodName);
        try {
            RmiSubscribeResponse response = this.remote.subscribe(consumerUrl, 
                    termTime, filters, user);
            this.log.debug("subscribe.end");
            return response;
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            throw new RemoteException(e.getMessage(),e);
        }
    }
    
    public Long renew(String subscriptionId, Long terminationTime, String user) 
            throws RemoteException {
        this.log.debug("renew.start");
        String methodName = "Renew";
        this.verifyRmiConnection(methodName);
        try {
            Long termTime = this.remote.renew(subscriptionId, terminationTime, user);
            this.log.debug("renew.end");
            return termTime;
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            throw new RemoteException(e.getMessage(),e);
        }
    }
    
    public void unsubscribe(String subscriptionId, String user)
            throws RemoteException {
        this.log.debug("unsubscribe.start");
        String methodName = "Unsubscribe";
        this.verifyRmiConnection(methodName);
        try {
            this.remote.unsubscribe(subscriptionId, user);
            this.log.debug("unsubscribe.end");
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            throw new RemoteException(e.getMessage(),e);
        }
    }

    public void pauseSubscription(String subscriptionId, String user)
            throws RemoteException {
        this.log.debug("pause.start");
        String methodName = "Pause";
        this.verifyRmiConnection(methodName);
        try {
            this.remote.pauseSubscription(subscriptionId, user);
            this.log.debug("pause.end");
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            throw new RemoteException(e.getMessage(),e);
        }
    }
    
    public void resumeSubscription(String subscriptionId, String user)
            throws RemoteException {
        this.log.debug("resume.start");
        String methodName = "Resume";
        this.verifyRmiConnection(methodName);
        try {
            this.remote.resumeSubscription(subscriptionId, user);
            this.log.debug("resume.end");
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            throw new RemoteException(e.getMessage(),e);
        }
    }
    
    public String registerPublisher(String publisherUrl, List<String> topics,
            Boolean demand, Long termTime, String user) throws RemoteException {
        this.log.debug("registerPublisher.start");
        String methodName = "RegisterPublisher";
        this.verifyRmiConnection(methodName);
        try {
            String publisherId = this.remote.registerPublisher(publisherUrl, topics, 
                    demand, termTime, user);
            this.log.debug("registerPublisher.end");
            return publisherId;
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            throw new RemoteException(e.getMessage(),e);
        }
    }

    public void destroyRegistration(String publisherId, String user)
            throws RemoteException {
        this.log.debug("destroyRegistration.start");
        String methodName = "RegisterPublisher";
        this.verifyRmiConnection(methodName);
        try {
            this.remote.destroyRegistration(publisherId, user);
            this.log.debug("destroyRegistration.end");
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            throw new RemoteException(e.getMessage(),e);
        }
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
