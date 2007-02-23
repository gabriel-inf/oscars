package net.es.oscars.bss.topology;

import java.util.Set;
import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Interface is adapted from a Middlegen class automatically generated 
 * from the schema for the topology.interfaces table.
 */
public class Interface implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4151;

    /** identifier field */
    private Integer id;

    /** persistent field */
    private boolean valid;

    /** persistent field */
    private int snmpIndex;

    /** nullable persistent field */
    private Long speed;

    /** nullable persistent field */
    private String description;

    /** nullable persistent field */
    private String alias;

    /** persistent field */
    private Router router;

    private Set ipaddrs;

    /** default constructor */
    public Interface() { }

    /**
     * @return primary key in the interfaces table
     */ 
    public Integer getId() { return this.id; }

    /**
     * @param id primary key in the interfaces table
     */ 
    public void setId(Integer id) { this.id = id; }


    /**
     * @return valid a boolean indicating whether interface is still valid
     */ 
    public boolean isValid() { return this.valid; }

    /**
     * @param valid a boolean indicating whether interface is still valid
     */ 
    public void setValid(boolean valid) { this.valid = valid; }


    /**
     * @return snmpIndex a SNMP index from ifrefpoll
     */ 
    public int getSnmpIndex() { return this.snmpIndex; }

    /**
     * @param snmpIndex a SNMP index
     */ 
    public void setSnmpIndex(int snmpIndex) { this.snmpIndex = snmpIndex; }


    /**
     * @return speed a long with the interface's maximum bandwidth
     */ 
    public Long getSpeed() { return this.speed; }

    /**
     * @param speed a long with the interface's maximum bandwidth
     */ 
    public void setSpeed(Long speed) { this.speed = speed; }


    /**
     * @return description a string with the interface's description
     */ 
    public String getDescription() { return this.description; }

    /**
     * @param description a string with the interface's description
     */ 
    public void setDescription(String description) {
        this.description = description;
    }


    /**
     * @return alias a string with the interface's alias
     */ 
    public String getAlias() { return this.alias; }

    /**
     * @param alias a string with the interface's alias
     */ 
    public void setAlias(String alias) { this.alias = alias; }


    /**
     * @return router a router instance (uses association)
     */ 
    public Router getRouter() { return this.router; }

    /**
     * @param router a router instance (uses association)
     */ 
    public void setRouter(Router router) { this.router = router; }

    public void setIpaddrs(Set ipaddrs) {
        this.ipaddrs = ipaddrs;
    }

    public Set getIpaddrs() {
        return this.ipaddrs;
    }

    public void addIpaddr(Ipaddr ipaddr) {
        ipaddr.setInterface(this);
        this.ipaddrs.add(ipaddr);
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof Interface) ) return false;
        Interface castOther = (Interface) other;
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
