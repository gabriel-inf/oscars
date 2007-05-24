package net.es.oscars.bss.topology;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;

import net.es.oscars.BeanUtils;

/**
 * PeerIpaddr is adapted from a Middlegen class automatically generated 
 * from the schema for the bss.ipaddrs table.
 */
public class PeerIpaddr extends BeanUtils implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4151;

    /** persistent field */
    private String IP;

    /** persistent field */
    private Domain domain;

    /** default constructor */
    public PeerIpaddr() { }

    /**
     * @return IP a string with the IP address
     */ 
    public String getIP() { return this.IP; }

    /**
     * @param IP a string with the IP address
     */ 
    public void setIP(String IP) { this.IP = IP; }


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
}
