package net.es.oscars.nsibridge.bridge;

import java.util.Collection;
import java.util.HashMap;

public class NSAFactory {
    private HashMap<String, NSAAPI> bridges;
    private static NSAFactory instance;
    private NSAFactory() { 
        bridges = new HashMap<String, NSAAPI>();
    }
    
    public static NSAFactory getInstance() {
        if (instance == null) {
            instance = new NSAFactory();
        }
        return instance;
    }
    
    public void addNSA(String nsaId, NSAAPI bridge) {
        bridges.put(nsaId, bridge);
    }
    public Collection<NSAAPI> getNSAs() {
        return bridges.values();
    }
    
    public NSAAPI getNSA(String nsaId) {
        return bridges.get(nsaId);
    }
    
}
