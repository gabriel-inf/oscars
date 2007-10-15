package net.es.oscars.aaa;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;

import net.es.oscars.database.HibernateBean;

/**
 * Attribute is the Hibernate bean associated with the aaa.attributes
 * table.
 */
public class Attribute extends HibernateBean implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4149;

    /** persistent field */
    private String name;

    /** nullable persistent field */
    private String attrType;

    /** default constructor */
    public Attribute() { }

    /**
     * @return name a string with the attribute name
     */ 
    public String getName() { return this.name; }

    /**
     * @param name A string with the attribute name
     */ 
    public void setName(String name) { this.name = name; }


    /**
     * @return attrType a string with the attribute type
     */ 
    public String getAttrType() { return this.attrType; }

    /**
     * @param attrType a string with the attribute type
     */ 
    public void setAttrType(String attrType) {
        this.attrType = attrType;
    }


    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }
}
