package net.es.oscars.rmi;

import java.util.*;

import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.net.*;
import java.net.UnknownHostException;
import org.apache.log4j.*;

public abstract class BaseRmiServer {
    protected Logger log;


    protected Properties properties = null; // set by child class.

    /* default configuration values, should match the ones in BaseRmiServer.java */
    protected String serviceName = null;  // set by child class, used in log messages
    protected int registryPort = Registry.REGISTRY_PORT;
    protected int serverPort = 0;
    protected String rmiServerName = null;// should be set by child class, name of Server that is to be exported
    protected String registryHost ="127.0.0.1";
    protected RMISocketFactory socketFactory = null;

    protected Registry registry = null;
    protected boolean createdRegistry = false;

    /**
     * init
     *   By default initializes the RMI server registry to listen on port 1099 (the default)
     *   the RMI server to listen on a random port, and both to listen only on the loopback
     *   interface. These values can be overidden by oscars.properties.
     *   Setting the registryHost to InetAddress.GetLocalHost will allow access from remote hosts and
     *   invalidate our security assumptions.
     *   Assumes that this.rmiServerName has been set to a default value by the caller.
     * @throws remoteException
     */
    public void init(Remote staticObject) throws RemoteException{

        Remote stub = null;
        String errorMsg = null;
        this.log = Logger.getLogger(this.getClass());

        this.log.debug(this.serviceName+".init().start");
        Properties props = this.getProperties();
        if (props == null) {
            log.warn("oscars.properties not found. Using default values");
            //errorMsg = "Properties not set!";
            //throw new RemoteException(errorMsg);
        } else {
            if (props.getProperty("registryPort") != null && !props.getProperty("registryPort").trim().equals("")) {
                try {
                    int port = Integer.decode(props.getProperty("registryPort").trim());
                    this.registryPort=port;
                } catch (NumberFormatException e) {
                    this.log.info("invalid registryPort value, using default: " + this.registryPort);
                }
            } else {
                this.log.info("registryPort property not set, using default: " + this.registryPort);
            }
            if (props.getProperty("serverPort") != null && !props.getProperty("serverPort").trim().equals("")) {
                try {
                    int port = Integer.decode(props.getProperty("serverPort").trim());
                    this.serverPort=port;
                } catch (NumberFormatException e) {
                    this.log.info("invalid serverPort value, using default: " + this.serverPort);
                }
            } else {
                this.log.info("registryPort property not set, using default: " + this.serverPort);
            }
            if (props.getProperty("registeredServerName") != null && !props.getProperty("registeredServerName").trim().equals("")) {
                this.rmiServerName=props.getProperty("registeredServerName").trim();
                this.log.info("Server name at registry: "+this.rmiServerName);
            } else if (this.rmiServerName != null) {
                this.log.info("registeredServerName not set in oscars.properties. Using default: " + this.rmiServerName);
            } else {
                errorMsg = "rmiServerName not set because registeredServerName property not set!";
                throw new RemoteException(errorMsg);
            }
        }
        this.log.info(serviceName+" RMI registry at: " + this.registryHost +":"+this.registryPort);
        InetAddress ipAddr = null;
        try {
            ipAddr = InetAddress.getByName(this.registryHost);
            // creates a custom socket that only listens on ipAddr
            socketFactory = new AnchorSocketFactory(ipAddr);
        } catch (UnknownHostException ex) {
            this.log.error(ex);
            throw new RemoteException("UnknownHostException creating socket on host: " +this.registryHost);
        }


        // Causes the endPoint of the remote sever object to match the interface that is listened on
        System.setProperty("java.rmi.server.hostname", this.registryHost);

        RemoteException connectError = null;
        try {
            this.log.info("Looking for RMI registry at " + this.registryPort);
            this.setRegistry(LocateRegistry.getRegistry(this.registryHost, this.registryPort, socketFactory));
            String[] services = this.getRegistry().list();
            for (String s: services) {
                this.log.debug("RMI Server: "+s);
            }
        } catch (RemoteException ex) {
            connectError = ex;
            this.setRegistry(null);
        }

        if (this.getRegistry() == null) {
            this.log.info("Could not locate RMI registry at " + this.registryHost +" creating one...");
            try {
                this.registry = LocateRegistry.createRegistry(this.registryPort, null, socketFactory);
            } catch (RemoteException ex) {
                this.log.error("Could not locate existing registry. Error was:"+connectError.getMessage());
                this.log.error("Tried to create registry but failed. Error was:"+ex.getMessage());
                throw ex;
            }
            this.createdRegistry = true;
        } else {
            this.log.debug("Registry found at " + this.registryHost);
        }

        this.log.debug("Binding to registry...");
        stub = UnicastRemoteObject.exportObject(staticObject, this.serverPort, null, this.socketFactory);
        try {
            this.registry.bind(this.rmiServerName, stub);
        } catch (AlreadyBoundException ex) {
            this.log.error(this.rmiServerName + " already running");
            throw new RemoteException(this.rmiServerName + " already running");
        }
        this.log.info("RegistryPort: " + registryPort + " rmiServerName: " + rmiServerName  + 
                "serverPort " + serverPort);
        this.log.debug(this.getServiceName()+".init().end");

    }

    /**
     * shutdown
     */
    public void shutdown(Remote staticObject) {
        this.log.debug(this.getServiceName()+".shutdown().start");
        try {
            java.rmi.server.UnicastRemoteObject.unexportObject(staticObject, true);
            this.registry.unbind(this.rmiServerName);
            if (this.createdRegistry) {
                java.rmi.server.UnicastRemoteObject.unexportObject(this.registry, true);
            }
        } catch (RemoteException ex) {
            this.log.error(this.getServiceName() + " Registry already shutdown ");
        } catch (NotBoundException ex) {
            this.log.error(this.getServiceName()+" RMI Server already unbound " + ex.getMessage());
        }
        this.log.debug(this.getServiceName()+".shutdown().end");
    }

    /**
     * @return the serviceName
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * @param serviceName the serviceName to set
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * @return the registry
     */
    public Registry getRegistry() {
        return registry;
    }

    /**
     * @param registry the registry to set
     */
    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    /**
     * @return the rmiServerName
     */
    public String getRmiServerName() {
        return rmiServerName;
    }

    /**
     * @param rmiServerName the rmiServerName to set
     */
    public void setRmiServerName(String rmiServerName) {
        this.rmiServerName = rmiServerName;
    }

    /**
     * @return the registryPort
     */
    public int getRegistryPort() {
        return registryPort;
    }

    /**
     * @param registryPort the registryPort to set
     */
    public void setRegistryPort(int registryPort) {
        this.registryPort = registryPort;
    }

    /**
     * @return the properties
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * @param properties the properties to set
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

}
