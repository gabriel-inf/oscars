package net.es.oscars.pss.common;

import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.topology.Path;
import net.es.oscars.pss.PSSException;

public interface PSSHandler {
    public void setup(Reservation resv, Path localPath, PSSDirection direction) throws PSSException;
    public void teardown(Reservation resv, Path localPath, PSSDirection direction) throws PSSException;
    public void status(Reservation resv, Path localPath, PSSDirection direction)throws PSSException;

    public void setConfig(PSSHandlerConfigBean config);
    public PSSHandlerConfigBean getConfig();

}
