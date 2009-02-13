package net.es.oscars.rmi.aaa;

import java.util.ArrayList;
import java.util.HashMap;
import java.rmi.Remote;
import java.rmi.RemoteException;
import net.es.oscars.aaa.AuthValue;
import net.es.oscars.aaa.AuthMultiValue;

public interface AaaRmiInterface extends Remote {

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
/**
 * Checks to see if the user belongs to the site containing either the specified start
 *   or end point
 * @param userName The name of the user.  May be null if institution is given
 * @param Institution Institution the user belongs to. May be null if userName is given
 * @param srcTopologyId the topologyIdentifer of the  start of a reservation
 * @param desTopologyId the topologyIdentifer of the  termination of a reservation
 * @return true or false
 */
    public Boolean
        checkDomainAccess(String userName,String institutionName,String srcTopologyId,String destTopologyId)
        throws RemoteException;
    
    public HashMap<String, Object> manageAaaObjects(HashMap<String, Object> parameters) throws RemoteException;


}
