package net.es.oscars.aaa;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;

import net.es.oscars.database.HibernateBean;

/**
 * This class is the Hibernate bean for the aaa.constraints table.
 */
public class Constraint extends HibernateBean implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4149;

    /** persistent field */
    private String name;

    /** persistent field */ 
    private String type;
    
    /** persistent field */
    private String description;

    /** default constructor */
    public Constraint() { }

    /**
     * @return name A String with the name of this constraint
     */ 
    public String getName() { return this.name; }

    /**
     * @param name A String with the name of this constraint
     */ 
    public void setName(String name) { this.name = name; }

    /**
     * @return type A String with the type of this constraint
     */ 
    public String getType() { return this.type; }

    /**
     * @param type A String with the type of this constraint
     */ 
    public void setType(String type) { this.type =type; }


    /**
     * @return description A String with the description of this constraint
     */ 
    public String getDescription() { return this.description; }

    /**
     * @param description A String with the description of this constraint
     */ 
    public void setDescription(String description) {
        this.description = description;
    }


    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }
}
