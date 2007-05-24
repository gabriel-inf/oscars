package net.es.oscars.aaa;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;

import net.es.oscars.BeanUtils;

/**
 * Authorization is adapted from a Middlegen class automatically generated 
 * from the schema for the aaa.authorizations table.
 */
public class Authorization extends BeanUtils implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4149;

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
     * @return userId An Integer containing a user table row primary key
     */ 
    public int getUserId() { return this.userId; }

    /**
     * @param userId An Integer containing a user table row primary key
     */ 
    public void setUserId(int userId) { this.userId = userId; }


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

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }
}
