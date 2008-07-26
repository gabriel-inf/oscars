package net.es.oscars.rmi;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Properties;

import net.es.oscars.PropHandler;

import org.apache.log4j.Logger;

public class CoreRmiClient implements CoreRmiInterface {
    private Logger log;
    private CoreRmiInterface remote;
    private boolean connected;

    public CoreRmiClient() {
        this.log = Logger.getLogger(this.getClass());
    }

    /**
     * init
     */
    public void init() throws RemoteException {
        this.remote = null;
        this.log.debug("init.start");
        this.connected = true;
        int port = 1099;  // default rmi registry port
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("rmi", true);
        if (props.getProperty("registryPort") != null ) {
            try {
                port = Integer.decode(props.getProperty("registryPort"));
            } catch (NumberFormatException e) { } 
        }
        try {
            Registry registry = LocateRegistry.getRegistry(port);
            this.remote = (CoreRmiInterface) registry.lookup("IDCRMIServer");
            this.connected = true;
            this.log.debug("Connected to IDC RMI server");
        } catch (RemoteException e) {
            this.connected = false;
            this.log.warn("Remote exception from RMI server: ", e);
            throw e;
        } catch (NotBoundException e) {
            this.connected = false;
            this.log.warn("Trying to access unregistered remote object: ", e);
        } catch (Exception e) {
            this.connected = false;
            this.log.warn("Could not connect", e);
        }
        this.log.debug("init.end");
    }
    
    /**
     *   createReservation
     *   
     *   @param inputMap HashMap<String, String[]> - contains input from web request
     *   @param String userName - authenticated login name of user
     *   @return HashMap<String, Object> - out values to pour into json Object.
     */

    public HashMap<String, Object> createReservation(HashMap<String, String[]> inputMap, String userName) 
        throws RemoteException {
        this.log.debug("createReservation.start");
        HashMap<String, Object> result = new HashMap<String, Object>();
        String methodName = "CreateReservation";
        if (this.remote == null) {
            this.log.error("Remote object not found");
            result.put("error", methodName + ": Remote object not found");
            return result;
        }
        if (!this.connected) {
            this.log.error("Not connected to RMI server");
            result.put("error", methodName + ": Could not connect to RMI server");
            return result;
        }

        try {
            result = this.remote.createReservation(inputMap, userName);
            this.log.debug("createReservation.end");
            return result;
        } catch (RemoteException e) {
            this.log.warn("Remote exception from RMI server: " + e.getMessage(), e);
            result.put("error", methodName + ": Remote exception from RMI server: " + e.getMessage());
        } catch (Exception e) {
            this.log.warn("Exception from RMI server" + e.getMessage(), e);
            result.put("error", methodName + ": Exception from RMI server: " + e.getMessage());
        }
        return result;
    }

    /**
     *   queryReservation
     *   
     *   @param inputMap HashMap<String, String[]> - contains input from web request
     *   @param String userName - authenticated login name of user
     *   @return HashMap<String, Object> - out values to pour into json Object.
     */

    public HashMap<String, Object> queryReservation(HashMap<String, String[]> inputMap, String userName) 
        throws RemoteException {
        this.log.debug("queryReservation.start");
        HashMap<String, Object> result =  new HashMap<String, Object>();
        String methodName = "QueryReservation";
        if (this.remote == null) {
            this.log.error("Remote object not found");
            result.put("error", methodName + ": Remote object not found");
            return result;
        }
        if (!this.connected) {
            this.log.error("Not connected to RMI server");
            result.put("error", methodName + ": Could not connect to RMI server");
            return result;
        }

        try {
            result = this.remote.queryReservation(inputMap, userName);
            this.log.debug("queryReservation.end");
            return result;
        } catch (RemoteException e) {
            this.log.warn("Remote exception from RMI server: " + e.getMessage(), e);
            result.put("error", methodName + ": Remote exception from RMI server: " + e.getMessage());
        } catch (Exception e) {
            this.log.warn("Exception from RMI server" + e.getMessage(), e);
            result.put("error", methodName + ": Exception from RMI server: " + e.getMessage());
        }
        return result;
    }

