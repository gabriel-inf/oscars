package net.es.oscars.bss.policy;

import java.util.List;

import net.es.oscars.bss.*;

public interface PolicyFilter {
    void applyFilter(Reservation newReservation, 
            List<Reservation> activeReservations) throws BSSException; 
}
