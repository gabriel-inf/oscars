package net.es.oscars.notifybroker.jobs;

import java.util.ArrayList;
import java.util.HashMap;
import org.apache.log4j.Logger;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.quartz.*;
import net.es.oscars.notifybroker.NotifyBrokerCore;
import net.es.oscars.notifybroker.policy.NotifyPEP;
import net.es.oscars.notifybroker.ws.SubscriptionAdapter;

import org.hibernate.*;

/**
 * A job for finding the subscribers that want to receive a notification 
 * and scheduling it to be sent to them.
 *
 * @author Andew Lake (alake@internet2.edu)
 */
public class ProcessNotifyJob implements Job{
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Logger log = Logger.getLogger(this.getClass());
        NotifyBrokerCore core = NotifyBrokerCore.getInstance();
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String jobName = context.getJobDetail().getFullName();
        log.info("ProcessNotifyJob.start name:"+jobName);
        NotificationMessageHolderType holder = 
             (NotificationMessageHolderType) dataMap.get("message");
        HashMap<String, ArrayList<String>> permissionMap = 
             (HashMap<String, ArrayList<String>>) dataMap.get("permissionMap");
        ArrayList<NotifyPEP> notifyPEPs = 
             (ArrayList<NotifyPEP>) dataMap.get("notifyPEPs");
        
        Session notify = core.getNotifySession();
        notify.beginTransaction();
        try{
            //sa.notify(holder, permissionMap, notifyPEPs);
            notify.getTransaction().commit();
        }catch(Exception ex){
            log.error(ex);
            ex.printStackTrace();
            notify.getTransaction().rollback();
        }
        log.info("ProcessNotifyJob.end name:"+jobName);
    }
}