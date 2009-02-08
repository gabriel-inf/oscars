package net.es.oscars.notifybroker.jobs;

import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.quartz.*;
import net.es.oscars.notifybroker.NotifyBrokerCore;
import net.es.oscars.notifybroker.SubscriptionManager;

import org.hibernate.*;
import org.jdom.Element;

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
        SubscriptionManager nbm = core.getNotifyBrokerManager();
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String jobName = context.getJobDetail().getFullName();
        log.debug("ProcessNotifyJob.start name:"+jobName);
        String publisherUrl = dataMap.getString("publisherUrl");
        List<String> topics = (List<String>) dataMap.get("topics");
        List<Element> msg = (List<Element>) dataMap.get("message");
        HashMap<String, List<String>> permissionMap = 
             (HashMap<String, List<String>>) dataMap.get("permissionMap");
        
        Session notify = core.getNotifySession();
        notify.beginTransaction();
        try{
            nbm.notify(publisherUrl, topics, permissionMap, msg);
            notify.getTransaction().commit();
        }catch(Exception ex){
            log.error(ex);
            ex.printStackTrace();
            notify.getTransaction().rollback();
        }
        log.debug("ProcessNotifyJob.end name:"+jobName);
    }
}