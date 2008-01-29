package net.es.oscars.bss.topology;

import net.es.oscars.database.HibernateBean;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import org.hibernate.Hibernate;

import java.io.Serializable;

import java.util.Set;


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
    private String topologyIdent;

    /** persistent field */
    private Domain domain;
    private Set ports;
    private NodeAddress nodeAddress;

    /** default constructor */
    public Node() {
    }

    /**
     * @return valid a boolean indicating whether this entry is still valid
     */
    public boolean isValid() {
        return this.valid;
    }

    /**
     * @param valid a boolean indicating whether this entry is still valid
     */
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    /**
     * @return topologyIdent a string with the name of this node
     */
    public String getTopologyIdent() {
        return this.topologyIdent;
    }

    /**
     * @param topologyIdent a string with the name of this node
     */
    public void setTopologyIdent(String topologyIdent) {
        this.topologyIdent = topologyIdent;
    }

    /**
     * @return domain a Domain instance (uses association)
     */
    public Domain getDomain() {
        return this.domain;
    }

    /**
     * @param domain a Domain instance (uses association)
     */
    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    public void setPorts(Set ports) {
        this.ports = ports;
    }

    public Set getPorts() {
        return this.ports;
    }

    public boolean addPort(Port port) {
        boolean added = this.ports.add(port);

        if (added) {
            port.setNode(this);
        }

        return added;
    }

    public void removePort(Port port) {
        this.ports.remove(port);
    }

    public void setNodeAddress(NodeAddress nodeAddress) {
        this.nodeAddress = nodeAddress;
    }

    public NodeAddress getNodeAddress() {
        return this.nodeAddress;
    }

    // need to override superclass because dealing with transient
    // instances as well
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        Class thisClass = Hibernate.getClass(this);

        if ((o == null) || (thisClass != Hibernate.getClass(o))) {
            return false;
        }

        Node castOther = (Node) o;

        // if both of these have been saved to the database
        if ((this.getId() != null) && (castOther.getId() != null)) {
            return new EqualsBuilder().append(this.getId(), castOther.getId())
                                      .isEquals();
        } else {
            // used in updating the topology database; only this field
            // are important in determining equality
        	/*
            return new EqualsBuilder().append(this.getTopologyIdent(),
                castOther.getTopologyIdent()).isEquals();
                */
            return new EqualsBuilder().append(this.getTopologyIdent(),
                    castOther.getTopologyIdent())
                                          .append(this.getDomain(),
                    castOther.getDomain()).isEquals();
        }
    }

    public String toString() {
        return new ToStringBuilder(this).append("id", getId()).toString();
    }
}