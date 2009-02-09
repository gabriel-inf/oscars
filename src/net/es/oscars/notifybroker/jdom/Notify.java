package net.es.oscars.notifybroker.jdom;

import java.util.ArrayList;
import java.util.List;

import net.es.oscars.notifybroker.ws.WSNotifyConstants;

import org.jdom.Element;

/**
 * Represents a Notify message as specified in the WS-Notification spec.
 * 
 * @author Andrew Lake (alake@internet2.edu)
 *
 */
public class Notify {
    private List<NotificationMessage> notificationMessages;
    final public static String ACTION = "http://oscars.es.net/OSCARS/Notify";
    
    public Notify(){
        this.notificationMessages= new ArrayList<NotificationMessage>();
    }

    /**
     * @return the notificationMessages
     */
    public List<NotificationMessage> getNotificationMessages() {
        return this.notificationMessages;
    }

    /**
     * @param notificationMessages the notificationMessages to set
     */
    public void setNotificationMessages(
            List<NotificationMessage> notificationMessages) {
        this.notificationMessages = notificationMessages;
    }
    
    public void addNotificationMessage(NotificationMessage msg){
        this.notificationMessages.add(msg);
    }

    /**
     * @return A JDOM representation of the element
     */
    public Element getJdom() {
        Element jdom = new Element("Notify", WSNotifyConstants.WSN_NS);
        for(NotificationMessage msg : this.notificationMessages){
            jdom.addContent(msg.getJdom());
        }
        return jdom;
    }
    
}
