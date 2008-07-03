package net.es.oscars.rmi;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class CoreRmiClient implements CoreRmiInterface {
    private Logger log;
    private CoreRmiInterface remote;
    private boolean connected;

    public CoreRmiClient() {
        this.log = Logger.getLogger(this.getClass());
    }

    public void init() throws RemoteException {
        this.remote = null;
        this.log.debug("init.start");
        this.connected = true;
        try {
            Registry registry = LocateRegistry.getRegistry(8091);
            this.remote = (CoreRmiInterface) registry.lookup("IDCRMIServer");
            this.connected = true;
            this.log.debug("Connected to IDC RMI server");
        } catch (RemoteException e) {
            this.connected = false;
            this.log.warn("Remote exception from RMI server: ", e);
            throw e;
        } catch (NotBoundException e) {
            this.connected = false;
            this.log.warn("Trying to access unregistered remote object: ", e);
        } catch (Exception e) {
            this.connected = false;
            this.log.warn("Could not connect", e);
        }
        this.log.debug("init.end");
    }

    public HashMap<String, Object> createReservation(HashMap<String, String[]> inputMap, String userName) throws RemoteException {
        this.log.debug("createReservation.start");
        HashMap<String, Object> result = null;
        if (this.remote == null) {
            this.log.error("Remote object not found");
            return result;
        }
        if (!this.connected) {
            this.log.error("Not connected to RMI server");
//            return;
        }

        try {
            result = remote.createReservation(inputMap, userName);
            this.log.debug("createReservation.end");
            return result;
        } catch (RemoteException e) {
            this.log.warn("Remote exception from RMI server: " + e.getMessage(), e);
        } catch (Exception e) {
            this.log.warn("Exception from RMI server" + e.getMessage(), e);
        }
        return result;
    }



}
