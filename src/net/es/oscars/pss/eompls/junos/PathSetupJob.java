package net.es.oscars.pss.eompls.junos;

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

    }

}
