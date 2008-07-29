package net.es.oscars.aaa;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;

import net.es.oscars.database.HibernateBean;

/**
 * Hibernate bean for aaa.rpcs table.
 */
public class Rpc extends HibernateBean implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4149;

    /** persistent field */
    private int resourceId;

    /** persistent field */
    private int permissionId;
    
    /** persistent field */
    private int constraintId;

    /** default constructor */
    public Rpc() { }

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
     * @return constraintId An Integer containing a constraint table row
     *         primary key
     */ 
    public int getConstraintId() { return this.constraintId; }

    /**
     * @param attrId An Integer containing a constraint table row primary key
     */ 
    public void setConstraintId(int constraintId) {
        this.constraintId = constraintId;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .append("resourceId",getResourceId())
            .append("permissionId", getPermissionId())
            .append("constraintId", getConstraintId())
            .toString();
    }
}
