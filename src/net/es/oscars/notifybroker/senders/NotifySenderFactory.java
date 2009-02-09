package net.es.oscars.notifybroker.senders;

/**
 * Factory class for creating NotifySenders. Right now there
 * is only one sender type but in the future it can be extended to 
 * create NotifySenders with different types.
 * 
 * @author Andrew Lake (alake@internet2.edu)
 */
public class NotifySenderFactory {
    public static NotifySender createNotifySender(){
        return new WSNotifySender();
    }
}
