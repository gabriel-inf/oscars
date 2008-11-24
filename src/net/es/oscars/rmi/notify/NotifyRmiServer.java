package net.es.oscars.rmi.notify;

import java.util.*;

import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.net.*;
import java.net.UnknownHostException;

import net.es.oscars.PropHandler;

import net.es.oscars.aaa.*;
import net.es.oscars.rmi.*;
import net.es.oscars.rmi.model.*;

import org.apache.log4j.*;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.w3.www._2005._08.addressing.EndpointReferenceType;

public class NotifyRmiServer  implements NotifyRmiInterface  {
    private Logger log = Logger.getLogger(NotifyRmiServer.class);
    private Registry registry;

    /** Static remote object so that GarbageCollector doesn't delete it */
    public static NotifyRmiServer staticObject;

    private NotifyRmiInterface stub;
    private NotifyRmiHandler notifyHandler;

    /**
     * NotifyRmiServer constructor
     * @throws RemoteException
     */
    public NotifyRmiServer() throws RemoteException {
        NotifyRmiServer.staticObject = this;
    }


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
        PropHandler propHandler = new PropHandler("rmi.properties");

        Properties props = propHandler.getPropertyGroup("notify", true);

        // default rmi registry port
        int rmiPort = NotifyRmiInterface.registryPort;
        // default rmi registry address
        String rmiIpaddr = NotifyRmiInterface.registryAddress;
        // default rmi registry name
        String rmiRegName = NotifyRmiInterface.registryName;

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

        InetAddress ipAddr = null;
        AnchorSocketFactory sf = null;
        // Causes the endPoint of the remote sever object to match the interface that is listened on
        System.setProperty("java.rmi.server.hostname",rmiIpaddr);
        try {
            ipAddr = InetAddress.getByName(rmiIpaddr);
            // creates a custom socket that only listens on ipAddr
            sf = new AnchorSocketFactory(ipAddr);
            this.registry = LocateRegistry.createRegistry(rmiPort, null, sf);
        } catch (UnknownHostException ex) {
            this.log.error(ex);
        }

        this.stub = (NotifyRmiInterface) UnicastRemoteObject.exportObject(NotifyRmiServer.staticObject, rmiPort, null, sf);
        this.registry.rebind(rmiRegName, this.stub);
        this.initHandlers();
        this.log.debug("NotifyRmiServer.init().end");
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

    /**
     * shutdown
     */
    public void shutdown() {
        try {
            java.rmi.server.UnicastRemoteObject.unexportObject(NotifyRmiServer.staticObject, true);
            java.rmi.server.UnicastRemoteObject.unexportObject(this.registry, true);
            this.registry.unbind(registryName);
        } catch (RemoteException ex) {
            this.log.error("Remote exception shutting down Notify RMI server", ex);

        } catch (NotBoundException ex) {
            this.log.error("Notify RMI Server already unbound", ex);
        }
    }

}
