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
    private String payloadSender;

	/** persistent field */
    private String status;
    
    /** persistent field */
    private Integer localStatus;

    /** nullable persistent field */
    private String description;

    /** nullable persistent field */
    private String globalReservationId;

    /** persistent field */
    private Token token;

    /** default constructor */
    public Reservation() { }

    private Map<String,Path> pathMap = new HashMap<String,Path>();

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
	 * @return the payloadSender
	 */
	public String getPayloadSender() {
		return payloadSender;
	}

	/**
	 * @param payloadSender the payloadSender to set
	 */
	public void setPayloadSender(String payloadSender) {
		this.payloadSender = payloadSender;
	}
	
    /**
     * @return status A String with the reservation's current status
     */
    public String getStatus() { return this.status; }

    /**
     * @param status A String with the reservation's current status
     */
    public void setStatus(String status) { this.status = status; }

    /**
     * @return status an Integer with the reservation's local status
     */
    public Integer getLocalStatus() { return this.localStatus; }

    /**
     * @param status an Integer with the reservation's local status
     */
    public void setLocalStatus(Integer status) { this.localStatus = status; }

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

    public Map<String,Path> getPathMap() {
        return this.pathMap;
    }

    public void setPathMap(Map<String,Path> pathMap) {
        this.pathMap = pathMap;
    }

    public Path getPath(String pathType) {
        return this.pathMap.get(pathType);
    }

    public void setPath(Path path, String pathType) {
        this.pathMap.put(pathType, path);
    }

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

        Utils utils = new Utils(dbname);
        Path path = this.getPath("intra");
        if (path == null) {
            return sb.toString();
        }
        sb.append(utils.pathDataToString(path));
        sb.append("intradomain hops: \n\n");
        sb.append(utils.pathToString(path, false));
        path = this.getPath("inter");
        if (path == null) {
            return sb.toString();
        }
        sb.append(utils.pathDataToString(path));
        sb.append("\ninterdomain hops: \n\n");
        sb.append(utils.pathToString(path, true));
        sb.append("\n");
        return sb.toString();
    }
}
