package net.es.oscars.pss.eompls.junos;

import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.topology.Path;
import net.es.oscars.pss.common.PSSDirection;
import net.es.oscars.pss.common.PSSHandlerConfigBean;
import net.es.oscars.pss.eompls.EoMPLSHandler;

public class EoMPLS_Junos implements EoMPLSHandler {
    private PSSHandlerConfigBean config;

    public void setup(Reservation resv, Path localPath,
            PSSDirection direction) {
        // TODO Auto-generated method stub
    }

    public void status(Reservation resv, Path localPath,
            PSSDirection direction) {
        // TODO Auto-generated method stub
    }

    public void teardown(Reservation resv, Path localPath,
            PSSDirection direction) {
        // TODO Auto-generated method stub
    }

    public void setConfig(PSSHandlerConfigBean config) {
        this.config = config;
    }

    public PSSHandlerConfigBean getConfig() {
        return config;
    }

}
