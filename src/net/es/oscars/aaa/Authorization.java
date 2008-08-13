package net.es.oscars.aaa;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;

import net.es.oscars.database.HibernateBean;

/**
 *  is adapted from a Middlegen class automatically generated 
 * from the schema for the aaa.authorizations table.
 */
public class Authorization extends HibernateBean implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4149;

    /** nullable persistent field */
    private String context;

    /** nullable persistent field */
    private Long updateTime;

    /** persistent field */
    private int attrId;

    /** persistent field */
    private int resourceId;

    /** persistent field */
    private int permissionId;
    
    /** persistent field */
    private int constraintId;
    
    /** nullable persistent field */
    private String constraintValue;

    /** default constructor */
    public Authorization() { }

    /**
     * @return context A String corresponding to a currently unused field
     */ 
    public String getContext() { return this.context; }

    /**
     * @param context A String corresponding to a currently unused field
     */ 
    public void setContext(String context) { this.context = context; }


    /**
     * @return updateTime A Long instance with the last update time
     */ 
    public Long getUpdateTime() { return this.updateTime; }

    /**
     * @param updateTime A Long instance with the last update time
     */ 
    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }


    /**
     * @return attrId An Integer containing a user table row primary key
     */ 
    public int getAttrId() { return this.attrId; }

    /**
     * @param attrId An Integer containing a user table row primary key
     */ 
    public void setAttrId(int attrId) { this.attrId = attrId; }


    /**
     * @return resourceId An Integer with a resource table primary key
     */ 
    public int getResourceId() { return this.resourceId; }

    /**
     * @param resourceId An Integer with a resource table primary key
     */ 
    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }


    /**
     * @return permissionId An Integer with a permission table primary key
     */ 
    public int getPermissionId() { return this.permissionId; }

    /**
     * @param permissionId An Integer with a permission table primary key
     */ 
    public void setPermissionId(int permissionId) {
        this.permissionId = permissionId;
    }

    /**
     * @return constraintId an Integer corresponding to a constraint for this authorization
     */ 
    public int getConstraintId() { return this.constraintId; }

    /**
     * @param constraintId an Integer corresponding to a constraint value for this authorization
     */ 
    public void setConstraintId(int constraintId ) { this.constraintId = constraintId; }

    /**
     * @return constraintValue A String corresponding to a constraintValue for this authorization
     */ 
    public String getConstraintValue() { return this.constraintValue; }

    /**
     * @param constraintValue A String corresponding to a constraint for this authorization
     */ 
    public void setConstraintValue(String constraintValue ) { this.constraintValue = constraintValue; }


    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .append("attrId", getAttrId())
            .append("resourceId",getResourceId())
            .append("permissionId", getPermissionId())
            .append("ConstraintId",getConstraintId())
            .append("ConstraintValue",getConstraintValue())
            .toString();
    }
}
