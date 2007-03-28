package net.es.oscars.pathfinder;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import net.es.oscars.bss.topology.Ipaddr;
import net.es.oscars.pathfinder.Domain;

/**
 * PeerIpaddr is adapted from a Middlegen class automatically generated 
 * from the schema for the topology.ipaddrs table.
 */
public class PeerIpaddr implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4151;

    /** identifier field */
    private Integer id;

    /** persistent field */
    private String ip;

    /** persistent field */
    private Domain domain;

    /** default constructor */
    public PeerIpaddr() { }

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
     * @return domain instance (association used)
     */ 
    public Domain getDomain() { return this.domain; }

    /**
     * @param domain a domain instance (association used)
     */ 
    public void setDomain(Domain domain) {
        this.domain = domain;
    }


    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof PeerIpaddr) ) return false;
        PeerIpaddr castOther = (PeerIpaddr) other;
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
