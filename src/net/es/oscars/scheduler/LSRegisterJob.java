package net.es.oscars.scheduler;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import net.es.oscars.PropHandler;
import net.es.oscars.bss.OSCARSCore;
import net.es.oscars.bss.topology.Domain;
import net.es.oscars.bss.topology.DomainDAO;
import net.es.oscars.bss.topology.DomainService;
import net.es.oscars.bss.topology.DomainServiceDAO;
import net.es.oscars.interdomain.ServiceManager;
import net.es.oscars.lookup.PSLookupClient;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class LSRegisterJob  implements Job{
    private Logger log;
    private OSCARSCore core;
    private long RENEW_TIME = 3600;//60 minutes

    public void execute(JobExecutionContext context) throws JobExecutionException {
        this.log = Logger.getLogger(this.getClass());
        String jobName = context.getJobDetail().getFullName();
        this.log.info("LSRegisterJob.start name:"+jobName);
        this.core = OSCARSCore.getInstance();
        ServiceManager serviceMgr = this.core.getServiceManager();
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String idcURL = dataMap.getString("idcURL");
        PSLookupClient client = this.core.getLookupClient();
        Session bss = this.core.getBssSession();
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("external.service.lsRegister", true);
        Properties nbProps = propHandler.getPropertyGroup("notifybroker", true);
        String nbURL = nbProps.getProperty("url");
        if(nbURL == null){
            nbURL = idcURL + "Notify";
        }
        if(props.getProperty("renewTime") != null){
            try{
                RENEW_TIME = Long.parseLong(props.getProperty("renewTime"));
            }catch(Exception e){}
        }

        try {
            bss.beginTransaction();
            //Get local domain
            DomainDAO domainDAO = new DomainDAO(this.core.getBssDbName());
            DomainServiceDAO dsDAO = new DomainServiceDAO(this.core.getBssDbName());
            Domain localDomain = domainDAO.getLocalDomain();
            if(localDomain == null){
                this.log.debug("Please specify the local domain");
                return;
            }
            String domainId = "urn:ogf:network:domain=" + localDomain.getTopologyIdent();
            HashMap<String,String> serviceKeys = new HashMap<String,String>();
            HashMap<String,String> nodeKeys = new HashMap<String,String>();
            List<DomainService> hLSs = dsDAO.getServices(localDomain, "LS");
            for(DomainService hLS : hLSs){
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
            HashMap<String,String> keys = client.registerIDC(idcURL, domainId, nbURL, serviceKeys, nodeKeys);
            this.saveKeys(keys);
            bss.getTransaction().commit();
        } catch (Exception e) {
            bss.getTransaction().rollback();
            e.printStackTrace();
        }finally{
            //Schedule next job
            long nextJobTime = System.currentTimeMillis() + RENEW_TIME*1000;
            serviceMgr.scheduleServiceJob(LSRegisterJob.class, dataMap, new Date(nextJobTime));
            this.log.info("LSRegisterJob.end name:"+jobName);
        }
    }

    private void saveKeys(HashMap<String, String> keys) {
        //NOTE: it won't save the node key if this is the first time saving
        OSCARSCore core = OSCARSCore.getInstance();
        DomainDAO domainDAO = new DomainDAO(core.getBssDbName());
        DomainServiceDAO dsDAO = new DomainServiceDAO(core.getBssDbName());
        Domain localDomain = domainDAO.getLocalDomain();
        for(String url : keys.keySet()){
            DomainService ds = dsDAO.queryByParam("url", url);
            if(ds == null || !(ds.getType().equals("LS") && ds.getDomain().equals(localDomain))){
                ds = new DomainService();
                ds.setDomain(domainDAO.getLocalDomain());
                ds.setType("LS");
                ds.setUrl(url);
            }
            ds.setServiceKey(keys.get(url));
            dsDAO.update(ds);
        }
    }
}
