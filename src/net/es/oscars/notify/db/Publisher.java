package net.es.oscars.notify.db;

import net.es.oscars.database.HibernateBean;
import org.apache.commons.lang.builder.ToStringBuilder;
import java.io.Serializable;


/**
 * Publisher is adapted from a Middlegen class automatically generated
 * from the schema for the notify.publishers table.
 */
public class Publisher extends HibernateBean implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4151;

    /** persistent field */
    private String referenceId;

    /** persistent field */
    private String userLogin;
    
    /** persistent field */
    private String url;
    
    /** persistent field */
    private Long createdTime;
    
    /** persistent field */
    private Long terminationTime;
    
    /** persistent field */
    private boolean demand;
    
    /** persistent field */
    private int status;
    
    /** default constructor */
    public Publisher() {}

    /**
     * @return a String with the id used in SubscriptionReference
     */
    public String getReferenceId() {
        return this.referenceId;
    }

    /**
     * @param referenceId a String with the id used in SubscriptionReference
     */
    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }
    
    /**
     * @return a String with login of user that owns subscription
     */
    public String getUserLogin() {
        return this.userLogin;
    }

    /**
     * @param userLogin a String with login of user that owns subscription
     */
    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }
    
    /**
     * @return a String with URL where notifiations are sent
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * @param url a String with URL where notifications are sent
     */
    public void setUrl(String url) {
        this.url = url;
    }
    
    /**
     * @return a Long with the creation time
     */
    public Long getCreatedTime() {
        return this.createdTime;
    }

    /**
     * @param createdTime a Long with the creation time
     */
    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }
    
    /**
     * @return a Long with the expiration time
     */
    public Long getTerminationTime() {
        return this.terminationTime;
    }

    /**
     * @param terminationTime a Long with the expiration time
     */
    public void setTerminationTime(Long terminationTime) {
        this.terminationTime = terminationTime;
    }
    
    /**
     * @return a boolean indicating whether this is a demand publisher
     */
    public boolean isDemand() {
        return this.demand;
    }
    
    /**
     * @param demand a boolean indicating whether this is a demand publisher
     */
    public void setDemand(boolean demand) {
        this.demand = demand;
    }
    
    /**
     * @return an int with the status
     */
    public int getStatus() {
        return this.status;
    }

    /**
     * @param status an int with the status
     */
    public void setStatus(int status) {
        this.status = status;
    }
    
    public String toString() {
        return new ToStringBuilder(this).append("id", getId()).toString();
    }
}
