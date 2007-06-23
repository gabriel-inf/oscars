package net.es.oscars.aaa;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;

import net.es.oscars.database.HibernateBean;

/**
 * Attribute is adapted from a Middlegen class automatically generated 
 * from the schema for the aaa.permissions table.
 */
public class Attribute extends HibernateBean implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 5025;

    /** persistent field */
    private String name;
    
    /** nullable persistent field */
    private String attrType;

    /** default constructor */
    public Attribute() { }

    /**
     * @return name A String with the attribute name
     */ 
    public String getName() { return this.name; }

    /**
     * @param name A String with the attribute name
     */ 
    public void setName(String name) { this.name = name; }

    /**
     * @return name A String with the attribute type
     */ 
    public String getAttrType() { return this.attrType; }

    /**
     * @param name A String with the attribute type
     */ 
    public void setAttrType(String name) { this.name = attrType; }


    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .append("name", this.getName())
            .append("attrType",this.getAttrType())
            .toString();
    }
}
