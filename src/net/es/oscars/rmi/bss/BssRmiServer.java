package net.es.oscars.rmi.bss;

import java.io.*;
import java.util.*;
import java.rmi.*;

import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.events.OSCARSEvent;
import net.es.oscars.wsdlTypes.GetTopologyContent;
import net.es.oscars.wsdlTypes.GetTopologyResponseContent;
import net.es.oscars.rmi.*;
import net.es.oscars.PropHandler;
import net.es.oscars.rmi.bss.xface.*;

import org.apache.log4j.*;

public class BssRmiServer extends BaseRmiServer implements BssRmiInterface {

    /* Make remote object static so GarbageCollector doesn't delete it */
    public static BssRmiServer staticObject;
    private CreateResRmiHandler createHandler;
    private QueryResRmiHandler queryHandler;
    private ListResRmiHandler listHandler;
    private CancelResRmiHandler cancelHandler;
    private ModifyResRmiHandler modifyHandler;
    private TopologyRmiHandler topologyHandler;
    private PathRmiHandler pathHandler;
    private EventRmiHandler eventHandler;
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
     * Initializes the RMI server registry given values from oscars.properties.
     *
     * @throws remoteException
     */
    public void init() throws RemoteException {
        this.log = Logger.getLogger(this.getClass());

        BssRmiServer.staticObject = this;

        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("rmi.bss", true);
        this.setProperties(props);
        // name of bss Server in registry, may be reset from bss.registeredServerName in oscars.bss.rmi properties
        this.rmiServerName=BssRmiInterface.registeredServerName;
        // used for logging in BaseRmiServer.init
        this.serviceName="BSS RMI Server";

        super.init(staticObject);
        this.initHandlers();
    }

    /**
     * Initialize all the RMI server method handlers.
     */
    public void initHandlers() {
        this.createHandler = new CreateResRmiHandler();
        this.queryHandler = new QueryResRmiHandler();
        this.listHandler = new ListResRmiHandler();
        this.cancelHandler = new CancelResRmiHandler();
        this.modifyHandler = new ModifyResRmiHandler();
        this.topologyHandler = new TopologyRmiHandler();
        this.pathHandler = new PathRmiHandler();
        this.eventHandler = new EventRmiHandler();
        this.unsafeCreatePathHandler = new UnsafeCreatePathRmiHandler();
        this.unsafeTeardownPathHandler = new UnsafeTeardownPathRmiHandler();
        this.unsafeModifyStatusHandler = new UnsafeModifyStatusRmiHandler();
    }

    /**
     * Shut down the server.
     */
    public void shutdown() {
        super.shutdown(staticObject);
    }

    /**
     * Creates a reservation, given requested parameters.
     *
     * @param resvRequest - partially filled in reservation with requested params
     * @param userName string with authenticated login name of user
     * @return gri - new global reservation id assigned to reservation
     * @throws IOException
     * @throws RemoteException
     */
    public String
        createReservation(Reservation resvRequest, String userName)
            throws RemoteException {

        return this.createHandler.createReservation(resvRequest, userName);
    }

    /**
     * Returns a reservation's details, given its global reservation id.
     *
     * @param request string with global reservation id
     * @param userName string with authenticated login name of user
     * @return RmiQueryResReply bean containing reservation
     * @throws IOException
     * @throws RemoteException
     */
    public RmiQueryResReply
        queryReservation(String request, String userName)
           throws RemoteException {

        return this.queryHandler.queryReservation(request, userName);
    }

    /**
     * Lists all reservations satisfying constraints from client.
     *
     * @param request RmiListResRequest contains list constraints from client
     * @param userName string with authenticated login name of user
     * @return RmiListResReply list of reservations satisfying constraints
     */
    public RmiListResReply
        listReservations(RmiListResRequest request, String userName)
            throws RemoteException {
        return this.listHandler.listReservations(request, userName);
    }

