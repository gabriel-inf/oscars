package net.es.oscars.notifybroker.jobs;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import net.es.oscars.PropHandler;
import net.es.oscars.lookup.LookupException;
import net.es.oscars.lookup.PSLookupClient;
import net.es.oscars.notifybroker.OSCARSNotifyCore;
import net.es.oscars.notifybroker.db.ExternalService;
import net.es.oscars.notifybroker.db.ExternalServiceDAO;
import net.es.oscars.notifybroker.db.Publisher;
import net.es.oscars.notifybroker.db.PublisherDAO;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class LSRegisterJob  implements Job{
	private Logger log;
	private OSCARSNotifyCore core;
	private long RENEW_TIME = 3600;//60 minutes
	
	public void execute(JobExecutionContext context) throws JobExecutionException {
		 this.log = Logger.getLogger(this.getClass());
	     String jobName = context.getJobDetail().getFullName();
	     this.log.info("LSRegisterJob.start name:"+jobName);
	     this.core = OSCARSNotifyCore.getInstance();
	     ServiceManager serviceMgr = this.core.getServiceManager();
	     JobDataMap dataMap = context.getJobDetail().getJobDataMap();
	     String nbURL = dataMap.getString("nbURL");
	     PSLookupClient client;
	     try {
	    	 client = new PSLookupClient();
	     } catch (LookupException e1) {
	    	 this.log.error(e1.getMessage());
	    	 return;
	     }
	     Session notify = this.core.getNotifySession();
	     PropHandler propHandler = new PropHandler("oscars.properties");
	     Properties props = propHandler.getPropertyGroup("external.service.lsRegister", true);
	     if(props.getProperty("renewTime") != null){
	    	 try{
	    		 RENEW_TIME = Long.parseLong(props.getProperty("renewTime"));
	         }catch(Exception e){}
	     }
	     
	     try {
	    	notify.beginTransaction();
			//Get local domain
	    	PublisherDAO pubDAO = new PublisherDAO(this.core.getNotifyDbName());
			ExternalServiceDAO extDAO = new ExternalServiceDAO(this.core.getNotifyDbName());
			List<ExternalService> hLSs = extDAO.getByType("LS");
			HashMap<String,String> serviceKeys = new HashMap<String,String>();
			HashMap<String,String> nodeKeys = new HashMap<String,String>();
			for(ExternalService hLS : hLSs){
				String url = hLS.getUrl();
				String keyStr = hLS.getServiceKey();
				if(keyStr == null){
					continue;
				}
				String[] keyPair = keyStr.split(";");
				serviceKeys.put(url, keyPair[0]);
				if(keyPair.length == 2){
					nodeKeys.put(url, keyPair[1]);
				}
			}
			List<Publisher> pubs = pubDAO.queryActive();
			ArrayList<String> pubURLs = new ArrayList<String>();
			for(Publisher pub : pubs){
				pubURLs.add(pub.getUrl());
			}
			HashMap<String,String> keys =client.registerNB(nbURL, pubURLs, serviceKeys, nodeKeys);
			this.saveKeys(keys);
			notify.getTransaction().commit();
	     } catch (Exception e) {
	    	 notify.getTransaction().rollback();
	    	 e.printStackTrace();
	     }finally{
	    	//Schedule next job
	         long nextJobTime = System.currentTimeMillis() + RENEW_TIME*1000;
	         serviceMgr.scheduleServiceJob(LSRegisterJob.class, dataMap, new Date(nextJobTime));
	         this.log.info("LSRegisterJob.end name:"+jobName);
	     }
	}
	
	private void saveKeys(HashMap<String, String> keys) {
     	OSCARSNotifyCore core = OSCARSNotifyCore.getInstance();
     	ExternalServiceDAO extDAO = new ExternalServiceDAO(core.getNotifyDbName());
 		for(String url : keys.keySet()){
 			ExternalService ext = extDAO.queryByParam("url", url);
 			if(ext == null || !(ext.getType().equals("LS"))){
 				ext = new ExternalService();
 				ext.setType("LS");
 				ext.setUrl(url);
 			}
 			ext.setServiceKey(keys.get(url));
 			extDAO.update(ext);
 		}
 	}

}