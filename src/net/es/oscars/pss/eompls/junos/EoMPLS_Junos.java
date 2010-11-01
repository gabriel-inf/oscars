package net.es.oscars.pss.eompls.junos;


import org.apache.log4j.Logger;
import org.jdom.Document;

import net.es.oscars.bss.Reservation;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.common.PSSAction;
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
        
        PSSConfigProvider pc = PSSConfigProvider.getInstance();
        boolean checkStatus = pc.getHandlerConfig().isCheckStatusAfterSetup();
        if (checkStatus) {
            try { 
                log.debug("waiting 10 sec before first status check");
                Thread.sleep(10000);
                log.debug("starting status check");
            } catch (InterruptedException e) {
                // nothing
            }

            String statusCmd = cg.generateL2Status(resv, direction);
            boolean doneChecking = false;
            int tries = 0;
            boolean setupSuccess = false;
            while (!doneChecking) {
                tries++;
                Document statusDoc = JunoscriptHandler.command(resv, direction, statusCmd, log);
                setupSuccess = cg.checkStatus(statusDoc, PSSAction.SETUP, direction, resv);
                if (tries > 3) {
                    doneChecking = true;
                } else if (setupSuccess) {
                    doneChecking = true;
                } else {
                    try { 
                        Thread.sleep(30000);
                    } catch (InterruptedException e) {
                        // nothing
                    }
                }
            }
            if (!setupSuccess) {
                throw new PSSException("could not set up");
            }
        }
        log.info("setup.finish");

    }

    public void teardown(Reservation resv, PSSDirection direction) throws PSSException {
        log.info("teardown.start");
        
        EoMPLSJunosConfigGen cg = EoMPLSJunosConfigGen.getInstance();
        String command = cg.generateL2Teardown(resv, direction);
        JunoscriptHandler.command(resv, direction, command, log);
        PSSConfigProvider pc = PSSConfigProvider.getInstance();
        boolean checkStatus = pc.getHandlerConfig().isCheckStatusAfterTeardown();
        if (checkStatus) {
            try { 
                log.debug("waiting 10 sec before first status check");
                Thread.sleep(10000);
                log.debug("starting status check");
            } catch (InterruptedException e) {
                // nothing
            }
            String statusCmd = cg.generateL2Status(resv, direction);
            boolean doneChecking = false;
            int tries = 0;
            boolean teardownSuccess = false;
            while (!doneChecking) {
                tries++;
                Document statusDoc = JunoscriptHandler.command(resv, direction, statusCmd, log);
                teardownSuccess = cg.checkStatus(statusDoc, PSSAction.TEARDOWN, direction, resv);
                if (tries > 3) {
                    doneChecking = true;
                } else if (teardownSuccess) {
                    doneChecking = true;
                } else{
                    try { 
                        Thread.sleep(30000);
                    } catch (InterruptedException e) {
                        // nothing
                    }
                }
            }
            if (!teardownSuccess) {
                throw new PSSException("could not tear down");
            }
        }        
        
        log.info("teardown.finish");
    }
    
    
}
