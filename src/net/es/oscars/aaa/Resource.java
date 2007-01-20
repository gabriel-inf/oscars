package net.es.oscars.aaa;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Resource is adapted from an Middlegen class automatically generated 
 * from the schema for the aaa.resources table.
 */
public class Resource implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4149;

    /** identifier field */
    private Integer id;

    /** persistent field */
    private String name;

    /** nullable persistent field */
    private String description;

    /** nullable persistent field */
    private Long updateTime;

    /** default constructor */
    public Resource() { }

    /**
     * Auto generated getter method
     * @return id An Integer with a resource table primary key
     */ 
    public Integer getId() { return this.id; }

    /**
     * Auto generated setter method
     * @param id An Integer with a resource table primary key
     */ 
    public void setId(Integer id) { this.id = id; }


    /**
     * Auto generated getter method
     * @return name A String with the name of this resource
     */ 
    public String getName() { return this.name; }

    /**
     * Auto generated setter method
     * @param name A String with the name of this resource
     */ 
    public void setName(String name) { this.name = name; }


    /**
     * Auto generated getter method
     * @return description A String with the description of this resource
     */ 
    public String getDescription() { return this.description; }

    /**
     * Auto generated setter method
     * @param description A String with the description of this resource
     */ 
    public void setDescription(String description) {
        this.description = description;
    }


    /**
     * Auto generated getter method
     * @return updateTime A Long with this row's last update time
     */ 
    public Long getUpdateTime() { return this.updateTime; }

    /**
     * Auto generated setter method
     * @param updateTime A Long with this row's last update time
     */ 
    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }


    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof Resource) ) return false;
        Resource castOther = (Resource) other;
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
