package net.es.oscars.bss;

import java.io.Serializable;
import net.es.oscars.database.HibernateBean;



/**
 * Job is the Hibernate bean for for the bss.jobs table.
 */
public class Job extends HibernateBean implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 5886;

    /** persistent field */
    private Reservation reservation;

    /** persistent field */
    private String operation;

    /** persistent field */
    private Boolean done;

    /** persistent field */
    private Long scheduledTime;

    /** persistent field */
    private Long actualTime;

    /** persistent field */
    private String result;

    /** default constructor */
    public Job() {
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
     * @return scheduledTime A Long with the scheduled operation time (Unix time)
     */
    public Long getScheduledTime() {
        return this.scheduledTime;
    }

    /**
     * @param scheduledTime A Long with the scheduled operation time
     */
    public void setScheduledTime(Long scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    /**
     * @return actualTime A Long with the actual operation time (Unix time)
     */
    public Long getActualTime() {
        return this.actualTime;
    }

    /**
     * @param actualTime A Long with the actual operation time
     */
    public void setActualTime(Long actualTime) {
        this.actualTime = actualTime;
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
     * @return operation A String that formalizes the operation
     * to be performed; use PATH_SETUP and PATH_TEARDOWN
     */
    public String getOperation() {
        return this.operation;
    }

    /**
     * @param operation The String that formalizes the operation
     * to be performed; use PATH_SETUP and PATH_TEARDOWN
     */
    public void setOperation(String operation) {
        this.operation = operation;
    }

    /**
     * @return done A Boolean describing if the job has been performed
     * or not 
     */
    public Boolean getDone() {
        return this.done;
    }

    /**
     * @return done A Boolean describing if the job must be performed
     */
    public void setDone(Boolean done) {
        this.done = done;
    }

}
