package net.es.oscars.rmi.aaa;

import java.rmi.*;
import java.util.*;

import org.apache.log4j.Logger;

import net.es.oscars.PropHandler;
import net.es.oscars.aaa.AuthValue;
import net.es.oscars.aaa.AuthMultiValue;
import net.es.oscars.rmi.BaseRmiClient;

/**
 * AAA RMI client
 *
 * Includes the wrapper functions for RMI calls to AAA RMI Servers
 *
 * @author Evangelos Chaniotakis, Mary Thompson
 */
public class AaaRmiClient extends BaseRmiClient implements AaaRmiInterface {
    private Logger log = Logger.getLogger(AaaRmiClient.class);

    /**
     * The remote object
     */
    protected AaaRmiInterface remote;


    /**
     * Initializes the client and connects to the AAA RMI registry.
     *
     * @throws RemoteException
     */
    public void init() throws RemoteException {
        this.log.info("starting aaa rmi connection");

        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("rmi.aaa", true);
        this.setProps(props);
        // name of aaa Server in registry, will be reset from aaa.registeredServerName in rmi properties
        this.rmiServerName = AaaRmiInterface.registeredServerName;
        // used for logging in BaseRmiServer.init
        this.serviceName = "AAA RMI Client";
        super.configure();

        Remote remote = super.startConnection();

        if (this.connected) {
            this.setRemote((AaaRmiInterface) remote);
            super.setRemote(remote);
        }
        //this.log.debug("AaaRmiClient.init().end");
    }


