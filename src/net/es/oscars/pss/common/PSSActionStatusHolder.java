package net.es.oscars.pss.common;

import java.util.concurrent.ConcurrentHashMap;

import net.es.oscars.pss.PSSException;

public class PSSActionStatusHolder {
    private ConcurrentHashMap<String, 
                ConcurrentHashMap<PSSDirection, 
                    ConcurrentHashMap<PSSAction, PSSActionStatus>>> byDirStatuses;
    

    public static PSSActionStatusHolder getInstance() {
        if (instance == null) {
            instance = new PSSActionStatusHolder();
        }
        return instance;
    }
    
    private static PSSActionStatusHolder instance;
    
    private PSSActionStatusHolder() {
        byDirStatuses = new ConcurrentHashMap<String, ConcurrentHashMap<PSSDirection, ConcurrentHashMap<PSSAction, PSSActionStatus>>>();
    }
    
    public PSSActionStatus getDirectionActionStatus(String gri, PSSDirection direction, PSSAction action) throws PSSException {
        if (byDirStatuses.containsKey(gri)) {
            ConcurrentHashMap<PSSDirection, ConcurrentHashMap<PSSAction, PSSActionStatus>> ds = byDirStatuses.get(gri);
            if (ds.containsKey(direction)) {
                ConcurrentHashMap<PSSAction, PSSActionStatus> as = ds.get(direction); 
                if (as.containsKey(action)) {
                    return as.get(action);
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
    public synchronized void setDirectionActionStatus(String gri, PSSDirection direction, PSSAction action, PSSActionStatus actionStatus) {
        System.out.println(gri+" "+direction+" "+action+" status set to: "+actionStatus.getStatus());
        ConcurrentHashMap<PSSDirection, ConcurrentHashMap<PSSAction, PSSActionStatus>> ds;
        if (byDirStatuses.containsKey(gri) && byDirStatuses.get(gri) != null) {
            ds = byDirStatuses.get(gri);
        } else {
            ds = new ConcurrentHashMap<PSSDirection, ConcurrentHashMap<PSSAction, PSSActionStatus>>();
            byDirStatuses.put(gri, ds);
        }
        ConcurrentHashMap<PSSAction, PSSActionStatus> as;
        if (ds.containsKey(direction) && ds.get(direction) != null) {
            as = ds.get(direction);
        } else {
            as = new ConcurrentHashMap<PSSAction, PSSActionStatus>();
            ds.put(direction, as);
        }
        as.put(action, actionStatus);
        
        
        
    }
    
}
