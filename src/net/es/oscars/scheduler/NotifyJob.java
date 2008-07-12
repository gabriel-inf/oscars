package net.es.oscars.scheduler;

import org.apache.log4j.Logger;
import org.quartz.*;

import net.es.oscars.oscars.OSCARSCore;
import net.es.oscars.notify.*;

public class NotifyJob implements Job{
    private Logger log;
    private OSCARSCore core;
    
    public void execute(JobExecutionContext context) throws JobExecutionException {
        this.log = Logger.getLogger(this.getClass());
        String jobName = context.getJobDetail().getFullName();
        this.log.info("NotifyJob.start name:"+jobName);
        
        this.core = OSCARSCore.getInstance();
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        NotifierSource observable = this.core.getNotifier().getSource();
        Object event = (Object) dataMap.get("event");

        observable.eventOccured(event);
        this.log.info("NotifyJob.end name:"+jobName);
    }
}