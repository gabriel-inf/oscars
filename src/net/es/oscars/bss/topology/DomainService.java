package net.es.oscars.bss.topology;

import net.es.oscars.database.HibernateBean;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.Hibernate;
import java.io.Serializable;
import java.util.*;


/**
 * DomainService is adapted from a Middlegen class automatically generated
 * from the schema for the bss.domainServices table.
 */
public class DomainService extends HibernateBean implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4151;

    /** persistent field */
    private String type;

    /** persistent field */
    private String url;
    
    /** persistent field */
    private Domain domain;
    
    /** default constructor */
    public DomainService() {}
    
    /**
     * @return a string with the type of service
     */
    public String getType() {
        return this.type;
    }

    /**
     * @param type a string with the type of service
     */
    public void setType(String type) {
        this.type = type;
    }
    
    /**
     * @return a string with the url of the service
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * @param url a string with the url of the service
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return domain a Domain instance (uses association)
     */
    public Domain getDomain() {
        return this.domain;
    }

    /**
     * @param domain a Domain instance (uses association)
     */
    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    public String toString() {
        return new ToStringBuilder(this).append("id", getId()).toString();
    }
}
