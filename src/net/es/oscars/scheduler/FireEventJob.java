package net.es.oscars.scheduler;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.quartz.*;

import net.es.oscars.bss.OSCARSCore;
import net.es.oscars.bss.events.*;

public class FireEventJob implements Job{
    private Logger log;
    private OSCARSCore core;

    public void execute(JobExecutionContext context) throws JobExecutionException {
        this.log = Logger.getLogger(this.getClass());
        String jobName = context.getJobDetail().getFullName();
        this.log.debug("FireEventJob.start name:"+jobName);

        this.core = OSCARSCore.getInstance();

        // need this to get info for Reservation objects
        Session bss = core.getBssSession();
        bss.beginTransaction();
        try{
            JobDataMap dataMap = context.getJobDetail().getJobDataMap();
            ObserverSource observable = this.core.getObserverMgr().getSource();
            Object event = (Object) dataMap.get("event");
            observable.eventOccured(event);
            bss.getTransaction().commit();
        }catch(Exception e){
            bss.getTransaction().rollback();
        }

        this.log.debug("FireEventJob.end name:"+jobName);
    }
}