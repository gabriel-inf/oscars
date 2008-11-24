package net.es.oscars.rmi.bss;

import java.io.IOException;
import java.util.HashMap;
import java.rmi.Remote;
import java.rmi.RemoteException;

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
     * Creates reservation given information from servlet.
     *
     * @param params HashMap<String, Object> - contains input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into JSON Object.
     * @throws IOException
     * @throws RemoteException
     */
    public HashMap<String, Object>
        createReservation(HashMap<String, Object> params, String userName)
            throws IOException, RemoteException;

    /**
     * Queries reservation given information from servlet.
     *
     * @param params HashMap<String, Object> - contains input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into JSON Object.
     * @throws IOException
     * @throws RemoteException
     */
    public HashMap<String, Object>
        queryReservation(HashMap<String, Object> params, String userName)
            throws IOException, RemoteException;

    /**
     * Lists reservations given criteria from servlet.
     *
     * @param params HashMap<String, Object> - contains input from web request
     * @param userName string with authenticated login name of user
     *
     * @return HashMap<String, Object> - out values to pour into JSON Object.
     * @throws IOException
     * @throws RemoteException
     */

    public HashMap<String, Object>
        listReservations(HashMap<String, Object> params, String userName)
            throws IOException, RemoteException;

    /**
     * Cancels reservation given information from servlet.
     * @param params HashMap<String, Object> - contains input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into JSON Object.
     * @throws IOException
     * @throws RemoteException
     */
    public HashMap<String, Object>
        cancelReservation(HashMap<String, Object> params, String userName)
            throws IOException, RemoteException;

    /**
     * Modifies reservation given information from servlet.
     *
     * @param params HashMap<String, Object> - contains input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into JSON Object.
     * @throws IOException
     * @throws RemoteException
     */
    public HashMap<String, Object>
        modifyReservation(HashMap<String, Object> params, String userName)
            throws IOException, RemoteException;

    /**
     * Immediately creates reservation circuit given information from servlet.
     * Only for network engineers from local domain.
     *
     * @param params HashMap<String, Object> - contains input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into JSON Object.
     * @throws IOException
     * @throws RemoteException
     */
    public HashMap<String, Object>
        createPath(HashMap<String, Object> params, String userName)
            throws IOException, RemoteException;

    /**
     * Immediately tears down reservation circuit given info from servlet.
     * Only for network engineers from the local domain.
     *
     * @param params HashMap<String, Object> - contains input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into JSON Object.
     * @throws IOException
     * @throws RemoteException
     */
    public HashMap<String, Object>
        teardownPath(HashMap<String, Object> params, String userName)
            throws IOException, RemoteException;

    /**
     * Forces the immediate status change of a reservation.
     * Only for network engineers from the local domain.
     *
     * @param params HashMap<String, Object> - contains input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into JSON Object.
     */
    public HashMap<String, Object>
        modifyStatus(HashMap<String, Object> params, String userName)
            throws IOException, RemoteException;
}
