package net.es.oscars.pss;

import net.es.oscars.pss.common.PSSFailureHandler;

public class PSSFailureManager {
    private PSSFailureHandler failureHandler;
    private static PSSFailureManager instance;
    
    
    public static PSSFailureManager getInstance() {
        if (instance == null) {
            instance = new PSSFailureManager();
        }
        return instance;
            
    }
    
    private PSSFailureManager() {
        
    }

    public void setFailureHandler(PSSFailureHandler failureHandler) {
        this.failureHandler = failureHandler;
    }

    public PSSFailureHandler getFailureHandler() {
        return failureHandler;
    }
    // TODO make this configurable
}
