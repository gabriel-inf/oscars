package net.es.oscars.bss.topology;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Ipaddr is adapted from a Middlegen class automatically generated 
 * from the schema for the topology.ipaddrs table.
 */
public class Ipaddr implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4151;

    /** identifier field */
    private Integer id;

    /** persistent field */
    private String ip;

    /** nullable persistent field */
    private String description;

    /** persistent field */
    private Interface xface;

    /** default constructor */
    public Ipaddr() { }

    /**
     * @return id primary key in the ipaddrs table
     */ 
    public Integer getId() { return this.id; }

    /**
     * @param id primary key in the ipaddrs table
     */ 
    public void setId(Integer id) { this.id = id; }


    /**
     * @return ip a string with the IP address
     */ 
    public String getIp() { return this.ip; }

    /**
     * @param ip a string with the IP address
     */ 
    public void setIp(String ip) { this.ip = ip; }


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


    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof Ipaddr) ) return false;
        Ipaddr castOther = (Ipaddr) other;
        return new EqualsBuilder()
            .append(this.getId(), castOther.getId())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getId())
            .toHashCode();
    }
}
