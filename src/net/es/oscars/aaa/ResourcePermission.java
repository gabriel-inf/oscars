package net.es.oscars.aaa;

import java.io.Serializable;

import net.es.oscars.database.HibernateBean;

/**
 * ResourcePermission is adapted from a Middlegen class automatically
 * generated from the schema for the oscars.resourcepermissions table.  It
 * is not currently functional.
 */
public class ResourcePermission extends HibernateBean implements Serializable {
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
}
