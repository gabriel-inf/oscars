package net.es.oscars.bss.topology;

import java.util.Set;
import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.hibernate.Hibernate;

import net.es.oscars.database.HibernateBean;

/**
 * NodeAddress is the Hibernate bean for the bss.nodeAddresses table.
 */
public class NodeAddress extends HibernateBean implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4151;

    /** persistent field */
    private String address;

    /** persistent field */
    private Node node;

    /** default constructor */
    public NodeAddress() { }


    public NodeAddress(Node nodeDB, boolean init) {
        if (!init) {
            return;
        }
        this.setAddress("changeme");
        this.setNode(nodeDB);
    }

    /**
     * @return address a string with the a node's address
     */
    public String getAddress() { return this.address; }

    /**
     * @param address a string with the a node's address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @return node a Node instance (uses association)
     */
    public Node getNode() { return this.node; }

    /**
     * @param node a Node instance (uses association)
     */
    public void setNode(Node node) { this.node = node; }

    // need to override superclass because dealing with transient
    // instances as well
    public boolean equals(Object o) {
        if (this == o) { return true; }
        Class thisClass = Hibernate.getClass(this);
        if (o == null || thisClass != Hibernate.getClass(o)) {
            return false;
        }
        NodeAddress castOther = (NodeAddress) o;
        // if both of these have been saved to the database
        if ((this.getId() != null) &&
            (castOther.getId() != null)) {
            return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
        } else {
            return new EqualsBuilder()
                .append(this.getAddress(), castOther.getAddress())
                .append(this.getNode(), castOther.getNode())
                .isEquals();
        }
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }
}
