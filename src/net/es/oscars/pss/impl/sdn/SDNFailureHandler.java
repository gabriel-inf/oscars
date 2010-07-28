package net.es.oscars.pss.impl.sdn;

import net.es.oscars.bss.Reservation;
import net.es.oscars.pss.common.PSSAction;
import net.es.oscars.pss.common.PSSDirection;
import net.es.oscars.pss.common.PSSFailureHandler;

public class SDNFailureHandler implements PSSFailureHandler {

    public void handleFailure(Reservation resv, PSSAction action) {
        
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
