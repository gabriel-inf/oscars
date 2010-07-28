package net.es.oscars.pss.common;

import net.es.oscars.bss.Reservation;

public interface PSSFailureHandler {
    public void handleFailure(Reservation resv, PSSAction action);
    
}
