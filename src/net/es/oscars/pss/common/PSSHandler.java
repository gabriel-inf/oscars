package net.es.oscars.pss.common;

import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.topology.Path;

public interface PSSHandler {
    public void setup(Reservation resv, Path localPath, PSSDirection direction);
    public void teardown(Reservation resv, Path localPath, PSSDirection direction);
    public void status(Reservation resv, Path localPath, PSSDirection direction);

    public void setConfig(PSSHandlerConfigBean config);
    public PSSHandlerConfigBean getConfig();

}
