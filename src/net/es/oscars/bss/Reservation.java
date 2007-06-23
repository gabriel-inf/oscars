package net.es.oscars.bss;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;

import net.es.oscars.database.HibernateBean;
import net.es.oscars.bss.topology.Path;
import net.es.oscars.bss.topology.Domain;
import net.es.oscars.pathfinder.CommonPath;

/**
 * Reservation is the Hibernate bean for for the bss.reservations table.
 */
public class Reservation extends HibernateBean implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4099;

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
    private Integer srcIpPort;

    /** nullable persistent field */
    private Integer destIpPort;

    /** nullable persistent field */
    private String dscp;

    /** nullable persistent field */
    private String protocol;

    /** nullable persistent field */
    private String description;

    /** persistent field */
    private Path path;

    private CommonPath commonPath;

    /** default constructor */
    public Reservation() { }


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
     * @return srcIpPort An Integer with the reservation's source port
     */ 
    public Integer getSrcIpPort() { return this.srcIpPort; }

    /**
     * @param srcIpPort An Integer with the reservation's source port
     */ 
    public void setSrcIpPort(Integer srcIpPort) { this.srcIpPort = srcIpPort; }


    /**
     * @return destIpPort An Integer with the reservation's destination port
     */ 
    public Integer getDestIpPort() { return this.destIpPort; }

    /**
     * @param destIpPort An Integer with the reservation's destination port
     */ 
    public void setDestIpPort(Integer destIpPort) { this.destIpPort = destIpPort; }


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


    /**
     * @return commonPath transient CommonPath instance for reservation
     */ 
    public CommonPath getCommonPath() { return this.commonPath; }

    /**
     * @param commonPath transient CommonPath instance 
     */ 
    public void setCommonPath(CommonPath commonPath) {
        this.commonPath = commonPath;
    }


    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .append("login", getLogin())
            .append("start", getSrcHost())
            .append("destination", getDestHost())
            .append("status", getStatus())
            .append("startTime", getStartTime())
            .append("endTime", getEndTime())
            .toString();
    }
}
