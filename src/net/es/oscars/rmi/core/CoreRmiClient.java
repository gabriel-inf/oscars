package net.es.oscars.rmi.core;

import java.rmi.*;
import java.rmi.registry.*;
import java.util.*;
import java.io.IOException;
import java.lang.reflect.*;

import net.es.oscars.PropHandler;
import net.es.oscars.rmi.bss.*;
import net.es.oscars.rmi.aaa.*;
import net.es.oscars.rmi.notify.*;
import net.es.oscars.aaa.AuthValue;
import net.es.oscars.aaa.AuthMultiValue;
import net.es.oscars.aaa.Resource;

import org.apache.log4j.Logger;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.w3.www._2005._08.addressing.EndpointReferenceType;

public class CoreRmiClient implements CoreRmiInterface {
    private Logger log;
    private CoreRmiInterface remote;
    private AaaRmiClient aaaRmiClient;
    private BssRmiClient bssRmiClient;
    private NotifyRmiClient notifyRmiClient;


    private boolean connected;


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

        this.remote = null;
        this.log.debug("CoreRmiClientInit.start");
        this.connected = true;
        int port = rmiPort;  // default rmi registry port

        this.aaaRmiClient = new AaaRmiClient();
        this.bssRmiClient = new BssRmiClient();
        this.notifyRmiClient = new NotifyRmiClient();

        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("rmi", true);
        if (props.getProperty("registryPort") != null ) {
            try {
                port = Integer.decode(props.getProperty("registryPort"));
            } catch (NumberFormatException e) { }
        }
        try {
            String rmiIpaddr = localhost;

            if (props.getProperty("serverIpaddr") != null && !props.getProperty("serverIpaddr").equals("")) {
                rmiIpaddr = props.getProperty("serverIpaddr");
            }
            Registry registry = LocateRegistry.getRegistry(rmiIpaddr, port);

            this.remote = (CoreRmiInterface) registry.lookup(registryName);
            this.log.debug("Got remote object \n" + remote.toString());
            this.connected = true;

            this.bssRmiClient.setRemote(remote);
            this.aaaRmiClient.setRemote(remote);
            this.notifyRmiClient.setRemote(remote);
            this.bssRmiClient.setConnected(connected);
            this.aaaRmiClient.setConnected(connected);
            this.notifyRmiClient.setConnected(connected);
            this.log.debug("Connected to "+registryName+" server");
        } catch (RemoteException e) {
            this.connected = false;
            this.bssRmiClient.setConnected(connected);
            this.aaaRmiClient.setConnected(connected);
            this.notifyRmiClient.setConnected(connected);
            this.log.warn("Remote exception from RMI server: trying to access " + this.remote.toString(), e);
            throw e;
        } catch (NotBoundException e) {
            this.connected = false;
            this.bssRmiClient.setConnected(connected);
            this.aaaRmiClient.setConnected(connected);
            this.notifyRmiClient.setConnected(connected);
            this.log.warn("Trying to access unregistered remote object: ", e);
        } catch (Exception e) {
            this.connected = false;
            this.bssRmiClient.setConnected(connected);
            this.aaaRmiClient.setConnected(connected);
            this.notifyRmiClient.setConnected(connected);
            this.log.warn("Could not connect", e);
        }
        this.log.debug("CoreRmiClientInit.end");
    }



    public String verifyLogin(String userName, String password, String sessionName) throws RemoteException {
        return this.aaaRmiClient.verifyLogin(userName, password, sessionName);
    }
    public String verifyDN(String dn) throws RemoteException {
        return this.aaaRmiClient.verifyDN(dn);
    }

    public Boolean validSession(String userName, String sessionName) throws RemoteException {
        return this.aaaRmiClient.validSession(userName, sessionName);
    }

    public AuthValue checkAccess(String userName, String resourceName, String permissionName) throws RemoteException {
        return this.aaaRmiClient.checkAccess(userName, resourceName, permissionName);
    }

    public AuthMultiValue checkMultiAccess(String userName, HashMap<String, ArrayList<String>> resourcePermissions) throws RemoteException {
        return this.aaaRmiClient.checkMultiAccess(userName, resourcePermissions);
    }

    public AuthValue checkModResAccess(String userName, String resourceName, String permissionName,
                int reqBandwidth, int reqDuration, boolean specPathElems, boolean specGRI)
                    throws RemoteException {
        return this.aaaRmiClient.checkModResAccess(userName, resourceName, permissionName, reqBandwidth, reqDuration, specPathElems, specGRI);
    }

    public HashMap<String, Object> manageAaaObjects(HashMap<String, Object> parameters) throws RemoteException {
        return this.aaaRmiClient.manageAaaObjects(parameters);
    }

    
    // Notify RMI stuff.

    public String checkSubscriptionId(String address, EndpointReferenceType msgSubRef) throws RemoteException {
    	return this.notifyRmiClient.checkSubscriptionId(address, msgSubRef);
    }
    
    public void Notify(Notify request) throws RemoteException {
    	this.notifyRmiClient.Notify(request);
    }



    public HashMap<String, Object> createReservation(HashMap<String, Object> params, String userName)
        throws IOException, RemoteException {
        return this.bssRmiClient.createReservation(params, userName);
    }

    public HashMap<String, Object> queryReservation(HashMap<String, Object> params, String userName)
            throws IOException, RemoteException {
        return this.bssRmiClient.queryReservation(params, userName);
    }


    public HashMap<String, Object> listReservations(HashMap<String, Object> params, String userName)
        throws IOException, RemoteException {
        return this.bssRmiClient.listReservations(params, userName);
    }

    public HashMap<String, Object> cancelReservation(HashMap<String, Object> params, String userName)
        throws IOException, RemoteException {
        return this.bssRmiClient.cancelReservation(params, userName);
    }

    public HashMap<String, Object> modifyReservation(HashMap<String, Object> params, String userName)
        throws IOException, RemoteException {
        return this.bssRmiClient.modifyReservation(params, userName);
    }

    public HashMap<String, Object> createPath(HashMap<String, Object> params, String userName)
        throws IOException, RemoteException {
        return this.bssRmiClient.createPath(params, userName);
    }


    public HashMap<String, Object> teardownPath(HashMap<String, Object> params, String userName)
        throws IOException, RemoteException {
        return this.bssRmiClient.teardownPath(params, userName);
    }

    public HashMap<String, Object> modifyStatus(HashMap<String, Object> params, String userName)
        throws IOException, RemoteException {
        return this.bssRmiClient.modifyStatus(params, userName);
    }

}
