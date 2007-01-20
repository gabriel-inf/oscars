package net.es.oscars.aaa;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
public class ResourcePermissionPK implements Serializable {

    /** identifier field */
    private Integer resourceId;

    /** identifier field */
    private Integer permissionId;

    /** full constructor */
    public ResourcePermissionPK(Integer resourceId, Integer permissionId) {
        this.resourceId = resourceId;
        this.permissionId = permissionId;
    }

    /** default constructor */
    public ResourcePermissionPK() {
    }


    public Integer getResourceId() {
        return this.resourceId;
    }

    public void setResourceId(Integer resourceId) {
        this.resourceId = resourceId;
    }


    public Integer getPermissionId() {
        return this.permissionId;
    }

    public void setPermissionId(Integer permissionId) {
        this.permissionId = permissionId;
    }


    public String toString() {
        return new ToStringBuilder(this)
            .append("resourceId", getResourceId())
            .append("permissionId", getPermissionId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof ResourcePermissionPK) ) return false;
        ResourcePermissionPK castOther = (ResourcePermissionPK) other;
        return new EqualsBuilder()
            .append(this.getResourceId(), castOther.getResourceId())
            .append(this.getPermissionId(), castOther.getPermissionId())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getResourceId())
            .append(getPermissionId())
            .toHashCode();
    }

}
