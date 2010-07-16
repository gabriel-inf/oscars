package net.es.oscars.pss.common;

import java.util.HashMap;

public class PSSActionStatusHolder {
    private HashMap<PSSGriDirection, PSSActionStatus> setupStatuses;
    private HashMap<PSSGriDirection, PSSActionStatus> teardownStatuses;
    

    private PSSActionStatusHolder() {
        setupStatuses = new HashMap<PSSGriDirection, PSSActionStatus>();
        teardownStatuses = new HashMap<PSSGriDirection, PSSActionStatus>();
    }
    
    public HashMap<PSSGriDirection, PSSActionStatus> getSetupStatuses() {
        return setupStatuses;
    }
    public HashMap<PSSGriDirection, PSSActionStatus> getTeardownStatuses() {
        return teardownStatuses;
    }
    public static PSSActionStatusHolder getInstance() {
        if (instance == null) {
            instance = new PSSActionStatusHolder();
        }
        return instance;
    }
    
    private static PSSActionStatusHolder instance;
}
