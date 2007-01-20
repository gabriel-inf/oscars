package net.es.oscars.aaa;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * ResourcePermission is adapted from a Middlegen class automatically
 * generated from the schema for the oscars.resourcepermissions table.  It
 * is not currently functional.
 */
public class ResourcePermission implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4149;

    /** identifier field */
    private net.es.oscars.aaa.ResourcePermissionPK comp_id;

    /** default constructor */
    public ResourcePermission() { }

    public net.es.oscars.aaa.ResourcePermissionPK getComp_id() {
        return this.comp_id;
    }

    public void setComp_id(net.es.oscars.aaa.ResourcePermissionPK comp_id) {
        this.comp_id = comp_id;
    }


    public String toString() {
        return new ToStringBuilder(this)
            .append("comp_id", getComp_id())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof ResourcePermission) ) return false;
        ResourcePermission castOther = (ResourcePermission) other;
        return new EqualsBuilder()
            .append(this.getComp_id(), castOther.getComp_id())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getComp_id())
            .toHashCode();
    }
}
