package net.es.oscars.pss.impl;

import java.util.List;

import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.common.PSSAction;
import net.es.oscars.pss.common.PSSActionStatus;
import net.es.oscars.pss.common.PSSActionStatusHolder;
import net.es.oscars.pss.common.PSSDirection;
import net.es.oscars.pss.common.PSSGriDirection;
import net.es.oscars.pss.common.PSSStatus;

public class SDNQueuer {
    public final static Integer staleAfter = 600;
    
    public PSSActionStatus getActionStatus(String gri, PSSDirection direction, PSSAction action) throws PSSException {
        PSSActionStatusHolder ah = PSSActionStatusHolder.getInstance();
        Long nowL = System.currentTimeMillis() / 1000;
        Integer now = nowL.intValue();
        PSSGriDirection griDir = new PSSGriDirection();
        griDir.setDirection(direction);
        griDir.setGri(gri);
        PSSActionStatus curActionStatus;
        if (action.equals(PSSAction.SETUP)) {
            curActionStatus = ah.getSetupStatuses().get(griDir);
        } else {
            curActionStatus = ah.getTeardownStatuses().get(griDir);
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
        PSSActionStatusHolder ah = PSSActionStatusHolder.getInstance();
        Long nowL = System.currentTimeMillis() / 1000;
        Integer now = nowL.intValue();
        boolean ok = true;
        String error = "";
        for (PSSDirection dir : directions) {
            PSSGriDirection griDir = new PSSGriDirection();
            griDir.setDirection(dir);
            griDir.setGri(gri);
            PSSActionStatus setupActionStatus   = ah.getSetupStatuses().get(griDir);
            PSSActionStatus tdActionStatus      = ah.getTeardownStatuses().get(griDir);
            
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
                PSSGriDirection griDir = new PSSGriDirection();
                griDir.setDirection(dir);
                griDir.setGri(gri);
                PSSActionStatus inprogress = new PSSActionStatus();
                inprogress.setStatus(PSSStatus.INPROGRESS);
                inprogress.setMessage("");
                inprogress.setTimestamp(now);
                if (action.equals(PSSAction.SETUP)) {
                    ah.getSetupStatuses().put(griDir, inprogress);
                } else {
                    ah.getTeardownStatuses().put(griDir, inprogress);
                }
            }
        } else {
            throw new PSSException(error);
        }
    }
    public synchronized void completeAction(String gri, PSSDirection direction, PSSAction action, 
                                            boolean success, String error) throws PSSException {
        PSSActionStatusHolder ah = PSSActionStatusHolder.getInstance();
        Long nowL = System.currentTimeMillis() / 1000;
        Integer now = nowL.intValue();
        PSSGriDirection griDir = new PSSGriDirection();
        griDir.setDirection(direction);
        griDir.setGri(gri);
        PSSActionStatus curActionStatus;
        if (action.equals(PSSAction.SETUP)) {
            curActionStatus = ah.getSetupStatuses().get(griDir);
        } else {
            curActionStatus = ah.getTeardownStatuses().get(griDir);
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
            ah.getSetupStatuses().put(griDir, as);
        } else {
            ah.getTeardownStatuses().put(griDir, as);
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
    }

}
