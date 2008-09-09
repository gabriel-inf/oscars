package net.es.oscars.rmi;

import java.io.*;
import java.util.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
//import java.rmi.server.UnicastRemoteObject;
import java.rmi.server.*;
import java.rmi.*;
import java.net.*;
import java.net.UnknownHostException;

import net.es.oscars.PropHandler;

import org.apache.log4j.*;

public class CoreRmiServer  implements CoreRmiInterface  {
    private Logger log;
    private Registry registry;

    /* Make remote object static so GarbageCollector doesn't delete it */
    public static  CoreRmiServer staticObject;
    private CoreRmiInterface stub;
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
    public CoreRmiServer() throws RemoteException {
        this.log = Logger.getLogger(this.getClass());
        CoreRmiServer.staticObject = this;
    }

    /**
     * init
     * @throws remoteException
     */
    public void init() throws RemoteException {
        this.log.debug("init.start");
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("rmi", true);
        int port = 1099;
        if (props.getProperty("registryPort") != null) {
            try {
                port = Integer.decode(props.getProperty("registryPort"));
            } catch (NumberFormatException e) { }
        }

        String rmiIpaddr = "127.0.0.1";
        if (props.getProperty("serverIpaddr") != null && !props.getProperty("serverIpaddr").equals("")) {
            rmiIpaddr = props.getProperty("serverIpaddr");
        }

        try {
            InetAddress ipAddr = InetAddress.getByName(rmiIpaddr);
            AnchorSocketFactory sf = new AnchorSocketFactory(ipAddr);
            this.registry = LocateRegistry.createRegistry(port, null, sf);
        } catch (UnknownHostException ex) {

        }
            // LocateRegistry.createRegistry(port);

        port = 0;
        if (props.getProperty("serverPort") != null) {
            try {
                port = Integer.decode(props.getProperty("serverPort"));
            } catch (NumberFormatException e) { }
        }
        this.stub = (CoreRmiInterface) UnicastRemoteObject.exportObject(CoreRmiServer.staticObject, port);
        this.registry.rebind("IDCRMIServer", this.stub);
        this.createHandler = new CreateResRmiHandler();
        this.queryHandler = new QueryResRmiHandler();
        this.listHandler = new ListResRmiHandler();
        this.cancelHandler = new CancelResRmiHandler();
        this.modifyHandler = new ModifyResRmiHandler();
        this.unsafeTeardownPathHandler = new UnsafeTeardownPathRmiHandler();
        this.unsafeCreatePathHandler = new UnsafeCreatePathRmiHandler();
        this.unsafeModifyStatusHandler = new UnsafeModifyStatusRmiHandler();
        this.log.debug("init.end");
    }

    /**
     * shutdown
     */
    public void shutdown() {

        try {
            java.rmi.server.UnicastRemoteObject.unexportObject(CoreRmiServer.staticObject, true);
            java.rmi.server.UnicastRemoteObject.unexportObject(this.registry, true);


            this.registry.unbind("IDCRMIServer");
        } catch (RemoteException ex) {
            this.log.error("Remote exception shutting down RMI server", ex);

        } catch (NotBoundException ex) {
            this.log.error("RMI Server already unbound", ex);
        }
    }

    /**
     *   createReservation
     *
     *   @param inputMap HashMap<String, String[]> - contains input from web request
     *   @param String userName - authenticated login name of user
     *   @return HashMap<String, Object> - out values to pour into json Object.
     */

    public HashMap<String, Object> createReservation(HashMap<String, String[]> inputMap, String userName)
        throws IOException, RemoteException {
        if (!checkClientHost()){
            throw new RemoteException("rmi call from non-local host");
        }
        return this.createHandler.createReservation(inputMap, userName);
    }

    /**
     *   queryReservation
     *
     *   @param inputMap HashMap<String, String[]> - contains input from web request
     *   @param String userName - authenticated login name of user
     *   @return HashMap<String, Object> - out values to pour into json Object.
     */

    public HashMap<String, Object> queryReservation(HashMap<String, String[]> inputMap, String userName)
        throws IOException, RemoteException {
        if (!checkClientHost()){
            throw new RemoteException("rmi call from non-local host");
        }
        return this.queryHandler.queryReservation(inputMap, userName);
    }

