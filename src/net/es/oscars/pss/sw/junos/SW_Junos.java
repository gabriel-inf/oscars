package net.es.oscars.pss.sw.junos;

import org.apache.log4j.Logger;

import net.es.oscars.bss.Reservation;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.common.PSSHandler;
import net.es.oscars.pss.common.PSSDirection;
import net.es.oscars.pss.connect.JunoscriptHandler;

public class SW_Junos implements PSSHandler {
    private Logger log = Logger.getLogger(SW_Junos.class);
    
    public void setup(Reservation resv, PSSDirection direction) throws PSSException {
        log.info("setup.start");

        SWJunosConfigGen cg = SWJunosConfigGen.getInstance();
        String command = cg.generateL2Setup(resv, direction);

        JunoscriptHandler.command(resv, direction, command, log);
        
        log.info("setup.finish");
    }

    public void teardown(Reservation resv, PSSDirection direction) throws PSSException {
        log.info("teardown.start");

        SWJunosConfigGen cg = SWJunosConfigGen.getInstance();
        String command = cg.generateL2Teardown(resv, direction);

        JunoscriptHandler.command(resv, direction, command, log);
        
        log.info("teardown.finish");
    }


}
