package net.es.oscars.scheduler;

import java.util.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
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

import edu.internet2.perfsonar.PSTopologyClient;

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
    private long RENEW_TIME = 60;//1 minute
    private String URL = "http://127.0.0.1:8089/perfSONAR_PS/services/topology";
    private boolean UPDATE_LOCAL = false;
    private boolean LS_UPDATE = false;
    private boolean makeDomainsOpaque = false;
    private static File tempFile = null;
    
    private final String TEMP_FILE_PREFIX = "OSCARS-topoReg";
    /**
     * Registers topology with the topology service
     *
     * @param context
     */
    public void execute(JobExecutionContext context) throws JobExecutionException {
        this.log = Logger.getLogger(this.getClass());
        String jobName = context.getJobDetail().getFullName();
        this.log.debug("TopologyRegisterJob.start name:"+jobName);
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
            boolean registered = false;

            //Send to perfsonar topology service
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
                    String domainXMLString = sw.toString();
                    //skip if no change in topology
                    if(!this.diff(domainXMLString)){ 
                        this.log.debug("No topology change since last update");
                        break; 
                    }
                    psClient.addReplaceDomain(domainXMLString);
                    registered = true;
                    break;
                }
            }
            
            //Update local OSCARS database
            if(UPDATE_LOCAL && registered){
                int startDomainCount = domainDAO.list().size();
                TopologyAxis2Importer topoImporter = new TopologyAxis2Importer(this.core.getBssDbName());
                topoImporter.updateDatabase(topology);
                int endDomainCount = domainDAO.list().size();
                //run LS update immediately if there are more domains
                if(startDomainCount < endDomainCount && LS_UPDATE){
                    LSDomainUpdateJob lsUpdateJob = new LSDomainUpdateJob();
                    lsUpdateJob.init();
                    lsUpdateJob.updateDB();
                }
            }
            
            if(registered){
                this.log.info("Topology update complete.");
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
        this.log.debug("TopologyRegisterJob.end name:"+jobName);
    }
    
    /**
     * Compares XML string to value stored in temporary file. Used to determine
     * if registration is required.
     * 
     * @param newDomainString the most recent domain as a string
     * @return true if there is a difference, false otherwise
     * @throws IOException
     */
    synchronized private boolean diff(String newDomainString) throws IOException{
        boolean isDiff = false;
        if(TopologyRegisterJob.tempFile == null || 
                (!TopologyRegisterJob.tempFile.exists())){
            //if no temp file then create one
            TopologyRegisterJob.tempFile = File.createTempFile(TEMP_FILE_PREFIX, "xml");
            this.log.debug("Created temp file " + 
                    TopologyRegisterJob.tempFile.getCanonicalPath());
            isDiff = true;
        }else{
            //if temp file then compare to most recent XML
            StringReader newReader = new StringReader(newDomainString);
            FileReader oldReader = new FileReader(TopologyRegisterJob.tempFile);

            /* If properly formated the below is sufficient for diff. 
             * Do not need to handle case where diferent sizes because 
             * closing tags will be different */
            char[] oldData = new char[1024];
            char[] newData = new char[1024];
            while((oldReader.read(oldData) != -1) && (newReader.read(newData) != -1)){
                String newStr = new String(newData);
                String oldStr = new String(oldData);
                if(!newStr.equals(oldStr)){
                    isDiff = true;
                    break;
                }
            }
            newReader.close();
            oldReader.close();
        }
        
        //If topology changes then update temp file
        if(isDiff){
            FileWriter fw = new FileWriter(TopologyRegisterJob.tempFile);
            fw.write(newDomainString);
            fw.close();
        }
        
        return isDiff;
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
        Properties props = propHandler.getPropertyGroup("external.service", true);
        Properties psProps = propHandler.getPropertyGroup("perfsonar", true);
        
        if(props.getProperty(ServiceManager.TOPOLOGY_REGISTER + ".renewTime") != null){
            try{
                RENEW_TIME = Long.parseLong(props.getProperty(
                        ServiceManager.TOPOLOGY_REGISTER + ".renewTime"));
            }catch(Exception e){}
        }
        
        if(props.getProperty(ServiceManager.TOPOLOGY_REGISTER + ".updateLocal") != null){
            UPDATE_LOCAL = "1".equals(props.getProperty(
                    ServiceManager.TOPOLOGY_REGISTER + ".updateLocal"));
        }
        
        for(int i = 1; props.getProperty(i+"") != null; i++){
            if(ServiceManager.LOOKUP_UPDATE.equals(props.getProperty(i+""))){
                LS_UPDATE = true;
            }
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