    public HashMap<String, Object> manageAaaObjects(HashMap<String, Object> parameters) throws RemoteException {
        this.log.debug("manageAaaObjects.start");
        String methodName = "manageAaaObjects";
        HashMap<String, Object> result = null;
        this.verifyRmiConnection(methodName);
        try {
            result = this.remote.manageAaaObjects(parameters);
        } catch (RemoteException e) {
            this.log.debug("Remote exception from RMI server: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            this.log.error(methodName + ": Exception from RMI server" + e.getMessage(), e);
            throw new RemoteException(e.getMessage());
        }
        this.log.debug("manageAaaObjects.end");
        return result;
    }


    public Boolean validSession(String userName, String sessionName)
            throws RemoteException {
        this.log.debug("validSession.start");

        String methodName = "validSession";
        Boolean result = false;
        this.verifyRmiConnection(methodName);
        try {
            result = this.remote.validSession(userName, sessionName);
        } catch (RemoteException e) {
            this.log.debug("Remote exception from RMI server: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            this.log.error(methodName + ": Exception:" + e.getMessage(), e);
            throw new RemoteException(e.getMessage(), e);
        }
        this.log.debug("validSession.end");
        return result;
    }

    public String verifyLogin(String userName, String password, String sessionName)
            throws RemoteException {

        String methodName = "verifyLogin";
        this.log.debug("verifyLogin.start for: "+ userName);

        String result = null;
        this.verifyRmiConnection(methodName);
        try {
            result = this.remote.verifyLogin(userName, password, sessionName);
        } catch (RemoteException e) {
            this.log.debug("Remote exception from RMI server: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            this.log.error(methodName + ": Exception from RMI server" + e.getMessage(), e);
            throw new RemoteException(e.getMessage(), e);
        }
        this.log.debug("verifyLogin.end for: " + userName);
        return result;
    }


    public String verifyDN(String dn) throws RemoteException {

        String methodName = "verifyDN";
        this.log.debug("verifyDN.start for: " + dn);

        String result = null;
        this.verifyRmiConnection(methodName);

        try {
            result = this.remote.verifyDN(dn);
        } catch (RemoteException e) {
            this.log.debug("Remote exception from RMI server: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            this.log.error(methodName + ": Exception from RMI server" + e.getMessage(), e);
            throw new RemoteException(e.getMessage(), e);
        }
        this.log.debug("verifyDN.end for:" + dn);
        return result;
    }

    public String getInstitution(String userName) throws RemoteException {

        String methodName = "getInstitution";
        this.log.debug("getInstitution.start");

        String result = null;
        this.verifyRmiConnection(methodName);
        try {
            result = this.remote.getInstitution(userName);
        } catch (RemoteException e) {
            this.log.debug("Remote exception from RMI server: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            this.log.error(methodName + ": Exception from RMI server" + e.getMessage(), e);
            throw new RemoteException(e.getMessage(), e);
        }
        this.log.debug("getInstitution.end");
        return result;
    }
    
    public List<String> getDomainInstitutions(String topologyId) throws RemoteException {

        String methodName = "getDomainInstitutions";
        this.log.debug("getDomainInstitutions.start");

        List<String> result = null;
        this.verifyRmiConnection(methodName);
        try {
            result = this.remote.getDomainInstitutions(topologyId);
        } catch (RemoteException e) {
            this.log.debug("Remote exception from RMI server: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            this.log.error(methodName + ": Exception from RMI server" + e.getMessage(), e);
            throw new RemoteException(e.getMessage(), e);
        }
        this.log.debug("getDomainInstitutions.end");
        return result;
    }

    public AuthValue checkAccess(String userName, String resourceName, String permissionName)
      throws RemoteException {
        String methodName = "checkAccess";
        this.log.debug("checkAccess.start");

        AuthValue result = AuthValue.DENIED;
        this.verifyRmiConnection(methodName);
        try {
            result = this.remote.checkAccess(userName, resourceName, permissionName);
        } catch (RemoteException e) {
            this.log.debug("Remote exception from RMI server: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            this.log.debug(methodName + ": Exception from RMI server" + e.getMessage(), e);
            throw new RemoteException(e.getMessage(), e);
        }
        this.log.debug("checkAccess.end");
        return result;
    }

    public AuthMultiValue
        checkMultiAccess(String userName, HashMap<String, ArrayList<String>> resourcePermissions)
            throws RemoteException {
        String methodName = "checkMultiAccess";
        this.log.debug("checkMultiAccess.start");

        AuthMultiValue result = new AuthMultiValue();
        this.verifyRmiConnection(methodName);
        try {
            result = this.remote.checkMultiAccess(userName, resourcePermissions);
        } catch (RemoteException e) {
            this.log.debug("Remote exception from RMI server: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            this.log.error(methodName + ": Exception from RMI server" + e.getMessage(), e);
            throw new RemoteException(e.getMessage(), e);
        }
        this.log.debug("checkMultiAccess.end");
        return result;
    }

    public AuthValue
        checkModResAccess(String userName, String resourceName, String permissionName,
            int reqBandwidth, int reqDuration, boolean specPathElems, boolean specGRI)
                throws RemoteException {

        this.log.debug("checkModResAccess.start");
        String methodName = "checkModResAccess";
        AuthValue result = AuthValue.DENIED;
        this.verifyRmiConnection(methodName);
        try {
            result = this.remote.checkModResAccess(userName, resourceName, permissionName, reqBandwidth, reqDuration, specPathElems, specGRI);
        } catch (RemoteException e) {
            this.log.debug("Remote exception from RMI server: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            this.log.error(methodName + ": Exception from RMI server" + e.getMessage(), e);
            throw new RemoteException(e.getMessage(), e);
        }
        this.log.debug("checkModResAccess.end");
        return result;
    }
    public Boolean
        checkDomainAccess(String userName,String institutionName, String srcTopologyId,String destTopologyId)
            throws RemoteException {
        this.log.debug("checkDomainAccess.start");
        String methodName = "checkDomaniAccess";
        Boolean result = false;
        this.verifyRmiConnection(methodName);
        try {
            result = this.remote.checkDomainAccess(userName, institutionName, srcTopologyId, destTopologyId);
        } catch (RemoteException e) {
            this.log.debug("Remote exception from RMI server: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            this.log.error(methodName + ": Exception from RMI server" + e.getMessage(), e);
            throw new RemoteException(e.getMessage(), e);
        }
        this.log.debug("checkDomainAccess.end");
        return result;
    }
    /**
     * @return the remote
     */
    public AaaRmiInterface getRemote() {
        return remote;
    }

    /**
     * @param remote the remote to set
     */
    public void setRemote(AaaRmiInterface remote) {
        super.setRemote(remote);
        this.remote = remote;
    }
}