    /**
     * Cancels a reservation, given its global reservation id.
     *
     * @param gri string containing reservation's global reservation id
     * @param userName string with authenticated login name of user
     * @throws RemoteException
     */
    public void
        cancelReservation(String gri, String userName)
            throws RemoteException {

        this.cancelHandler.cancelReservation(gri, userName);
    }

    /**
     * Submits job to modify a reservation, given new parameters.
     *
     * @param resv transient Reservation containing parameters to be modified
     * @param userName string with authenticated login name of user
     * @return persistentResv matching Reservation from database
     * @throws RemoteException
     */
    public Reservation
        modifyReservation(Reservation resv, String userName)
            throws  RemoteException {

        return this.modifyHandler.modifyReservation(resv, userName);
    }

    /**
     * Gets network topology.
     *
     * @param getTopoRequest Axis2 type containing network topology request
     * @param userName string with authenticated login name of user
     * @return Axis2 type containing network topology
     * @throws IOException
     * @throws RemoteException
     */
    public GetTopologyResponseContent
        getNetworkTopology(GetTopologyContent getTopoRequest, String userName)
            throws  RemoteException {
        return this.topologyHandler.getNetworkTopology(getTopoRequest,
                                                       userName);
    }

    /**
     * Sets up a path via signalling.  Can be interdomain.
     *
     * @param request RmiPathRequest containing request parameters
     * @param userName string with authenticated login name of user
     * @return result string with status of path setup for reservation
     * @throws IOException
     * @throws RemoteException
     */
    public String
        createPath(RmiPathRequest request, String userName)
            throws RemoteException {
        return this.pathHandler.createPath(request, userName);
    }

    /**
     * Refreshes a path via signaling.
     *
     * @param request RmiPathRequest containing request parameters
     * @param userName string with authenticated login name of user
     * @return result string with status of path refresh for reservation
     * @throws IOException
     * @throws RemoteException
     */
    public String
        refreshPath(RmiPathRequest request, String userName)
            throws RemoteException {
        return this.pathHandler.refreshPath(request, userName);
    }

    /**
     * Tears down a path via signaling.
     *
     * @param request RmiPathRequest containing request parameters
     * @param userName string with authenticated login name of user
     * @return result string with status of path teardown for reservation
     * @throws IOException
     * @throws RemoteException
     */
    public String
        teardownPath(RmiPathRequest request, String userName)
            throws  RemoteException {
        return this.pathHandler.teardownPath(request, userName);
    }
    
    /**
     * Handles an event received from another IDC
     * 
     * @param event the event received from another IDC
     */
    public void handleEvent(OSCARSEvent event) throws RemoteException {
        this.eventHandler.handleEvent(event);
    }

    /**
     * Forces path creation in the local domain for a reservation.
     * Requires additional authorization.
     *
     * @param request RmiPathRequest containing request parameters
     * @param userName string with authenticated login name of user
     * @return result string with status of path setup for reservation
     * @throws IOException
     * @throws RemoteException
     */
    public String
        unsafeCreatePath(RmiPathRequest request, String userName)
            throws RemoteException {
        return this.unsafeCreatePathHandler.unsafeCreatePath(request, userName);
    }

    /**
     * Forces path teardown in the local domain for a reservation.
     * Requires additional authorization
     *
     * @param request RmiPathRequest containing request parameters
     * @param userName string with login of user making request
     * @return result string with status of path teardown for reservation
     * @throws IOException
     * @throws RemoteException
     */
    public String
        unsafeTeardownPath(RmiPathRequest request, String userName)
            throws RemoteException {

        return this.unsafeTeardownPathHandler.unsafeTeardownPath(request,
                                                                 userName);
    }

    /**
     * Forces a status change in the given reservation.
     * Requires additional authorization.
     *
     * @param request RmiModifyStatusRequest containing request parameters
     * @param userName string with login of user making request
     * @return result string with status of forced status change for reservation
     * @throws IOException
     * @throws RemoteException
     */
    public String
        unsafeModifyStatus(RmiModifyStatusRequest request, String userName)
            throws  RemoteException {

        return this.unsafeModifyStatusHandler.unsafeModifyStatus(request,
                                                                 userName);
    }
    
}
