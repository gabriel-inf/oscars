package net.es.oscars.bss.topology;

import java.util.Set;
import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.hibernate.Hibernate;

import net.es.oscars.database.HibernateBean;

/**
 * Link is adapted from a Middlegen class automatically generated 
 * from the schema for the bss.ports table.
 */
public class Link extends HibernateBean implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4151;

    /** persistent field */
    private boolean valid;

    /** persistent field */
    private int snmpIndex;

    /** nullable persistent field */
    private String name;

    /** persistent field */
    private Long maximumCapacity;

    /** persistent field */
    private Long maximumReservableCapacity;

    /** nullable persistent field */
    private Long granularity;

    /** persistent field */
    private String description;

    /** nullable persistent field */
    private String alias;

    /** persistent field */
    private Port port;

    private Set ipaddrs;

    /** default constructor */
    public Link() { }

    /**
     * @return valid a boolean indicating whether port is still valid
     */ 
    public boolean isValid() { return this.valid; }

    /**
     * @param valid a boolean indicating whether port is still valid
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
     * @return maximumCapacity a long with the port's maximum bandwidth
     */ 
    public Long getMaximumCapacity() { return this.maximumCapacity; }

    /**
     * @param maximumCapacity a long with the port's maximum bandwidth
     */ 
    public void setMaximumCapacity(Long maximumCapacity) {
        this.maximumCapacity = maximumCapacity;
    }


    /**
     * @return maximumReservableCapacity Long with the maximum utilization
     */ 
    public Long getMaximumReservableCapacity() {
        return this.maximumReservableCapacity;
    }

    /**
     * @param maximumReservableCapacity Long with the maximum utilization
     */ 
    public void setMaximumReservableCapacity(Long maximumReservableCapacity) {
        this.maximumReservableCapacity = maximumReservableCapacity;
    }


    /**
     * @return granularity increment of bandwidth that can be requested
     */ 
    public Long getGranularity() { return this.granularity; }

    /**
     * @param granularity increment of bandwidth that can be requested
     */ 
    public void setGranularity(Long granularity) { this.granularity = granularity; }


    /**
     * @return description a string with the port's description
     */ 
    public String getDescription() { return this.description; }

    /**
     * @param description a string with the port's description
     */ 
    public void setDescription(String description) {
        this.description = description;
    }


    /**
     * @return name a string with the port's logical name
     */ 
    public String getName() { return this.name; }

    /**
     * @param name a string with the port's logical name
     */ 
    public void setName(String name) {
        this.name = name;
    }


    /**
     * @return alias a string with the port's alias
     */ 
    public String getAlias() { return this.alias; }

    /**
     * @param alias a string with the port's alias
     */ 
    public void setAlias(String alias) { this.alias = alias; }


    /**
     * @return port a Port instance (uses association)
     */ 
    public Port getPort() { return this.port; }

    /**
     * @param port a Port instance (uses association)
     */ 
    public void setPort(Port port) { this.port = port; }

    public void setIpaddrs(Set ipaddrs) {
        this.ipaddrs = ipaddrs;
    }

    public Set getIpaddrs() {
        return this.ipaddrs;
    }

    public void addIpaddr(Ipaddr ipaddr) {
        //ipaddr.setLink(this);
        //this.ipaddrs.add(ipaddr);
    }


    // need to override superclass because dealing with transient
    // instances as well
    public boolean equals(Object o) {
        if (this == o) { return true; }
        Class thisClass = Hibernate.getClass(this);
        if (o == null || thisClass != Hibernate.getClass(o)) {
            return false;
        }
        Link castOther = (Link) o;
        // if both of these have been saved to the database
        if ((this.getId() != null) &&
            (castOther.getId() != null)) {
            return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
        } else {
            return new EqualsBuilder()
                .append(this.isValid(), castOther.isValid())
                .append(this.getSnmpIndex(), castOther.getSnmpIndex())
                .append(this.getMaximumCapacity(), castOther.getMaximumCapacity())
                .append(this.getDescription(), castOther.getDescription())
                .append(this.getAlias(), castOther.getAlias())
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
