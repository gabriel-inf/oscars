package net.es.oscars.bss.topology;

import net.es.oscars.database.HibernateBean;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import org.hibernate.Hibernate;

import java.io.Serializable;

import java.util.HashSet;
import java.util.Set;


/**
 * Port is adapted from a Middlegen class automatically generated
 * from the schema for the bss.ports table.
 */
public class Port extends HibernateBean implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4151;

    /** persistent field */
    private boolean valid;

    /** persistent field */
    private int snmpIndex;

    /** persistent field */
    private String topologyIdent;

    /** persistent field */
    private Long capacity;

    /** persistent field */
    private Long maximumReservableCapacity;

    /** persistent field */
    private Long minimumReservableCapacity;

    /** nullable persistent field */
    private Long granularity;

    /** persistent field */
    private Long unreservedCapacity;

    /** nullable persistent field */
    private String alias;

    /** persistent field */
    private Node node;
    private Set links;

    /** default constructor */
    public Port() {
    }
    /** initializing constructor */
    public Port(Node nodeDB, boolean init) {
        if (!init) {
            return;
        }

        this.setValid(true);
        this.setTopologyIdent("changeme");
        this.setCapacity(0L);
        this.setMaximumReservableCapacity(0L);
        this.setMinimumReservableCapacity(0L);
        this.setUnreservedCapacity(0L);
        this.setGranularity(0L);
        this.setAlias("changeme");
        this.setSnmpIndex(1);
        this.setLinks(new HashSet());
        this.setNode(nodeDB);
    }

    public Port(Port port) {
        this.valid = port.isValid();
        this.alias = port.getAlias();
        this.capacity = port.getCapacity();
        this.granularity = port.getGranularity();
        this.maximumReservableCapacity = port.getMaximumReservableCapacity();
        this.minimumReservableCapacity = port.getMinimumReservableCapacity();
        this.snmpIndex = port.getSnmpIndex();
        this.topologyIdent = port.getTopologyIdent();
        this.links = port.getLinks();
        this.unreservedCapacity = port.getUnreservedCapacity();
        this.node = port.getNode();
    }

    /**
     * @return valid a boolean indicating whether port is still valid
     */
    public boolean isValid() {
        return this.valid;
    }

    /**
     * @param valid a boolean indicating whether port is still valid
     */
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    /**
     * @return snmpIndex int with SNMP index of this port
     */
    public int getSnmpIndex() {
        return this.snmpIndex;
    }

    /**
     * @param snmpIndex int with SNMP index
     */
    public void setSnmpIndex(int snmpIndex) {
        this.snmpIndex = snmpIndex;
    }

    /**
     * @return topologyIdent a string with the port's name in topology
     */
    public String getTopologyIdent() {
        return this.topologyIdent;
    }

    /**
     * @param topologyIdent a string with the port's name in topology
     */
    public void setTopologyIdent(String topologyIdent) {
        this.topologyIdent = topologyIdent;
    }

    /**
     * @return capacity a long with the port's maximum bandwidth
     */
    public Long getCapacity() {
        return this.capacity;
    }

    /**
     * @param capacity a long with the port's maximum bandwidth
     */
    public void setCapacity(Long capacity) {
        this.capacity = capacity;
    }

    /**
     * @return maximumReservableCapacity Long with the maximum utilization
     */
    public Long getMaximumReservableCapacity() {
        return this.maximumReservableCapacity;
    }

    /**
     * @param maximumReservableCapacity Long with the maximum utilization
     */
    public void setMaximumReservableCapacity(Long maximumReservableCapacity) {
        this.maximumReservableCapacity = maximumReservableCapacity;
    }

    /**
     * @return minimumReservableCapacity Long with the minimum utilization
     */
    public Long getMinimumReservableCapacity() {
        return this.minimumReservableCapacity;
    }

    /**
     * @param minimumReservableCapacity Long with the minimum utilization
     */
    public void setMinimumReservableCapacity(Long minimumReservableCapacity) {
        this.minimumReservableCapacity = minimumReservableCapacity;
    }

    /**
     * @return granularity increment of bandwidth that can be requested
     */
    public Long getGranularity() {
        return this.granularity;
    }

    /**
     * @param granularity increment of bandwidth that can be requested
     */
    public void setGranularity(Long granularity) {
        this.granularity = granularity;
    }

    /**
     * @return unreservedCapacity Long with the bandwidth available
     */
    public Long getUnreservedCapacity() {
        return this.unreservedCapacity;
    }

    /**
     * @param unreservedCapacity Long with the bandwidth available
     */
    public void setUnreservedCapacity(Long unreservedCapacity) {
        this.unreservedCapacity = unreservedCapacity;
    }

    /**
     * @return alias a string with the port's alias
     */
    public String getAlias() {
        return this.alias;
    }

    /**
     * @param alias a string with the port's alias
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * @return node a Node instance (uses association)
     */
    public Node getNode() {
        return this.node;
    }

    /**
     * @param node a Node instance (uses association)
     */
    public void setNode(Node node) {
        this.node = node;
    }

    public void setLinks(Set links) {
        this.links = links;
    }

    public Set getLinks() {
        return this.links;
    }

    public boolean addLink(Link link) {
        boolean added = this.links.add(link);

        if (added) {
            link.setPort(this);
        }

        return added;
    }

    public void removeLink(Link link) {
        this.links.remove(link);
    }

    public boolean equalsTopoId(Port port) {
        String thisFQTI = this.getFQTI();
        String thatFQTI = port.getFQTI();
        return thisFQTI.equals(thatFQTI);
    }

    /**
     * Constructs the fully qualified topology identifier
     * @return the topology identifier
     */
    public String getFQTI() {
        String parentFqti = this.getNode().getFQTI();
        String topoId = TopologyUtil.getLSTI(this.getTopologyIdent(), "Port");

        return (parentFqti + ":port=" + topoId);
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

        Port castOther = (Port) o;

        // if both of these have been saved to the database
        if ((this.getId() != null) && (castOther.getId() != null)) {
            return new EqualsBuilder().append(this.getId(), castOther.getId())
                                      .isEquals();
        } else {
            // used in updating the topology database; only these fields
            // are important in determining equality
            /*
            return new EqualsBuilder().append(this.getSnmpIndex(),
                castOther.getSnmpIndex())
                                      .append(this.getNode(),
                castOther.getNode()).isEquals();
            */
            return new EqualsBuilder().append(this.getTopologyIdent(),
                    castOther.getTopologyIdent())
                                          .append(this.getNode(),
                    castOther.getNode()).isEquals();
        }
    }

    public String toString() {
        return new ToStringBuilder(this).append("id", getId()).toString();
    }
}
