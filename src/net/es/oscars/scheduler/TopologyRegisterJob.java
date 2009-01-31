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
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneNodeContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePortContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;
import edu.internet2.perfsonar.*;

import net.es.oscars.interdomain.ServiceManager;
import net.es.oscars.bss.OSCARSCore;
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
    private boolean makeDomainsOpaque = false;
    
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
        this.log.debug("Topology URL=" + URL);
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
                    if (this.makeDomainsOpaque) {
                        this.makeDomainOpaque(domain);
                    }

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
    * We iterate through the domain, blowing away the existing element children
    * and re-adding them if they are an external facing element.
    */
     private void makeDomainOpaque(CtrlPlaneDomainContent domain) {
        String domainId = domain.getId();

        if (domainId == null) {
            return;
        }

        List<CtrlPlaneNodeContent> newNodes = new ArrayList<CtrlPlaneNodeContent>();
 
        Iterator<CtrlPlaneNodeContent> nodeIter = Arrays.asList(domain.getNode()).iterator();
        while(nodeIter.hasNext()) {
            CtrlPlaneNodeContent node = nodeIter.next();

            List<CtrlPlanePortContent> newPorts = new ArrayList<CtrlPlanePortContent>();
            Iterator<CtrlPlanePortContent> portIter = Arrays.asList(node.getPort()).iterator();
            while(portIter.hasNext()) {
                CtrlPlanePortContent port = portIter.next();

                List<CtrlPlaneLinkContent> newLinks = new ArrayList<CtrlPlaneLinkContent>();

                Iterator<CtrlPlaneLinkContent> linkIter = Arrays.asList(port.getLink()).iterator();
                while(linkIter.hasNext()) {
                    CtrlPlaneLinkContent link = linkIter.next();

                    String localId = link.getId();
                    String remoteLinkId = link.getRemoteLinkId();

                    if (remoteLinkId == null || localId == null) {
                        continue;
                    }

                    Hashtable<String, String> remoteLinkURN = URNParser.parseTopoIdent(remoteLinkId);
                    if (remoteLinkURN.get("error") != null) {
                        continue;
                    }

                    if (remoteLinkURN.get("domainFQID").equals(domainId) == false) {
                        newLinks.add(link);
                    }
                }

                if (newLinks.isEmpty() == false) {
                    port.setLink(newLinks.toArray(new CtrlPlaneLinkContent[newLinks.size()]));
                    newPorts.add(port);
                }
            }

            if (newPorts.isEmpty() == false) {
                node.setPort(newPorts.toArray(new CtrlPlanePortContent[newPorts.size()]));
                newNodes.add(node);
            }
        }

        domain.setNode(newNodes.toArray(new CtrlPlaneNodeContent[newNodes.size()]));
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

        if(psProps.getProperty("domainOpacity") != null){
            String opacity = psProps.getProperty("domainOpacity");
            if (opacity.equals("complete")) {
                this.makeDomainsOpaque = true;
            } else if (opacity.equals("none")) {
                this.makeDomainsOpaque = false;
            } else {
                this.log.error("Unknown domain opacity for topology registration, "+opacity+", must be 'complete' or 'none'");
            }
        }
    }
    
 }
