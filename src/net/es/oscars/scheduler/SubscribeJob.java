package net.es.oscars.scheduler;

import java.util.*;
import java.net.URL;
import java.rmi.RemoteException;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.quartz.*;
import org.oasis_open.docs.wsn.b_2.*;
import net.es.oscars.oscars.OSCARSCore;
import net.es.oscars.interdomain.ServiceManager;
import net.es.oscars.bss.topology.*;
import net.es.oscars.client.Client;
import net.es.oscars.notify.ws.*;
import org.w3.www._2005._08.addressing.*;
import org.apache.axis2.databinding.types.URI.MalformedURIException;
import org.apache.axis2.AxisFault;
import net.es.oscars.PropHandler;

/**
 * Job that subscribes to notifications from other IDCs.
 */
public class SubscribeJob implements Job{
    private Logger log;
    private OSCARSCore core;
    private ServiceManager serviceMgr;
    private String idcURL;
    private String repo;
    private String axisConfig;
    private static double TERM_TIME_WINDOW;
    private static long RETRY_INTERVAL;
    private static String TOPICS;
    
    private final double DEFAULT_TERM_TIME_WINDOW = .2;
    private final long DEFAULT_RETRY_INTERVAL = 1800;//30 minutes
    private final String DEFAULT_TOPICS = "idc:INFO";
    
    /**
     * Detects whether this is an initial call where it needs to subscribe to 
     * all the neighbors, a single subscribe message that needs to be re-sent 
     * because of a previously failed attempt, or a renewal of an existing 
     * subscription and executes the appropriate task.
     *
     * @param context
     */
    public void execute(JobExecutionContext context) throws JobExecutionException {
        this.log = Logger.getLogger(this.getClass());
        String jobName = context.getJobDetail().getFullName();
        this.log.info("SubscribeJob.start name:"+jobName);
        this.core = OSCARSCore.getInstance();
        this.serviceMgr = this.core.getServiceManager();
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        this.idcURL = dataMap.getString("idcURL");
        this.repo = dataMap.getString("repo");
        this.axisConfig = dataMap.getString("axisConfig");
        Session bss = this.core.getBssSession();
        bss.beginTransaction();
        try{
            if(dataMap.containsKey("renew")){
                this.renew(dataMap);
            }else if(dataMap.containsKey("init")){
                dataMap.remove("init");
                this.init();
                HashMap<String, EndpointReferenceType> map = new HashMap<String, EndpointReferenceType>();
                this.serviceMgr.putServiceData("NB", (Object) map);
                this.subscribeAll(dataMap);
            }else if(dataMap.containsKey("subscribe")){
                this.subscribe(dataMap, null);
            }
            bss.getTransaction().commit();
        }catch(Exception e){
            bss.getTransaction().rollback();
            e.printStackTrace();
        }
        this.log.info("SubscribeJob.end name:"+jobName);
    }
    
    /**
     * Initializes the job and loads values from oscars.properties into global variables
     */
    private void init(){
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("external.service.subscribe", true);
        
        SubscribeJob.TERM_TIME_WINDOW = -1;
        if(props.getProperty("termTimeWindow") != null){
            try{
                SubscribeJob.TERM_TIME_WINDOW = Double.parseDouble(props.getProperty("termTimeWindow"));
            }catch(Exception e){}
        }
        if(SubscribeJob.TERM_TIME_WINDOW < 0 || SubscribeJob.TERM_TIME_WINDOW >= 1){
            SubscribeJob.TERM_TIME_WINDOW = DEFAULT_TERM_TIME_WINDOW;
        }
        
        SubscribeJob.RETRY_INTERVAL = 0;
        if(props.getProperty("retryInterval") != null){
            try{
                SubscribeJob.RETRY_INTERVAL = Long.parseLong(props.getProperty("retryInterval"));
            }catch(Exception e){}
        }
        if(SubscribeJob.RETRY_INTERVAL <= 10){//minimum 10 seconds
            SubscribeJob.RETRY_INTERVAL = DEFAULT_RETRY_INTERVAL;
        }
        
        SubscribeJob.TOPICS = props.getProperty("topics");
        if(SubscribeJob.TOPICS == null){
            SubscribeJob.TOPICS = DEFAULT_TOPICS;
        }
    }
    
