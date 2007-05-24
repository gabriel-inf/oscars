package net.es.oscars.aaa;

import java.io.Serializable;

import net.es.oscars.BeanUtils;

/**
 * UserAttribute is adapted from a Middlegen class automatically
 * generated from the schema for the oscars.UserAttributes table.  It
 * is not currently functional.
 */
public class UserAttribute extends BeanUtils implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 5025;

    /** identifier field */
    private net.es.oscars.aaa.UserAttributePK comp_id;

    /** default constructor */
    public UserAttribute() { }

    public net.es.oscars.aaa.UserAttributePK getComp_id() {
        return this.comp_id;
    }

    public void setComp_id(net.es.oscars.aaa.UserAttributePK comp_id) {
        this.comp_id = comp_id;
    }
}
