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

    /**
     * The remote object
     */
    protected Remote remote;

    /**
     * True if we have a connection to the RMI registry, false otherwise
     */

    protected Properties properties;  // set by child class
    protected String serviceName = null;  // set by child class, used in log messages
    protected int registryPort = Registry.REGISTRY_PORT;
    protected String rmiServiceName = null;// may be set by child class, name of service that is registered
    protected String registryHost ="127.0.0.1";
    protected boolean configured = false;
    protected boolean connected;


    public Remote startConnection() throws RemoteException {
        this.log.debug("startConnection().start");

        if (!this.configured) {
            throw new RemoteException("RMI client not configured");
        }
        this.remote = null;
        this.connected = false;
        String errMsg = null;
        try {
            Registry registry = LocateRegistry.getRegistry(registryHost, registryPort);
            this.remote = (Remote) registry.lookup(rmiServiceName);
            this.connected = true;
            this.log.debug("Connected to "+rmiServiceName+" service");
        } catch (RemoteException e) {
            errMsg="Remote exception from RMI server: trying to access " + this.remote.toString();
            this.log.warn("Remote exception from RMI server: trying to access " + this.remote.toString(), e);
        } catch (NotBoundException e) {
            errMsg="Trying to access unregistered remote object: ";
            this.log.warn("Trying to access unregistered remote object: ", e);
        } catch (Exception e) {
            errMsg= "Could not connect";
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
        int rmiPort = registryPort;
        // rmi registry address
        String rmiIpaddr ="127.0.0.1";
        // default rmi registry name
        String rmiRegName;

        Properties props = this.properties;
        if (props == null) {
            log.warn("rmi.properties not found. Using default values");
            //errorMsg = "Properties not set!";
            //throw new RemoteException(errorMsg);
        } else {
            if (props.getProperty("registryPort") != null && !props.getProperty("registryPort").trim().equals("")) {
                try {
                    rmiPort = Integer.decode(props.getProperty("registryPort").trim());
                    this.registryPort = rmiPort; 
                } catch (NumberFormatException e) {
                    this.log.warn("invalid registryPort value, using default");
                }
            } else {
                this.log.debug("registryPort property not set, using default");
            }

            if (props.getProperty("registryAddress") != null && !props.getProperty("registryAddress").equals("")) {
                rmiIpaddr = props.getProperty("registryAddress");
                this.registryHost = rmiIpaddr;
            } else {
                this.log.warn("registryAddress property not set: using localhost");
            }


            if (props.getProperty("registryName") != null && !props.getProperty("registryName").equals("")) {
                rmiRegName = props.getProperty("registryName");
                this.rmiServiceName = rmiRegName;
            } else {
                this.log.warn("registryName property not set. Using default: " + this.rmiServiceName);
            }
        }
        this.configured = true;
        this.log.debug("Client RMI info: "+this.registryHost+":"+this.registryPort+":"+this.rmiServiceName);
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
