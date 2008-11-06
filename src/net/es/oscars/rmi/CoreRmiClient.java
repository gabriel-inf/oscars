package net.es.oscars.rmi;

import java.rmi.NotBoundException;
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
     *   By default initializes the RMI server registry  port to 1099 (the default)
     *   This can be overridden by the oscars.properties rmi.registryPort and must 
     *   match the port that CoreRmiServer.init used.
     *
     * @throws RemoteException
     */
    public void init() throws RemoteException {
        this.remote = null;
        this.log.debug("RMIClientInit.start");
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
            String rmiIpaddr = "127.0.0.1";

            if (props.getProperty("serverIpaddr") != null && !props.getProperty("serverIpaddr").equals("")) {
                rmiIpaddr = props.getProperty("serverIpaddr");
            }
            Registry registry = LocateRegistry.getRegistry(rmiIpaddr, port);

            this.remote = (CoreRmiInterface) registry.lookup("IDCRMIServer");
            this.log.debug("Got remote object \n" + remote.toString());
            this.connected = true;
            this.log.debug("Connected to IDC RMI server");
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
        this.log.debug("init.end");
    }

    /**
     * createReservation
     *
     * @param inputMap HashMap<String, String[]> - contains input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into JSON Object.
     * @throws RemoteException
     */
    public HashMap<String, Object>
        createReservation(HashMap<String, String[]> inputMap, String userName)
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
     * queryReservation
     *
     * @param inputMap HashMap<String, String[]> - contains input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into JSON Object.
     * @throws RemoteException
     */
    public HashMap<String, Object>
        queryReservation(HashMap<String, String[]> inputMap, String userName)
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
     * listReservations
     *
     * @param inputMap HashMap<String, String[]> - contains input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into JSON Object.
     * @throws RemoteException
     */
    public HashMap<String, Object>
        listReservations(HashMap<String, String[]> inputMap, String userName)
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
     * cancelReservation
     * @param inputMap HashMap<String, String[]> - contains input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into JSON Object.
     * @throws RemoteException
     */
    public HashMap<String, Object>
        cancelReservation(HashMap<String, String[]> inputMap, String userName)
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
     * modifyReservation
     *
     * @param inputMap HashMap<String, String[]> - contains input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into JSON Object.
     * @throws RemoteException
     */
    public HashMap<String, Object>
        modifyReservation(HashMap<String, String[]> inputMap, String userName)
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

    /**
     * createPath
     *
     * @param inputMap HashMap<String, String[]> - contains input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into JSON Object.
     * @throws RemoteException
     */
    public HashMap<String, Object>
        createPath(HashMap<String, String[]> inputMap, String userName)
            throws RemoteException {

        this.log.debug("createPath.start");
        String methodName = "CreatePath";
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
            result = this.remote.createPath(inputMap, userName);
            this.log.debug("createPath.end");
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
     * teardownPath
     *
     * @param inputMap HashMap<String, String[]> - contains input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into JSON Object.
     */
    public HashMap<String, Object>
        teardownPath(HashMap<String, String[]> inputMap, String userName)
            throws RemoteException {

        this.log.debug("teardownPath.start");
        String methodName = "TeardownPath";
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
            result = this.remote.teardownPath(inputMap, userName);
            this.log.debug("teardownPath.end");
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
     * modifyStatus
     *
     * @param inputMap HashMap<String, String[]> - contains input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into JSON Object.
     * @throws RemoteException
     */
    public HashMap<String, Object>
        modifyStatus(HashMap<String, String[]> inputMap, String userName)
            throws RemoteException {

        this.log.debug("modifyStatus.start");
        String methodName = "ModifyStatus";
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
            result = this.remote.modifyStatus(inputMap, userName);
            this.log.debug("modifyStatus.end");
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
