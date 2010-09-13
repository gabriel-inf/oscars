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
        String jobName = context.getJobDetail().getFullName();
        this.log.debug("ContactNodeJob.start name: "+jobName);
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();

        
        
        Reservation resv        = (Reservation) dataMap.get("resv");
        PSSDirection direction  = (PSSDirection) dataMap.get("direction");
        PSSAction action        = (PSSAction) dataMap.get("action");
        PSSHandler handler      = (PSSHandler) dataMap.get("handler");
        
        
        boolean persist = false;
        try {
            SDNQueuer q = SDNQueuer.getInstance();
            
            String gri = resv.getGlobalReservationId();
            
            Path localPath = resv.getPath(PathType.LOCAL);
            
            List<PathElem> resvPathElems = localPath.getPathElems();
            for (PathElem pe : resvPathElems) {
                try {
                    String fqti = pe.getLink().getFQTI();
                    log.debug(fqti);
                } catch (org.hibernate.LazyInitializationException ex) {
                    persist = true;
                }
            }
    
            StateEngine se = null;
            Session bss = null;
            
            if (persist) {
                OSCARSCore core = OSCARSCore.getInstance();
                core.getBssDbName();
                core.getBssSession();
                String bssDbName = core.getBssDbName();
                bss = core.getBssSession();
                se = core.getStateEngine();
                bss.beginTransaction();
                ReservationDAO resvDAO = new ReservationDAO(bssDbName);
                try {
                    resv = resvDAO.query(gri);
                } catch (BSSException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            
            
            try {
                
                if (action.equals(PSSAction.SETUP)) {
                    handler.setup(resv, direction);
                    q.completeAction(gri, direction, action, true, "");
                } else if (action.equals(PSSAction.TEARDOWN)) {
                    handler.teardown(resv, direction);
                    q.completeAction(gri, direction, action, true, "");
                } else {
                    // oops, never happen
                }
                
            } catch (PSSException e) {
                try {
                    q.completeAction(gri, direction, action, false, e.getMessage());
                } catch (PSSException ex) {
                    log.error(ex);
                }
            }
            if (persist) {
                se.safeHibernateCommit(resv, bss);
            }
        } catch (Exception ex) {
            log.error(ex);
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
