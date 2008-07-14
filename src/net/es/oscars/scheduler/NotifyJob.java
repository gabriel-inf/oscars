package net.es.oscars.scheduler;

import org.apache.log4j.Logger;
import org.hibernate.Session;
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

        // need this to get info for Reservation objects
        Session bss = core.getBssSession();
        bss.beginTransaction();
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        NotifierSource observable = this.core.getNotifier().getSource();
        Object event = (Object) dataMap.get("event");

        observable.eventOccured(event);
        bss.getTransaction().commit();


        this.log.info("NotifyJob.end name:"+jobName);
    }
}