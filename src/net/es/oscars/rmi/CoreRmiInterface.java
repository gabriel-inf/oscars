package net.es.oscars.rmi;

import java.io.IOException;
import java.util.HashMap;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CoreRmiInterface extends Remote {

    public HashMap<String, Object> createReservation(HashMap<String, String[]> inputMap, String userName)  throws IOException, RemoteException;
    public void init()  throws RemoteException;

}
