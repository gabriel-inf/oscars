package net.es.oscars.rmi.bss;

import java.io.IOException;
import java.util.HashMap;
import java.rmi.Remote;
import java.rmi.RemoteException;

import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.events.OSCARSEvent;
import net.es.oscars.wsdlTypes.GetTopologyContent;
import net.es.oscars.wsdlTypes.GetTopologyResponseContent;
import net.es.oscars.rmi.bss.xface.*;

public interface BssRmiInterface extends Remote {

    /* Default registered Service name */
    static String registeredServerName = "BSSRMIServer";

    public void init() throws RemoteException;


    /**
     * Creates reservation given information from client.
     *
     * @param resvRequest - partially filled in reservation with requested params
     * @param userName string with authenticated login name of user
     * @return gri - new global reservation id assigned to reservation
     * @throws RemoteException
     */
    public String
        createReservation(Reservation resvRequest, String userName)
            throws  RemoteException;

    /**
     * Queries reservation given information from client.
     *
     * @param request string with global reservation id
     * @param userName string with authenticated login name of user
     * @return RmiQueryResReply bean containing reservation
     * @throws RemoteException
     */
    public RmiQueryResReply
        queryReservation(String request, String userName)
            throws  RemoteException;

    /**
     * Lists reservations given criteria from client.
     *
     * @param request - RmiListResRequest contains input from component
     *
     * @return RmiListResReply list of reservations satisfying criteria
     * @throws RemoteException
     */

    public RmiListResReply
        listReservations(RmiListResRequest request, String userName)
            throws RemoteException;

    /**
     * Cancels reservation given information from client.
     *
     * @param gri String GlobalReservationId of reservation to be canceled
     * @param userName string with authenticated login name of user
     * @throws RemoteException
     */
    public void
        cancelReservation(String gri, String userName)
            throws RemoteException;

    /**
     * Submits job to modify a reservation, given new parameters.
     *
     * @param resv transient Reservation containing parameters to be modified
     * @param userName string with authenticated login name of user
     * @return matching Reservation from database
     * @throws IOException
     * @throws RemoteException
     */
    public Reservation
        modifyReservation(Reservation resv, String userName)
            throws  RemoteException;
    
    /**
     * Gets network topology.
     *
     * @param getTopoRequest Axis2 type containing network topology request
     * @param userName string with authenticated login name of user
     * @return result Axis2 type containing network topology
     * @throws IOException
     * @throws RemoteException
     */
    public GetTopologyResponseContent
        getNetworkTopology(GetTopologyContent getTopoRequest, String userName)
            throws  RemoteException;

    /**
     * Sets up a path.  Forwards the request first, and sets up path if reply.
     * If there is an error during local path setup a teardownPath message
     * is issued.  Different from unsafeCreatePath, which is only for
     * local paths.
     *
     * @param request RmiPathRequest containing request parameters
     * @param userName string with authenticated login name of user
     * @return result string with status of path setup for reservation
     * @throws IOException
     * @throws RemoteException
     */
    public String
        createPath(RmiPathRequest request, String userName)
            throws  RemoteException;

    /**
     * Verifies a path in response to a refreshPath request. Checks local path
     * first and then forwards request.
     *
     * @param request RmiPathRequest containing request parameters
     * @param userName string with authenticated login name of user
     * @return result string with status of path setup for reservation
     * @throws IOException
     * @throws RemoteException
     */
    public String
        refreshPath(RmiPathRequest request, String userName)
            throws RemoteException;

    /**
     * Removes a path in response to a teardown request. Removes local path
     * first and then forwards request. If there is a failure in the local path
     * teardown the request is still forwarded. The exception is reported
     * upstream.
     *
     * @param request RmiPathRequest containing request parameters
     * @param userName string with authenticated login name of user
     * @return result string with status of path teardown for reservation
     * @throws IOException
     * @throws RemoteException
     */
    public String
        teardownPath(RmiPathRequest request, String userName)
            throws RemoteException;
    
    /**
     * Handles an event received from another IDC
     * 
     * @param event the event received from another IDC
     */
    public void handleEvent(OSCARSEvent event) throws RemoteException;

    /**
     * Immediately creates reservation circuit given information from client.
     * Only for network engineers from local domain.
     *
     * @param request RmiPathRequest containing request parameters
     * @param userName string with authenticated login name of user
     * @return result string with status of path setup for reservation
     * @throws IOException
     * @throws RemoteException
     */
    public String
        unsafeCreatePath(RmiPathRequest request, String userName)
            throws RemoteException;

    /**
     * Immediately tears down reservation circuit given info from client.
     * Only for network engineers from the local domain.
     *
     * @param request RmiPathRequest containing request parameters
     * @param userName string with login of user making request
     * @return result string with status of path teardown for reservation
     * @throws IOException
     * @throws RemoteException
     */
    public String
        unsafeTeardownPath(RmiPathRequest request, String userName)
            throws RemoteException;

    /**
     * Forces the immediate status change of a reservation.
     * Only for network engineers from the local domain.
     *
     * @param request RmiModifyStatusRequest containing request parameters
     * @param userName string with login of user making request
     * @return result string with status of forced status change for reservation
     */
    public String
        unsafeModifyStatus(RmiModifyStatusRequest request, String userName)
            throws RemoteException;
}
