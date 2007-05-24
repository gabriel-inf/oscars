package net.es.oscars.bss.topology;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;

import net.es.oscars.BeanUtils;

/**
 * Ipaddr is adapted from a Middlegen class automatically generated 
 * from the schema for the bss.ipaddrs table.
 */
public class Ipaddr extends BeanUtils implements Serializable {
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
    private Interface xface;

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
     * @return xface interface instance (association used)
     */ 
    public Interface getInterface() { return this.xface; }

    /**
     * @param xface an interface instance (association used)
     */ 
    public void setInterface(Interface xface) {
        this.xface = xface;
    }


    // need to override superclass because dealing with transient
    // instances as well
    public boolean equals(Object o) {
        if (this == o) { return true; }
        Class thisClass = getClass();
        if (o == null || thisClass != o.getClass()) {
            return false;
        }
        Ipaddr castOther = (Ipaddr) o;
        // if one of these has been saved to the database
        if ((this.getId() != null) ||
            (castOther.getId() != null)) {
            return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
        } else {
            return new EqualsBuilder()
                .append(this.isValid(), castOther.isValid())
                .append(this.getIP(), castOther.getIP())
                .append(this.getDescription(), castOther.getDescription())
                .append(this.getInterface(), castOther.getInterface())
                .isEquals();
        }
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }
}
