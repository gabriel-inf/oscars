package net.es.oscars.rmi.model;

import java.util.HashMap;
import java.rmi.RemoteException;

public interface ModelRmiHandler {
    public HashMap<String, Object> manage(HashMap<String, Object> parameters) throws RemoteException;
    public HashMap<String, Object> list(HashMap<String, Object> parameters) throws RemoteException;
    public HashMap<String, Object> add(HashMap<String, Object> parameters) throws RemoteException;
    public HashMap<String, Object> modify(HashMap<String, Object> parameters) throws RemoteException;
    public HashMap<String, Object> delete(HashMap<String, Object> parameters) throws RemoteException;
    public HashMap<String, Object> find(HashMap<String, Object> parameters) throws RemoteException;


}
