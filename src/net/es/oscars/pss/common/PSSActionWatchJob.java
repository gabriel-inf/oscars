package net.es.oscars.pss.common;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import net.es.oscars.pss.PSSException;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;


public class PSSActionWatchJob implements Job {

    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        System.out.println("PSSActionWatchJob starting up");
        
        PSSActionWatcher aw = PSSActionWatcher.getInstance();
        ConcurrentHashMap<String, PSSActionDirections> watchList = aw.getWatchList();
        PSSActionStatusHolder ah = PSSActionStatusHolder.getInstance();
        
        for (String gri : watchList.keySet()) {
            PSSActionDirections ads = watchList.get(gri);
            PSSAction action = ads.getAction();
            List<PSSDirection> directions = ads.getDirections();
            
            for (PSSDirection direction : directions) {
                PSSActionStatus as;
                try {
                    as = ah.getDirectionActionStatus(gri, direction, action);
                } catch (PSSException e) {
                    throw new JobExecutionException(e);
                }
                PSSStatus status = as.getStatus();
                System.out.println(gri+" "+direction+" "+status);
                if (direction.equals(PSSDirection.BIDIRECTIONAL)) {
                    if (status.equals(PSSStatus.FAILURE)) {
                        
                        // TODO
                    } else if (status.equals(PSSStatus.SUCCESS)) {
                        // TODO
                        
                    } else if (status.equals(PSSStatus.INPROGRESS)) {
                        // TODO
                    } else if (status.equals(PSSStatus.UNSTARTED)) {
                        // TODO
                    }
                } else {
                    // TODO
                    if (status.equals(PSSStatus.FAILURE)) {
                        
                        // TODO
                    } else if (status.equals(PSSStatus.SUCCESS)) {
                        // TODO
                        
                    } else if (status.equals(PSSStatus.INPROGRESS)) {
                        // TODO
                    } else if (status.equals(PSSStatus.UNSTARTED)) {
                        // TODO
                    }
                    
                }
            }
        }
        System.out.println("PSSActionWatchJob done");
    }
    

}