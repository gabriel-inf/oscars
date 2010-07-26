package net.es.oscars.pss.eompls.junos;


import org.testng.log4testng.Logger;

import net.es.oscars.bss.OSCARSCore;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.topology.Node;
import net.es.oscars.bss.topology.Path;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.common.PSSConfigProvider;
import net.es.oscars.pss.common.PSSHandler;
import net.es.oscars.pss.common.PSSDirection;
import net.es.oscars.pss.common.PSSHandlerConfigBean;
import net.es.oscars.pss.common.PathUtils;

public class EoMPLS_Junos implements PSSHandler {
    private Logger log = Logger.getLogger(EoMPLS_Junos.class);
    
    
    public void setup(Reservation resv, PSSDirection direction) throws PSSException {
        Path localPath = PathUtils.getLocalPath(resv);
        
        PSSConfigProvider pc = PSSConfigProvider.getInstance();

        System.out.println("starting setup for: "+resv.getGlobalReservationId());
        

    }

    public void teardown(Reservation resv, PSSDirection direction) throws PSSException {
        System.out.println("starting teardown for: "+resv.getGlobalReservationId());
    }


}
