package net.es.oscars.resourceManager.scheduler;

import org.quartz.Job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.apache.log4j.Logger;

import net.es.oscars.resourceManager.common.RMCore;

import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.resourceManager.common.ScanReservations;

public class SchedulerJob implements Job {

    public SchedulerJob() {

    }
    
    public void execute(JobExecutionContext context) throws JobExecutionException {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(ModuleName.RMSCHED, "0001");
        ScanReservations scanReservations = ScanReservations.getInstance(null);
        if (System.currentTimeMillis() >= scanReservations.getLastScanned() + 
            RMCore.getInstance().getScanInterval() * 1000) {

            scanReservations.scan();
        }
    }  
    
}
 