    /**
     *   listReservations
     *
     *   @param inputMap HashMap<String, String[]> - contains input from web request
     *   @param String userName - authenticated login name of user
     *   @return HashMap<String, Object> - out values to pour into json Object.
     */

    public HashMap<String, Object> listReservations(HashMap<String, String[]> inputMap, String userName)
        throws IOException, RemoteException {
        if (!checkClientHost()){
            throw new RemoteException("rmi call from non-local host");
        }
        return this.listHandler.listReservations(inputMap, userName);
    }

    /**
     *   cancelReservationOverride
     *   @param inputMap HashMap<String, String[]> - contains input from web request
     *   @param String userName - authenticated login name of user
     *   @return HashMap<String, Object> - out values to pour into json Object.
     */

    public HashMap<String, Object> cancelReservation(HashMap<String, String[]> inputMap, String userName)
        throws IOException, RemoteException {
        if (!checkClientHost()){
            throw new RemoteException("rmi call from non-local host");
        }
        return this.cancelHandler.cancelReservation(inputMap, userName);
    }

    /**
     *   modifyReservation
     *
     *   @param inputMap HashMap<String, String[]> - contains input from web request
     *   @param String userName - authenticated login name of user
     *   @return HashMap<String, Object> - out values to pour into json Object.
     */

    public HashMap<String, Object> modifyReservation(HashMap<String, String[]> inputMap, String userName)
        throws IOException, RemoteException {
        if (!checkClientHost()){
            throw new RemoteException("rmi call from non-local host");
        }
        return this.modifyHandler.modifyReservation(inputMap, userName);
    }



    /**
     *   createPath
     *
     *   @param inputMap HashMap<String, String[]> - contains input from web request
     *   @param String userNaOverrideme - authenticated login name of user
     *   @return HashMap<String, Object> - out values to pour into json Object.
     */

    public HashMap<String, Object> createPath(HashMap<String, String[]> inputMap, String userName)
        throws IOException, RemoteException {
        if (!checkClientHost()){
            throw new RemoteException("rmi call from non-local host");
        }
        return this.unsafeCreatePathHandler.createPath(inputMap, userName);
    }

    /**
     *   teardownPath
     *
     *   @param inputMap HashMap<String, String[]> - contains input from web request
     *   @param String userName - authenticated login name of user
     *   @return HashMap<String, Object> - out values to pour into json Object.
     */

    public HashMap<String, Object> teardownPath(HashMap<String, String[]> inputMap, String userName)
        throws IOException, RemoteException {
        if (!checkClientHost()){
            throw new RemoteException("rmi call from non-local host");
        }
        return this.unsafeTeardownPathHandler.teardownPath(inputMap, userName);
    }

    /**
     *   modifyStatus
     *
     *   @param inputMap HashMap<String, String[]> - contains input from web request
     *   @param String userName - authenticated login name of user
     *   @return HashMap<String, Object> - out values to pour into json Object.
     */

    public HashMap<String, Object> modifyStatus(HashMap<String, String[]> inputMap, String userName)
        throws IOException, RemoteException {
        if (!checkClientHost()){
            throw new RemoteException("rmi call from non-local host");
        }
        return this.unsafeModifyStatusHandler.modifyStatus(inputMap, userName);
    }

    /**
     * Check that rmi call came from the local host
     * @return true/false
     */
    private boolean checkClientHost(){
        try {
            String remoteHost = RemoteServer.getClientHost();
            String localHost = InetAddress.getLocalHost().getHostAddress();
            //this.log.info("client host for list reservations is: " + remoteHost);
            //this.log.info ("local host ipAddr is " + localHost );
            if (remoteHost.equals(localHost)) {
                return true;
            }
            this.log.warn("rmiServer called by non-local host: " + remoteHost);
            return false;
        } catch (ServerNotActiveException e) {
            this.log.warn ("Can't get client host in listReservations");
            return false;
        } catch (UnknownHostException e) {
            this.log.warn("Can't get localHost Ipaddr");
            return false;
        }
    }

}