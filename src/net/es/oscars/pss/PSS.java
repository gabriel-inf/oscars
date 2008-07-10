package net.es.oscars.pss;

import net.es.oscars.bss.Reservation;

/**
 * Interface implemented by components that setup paths. Modules written
 * to handle network configuration should implement this interface.
 *
 * @author Andrew Lake (alake@internet2.edu)
 */
public interface PSS{
    public String createPath(Reservation resv) throws PSSException;
    public String refreshPath(Reservation resv) throws PSSException;
    public String teardownPath(Reservation resv, String newStatus) throws PSSException;
}