    /**
     * Subscribes to each neighbor.
     *
     * @param dataMap the subscription paramters
     */
    private void subscribeAll(JobDataMap dataMap){
        //get neighbors
        this.log.debug("subscribeAll.start");
        DomainDAO domainDAO = new DomainDAO(this.core.getBssDbName());
        List<Domain> neighbors = domainDAO.getNeighbors();
        if(neighbors == null){
            this.log.debug("No neighbors in database");
            return;
        }
        for(Domain neighbor : neighbors){
            String neighborID = neighbor.getTopologyIdent();
            this.log.debug("neighbor=" + neighborID);
            String neighborURL = neighbor.getUrl();
            try{
                new URL(neighborURL);
            }catch(Exception e){
                this.log.debug("Skipping neighbor " + neighborID + 
                               " because URL is invalid");
                continue;
            }
            this.subscribe(dataMap, neighbor);
        }
        this.log.debug("subscribeAll.end");
    }
    
    /**
     * Subscribes to a single neighbor's notifications.
     *
     * @param dataMap the subscription paramters
     * @param neighbor the neighboring domain to which to subscribe
     */
    private void subscribe(JobDataMap dataMap, Domain neighbor){
        this.log.debug("subscribe.start");
        if(neighbor == null && dataMap.get("neighbor") == null){
            this.log.debug("No domain provided");
            return;
        }else if(neighbor == null){
             DomainDAO domainDAO = new DomainDAO(this.core.getBssDbName());
             neighbor = domainDAO.fromTopologyIdent(dataMap.getString("neighbor"));
        }
        //if neighbor is STILL null
        if(neighbor == null){
            this.log.debug("Domain not found.");
            return;
        }

        String neighborID = neighbor.getTopologyIdent();
        String neighborURL = neighbor.getUrl();
        String subscribeURL = this.lookup(neighbor);
        this.log.debug("subscribeURL=" + subscribeURL);
        SubscribeResponse response = null;
        try{
            response = this.sendSubscribe(subscribeURL, neighborURL);
        }catch(Exception e){
            this.log.error(e);
            e.printStackTrace();
        }
        
        long currTime = System.currentTimeMillis();
        long nextJobTime = currTime + SubscribeJob.RETRY_INTERVAL*1000;
        if(response != null){
            EndpointReferenceType subRef = response.getSubscriptionReference();
            java.util.Calendar termTime = response.getTerminationTime();
            if(termTime != null){ 
                nextJobTime = currTime;
                nextJobTime += (long)((1.0-SubscribeJob.TERM_TIME_WINDOW) * 
                                (double)(termTime.getTimeInMillis() - currTime)); 
                this.log.debug("termTime=" + termTime.getTimeInMillis());
            }
            this.log.debug("nextJobTime=" + nextJobTime);
            //schedule renewal
            dataMap.put("renew", true);
            this.serviceMgr.putServiceMapData("NB", neighborID, (Object)subRef);
        }else{
            dataMap.put("subscribe", true);
        }
        
        //schedule next job
        dataMap.put("neighbor", neighborID);
        this.serviceMgr.scheduleServiceJob(SubscribeJob.class, dataMap, new Date(nextJobTime));
        
        this.log.debug("subscribe.end");
    }
    
    /** 
     * Sends the subscribe message and returns the response. This will only
     * be called on initialization or after a renew fails.
     *
     * @param subscribeURL whereto send the request
     * @param neighborURL the IDC whose notifications are wanted
     * @return the response of the subscription. null if no subscribeURL given
     * @throws AxisFault
     * @throws MalformedURIException
     * @throws RemoteException
     * @throws Exception
     */
    private SubscribeResponse sendSubscribe(String subscribeURL, String neighborURL)
        throws AxisFault, MalformedURIException, RemoteException, Exception{
        if(subscribeURL == null){
            return null;
        }
        this.log.debug("sendSubscribe.start");
        Client client = new Client();
        client.setUpNotify(true, subscribeURL, this.repo, this.axisConfig);
        //Clear out any old Subscriptions, ignore failures
        try{
            Unsubscribe unsubscribe = new Unsubscribe();
            EndpointReferenceType subRef = client.generateEndpointReference(subscribeURL, "ALL");
            unsubscribe.setSubscriptionReference(subRef);
            client.unsubscribe(unsubscribe);
        }catch(Exception e){}
        //Create new subscription
        Subscribe subscribe = new Subscribe();
        String[] neighborURLArr = new String[1];
        neighborURLArr[0] =neighborURL;
        FilterType filter = new FilterType();
        TopicExpressionType topicExpr = client.generateTopicExpression(SubscribeJob.TOPICS);
        QueryExpressionType producerProps = client.generateProducerProperties(neighborURLArr);
        EndpointReferenceType consumerRef = client.generateEndpointReference(this.idcURL);
        filter.addTopicExpression(topicExpr);
        filter.addProducerProperties(producerProps);
        subscribe.setConsumerReference(consumerRef);
        subscribe.setFilter(filter);
        SubscribeResponse response = client.subscribe(subscribe);
        
        this.log.debug("sendSubscribe.end");
        return response;
    }
    
