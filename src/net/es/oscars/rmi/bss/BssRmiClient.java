package net.es.oscars.rmi.bss;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Properties;

import net.es.oscars.rmi.BaseRmiClient;
import net.es.oscars.rmi.bss.xface.*;
import net.es.oscars.PropertyLoader;

import org.apache.log4j.Logger;

public class BssRmiClient extends BaseRmiClient implements BssRmiInterface  {
    private Logger log;
    private BssRmiInterface remote;

    public BssRmiClient() {
        this.log = Logger.getLogger(this.getClass());
    }

    /**
     * Initializes the client and connects to the BSS RMI registry.
     *
     * @throws RemoteException
     */
    public void init() throws RemoteException {
        this.log.debug("BssRmiClient.init().start");

        Properties props = PropertyLoader.loadProperties("rmi.properties","bss",true);
        this.setProps(props);
        super.configure();

        Remote remote = super.startConnection();

        if (this.connected) {
            this.setRemote((BssRmiInterface) remote);
            super.setRemote(remote);
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
        this.verifyRmiConnection(methodName);
        try {
            result = this.remote.createReservation(params, userName);
            this.log.debug("createReservation.end");
            return result;
        } catch (RemoteException e) {
            this.log.debug("Remote exception from RMI server: " + e.getMessage(), e);
            result.put("error", methodName + ": Remote exception from RMI server: " + e.getMessage());
        } catch (Exception e) {
            this.log.debug("Exception from RMI server" + e.getMessage(), e);
            result.put("error", methodName + ": Exception from RMI server: " + e.getMessage());
        }
        return result;
    }

    /**
     * queryReservation
     *
     * @param request RmiQueryResRequest contains input from component
     * @param userName string with authenticated login name of user
     * @return result RmiQueryResReply bean containing reservation
     * @throws RemoteException
     */
    public RmiQueryResReply
        queryReservation(RmiQueryResRequest request, String userName)
            throws RemoteException {

        this.log.debug("queryReservation.start");
        String methodName = "QueryReservation";
        this.verifyRmiConnection(methodName);
        RmiQueryResReply result = null;
        try {
            result = this.remote.queryReservation(request, userName);
            this.log.debug("queryReservation.end");
            return result;
        } catch (RemoteException e) {
            this.log.debug("Remote exception from RMI server: " + e.getMessage());
            throw new RemoteException(methodName + ": Remote exception from RMI server: " + e.getCause().getMessage());
        } catch (Exception e) {  // shouldn't happen
            this.log.info("Exception from RMI server" + e.getMessage(), e);
            throw new RemoteException(e.getMessage(),e);
        }
    }

    /**
     * listReservations
     *
     * @param request RmiListResRequest containing search parameters
     * @param userName string with login of user making request
     * @return reply RmiListResReply containing list of resulting reservations
     * @throws RemoteException
     */
    public RmiListResReply
        listReservations(RmiListResRequest request, String userName)
            throws RemoteException {

        this.log.debug("listReservations.start");
        RmiListResReply result = new RmiListResReply();
        String methodName = "ListReservations";
        this.verifyRmiConnection(methodName);
        try {
            result = this.remote.listReservations(request, userName);
        } catch (Exception e) {
            this.log.debug("Exception from RMI server" + e.getMessage(), e);
            throw new RemoteException(methodName +
                              ": Exception from RMI server: " + e.getMessage());
        }
        this.log.debug("listReservations.end");
        return result;
    }

    /**
     * cancelReservation
     * @param gri String GlobalReservationId of reservation to be canceled
     * @param userName string with authenticated login name of user
     * @
     * @throws RemoteException
     */
    public void
        cancelReservation(String gri, String userName)
            throws RemoteException {

        this.log.debug(" cancelReservation.start");
        String methodName = "CancelReservation";
        this.verifyRmiConnection(methodName);
        try {
            this.remote.cancelReservation(gri, userName);
            this.log.debug(" cancelReservation.end");
        } catch (RemoteException e) {
            this.log.debug("Remote exception from RMI server: " + e.getMessage());
            throw new RemoteException(methodName + ": Remote exception from RMI server: " + e.getMessage());
        } catch (Exception e) {
            this.log.error("Exception from RMI server" + e.getMessage(), e);
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
        this.verifyRmiConnection(methodName);
        try {
            // TODO switch to throwing exceptions
            result = this.remote.modifyReservation(params, userName);
            this.log.debug("modifyReservation.end");
            return result;
        } catch (RemoteException e) {
            this.log.debug("Remote exception from RMI server: " + e.getMessage(), e);
            result.put("error", methodName + ": Remote exception from RMI server: " + e.getMessage());
        } catch (Exception e) {
            this.log.debug("Exception from RMI server" + e.getMessage(), e);
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
        this.verifyRmiConnection(methodName);
        try {
            // TODO switch to throwing exceptions
            result = this.remote.createPath(params, userName);
            this.log.debug("createPath.end");
            return result;
        } catch (RemoteException e) {
            this.log.debug("Remote exception from RMI server: " + e.getMessage(), e);
            result.put("error", methodName + ": Remote exception from RMI server: " + e.getMessage());
        } catch (Exception e) {
            this.log.info("Exception from RMI server" + e.getMessage(), e);
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
        this.verifyRmiConnection(methodName);
        try {
            // TODO switch to throwing exceptions
            result = this.remote.teardownPath(params, userName);
            this.log.debug("teardownPath.end");
            return result;
        } catch (RemoteException e) {
            this.log.debug("Remote exception from RMI server: " + e.getMessage(), e);
            result.put("error", methodName + ": Remote exception from RMI server: " + e.getMessage());
        } catch (Exception e) {
            this.log.info("Exception from RMI server" + e.getMessage(), e);
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
        this.verifyRmiConnection(methodName);
        try {
            // TODO switch to throwing exceptions
            result = this.remote.modifyStatus(params, userName);
            this.log.debug("modifyStatus.end");
            return result;
        } catch (RemoteException e) {
            this.log.debug("Remote exception from RMI server: " + e.getMessage(), e);
            result.put("error", methodName + ": Remote exception from RMI server: " + e.getMessage());
        } catch (Exception e) {
            this.log.info("Exception from RMI server" + e.getMessage(), e);
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
