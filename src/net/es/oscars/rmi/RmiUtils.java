package net.es.oscars.rmi;

import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.HashMap;

import net.es.oscars.aaa.AuthValue;
import net.es.oscars.rmi.aaa.*;
import net.es.oscars.rmi.core.CoreRmiClient;
import net.es.oscars.rmi.core.CoreRmiInterface;
import net.es.oscars.servlets.ServletUtils;

import org.apache.log4j.Logger;

public class RmiUtils {

    public static CoreRmiInterface getCoreRmiClient(String methodName, Logger log) throws RemoteException {
        CoreRmiInterface rmiClient;
        rmiClient = new CoreRmiClient();
        try {
            rmiClient.init();
        } catch (RemoteException ex) {
            log.error(ex);
            throw ex;
        }
        return rmiClient;
    }

    public static AaaRmiInterface getAaaRmiClient(String methodName, Logger log) throws RemoteException {
        AaaRmiInterface rmiClient;
        rmiClient = new AaaRmiClient();
        rmiClient.init();
        return rmiClient;
    }

/* This was only called from servletsUtils and assumes it is called by a servlet since it sets the out 
 * parameter to the error message, so I moved the code the servletsUtil -mrt 

    public static AuthValue getAuth(String userName, String resourceName, String permissionName, AaaRmiInterface rmiClient, String methodName, Logger log, PrintWriter out) {
        HashMap<String, Object> authResult = new HashMap<String, Object>();
        AuthValue authVal;
        try {
            authVal = rmiClient.checkAccess(userName, resourceName, permissionName);
        } catch (RemoteException ex) {
            log.error("RMI exception:  " + ex.getMessage());
            ServletUtils.handleFailure(out, methodName + " RMI exception: " + ex.getMessage(), methodName);
            authVal = AuthValue.DENIED;
        }
        return authVal;
    }
     */

}
