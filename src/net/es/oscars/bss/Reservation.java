package net.es.oscars.bss;

import java.io.Serializable;
import java.util.*;
import java.text.DateFormat;

import org.apache.commons.lang.builder.ToStringBuilder;

import net.es.oscars.database.HibernateBean;
import net.es.oscars.bss.topology.*;

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
     * @return startTime A Long with the reservation start time (Unix time)
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
            .toString();
    }

    public String toString(String dbname) {
        StringBuilder sb = new StringBuilder();
        String strParam = null;

        // this may be called from methods where the reservation has
        // not been completely set up, so more null checks are
        // necessary here
        if (this.getGlobalReservationId() != null) {
            sb.append("\nGRI: " + this.getGlobalReservationId() + "\n");
        }
        strParam = this.getDescription();
        if (strParam != null) {
            sb.append("description: " + strParam + "\n");
        }
        if (this.getLogin() != null) {
            sb.append("login: " + this.getLogin() + "\n");
        }
        if (this.getStatus() != null) {
            sb.append("status: " + this.getStatus() + "\n");
        }
        Long tm = this.getStartTime() * 1000L;
        DateFormat df = DateFormat.getInstance();
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        if (tm != null) {
            Date date = new Date(tm);
            sb.append("start time: " + df.format(date) + " UTC\n");
        }
        tm = this.getEndTime() * 1000L;
        if (tm != null) {
            Date date = new Date(tm);
            sb.append("end time: " + df.format(date) + " UTC\n");
        }
        if (this.getBandwidth() != null) {
            sb.append("bandwidth: " + this.getBandwidth() + "\n");
        }
       
        Path path= this.getPath();
        if (path == null) {
            return sb.toString();
        }
        if (path.getPathSetupMode() != null) {
            sb.append("path setup mode: " + path.getPathSetupMode() + "\n");
        }
        Layer2Data layer2Data = path.getLayer2Data();
        if (layer2Data != null) {
            sb.append("layer: 2\n");
            if (layer2Data.getSrcEndpoint() != null) {
                sb.append("source endpoint: " +
                      layer2Data.getSrcEndpoint() + "\n");
            }
            if (layer2Data.getDestEndpoint() != null) {
                sb.append("dest endpoint: " +
                          layer2Data.getDestEndpoint() + "\n");
            }
            PathElem pathElem = path.getPathElem();
            if (pathElem != null) {
                String linkDescr = pathElem.getLinkDescr();
                if (linkDescr != null) {
                    sb.append("VLAN tag: " + linkDescr + "\n");
                }
            }
        }
        Layer3Data layer3Data = path.getLayer3Data();
        if (layer3Data != null) {
            sb.append("layer: 3\n");
            if (layer3Data.getSrcHost() != null) {
                sb.append("source host: " + layer3Data.getSrcHost() + "\n");
            }
            if (layer3Data.getDestHost() != null) {
                sb.append("dest host: " + layer3Data.getDestHost() + "\n");
            }
            if (layer3Data.getProtocol() != null) {
                sb.append("protocol: " + layer3Data.getProtocol() + "\n");
            }
            if ((layer3Data.getSrcIpPort() != null) &&
                (layer3Data.getSrcIpPort() != 0)) {
                sb.append("src IP port: " + layer3Data.getSrcIpPort() + "\n");
            }
            if ((layer3Data.getDestIpPort() != null) &&
                (layer3Data.getDestIpPort() != 0)) {
                sb.append("dest IP port: " +
                          layer3Data.getDestIpPort() + "\n");
            }
            if (layer3Data.getDscp() != null) {
                sb.append("dscp: " +  layer3Data.getDscp() + "\n");
            }
        }
        MPLSData mplsData = path.getMplsData();
        if (mplsData != null) {
            if (mplsData.getBurstLimit() != null) {
                sb.append("burst limit: " + mplsData.getBurstLimit() + "\n");
            }
            if (mplsData.getLspClass() != null) {
                sb.append("LSP class: " + mplsData.getLspClass() + "\n");
            }
        }
        sb.append("local hops: \n\n");
        Utils utils = new Utils(dbname);
        sb.append(utils.pathToString(path));
        sb.append("\n");
        return sb.toString();
    }
}
