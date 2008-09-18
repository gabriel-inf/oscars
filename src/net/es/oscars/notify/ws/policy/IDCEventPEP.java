package net.es.oscars.notify.ws.policy;

import java.util.*;
import org.apache.axiom.om.OMElement;
import org.apache.log4j.*;
import org.hibernate.*;
import org.oasis_open.docs.wsn.b_2.*;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import net.es.oscars.notify.ws.OSCARSNotifyCore;
import net.es.oscars.aaa.*;
import net.es.oscars.bss.topology.*;
import net.es.oscars.notify.ws.AAAFaultMessage;
import net.es.oscars.aaa.UserManager.AuthValue;
import net.es.oscars.wsdlTypes.EventContent;


/**
 * IDCEventPEP extracts the user login from and idc:event message
 * and finds the institution.
 *
 * @author Andrew Lake (alake@internet2.edu)
 */
public class IDCEventPEP implements NotifyPEP{
    private Logger log;
    private OSCARSNotifyCore core;
    private UserManager userMgr;
    private String dbname;

    public IDCEventPEP(){
        this.log = Logger.getLogger(this.getClass());
        this.core = OSCARSNotifyCore.getInstance();
    }
    
    public void init(String dbname){
        this.userMgr = OSCARSNotifyCore.getInstance().getUserManager();
        this.dbname = dbname;
    }
    
    public boolean matches(ArrayList<String> topics){
        for(String topic : topics){
            if(topic.startsWith("idc:")){
                return true;
            }
        }
        return false;
    }
    
    public HashMap<String,String> prepare(String subscriberLogin) throws AAAFaultMessage{
        HashMap<String,String> permissionMap = new HashMap<String,String>();
        UserManager.AuthValue authVal = this.userMgr.checkAccess(subscriberLogin, "Reservations", "query");
        if (authVal.equals(AuthValue.DENIED)) {
            throw new AAAFaultMessage("Subscriber " + subscriberLogin +
                    "does not have permission to view this notification.");
        }else if (authVal.equals(AuthValue.SELFONLY)){
            permissionMap.put("IDC_RESV_USER", subscriberLogin);
        }else if (authVal.equals(AuthValue.MYSITE)) {
            String institution = this.userMgr.getInstitution(subscriberLogin);
            permissionMap.put("IDC_RESV_ENDSITE_INSTITUTION", institution);
        }else{
            permissionMap.put("IDC_RESV_USER", "ALL");
        }
        
        return permissionMap;
    }
    
    public HashMap<String, ArrayList<String>> enforce(OMElement[] omEvents) throws AAAFaultMessage{
        HashMap<String, ArrayList<String>> permissionMap = new HashMap<String, ArrayList<String>>();
        for(OMElement omEvent : omEvents){
            HashMap<String, ArrayList<String>> tmpMap = this.enforce(omEvent);
            if(tmpMap != null){
                permissionMap.putAll(tmpMap);
            }
        }
        return permissionMap;
    }
    
    public HashMap<String, ArrayList<String>> enforce(OMElement omEvent) throws AAAFaultMessage{
        this.log.debug("prepare.start");
        HashMap<String, ArrayList<String>> permissionMap = new HashMap<String, ArrayList<String>>();
        ArrayList<String> userList = new ArrayList<String>();
        ArrayList<String> institutionList = new ArrayList<String>();
        String userLogin = null;
        String resvLogin = null;
        EventContent event = null;
        try{
            event = EventContent.Factory.parse(omEvent.getXMLStreamReaderWithoutCaching());
            userLogin = event.getUserLogin();
            if(event.getResDetails() != null && event.getResDetails().getLogin() != null){
                resvLogin = event.getResDetails().getLogin();
            }
        }catch(Exception e){
            this.log.debug("notification not an idc:event");
            return permissionMap;
        }
        
        //Add user
        userList.add("ALL");
        if(userLogin != null && (!userLogin.trim().equals(""))){
            this.log.debug("Adding user login '"+ userLogin + "'");
            userList.add(userLogin);
        }
        if(resvLogin != null && (!resvLogin.trim().equals("")) &&
                (!resvLogin.equals(userLogin))){
            this.log.debug("Adding resv login '"+ resvLogin + "'");
            userList.add(resvLogin);
        }
        permissionMap.put("IDC_RESV_USER", userList);
        
        Session bss = null;
        try{
            bss = this.core.getBssSession();
            bss.beginTransaction();
            
            if(event.getResDetails() == null || 
               event.getResDetails().getPathInfo().getPath() == null ||
               event.getResDetails().getPathInfo().getPath().getHop() == null){
                this.log.debug("prepare.end");
                return permissionMap;   
            }
            CtrlPlaneHopContent[] hops = event.getResDetails().getPathInfo().getPath().getHop();
            HashMap<String,String> domainInsts = new HashMap<String,String>();
            for(CtrlPlaneHopContent hop : hops){
                CtrlPlaneLinkContent link = hop.getLink();
                if(link == null){
                    continue;
                }
                String urn = link.getId();
                this.addInst(urn,institutionList, domainInsts);
                this.addRemoteInst(urn,institutionList, domainInsts);
            }
            if(!institutionList.isEmpty()){
                permissionMap.put("IDC_RESV_ENDSITE_INSTITUTION", institutionList);
            }
            bss.getTransaction().commit();
        }catch(Exception e){
            bss.getTransaction().rollback();
            e.printStackTrace();
            this.log.info("Ignoring error");
        }
        
        this.log.debug("prepare.end");
        return permissionMap;
    }
    
    private void addInst(String urn, ArrayList<String> insts, HashMap<String,String> domainInsts){
        DomainDAO domainDAO = new DomainDAO(this.core.getBssDbName());
        Hashtable<String, String> parseResults = URNParser.parseTopoIdent(urn);
        this.log.debug("urn=" + urn);
        String domainId = parseResults.get("domainId");
        if(domainId == null || domainInsts.containsKey(domainId)){
            return;
        }
        Domain domain = domainDAO.fromTopologyIdent(domainId);
        if(domain == null){
            return;
        }
        Site site = domain.getSite();
        if(site == null){
            return;
        }
        domainInsts.put(domainId, site.getName());
        insts.add(site.getName());
        this.log.debug("site=" + site.getName());
    }
    
    private void addRemoteInst(String urn, ArrayList<String> insts, HashMap<String,String> domainInsts){
        DomainDAO domainDAO = new DomainDAO(this.core.getBssDbName());
        Link link = domainDAO.getFullyQualifiedLink(urn);
        if(link == null){
            return;
        }
        Link remoteLink = link.getRemoteLink();
        if(remoteLink == null){
            return;
        }
        Domain remoteDomain = remoteLink.getPort().getNode().getDomain();
        if(domainInsts.containsKey(remoteDomain.getTopologyIdent())){
            return;
        }
        Site site = remoteDomain.getSite();
        if(site == null){
            return;
        }
        domainInsts.put(remoteDomain.getTopologyIdent(), site.getName());
        insts.add(site.getName());
        this.log.debug("site=" + site.getName());
    }
}