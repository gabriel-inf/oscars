package net.es.oscars.scheduler;

import org.apache.axis2.AxisFault;
import org.apache.axis2.databinding.types.URI.MalformedURIException;
import org.apache.log4j.Logger;
import org.quartz.*;
import java.rmi.RemoteException;
import java.util.*;
import net.es.oscars.client.*;
import net.es.oscars.notify.WSObserver;
import net.es.oscars.notify.ws.*;
import net.es.oscars.oscars.OSCARSCore;
import net.es.oscars.PropHandler;
import net.es.oscars.wsdlTypes.*;
import org.oasis_open.docs.wsn.b_2.*;
import org.oasis_open.docs.wsn.br_2.*;
import org.w3.www._2005._08.addressing.*;

public class RegisterPublisherJob implements Job{
    private Logger log;
    private OSCARSCore core;
    
    public void execute(JobExecutionContext context) throws JobExecutionException {
        this.log = Logger.getLogger(this.getClass());
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        this.core = OSCARSCore.getInstance();
        String jobName = context.getJobDetail().getFullName();
        log.info("ProcessNotifyJob.start name:"+jobName);
        String url = dataMap.getString("url");
        String repo = dataMap.getString("repo");
        String publisherURL = dataMap.getString("publisher");
        String consumerURL = url;
        Client client = new Client();
        RegisterPublisher request = new RegisterPublisher();
        EndpointReferenceType publisherRef = null;
        RegisterPublisherResponse response = null;
        String publisherRegistrationId = "";
        
        //send message
        try{
            publisherRef = client.generateEndpointReference(publisherURL);
            request.setPublisherReference(publisherRef);
            client.setUpNotify(true, url, repo, null);
            //send registration
            response = client.registerPublisher(request);
        }catch(MalformedURIException e){
            this.log.error("Cannot register with notification broker. The" +
                           " idc.url property in oscars.properties. is " +
                           "invalid. Please set that property to a valid" +
                           " URL and restart OSCARS.");
             return;
        }catch(Exception e){
            this.log.error("Exception when trying to register with " +
                           "NotificationBroker: " + e);
            this.log.info("Scheduling another register attempt...");
            this.reschedule(dataMap);
            WSObserver.registered(null, consumerURL);
            return;
        }
        
        //parse results
        EndpointReferenceType conRef = response.getConsumerReference();
        if(conRef != null){
            consumerURL = conRef.getAddress().toString();
        }
        /* Get PublisherRegistrationId. If doesn't exist, then assume it's not
           required by the NotificationBroker implementation */
        EndpointReferenceType pubRef = response.getPublisherRegistrationReference();
        ReferenceParametersType refParams = pubRef.getReferenceParameters();
        if(refParams != null && refParams.getPublisherRegistrationId() != null){
            publisherRegistrationId = refParams.getPublisherRegistrationId();
        }
        
        WSObserver.registered(publisherRegistrationId, consumerURL);
        log.info("ProcessNotifyJob.end name:"+jobName);
    }
     
    private void reschedule(JobDataMap dataMap){
        this.log.info("reschedule.start");
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("notify.ws.broker", true);
        String retryWaitStr = props.getProperty("secondsBetweenRegistrationRetries");
        long retryWait = 60000L;//default to 60 seconds
        if(retryWaitStr != null){
            try{
                retryWait = Long.parseLong(retryWaitStr) * 1000L;
            }catch(Exception e){
                this.log.error("Invalid notify.ws.broker.secondsBetweenRegistrationRetries" +
                               "property. Defaulting to 60 seconds");
            }
        }
        if(retryWait < 10000){//minimum is 10 seconds
            retryWait = 10000L;
            this.log.info("Defaulting to minimum retry of 10 seconds.");
        }
        
        Scheduler sched = this.core.getScheduleManager().getScheduler();
        String triggerName = "reschedulePubRegTrig-" + this.hashCode();
        String jobName = "rescheduleRegPublisher-" + this.hashCode();
        long timestamp = System.currentTimeMillis() + retryWait;
        SimpleTrigger trigger = new SimpleTrigger(triggerName, null, 
                                                  new Date(timestamp), 
                                                  null, 0, 0L);
        JobDetail jobDetail = new JobDetail(jobName, "REGISTER_PUBLISHER",
                                            RegisterPublisherJob.class);
        jobDetail.setJobDataMap(dataMap);
        
        try{
            this.log.debug("Adding job " + jobName);
            sched.scheduleJob(jobDetail, trigger);
            this.log.debug("Job added.");
            this.log.info("Retrying RegisterPublisher in " + retryWait/1000 + " seconds.");
        }catch(SchedulerException ex){
            this.log.error("Scheduler exception: " + ex);
            this.log.error("ERROR: OSCARS was unable to register with a " + 
                           "NotificationBroker and unable to schedule a " +
                           "later re-attempt. Please check " +
                           "notify.ws.broker.url in oscars.properties and" +
                           " restart OSCARS.");
        }
        this.log.info("reschedule.end");
    }
}