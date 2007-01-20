package net.es.oscars.aaa;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Authorization is adapted from a Middlegen class automatically generated 
 * from the schema for the aaa.authorizations table.
 */
public class Authorization implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4149;

    /** identifier field */
    private Integer id;

    /** nullable persistent field */
    private String context;

    /** nullable persistent field */
    private Long updateTime;

    /** persistent field */
    private int userId;

    /** persistent field */
    private int resourceId;

    /** persistent field */
    private int permissionId;

    /** default constructor */
    public Authorization() { }

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
     * @return context A String corresponding to a currently unused field
     */ 
    public String getContext() { return this.context; }

    /**
     * Auto generated setter method
     * @param context A String corresponding to a currently unused field
     */ 
    public void setContext(String context) { this.context = context; }


    /**
     * Auto generated getter method
     * @return updateTime A Long instance with the last update time
     */ 
    public Long getUpdateTime() { return this.updateTime; }

    /**
     * Auto generated setter method
     * @param updateTime A Long instance with the last update time
     */ 
    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }


    /**
     * Auto generated getter method.
     * @return userId An Integer containing a user table row primary key
     */ 
    public int getUserId() { return this.userId; }

    /**
     * Auto generated setter method
     * @param userId An Integer containing a user table row primary key
     */ 
    public void setUserId(int userId) { this.userId = userId; }


    /**
     * Auto generated getter method
     * @return resourceId An Integer with a resource table primary key
     */ 
    public int getResourceId() { return this.resourceId; }

    /**
     * Auto generated setter method
     * @param resourceId An Integer with a resource table primary key
     */ 
    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }


    /**
     * Auto generated getter method
     * @return permissionId An Integer with a permission table primary key
     */ 
    public int getPermissionId() { return this.permissionId; }

    /**
     * Auto generated setter method
     * @param permissionId An Integer with a permission table primary key
     */ 
    public void setPermissionId(int permissionId) {
        this.permissionId = permissionId;
    }


    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof Authorization) ) return false;
        Authorization castOther = (Authorization) other;
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
