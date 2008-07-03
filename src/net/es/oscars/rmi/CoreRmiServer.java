package net.es.oscars.rmi;

import java.io.*;
import java.util.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.es.oscars.notify.RemoteEventProducer;

import org.apache.log4j.*;

public class CoreRmiServer implements CoreRmiInterface {
    private Logger log;
    private Registry registry;

    /* Make remote object static so GarbageCollector doesn't delete it */
    public static  CoreRmiServer staticObject;
    private CoreRmiInterface stub;
    private ServletRmiHandlerSwitchboard switchboard;

    public CoreRmiServer() throws RemoteException {
        this.log = Logger.getLogger(this.getClass());
        CoreRmiServer.staticObject = this;
    }

    public void init() throws RemoteException {
        this.log.debug("init.start");
        this.registry = LocateRegistry.createRegistry(8091);

        this.stub = (CoreRmiInterface) UnicastRemoteObject.exportObject(CoreRmiServer.staticObject, 0);

        this.registry.rebind("IDCRMIServer", this.stub);
        this.switchboard = new ServletRmiHandlerSwitchboard();
        this.log.debug("init.end");
    }

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

    public HashMap<String, Object> createReservation(HashMap<String, String[]> inputMap, String userName) throws IOException, RemoteException {
        return this.switchboard.createReservation(inputMap, userName);
    }



}