package net.es.oscars.pss.impl.sdn;

import org.apache.log4j.Logger;

import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.StateEngine;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.common.PSSAction;
import net.es.oscars.pss.common.PSSConfigProvider;
import net.es.oscars.pss.common.PSSFailureHandler;

public class SDNFailureHandler implements PSSFailureHandler {
    private Logger log = Logger.getLogger(SDNFailureHandler.class);

    public void handleFailure(Reservation resv, PSSAction action) throws PSSException {
        String gri = resv.getGlobalReservationId();

        /* comments below are wrong, if FAILED it can't be changed to INSETUP
         
        StateEngine stateEngine = OSCARSCore.getInstance().getStateEngine();
        if (action.equals(PSSAction.SETUP)) {
            // to conform to the state diagram we need to change the status 
            //.to INSETUP before we start tearing the reservation down 
            try {
                stateEngine.updateStatus(resv, StateEngine.INSETUP);
            } catch (BSSException e) {
                log.error(e);
                throw new PSSException(e.getMessage());
            }
        }
        */

        PSSConfigProvider pc = PSSConfigProvider.getInstance();
        if (pc.getHandlerConfig().isTeardownOnFailure()) {
            log.info(gri+" failure in "+action+" .  Removing configuration");
            if (action.equals(PSSAction.SETUP)) {
                SDNPSS.getInstance().teardownPath(resv, StateEngine.FAILED);

                
            } else if (action.equals(PSSAction.TEARDOWN)) {
                log.info("Action was teardown; must remove config manually");
            }
        } else {
            log.info("no action after failure will be taken");
        }
        
    }
    private static SDNFailureHandler instance;
    public static SDNFailureHandler getInstance() {
        if (instance == null) {
            instance = new SDNFailureHandler();
        }
        return instance;
    }
    private SDNFailureHandler() {
        
    }

}
