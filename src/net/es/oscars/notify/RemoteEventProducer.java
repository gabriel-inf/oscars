package net.es.oscars.notify;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteEventProducer extends Remote{
    public void addEvent(Event event) throws RemoteException;
}