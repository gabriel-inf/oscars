package net.es.oscars.rmi.bss.xface;

import java.io.Serializable;
import java.util.List;

import net.es.oscars.bss.Reservation;

/**
 * Bean containing reservation, if any from a RMI query of a given global
 * reservation id.
 */
public class RmiQueryResReply implements Serializable {
    private static final long serialVersionUID = 50;

    // reservation, if any, matching request
    private Reservation reservation;

    private boolean internalPathAuthorized;  // whether to display internal path

    public RmiQueryResReply() {
    }

    /**
     * @return reservation reservation matching gri
     */
    public Reservation getReservation() {
        return this.reservation;
    }

    /**
     * @param reservation reservation matching gri
     */
    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    /**
     * @return a boolean indicating whether can display internal path
     */
    public boolean isInternalPathAuthorized() {
        return this.internalPathAuthorized;
    }

    /**
     * @param auth a boolean indicating whether can display internal path
     */
    public void setInternalPathAuthorized(boolean auth) {
        this.internalPathAuthorized = auth;
    }
}
