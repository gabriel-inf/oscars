package net.es.oscars.notifybroker.jobs;

import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.quartz.*;

import net.es.oscars.notifybroker.senders.Notification;
import net.es.oscars.notifybroker.senders.NotifySender;
import net.es.oscars.notifybroker.senders.NotifySenderFactory;

/**
 * A job for sending a notification to a subscriber
 *
 * @author Andew Lake (alake@internet2.edu)
 */
public class SendNotifyJob implements Job{
    public void execute(JobExecutionContext context) throws JobExecutionException{
        Logger log = Logger.getLogger(this.getClass());
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String jobName = context.getJobDetail().getFullName();
        log.debug("SendNotifyJob.start name:"+jobName);
        Notification notify = new Notification();
        notify.setDestinationUrl(dataMap.getString("url"));
        notify.setMsg((List<Element>) dataMap.get("message"));
        notify.setPublisherUrl(dataMap.getString("publisherUrl"));
        notify.setSubscriptioId(dataMap.getString("subRefId"));
        notify.setTopics((List<String>) dataMap.get("topics"));
        
        try{
            NotifySender sender = NotifySenderFactory.createNotifySender();
            sender.sendNotify(notify);
        }catch(Exception ex){
            log.error("Could not send Notify: " + ex);
            ex.printStackTrace();
        }
        log.debug("SendNotifyJob.end name:"+jobName);
    }
}