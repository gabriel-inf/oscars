package net.es.oscars.rmi.bss;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Properties;

import net.es.oscars.rmi.BaseRmiClient;
import net.es.oscars.rmi.bss.xface.*;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.events.OSCARSEvent;
import net.es.oscars.wsdlTypes.GetTopologyContent;
import net.es.oscars.wsdlTypes.GetTopologyResponseContent;
import net.es.oscars.PropHandler;

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

        this.log.info("Starting BssRmi connection");

        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("rmi.bss", true);
        this.setProps(props);
        // name of bss Server in registry, may be reset from bss.registeredServerName in oscars.bss.rmi properties
        this.rmiServerName = BssRmiInterface.registeredServerName;
        // used for logging in BaseRmiServer.init
        this.serviceName="BSS RMI Client";

        super.configure();
        Remote remote = super.startConnection();
        if (this.connected) {
            this.setRemote((BssRmiInterface) remote);
            super.setRemote(remote);
        }
        //this.log.debug("BssRmiClient.init().end");
    }

    /**
     * Makes call to RMI server to create reservation.
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
     * Makes call to RMI server to request reservation details.
     *
     * @param request string containing global reservation id
     * @param userName string with authenticated login name of user
     * @return result RmiQueryResReply bean containing reservation
     * @throws RemoteException
     */
    public RmiQueryResReply
        queryReservation(String request, String userName)
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
     * Makes call to RMI server to list reservations satisfying client criteria.
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
     * Makes call to RMI server to cancel a reservation with the given
     * global reservation id.
     *
     * @param gri String GlobalReservationId of reservation to be canceled
     * @param userName string with authenticated login name of user
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
     * Makes call to RMI server to submit a job to modify reservation, given
     * new parameters.
     *
     * @param resv transient Reservation containing parameters to modify
     * @param userName string with authenticated login name of user
     * @return persistentResv matching Reservation from database
     * @throws RemoteException
     */
    public Reservation
        modifyReservation(Reservation resv, String userName)
            throws RemoteException {

        this.log.debug("modifyReservation.start");
        String methodName = "ModifyReservation";
        Reservation persistentResv = null;
        this.verifyRmiConnection(methodName);
        try {
            persistentResv = this.remote.modifyReservation(resv, userName);
            this.log.debug("modifyReservation.end");
            return persistentResv;
        } catch (RemoteException e) {
            this.log.debug("Remote exception from RMI server: " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            this.log.error(methodName + ": Exception from RMI server" + e.getMessage(), e);
            throw new RemoteException (e.getMessage(),e);
        }
    }

    /**
     * Makes call to RMI server to get network topology.
     *
     * @param getTopoRequest Axis2 type containing network topology request
     * @param userName string with login of user making request
     * @return result Axis2 type containing network topology
     * @throws RemoteException
     */
    public GetTopologyResponseContent
        getNetworkTopology(GetTopologyContent getTopoRequest, String userName)
            throws RemoteException {

        this.log.debug("getNetworkTopology.start");
        String methodName = "GetNetworkTopology";
        GetTopologyResponseContent result = null;
        this.verifyRmiConnection(methodName);
        try {
            result = this.remote.getNetworkTopology(getTopoRequest, userName);
            this.log.debug("getNetworkTopology.end");
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
     * Makes call to RMI server to create path via signalling.
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
     * Makes call to RMI server to refresh path via signalling.
     *
     * @param request RmiPathRequest containing request parameters
     * @param userName string with login of user making request
     * @return result string with status of path refresh for reservation
     * @throws RemoteException
     */
    public String
        refreshPath(RmiPathRequest request, String userName)
            throws RemoteException {

        this.log.debug("refreshPath.start");
        String methodName = "RefreshPath";
        String result = null;
        this.verifyRmiConnection(methodName);
        try {
            result = this.remote.refreshPath(request, userName);
            this.log.debug("refreshPath.end");
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
     * Makes call to RMI server to teardown path via signaling.
     *
     * @param request RmiPathRequest containing request parameters
     * @param userName string with login of user making request
     * @return result string with status of path teardown for reservation
     * @throws RemoteException
     */
    public String
        teardownPath(RmiPathRequest request, String userName)
            throws RemoteException {

        this.log.debug("teardownPath.start");
        String methodName = "TeardownPath";
        String result = null;
        this.verifyRmiConnection(methodName);
        try {
            result = this.remote.teardownPath(request, userName);
            this.log.debug("teardownPath.end");
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
     * Handles an event received from another IDC
     * 
     * @param event the event received from another IDC
     */
    public void handleEvent(OSCARSEvent event) throws RemoteException {
        this.log.debug("handleEvent.start");
        String methodName = "HandleEvent";
        this.verifyRmiConnection(methodName);
        try {
            this.remote.handleEvent(event);
            this.log.debug("handleEvent.end");
        } catch (RemoteException e) {
            this.log.debug("Remote exception from RMI server: " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            this.log.error("Exception from RMI server" + e.getMessage(), e);
            throw new RemoteException (e.getMessage(),e);
        }
    }

    /**
     * Makes call to RMI server to force path creation in the local domain.
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
     * Makes call to RMI server to force path teardown in local domain.
     *
     * @param request RmiPathRequest containing request parameters
     * @param userName string with login of user making request
     * @return result string with status of path teardown for reservation
     * @throws RemoteException
     */
    public String
        unsafeTeardownPath(RmiPathRequest request, String userName)
            throws RemoteException {

        this.log.debug("unsafeTeardownPath.start");
        String methodName = "UnsafeTeardownPath";
        String result = null;
        this.verifyRmiConnection(methodName);
        try {
            result = this.remote.unsafeTeardownPath(request, userName);
            this.log.debug("unsafeTeardownPath.end");
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
     * Makes call to RMI server to force modification of reservation's status,
     * which can set in motion other events.
     *
     * @param request RmiModifyStatusRequest containing request parameters
     * @param userName string with login of user making request
     * @return result string with status of forced status change for reservation
     * @throws RemoteException
     */
    public String
        unsafeModifyStatus(RmiModifyStatusRequest request, String userName)
            throws RemoteException {

        this.log.debug("unsafeModifyStatus.start");
        String methodName = "UnsafeModifyStatus";
        String result = null;
        this.verifyRmiConnection(methodName);
        try {
            result = this.remote.unsafeModifyStatus(request, userName);
            this.log.debug("unsafeModifyStatus.end");
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
