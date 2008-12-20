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

    protected String serviceName = null;
    protected Registry registry = null;

    protected String rmiServiceName = null;
    protected int registryPort = Registry.REGISTRY_PORT;
    protected Properties properties = null;

    protected RMISocketFactory socketFactory = null;

    protected boolean createdRegistry = false;



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
    public void init(Remote staticObject) throws RemoteException {

        Remote stub = null;
        String errorMsg = null;
        this.log = Logger.getLogger(this.getClass());

        this.log.debug(this.getServiceName()+".init().start");
        Properties props = this.getProperties();
        if (props == null) {
            errorMsg = "Properties not set!";
            throw new RemoteException(errorMsg);
        }

        int port = registryPort;
        if (props.getProperty("registryPort") != null && !props.getProperty("registryPort").trim().equals("")) {
            try {
                port = Integer.decode(props.getProperty("registryPort").trim());
            } catch (NumberFormatException e) {
                this.log.warn("invalid registryPort value, using default");
            }
        } else {
            this.log.info("registryPort property not set, using default");
        }
        this.setRegistryPort(port);


        this.log.info(this.getServiceName()+" RMI registry at: " + localhost+":"+port);

        if (props.getProperty("registryName") != null && !props.getProperty("registryName").trim().equals("")) {
            this.setRmiServiceName(props.getProperty("registryName").trim());
            this.log.info("Service name at registry: "+this.getRmiServiceName());
        } else if (this.getRmiServiceName() == null) {
            errorMsg = "rmiServiceName not set and registryName property not set!";
            throw new RemoteException(errorMsg);
        }

        InetAddress ipAddr = null;
        try {
            ipAddr = InetAddress.getByName(localhost);
            // creates a custom socket that only listens on ipAddr
            socketFactory = new AnchorSocketFactory(ipAddr);
        } catch (UnknownHostException ex) {
            this.log.error(ex);
        }

        System.setProperty("java.rmi.server.hostname", localhost);

        RemoteException connectError = null;
        try {
            this.log.info("Looking for RMI registry at localhost...");
            this.setRegistry(LocateRegistry.getRegistry(localhost, this.getRegistryPort(), socketFactory));
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
            // Causes the endPoint of the remote sever object to match the interface that is listened on
            System.setProperty("java.rmi.server.hostname", localhost);
            try {
                this.registry = LocateRegistry.createRegistry(this.getRegistryPort(), null, socketFactory);
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
            this.log.error("Remote exception shutting down "+this.getServiceName(), ex);
        } catch (NotBoundException ex) {
            this.log.error(this.getServiceName()+" RMI service already unbound", ex);
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