    /**
     * Handles renewing an existing subscription
     *
     * @param dataMap the renewal parameters
     */
    private void renew(JobDataMap dataMap){
        this.log.debug("renew.start");
        String neighborID = dataMap.getString("neighbor");
        if(neighborID == null){
            this.log.debug("renew=No domain specified");
            return;
        }
        DomainDAO domainDAO = new DomainDAO(this.core.getBssDbName());
        Domain neighbor = domainDAO.fromTopologyIdent(neighborID);
        if(neighbor == null){
            this.log.debug("renew=Cannot find neighbor");
            return;
        }
        EndpointReferenceType subRef = (EndpointReferenceType) this.serviceMgr.getServiceMapData("NB", neighborID);
        if(subRef == null){
            this.log.debug("renew=Subscription not found, trying subscribe.");
            dataMap.remove("renew");
            this.subscribe(dataMap, neighbor);
            return;
        }
        
        String neighborURL = neighbor.getUrl();
        String subscribeURL = this.lookup(neighbor);
        RenewResponse response = null;
        try{
            response = this.sendRenew(subRef, subscribeURL);
        }catch(Exception e){
            //if fails, try creating a new subscription
            dataMap.remove("renew");
            this.subscribe(dataMap, neighbor);
            return;
        }
        
        //Save subscription reference (may not have changed) and termination time
        long currTime = System.currentTimeMillis();
        long nextJobTime = currTime + SubscribeJob.RETRY_INTERVAL*1000;
        if(response != null){
            subRef = response.getSubscriptionReference();
            java.util.Calendar termTime = response.getTerminationTime();
            nextJobTime = currTime;
            nextJobTime += (long)((1.0-SubscribeJob.TERM_TIME_WINDOW) * 
                           (double)(termTime.getTimeInMillis() - nextJobTime)); 
            this.log.debug("termTime=" + termTime.getTimeInMillis());
            this.log.debug("nextJobTime=" + nextJobTime);
            this.serviceMgr.putServiceMapData("NB", neighborID, (Object)subRef);
        }else{
            dataMap.remove("renew");
            this.serviceMgr.putServiceMapData("NB", neighborID, null);
            dataMap.put("subscribe", true);
        }
        
        //schedule next job
        this.serviceMgr.scheduleServiceJob(SubscribeJob.class, dataMap, new Date(nextJobTime));
        this.log.debug("renew.end");
        return;
    }
    
    /**
     * Sends the Renew message to the given URL
     *
     * @param subRef a reference to the subscription that needs renewal
     * @param url where to send the request
     * @param the response of the renewal message. null if not URL given.
     * @throws RemoteException, 
     * @throws ResourceUnknownFault
     * @throws UnacceptableTerminationTimeFault
     * @throws AAAFaultMessage
     */
    private RenewResponse sendRenew(EndpointReferenceType subRef, String url)
                throws RemoteException, ResourceUnknownFault,
                       net.es.oscars.notify.ws.UnacceptableTerminationTimeFault, 
                       AAAFaultMessage{
        if(url == null){
            return null;
        }
        Client client = new Client();
        client.setUpNotify(true, url, this.repo, this.axisConfig);
        Renew request = new Renew();
        request.setSubscriptionReference(subRef);
        RenewResponse response = client.renew(request);
        return response;
    }
    
    /**
     * Looks-up where to send Subscribe messages for a given domain. First tries
     * the lookup service and then tries its own internal tables.
     *
     * @param neighbor the domain withthe URL to lookup
     * @return the URL where Subscribe messages should be sent
     */
    private String lookup(Domain neighbor){
        //TODO: Try lookup service first
        DomainServiceDAO dao = new DomainServiceDAO(this.core.getBssDbName());
        return dao.getUrl(neighbor, "NB");
    }
}