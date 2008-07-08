package net.es.oscars.rmi;

import java.io.IOException;
import java.util.HashMap;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CoreRmiInterface extends Remote {

    public void init()  throws RemoteException;
    
    /**
     *   createReservation
     *   
     *   @param inputMap HashMap<String, String[]> - contains input from web request
     *   @param String userName - authenticated login name of user
     *   @return HashMap<String, Object> - out values to pour into jason Object.
     */
    public HashMap<String, Object> createReservation(HashMap<String, String[]> inputMap, String userName)
         throws IOException, RemoteException;

    /**
     *   queryReservation
     *   
     *   @param inputMap HashMap<String, String[]> - contains input from web request
     *   @param String userName - authenticated login name of user
     *   @return HashMap<String, Object> - out values to pour into jason Object.
     */

    public HashMap<String, Object> queryReservation(HashMap<String, String[]> inputMap, String userName)
        throws IOException, RemoteException;
    /**
     *   listReservations
     *   
     *   @param inputMap HashMap<String, String[]> - contains input from web request
     *   @param String userName - authenticated login name of user
     *
     *   @return HashMap<String, Object> - out values to pour into jason Object.
     */

    public HashMap<String, Object> listReservations(HashMap<String, String[]> inputMap, String userName)
        throws IOException, RemoteException;
    
    /**
     *   cancelReservation
     *   @param inputMap HashMap<String, String[]> - contains input from web request
     *   @param String userName - authenticated login name of user
     *   @return HashMap<String, Object> - out values to pour into jason Object.
     */
   
    public HashMap<String, Object> cancelReservation(HashMap<String, String[]> inputMap, String userName)
        throws IOException, RemoteException;
}
