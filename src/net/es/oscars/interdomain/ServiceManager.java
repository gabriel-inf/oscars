package net.es.oscars.interdomain;

import org.apache.log4j.Logger;
import org.quartz.*;
import java.util.*;
import java.net.InetAddress;

import net.es.oscars.bss.OSCARSCore;
import net.es.oscars.scheduler.*;
import net.es.oscars.PropHandler;

/**
 * Class for managing OSCARS interaction with external services. This may 
 * include subscribing to other domain's notifications, registering with a
 * lookup service, or pushing topology information.
 */
public class ServiceManager{
    private Logger log;
    private ArrayList<Class> serviceJobs;
    private OSCARSCore core;
    private HashMap<String, Object> serviceData;
    private String idcURL;
    private String repo;
    private String axisConfig;
    private String axisConfigNoRampart;
    
    /**
     * Default constructor. Schedules those
     * jobs that are specified in oscars.properties.
     */
    public ServiceManager(){
        this.log = Logger.getLogger(this.getClass());
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("external.service", true);
        Properties idcProps = propHandler.getPropertyGroup("idc", true); 
        this.serviceJobs = new ArrayList<Class>();
        this.core = OSCARSCore.getInstance();
        this.serviceData = new HashMap<String, Object>();
        String localhost = null;
        this.idcURL = idcProps.getProperty("url");
        
        /* FIXME */
        String catalinaHome = System.getProperty("catalina.home");
        if (catalinaHome == null) {
            catalinaHome = System.getenv("CATALINA_HOME");
        }
        // check for trailing slash
        if (catalinaHome != null) {
            if (!catalinaHome.endsWith("/")) {
                catalinaHome += "/";
            }
            this.repo = catalinaHome + "shared/classes/repo/";
        } else {
        	// this is a better place..
        	this.repo = "conf/server/";
        }
        this.log.debug ("repo is set to:" + this.repo);
        this.axisConfig = this.repo + "axis2.xml";
        this.axisConfigNoRampart = this.repo + "axis2-norampart.xml";
        
        /* Set IDC URL */
        if(this.idcURL == null){
            try{
                localhost = InetAddress.getLocalHost().getHostName();
                this.idcURL = "https://" + localhost + ":8443/axis2/services/OSCARS";
                this.log.info("idc.url not set in oscars.properties. Defaulting to " + this.idcURL);
            }catch(Exception e){
                this.log.error("Unable to determine localhost.");
                this.log.error("You need to set idc.url in oscars.properties!");
                return;
            }
        }
        
        /* Load service modules */
        for(int i = 1; props.getProperty(i+"") != null; i++){
            String service = props.getProperty(i+"");
            if("lsregister".equals(service.toLowerCase())){
                this.serviceJobs.add(LSRegisterJob.class);
            }else if("lsdomainupdate".equals(service.toLowerCase())){
                this.serviceJobs.add(LSDomainUpdateJob.class);
            }else if("topology".equals(service.toLowerCase())){
                this.serviceJobs.add(TopologyRegisterJob.class);
            }else if("subscribe".equals(service.toLowerCase())){
                this.serviceJobs.add(SubscribeJob.class);
            }
        }
        
        /* Load JobDataMap and schedule jobs */
        JobDataMap dataMap = new JobDataMap();
        dataMap.put("init", true);
        for(Class serviceJob : serviceJobs){
            this.scheduleServiceJob(serviceJob, dataMap, new Date());
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
        Scheduler sched = this.core.getScheduleManager().getScheduler();
        long currTime = System.currentTimeMillis();
        String triggerName = "serviceTrig-" + job.hashCode()+currTime;
        String jobName = "serviceJob-" + job.hashCode()+currTime;
        SimpleTrigger trigger = new SimpleTrigger(triggerName, null, 
                                                  date, null, 0, 0L);
        JobDetail jobDetail = new JobDetail(jobName, "EXT_SERVICE", job);
        dataMap.put("idcURL", this.idcURL);
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
    
    /**
     * Retrieves stored data about a specific service type. The object
     * returned is dependent on the service being looked-up.
     *
     * @param service the type of service to get data about
     * @return service-speicific information
     */
    public Object getServiceData(String service){
        return this.serviceData.get(service);
    }
    
    /**
     * Adds data about a specific service.
     *
     * @param service the type of service to store data about
     * @param value the object to store
     */
    public synchronized void putServiceData(String service, Object value){
        this.serviceData.put(service, value);
    }
    
    /**
     * Convenience function for retrieving data stored as a Map
     *
     * @param service the type of service to query
     * @param key the key to the Map entry of interest
     * @return the value from the stored map
     */
    public Object getServiceMapData(String service, String key){
        Map<String, Object> map = (Map<String, Object>)this.serviceData.get(service);
        return map.get(key);
    } 
    
    /**
     * Convenience function for storing service data stored in a Map
     *
     * @param service the type of service
     * @param key the key of the Map entry
     * @param value the value of the Map entry
     */
    public synchronized void putServiceMapData(String service, String key, Object value){
        Map<String, Object> map = (Map<String, Object>)this.serviceData.get(service);
        map.put(key, value);
        this.putServiceData(service, (Object) map);
    }
    
    /**
	 * @return the path to the rampart-enabled axis2.xml configuration file 
	 */
	public String getAxisConfig() {
		return axisConfig;
	}
	
	/**
	 * @param axisConfig the path to the rampart-enabled axis2.xml configuration file 
	 */
	public void setAxisConfig(String axisConfig) {
		this.axisConfig = axisConfig;
	}
	
	/**
	 * @return the path to the rampart-disabled axis2.xml configuration file 
	 */
	public String getAxisConfigNoRampart() {
		return axisConfigNoRampart;
	}
	
	/**
	 * @param axisConfigNoRampart the path to the rampart-disabled axis2.xml configuration file 
	 */
	public void setAxisConfigNoRampart(String axisConfigNoRampart) {
		this.axisConfigNoRampart = axisConfigNoRampart;
	}
	
	/**
	 * @return the URL of the local IDC
	 */
	public String getIdcURL() {
		return idcURL;
	}
	
	/**
	 * @param idcURL the URL of the local IDC
	 */
	public void setIdcURL(String idcURL) {
		this.idcURL = idcURL;
	}
	
	/**
	 * @return the repo
	 */
	public String getRepo() {
		return repo;
	}
	
	/**
	 * @param repo the repo to set
	 */
	public void setRepo(String repo) {
		this.repo = repo;
	}
	
	/**
	 * @return the list of job types loaded on intialization
	 */
	public ArrayList<Class> getServiceJobs() {
		return serviceJobs;
	}
	
	/**
	 * @param serviceJobs the list of job types loaded on intialization
	 */
	public void setServiceJobs(ArrayList<Class> serviceJobs) {
		this.serviceJobs = serviceJobs;
	}
}