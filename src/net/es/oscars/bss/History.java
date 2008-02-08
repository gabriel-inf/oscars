package net.es.oscars.bss;

import net.es.oscars.bss.topology.*;
import net.es.oscars.database.*;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.*;
import java.math.*;
import java.security.*;
import java.text.DateFormat;
import java.io.Serializable;


/**
 * History is the Hibernate bean for for the bss.history table.
 */
public class History extends HibernateBean implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 5886;

    /** persistent field */
    private Reservation reservation;

    /** persistent field */
    private String traceId;

    /** persistent field */
    private String description;

    /** persistent field */
    private String operationType;

    /** persistent field */
    private Long operationTime;

    /** persistent field */
    private String result;

    /** persistent field */
    private String receivedFrom;

    /** persistent field */
    private String forwardedTo;

    /** default constructor */
    public History() {
    }

    /**
     * @return Reservation the Reservation object this history message is attached to
     */
    public Reservation getReservation() {
        return this.reservation;
    }

    /**
     * @param Reservation reservation the Reservation this history message
     * should be attached to
     */
    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    /**
     * @return operationTime A Long with the operation time (Unix time)
     */
    public Long getOperationTime() {
        return this.operationTime;
    }

    /**
     * @param operationTime A Long with the operation time
     */
    public void setOperationTime(Long operationTime) {
        this.operationTime = operationTime;
    }

    /**
     * @return traceId A String that contains the globally unique traceId
     */
    public String getTraceId() {
        return this.traceId;
    }

    /**
     * @param traceId The String that contains the globally unique traceId
     */
    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    /**
     * @return description A String that contains the human-readable description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @param description The String that contains the human-readable description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return result A String that contains the result of the operation
     */
    public String getResult() {
        return this.result;
    }

    /**
     * @param result The String that contains the result of the operation
     */
    public void setResult(String result) {
        this.result = result;
    }

    /**
     * @return operationType A String that formalizes the operation
     * type; typically the SOAP method name
     */
    public String getOperationType() {
        return this.operationType;
    }

    /**
     * @param operationType The String that formalizes the operation
     * type; typically the SOAP method name
     */
    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    /**
     * @return receivedFrom A String that contains the URI of the IDC
     * this operation was received from; null or empty if none
     */
    public String getReceivedFrom() {
        return this.receivedFrom;
    }

    /**
     * @param receivedFrom The String that contains the URI of the IDC
     * this operation was received from; null or empty if none
     */
    public void setReceivedFrom(String receivedFrom) {
        this.receivedFrom = receivedFrom;
    }

    /**
     * @return forwardedTo A String that contains the URI of the IDC
     * this operation was forwarded to; null or empty if none
     */
    public String getForwardedTo() {
        return this.forwardedTo;
    }

    /**
     * @param forwardedTo The String that contains the URI of the IDC
     * this operation was forwarded to; null or empty if none
     */
    public void setForwardedTo(String forwardedTo) {
        this.forwardedTo = forwardedTo;
    }

    public static String makeTraceId(String uri) {
        Date d = new Date();
        String s = uri + d.toString();
        MessageDigest m;
        try {
        	 m = MessageDigest.getInstance("MD5");
        } catch (java.security.NoSuchAlgorithmException ex) {
        	return null; // should never happen
        }
        m.update(s.getBytes(), 0, s.length());

        BigInteger i = new BigInteger(1, m.digest());

        return i.toString(16);
    }
}
