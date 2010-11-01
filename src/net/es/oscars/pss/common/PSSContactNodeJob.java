package net.es.oscars.pss.common;

import java.util.List;

import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.OSCARSCore;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.ReservationDAO;
import net.es.oscars.bss.StateEngine;
import net.es.oscars.bss.topology.Path;
import net.es.oscars.bss.topology.PathElem;
import net.es.oscars.bss.topology.PathType;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.impl.sdn.SDNQueuer;
import net.es.oscars.scheduler.ChainingJob;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class PSSContactNodeJob extends ChainingJob  implements Job{
    private Logger log;

    public void execute(JobExecutionContext context) throws JobExecutionException {
        this.log = Logger.getLogger(this.getClass());
        this.log.debug("ContactNodeJob.start");
        
        if (context == null) {
            log.error("No context!");
            return;
        } else if (context.getJobDetail() == null) {
            log.error("No job detail!");
            return;
        } else if (context.getJobDetail().getJobDataMap() == null) {
            log.error("No job data map!");
            return;
        }
        
        String jobName = context.getJobDetail().getFullName();
        this.log.debug("ContactNodeJob jobName: "+jobName);
        
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();

        Reservation resv        = (Reservation) dataMap.get("resv");
        PSSDirection direction  = (PSSDirection) dataMap.get("direction");
        PSSAction action        = (PSSAction) dataMap.get("action");
        PSSHandler handler      = (PSSHandler) dataMap.get("handler");
        if (resv == null) {
            log.error("No resv!");
            return;
        } else if (direction == null) {
            log.error("No direction!");
            return;
        } else if (action == null) {
            log.error("No action!");
            return;
        } else if (handler == null) {
            log.error("No handler!");
            return;
        }
        
        try {
            SDNQueuer q = SDNQueuer.getInstance();
            OSCARSCore core = OSCARSCore.getInstance();
            core.getBssDbName();
            core.getBssSession();
            String bssDbName = core.getBssDbName();
            StateEngine se = null;
            Session bss = null;

            bss = core.getBssSession();
            se = core.getStateEngine();
            bss.beginTransaction();
            ReservationDAO resvDAO = new ReservationDAO(bssDbName);
            
            String gri = resv.getGlobalReservationId();
            log.debug("gri; "+gri);
            
            Path localPath = resv.getPath(PathType.LOCAL);
            
            List<PathElem> resvPathElems = localPath.getPathElems();
            for (PathElem pe : resvPathElems) {
                String fqti = pe.getLink().getFQTI();
                log.debug(fqti);
            }
    
            

            try {
                resv = resvDAO.query(gri);
            } catch (BSSException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            
            try {
                
                if (action.equals(PSSAction.SETUP)) {
                    log.debug("starting "+gri+" : "+direction+" : "+action);
                    handler.setup(resv, direction);
                    log.debug("completing "+gri+" : "+direction+" : "+action);
                    q.completeAction(gri, direction, action, true, "");
                    log.debug("completed "+gri+" : "+direction+" : "+action);
                } else if (action.equals(PSSAction.TEARDOWN)) {
                    log.debug("starting "+gri+" : "+direction+" : "+action);
                    handler.teardown(resv, direction);
                    log.debug("completing "+gri+" : "+direction+" : "+action);
                    q.completeAction(gri, direction, action, true, "");
                    log.debug("completed "+gri+" : "+direction+" : "+action);
                } else {
                    log.error("invalid action: "+action);
                    // oops, never happen
                }
                
            } catch (PSSException e) {
                try {
                    log.debug("error at: "+gri+" : "+direction+" : "+action);
                    q.completeAction(gri, direction, action, false, e.getMessage());
                    log.debug("handled error at: "+gri+" : "+direction+" : "+action);
                } catch (PSSException ex) {
                    log.error(ex);
                }
            }
            se.safeHibernateCommit(resv, bss);
        } catch (Exception ex) {
            
            log.error("error: ", ex);
            
        } finally {
            //this delays the queue a domain-specific amount
            try { 
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                
            }
            this.runNextJob(context);
        }

    }

}
