package net.es.oscars.rmi.core;

import java.io.*;
import java.util.*;

import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.net.*;
import java.net.UnknownHostException;

import net.es.oscars.aaa.AuthValue;
import net.es.oscars.aaa.AuthMultiValue;
import net.es.oscars.aaa.Resource;
import net.es.oscars.rmi.*;
import net.es.oscars.rmi.aaa.*;
import net.es.oscars.rmi.bss.*;
import net.es.oscars.rmi.bss.xface.*;
import net.es.oscars.rmi.notify.*;

import net.es.oscars.PropHandler;
import net.es.oscars.PropertyLoader;

import org.apache.log4j.*;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.w3.www._2005._08.addressing.EndpointReferenceType;

public class CoreRmiServer extends BaseRmiServer implements CoreRmiInterface  {
    private Logger log;

    /* Make remote object static so GarbageCollector doesn't delete it */
    protected static CoreRmiServer staticObject;
    private BssRmiServer bssRmiServer;
    private AaaRmiInterface aaaRmiServer;
    private NotifyRmiServer notifyRmiServer;

    /**
     * CoreRmiServer constructor
     * @throws RemoteException
     */
    public CoreRmiServer() throws RemoteException {
        this.log = Logger.getLogger(this.getClass());
    }

    /**
     * init
     *   By default initializes the RMI server registry to listen on port 1099 (the default)
     *   the RMI server to listen on a random port, and both to listen only on the loopback
     *   interface. These values can be overidden by oscars.properties.
     *   Setting the serverIpaddr to localhost will allow access from remote hosts and
     *   invalidate our security assumptions.
     *
     * @throws remoteException
     */
    public void init() throws RemoteException {

        CoreRmiServer.staticObject = this;

        /*
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("rmi", true);
         */

        Properties props = PropertyLoader.loadProperties("rmi.properties", "core", true);
        this.setProperties(props);

        this.setRmiServiceName("IDCRMIServer");
        this.setServiceName("OSCARS Core RMI Server");

        super.init(staticObject);

        this.bssRmiServer = new BssRmiServer();
        this.bssRmiServer.initHandlers();

        this.aaaRmiServer = new AaaRmiClient();
        this.aaaRmiServer.init();

        this.notifyRmiServer = new NotifyRmiServer();
        this.notifyRmiServer.initHandlers();

    }

    /**
     * shutdown
     */
    public void shutdown() {
        super.shutdown(staticObject);
    }




    public Boolean validSession(String userName, String sessionName) throws RemoteException {
        return this.aaaRmiServer.validSession(userName, sessionName);
    }

    public String verifyLogin(String userName, String password, String sessionName) throws RemoteException {
        return this.aaaRmiServer.verifyLogin(userName, password, sessionName);
    }

    public String verifyDN(String dn) throws RemoteException {
        return this.aaaRmiServer.verifyDN(dn);
    }

    public String getInstitution(String userName) throws RemoteException {
        return this.aaaRmiServer.getInstitution(userName);
    }

    public AuthValue checkAccess(String userName, String resourceName, String permissionName) throws RemoteException {
        return this.aaaRmiServer.checkAccess(userName, resourceName, permissionName);
    }

    public AuthMultiValue checkMultiAccess(String userName, HashMap<String, ArrayList<String>> resourcePermissions) throws RemoteException {
        return this.aaaRmiServer.checkMultiAccess(userName, resourcePermissions);
    }

    public AuthValue
        checkModResAccess(String userName, String resourceName, String permissionName,
            int reqBandwidth, int reqDuration, boolean specPathElems, boolean specGRI) throws RemoteException {
        return this.aaaRmiServer.checkModResAccess(userName, resourceName, permissionName, reqBandwidth, reqDuration, specPathElems, specGRI);
    }


    public HashMap<String, Object> manageAaaObjects(HashMap<String, Object> parameters) throws RemoteException {
        return this.aaaRmiServer.manageAaaObjects(parameters);
    }



    public String checkSubscriptionId(String address, EndpointReferenceType msgSubRef) throws RemoteException {
        return this.notifyRmiServer.checkSubscriptionId(address, msgSubRef);
    }

    public void Notify(Notify request) throws RemoteException {
        this.notifyRmiServer.Notify(request);
    }


    /**
     * createReservation
     *
     * @param params HashMap<String, Object> - contains input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into json Object.
     * @throws IOException
     * @throws RemoteException
     */
    public HashMap<String, Object>
        createReservation(HashMap<String, Object> params, String userName)
            throws IOException, RemoteException {
        return this.bssRmiServer.createReservation(params, userName);
    }

    /**
     * queryReservation
     *
     * @param params HashMap<String, Object> - input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into json Object.
     * @throws IOException
     * @throws RemoteException
     */
    public HashMap<String, Object>
        queryReservation(HashMap<String, Object> params, String userName)
            throws IOException, RemoteException {
        return this.bssRmiServer.queryReservation(params, userName);
    }

    /**
     * listReservations
     *
     * @param request RmiListResRequest - contains criteria from another component
     * @param userName string with authenticated login name of user
     * @return RmiListResReply list of reservations satisfying criteria
     */
    public RmiListResReply
        listReservations(RmiListResRequest request, String userName)
            throws IOException, RemoteException {
        return this.bssRmiServer.listReservations(request, userName);
    }

    /**
     * cancelReservationOverride
     * @param params HashMap<String, Object> - contains input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into json Object.
     * @throws IOException
     * @throws RemoteException
     */
    public HashMap<String, Object>
        cancelReservation(HashMap<String, Object> params, String userName)
            throws IOException, RemoteException {
        return this.bssRmiServer.cancelReservation(params, userName);
    }

    /**
     * modifyReservation
     *
     * @param params HashMap<String, Object> - contains input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into json Object.
     * @throws IOException
     * @throws RemoteException
     */
    public HashMap<String, Object>
        modifyReservation(HashMap<String, Object> params, String userName)
            throws IOException, RemoteException {
        return this.bssRmiServer.modifyReservation(params, userName);
    }

    /**
     * createPath
     *
     * @param params HashMap<String, Object> - contains input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into json Object.
     * @throws IOException
     * @throws RemoteException
     */
    public HashMap<String, Object>
        createPath(HashMap<String, Object> params, String userName)
            throws IOException, RemoteException {
        return this.bssRmiServer.createPath(params, userName);
    }

    /**
     * teardownPath
     *
     * @param params HashMap<String, Object> - contains input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into json Object.
     * @throws IOException
     * @throws RemoteException
     */
    public HashMap<String, Object>
        teardownPath(HashMap<String, Object> params, String userName)
            throws IOException, RemoteException {
        return this.bssRmiServer.teardownPath(params, userName);
    }

    /**
     * modifyStatus
     *
     * @param params HashMap<String, Object> - contains input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into json Object.
     * @throws IOException
     * @throws RemoteException
     */
    public HashMap<String, Object>
        modifyStatus(HashMap<String, Object> params, String userName)
            throws IOException, RemoteException {
        return this.bssRmiServer.modifyStatus(params, userName);
    }
}
