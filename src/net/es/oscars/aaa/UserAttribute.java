package net.es.oscars.aaa;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;

import net.es.oscars.database.HibernateBean;

/**
 * UserAttribute is adapted from a Middlegen class automatically generated 
 * from the schema for the aaa.attributes table.
 */
public class UserAttribute extends HibernateBean implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 5025;

    /** persistent field */
    private int userId;

    /** persistent field */
    private int attributeId;
    
    /** default constructor */
    public UserAttribute() { }

    /**
     * @return userId the foreign key into the users table
     */ 
    public int getUserId() { return this.userId; }

    /**
     * @return  attributeId the foreign key into the attribute table
     */ 
    public int getAttributeId() { return this.attributeId; }

    /**
     * @param userId An int that is a foreign key into the users table
     * */
    public void setUserId(int userId) { this.userId = userId; }

    /**
     * @param attributeId An int that is a foreign key into the attributes table
     * */
    public void setAttributeId(int attributeId) { this.attributeId = attributeId; }
    

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .append("userId",getUserId())
            .append("attributeId", getAttributeId())
            .toString();
    }
}
