package net.es.oscars.rmi.notifybroker.xface;

import java.io.Serializable;

/**
 * Java object that holds all the parameters of a response to the 
 * Subscribe call.
 * 
 * @author Andrew Lake (alake@internet2.edu)
 */
public class RmiSubscribeResponse implements Serializable{
    private String subscriptionId;
    private Long terminationTime;
    private Long createdTime;
    
    /**
     * @return the subscriptionId
     */
    public synchronized String getSubscriptionId() {
        return this.subscriptionId;
    }
    /**
     * @param subscriptionId the subscriptionId to set
     */
    public synchronized void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }
    /**
     * @return the terminationTime
     */
    public synchronized Long getTerminationTime() {
        return this.terminationTime;
    }
    /**
     * @param terminationTime the terminationTime to set
     */
    public synchronized void setTerminationTime(Long terminationTime) {
        this.terminationTime = terminationTime;
    }
    /**
     * @return the createdTime
     */
    public synchronized Long getCreatedTime() {
        return this.createdTime;
    }
    /**
     * @param createdTime the createdTime to set
     */
    public synchronized void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }
}
