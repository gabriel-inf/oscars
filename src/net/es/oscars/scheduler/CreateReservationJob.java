package net.es.oscars.scheduler;
import org.apache.log4j.Logger;
import org.quartz.*;
import net.es.oscars.bss.*;
import net.es.oscars.oscars.OSCARSCore;
import net.es.oscars.pss.PSSException;

public class CreateReservationJob extends ChainingJob implements org.quartz.Job {
    private Logger log;
    private OSCARSCore core;


    public void execute(JobExecutionContext context) throws JobExecutionException {
        String jobName = context.getJobDetail().getFullName();
        this.log = Logger.getLogger(this.getClass());
        this.log.debug("CreateReservationJob.start name:"+jobName);

        this.core = OSCARSCore.getInstance();
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        Reservation resv = (Reservation) dataMap.get("reservation");

        String gri;
        if (resv != null) {
            gri = (String) resv.getGlobalReservationId();
        } else {
            this.log.error("No reservation associated with job name:"+jobName);
            return;
        }
        this.log.debug("GRI is: "+gri+"for job name: "+jobName);
        super.execute(context);



        this.log.debug("CreateReservationJob.end name:"+jobName);
    }

}