    /**
     *   listReservations
     *   
     *   @param inputMap HashMap<String, String[]> - contains input from web request
     *   @param String userName - authenticated login name of user
     *   @return HashMap<String, Object> - out values to pour into json Object.
     */

    public HashMap<String, Object> listReservations(HashMap<String, String[]> inputMap, String userName) 
        throws RemoteException {
        this.log.debug(" listReservations.start");
        HashMap<String, Object> result = new HashMap<String, Object>();
        String methodName = "ListReservations";
        if (this.remote == null) {
            this.log.error("Remote object not found");
            result.put("error", methodName + ": Remote object not found");
            return result;
        }
        if (!this.connected) {
            this.log.error("Not connected to RMI server");
            result.put("error", methodName + ": Could not connect to RMI server");
            return result;
        }

        try {
            result = this.remote.listReservations(inputMap, userName);
            this.log.debug(" listReservations.end");
            return result;
        } catch (RemoteException e) {
            this.log.warn("Remote exception from RMI server: " + e.getMessage(), e);
            result.put("error", methodName + ": Remote exception from RMI server: " + e.getMessage());
        } catch (Exception e) {
            this.log.warn("Exception from RMI server" + e.getMessage(), e);
            result.put("error", methodName + ": Exception from RMI server: " + e.getMessage());
        }
        return result;
    }

    /**
     *   cancelReservation
     *   @param inputMap HashMap<String, String[]> - contains input from web request
     *   @param String userName - authenticated login name of user
     *   @return HashMap<String, Object> - out values to pour into json Object.
     */

    public HashMap<String, Object> cancelReservation(HashMap<String, String[]> inputMap, String userName) 
        throws RemoteException {
        this.log.debug(" cancelReservation.start");
        HashMap<String, Object> result = new HashMap<String, Object>();
        String methodName = "CancelReservation";
        if (this.remote == null) {
            this.log.error("Remote object not found");
            result.put("error", methodName + ": Remote object not found");
            return result;
        }
        if (!this.connected) {
            this.log.error("Not connected to RMI server");
            result.put("error", methodName + ": Could not connect to RMI server");
            return result;
        }

        try {
            result = this.remote.cancelReservation(inputMap, userName);
            this.log.debug(" cancelReservation.end");
            return result;
        } catch (RemoteException e) {
            this.log.warn("Remote exception from RMI server: " + e.getMessage(), e);
            result.put("error", methodName + ": Remote exception from RMI server: " + e.getMessage());
        } catch (Exception e) {
            this.log.warn("Exception from RMI server" + e.getMessage(), e);
            result.put("error", methodName + ": Exception from RMI server: " + e.getMessage());
        }
        return result;
    }
    
    /**
     *   modifyReservation
     *   
     *   @param inputMap HashMap<String, String[]> - contains input from web request
     *   @param String userName - authenticated login name of user
     *   @return HashMap<String, Object> - out values to pour into json Object.
     */

    public HashMap<String, Object> modifyReservation(HashMap<String, String[]> inputMap, String userName) 
        throws RemoteException {
        this.log.debug("modifyReservation.start");
        String methodName = "ModifyReservation";
        HashMap<String, Object> result = new HashMap<String, Object>();
        if (this.remote == null) {
            this.log.error("Remote object not found");
            result.put("error", methodName + ": Remote object not found");
            return result;
        }
        if (!this.connected) {
            this.log.error("Not connected to RMI server");
            result.put("error", methodName + ": Could not connect to RMI server");
            return result;
        }

        try {
            result = this.remote.modifyReservation(inputMap, userName);
            this.log.debug("modifyReservation.end");
            return result;
        } catch (RemoteException e) {
            this.log.warn("Remote exception from RMI server: " + e.getMessage(), e);
            result.put("error", methodName + ": Remote exception from RMI server: " + e.getMessage());
        } catch (Exception e) {
            this.log.warn("Exception from RMI server" + e.getMessage(), e);
            result.put("error", methodName + ": Exception from RMI server: " + e.getMessage());
        }
        return result;
    }


}
