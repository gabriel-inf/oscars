package net.es.oscars.bss;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import net.es.oscars.pathfinder.Path;

/**
 * Reservation is adapted from a Middlegen class automatically generated 
 * from the schema for the oscars.reservations table.
 */
public class Reservation implements Serializable {
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
    private Long bandwidth;

    /** persistent field */
    private Long burstLimit;

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

    /** persistent field */
    private Path path;

    /** default constructor */
    public Reservation() { }


    /**
     * @return id An Integer with the reservation's primary key
     */ 
    public Integer getId() { return this.id; }

    /**
     * @param id An Integer with the primary key
     */ 
    public void setId(Integer id) { this.id = id; }


    /**
     * @return startTime A Long with the reservation start time in epoch ms
     */ 
    public Long getStartTime() { return this.startTime; }

    /**
     * @param startTime A Long with the reservation start time
     */ 
    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }


    /**
     * @return endTime A Long with the reservation end time
     */ 
    public Long getEndTime() { return this.endTime; }

    /**
     * @param endTime A Long with the reservation end time
     */ 
    public void setEndTime(Long endTime) { this.endTime = endTime; }


    /**
     * @return createdTime A Long with the reservation creation time
     */ 
    public Long getCreatedTime() { return this.createdTime; }

    /**
     * @param createdTime A Long with the reservation creation time
     */ 
    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }


    /**
     * @return bandwidth A Long with the reservation's requested bandwidth
     */ 
    public Long getBandwidth() { return this.bandwidth; }

    /**
     * @param bandwidth A Long with the reservation's requested bandwidth
     */ 
    public void setBandwidth(Long bandwidth) { this.bandwidth = bandwidth; }


    /**
     * @return burstLimit A Long with the reservation's burst limit
     */ 
    public Long getBurstLimit() { return this.burstLimit; }

    /**
     * @param burstLimit A Long with the reservation's burst limit
     */ 
    public void setBurstLimit(Long burstLimit) {
        this.burstLimit = burstLimit;
    }


    /**
     * @return login A String with the user's login name
     */ 
    public String getLogin() { return this.login; }

    /**
     * @param login A String with the user's login name
     */ 
    public void setLogin(String login) { this.login = login; }


    /**
     * @return status A String with the reservation's current status
     */ 
    public String getStatus() { return this.status; }

    /**
     * @param status A String with the reservation's current status
     */ 
    public void setStatus(String status) { this.status = status; }


    /**
     * @return lspClass A String with the reservation's LSP class
     */ 
    public String getLspClass() { return this.lspClass; }

    /**
     * @param lspClass A String with the reservation's LSP class
     */ 
    public void setLspClass(String lspClass) { this.lspClass = lspClass; }


    /**
     * @return srcHost A String with the source's IP address
     */ 
    public String getSrcHost() { return this.srcHost; }

    /**
     * @param srcHost A String with the source's IP address
     */ 
    public void setSrcHost(String srcHost) { this.srcHost = srcHost; }


    /**
     * @return destHost A String with the destination's IP address
     */ 
    public String getDestHost() { return this.destHost; }

    /**
     * @param destHost A String with the destination's IP address
     */ 
    public void setDestHost(String destHost) { this.destHost = destHost; }


    /**
     * @return srcPort An Integer with the reservation's source port
     */ 
    public Integer getSrcPort() { return this.srcPort; }

    /**
     * @param srcPort An Integer with the reservation's source port
     */ 
    public void setSrcPort(Integer srcPort) { this.srcPort = srcPort; }


    /**
     * @return destPort An Integer with the reservation's destination port
     */ 
    public Integer getDestPort() { return this.destPort; }

    /**
     * @param destPort An Integer with the reservation's destination port
     */ 
    public void setDestPort(Integer destPort) { this.destPort = destPort; }


    /**
     * @return dscp A String with the reservation's DSCP
     */ 
    public String getDscp() { return this.dscp; }

    /**
     * @param dscp A String with the reservation's DSCP
     */ 
    public void setDscp(String dscp) { this.dscp = dscp; }


    /**
     * @return protocol A String with the reservation's desired protocol
     */ 
    public String getProtocol() { return this.protocol; }

    /**
     * @param protocol A String with the reservation's desired protocol
     */ 
    public void setProtocol(String protocol) { this.protocol = protocol; }


    /**
     * @return description A String with the reservation's description
     */ 
    public String getDescription() { return this.description; }

    /**
     * @param description A String with the reservation's description
     */ 
    public void setDescription(String description) {
        this.description = description;
    }


    /**
     * @return path starting path instance associated with reservation
     */ 
    public Path getPath() { return this.path; }

    /**
     * @param path path instance to associate with this reservation
     */ 
    public void setPath(Path path) { this.path = path; }


    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof Reservation) ) return false;
        Reservation castOther = (Reservation) other;
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
