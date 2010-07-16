package net.es.oscars.pss.common;

import java.util.HashMap;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class PSSActionWatchJob implements Job{

    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        PSSActionWatcher aw = PSSActionWatcher.getInstance();
        HashMap<String, PSSActionDirections> watchList = aw.getWatchList();
        PSSActionStatusHolder ah = PSSActionStatusHolder.getInstance();
        
        for (String gri : watchList.keySet()) {
            PSSActionDirections ads = watchList.get(gri);
            PSSAction action = ads.getAction();
            List<PSSDirection> directions = ads.getDirections();
            HashMap<PSSGriDirection, PSSActionStatus> statuses;
            if (action.equals(PSSAction.SETUP)) {
                statuses = ah.getSetupStatuses();
            } else {
                statuses = ah.getTeardownStatuses();
            }
            
            for (PSSDirection direction : directions) {
                PSSGriDirection griDir = new PSSGriDirection();
                griDir.setDirection(direction);
                griDir.setGri(gri);
                PSSActionStatus as = statuses.get(griDir);
                PSSStatus status = as.getStatus();
                if (direction.equals(PSSDirection.BIDIRECTIONAL)) {
                    if (status.equals(PSSStatus.FAILURE)) {
                        
                    } else if (status.equals(PSSStatus.SUCCESS)) {
                        
                    } else if (status.equals(PSSStatus.INPROGRESS)) {
                    } else if (status.equals(PSSStatus.UNSTARTED)) {
                    }
                } else {
                    
                }
            }
        }
        
        
    }
    

}
