package net.es.oscars.bss.topology;

import java.util.Set;
import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Router is adapted from a Middlegen class automatically generated 
 * from the schema for the topology.routers table.
 */
public class Router implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4151;

    /** identifier field */
    private Integer id;

    /** persistent field */
    private boolean valid;

    /** persistent field */
    private String name;

    private Set xfaces;

    /** default constructor */
    public Router() { }

    /**
     * @return id primary key in the routers table
     */ 
    public Integer getId() { return this.id; }

    /**
     * @param id primary key in the routers table
     */ 
    public void setId(Integer id) { this.id = id; }


    /**
     * @return valid a boolean indicating whether this entry is still valid
     */ 
    public boolean isValid() { return this.valid; }

    /**
     * @param valid a boolean indicating whether this entry is still valid
     */ 
    public void setValid(boolean valid) { this.valid = valid; }


    /**
     * @return name a string with the name of this router
     */ 
    public String getName() { return this.name; }

    /**
     * @param name a string with the name of this router
     */ 
    public void setName(String name) { this.name = name; }

    public void setInterfaces(Set xfaces) {
        this.xfaces = xfaces;
    }

    public Set getInterfaces() {
        return this.xfaces;
    }

    public void addInterface(Interface xface) {
        xface.setRouter(this);
        this.xfaces.add(xface);
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof Router) ) return false;
        Router castOther = (Router) other;
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
