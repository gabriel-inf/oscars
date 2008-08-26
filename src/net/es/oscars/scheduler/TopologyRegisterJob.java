package net.es.oscars.scheduler;

import java.util.*;
import java.io.StringWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.quartz.*;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axis2.databinding.utils.writer.MTOMAwareXMLSerializer;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneTopologyContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneDomainContent;

import net.es.oscars.oscars.OSCARSCore;
import net.es.oscars.interdomain.ServiceManager;
import net.es.oscars.perfsonar.*;
import net.es.oscars.bss.topology.*;
import net.es.oscars.tss.*;
import net.es.oscars.PropHandler;

/**
 * Job that registers topology with the perfSONAR Topology service
 */
public class TopologyRegisterJob implements Job{
    private Logger log;
    private OSCARSCore core;
    private long RENEW_TIME = 1800;//30 minutes
    private String URL = "http://127.0.0.1:8089/perfSONAR_PS/services/topology";
    
    /**
     * Registers topology with the topology service
     *
     * @param context
     */
    public void execute(JobExecutionContext context) throws JobExecutionException {
        this.log = Logger.getLogger(this.getClass());
        String jobName = context.getJobDetail().getFullName();
        this.log.info("TopologyRegisterJob.start name:"+jobName);
        this.core = OSCARSCore.getInstance();
        TopologyExchangeManager texManager = this.core.getTopologyExchangeManager();
        ServiceManager serviceMgr = this.core.getServiceManager();
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        this.init();
        PSTopologyClient psClient = new PSTopologyClient(URL);
        
        Session bss = this.core.getBssSession();
        bss.beginTransaction();
        try{
            TEDB tedb = texManager.getTEDB();
            CtrlPlaneTopologyContent topology = tedb.selectNetworkTopology("all");
            CtrlPlaneDomainContent[] domains = topology.getDomain();
            DomainDAO domainDAO = new DomainDAO(this.core.getBssDbName());
            for(CtrlPlaneDomainContent domain : domains){
                Hashtable<String, String> parseResults = 
                                    URNParser.parseTopoIdent(domain.getId());
                String domainId = parseResults.get("domainId");
                Domain dbDomain = domainDAO.fromTopologyIdent(domainId);
                if(dbDomain != null && dbDomain.isLocal()){
                    StringWriter sw = new StringWriter();
                    XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
                    MTOMAwareXMLSerializer mtom = new MTOMAwareXMLSerializer(writer);
                    domain.serialize(org.ogf.schema.network.topology.ctrlplane.Domain.MY_QNAME, 
                                     OMAbstractFactory.getOMFactory(), mtom);
                    mtom.flush();
                    psClient.addReplaceDomain(sw.toString());
                    break;
                }
            }
            bss.getTransaction().commit();
        }catch(Exception e){
            e.printStackTrace();
            this.log.error(e);
            bss.getTransaction().rollback();
        }
        
        //Schedule next job
        long nextJobTime = System.currentTimeMillis() + RENEW_TIME*1000;
        serviceMgr.scheduleServiceJob(TopologyRegisterJob.class, dataMap, new Date(nextJobTime));
        this.log.info("TopologyRegisterJob.end name:"+jobName);
    }
    
    /**
     * Initializes the job and loads values from oscars.properties into global variables
     */
    private void init(){
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("external.service.topology", true);
        Properties psProps = propHandler.getPropertyGroup("perfsonar", true);
        
        if(props.getProperty("renewTime") != null){
            try{
                RENEW_TIME = Long.parseLong(props.getProperty("renewTime"));
            }catch(Exception e){}
        }
        
        if(psProps.getProperty("topology_url") != null){
            try{
                URL = psProps.getProperty("topology_url");
            }catch(Exception e){}
        }
    }
    
 }