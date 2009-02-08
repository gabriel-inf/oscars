package net.es.oscars.notifybroker.senders;

public class NotifySenderFactory {
    public static NotifySender createNotifySender(){
        return new WSNotifySender();
    }
}
