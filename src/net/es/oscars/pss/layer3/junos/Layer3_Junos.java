package net.es.oscars.pss.layer3.junos;

import org.apache.log4j.Logger;

import net.es.oscars.bss.Reservation;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.common.PSSHandler;
import net.es.oscars.pss.common.PSSDirection;
import net.es.oscars.pss.connect.JunoscriptHandler;

public class Layer3_Junos implements PSSHandler {
    private Logger log = Logger.getLogger(Layer3_Junos.class);

    public void setup(Reservation resv, PSSDirection direction) throws PSSException {
        log.info("setup.start");
        
        Layer3JunosConfigGen cg = Layer3JunosConfigGen.getInstance();
        
        String command = cg.generateL3Setup(resv, direction);

        JunoscriptHandler.command(resv, direction, command, log);

        log.info("setup.finish");

    }

    public void teardown(Reservation resv, PSSDirection direction) throws PSSException {
        
        log.info("teardown.start");
        Layer3JunosConfigGen cg = Layer3JunosConfigGen.getInstance();
        
        String command = cg.generateL3Teardown(resv, direction);
        
        JunoscriptHandler.command(resv, direction, command, log);
        
        log.info("teardown.finish");
    }




}
