package net.es.oscars.rmi.core;

import java.rmi.*;
import java.util.*;
import java.io.IOException;

import net.es.oscars.PropHandler;
import net.es.oscars.PropertyLoader;
import net.es.oscars.rmi.bss.*;
import net.es.oscars.rmi.aaa.*;
import net.es.oscars.rmi.notify.*;
import net.es.oscars.rmi.BaseRmiClient;
import net.es.oscars.rmi.bss.xface.*;

import net.es.oscars.aaa.AuthValue;
import net.es.oscars.aaa.AuthMultiValue;

import org.apache.log4j.Logger;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.w3.www._2005._08.addressing.EndpointReferenceType;

public class CoreRmiClient extends BaseRmiClient implements CoreRmiInterface {
    protected Logger log;
    protected CoreRmiInterface remote = null;
    private AaaRmiClient aaaRmiClient;
    private BssRmiClient bssRmiClient;
    private NotifyRmiClient notifyRmiClient;


    public CoreRmiClient() {
        this.log = Logger.getLogger(this.getClass());
    }

    /**
     * init
     *   By default initializes the RMI server registry  port to 1099 (the default)
     *   This can be overridden by the oscars.properties rmi.registryPort and must
     *   match the port that CoreRmiServer.init used.
     *
     * @throws RemoteException
     */
    public void init() throws RemoteException {

        this.log.info("CoreRmiClientInit.start");

        this.aaaRmiClient = new AaaRmiClient();
        this.bssRmiClient = new BssRmiClient();
        this.notifyRmiClient = new NotifyRmiClient();
        Properties props = PropertyLoader.loadProperties("rmi.properties","core",true);
        this.setProps(props);
        this.configure();

        try {
            this.remote = (CoreRmiInterface) this.startConnection();
            super.setRemote(remote);
            if (this.remote == null) {
                this.log.error("Remote is null!");
            }

            this.connected = true;

            this.bssRmiClient.setRemote(remote);
            this.aaaRmiClient.setRemote(remote);
            this.notifyRmiClient.setRemote(remote);
            this.bssRmiClient.setConnected(connected);
            this.aaaRmiClient.setConnected(connected);
            this.notifyRmiClient.setConnected(connected);

        } catch (RemoteException e) {
            this.connected = false;
            this.bssRmiClient.setConnected(connected);
            this.aaaRmiClient.setConnected(connected);
            this.notifyRmiClient.setConnected(connected);
            String remoteStr = (this.remote != null ? this.remote.toString() : "RMI server but it is not running");
            this.log.warn("Remote exception from RMI server: trying to access " + remoteStr, e);
            throw e;
        } catch (Exception e) {
            this.connected = false;
            this.bssRmiClient.setConnected(connected);
            this.aaaRmiClient.setConnected(connected);
            this.notifyRmiClient.setConnected(connected);
            this.log.warn("Could not connect", e);
        }
        this.log.info("CoreRmiClientInit.end");
    }



    public String verifyLogin(String userName, String password, String sessionName) throws RemoteException {
        if (!this.connected) {
            this.init();
        }
        return this.aaaRmiClient.verifyLogin(userName, password, sessionName);
    }
    public String verifyDN(String dn) throws RemoteException {
        if (!this.connected) {
            this.init();
        }
        return this.aaaRmiClient.verifyDN(dn);
    }

    public Boolean validSession(String userName, String sessionName) throws RemoteException {
        if (!this.connected) {
            this.init();
        }
        return this.aaaRmiClient.validSession(userName, sessionName);
    }

    public AuthValue checkAccess(String userName, String resourceName, String permissionName) throws RemoteException {
        if (!this.connected) {
            this.init();
        }
        return this.aaaRmiClient.checkAccess(userName, resourceName, permissionName);
    }

    public String getInstitution(String userName) throws RemoteException {
        if (!this.connected) {
            this.init();
        }
        return this.aaaRmiClient.getInstitution(userName);
    }

    public AuthMultiValue checkMultiAccess(String userName, HashMap<String, ArrayList<String>> resourcePermissions) throws RemoteException {
        if (!this.connected) {
            this.init();
        }
        return this.aaaRmiClient.checkMultiAccess(userName, resourcePermissions);
    }

    public AuthValue checkModResAccess(String userName, String resourceName, String permissionName,
                int reqBandwidth, int reqDuration, boolean specPathElems, boolean specGRI)
                    throws RemoteException {
        if (!this.connected) {
            this.init();
        }
        return this.aaaRmiClient.checkModResAccess(userName, resourceName, permissionName, reqBandwidth, reqDuration, specPathElems, specGRI);
    }

    public HashMap<String, Object> manageAaaObjects(HashMap<String, Object> parameters) throws RemoteException {
        if (!this.connected) {
            this.init();
        }
        return this.aaaRmiClient.manageAaaObjects(parameters);
    }


    // Notify RMI stuff.

    public String checkSubscriptionId(String address, EndpointReferenceType msgSubRef) throws RemoteException {
        if (!this.connected) {
            this.init();
        }
        return this.notifyRmiClient.checkSubscriptionId(address, msgSubRef);
    }

    public void Notify(Notify request) throws RemoteException {
        if (!this.connected) {
            this.init();
        }
        this.notifyRmiClient.Notify(request);
    }



    public HashMap<String, Object> createReservation(HashMap<String, Object> params, String userName)
        throws IOException, RemoteException {
        if (!this.connected) {
            this.init();
        }
        return this.bssRmiClient.createReservation(params, userName);
    }

    public HashMap<String, Object> queryReservation(HashMap<String, Object> params, String userName)
            throws IOException, RemoteException {
        if (!this.connected) {
            this.init();
        }
        return this.bssRmiClient.queryReservation(params, userName);
    }


    public RmiListResReply listReservations(RmiListResRequest request,
                                            String userName)
        throws IOException, RemoteException {

        if (!this.connected) {
            this.init();
        }
        return this.bssRmiClient.listReservations(request, userName);
    }

    public HashMap<String, Object> cancelReservation(HashMap<String, Object> params, String userName)
        throws IOException, RemoteException {
        if (!this.connected) {
            this.init();
        }
        return this.bssRmiClient.cancelReservation(params, userName);
    }

    public HashMap<String, Object> modifyReservation(HashMap<String, Object> params, String userName)
        throws IOException, RemoteException {
        if (!this.connected) {
            this.init();
        }
        return this.bssRmiClient.modifyReservation(params, userName);
    }

    public HashMap<String, Object> createPath(HashMap<String, Object> params, String userName)
        throws IOException, RemoteException {
        if (!this.connected) {
            this.init();
        }
        return this.bssRmiClient.createPath(params, userName);
    }


    public HashMap<String, Object> teardownPath(HashMap<String, Object> params, String userName)
        throws IOException, RemoteException {
        if (!this.connected) {
            this.init();
        }
        return this.bssRmiClient.teardownPath(params, userName);
    }

    public HashMap<String, Object> modifyStatus(HashMap<String, Object> params, String userName)
        throws IOException, RemoteException {
        if (!this.connected) {
            this.init();
        }
        return this.bssRmiClient.modifyStatus(params, userName);
    }

}
