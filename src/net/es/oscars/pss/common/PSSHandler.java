package net.es.oscars.pss.common;

import net.es.oscars.bss.Reservation;
import net.es.oscars.pss.PSSException;

public interface PSSHandler {
    
    /**
     * sets up a reservation in one direction
     * 
     * @param resv
     * @param localPath
     * @param direction
     * @param checkStatus
     * @throws PSSException in case of any failure
     */
    
    public void setup(Reservation resv, PSSDirection direction) throws PSSException;
    /**
     * tears down a reservation for one direction
     * 
     * @param resv
     * @param localPath
     * @param direction
     * @param checkStatus
     * @throws PSSException in case of any failure
     */
    public void teardown(Reservation resv, PSSDirection direction) throws PSSException;

}
