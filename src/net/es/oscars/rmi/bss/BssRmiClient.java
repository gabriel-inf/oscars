package net.es.oscars.rmi.bss;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Properties;

import net.es.oscars.PropHandler;
import net.es.oscars.PropertyLoader;

import org.apache.log4j.Logger;

public class BssRmiClient implements BssRmiInterface  {
    private Logger log;
    private BssRmiInterface remote;
    private boolean connected;

    public BssRmiClient() {
        this.log = Logger.getLogger(this.getClass());
    }

    /**
     * Initializes the client and connects to the BSS RMI registry.
     *
     * DO NOT use this yet; currently only here for completeness
     *
     * @throws RemoteException
     */
    public void init() throws RemoteException {
        this.remote = null;
        this.log.debug("BssRmiClient.init().start");
        this.connected = true;

        // default rmi registry port
        int rmiPort = BssRmiInterface.registryPort;
        // default rmi registry address
        String rmiIpaddr = BssRmiInterface.registryAddress;
        // default rmi registry name
        String rmiRegName = BssRmiInterface.registryName;


        Properties props = PropertyLoader.loadProperties("rmi.properties","bss",true);

        // PropHandler propHandler = new PropHandler("rmi.properties");
        // Properties props = propHandler.getPropertyGroup("aaa", true);
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

        this.log.info("BSS client RMI info: "+rmiIpaddr+":"+rmiPort+":"+rmiRegName);

        try {
            Registry registry = LocateRegistry.getRegistry(rmiIpaddr, rmiPort);
            this.remote = (BssRmiInterface) registry.lookup(rmiRegName);

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

        this.log.debug("BssRmiClient.init().end");
    }


    /**
     * createReservation
     *
     * @param params HashMap<String, Object> - contains input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into JSON Object.
     * @throws RemoteException
     */
    public HashMap<String, Object>
        createReservation(HashMap<String, Object> params, String userName)
            throws RemoteException {

        this.log.debug("createReservation.start");
        HashMap<String, Object> result = new HashMap<String, Object>();
        String methodName = "CreateReservation";
        result = this.checkConnectionErrors(methodName);
        if (result != null) {
            return result;
        }
        try {
            result = this.remote.createReservation(params, userName);
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
     * @param params HashMap<String, Object> - contains input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into JSON Object.
     * @throws RemoteException
     */
    public HashMap<String, Object>
        queryReservation(HashMap<String, Object> params, String userName)
            throws RemoteException {

        this.log.debug("queryReservation.start");
        HashMap<String, Object> result =  new HashMap<String, Object>();
        String methodName = "QueryReservation";
        result = this.checkConnectionErrors(methodName);
        if (result != null) {
            return result;
        }
        try {
            result = this.remote.queryReservation(params, userName);
            this.log.debug("queryReservation.end");
            return result;
        } catch (RemoteException e) {
            this.log.info("Remote exception from RMI server: " + e.getMessage());
            throw new RemoteException(methodName + ": Remote exception from RMI server: " + e.getCause().getMessage());
        } catch (Exception e) {  // shouldn't happen
            this.log.warn("Exception from RMI server" + e.getMessage(), e);
            throw new RemoteException(e.getMessage(),e);
        }
    }

    /**
     * listReservations
     *
     * @param params HashMap<String, Object> - contains input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into JSON Object.
     * @throws RemoteException
     */
    public HashMap<String, Object>
        listReservations(HashMap<String, Object> params, String userName)
            throws RemoteException {

        this.log.debug(" listReservations.start");
        HashMap<String, Object> result = new HashMap<String, Object>();
        String methodName = "ListReservations";
        result = this.checkConnectionErrors(methodName);
        if (result != null) {
            return result;
        }
        try {
            // TODO switch to throwing exceptions
            result = this.remote.listReservations(params, userName);
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
     * @param params HashMap<String, Object> - contains input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into JSON Object.
     * @throws RemoteException
     */
    public HashMap<String, Object>
        cancelReservation(HashMap<String, Object> params, String userName)
            throws RemoteException {

        this.log.debug(" cancelReservation.start");
        HashMap<String, Object> result = new HashMap<String, Object>();
        String methodName = "CancelReservation";
        result = this.checkConnectionErrors(methodName);
        if (result != null) {
            return result;
        }
        try {
            result = this.remote.cancelReservation(params, userName);
            this.log.debug(" cancelReservation.end");
            return result;
        } catch (RemoteException e) {
            this.log.warn("Remote exception from RMI server: " + e.getMessage(), e);
            throw new RemoteException(methodName + ": Remote exception from RMI server: " + e.getMessage());
        } catch (Exception e) {
            this.log.warn("Exception from RMI server" + e.getMessage(), e);
            throw new RemoteException (e.getMessage(),e);
        }
    }

    /**
     * modifyReservation
     *
     * @param params HashMap<String, Object> - contains input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into JSON Object.
     * @throws RemoteException
     */
    public HashMap<String, Object>
        modifyReservation(HashMap<String, Object> params, String userName)
            throws RemoteException {

        this.log.debug("modifyReservation.start");
        String methodName = "ModifyReservation";
        HashMap<String, Object> result = new HashMap<String, Object>();
        result = this.checkConnectionErrors(methodName);
        if (result != null) {
            return result;
        }
        try {
            // TODO switch to throwing exceptions
            result = this.remote.modifyReservation(params, userName);
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
     * @param params HashMap<String, Object> - contains input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into JSON Object.
     * @throws RemoteException
     */
    public HashMap<String, Object>
        createPath(HashMap<String, Object> params, String userName)
            throws RemoteException {

        this.log.debug("createPath.start");
        String methodName = "CreatePath";
        HashMap<String, Object> result = new HashMap<String, Object>();
        result = this.checkConnectionErrors(methodName);
        if (result != null) {
            return result;
        }
        try {
            // TODO switch to throwing exceptions
            result = this.remote.createPath(params, userName);
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
     * @param params HashMap<String, Object> - contains input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into JSON Object.
     */
    public HashMap<String, Object>
        teardownPath(HashMap<String, Object> params, String userName)
            throws RemoteException {

        this.log.debug("teardownPath.start");
        String methodName = "TeardownPath";
        HashMap<String, Object> result = new HashMap<String, Object>();
        result = this.checkConnectionErrors(methodName);
        if (result != null) {
            return result;
        }
        try {
            // TODO switch to throwing exceptions
            result = this.remote.teardownPath(params, userName);
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
     * @param params HashMap<String, Object> - contains input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into JSON Object.
     * @throws RemoteException
     */
    public HashMap<String, Object>
        modifyStatus(HashMap<String, Object> params, String userName)
            throws RemoteException {

        this.log.debug("modifyStatus.start");
        String methodName = "ModifyStatus";
        HashMap<String, Object> result = new HashMap<String, Object>();
        result = this.checkConnectionErrors(methodName);
        if (result != null) {
            return result;
        }
        try {
            // TODO switch to throwing exceptions
            result = this.remote.modifyStatus(params, userName);
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

    /**
     * @return the remote
     */
    public BssRmiInterface getRemote() {
        return remote;
    }

    /**
     * @param remote the remote to set
     */
    public void setRemote(BssRmiInterface remote) {
        this.remote = remote;
    }

    public HashMap<String, Object> checkConnectionErrors(String methodName) {
        HashMap<String, Object> result = new HashMap<String, Object>();
        // TODO switch to throwing exceptions
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
        return null;
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
