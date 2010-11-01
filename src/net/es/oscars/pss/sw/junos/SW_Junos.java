package net.es.oscars.pss.sw.junos;

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

public class SW_Junos implements PSSHandler {
    private Logger log = Logger.getLogger(SW_Junos.class);
    private static SW_Junos instance;
    public static SW_Junos getInstance() {
        if (instance == null) {
            instance = new SW_Junos();
        }
        return instance;
    }
    
    private SW_Junos() {
        SWJunosConfigGen cg = SWJunosConfigGen.getInstance();

        PSSConfigProvider pc = PSSConfigProvider.getInstance();
        PSSHandlerConfigBean hc = pc.getHandlerConfig();
        cg.setTemplateDir(hc.getTemplateDir());

    }

    public void setup(Reservation resv, PSSDirection direction) throws PSSException {
        log.info("setup.start");

        SWJunosConfigGen cg = SWJunosConfigGen.getInstance();
        
        String command = cg.generateL2Setup(resv, direction);

        JunoscriptHandler.command(resv, direction, command, log);
        String gri = resv.getGlobalReservationId();
        PSSConfigProvider pc = PSSConfigProvider.getInstance();
        boolean checkStatus = pc.getHandlerConfig().isCheckStatusAfterSetup();
        Integer maxTries = pc.getHandlerConfig().getCheckStatusMaxTries();
        Integer initialDelay = pc.getHandlerConfig().getCheckStatusInitialDelay();
        Integer delayBetween = pc.getHandlerConfig().getCheckStatusDelayBetween();
        
        
        if (checkStatus) {
            try { 
                log.debug("waiting "+initialDelay+" sec before first status check");
                Thread.sleep(initialDelay * 1000);
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
                log.info("checking setup status for "+gri+" "+direction+" tries: "+tries);
                Document statusDoc = JunoscriptHandler.command(resv, direction, statusCmd, log);
                setupSuccess = this.checkStatus(statusDoc, PSSAction.SETUP, direction, resv);
                if (tries >= maxTries) {
                    doneChecking = true;
                } else if (setupSuccess) {
                    doneChecking = true;
                } else {
                    try { 
                        log.debug("waiting "+delayBetween+" sec before next status check");
                        Thread.sleep(delayBetween*1000);
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
        
        SWJunosConfigGen cg = SWJunosConfigGen.getInstance();
        String command = cg.generateL2Teardown(resv, direction);
        String gri = resv.getGlobalReservationId();

        JunoscriptHandler.command(resv, direction, command, log);
        
        PSSConfigProvider pc = PSSConfigProvider.getInstance();
        boolean checkStatus = pc.getHandlerConfig().isCheckStatusAfterTeardown();
        Integer maxTries = pc.getHandlerConfig().getCheckStatusMaxTries();
        Integer initialDelay = pc.getHandlerConfig().getCheckStatusInitialDelay();
        Integer delayBetween = pc.getHandlerConfig().getCheckStatusDelayBetween();
        if (checkStatus) {
            try { 
                log.debug("waiting "+initialDelay+" sec before first status check");
                Thread.sleep(initialDelay * 1000);
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
                log.info("checking teardown status for "+gri+" "+direction+" tries: "+tries);
                Document statusDoc = JunoscriptHandler.command(resv, direction, statusCmd, log);
                teardownSuccess = this.checkStatus(statusDoc, PSSAction.TEARDOWN, direction, resv);
                if (tries >= maxTries) {
                    doneChecking = true;
                } else if (teardownSuccess) {
                    doneChecking = true;
                } else {
                    try { 
                        log.debug("waiting "+delayBetween+" sec before next status check");
                        Thread.sleep(delayBetween*1000);
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
    
    /**
     * @return true if the status is what it is supposed to be (i.e up for setup, down for teardown)
     */
    private boolean checkStatus(Document statusDoc, PSSAction action, PSSDirection direction, Reservation resv) {
        boolean result = false;
        PSSConfigProvider pc = PSSConfigProvider.getInstance();
        if (pc.getHandlerConfig().isStubMode()) {
            log.debug("stub mode; status is success");
            return true;
        }
        // FIXME: actually check!
        log.error("unimplemented: status is success");
        result = true;
        
        return result;
    }
}
