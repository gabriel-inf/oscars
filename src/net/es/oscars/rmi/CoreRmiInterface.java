package net.es.oscars.rmi;

import java.io.IOException;
import java.util.HashMap;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CoreRmiInterface extends Remote {

    public void init()  throws RemoteException;

    /**
     * Creates reservation given information from servlet.
     *
     * @param inputMap HashMap<String, String[]> - contains input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into JSON Object.
     * @throws IOException
     * @throws RemoteException
     */
    public HashMap<String, Object>
        createReservation(HashMap<String, String[]> inputMap, String userName)
            throws IOException, RemoteException;

    /**
     * Queries reservation given information from servlet.
     *
     * @param inputMap HashMap<String, String[]> - contains input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into JSON Object.
     * @throws IOException
     * @throws RemoteException
     */
    public HashMap<String, Object>
        queryReservation(HashMap<String, String[]> inputMap, String userName)
            throws IOException, RemoteException;

    /**
     * Lists reservations given criteria from servlet.
     *
     * @param inputMap HashMap<String, String[]> - contains input from web request
     * @param userName string with authenticated login name of user
     *
     * @return HashMap<String, Object> - out values to pour into JSON Object.
     * @throws IOException
     * @throws RemoteException
     */

    public HashMap<String, Object>
        listReservations(HashMap<String, String[]> inputMap, String userName)
            throws IOException, RemoteException;

    /**
     * Cancels reservation given information from servlet.
     * @param inputMap HashMap<String, String[]> - contains input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into JSON Object.
     * @throws IOException
     * @throws RemoteException
     */
    public HashMap<String, Object>
        cancelReservation(HashMap<String, String[]> inputMap, String userName)
            throws IOException, RemoteException;

    /**
     * Modifies reservation given information from servlet.
     *
     * @param inputMap HashMap<String, String[]> - contains input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into JSON Object.
     * @throws IOException
     * @throws RemoteException
     */
    public HashMap<String, Object>
        modifyReservation(HashMap<String, String[]> inputMap, String userName)
            throws IOException, RemoteException;

    /**
     * Immediately creates reservation circuit given information from servlet.
     * Only for network engineers from local domain.
     *
     * @param inputMap HashMap<String, String[]> - contains input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into JSON Object.
     * @throws IOException
     * @throws RemoteException
     */
    public HashMap<String, Object>
        createPath(HashMap<String, String[]> inputMap, String userName)
            throws IOException, RemoteException;

    /**
     * Immediately tears down reservation circuit given info from servlet.
     * Only for network engineers from the local domain.
     *
     * @param inputMap HashMap<String, String[]> - contains input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into JSON Object.
     * @throws IOException
     * @throws RemoteException
     */
    public HashMap<String, Object>
        teardownPath(HashMap<String, String[]> inputMap, String userName)
            throws IOException, RemoteException;

    /**
     * Forces the immediate status change of a reservation.
     * Only for network engineers from the local domain.
     *
     * @param inputMap HashMap<String, String[]> - contains input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into JSON Object.
     */
    public HashMap<String, Object>
        modifyStatus(HashMap<String, String[]> inputMap, String userName)
            throws IOException, RemoteException;
}
