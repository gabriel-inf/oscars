package net.es.oscars.rmi;

import java.rmi.*;
import java.rmi.registry.*;
import java.util.*;

import org.apache.log4j.Logger;


/**
 * Base RMI client
 *
 * @author Evangelos Chaniotakis, Mary Thompson
 */
public class BaseRmiClient implements Remote {
    private Logger log = Logger.getLogger(BaseRmiClient.class);

    protected Properties properties;  // set by child class
    
    /* default configuration values, should match the ones in BaseRmiServer.java */
    protected String serviceName = null;  // set by child class, used in log messages
    protected int registryPort = Registry.REGISTRY_PORT;
    protected String rmiServerName = null;// should be set by child class, name of server that is registered
    protected String registryHost ="127.0.0.1";
    
    /* The remote object  */
    protected Remote remote;
    /* True if we have a connection to the RMI registry, false otherwise */
    protected boolean connected;
    protected boolean configured = false;


    public Remote startConnection() throws RemoteException {
        this.log.debug("startConnection().start");

        if (!this.configured) {
            this.log.error("RMI client not configured");
            throw new RemoteException("RMI client not configured");
        }
        this.remote = null;
        this.connected = false;
        String errMsg = null;
        try {
            Registry registry = LocateRegistry.getRegistry(registryHost, registryPort);
            this.remote = (Remote) registry.lookup(rmiServerName);
            this.connected = true;
            this.log.debug("Connected to "+rmiServerName+" service");
        } catch (NotBoundException e) {
            errMsg="Trying to access unregistered remote object: " + rmiServerName;
            this.log.error(errMsg);
        } catch (RemoteException e) {
            errMsg="Remote exception from RMI server: trying to access " + rmiServerName + " " +e.getMessage();
            this.log.error(errMsg);
        } catch (Exception e) {
            errMsg= "Could not connect to " + rmiServerName + " " + e.toString();
            this.log.error(errMsg, e);
        } finally {
            if (errMsg != null) {
                throw new RemoteException(errMsg);
            }
        }
        this.log.debug("startConnection().end");
        return this.remote;
    }


    protected void configure() throws RemoteException {
        this.log.debug(this.serviceName + " configure().start");


        Properties props = this.properties;
        if (props == null) {
            log.warn("properties for " + this.serviceName + " not found. Using default values");
        } else {
            if (props.getProperty("registryPort") != null && !props.getProperty("registryPort").trim().equals("")) {
                try {
                    int rmiPort = Integer.decode(props.getProperty("registryPort").trim());
                    this.registryPort = rmiPort; 
                } catch (NumberFormatException e) {
                    this.log.info("invalid registryPort value, using default: " + this.registryPort);
                }
            } else {
                this.log.info("registryPort property not set, using default " + this.registryPort);
            }

            if (props.getProperty("registryHost") != null && !props.getProperty("registryHost").equals("")) {
                String rmiIpaddr = props.getProperty("registryHost");
                this.registryHost = rmiIpaddr;
            } else {
                this.log.info("registryHost property not set: using default: " + this.registryHost);
            }


            if (props.getProperty("registeredServerName") != null && !props.getProperty("registeredServerName").equals("")) {
                String rmiRegName = props.getProperty("registeredServerName");
                this.rmiServerName = rmiRegName;
            } else {
                this.log.info("registeredServerName property not set. Using default: " + this.rmiServerName);
            }
        }
        this.configured = true;
        this.log.debug("Client RMI info: "+this.registryHost+":"+this.registryPort+":"+this.rmiServerName);
        this.log.debug(this.serviceName + " configure().end");
    }


    /**
     * @param methodName the calling method, for logging
     * @return true if the RMI connection is OK
     */
    protected boolean verifyRmiConnection(String methodName) throws RemoteException {
        if (methodName == null) {
            methodName = "";
        }
        if (this.remote == null) {
            this.log.error(methodName+": Remote object not found");
            throw new RemoteException("Remote object not found");
        }
        if (!this.connected) {
            this.log.error(methodName+": Not connected to RMI server");
            throw new RemoteException("Not connected to RMI server");
        }
        return true;
    }


    /**
     * @return the remote
     */
    public Remote getRemote() {
        return remote;
    }

    /**
     * @param remote the remote to set
     */
    public void setRemote(Remote remote) {
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




    /**
     * @return the props
     */
    public Properties getProps() {
        return properties;
    }




    /**
     * @param props the props to set
     */
    public void setProps(Properties props) {
        this.properties = props;
    }




}
