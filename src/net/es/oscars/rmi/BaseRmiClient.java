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
    protected boolean connected;

    protected Properties props;

    protected int registryPort;
    protected String rmiServiceName;
    protected String registryHost;
    protected boolean configured = false;



    public Remote startConnection() throws RemoteException {
        this.log.debug("startConnection().start");

        if (!this.configured) {
            throw new RemoteException("RMI client not configured");
        }
        this.remote = null;
        this.connected = true;

        try {
            Registry registry = LocateRegistry.getRegistry(registryHost, registryPort);
            this.remote = (Remote) registry.lookup(rmiServiceName);

            this.log.debug("Got remote object \n" + remote.toString());
            this.connected = true;
            this.log.debug("Connected to "+rmiServiceName+" service");
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
        this.log.debug("startConnection().end");
        return this.remote;
    }


    protected void configure() throws RemoteException {
        this.log.debug("configure().start");
        // default rmi registry port
        int rmiPort = Registry.REGISTRY_PORT;
        // rmi registry address
        String rmiIpaddr;
        // default rmi registry name
        String rmiRegName;

        if (props.getProperty("registryPort") != null && !props.getProperty("registryPort").trim().equals("")) {
            try {
                rmiPort = Integer.decode(props.getProperty("registryPort").trim());
            } catch (NumberFormatException e) {
                this.log.warn("invalid registryPort value, using default");
            }
        } else {
            this.log.debug("registryPort property not set, using default");
        }
        this.registryPort = rmiPort;


        if (props.getProperty("registryAddress") != null && !props.getProperty("registryAddress").equals("")) {
            rmiIpaddr = props.getProperty("registryAddress");
        } else {
            throw new RemoteException("registryAddress property not set!");
        }
        this.registryHost = rmiIpaddr;

        if (props.getProperty("registryName") != null && !props.getProperty("registryName").equals("")) {
            rmiRegName = props.getProperty("registryName");
        } else {
            throw new RemoteException("registryName property not set!");
        }
        this.rmiServiceName = rmiRegName;

        this.configured = true;
        this.log.info("Client RMI info: "+rmiIpaddr+":"+rmiPort+":"+rmiRegName);
        this.log.debug("configure().end");
    }


    /**
     * @param methodName the calling method, for logging
     * @return true if the RMI connection is OK
     */
    protected boolean verifyRmiConnection(String methodName) {
        if (methodName == null) {
            methodName = "";
        }
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
        return props;
    }




    /**
     * @param props the props to set
     */
    public void setProps(Properties props) {
        this.props = props;
    }




}
