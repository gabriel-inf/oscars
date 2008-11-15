package net.es.oscars.rmi.bss;

import java.io.*;
import java.util.*;

import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.net.*;
import java.net.UnknownHostException;


import net.es.oscars.rmi.AnchorSocketFactory;
import net.es.oscars.PropHandler;

import org.apache.log4j.*;

public class BssRmiServer  implements BssRmiInterface {
    private Logger log = Logger.getLogger(BssRmiServer.class);
    private Registry registry;

    /* Make remote object static so GarbageCollector doesn't delete it */
    public static BssRmiServer staticObject;
    private BssRmiInterface stub;
    private CreateResRmiHandler createHandler;
    private QueryResRmiHandler queryHandler;
    private ListResRmiHandler listHandler;
    private CancelResRmiHandler cancelHandler;
    private ModifyResRmiHandler modifyHandler;
    private UnsafeCreatePathRmiHandler unsafeCreatePathHandler;
    private UnsafeTeardownPathRmiHandler unsafeTeardownPathHandler;
    private UnsafeModifyStatusRmiHandler unsafeModifyStatusHandler;

    /**
     * CoreRmiServer constructor
     * @throws RemoteException
     */
    public BssRmiServer() throws RemoteException {
        BssRmiServer.staticObject = this;
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
        this.log.debug("BssRmiServer.init().start");
        PropHandler propHandler = new PropHandler("rmi.properties");
        Properties props = propHandler.getPropertyGroup("bss", true);

        // default rmi registry port
        int rmiPort = BssRmiInterface.registryPort;
        // default rmi registry address
        String rmiIpaddr = BssRmiInterface.registryAddress;
        // default rmi registry name
        String rmiRegName = BssRmiInterface.registryName;

        if (props.getProperty("registryPort") != null && !props.getProperty("registryPort").equals("")) {
            try {
                rmiPort = Integer.decode(props.getProperty("registryPort"));
            } catch (NumberFormatException e) {
                this.log.warn(e);
            }
        }

        if (props.getProperty("registryAddress") != null && !props.getProperty("registryAddress").equals("")) {
            rmiIpaddr = props.getProperty("registryAddress");
        }

        if (props.getProperty("registryName") != null && !props.getProperty("registryName").equals("")) {
            rmiRegName = props.getProperty("registryName");
        }


        InetAddress ipAddr = null;
        AnchorSocketFactory sf = null;
        // Causes the endPoint of the remote sever object to match the interface that is listened on
        System.setProperty("java.rmi.server.hostname",rmiIpaddr);
        try {
            ipAddr = InetAddress.getByName(rmiIpaddr);
            // creates a custom socket that only listens on ipAddr
            sf = new AnchorSocketFactory(ipAddr);
            this.registry = LocateRegistry.createRegistry(rmiPort, null, sf);
        } catch (UnknownHostException ex) {
            this.log.error(ex);
        }

        this.stub = (BssRmiInterface) UnicastRemoteObject.exportObject(BssRmiServer.staticObject, rmiPort, null, sf);
        this.registry.rebind(rmiRegName, this.stub);
        this.initHandlers();
        this.log.debug("BssRmiServer.init().end");
    }

    public void initHandlers() {
        this.createHandler = new CreateResRmiHandler();
        this.queryHandler = new QueryResRmiHandler();
        this.listHandler = new ListResRmiHandler();
        this.cancelHandler = new CancelResRmiHandler();
        this.modifyHandler = new ModifyResRmiHandler();
        this.unsafeTeardownPathHandler = new UnsafeTeardownPathRmiHandler();
        this.unsafeCreatePathHandler = new UnsafeCreatePathRmiHandler();
        this.unsafeModifyStatusHandler = new UnsafeModifyStatusRmiHandler();
    }

    /**
     * shutdown
     */
    public void shutdown() {
        try {
            java.rmi.server.UnicastRemoteObject.unexportObject(BssRmiServer.staticObject, true);
            java.rmi.server.UnicastRemoteObject.unexportObject(this.registry, true);
            this.registry.unbind(registryName);
        } catch (RemoteException ex) {
            this.log.error("Remote exception shutting down BSS RMI server", ex);

        } catch (NotBoundException ex) {
            this.log.error("BSS RMI Server already unbound", ex);
        }
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

        return this.createHandler.createReservation(inputMap, userName);
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

        return this.queryHandler.queryReservation(inputMap, userName);
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
        return this.listHandler.listReservations(inputMap, userName);
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

        return this.cancelHandler.cancelReservation(inputMap, userName);
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

        return this.modifyHandler.modifyReservation(inputMap, userName);
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
        return this.unsafeCreatePathHandler.createPath(inputMap, userName);
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

        return this.unsafeTeardownPathHandler.teardownPath(inputMap, userName);
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

        return this.unsafeModifyStatusHandler.modifyStatus(inputMap, userName);
    }

}
