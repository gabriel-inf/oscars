package net.es.oscars.scheduler;

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import net.es.oscars.PropHandler;
import net.es.oscars.bss.OSCARSCore;
import net.es.oscars.bss.topology.Domain;
import net.es.oscars.bss.topology.DomainDAO;
import net.es.oscars.bss.topology.DomainService;
import net.es.oscars.bss.topology.DomainServiceDAO;
import net.es.oscars.interdomain.ServiceManager;
import net.es.oscars.lookup.LookupException;
import net.es.oscars.lookup.PSLookupClient;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import edu.internet2.perfsonar.dcn.DCNLookupClient;

public class LSDomainUpdateJob implements Job{
	private Logger log;
	private OSCARSCore core;
	private long REFRESH_TIME = 4200;//70 minutes
	
	public void execute(JobExecutionContext context) throws JobExecutionException {
		 this.log = Logger.getLogger(this.getClass());
	     String jobName = context.getJobDetail().getFullName();
	     this.log.debug("LSDomainUpdateJob.start name:"+jobName);
	     this.core = OSCARSCore.getInstance();
	     ServiceManager serviceMgr = this.core.getServiceManager();
	     JobDataMap dataMap = context.getJobDetail().getJobDataMap();
	     DCNLookupClient client = this.core.getLookupClient().getClient();
	     PropHandler propHandler = new PropHandler("oscars.properties");
	     Properties props = propHandler.getPropertyGroup("external.service.lsDomainUpdate", true);
	     if(props.getProperty("refreshTime") != null){
	    	 try{
	    		 REFRESH_TIME = Long.parseLong(props.getProperty("refreshTime"));
	         }catch(Exception e){}
	     }
	     
	     Session bss = this.core.getBssSession();
	     try {
			bss.beginTransaction();
			DomainDAO domainDAO = new DomainDAO(this.core.getBssDbName());
			DomainServiceDAO dsDAO = new DomainServiceDAO(this.core.getBssDbName());
			List<Domain> domains = domainDAO.list();
			for(Domain domain : domains){
				String url = domain.getUrl();
				String domainId = "urn:ogf:network:domain=";
				if(domain.isLocal() || url.toLowerCase().equals("nan")){ 
					this.log.debug("Skipping local domain or non-neigbor");
					continue; 
				}
				domainId += domain.getTopologyIdent();
				String[] urls = client.lookupIDCUrl(domainId);
				if(urls != null && urls.length > 0){
					for(String newUrl : urls){
						try{ new URL(newUrl); }catch(Exception e){ continue; }
						this.log.debug("domain.idc."+domainId+".url=" + newUrl);
						domain.setUrl(newUrl);
						domainDAO.update(domain);
						break;
					}
				}
				String[] nbUrls = client.lookupNBUrl(domain.getUrl());
				if(nbUrls == null || nbUrls.length == 0){ continue; }
				List<DomainService> dsList = dsDAO.getServices(domain, "NB");
				DomainService ds = null;
				if(dsList != null && (!dsList.isEmpty())){
					ds = dsList.get(0);
				}else{
					this.log.debug("Creating domain service for " + domainId);
					ds = new DomainService();
					ds.setType("NB");
					ds.setDomain(domain);
				}
				for(String newNbUrl : nbUrls){
					try{ new URL(newNbUrl); }catch(Exception e){ continue; }
					ds.setUrl(newNbUrl);
					break;
				}
				dsDAO.update(ds);
				this.log.debug("domainService.nb." + domainId + ".url=" + ds.getUrl());
			}
			bss.getTransaction().commit();
	     } catch (Exception e) {
	    	 bss.getTransaction().rollback();
	    	 e.printStackTrace();
	     }finally{
	    	//Schedule next job
	         long nextJobTime = System.currentTimeMillis() + REFRESH_TIME*1000;
	         serviceMgr.scheduleServiceJob(LSDomainUpdateJob.class, dataMap, new Date(nextJobTime));
	         this.log.debug("LSDomainUpdateJob.end name:"+jobName);
	     }
	}
}
