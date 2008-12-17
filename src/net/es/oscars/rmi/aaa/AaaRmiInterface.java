package net.es.oscars.rmi.aaa;

import java.util.ArrayList;
import java.util.HashMap;
import java.rmi.Remote;
import java.rmi.RemoteException;
import net.es.oscars.aaa.AuthValue;
import net.es.oscars.aaa.AuthMultiValue;

public interface AaaRmiInterface extends Remote {
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
    static String registryName = "AAARMIServer";

    public void init() throws RemoteException;

    /**
     * Logs a user in - used by servlets
     * Throws an exception if username is invalid, password is incorrect
     * or if the user has no attributes
     *
     * @param userName the username to verify the login for
     * @param password the password
     * @param sessionName the session name to set
     * @return String the username
     * @throws RemoteException
     */
    public String verifyLogin(String userName, String password, String sessionName) throws RemoteException;

    public String verifyDN(String dn) throws RemoteException;


    /**
     * Verifies if a user has a valid session
     * @param userName The username to check
     * @param sessionName The session name to check against
     * @return true if the username and session name match, false otherwise
     * @throws RemoteException
     */
    public Boolean validSession(String userName, String sessionName) throws RemoteException;

    public String getInstitution(String userName) throws RemoteException;

    public AuthValue checkAccess(String userName, String resourceName, String permissionName) throws RemoteException;
    public AuthMultiValue checkMultiAccess(String userName, HashMap<String, ArrayList<String>> resourcePermissions) throws RemoteException;

    public AuthValue
        checkModResAccess(String userName, String resourceName, String permissionName,
                int reqBandwidth, int reqDuration, boolean specPathElems, boolean specGRI) throws RemoteException;

    public HashMap<String, Object> manageAaaObjects(HashMap<String, Object> parameters) throws RemoteException;


}
