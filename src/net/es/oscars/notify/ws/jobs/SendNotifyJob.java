package net.es.oscars.notify.ws.jobs;

import java.util.ArrayList;
import java.util.HashMap;
import org.apache.log4j.Logger;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.quartz.*;
import net.es.oscars.notify.ws.OSCARSNotifyCore;
import net.es.oscars.notify.ws.SubscriptionAdapter;

/**
 * A job for sending a notification to a subscriber
 *
 * @author Andew Lake (alake@internet2.edu)
 */
public class SendNotifyJob implements Job{
    public void execute(JobExecutionContext context) throws JobExecutionException{
        Logger log = Logger.getLogger(this.getClass());
        OSCARSNotifyCore core = OSCARSNotifyCore.getInstance();
                SubscriptionAdapter sa = core.getSubscriptionAdapter();
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String jobName = context.getJobDetail().getFullName();
        log.info("SendNotifyJob.start name:"+jobName);
        NotificationMessageHolderType holder = 
                        (NotificationMessageHolderType) dataMap.get("message");
        String url= dataMap.getString("url");
        String subRefId = dataMap.getString("subRefId");
        try{
            sa.sendNotify(holder, url, subRefId);
        }catch(Exception ex){
            log.error("Could not send Notify: " + ex);
        }
        log.info("SendNotifyJob.end name:"+jobName);
    }
}