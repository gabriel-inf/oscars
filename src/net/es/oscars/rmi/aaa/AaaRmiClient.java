package net.es.oscars.rmi.aaa;

import java.rmi.*;
import java.rmi.registry.*;
import java.util.*;

import net.es.oscars.PropHandler;
import org.apache.log4j.Logger;
import net.es.oscars.aaa.AuthValue;
import net.es.oscars.aaa.AuthMultiValue;
import net.es.oscars.aaa.Resource;


/**
 * AAA RMI client
 *
 * Includes the wrapper functions for RMI calls to AAA RMI services
 *
 * @author Evangelos Chaniotakis, Mary Thompson
 */
public class AaaRmiClient implements AaaRmiInterface {
    private Logger log = Logger.getLogger(AaaRmiClient.class);

    /**
     * The remote object
     */
    private AaaRmiInterface remote;

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
        this.log.debug("AaaRmiClient.init().start");
        this.connected = true;

        // default rmi registry port
        int rmiPort = AaaRmiInterface.registryPort;
        // default rmi registry address
        String rmiIpaddr = AaaRmiInterface.registryAddress;
        // default rmi registry name
        String rmiRegName = AaaRmiInterface.registryName;


        PropHandler propHandler = new PropHandler("rmi.properties");
        Properties props = propHandler.getPropertyGroup("aaa", true);
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
            this.remote = (AaaRmiInterface) registry.lookup(rmiRegName);

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


    public HashMap<String, Object> manageAaaObjects(HashMap<String, Object> parameters) throws RemoteException {
        this.log.debug("manageAaaObjects.start");
        String methodName = "manageAaaObjects";
        HashMap<String, Object> result = null;
        if (!this.verifyRmiConnection(methodName)) {
            return result;
        }
        try {
            result = this.remote.manageAaaObjects(parameters);
        } catch (RemoteException e) {
            this.log.warn("Remote exception from RMI server: " + e.getMessage(), e);
        } catch (Exception e) {
            this.log.warn("Exception from RMI server" + e.getMessage(), e);
        }

        this.log.debug("manageAaaObjects.end");
        return result;
    }


    public Boolean validSession(String userName, String sessionName)
            throws RemoteException {
        this.log.debug("validSession.start");

        String methodName = "validSession";
        Boolean result = false;
        if (!this.verifyRmiConnection(methodName)) {
            return result;
        }

        try {
            result = this.remote.validSession(userName, sessionName);
        } catch (RemoteException e) {
            this.log.warn("Remote exception: " + e.getMessage(), e);
            throw new RemoteException(e.getMessage());
        } catch (Exception e) {
            this.log.warn("Exception:" + e.getMessage(), e);
            throw new RemoteException(e.getMessage());
        }
        this.log.debug("validSession.end");
        return result;
    }

    public String verifyLogin(String userName, String password, String sessionName)
            throws RemoteException {

        String methodName = "verifyLogin";
        this.log.debug("verifyLogin.start");

        String result = null;
        if (!this.verifyRmiConnection(methodName)) {
            return result;
        }


        try {
            result = this.remote.verifyLogin(userName, password, sessionName);
        } catch (RemoteException e) {
            this.log.warn("Remote exception from RMI server: " + e.getMessage(), e);
        } catch (Exception e) {
            this.log.warn("Exception from RMI server" + e.getMessage(), e);
        }
        this.log.debug("verifyLogin.end");
        return result;
    }


    public AuthValue checkAccess(String userName, String resourceName, String permissionName)
            throws RemoteException {
        String methodName = "checkAccess";
        this.log.debug("checkAccess.start");

        AuthValue result = AuthValue.DENIED;

        if (!this.verifyRmiConnection(methodName)) {
            return result;
        }

        try {
            result = this.remote.checkAccess(userName, resourceName, permissionName);
        } catch (RemoteException e) {
            this.log.warn("Remote exception from RMI server: " + e.getMessage(), e);
        } catch (Exception e) {
            this.log.warn("Exception from RMI server" + e.getMessage(), e);
        }
        this.log.debug("checkAccess.end");
        return result;
    }

    public AuthMultiValue
        checkMultiAccess(String userName, HashMap<String, ArrayList<String>> resourcePermissions)
            throws RemoteException {
        String methodName = "checkMultiAccess";
        this.log.debug("checkMultiAccess.start");

        AuthMultiValue result = new AuthMultiValue();
        if (!this.verifyRmiConnection(methodName)) {
            return result;
        }


        try {
            result = this.remote.checkMultiAccess(userName, resourcePermissions);
        } catch (RemoteException e) {
            this.log.warn("Remote exception from RMI server: " + e.getMessage(), e);
        } catch (Exception e) {
            this.log.warn("Exception from RMI server" + e.getMessage(), e);
        }

        this.log.debug("checkMultiAccess.end");
        return result;
    }

    public AuthValue
        checkModResAccess(String userName, String resourceName, String permissionName,
            int reqBandwidth, int reqDuration, boolean specPathElems, boolean specGRI)
                throws RemoteException {

        this.log.debug("checkModResAccess.start");
        String methodName = "checkModResAccess";
        AuthValue result = AuthValue.DENIED;

        if (!this.verifyRmiConnection(methodName)) {
            return result;
        }
        try {
            result = this.remote.checkModResAccess(userName, resourceName, permissionName, reqBandwidth, reqDuration, specPathElems, specGRI);
        } catch (RemoteException e) {
            this.log.warn("Remote exception from RMI server: " + e.getMessage(), e);
        } catch (Exception e) {
            this.log.warn("Exception from RMI server" + e.getMessage(), e);
        }
        this.log.debug("checkModResAccess.end");
        return result;
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
    public AaaRmiInterface getRemote() {
        return remote;
    }

    /**
     * @param remote the remote to set
     */
    public void setRemote(AaaRmiInterface remote) {
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
