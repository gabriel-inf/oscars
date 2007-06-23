package net.es.oscars.bss.topology;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.Hibernate;

import net.es.oscars.database.HibernateBean;

/**
 * Vlan is the Hibernate bean class for the bss.vlans table.
 */
public class Vlan extends HibernateBean implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4151;

    /** persistent field */
    private String vlanTag;

    /** persistent field */
    private Port port;

    /** default constructor */
    public Vlan() { }

    /**
     * @return vlanTag a VLAN tag
     */ 
    public String getVlanTag() { return this.vlanTag; }

    /**
     * @param vlanTag VLAN tag to associate with this instance
     */ 
    public void setVlanTag(String vlanTag) { this.vlanTag = vlanTag; }


    /**
     * @return port a port instance (uses association)
     */ 
    public Port getPort() { return this.port; }

    /**
     * @param port a port instance (uses association)
     */ 
    public void setPort(Port port) { this.port = port; }


    // need to override superclass because dealing with transient
    // instances as well
    public boolean equals(Object o) {
        if (this == o) { return true; }
        Class thisClass = Hibernate.getClass(this);
        if (o == null || thisClass != Hibernate.getClass(o)) {
            return false;
        }
        Vlan castOther = (Vlan) o;
        // if both of these have been saved to the database
        if ((this.getId() != null) &&
            (castOther.getId() != null)) {
            return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
        } else {
            return new EqualsBuilder()
                .append(this.getVlanTag(), castOther.getVlanTag())
                .append(this.getPort(), castOther.getPort())
                .isEquals();
        }
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }
}
