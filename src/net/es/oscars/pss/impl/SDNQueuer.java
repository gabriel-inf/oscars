package net.es.oscars.pss.impl;

import java.util.HashMap;
import java.util.List;

import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.common.PSSAction;
import net.es.oscars.pss.common.PSSActionStatus;
import net.es.oscars.pss.common.PSSDirection;
import net.es.oscars.pss.common.PSSStatus;

public class SDNQueuer {
    public final static Integer staleAfter = 600;
    
    private class ResvDirection {
        private String gri;
        private PSSDirection dir;
        public void setGri(String gri) {
            this.gri = gri;
        }
        public String getGri() {
            return gri;
        }
        public void setDir(PSSDirection dir) {
            this.dir = dir;
        }
        public PSSDirection getDir() {
            return dir;
        }
        public boolean equals(ResvDirection resvDir) {
            if (resvDir == null) return false;
            if (gri != null && dir != null && resvDir.getGri() != null && resvDir.getDir() != null) {
                return (resvDir.getGri().equals(gri) && resvDir.getDir().equals(dir));
            } else if (gri == null && resvDir.getGri() == null) {
                if (dir == null && resvDir.getDir() == null) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }
    
    
    private HashMap<ResvDirection, PSSActionStatus> setupStatuses;
    private HashMap<ResvDirection, PSSActionStatus> teardownStatuses;
    
    public PSSActionStatus getActionStatus(String gri, PSSDirection direction, PSSAction action) throws PSSException {
        Long nowL = System.currentTimeMillis() / 1000;
        Integer now = nowL.intValue();
        ResvDirection resvDir = new ResvDirection();
        resvDir.setDir(direction);
        resvDir.setGri(gri);
        PSSActionStatus curActionStatus;
        if (action.equals(PSSAction.SETUP)) {
            curActionStatus = setupStatuses.get(resvDir);
        } else {
            curActionStatus = teardownStatuses.get(resvDir);
        }
        if (curActionStatus == null) {
            PSSActionStatus unstarted = new PSSActionStatus();
            unstarted.setStatus(PSSStatus.UNSTARTED);
            unstarted.setMessage("");
            unstarted.setTimestamp(now);
            return unstarted;
        }
        return curActionStatus;
        
    }
    
    public synchronized void startAction(String gri, List<PSSDirection> directions, PSSAction action) throws PSSException {
        if (directions.isEmpty()) {
            throw new PSSException("no directions set");
        }
        Long nowL = System.currentTimeMillis() / 1000;
        Integer now = nowL.intValue();
        boolean ok = true;
        String error = "";
        for (PSSDirection dir : directions) {
            ResvDirection resvDir = new ResvDirection();
            resvDir.setDir(dir);
            resvDir.setGri(gri);
            PSSActionStatus setupActionStatus = setupStatuses.get(resvDir);
            PSSActionStatus tdActionStatus = teardownStatuses.get(resvDir);
            
            if (setupActionStatus == null) {
                PSSActionStatus unstarted = new PSSActionStatus();
                unstarted.setStatus(PSSStatus.UNSTARTED);
                unstarted.setMessage("");
                unstarted.setTimestamp(now);
                setupActionStatus = unstarted;
            }
            if (tdActionStatus == null) {
                PSSActionStatus unstarted = new PSSActionStatus();
                unstarted.setStatus(PSSStatus.UNSTARTED);
                unstarted.setMessage("");
                unstarted.setTimestamp(now);
                tdActionStatus = unstarted;
            }
            PSSStatus setupStatus = setupActionStatus.getStatus();
            PSSStatus tdStatus = tdActionStatus.getStatus();
            
            if (setupStatus.equals(PSSStatus.INPROGRESS)) {
                ok = false;
                if (setupActionStatus.getTimestamp() < now - staleAfter) {
                    error = gri+" "+dir+" is stuck INPROGRESS for setup";
                } else {
                    error = gri+" "+dir+" cannot "+action+" since it already is in setup";
                }
            } else if (tdStatus.equals(PSSStatus.INPROGRESS)) {
                ok = false;
                if (tdActionStatus.getTimestamp() < now - staleAfter) {
                    error = gri+" "+dir+" is stuck INPROGRESS for teardown";
                } else {
                    error = gri+" "+dir+" cannot "+action+" since it already is in teardown";
                }
            }
        }
        if (ok) {
            for (PSSDirection dir : directions) {
                ResvDirection resvDir = new ResvDirection();
                resvDir.setDir(dir);
                resvDir.setGri(gri);
                PSSActionStatus inprogress = new PSSActionStatus();
                inprogress.setStatus(PSSStatus.INPROGRESS);
                inprogress.setMessage("");
                inprogress.setTimestamp(now);
                if (action.equals(PSSAction.SETUP)) {
                    setupStatuses.put(resvDir, inprogress);
                } else {
                    teardownStatuses.put(resvDir, inprogress);
                }
            }
        } else {
            throw new PSSException(error);
        }
    }
    public synchronized void completeAction(String gri, PSSDirection direction, PSSAction action, 
                                            boolean success, String error) throws PSSException {
        Long nowL = System.currentTimeMillis() / 1000;
        Integer now = nowL.intValue();
        ResvDirection resvDir = new ResvDirection();
        resvDir.setDir(direction);
        resvDir.setGri(gri);
        PSSActionStatus curActionStatus;
        if (action.equals(PSSAction.SETUP)) {
            curActionStatus = setupStatuses.get(resvDir);
        } else {
            curActionStatus = teardownStatuses.get(resvDir);
        }
        if (curActionStatus == null) {
            throw new PSSException("expected INPROGRESS current status, but no action status set");
        }
        
        PSSStatus curStatus = curActionStatus.getStatus();
        if (curStatus == null) {
            throw new PSSException("expected INPROGRESS current status, got null");
        } else if (!curStatus.equals(PSSStatus.INPROGRESS)) {
            throw new PSSException("expected INPROGRESS current status, got "+curStatus);
        }
        // if we're here we can set it to completed, either success or failure
        
        PSSActionStatus as = new PSSActionStatus();
        as.setMessage(error);
        as.setTimestamp(now);
        if (success) {
            as.setStatus(PSSStatus.SUCCESS);
        } else {
            as.setStatus(PSSStatus.FAILURE);
        }
        if (action.equals(PSSAction.SETUP)) {
            setupStatuses.put(resvDir, as);
        } else {
            teardownStatuses.put(resvDir, as);
        }
        
    }
    
    
    public static SDNQueuer getInstance() {
        if (instance == null) {
            instance = new SDNQueuer();
        }
        return instance;
    }
    private static SDNQueuer instance;
    
    private SDNQueuer() {
        setupStatuses = new HashMap<ResvDirection, PSSActionStatus>();
        teardownStatuses = new HashMap<ResvDirection, PSSActionStatus>();
    }

}
