package net.es.oscars.rmi.bss.xface;

import java.io.Serializable;
import java.util.List;

import net.es.oscars.bss.Reservation;

/**
 * Bean containing response from a RMI request to return reservations matching
 * given set of parameters.
 */
public class RmiListResReply implements Serializable {
    // for version 0.5
    private static final long serialVersionUID = 50;

    // list of reservations, if any, matching request
    private List<Reservation> reservations;

    public RmiListResReply() {
    }

    /**
     * @return reservations list of reservations
     */
    public List<Reservation> getReservations() {
        return this.reservations;
    }

    /**
     * @param reservations list of reservations
     */
    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }
}
