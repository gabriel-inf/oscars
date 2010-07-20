package net.es.oscars.pss.eompls.junos;

import net.es.oscars.bss.Reservation;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.common.PSSAction;
import net.es.oscars.pss.common.PSSDirection;
import net.es.oscars.pss.impl.sdn.SDNQueuer;
import net.es.oscars.scheduler.ChainingJob;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class PathSetupJob extends ChainingJob  implements Job{
    private Logger log;

    public void execute(JobExecutionContext context) throws JobExecutionException {
        this.log = Logger.getLogger(this.getClass());
        String jobName = context.getJobDetail().getFullName();
        this.log.debug("EoMPLS_JunosPathSetupJob.start name: "+jobName);
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        Reservation resv = (Reservation) dataMap.get("resv");
        PSSDirection direction = (PSSDirection) dataMap.get("direction");
        String gri = resv.getGlobalReservationId();
        PSSAction action = PSSAction.SETUP;
        boolean success = true;
        String error = "";
        try {
            String config = EoMPLSJunosConfigGen.getInstance().generateL2Setup(resv, direction);
            System.out.println(config);
            // FIXME genericize this
            SDNQueuer.getInstance().completeAction(gri, direction, action, success, error);
            
            
        } catch (PSSException e) {
            log.error(e);
        }
        

    }

}
