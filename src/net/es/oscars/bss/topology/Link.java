package net.es.oscars.bss.topology;

import net.es.oscars.database.HibernateBean;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import org.hibernate.Hibernate;

import java.io.Serializable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * Link is adapted from a Middlegen class automatically generated
 * from the schema for the bss.ports table.
 */
public class Link extends HibernateBean implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4151;

    /** persistent field */
    private boolean valid;

    /** persistent field */
    private int snmpIndex;

    /** persistent field */
    private String topologyIdent;

    /** nullable persistent field */
    private String trafficEngineeringMetric;

    /** nullable persistent field */
    private Long capacity;

    /** nullable persistent field */
    private Long maximumReservableCapacity;

    /** nullable persistent field */
    private Long minimumReservableCapacity;

    /** nullable persistent field */
    private Long granularity;

    /** nullable persistent field */
    private Long unreservedCapacity;

    /** nullable persistent field */
    private String alias;

    /** nullable persistent field */
    private Link remoteLink;

    /** persistent field */
    private Port port;
    private Set ipaddrs;
    private L2SwitchingCapabilityData l2SwitchingCapabilityData;

    /** default constructor */
    public Link() {
    }
    public Link(Port portDB, boolean init) {
        if (!init) {
            return;
        }
        this.setValid(true);
        this.setTopologyIdent("changeme");
        this.setAlias("changeme");

        this.setSnmpIndex(1); // we don't have this info
        this.setTrafficEngineeringMetric("100"); // what should this be?

        this.setCapacity(0L);
        this.setMaximumReservableCapacity(0L);
        this.setMinimumReservableCapacity(0L);
        this.setUnreservedCapacity(0L);
        this.setGranularity(0L);
        this.setPort(portDB);
        this.setRemoteLink(null);
        this.setIpaddrs(new HashSet());
    }


    /**
     * @return valid a boolean indicating whether link is still valid
     */
    public boolean isValid() {
        return this.valid;
    }

    /**
     * @param valid a boolean indicating whether link is still valid
     */
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    /**
     * @return snmpIndex a SNMP index from ifrefpoll
     */
    public int getSnmpIndex() {
        return this.snmpIndex;
    }

    /**
     * @param snmpIndex a SNMP index
     */
    public void setSnmpIndex(int snmpIndex) {
        this.snmpIndex = snmpIndex;
    }

    /**
     * @return topologyIdent a string with the link's logical name
     */
    public String getTopologyIdent() {
        return this.topologyIdent;
    }

    /**
     * @param topologyIdent a string with the link's logical name
     */
    public void setTopologyIdent(String topologyIdent) {
        this.topologyIdent = topologyIdent;
    }

    /**
     * @return a string with the link's trafficEngineeringMetric
     */
    public String getTrafficEngineeringMetric() {
        return this.trafficEngineeringMetric;
    }

    /**
     * @param trafficEngineeringMetric a string with the te metric
     */
    public void setTrafficEngineeringMetric(String trafficEngineeringMetric) {
        this.trafficEngineeringMetric = trafficEngineeringMetric;
    }

    /**
     * @return capacity a long with the link's maximum bandwidth
     */
    public Long getCapacity() {
        return this.capacity;
    }

    /**
     * @param capacity a long with the link's maximum bandwidth
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
     * @return alias a string with the link's alias
     */
    public String getAlias() {
        return this.alias;
    }

    /**
     * @param alias a string with the link's alias
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * @return remoteLink Link instance with the other side of the connection
     */
    public Link getRemoteLink() {
        return this.remoteLink;
    }

    /**
     * @param remoteLink Link instance with the other side of the connection
     */
    public void setRemoteLink(Link remoteLink) {
        this.remoteLink = remoteLink;
    }

    /**
     * @return port a Port instance (uses association)
     */
    public Port getPort() {
        return this.port;
    }

    /**
     * @param port a Port instance (uses association)
     */
    public void setPort(Port port) {
        this.port = port;
    }

    /**
     * @return l2SwitchingCapabilityData an optional L2SwitchingCapabilityData instance
     */
    public L2SwitchingCapabilityData getL2SwitchingCapabilityData() {
        return this.l2SwitchingCapabilityData;
    }

    /**
     * @param l2SwitchingCapabilityData an optional L2SwitchingCapabilityData instance
     */
    public void setL2SwitchingCapabilityData(
        L2SwitchingCapabilityData l2SwitchingCapabilityData) {
        this.l2SwitchingCapabilityData = l2SwitchingCapabilityData;
    }

    public void setIpaddrs(Set ipaddrs) {
        this.ipaddrs = ipaddrs;
    }

    public Set getIpaddrs() {
        return this.ipaddrs;
    }

    public boolean addIpaddr(Ipaddr ipaddr) {
        boolean added = this.ipaddrs.add(ipaddr);

        if (added) {
            ipaddr.setLink(this);
        }

        return added;
    }

    public void removeIpaddr(Ipaddr ipaddr) {
        this.ipaddrs.remove(ipaddr);
    }

    public Ipaddr getIpaddrByIP(String IP) {
        if (this.ipaddrs == null) {
            return null;
        }

        Iterator ipaddrIt = this.ipaddrs.iterator();
        while (ipaddrIt.hasNext()) {
            Ipaddr ipaddr = (Ipaddr) ipaddrIt.next();
            if (ipaddr.getIP().equals(IP)) {
                return ipaddr;
            }
        }
        return null;
    }


    public boolean equalsTopoId(Link link) {
        String thisFQTI = this.getFQTI();
        String thatFQTI = link.getFQTI();
        return thisFQTI.equals(thatFQTI);
    }

    /**
     * Constructs the fully qualified topology identifier
     * @return the topology identifier
     */
    public String getFQTI() {
        String parentFqti = this.getPort().getFQTI();
        String topoId = TopologyUtil.getLSTI(this.getTopologyIdent(), "Link");

        return (parentFqti + ":link=" + topoId);
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

        Link castOther = (Link) o;

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
                                      .append(this.getPort(),
                castOther.getPort()).isEquals();
            */
            return new EqualsBuilder().append(this.getTopologyIdent(),
                    castOther.getTopologyIdent())
                                          .append(this.getPort(),
                    castOther.getPort()).isEquals();
        }
    }
    
    /**
     * Only copies topology identifier information. Useful for detaching
     * object from hibernate and passing to other processes that only care
     * about IDs.
     *
     * @return a copy of this link
     **/
    public Link topoCopy(){
        Link linkCopy = new Link();
        Port portCopy = null;
        linkCopy.setTopologyIdent(this.topologyIdent);
        
        if(this.port != null){
            portCopy = this.port.topoCopy();
        }
        linkCopy.setPort(portCopy);
        
        return linkCopy;
    }
    
    public String toString() {
        return new ToStringBuilder(this).append("id", getId()).toString();
    }
}
