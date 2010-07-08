package net.es.oscars.pss.eompls;

import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.topology.Path;
import net.es.oscars.pss.common.PSSDirection;
import net.es.oscars.pss.common.PSSHandlerConfigBean;

public interface EoMPLSHandler {
    public void setup(Reservation resv, Path localPath, PSSDirection direction);
    public void teardown(Reservation resv, Path localPath, PSSDirection direction);
    public void status(Reservation resv, Path localPath, PSSDirection direction);

    public void setConfig(PSSHandlerConfigBean config);
    public PSSHandlerConfigBean getConfig();

}
