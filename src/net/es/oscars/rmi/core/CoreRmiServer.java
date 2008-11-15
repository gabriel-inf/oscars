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

import net.es.oscars.PropHandler;

import org.apache.log4j.*;

public class CoreRmiServer  implements CoreRmiInterface  {
    private Logger log;
    private Registry registry;

    /* Make remote object static so GarbageCollector doesn't delete it */
    public static CoreRmiServer staticObject;
    private CoreRmiInterface stub;
    private BssRmiServer bssRmiServer;
    private AaaRmiServer aaaRmiServer;

    /**
     * CoreRmiServer constructor
     * @throws RemoteException
     */
    public CoreRmiServer() throws RemoteException {
        this.log = Logger.getLogger(this.getClass());
        CoreRmiServer.staticObject = this;
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
        this.log.debug("CoreRmiServer.init().start");
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("rmi", true);
        int port = rmiPort;
        if (props.getProperty("registryPort") != null) {
            try {
                port = Integer.decode(props.getProperty("registryPort"));
            } catch (NumberFormatException e) { }
        }

        String rmiIpaddr = localhost;
        if (props.getProperty("serverIpaddr") != null && !props.getProperty("serverIpaddr").equals("")) {
            rmiIpaddr = props.getProperty("serverIpaddr");
        }
        this.log.info("CoreRmiServer listening on " + rmiIpaddr);
        if (!rmiIpaddr.equals(localhost)){
            this.log.warn("CoreRmiServer listening on " + rmiIpaddr + " . Possible security vulnerability");
        }
        InetAddress ipAddr = null;
        AnchorSocketFactory sf = null;
        // Causes the endPoint of the remote sever object to match the interface that is listened on
        System.setProperty("java.rmi.server.hostname",rmiIpaddr);
        try {
            ipAddr = InetAddress.getByName(rmiIpaddr);
            // creates a custom socket that only listens on ipAddr
            sf = new AnchorSocketFactory(ipAddr);
            this.registry = LocateRegistry.createRegistry(port, null, sf);
        } catch (UnknownHostException ex) {

        }

        port = rmiPort;
        if (props.getProperty("serverPort") != null) {
            try {
                port = Integer.decode(props.getProperty("serverPort"));
            } catch (NumberFormatException e) { }
        }
        this.stub = (CoreRmiInterface) UnicastRemoteObject.exportObject(CoreRmiServer.staticObject, port, null,sf);
        this.registry.rebind(registryName, this.stub);

        this.bssRmiServer = new BssRmiServer();
        this.bssRmiServer.initHandlers();
        this.aaaRmiServer = new AaaRmiServer();
        this.aaaRmiServer.initHandlers();

        this.log.debug("CoreRmiServer.init().end");
    }

    /**
     * shutdown
     */
    public void shutdown() {
        try {
            java.rmi.server.UnicastRemoteObject.unexportObject(CoreRmiServer.staticObject, true);
            java.rmi.server.UnicastRemoteObject.unexportObject(this.registry, true);
            this.registry.unbind(registryName);
        } catch (RemoteException ex) {
            this.log.error("Remote exception shutting down Core RMI server", ex);

        } catch (NotBoundException ex) {
            this.log.error("RMI Server already unbound", ex);
        }
    }




    public Boolean validSession(String userName, String sessionName) throws RemoteException {
        return this.aaaRmiServer.validSession(userName, sessionName);
    }

    public String verifyLogin(String userName, String password, String sessionName) throws RemoteException {
        return this.aaaRmiServer.verifyLogin(userName, password, sessionName);
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





    /**
     * createReservation
     *
     * @param inputMap HashMap<String, String[]> - contains input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into json Object.
     * @throws IOException
     * @throws RemoteException
     */
    public HashMap<String, Object>
        createReservation(HashMap<String, String[]> inputMap, String userName)
            throws IOException, RemoteException {
        return this.bssRmiServer.createReservation(inputMap, userName);
    }

    /**
     * queryReservation
     *
     * @param inputMap HashMap<String, String[]> - input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into json Object.
     * @throws IOException
     * @throws RemoteException
     */
    public HashMap<String, Object>
        queryReservation(HashMap<String, String[]> inputMap, String userName)
            throws IOException, RemoteException {
        return this.bssRmiServer.queryReservation(inputMap, userName);
    }

    /**
     * listReservations
     *
     * @param inputMap HashMap<String, String[]> - contains input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into json Object.
     */
    public HashMap<String, Object>
        listReservations(HashMap<String, String[]> inputMap, String userName)
            throws IOException, RemoteException {
        return this.bssRmiServer.listReservations(inputMap, userName);
    }

    /**
     * cancelReservationOverride
     * @param inputMap HashMap<String, String[]> - contains input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into json Object.
     * @throws IOException
     * @throws RemoteException
     */
    public HashMap<String, Object>
        cancelReservation(HashMap<String, String[]> inputMap, String userName)
            throws IOException, RemoteException {
        return this.bssRmiServer.cancelReservation(inputMap, userName);
    }

    /**
     * modifyReservation
     *
     * @param inputMap HashMap<String, String[]> - contains input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into json Object.
     * @throws IOException
     * @throws RemoteException
     */
    public HashMap<String, Object>
        modifyReservation(HashMap<String, String[]> inputMap, String userName)
            throws IOException, RemoteException {
        return this.bssRmiServer.modifyReservation(inputMap, userName);
    }

    /**
     * createPath
     *
     * @param inputMap HashMap<String, String[]> - contains input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into json Object.
     * @throws IOException
     * @throws RemoteException
     */
    public HashMap<String, Object>
        createPath(HashMap<String, String[]> inputMap, String userName)
            throws IOException, RemoteException {
        return this.bssRmiServer.createPath(inputMap, userName);
    }

    /**
     * teardownPath
     *
     * @param inputMap HashMap<String, String[]> - contains input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into json Object.
     * @throws IOException
     * @throws RemoteException
     */
    public HashMap<String, Object>
        teardownPath(HashMap<String, String[]> inputMap, String userName)
            throws IOException, RemoteException {
        return this.bssRmiServer.teardownPath(inputMap, userName);
    }

    /**
     * modifyStatus
     *
     * @param inputMap HashMap<String, String[]> - contains input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into json Object.
     * @throws IOException
     * @throws RemoteException
     */
    public HashMap<String, Object>
        modifyStatus(HashMap<String, String[]> inputMap, String userName)
            throws IOException, RemoteException {
        return this.bssRmiServer.modifyStatus(inputMap, userName);
    }
}
