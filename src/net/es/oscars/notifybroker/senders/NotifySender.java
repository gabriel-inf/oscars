package net.es.oscars.notifybroker.senders;

import java.rmi.RemoteException;

/**
 * Interface for defining different types of notifications to be sent.
 * Useful if need to define different messaging versions.
 * 
 * @author Andrew Lake (alake@internet2.edu)
 *
 */
public interface NotifySender {
    public void sendNotify(Notification notify) throws RemoteException;
}
