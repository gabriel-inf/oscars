package net.es.oscars.pss.impl.sdn;

import net.es.oscars.bss.Reservation;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.common.PSSAction;
import net.es.oscars.pss.common.PSSDirection;
import net.es.oscars.pss.common.PSSHandler;
import net.es.oscars.pss.impl.sdn.SDNQueuer;
import net.es.oscars.scheduler.ChainingJob;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class ContactNodeJob extends ChainingJob  implements Job{
    private Logger log;

    public void execute(JobExecutionContext context) throws JobExecutionException {
        this.log = Logger.getLogger(this.getClass());
        String jobName = context.getJobDetail().getFullName();
        this.log.debug("EoMPLS_JunosPathSetupJob.start name: "+jobName);
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();

        
        
        Reservation resv        = (Reservation) dataMap.get("resv");
        PSSDirection direction  = (PSSDirection) dataMap.get("direction");
        PSSAction action        = (PSSAction) dataMap.get("action");
        PSSHandler handler      = (PSSHandler) dataMap.get("handler");
        SDNQueuer q = SDNQueuer.getInstance();
        
        String gri = resv.getGlobalReservationId();
        
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
            } catch (PSSException e1) {
                log.error(e1);
            }
        }
        

    }

}
