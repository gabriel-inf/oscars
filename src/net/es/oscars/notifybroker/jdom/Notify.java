package net.es.oscars.notifybroker.jdom;

import java.util.ArrayList;
import java.util.List;

import net.es.oscars.notifybroker.ws.WSNotifyConstants;

import org.jdom.Element;

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
     * @return the jdom
     */
    public Element getJdom() {
        Element jdom = new Element("Notify", WSNotifyConstants.WSN_NS);
        for(NotificationMessage msg : this.notificationMessages){
            jdom.addContent(msg.getJdom());
        }
        return jdom;
    }
    
}
