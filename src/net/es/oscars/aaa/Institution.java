package net.es.oscars.aaa;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Institution is adapted from a Middlegen class automatically generated 
 * from the schema for the aaa.institutions table.
 */
public class Institution implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4149;

    /** identifier field */
    private Integer id;

    /** persistent field */
    private String name;

    /** default constructor */
    public Institution() { }

    /**
     * Auto generated getter method
     * @return id An Integer containing the primary key
     */ 
    public Integer getId() { return this.id; }

    /**
     * Auto generated setter method
     * @param id An Integer containing the primary key
     */ 
    public void setId(Integer id) { this.id = id; }


    /**
     * Auto generated getter method
     * @return name A String with the institution name
     */ 
    public String getName() { return this.name; }

    /**
     * Auto generated getter method
     * @param name A String with the institution name
     */ 
    public void setName(String name) { this.name = name; }


    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof Institution) ) return false;
        Institution castOther = (Institution) other;
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
