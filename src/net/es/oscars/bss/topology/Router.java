package net.es.oscars.bss.topology;

import java.util.Set;
import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;

import net.es.oscars.BeanUtils;

/**
 * Router is adapted from a Middlegen class automatically generated 
 * from the schema for the bss.routers table.
 */
public class Router extends BeanUtils implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4151;

    /** persistent field */
    private boolean valid;

    /** persistent field */
    private String name;

    private Set xfaces;

    /** default constructor */
    public Router() { }

    /**
     * @return valid a boolean indicating whether this entry is still valid
     */ 
    public boolean isValid() { return this.valid; }

    /**
     * @param valid a boolean indicating whether this entry is still valid
     */ 
    public void setValid(boolean valid) { this.valid = valid; }


    /**
     * @return name a string with the name of this router
     */ 
    public String getName() { return this.name; }

    /**
     * @param name a string with the name of this router
     */ 
    public void setName(String name) { this.name = name; }


    public void setInterfaces(Set xfaces) {
        this.xfaces = xfaces;
    }

    public Set getInterfaces() {
        return this.xfaces;
    }

    public void addInterface(Interface xface) {
        xface.setRouter(this);
        this.xfaces.add(xface);
    }


    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }
}
