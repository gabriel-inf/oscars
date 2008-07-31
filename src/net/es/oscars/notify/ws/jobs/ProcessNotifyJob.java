package net.es.oscars.notify.ws.jobs;

import java.util.ArrayList;
import java.util.HashMap;
import org.apache.log4j.Logger;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.quartz.*;
import net.es.oscars.notify.ws.OSCARSNotifyCore;
import net.es.oscars.notify.ws.SubscriptionAdapter;

/**
 * A job for finding the subscribers that want to receive a notification 
 * and scheduling it to be sent to them.
 *
 * @author Andew Lake (alake@internet2.edu)
 */
public class ProcessNotifyJob implements Job{
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Logger log = Logger.getLogger(this.getClass());
        OSCARSNotifyCore core = OSCARSNotifyCore.getInstance();
        SubscriptionAdapter sa = core.getSubscriptionAdapter();
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String jobName = context.getJobDetail().getFullName();
        log.info("ProcessNotifyJob.start name:"+jobName);
        
        NotificationMessageHolderType holder = 
                        (NotificationMessageHolderType) dataMap.get("message");
        HashMap<String, ArrayList<String>> permissionMap = 
             (HashMap<String, ArrayList<String>>) dataMap.get("permissionMap");
        try{
            sa.notify(holder, permissionMap);
        }catch(Exception ex){
            log.error(ex);
        }
        log.info("ProcessNotifyJob.end name:"+jobName);
    }
}