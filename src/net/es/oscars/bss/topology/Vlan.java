package net.es.oscars.bss.topology;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;

import net.es.oscars.BeanUtils;

/**
 * Vlan is the Hibernate bean class for the bss.vlans table.
 */
public class Vlan extends BeanUtils implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4151;

    /** persistent field */
    private int vlanTag;

    /** persistent field */
    private Router router;

    /** default constructor */
    public Vlan() { }

    /**
     * @return vlanTag a VLAN tag
     */ 
    public int getVlanTag() { return this.vlanTag; }

    /**
     * @param vlanTag VLAN tag to associate with this instance
     */ 
    public void setVlanTag(int vlanTag) { this.vlanTag = vlanTag; }


    /**
     * @return router a router instance (uses association)
     */ 
    public Router getRouter() { return this.router; }

    /**
     * @param router a router instance (uses association)
     */ 
    public void setRouter(Router router) { this.router = router; }


    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }
}
