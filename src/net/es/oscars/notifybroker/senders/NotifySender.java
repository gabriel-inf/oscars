package net.es.oscars.notifybroker.senders;

import java.rmi.RemoteException;

public interface NotifySender {
    public void sendNotify(Notification notify) throws RemoteException;
}
