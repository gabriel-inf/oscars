package net.es.oscars.bss;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * ArchivedReservation is adapted from a Middlegen class automatically
 * generated from the schema for the oscars.archivedReservations table.  It is
 * not currently functional.
 */
public class ArchivedReservation implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4099;

    /** identifier field */
    private Integer id;

    /** persistent field */
    private Long startTime;

    /** persistent field */
    private Long endTime;

    /** persistent field */
    private Long createdTime;

    /** persistent field */
    private int bandwidth;

    /** persistent field */
    private int burstLimit;

    /** persistent field */
    private String login;

    /** persistent field */
    private String status;

    /** persistent field */
    private String lspClass;

    /** persistent field */
    private String srcHost;

    /** persistent field */
    private String destHost;

    /** nullable persistent field */
    private Integer srcPort;

    /** nullable persistent field */
    private Integer destPort;

    /** nullable persistent field */
    private String dscp;

    /** nullable persistent field */
    private String protocol;

    /** nullable persistent field */
    private String description;

    /** nullable persistent field */
    private String path;

    /** default constructor */
    public ArchivedReservation() { }

    /**
     * Auto generated getter method
     * @return id An Integer with the reservation's primary key
     */ 
    public Integer getId() { return this.id; }

    /**
     * Auto generated setter method
     * @param id An Integer with the primary key
     */ 
    public void setId(Integer id) { this.id = id; }


    /**
     * Auto generated getter method
     * @return startTime A Long with the reservation start time
     */ 
    public Long getStartTime() { return this.startTime; }

    /**
     * Auto generated setter method
     * @param startTime A Long with the reservation start time
     */ 
    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }


    /**
     * Auto generated getter method
     * @return endTime A Long with the reservation end time
     */ 
    public Long getEndTime() { return this.endTime; }

    /**
     * Auto generated setter method
     * @param endTime A Long with the reservation end time
     */ 
    public void setEndTime(Long endTime) { this.endTime = endTime; }


    /**
     * Auto generated getter method
     * @return createdTime A Long with the reservation creation time
     */ 
    public Long getCreatedTime() { return this.createdTime; }

    /**
     * Auto generated setter method
     * @param createdTime A Long with the reservation creation time
     */ 
    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }


    /**
     * Auto generated getter method
     * @return bandwidth An int with the reservation's requested bandwidth
     */ 
    public int getBandwidth() { return this.bandwidth; }

    /**
     * Auto generated setter method
     * @param bandwidth An int with the reservation's requested bandwidth
     */ 
    public void setBandwidth(int bandwidth) { this.bandwidth = bandwidth; }


    /**
     * Auto generated getter method
     * @return burstLimit An int with the reservation's burst limit
     */ 
    public int getBurstLimit() { return this.burstLimit; }

    /**
     * Auto generated setter method
     * @param burstLimit An int with the reservation's burst limit
     */ 
    public void setBurstLimit(int burstLimit) {
        this.burstLimit = burstLimit;
    }


    /**
     * Auto generated getter method
     * @return login A String with the user's login name
     */ 
    public String getLogin() { return this.login; }

    /**
     * Auto generated setter method
     * @param login A String with the user's login name
     */ 
    public void setLogin(String login) { this.login = login; }


    /**
     * Auto generated getter method
     * @return status A String with the reservation's current status
     */ 
    public String getStatus() { return this.status; }

    /**
     * Auto generated setter method
     * @param status A String with the reservation's current status
     */ 
    public void setStatus(String status) { this.status = status; }


    /**
     * Auto generated getter method
     * @return lspClass A String with the reservation's LSP class
     */ 
    public String getLspClass() { return this.lspClass; }

    /**
     * Auto generated setter method
     * @param lspClass A String with the reservation's LSP class
     */ 
    public void setLspClass(String lspClass) { this.lspClass = lspClass; }


    /**
     * Auto generated getter method
     * @return srcHost A String with the source's IP address
     */ 
    public String getSrcHost() { return this.srcHost; }

    /**
     * Auto generated setter method
     * @param srcHost A String with the source's IP address
     */ 
    public void setSrcHost(String srcHost) { this.srcHost = srcHost; }


    /**
     * Auto generated getter method
     * @return destHost A String with the destination's IP address
     */ 
    public String getDestHost() { return this.destHost; }

    /**
     * Auto generated setter method
     * @param destHost A String with the destination's IP address
     */ 
    public void setDestHost(String destHost) { this.destHost = destHost; }


    /**
     * Auto generated getter method
     * @return srcPort An Integer with the reservation's source port
     */ 
    public Integer getSrcPort() { return this.srcPort; }

    /**
     * Auto generated setter method
     * @param srcPort An Integer with the reservation's source port
     */ 
    public void setSrcPort(Integer srcPort) { this.srcPort = srcPort; }


    /**
     * Auto generated getter method
     * @return destPort An Integer with the reservation's destination port
     */ 
    public Integer getDestPort() { return this.destPort; }

    /**
     * Auto generated setter method
     * @param destPort An Integer with the reservation's destination port
     */ 
    public void setDestPort(Integer destPort) { this.destPort = destPort; }


    /**
     * Auto generated getter method
     * @return dscp A String with the reservation's DSCP
     */ 
    public String getDscp() { return this.dscp; }

    /**
     * Auto generated setter method
     * @param dscp A String with the reservation's DSCP
     */ 
    public void setDscp(String dscp) { this.dscp = dscp; }


    /**
     * Auto generated getter method
     * @return protocol A String with the reservation's desired protocol
     */ 
    public String getProtocol() { return this.protocol; }

    /**
     * Auto generated setter method
     * @param protocol A String with the reservation's desired protocol
     */ 
    public void setProtocol(String protocol) { this.protocol = protocol; }


    /**
     * Auto generated getter method
     * @return description A String with the reservation's description
     */ 
    public String getDescription() { return this.description; }

    /**
     * Auto generated setter method
     * @param description A String with the reservation's description
     */ 
    public void setDescription(String description) {
        this.description = description;
    }


    /**
     * Auto generated getter method
     * @return path A String listing the IP addresses in the circuit path
     */ 
    public String getPath() { return this.path; }

    /**
     * Auto generated setter method
     * @param path A String listing the IP addresses in the circuit path
     */ 
    public void setPath(String path) { this.path = path; }


    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof ArchivedReservation) ) return false;
        ArchivedReservation castOther = (ArchivedReservation) other;
        return new EqualsBuilder()
            .append(this.getId(), castOther.getId())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getId())
            .toHashCode();
    }

}
