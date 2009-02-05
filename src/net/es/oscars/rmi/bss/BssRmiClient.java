package net.es.oscars.rmi.bss;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Properties;

import net.es.oscars.rmi.BaseRmiClient;
import net.es.oscars.rmi.bss.xface.*;
import net.es.oscars.bss.Reservation;
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

        //this.log.debug("BssRmiClient.init().start");
        this.log.info("Starting BssRmi connection");

        Properties props = PropertyLoader.loadProperties("rmi.properties","bss",true);
        this.setProps(props);
        // name of bss service in registry, will be reset from bss.registryName in rmi properties
        this.rmiServiceName = "BSSRMIServer";
        // used for logging in BaseRmiServer.init
        this.serviceName="BSS RMI Server";

        super.configure();
        Remote remote = super.startConnection();
        if (this.connected) {
            this.setRemote((BssRmiInterface) remote);
            super.setRemote(remote);
        }
        //this.log.debug("BssRmiClient.init().end");
    }

    /**
     * createReservation
     *
     * @param resvRequest - partially filled in reservation with requested params
     * @param userName string with authenticated login name of user
     * @return gri - new global reservation id assigned to reservation
     * @throws RemoteException
     */
    public String
        createReservation(Reservation resvRequest, String userName)
            throws RemoteException {

        this.log.debug("createReservation.start");
        String methodName = "CreateReservation";
        this.verifyRmiConnection(methodName);
        try {
            String gri = this.remote.createReservation(resvRequest, userName);
            this.log.debug("createReservation.end");
            return gri;
        } catch (RemoteException e) {
            this.log.debug("Remote exception from RMI server: " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            // shouldn't happen
            this.log.error(methodName + ": Exception from RMI server" + e.getMessage(), e);
            throw new RemoteException(e.getMessage(),e);
        }
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
            throw e;
        } catch (Exception e) {  // shouldn't happen
            this.log.error(methodName +": Exception from RMI server" + e.getMessage(), e);
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
        } catch (RemoteException e) {
            this.log.debug("Remote exception from RMI server: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            //shouldn't happen 
            this.log.error(methodName + ": Exception from RMI server" + e.getMessage(), e);
            throw new RemoteException(e.getMessage(),e);
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

        String methodName = "CancelReservation";
        this.verifyRmiConnection(methodName);
        try {
            this.remote.cancelReservation(gri, userName);
            this.log.debug(" cancelReservation.end");
        } catch (RemoteException e) {
            this.log.debug("Remote exception from RMI server: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            this.log.error(methodName + ": Exception from RMI server" + e.getMessage(), e);
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
            result = this.remote.modifyReservation(params, userName);
            this.log.debug("modifyReservation.end");
            return result;
        } catch (RemoteException e) {
            this.log.debug("Remote exception from RMI server: " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            this.log.error(methodName + ": Exception from RMI server" + e.getMessage(), e);
            throw new RemoteException (e.getMessage(),e);
        }
    }

    /**
     * createPath
     *
     * @param request RmiPathRequest containing request parameters
     * @param userName string with login of user making request
     * @return result string with status of path setup for reservation
     * @throws RemoteException
     */
    public String
        createPath(RmiPathRequest request, String userName)
            throws RemoteException {

        this.log.debug("createPath.start");
        String methodName = "CreatePath";
        String result = null;
        this.verifyRmiConnection(methodName);
        try {
            result = this.remote.createPath(request, userName);
            this.log.debug("createPath.end");
            return result;
        } catch (RemoteException e) {
            this.log.debug("Remote exception from RMI server: " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            this.log.error("Exception from RMI server" + e.getMessage(), e);
            throw new RemoteException (e.getMessage(),e);
        }
    }

    /**
     * unsafeCreatePath
     *
     * @param request RmiPathRequest containing request parameters
     * @param userName string with login of user making request
     * @return result string with status of path setup for reservation
     * @throws RemoteException
     */
    public String
        unsafeCreatePath(RmiPathRequest request, String userName)
            throws RemoteException {

        this.log.debug("unsafeCreatePath.start");
        String methodName = "UnsafeCreatePath";
        String result = null;
        this.verifyRmiConnection(methodName);
        try {
            result = this.remote.unsafeCreatePath(request, userName);
            this.log.debug("unsafeCreatePath.end");
            return result;
        } catch (RemoteException e) {
            this.log.debug("Remote exception from RMI server: " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            this.log.error("Exception from RMI server" + e.getMessage(), e);
            throw new RemoteException (e.getMessage(),e);
        }
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
            result = this.remote.teardownPath(params, userName);
            this.log.debug("teardownPath.end");
            return result;
        } catch (RemoteException e) {
            this.log.debug("Remote exception from RMI server: " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            this.log.error(methodName + ": Exception from RMI server" + e.getMessage(), e);
            throw new RemoteException (e.getMessage(),e);
        }
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
            result = this.remote.modifyStatus(params, userName);
            this.log.debug("modifyStatus.end");
            return result;
        } catch (RemoteException e) {
            this.log.debug("Remote exception from RMI server: " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            this.log.error(methodName + ": Exception from RMI server" + e.getMessage(), e);
            throw new RemoteException (e.getMessage(),e);
        }
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
 
}
