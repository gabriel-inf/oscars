package net.es.oscars.rmi;

import java.io.*;
import java.util.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.*;

import net.es.oscars.notify.RemoteEventProducer;

import org.apache.log4j.*;

public class CoreRmiServer implements CoreRmiInterface {
    private Logger log;
    private Registry registry;

    /* Make remote object static so GarbageCollector doesn't delete it */
    public static  CoreRmiServer staticObject;
    private CoreRmiInterface stub;
    private RmiHandlerSwitchboard switchboard;

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
        this.registry = LocateRegistry.createRegistry(8091);

        this.stub = (CoreRmiInterface) UnicastRemoteObject.exportObject(CoreRmiServer.staticObject, 0);

        this.registry.rebind("IDCRMIServer", this.stub);
        this.switchboard = new RmiHandlerSwitchboard();
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
     *   @return HashMap<String, Object> - out values to pour into jason Object.
     */
    
    public HashMap<String, Object> createReservation(HashMap<String, String[]> inputMap, String userName) 
        throws IOException, RemoteException {
        return this.switchboard.createReservation(inputMap, userName);
    }

    /**
     *   queryReservation
     *   
     *   @param inputMap HashMap<String, String[]> - contains input from web request
     *   @param String userName - authenticated login name of user
     *   @return HashMap<String, Object> - out values to pour into jason Object.
     */
   
    public HashMap<String, Object> queryReservation(HashMap<String, String[]> inputMap, String userName) 
        throws IOException, RemoteException {
        return this.switchboard.queryReservation(inputMap, userName);
    }
    
    /**
     *   listReservations
     *   
     *   @param inputMap HashMap<String, String[]> - contains input from web request
     *   @param String userName - authenticated login name of user
     *   @return HashMap<String, Object> - out values to pour into jason Object.
     */
   
    public HashMap<String, Object> listReservations(HashMap<String, String[]> inputMap, String userName) 
        throws IOException, RemoteException {
        return this.switchboard.listReservations(inputMap, userName);
    }
    
    /**
     *   cancelReservation
     *   @param inputMap HashMap<String, String[]> - contains input from web request
     *   @param String userName - authenticated login name of user
     *   @return HashMap<String, Object> - out values to pour into jason Object.
     */
    
    public HashMap<String, Object> cancelReservation(HashMap<String, String[]> inputMap, String userName) 
        throws IOException, RemoteException {
        return this.switchboard.cancelReservation(inputMap, userName);
    }
}