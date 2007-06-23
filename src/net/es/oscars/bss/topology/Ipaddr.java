package net.es.oscars.bss.topology;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.hibernate.Hibernate;

import net.es.oscars.database.HibernateBean;

/**
 * Ipaddr is adapted from a Middlegen class automatically generated 
 * from the schema for the bss.ipaddrs table.
 */
public class Ipaddr extends HibernateBean implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4151;

    /** persistent field */
    private boolean valid;

    /** persistent field */
    private String IP;

    /** nullable persistent field */
    private String description;

    /** persistent field */
    private Port port;

    /** default constructor */
    public Ipaddr() { }


    /**
     * @return valid a boolean indicating whether this entry is still valid
     */ 
    public boolean isValid() { return this.valid; }

    /**
     * @param valid a boolean indicating whether this entry is still valid
     */ 
    public void setValid(boolean valid) { this.valid = valid; }


    /**
     * @return ip a string with the IP address
     */ 
    public String getIP() { return this.IP; }

    /**
     * @param ip a string with the IP address
     */ 
    public void setIP(String IP) { this.IP = IP; }


    /**
     * @return description a string with this address's description
     */ 
    public String getDescription() { return this.description; }

    /**
     * @param description a string with this address's description
     */ 
    public void setDescription(String description) {
        this.description = description;
    }


    /**
     * @return port port instance (association used)
     */ 
    public Port getPort() { return this.port; }

    /**
     * @param port a port instance (association used)
     */ 
    public void setPort(Port port) {
        this.port = port;
    }


    // need to override superclass because dealing with transient
    // instances as well
    public boolean equals(Object o) {
        if (this == o) { return true; }
        Class thisClass = Hibernate.getClass(this);
        if (o == null || thisClass != Hibernate.getClass(o)) {
            return false;
        }
        Ipaddr castOther = (Ipaddr) o;
        // if both of these have been saved to the database
        if ((this.getId() != null) &&
            (castOther.getId() != null)) {
            return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
        } else {
            return new EqualsBuilder()
                .append(this.isValid(), castOther.isValid())
                .append(this.getIP(), castOther.getIP())
                .append(this.getDescription(), castOther.getDescription())
                .append(this.getPort(), castOther.getPort())
                .isEquals();
        }
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }
}
