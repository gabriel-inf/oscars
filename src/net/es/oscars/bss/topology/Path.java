package net.es.oscars.bss.topology;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Path is adapted from a Middlegen class automatically generated 
 * from the schema for the topology.paths table.
 */
public class Path implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4151;

    /** identifier field */
    private Integer id;

    /** nullable persistent field */
    private String addressType;

    /** nullable persistent field */
    private Path nextPath;

    /** persistent field */
    private Ipaddr ipaddr;

    /** default constructor */
    public Path() { }

    /**
     * Auto generated getter method.
     * @return id primary key in the paths table
     */ 
    public Integer getId() { return this.id; }

    /**
     * Auto generated setter method
     * @param id primary key in the paths table
     */ 
    public void setId(Integer id) { this.id = id; }


    /**
     * Auto generated getter method.
     * @return addressType a string with this path element's address type
     */ 
    public String getAddressType() { return this.addressType; }

    /**
     * Auto generated setter method.
     * @param addressType a string with this path element's address type
     */ 
    public void setAddressType(String addressType) {
        this.addressType = addressType;
    }


    /**
     * Auto generated getter method.
     * @return nextPath a path with the next path element (uses association)
     */ 
    public Path getNextPath() { return this.nextPath; }

    /**
     * Auto generated setter method.
     * @param nextPath a path with the next path element (uses association)
     */ 
    public void setNextPath(Path nextPath) { this.nextPath = nextPath; }


    /**
     * Auto generated getter method.
     * @return ipaddr ipaddr instance associated with this path instance
     */ 
    public Ipaddr getIpaddr() { return this.ipaddr; }

    /**
     * Auto generated setter method.
     * @param ipaddr ipaddr instance associated with this path instance
     */ 
    public void setIpaddr(Ipaddr ipaddr) { this.ipaddr = ipaddr; }


    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof Path) ) return false;
        Path castOther = (Path) other;
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
