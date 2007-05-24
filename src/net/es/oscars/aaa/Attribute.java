package net.es.oscars.aaa;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;

import net.es.oscars.BeanUtils;

/**
 * Attribute is adapted from a Middlegen class automatically generated 
 * from the schema for the aaa.permissions table.
 */
public class Attribute extends BeanUtils implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 5025;

    /** persistent field */
    private String name;

    /** default constructor */
    public Attribute() { }

    /**
     * @return name A String with the permission name
     */ 
    public String getName() { return this.name; }

    /**
     * @param name A String with the permission name
     */ 
    public void setName(String name) { this.name = name; }


    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }
}
