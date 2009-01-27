package net.es.oscars.rmi.bss;

import java.io.*;
import java.util.*;

import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.net.*;
import java.net.UnknownHostException;

import net.es.oscars.bss.Reservation;
import net.es.oscars.rmi.*;
import net.es.oscars.PropertyLoader;
import net.es.oscars.rmi.aaa.AaaRmiServer;
import net.es.oscars.rmi.bss.xface.*;

import org.apache.log4j.*;

public class BssRmiServer extends BaseRmiServer implements BssRmiInterface {
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
        this.log = Logger.getLogger(this.getClass());

        BssRmiServer.staticObject = this;

        Properties props = PropertyLoader.loadProperties("rmi.properties","bss",true);
        this.setProperties(props);

        this.setRmiServiceName("BSSRMIServer");
        this.setServiceName("BSS RMI Server");

        super.init(staticObject);
        this.initHandlers();
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
    }*/
    public void shutdown() {
        super.shutdown(staticObject);
    }

    /**
     * createReservation
     *
     * @param resvRequest - partially filled in reservation with requested params
     * @param userName string with authenticated login name of user
     * @return gri - new global reservation id assigned to reservation
     * @throws IOException
     * @throws RemoteException
     */
    public String
        createReservation(Reservation resvRequest, String userName)
            throws IOException, RemoteException {

        return this.createHandler.createReservation(resvRequest, userName);
    }

    /**
     * queryReservation
     *
     * @param request RmiQueryResRequest contains input from component
     * @param userName string with authenticated login name of user
     * @return RmiQueryResReply bean containing reservation
     * @throws IOException
     * @throws RemoteException
     */
    public RmiQueryResReply
        queryReservation(RmiQueryResRequest request, String userName)
            throws IOException, RemoteException {

        return this.queryHandler.queryReservation(request, userName);
    }

    /**
     * listReservations
     *
     * @param request RmiListResRequest contains list constraints from client
     * @param userName string with authenticated login name of user
     * @return RmiListResReply list of reservations satisfying constraints
     */
    public RmiListResReply
        listReservations(RmiListResRequest request, String userName)
            throws IOException, RemoteException {
        return this.listHandler.listReservations(request, userName);
    }

    /**
     * cancelReservationOverride
     * @param params HashMap<String, Object> - contains input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into json Object.
     * @throws IOException
     * @throws RemoteException
     */
    public void
        cancelReservation(String gri, String userName)
            throws RemoteException {

        this.cancelHandler.cancelReservation(gri, userName);
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

        return this.modifyHandler.modifyReservation(params, userName);
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
        return this.unsafeCreatePathHandler.createPath(params, userName);
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

        return this.unsafeTeardownPathHandler.teardownPath(params, userName);
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

        return this.unsafeModifyStatusHandler.modifyStatus(params, userName);
    }

}
