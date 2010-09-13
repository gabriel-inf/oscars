package net.es.oscars.pss.eompls.junos;


import org.apache.log4j.Logger;

import net.es.oscars.bss.Reservation;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.common.PSSConfigProvider;
import net.es.oscars.pss.common.PSSHandler;
import net.es.oscars.pss.common.PSSDirection;
import net.es.oscars.pss.common.PSSHandlerConfigBean;
import net.es.oscars.pss.connect.JunoscriptHandler;

public class EoMPLS_Junos implements PSSHandler {
    private Logger log = Logger.getLogger(EoMPLS_Junos.class);
    private static EoMPLS_Junos instance;

    public static EoMPLS_Junos getInstance() {
        if (instance == null) {
            instance = new EoMPLS_Junos();
        }
        return instance;
    }

    private EoMPLS_Junos() {
        EoMPLSJunosConfigGen cg = EoMPLSJunosConfigGen.getInstance();
        PSSConfigProvider pc = PSSConfigProvider.getInstance();
        PSSHandlerConfigBean hc = pc.getHandlerConfig();
        cg.setTemplateDir(hc.getTemplateDir());
    }
    
    public void setup(Reservation resv, PSSDirection direction) throws PSSException {
        log.info("setup.start");
        
        EoMPLSJunosConfigGen cg = EoMPLSJunosConfigGen.getInstance();
        String command = cg.generateL2Setup(resv, direction);
        JunoscriptHandler.command(resv, direction, command, log);
        
        log.info("setup.finish");

    }

    public void teardown(Reservation resv, PSSDirection direction) throws PSSException {
        log.info("teardown.start");
        
        EoMPLSJunosConfigGen cg = EoMPLSJunosConfigGen.getInstance();
        String command = cg.generateL2Teardown(resv, direction);
        JunoscriptHandler.command(resv, direction, command, log);
        
        
        log.info("teardown.finish");
    }

}
