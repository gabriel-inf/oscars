package net.es.oscars.notify;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface for class passed over RMI that allows access to a 
 * central event queue.
 */
public interface RemoteEventProducer extends Remote{
    /**
     * Adds an event to the event queue
     *
     * @param event the event to add to the queue
     */
    public void addEvent(Event event) throws RemoteException;
}