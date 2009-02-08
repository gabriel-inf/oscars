package net.es.oscars.notifybroker.senders;

import java.util.List;

import org.jdom.Element;

public class Notification {
    private String destinationUrl;
    private String subscriptioId;
    private String publisherUrl;
    private List<String> topics;
    private List<Element> msg;
    
    /**
     * @return the destinationUrl
     */
    public String getDestinationUrl() {
        return this.destinationUrl;
    }
    /**
     * @param destinationUrl the destinationUrl to set
     */
    public void setDestinationUrl(String destinationUrl) {
        this.destinationUrl = destinationUrl;
    }
    /**
     * @return the subscriptioId
     */
    public String getSubscriptioId() {
        return this.subscriptioId;
    }
    /**
     * @param subscriptioId the subscriptioId to set
     */
    public void setSubscriptioId(String subscriptioId) {
        this.subscriptioId = subscriptioId;
    }
    /**
     * @return the publisherUrl
     */
    public String getPublisherUrl() {
        return this.publisherUrl;
    }
    /**
     * @param publisherUrl the publisherUrl to set
     */
    public void setPublisherUrl(String publisherUrl) {
        this.publisherUrl = publisherUrl;
    }
    /**
     * @return the topics
     */
    public List<String> getTopics() {
        return this.topics;
    }
    /**
     * @param topics the topics to set
     */
    public void setTopics(List<String> topics) {
        this.topics = topics;
    }
    /**
     * @return the msg
     */
    public List<Element> getMsg() {
        return this.msg;
    }
    /**
     * @param msg the msg to set
     */
    public void setMsg(List<Element> msg) {
        this.msg = msg;
    }
}
