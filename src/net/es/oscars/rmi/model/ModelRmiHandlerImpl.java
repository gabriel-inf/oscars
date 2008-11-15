package net.es.oscars.rmi.model;

import java.util.HashMap;
import java.util.List;
import java.rmi.RemoteException;

import net.es.oscars.aaa.Attribute;
import net.es.oscars.aaa.AttributeDAO;

import org.hibernate.Session;

public class ModelRmiHandlerImpl implements ModelRmiHandler {

    public HashMap<String, Object> manage(HashMap<String, Object> parameters) throws RemoteException {
        ModelOperation operation = (ModelOperation) parameters.get("operation");
        if (operation == null) {
            throw new RemoteException("Null operation");
        } else if (operation == ModelOperation.LIST) {
            return this.list(parameters);
        } else if (operation == ModelOperation.ADD) {
            return this.add(parameters);
        } else if (operation == ModelOperation.MODIFY) {
            return this.modify(parameters);
        } else if (operation == ModelOperation.DELETE) {
            return this.delete(parameters);
        } else if (operation == ModelOperation.FIND) {
            return this.find(parameters);
        } else {
            throw new RemoteException("Unknown operation");
        }
    }

    public HashMap<String, Object> list(HashMap<String, Object> parameters) throws RemoteException {
        HashMap<String, Object> result = new HashMap<String, Object>();
        return result;
    }
    public HashMap<String, Object> add(HashMap<String, Object> parameters) throws RemoteException {
        HashMap<String, Object> result = new HashMap<String, Object>();
        return result;
    }
    public HashMap<String, Object> modify(HashMap<String, Object> parameters) throws RemoteException {
        HashMap<String, Object> result = new HashMap<String, Object>();
        return result;
    }
    public HashMap<String, Object> delete(HashMap<String, Object> parameters) throws RemoteException {
        HashMap<String, Object> result = new HashMap<String, Object>();
        return result;
    }
    public HashMap<String, Object> find(HashMap<String, Object> parameters) throws RemoteException {
        HashMap<String, Object> result = new HashMap<String, Object>();
        return result;
    }
}
