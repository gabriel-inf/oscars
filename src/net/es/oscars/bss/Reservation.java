package net.es.oscars.bss;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;

import net.es.oscars.database.HibernateBean;
import net.es.oscars.bss.topology.Path;

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
    private String login;

    /** persistent field */
    private String status;

    /** nullable persistent field */
    private String description;
    
    /** nullable persistent field */
    private String globalReservationId;
    
    /** persistent field */
    private Path path;
    
    /** persistent field */
    private Token token;
    
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
     * @return an String with the reservation's GRI
     */ 
    public String getGlobalReservationId() { 
        return this.globalReservationId; 
    }

    /**
     * @param globalReservationId an String with the reservation's GRI
     */ 
    public void setGlobalReservationId(String globalReservationId) {
        this.globalReservationId = globalReservationId;
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
     * @return token instance associated with reservation
     */ 
    public Token getToken() { return this.token; }

    /**
     * @param token token instance to associate with this reservation
     */ 
    public void setToken(Token token) { this.token = token; }


    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .append("startTime", getStartTime())
            .append("endTime", getEndTime())
            .append("bandwidth", getBandwidth())
            .append("login", getLogin())
            .append("status", getStatus())
            .toString();
    }
}
