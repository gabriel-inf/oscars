package net.es.oscars.notify.ws.policy;

import java.util.*;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.databinding.ADBException;
import org.apache.log4j.*;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMFactory;
import org.hibernate.*;
import org.oasis_open.docs.wsn.b_2.*;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import net.es.oscars.notify.ws.OSCARSNotifyCore;
import net.es.oscars.aaa.*;
import net.es.oscars.bss.topology.*;
import net.es.oscars.notify.ws.AAAFaultMessage;
import net.es.oscars.aaa.UserManager.AuthValue;

/**
 * IDCEventPEP extracts the user login from and idc:event message
 * and finds the institution.
 *
 * @author Andrew Lake (alake@internet2.edu)
 */
public class IDCEventPEP implements NotifyPEP{
    private Logger log;
    private OSCARSNotifyCore core;
    private HashMap<String,String> namespaces;
    private UserManager userMgr;
    private String dbname;
    
    final public String ROOT_XPATH = "/idc:event";
    final public String USERLOGIN_XPATH = "/idc:event/idc:userLogin";
    final public String RESV_LOGIN_XPATH = "/idc:event/idc:resDetails/idc:login";
    final public String RESV_LINKS_XPATH = "/idc:event/idc:resDetails/idc:pathInfo/idc:path/nmwg-ctrlp:hop/nmwg-ctrlp:link";
    public IDCEventPEP(){
        this.log = Logger.getLogger(this.getClass());
        this.core = OSCARSNotifyCore.getInstance();
        this.namespaces = new HashMap<String,String>();
        this.namespaces.put("idc", "http://oscars.es.net/OSCARS");
        this.namespaces.put("nmwg-ctrlp", "http://ogf.org/schema/network/topology/ctrlPlane/20080828/");
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
    
    public HashMap<String, ArrayList<String>> enforce(OMElement omMessage) throws AAAFaultMessage{
        this.log.debug("prepare.start");
        HashMap<String, ArrayList<String>> permissionMap = new HashMap<String, ArrayList<String>>();
        ArrayList<String> userList = new ArrayList<String>();
        ArrayList<String> institutionList = new ArrayList<String>();
        UserDAO dao = new UserDAO(this.dbname);
        User user = null;
        OMElement userLogin = null;
        OMElement resvLoginElem = null;
        try{
            AXIOMXPath xpathExpression = new AXIOMXPath(this.RESV_LOGIN_XPATH); 
            SimpleNamespaceContext nsContext = new SimpleNamespaceContext(this.namespaces);
            xpathExpression.setNamespaceContext(nsContext);
            userLogin = (OMElement) xpathExpression.selectSingleNode(omMessage);
        }catch(Exception e){
            e.printStackTrace();
            throw new AAAFaultMessage(e.getMessage());
        }
        
        //Add user
        userList.add("ALL");
        if(userLogin != null){
            String userString = userLogin.getText();
            this.log.debug("Adding user login '"+ userString + "'");
            userList.add(userString);
            user = dao.query(userString);
        }else{
            this.log.debug("Did not find user login in notify message!");
        }
        permissionMap.put("IDC_RESV_USER", userList);
        
        Session bss = null;
        try{
            bss = this.core.getBssSession();
            bss.beginTransaction();
            AXIOMXPath xpathExpression = new AXIOMXPath(this.RESV_LINKS_XPATH); 
            SimpleNamespaceContext nsContext = new SimpleNamespaceContext(this.namespaces);
            xpathExpression.setNamespaceContext(nsContext);
            List<OMElement> links = (List<OMElement>) xpathExpression.selectNodes(omMessage);
            if(links == null){
                this.log.debug("prepare.end");
                return permissionMap;
            }
            HashMap<String,String> domainInsts = new HashMap<String,String>();
            for(OMElement omLink : links){
                CtrlPlaneLinkContent link = CtrlPlaneLinkContent.Factory.parse(omLink.getXMLStreamReaderWithoutCaching());
                String urn = link.getId();
                if(urn == null){
                    continue;
                }
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