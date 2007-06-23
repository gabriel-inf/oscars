package net.es.oscars.bss.topology;

import java.util.Set;
import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;

import net.es.oscars.database.HibernateBean;

/**
 * Node is adapted from a Middlegen class automatically generated 
 * from the schema for the bss.nodes table.
 */
public class Node extends HibernateBean implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4151;

    /** persistent field */
    private boolean valid;

    /** persistent field */
    private String name;

    private Set ports;

    /** default constructor */
    public Node() { }

    /**
     * @return valid a boolean indicating whether this entry is still valid
     */ 
    public boolean isValid() { return this.valid; }

    /**
     * @param valid a boolean indicating whether this entry is still valid
     */ 
    public void setValid(boolean valid) { this.valid = valid; }


    /**
     * @return name a string with the name of this node
     */ 
    public String getName() { return this.name; }

    /**
     * @param name a string with the name of this node
     */ 
    public void setName(String name) { this.name = name; }


    public void setPorts(Set ports) {
        this.ports = ports;
    }

    public Set getPorts() {
        return this.ports;
    }

    public void addPort(Port port) {
        port.setNode(this);
        this.ports.add(port);
    }


    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }
}
