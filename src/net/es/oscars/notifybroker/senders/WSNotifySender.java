package net.es.oscars.notifybroker.senders;

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.util.Properties;

import net.es.oscars.PropHandler;
import net.es.oscars.notifybroker.jdom.NBSoapClient;
import net.es.oscars.notifybroker.jdom.NotificationMessage;
import net.es.oscars.notifybroker.jdom.Notify;
import net.es.oscars.notifybroker.jdom.ProducerReference;
import net.es.oscars.notifybroker.jdom.SubscriptionReference;
import net.es.oscars.notifybroker.jdom.Topic;
import net.es.oscars.notifybroker.ws.WSNotifyConstants;

import org.apache.log4j.Logger;

/**
 * Sends WS-Notifications over HTTP
 */
public class WSNotifySender implements NotifySender{
    private Logger log;
    private String subscriptionManagerURL;
    private NBSoapClient client;
    
    public WSNotifySender(){
        this.log = Logger.getLogger(this.getClass());
        this.client = new NBSoapClient();
        //Load properties
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("notify.ws.broker", true); 
        this.subscriptionManagerURL = props.getProperty("url");
        if(this.subscriptionManagerURL == null){
            String localhost = null;
            try{
                localhost = InetAddress.getLocalHost().getHostName();
            }catch(Exception e){
                this.log.error("Please set 'notifybroker.url' in oscars.properties!");
            }
            this.subscriptionManagerURL = "https://" + localhost + ":8443/axis2/services/OSCARSNotify";
        }
        this.log.debug("OSCARSNotify.url=" + this.subscriptionManagerURL);
    }
    
    public void sendNotify(Notification notify) throws RemoteException {
        this.log.debug("sendNotify.start");
        
        /* Create subscription reference */
        SubscriptionReference subscrRef = new SubscriptionReference();
        subscrRef.setAddress(this.subscriptionManagerURL);
        subscrRef.setSubscriptionId(notify.getSubscriptioId());
        
        /* Create Topic expression */
        Topic topic = new Topic();
        topic.setDialect(WSNotifyConstants.WS_TOPIC_FULL);
        boolean first = true;
        String topicExpr = "";
        for(String topicVal : notify.getTopics()){
            topicExpr += (first ? "" : "|");
            topicExpr += topicVal;
        }
        topic.setExpression(topicExpr);
        
        /* Create producer reference */
        ProducerReference producerRef = new ProducerReference();
        producerRef.setAddress(notify.getPublisherUrl());
        
        /* Combine everything into a NotificationMessage */
        NotificationMessage notficationMsg = new NotificationMessage();
        notficationMsg.setSubscriptionReference(subscrRef);
        notficationMsg.setTopic(topic);
        notficationMsg.setProducerReference(producerRef);
        notficationMsg.setMessage(notify.getMsg());
        
        /* Add NotificationMessage to Notify Element */
        Notify wsNotify = new Notify();
        wsNotify.addNotificationMessage(notficationMsg);
        
        /* Send XML to subscriber */
        this.client.sendAsyncSoapMessage(notify.getDestinationUrl(), Notify.ACTION, wsNotify.getJdom());
        
        this.log.debug("sendNotify.end"); 
    }
}
