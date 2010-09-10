package net.es.oscars.pss.common;

import net.es.oscars.bss.Reservation;
import net.es.oscars.pss.PSSException;

public interface PSSFailureHandler {
    public void handleFailure(Reservation resv, PSSAction action) throws PSSException;
    
}
