package net.es.oscars.notifybroker.db;

import net.es.oscars.database.HibernateBean;

import org.apache.commons.lang.builder.ToStringBuilder;
import java.io.Serializable;

/**
 * Port is adapted from a Middlegen class automatically generated
 * from the schema for the notify.subscriptionFilters table.
 */
public class SubscriptionFilter extends HibernateBean implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4151;

    /** persistent field */
    private String type;

    /** persistent field */
    private String value;
    
    /** persistent field */
    private Subscription subscription;
    
    /** default constructor */
    public SubscriptionFilter() {}
    
    /** constructor */
    public SubscriptionFilter(String type, String value) {
        this.type = type;
        this.value = value;
    }
    
    /**
     * @return a String with type of filter
     */
    public String getType() {
        return this.type;
    }

    /**
     * @param type a String with the filter type
     */
    public void setType(String type) {
        this.type = type;
    }
    
    /**
     * @return a String with the filter value
     */
    public String getValue() {
        return this.value;
    }

    /**
     * @param value a String with the filter value
     */
    public void setValue(String value) {
        this.value = value;
    }
    
    /**
     * @return the Subscription that owns the filter
     */
    public Subscription getSubscription() {
        return this.subscription;
    }

    /**
     * @param subscription the Subscription that owns the filter
     */
    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }
    
    public String toString() {
        return new ToStringBuilder(this).append("id", getId()).toString();
    }
}
