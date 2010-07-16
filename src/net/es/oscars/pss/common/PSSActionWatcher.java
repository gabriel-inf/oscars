package net.es.oscars.pss.common;

import java.util.HashMap;
import java.util.List;

import net.es.oscars.pss.PSSException;

public class PSSActionWatcher {
    private boolean watching = false;

    private HashMap<String, PSSActionDirections> watchList = new HashMap<String, PSSActionDirections>();
    
    public synchronized void watch(String gri, PSSAction action, List<PSSDirection> directions) throws PSSException {
        if (this.watchList.containsKey(gri)) {
            throw new PSSException("was already watching "+gri);
        }
        PSSActionDirections ad = new PSSActionDirections();
        ad.setAction(action);
        ad.setDirections(directions);
        watchList.put(gri, ad);
        if (!watching) {
            this.startWatchJob();
            watching = true;
        }
    }
    public synchronized void unWatch(String gri) {
        this.watchList.remove(gri);
    }
    
    
    private void startWatchJob() {
        // start a thread that will watch the watchList from here and 
    }
    
    private static PSSActionWatcher instance;
    private PSSActionWatcher() {
        
    }
    public static PSSActionWatcher getInstance() {
        if (instance == null) {
            instance = new PSSActionWatcher();
        }
        return instance;
    }
    public HashMap<String, PSSActionDirections> getWatchList() {
        return watchList;
    }
}
