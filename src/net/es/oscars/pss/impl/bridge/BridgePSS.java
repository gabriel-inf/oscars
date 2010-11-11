package net.es.oscars.pss.impl.bridge;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.OSCARSCore;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.StateEngine;
import net.es.oscars.bss.topology.Path;
import net.es.oscars.bss.topology.PathType;
import net.es.oscars.pss.PSS;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.common.PSSAction;
import net.es.oscars.pss.common.PSSHandler;
import net.es.oscars.pss.common.PSSDirection;
import net.es.oscars.pss.common.PathUtils;


public class BridgePSS implements PSS {
    private static BridgePSS instance = null;
    private Logger log;

    public String refreshPath(Reservation resv) throws PSSException {
        Path path = null;
        try {
            path = resv.getPath(PathType.LOCAL);
            if (path == null) {
                throw new PSSException("No local path set");
            }
            if (path.isLayer3()) {
                throw new PSSException("Bridge PSS does not support L3");
            } else if (path.isLayer2()) {
                throw new PSSException("Refresh not implemented for L2");
            } else {
                throw new PSSException("Local path has neither L2 nor L3 information");
            }
        } catch (BSSException e) {
            this.log.error(e);
            throw new PSSException(e.getMessage());
        }
    }
    
    public String createPath(Reservation resv) throws PSSException {
        try {
            StateEngine.canUpdateStatus(resv, StateEngine.INSETUP);
        } catch (BSSException ex) {
            throw new PSSException(ex);
        }
        this.startAction(resv, PSSAction.SETUP); 
        
        return "";
    }

    public String teardownPath(Reservation resv, String newStatus) throws PSSException {
        if (!newStatus.equals(StateEngine.FAILED)) {
            try {
                StateEngine.canUpdateStatus(resv, StateEngine.INTEARDOWN);
            } catch (BSSException ex) {
                throw new PSSException(ex);
            }
        } else {
           // teardown because of failure
        }
        this.startAction(resv, PSSAction.TEARDOWN); 
        
        return "";
    }

    
    /** 
     * 
     * @param resv
     * @param localPath
     * @param action
     * @throws PSSException
     */
    private void tryStarting(Reservation resv, Path localPath, PSSAction action) throws PSSException  {
        String gri = resv.getGlobalReservationId();
        BridgeQueuer q = BridgeQueuer.getInstance();
        ArrayList<PSSDirection> directions = new ArrayList<PSSDirection>();
        directions.add(PSSDirection.BIDIRECTIONAL);
        // TODO: make these configurable
        int maxTries = 12;
        int sleep = 10;
        int currentTries = 0;
        boolean ok = false;
        while (currentTries < maxTries && !ok) {
            try {
                currentTries++;
                q.startAction(gri, directions, action);
                ok = true;
            } catch (PSSException e) {
                if (currentTries == maxTries) {
                    log.error(e);
                    throw new PSSException("could not start "+action+" for "+gri+" after "+currentTries+" tries"+e.getMessage());
                } else {
                    // wait a bit then try again
                    log.info(e);
                    try {
                        Long sleeptime = sleep*1000L;
                        Thread.sleep(sleeptime);
                    } catch (InterruptedException e1) {
                        log.error(e1);
                        throw new PSSException(e1.getMessage());
                    }
                }
                
            }
        }
    }
    

    /**
     * starts an action (either SETUP or TEARDOWN) for a reservation
     * will split up into sub-actions per direction
     * uses SDNQueuer to schedule the actions
     * 
     * @param resv
     * @param action
     * @throws PSSException
     */
    public void startAction(Reservation resv, PSSAction action) throws PSSException {

        Path localPath  = PathUtils.getLocalPath(resv);
        
        try {
            if (localPath.isLayer2()) {
                // nada
            } else if (localPath.isLayer3()) {
                throw new PSSException("L3 not supported");
            } else {
                throw new PSSException("Local path has neither L2 nor L3 information");
            }
        } catch (BSSException e) {
            throw new PSSException(e.getMessage());
        }
        
        BridgeQueuer q = BridgeQueuer.getInstance();
        
        // ensure another setup or teardown are not in progress
        // for the exact same reservation 
        /// should never happen because PathSetupManager enforces waiting
        this.tryStarting(resv, localPath, action);
        
        
        
        PSSHandler handler = BridgeHandler.getInstance();
        q.scheduleAction(resv, PSSDirection.BIDIRECTIONAL, action, handler);

        
    }
    
    
    
    /**
     * a singleton
     */
    private BridgePSS() throws PSSException {
        this.log = Logger.getLogger(this.getClass());
        
        log.debug("Setting up Bridge PSS");
        
        BridgeQueuer q = BridgeQueuer.getInstance();
        q.setScheduler(OSCARSCore.getInstance().getScheduleManager().getScheduler());

        log.debug("Bridge PSS setup complete");

        
    }




    /**
     * singleton constructor
     * @return the instance
     */
    public static BridgePSS getInstance() throws PSSException {
        if (instance == null) {
            instance = new BridgePSS();
        }
        return instance;
    }


    

}
