package net.es.oscars.notifybroker.jdom;

import java.util.List;

import net.es.oscars.notifybroker.ws.WSNotifyConstants;

import org.jdom.Element;

public class NotificationMessage {
    private SubscriptionReference subscriptionReference;
    private Topic topic;
    private ProducerReference producerReference;
    private List<Element> message;
    
    /**
     * @return the subscriptionReference
     */
    public SubscriptionReference getSubscriptionReference() {
        return this.subscriptionReference;
    }
    
    /**
     * @param subscriptionReference the subscriptionReference to set
     */
    public void setSubscriptionReference(SubscriptionReference subscriptionReference) {
        this.subscriptionReference = subscriptionReference;
    }
    
    /**
     * @return the topic
     */
    public Topic getTopic() {
        return this.topic;
    }
    
    /**
     * @param topic the topic to set
     */
    public void setTopic(Topic topic) {
        this.topic = topic;
    }
    
    /**
     * @return the producerReference
     */
    public ProducerReference getProducerReference() {
        return this.producerReference;
    }
    
    /**
     * @param producerReference the producerReference to set
     */
    public void setProducerReference(ProducerReference producerReference) {
        this.producerReference = producerReference;
    }
    
    /**
     * @return the message
     */
    public List<Element> getMessage() {
        return this.message;
    }
    
    /**
     * @param message the message to set
     */
    public void setMessage(List<Element> message) {
        this.message = message;
    }
    
    /**
     * @return the jdom
     */
    public Element getJdom() {
        Element jdom = new Element("NotificationMessage", WSNotifyConstants.WSN_NS);
        jdom.addContent(this.subscriptionReference.getJdom());
        jdom.addContent(topic.getJdom());
        jdom.addContent(this.producerReference.getJdom());
        Element msgElem = new Element("Message", WSNotifyConstants.WSN_NS);
        for(Element msg : message){
            //Clone to prevent concurrency problems
            msgElem.addContent((Element)msg.clone());
        }
        jdom.addContent(msgElem);
        
        return jdom;
    }
}
