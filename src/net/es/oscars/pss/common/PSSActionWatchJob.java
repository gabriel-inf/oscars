package net.es.oscars.pss.common;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.OSCARSCore;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.StateEngine;
import net.es.oscars.bss.events.EventProducer;
import net.es.oscars.bss.events.OSCARSEvent;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.PSSFailureManager;
import net.es.oscars.pss.PathSetupManager;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;


public class PSSActionWatchJob implements Job {
    private Logger log = Logger.getLogger(PSSActionWatchJob.class);
    
    // change these thru JobDataMap
    private Integer staleTimeout = 300;
    private Boolean notify = false;
    
    public void execute(JobExecutionContext context) throws JobExecutionException {
        PSSActionWatcher aw = PSSActionWatcher.getInstance();
        ConcurrentHashMap<Reservation, PSSActionDirections> watchList = aw.getWatchList();
        PSSActionStatusHolder ah = PSSActionStatusHolder.getInstance();
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        if (dataMap.get("staleTimeout") != null) {
            staleTimeout = (Integer) dataMap.get("staleTimeout");
        }
        if (dataMap.get("notify") != null) {
            notify = (Boolean) dataMap.get("notify");
        }
        for (Reservation resv: watchList.keySet()) {
            PSSActionDirections ads = watchList.get(resv);
            String gri = resv.getGlobalReservationId();
            PSSAction action = ads.getAction();
            List<PSSDirection> directions = ads.getDirections();
            
            PSSStatus aToZStatus = null;
            PSSStatus zToAStatus = null;
            for (PSSDirection direction : directions) {
                PSSActionStatus as;
                try {
                    as = ah.getDirectionActionStatus(gri, direction, action);
                } catch (PSSException e) {
                    log.error(e);
                    throw new JobExecutionException(e);
                }
                String errMsg = as.getMessage();
                PSSStatus status = as.getStatus();
                log.debug("examining: "+gri+" "+direction+" "+status);
                
                if (direction.equals(PSSDirection.BIDIRECTIONAL)) {
                    if (status.equals(PSSStatus.FAILURE)) {
                        this.handleFailure(resv, action, directions, errMsg, false);
                    } else if (status.equals(PSSStatus.SUCCESS)) {
                        this.handleSuccess(resv, action, directions);
                    } else if (status.equals(PSSStatus.INPROGRESS)) {
                        if (!this.canWait(as)) { 
                            this.handleFailure(resv, action, directions, errMsg, true);
                        }
                    } else if (status.equals(PSSStatus.UNSTARTED)) {
                        if (!this.canWait(as)) { 
                            this.handleFailure(resv, action, directions, errMsg, true);
                        }
                    } 
                } else {
                    // get statuses for both directions
                    if (direction.equals(PSSDirection.A_TO_Z)) {
                        aToZStatus = status;
                    } else if (direction.equals(PSSDirection.Z_TO_A)) {
                        zToAStatus = status;
                    }
                    
                    
                    /*
                     * case diagram for both directions
                     * 
                     * axes:
                     * U = UNSTARTED
                     * I = INPROGRESS
                     * F = FAILURE
                     * S = SUCCESS
                     * 
                     * table contents
                     * W = WAIT (or FAIL if stale) 
                     * F = IMMEDIATE FAILURE
                     * S = IMMEDIATE SUCCESS
                     * 
                     * 
                     *   | U | I | F | S | 
                     * --+---+---+---+---+
                     * U | W | W | W | W |
                     * --+---+---+---+---+
                     * I | W | W | W | W |
                     * --+---+---+---+---+
                     * F | W | W | F | F |
                     * --+---+---+---+---+
                     * S | W | W | F | S |
                     * --+---+---+---+---+
                     */
                    
                    // go over the various cases
                    if (aToZStatus != null && zToAStatus != null) {
                        // if in failure, wait a bit for the other action to  start and finish

                        if (aToZStatus.equals(PSSStatus.FAILURE)) {
                            if (zToAStatus.equals(PSSStatus.FAILURE)) {
                                // two-sides failure
                                this.handleFailure(resv, action, directions, errMsg, false);
                            } else if (zToAStatus.equals(PSSStatus.SUCCESS)) {
                                // one-side failure
                                this.handleFailure(resv, action, directions, errMsg, false);
                            } else if (zToAStatus.equals(PSSStatus.INPROGRESS)) {
                                if (!this.canWait(as)) { 
                                    this.handleFailure(resv, action, directions, errMsg, true);
                                }
                            } else if (zToAStatus.equals(PSSStatus.UNSTARTED)) {
                                if (!this.canWait(as)) { 
                                    this.handleFailure(resv, action, directions, errMsg, true);
                                }
                            } 
                        } else if (aToZStatus.equals(PSSStatus.SUCCESS)) {
                            if (zToAStatus.equals(PSSStatus.FAILURE)) {
                                // one-side failure
                                this.handleFailure(resv, action, directions, errMsg, false);
                            } else if (zToAStatus.equals(PSSStatus.SUCCESS)) {
                                // success!
                                this.handleSuccess(resv, action, directions);
                            } else if (zToAStatus.equals(PSSStatus.INPROGRESS)) {
                                if (!this.canWait(as)) { 
                                    this.handleFailure(resv, action, directions, errMsg, true);
                                }
                            } else if (zToAStatus.equals(PSSStatus.UNSTARTED)) {
                                if (!this.canWait(as)) { 
                                    this.handleFailure(resv, action, directions, errMsg, true);
                                }
                            } 

                        // if in progress, do nothing & wait for things to complete. 
                        // if status never changes then it's a stale action
                        } else if (aToZStatus.equals(PSSStatus.INPROGRESS)) {
                            if (!this.canWait(as)) {
                                this.handleFailure(resv, action, directions, errMsg, true);
                            }
                        // if unstarted, do nothing & wait for things to get started. 
                        // if status never changes then it's a stale action
                        } else if (aToZStatus.equals(PSSStatus.UNSTARTED)) {
                            if (!this.canWait(as)) { 
                                this.handleFailure(resv, action, directions, errMsg, true);
                            }
                        } 
                    }
                }
            }
        }
        aw.stopIfUnneeded();
    }
    

    
    private void handleFailure(Reservation resv, PSSAction action, List<PSSDirection> directions, String errMsg, boolean stale) {
        PSSActionWatcher aw = PSSActionWatcher.getInstance();
        String gri = resv.getGlobalReservationId();
        
        String dirStr = "";
        for (PSSDirection direction : directions) {
            dirStr =  dirStr + direction+ " ";
        }

        String errorMessage = "FAILURE: "+gri+" "+action+" "+dirStr+" ["+errMsg+"]";
        if (stale) {
            errorMessage += " (stale)";
        }
        
        log.error(errorMessage);
        
        if (notify) {
            EventProducer eventProducer = new EventProducer();
            StateEngine stateEngine = OSCARSCore.getInstance().getStateEngine();
            try {
                stateEngine.updateStatus(resv, StateEngine.FAILED);
                if (action.equals(PSSAction.SETUP)) {
                    eventProducer.addEvent(OSCARSEvent.PATH_SETUP_FAILED, "", "JOB", resv, "", errorMessage);
                } else if (action.equals(PSSAction.TEARDOWN)) {
                    eventProducer.addEvent(OSCARSEvent.PATH_TEARDOWN_FAILED, "", "JOB", resv, "", errorMessage);
                } else {
                    log.error("invalid action: "+action);
                    return;
                }
            } catch (BSSException e) {
                log.error(e);
            }
        }
        

        aw.unwatch(resv);
        PSSFailureHandler fh = PSSFailureManager.getInstance().getFailureHandler();
        if (fh != null) {
            fh.handleFailure(resv, action);
        } else {
            log.info("No PSS failure handler");
        }
    }
    
    private void handleSuccess(Reservation resv, PSSAction action, List<PSSDirection> directions) {
        String dirStr = "";
        for (PSSDirection direction : directions) {
            dirStr = direction + dirStr+ " ";
        }

        String gri = resv.getGlobalReservationId();
        log.info("SUCCESS: "+gri+" "+action+" "+dirStr);

        if (notify) {
            PathSetupManager pe = OSCARSCore.getInstance().getPathSetupManager();
            try {
                if (action.equals(PSSAction.SETUP)) {
                    pe.updateCreateStatus(StateEngine.CONFIRMED, resv);
                } else if (action.equals(PSSAction.TEARDOWN)) {
                    pe.updateTeardownStatus(StateEngine.CONFIRMED, resv);
                } else {
                    log.error("invalid action: "+action);
                    return;
                }
            } catch (BSSException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        PSSActionWatcher aw = PSSActionWatcher.getInstance();
        aw.unwatch(resv);
        
    }
    
    private boolean canWait(PSSActionStatus as) {
        
        Integer ts = as.getTimestamp();
        Long now = new Date().getTime()/1000;
        
        if (now - ts > staleTimeout) {
            return false;
        }
        return true;
    }
    
    

}