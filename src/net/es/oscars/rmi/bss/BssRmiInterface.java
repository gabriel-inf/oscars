package net.es.oscars.rmi.bss;

import java.io.IOException;
import java.util.HashMap;
import java.rmi.Remote;
import java.rmi.RemoteException;

import net.es.oscars.bss.Reservation;
import net.es.oscars.wsdlTypes.GetTopologyContent;
import net.es.oscars.wsdlTypes.GetTopologyResponseContent;
import net.es.oscars.rmi.bss.xface.*;

public interface BssRmiInterface extends Remote {

     /**
     * Default registry port
     */
    static int registryPort = 1099;
    /**
     * Default registry address
     */
    static String registryAddress = "127.0.0.1";
    /**
     * Default registry name
     */
    static String registryName = "BSSRMIServer";



    public void init() throws RemoteException;


    /**
     * Creates reservation given information from client.
     *
     * @param resvRequest - partially filled in reservation with requested params
     * @param userName string with authenticated login name of user
     * @return gri - new global reservation id assigned to reservation
     * @throws IOException
     * @throws RemoteException
     */
    public String
        createReservation(Reservation resvRequest, String userName)
            throws IOException, RemoteException;

    /**
     * Queries reservation given information from client.
     *
     * @param request RmiQueryResRequest contains input from component
     * @param userName string with authenticated login name of user
     * @return RmiQueryResReply bean containing reservation
     * @throws IOException
     * @throws RemoteException
     */
    public RmiQueryResReply
        queryReservation(RmiQueryResRequest request, String userName)
            throws IOException, RemoteException;

    /**
     * Lists reservations given criteria from client.
     *
     * @param request - RmiListResRequest contains input from component
     *
     * @return RmiListResReply list of reservations satisfying criteria
     * @throws IOException
     * @throws RemoteException
     */

    public RmiListResReply
        listReservations(RmiListResRequest request, String userName)
            throws IOException, RemoteException;

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
            throws IOException, RemoteException;

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
            throws IOException, RemoteException;

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
            throws IOException, RemoteException;

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
            throws IOException, RemoteException;

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
            throws IOException, RemoteException;

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
            throws IOException, RemoteException;

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
            throws IOException, RemoteException;

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
            throws IOException, RemoteException;
}
