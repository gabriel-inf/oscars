package net.es.oscars.notify.ws.jobs;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import net.es.oscars.PropHandler;
import net.es.oscars.notify.ws.OSCARSNotifyCore;
import net.es.oscars.notify.ws.jobs.LSRegisterJob;

import org.apache.log4j.Logger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;

public class ServiceManager {
	private Logger log;
    private ArrayList<Class> serviceJobs;
    private OSCARSNotifyCore core;
    private String nbURL;
    private String repo;
    private String axisConfig;
    private String axisConfigNoRampart;
    
    private long INIT_WAIT = 30;
    
    /**
     * Default constructor. Schedules those
     * jobs that are specified in oscars.properties.
     */
    public ServiceManager(){
        this.log = Logger.getLogger(this.getClass());
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("external.service", true);
        Properties nbProps = propHandler.getPropertyGroup("notify.ws.broker", true); 
        this.serviceJobs = new ArrayList<Class>();
        this.core = OSCARSNotifyCore.getInstance();
        String localhost = null;
        this.nbURL = nbProps.getProperty("url");
        String catalinaHome = System.getProperty("catalina.home");
        // check for trailing slash
        if (!catalinaHome.endsWith("/")) {
            catalinaHome += "/";
        }
        this.repo = catalinaHome + "shared/classes/repo/";
        this.axisConfig = this.repo + "axis2.xml";
        this.axisConfigNoRampart = this.repo + "axis2-norampart.xml";
        
        /* Set IDC URL */
        if(this.nbURL == null){
            try{
                localhost = InetAddress.getLocalHost().getHostName();
                this.nbURL = "https://" + localhost + ":8443/axis2/services/OSCARSNotify";
                this.log.info("notifybroker.url not set in oscars.properties. Defaulting to " + this.nbURL);
            }catch(Exception e){
                this.log.error("Unable to determine localhost.");
                this.log.error("You need to set notifybroker.url in oscars.properties!");
                return;
            }
        }
        
        /* Load service modules */
        for(int i = 1; props.getProperty(i+"") != null; i++){
            String service = props.getProperty(i+"");
            if("lsregister".equals(service.toLowerCase())){
                this.serviceJobs.add(LSRegisterJob.class);
            }
        }
        if(props.getProperty("initWaitTime") != null){
	    	 try{
	    		 INIT_WAIT = Long.parseLong(props.getProperty("initWaitTime"));
	         }catch(Exception e){}
	     }
        /* Load JobDataMap and schedule jobs */
        JobDataMap dataMap = new JobDataMap();
        dataMap.put("init", true);
        for(Class serviceJob : serviceJobs){
        	Date date = new Date(System.currentTimeMillis() + INIT_WAIT*1000);
            this.scheduleServiceJob(serviceJob, dataMap, date);
        }
    }
    
    /**
     * Schedules a service job for execution. Adds values to JobDataMap
     * common to external services such as local IDC URL and the location
     * of Axis2 client configuration files.
     *
     * @param job the type of job to schedule
     * @param dataMap the initial dataMap for the job
     * @param date a Date indicating when the job should start
     */
    public void scheduleServiceJob(Class job, JobDataMap dataMap, Date date){
        Scheduler sched = this.core.getScheduler();
        long currTime = System.currentTimeMillis();
        String triggerName = "serviceTrig-" + job.hashCode()+currTime;
        String jobName = "serviceJob-" + job.hashCode()+currTime;
        SimpleTrigger trigger = new SimpleTrigger(triggerName, null, 
                                                  date, null, 0, 0L);
        JobDetail jobDetail = new JobDetail(jobName, "EXT_SERVICE", job);
        dataMap.put("nbURL", this.nbURL);
        dataMap.put("repo", this.repo);
        dataMap.put("axisConfig", this.axisConfig);
        dataMap.put("axisConfigNoRampart", this.axisConfigNoRampart);
        jobDetail.setJobDataMap(dataMap);
        try{
            this.log.debug("Adding job " + jobName);
            sched.scheduleJob(jobDetail, trigger);
            this.log.debug("Job added.");
        }catch(SchedulerException ex){
            this.log.error("Scheduler exception: " + ex);    
        }
    }
	    
}
