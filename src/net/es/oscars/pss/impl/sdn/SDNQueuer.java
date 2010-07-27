package net.es.oscars.pss.impl.sdn;

import java.util.ArrayList;
import java.util.List;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.testng.log4testng.Logger;

import net.es.oscars.bss.OSCARSCore;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.topology.Node;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.common.PSSAction;
import net.es.oscars.pss.common.PSSActionStatus;
import net.es.oscars.pss.common.PSSActionStatusHolder;
import net.es.oscars.pss.common.PSSActionWatcher;
import net.es.oscars.pss.common.PSSDirection;
import net.es.oscars.pss.common.PSSHandler;
import net.es.oscars.pss.common.PSSQueuer;
import net.es.oscars.pss.common.PSSStatus;
import net.es.oscars.pss.common.PathUtils;

public class SDNQueuer implements PSSQueuer {
    public final static Integer staleAfter = 600;
    private Logger log = Logger.getLogger(SDNQueuer.class);
    private Scheduler scheduler = null;

    public PSSActionStatus getActionStatus(String gri, PSSDirection direction, PSSAction action) throws PSSException {
        PSSActionStatusHolder ah = PSSActionStatusHolder.getInstance();
        Long nowL = System.currentTimeMillis() / 1000;
        Integer now = nowL.intValue();
        PSSActionStatus curActionStatus = ah.getDirectionActionStatus(gri, direction, action);
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
        for (PSSDirection direction : directions) {
            PSSActionStatus setupActionStatus   = ah.getDirectionActionStatus(gri, direction, PSSAction.SETUP);
            PSSActionStatus tdActionStatus      = ah.getDirectionActionStatus(gri, direction, PSSAction.TEARDOWN);
            
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
                    error = gri+" "+direction+" is stuck INPROGRESS for setup";
                } else {
                    error = gri+" "+direction+" cannot "+action+" since it already is in setup";
                }
            } else if (tdStatus.equals(PSSStatus.INPROGRESS)) {
                ok = false;
                if (tdActionStatus.getTimestamp() < now - staleAfter) {
                    error = gri+" "+direction+" is stuck INPROGRESS for teardown";
                } else {
                    error = gri+" "+direction+" cannot "+action+" since it already is in teardown";
                }
            }
        }
        if (ok) {
            for (PSSDirection direction : directions) {
                System.out.println(gri+" set to INPROGRESS for "+direction+" "+action);
                PSSActionStatus inprogress = new PSSActionStatus();
                inprogress.setStatus(PSSStatus.INPROGRESS);
                inprogress.setMessage("");
                inprogress.setTimestamp(now);
                ah.setDirectionActionStatus(gri, direction, action, inprogress);
            }
        } else {
            throw new PSSException(error);
        }
    }
    /**
     * 
     * @param gri
     * @param direction
     * @param action
     * @param success
     * @param error
     * @throws PSSException
     */
    public synchronized void completeAction(String gri, PSSDirection direction, PSSAction action, 
                                            boolean success, String error) throws PSSException {
        PSSActionStatusHolder ah = PSSActionStatusHolder.getInstance();
        Long nowL = System.currentTimeMillis() / 1000;
        Integer now = nowL.intValue();
        System.out.println("completing "+action+" for "+gri+" "+direction);
        PSSActionStatus curActionStatus = ah.getDirectionActionStatus(gri, direction, action);

        if (curActionStatus == null) {
            throw new PSSException("expected INPROGRESS current status, but no action status set");
        }
        
        PSSStatus curStatus = curActionStatus.getStatus();
        if (!curStatus.equals(PSSStatus.INPROGRESS)) {
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
        ah.setDirectionActionStatus(gri, direction, action, as);

    }

    
    public synchronized void scheduleAction(Reservation resv, PSSDirection direction, PSSAction action, PSSHandler handler) throws PSSException {
        Scheduler scheduler = null;

        List<PSSDirection> directions = new ArrayList<PSSDirection>();
        if (direction.equals(PSSDirection.BIDIRECTIONAL)) {
            directions.add(direction);
        } else {
            directions.add(PSSDirection.A_TO_Z);
            directions.add(PSSDirection.Z_TO_A);
        }
        
        
        String gri = resv.getGlobalReservationId();
        if (scheduler == null) {
            OSCARSCore core = OSCARSCore.getInstance();
            scheduler = core.getScheduleManager().getScheduler();
        }
        
        Node node = PathUtils.getNodeToConfigure(resv, direction);
        String nodeId = node.getTopologyIdent();
        
        String jobName = "contact-"+nodeId+"-"+gri;
        JobDetail jobDetail = new JobDetail(jobName, "SERIALIZE_NODECONFIG_"+nodeId, ContactNodeJob.class);
        log.debug("Adding job "+jobName);

        System.out.println("Adding job "+jobName);
        jobDetail.setDurability(true);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("resv", resv);
        jobDataMap.put("direction", direction);
        jobDataMap.put("action", action);
        jobDataMap.put("handler", handler);
        jobDetail.setJobDataMap(jobDataMap);
        
        try {
            scheduler.addJob(jobDetail, false);
            PSSActionWatcher.getInstance().watch(resv.getGlobalReservationId(), action, directions);
        } catch (SchedulerException e) {
            log.error(e);
            throw new PSSException(e.getMessage());
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

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
        PSSActionWatcher.getInstance().setScheduler(scheduler);
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

}
