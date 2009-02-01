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

    public final static String localhost = "127.0.0.1";

    protected String serviceName = null;  // set by child class, used in log messages
    protected Registry registry = null;

    protected String rmiServiceName = null; // may be set by child class, name of service that is registered
    protected int registryPort = Registry.REGISTRY_PORT;
    protected Properties properties = null; // set by child class.

    protected RMISocketFactory socketFactory = null;

    protected boolean createdRegistry = false;

    /**
     * init
     *   By default initializes the RMI server registry to listen on port 1099 (the default)
     *   the RMI server to listen on a random port, and both to listen only on the loopback
     *   interface. These values can be overidden by oscars.properties.
     *   Setting the serverIpaddr to localhost will allow access from remote hosts and
     *   invalidate our security assumptions.
     *   Assumes that this.rmiServiceName has been set to a default value by the caller.
     * @throws remoteException
     */
    public void init(Remote staticObject) throws RemoteException {

        Remote stub = null;
        String errorMsg = null;
        this.log = Logger.getLogger(this.getClass());

        int port = registryPort;
        this.log.debug(this.serviceName+".init().start");
        Properties props = this.getProperties();
        if (props == null) {
            log.warn("rmi.properties not found. Using default values");
            //errorMsg = "Properties not set!";
            //throw new RemoteException(errorMsg);
        } else {
            if (props.getProperty("registryPort") != null && !props.getProperty("registryPort").trim().equals("")) {
                try {
                    port = Integer.decode(props.getProperty("registryPort").trim());
                    this.setRegistryPort(port);
                } catch (NumberFormatException e) {
                    this.log.warn("invalid registryPort value, using default");
                }
            } else {
                this.log.warn("registryPort property not set, using default");
            }
            // this.log.info(serviceName+" RMI registry at: " + localhost+":"+port);
            if (props.getProperty("registryName") != null && !props.getProperty("registryName").trim().equals("")) {
                this.setRmiServiceName(props.getProperty("registryName").trim());
                this.log.info("Service name at registry: "+this.getRmiServiceName());
            } else if (this.getRmiServiceName() != null) {
                this.log.warn("RegistryName not set in rmi.properties. Using default: " + this.rmiServiceName);
            } else {
                errorMsg = "rmiServiceName not set because registryName property not set!";
                throw new RemoteException(errorMsg);
            }
        }
        InetAddress ipAddr = null;
        try {
            ipAddr = InetAddress.getByName(localhost);
            // creates a custom socket that only listens on ipAddr
            socketFactory = new AnchorSocketFactory(ipAddr);
        } catch (UnknownHostException ex) {
            this.log.error(ex);
            throw new RemoteException("UnknownHostException creating socket on host: " +localhost);
        }


        // Causes the endPoint of the remote sever object to match the interface that is listened on
        System.setProperty("java.rmi.server.hostname", localhost);

        RemoteException connectError = null;
        try {
            this.log.info("Looking for RMI registry at localhost:" + this.registryPort);
            this.setRegistry(LocateRegistry.getRegistry(localhost, this.registryPort, socketFactory));
            String[] services = this.getRegistry().list();
            for (String s: services) {
                this.log.debug("RMI service: "+s);
            }
        } catch (RemoteException ex) {
            connectError = ex;
            this.setRegistry(null);
        }

        if (this.getRegistry() == null) {
            this.log.info("Could not locate RMI registry at localhost, creating one...");
            try {
                this.registry = LocateRegistry.createRegistry(this.registryPort, null, socketFactory);
            } catch (RemoteException ex) {
                this.log.error("Could not locate existing registry. Error was:"+connectError.getMessage());
                this.log.error("Tried to create registry but failed. Error was:"+ex.getMessage());
                throw ex;
            }
            this.createdRegistry = true;
        } else {
            this.log.debug("Registry found at localhost");
        }

        this.log.debug("Binding to registry...");
        stub = UnicastRemoteObject.exportObject(staticObject, 0, null, this.socketFactory);
        this.registry.rebind(this.getRmiServiceName(), stub);
        this.log.info("RegistryPort: " + registryPort + " rmiServiceName: " + rmiServiceName );
        this.log.debug(this.getServiceName()+".init().end");

    }

    /**
     * shutdown
     */
    public void shutdown(Remote staticObject) {
        this.log.debug(this.getServiceName()+".shutdown().start");
        try {
            java.rmi.server.UnicastRemoteObject.unexportObject(staticObject, true);
            this.registry.unbind(this.getRmiServiceName());
            if (this.createdRegistry) {
                java.rmi.server.UnicastRemoteObject.unexportObject(this.registry, true);
            }
        } catch (RemoteException ex) {
            this.log.error(this.getServiceName() + " Registry already shutdown ");
        } catch (NotBoundException ex) {
            this.log.error(this.getServiceName()+" RMI service already unbound " + ex.getMessage());
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
     * @return the rmiServiceName
     */
    public String getRmiServiceName() {
        return rmiServiceName;
    }

    /**
     * @param rmiServiceName the rmiServiceName to set
     */
    public void setRmiServiceName(String rmiServiceName) {
        this.rmiServiceName = rmiServiceName;
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
