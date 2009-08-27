package net.es.oscars.bss.policy;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.databinding.ADBException;
import org.hibernate.Session;

import edu.internet2.perfsonar.dcn.DCNLookupClient;

import net.es.oscars.ConfigFinder;
import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.BssUtils;
import net.es.oscars.bss.OSCARSCore;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.topology.Domain;
import net.es.oscars.bss.topology.DomainDAO;
import net.es.oscars.bss.topology.Path;
import net.es.oscars.bss.topology.PathElem;
import net.es.oscars.bss.topology.PathType;
import net.es.oscars.bss.topology.URNParser;
import net.es.oscars.policy.axis2.Axis2PolicyClient;
import net.es.oscars.policy.client.CheckPolicyRequest;
import net.es.oscars.policy.client.CheckPolicyResponse;
import net.es.oscars.policy.client.PolicyClientException;
import net.es.oscars.policy.client.Requester;
import net.es.oscars.ws.WSDLTypeConverter;
import net.es.oscars.wsdlTypes.ReservationResource;
import net.es.oscars.wsdlTypes.ReservationResourceType;

public class PolicyClient extends Axis2PolicyClient{
    private boolean activated;
    private String policyServiceUrl;
    private String localDomain;
    private int level;
    
    final public static String CREATE_ACTION_URN = "urn:dcn:oscars:action:createReservation";
    final public static String MODIFY_ACTION_URN = "urn:dcn:oscars:action:modifyReservation";
    final public static int PATH_COMP_LEVEL = 1;
    final public static int INTER_LEVEL = 2;
    final public static int SUBMIT_LEVEL = 3;
    
    public PolicyClient(Properties props) throws BSSException{
        //init configuration map and logger
        super();
       
        OSCARSCore core = OSCARSCore.getInstance();
        String useService = props.getProperty("useService");
        if(useService == null || (!"1".equals(useService))){
            this.log.debug("policy service client off");
            this.activated = false;
            return;
        }
        this.log.debug("policy service client activated");
        
        this.policyServiceUrl = props.getProperty("service.url");
        if(this.policyServiceUrl == null){
            throw new BSSException("Cannot use policy service because no URL" +
                    " specified. Please set 'policy.service.url' " +
                    "in oscars.properties.");
        }
        this.log.debug("policy.service.url=" + this.policyServiceUrl);
        
        this.level = 1;
        String strLevel = props.getProperty("level");
        if(strLevel != null){
            try{
                this.level = Integer.parseInt(strLevel);
            }catch(Exception e){
                this.log.warn("policy.level is not a number. defaulting to 1");
            }
        }
        
        try {
            new URL(this.policyServiceUrl);
        } catch (MalformedURLException e) {
            throw new BSSException("'policy.service.url' in " +
                    "oscars.properties is not a alid URL: " + e.getMessage());
        }
        
        //set local domain
        Session bss = core.getBssSession();
        bss.beginTransaction();
        try{
            DomainDAO domainDAO = new DomainDAO(core.getBssDbName());
            this.localDomain= domainDAO.getLocalDomain().getTopologyIdent();
            bss.getTransaction().commit();
        }catch(Exception e){
            bss.getTransaction().rollback();
        }
        
        //Set Axis2 configuration
        ConfigFinder configFinder = ConfigFinder.getInstance();
        try {
            String axis2Xml = configFinder.find(ConfigFinder.AXIS_TOMCAT_DIR, "axis2-norampart.xml");
            String repo = (new File(axis2Xml)).getParent();
            this.configuration.put(Axis2PolicyClient.AXIS2_CONFIG, axis2Xml);
            this.configuration.put(Axis2PolicyClient.REPO_CONFIG, repo);
        } catch (RemoteException e) {
            throw new BSSException(e.getMessage());
        }
        
        this.activated = true;
    }
    
    public void checkPolicy(String login, String action, Reservation resv, int level) throws BSSException{
        this.checkPolicy(login, action, resv, null, level);
    }
    
    public void checkPolicy(String login, String action, Reservation resv, Reservation modifyResv, int level) throws BSSException{
        this.log.debug("checkPolicy.start");
        //return if admin settings dictate check is not needed
        if(this.level < level){
            return;
        }
        
        CheckPolicyRequest<OMElement> request = this.createRequest();
        ArrayList<String> domainPath = new ArrayList<String>();
        boolean localFound = false;
        
        //Try to convert login to X.509
        String x509Subj = BssUtils.lookupX509Subj(login);
        login = (x509Subj != null ? x509Subj : login);
        
        //TODO: will break if domain appears in path multiple times
        //find ordered list of domains before local domains
        Path path = resv.getPath(PathType.INTERDOMAIN);
        if(path == null){
            path = BssUtils.getPath(resv);
        }
        for(PathElem elem : path.getPathElems()){
            String domainId = URNParser.parseTopoIdent(elem.getUrn()).get("domainId");
            if(this.localDomain.equals(domainId)){
                localFound = true;
                break;
            }
            if(domainPath.isEmpty() || (!domainPath.get(domainPath.size() - 1).equals(domainId))){
                domainPath.add(domainId);
            }
        }
        
        //if no local domain in the path then assume 
        //local domain is next to last in path
        if(!localFound){
            domainPath.remove(domainPath.size()-1);
            domainPath.add(this.localDomain);
        }
        
        if(resv.getPayloadSender() == null && (!domainPath.isEmpty())){
            throw new BSSException("Cannot apply policy because previous " +
                    "domain did not forward the payloadSender");
        }else if(!domainPath.isEmpty()){
            //if multiple domains add originator and previous domain
            request.getRequesters().add(this.createRequester(resv.getPayloadSender(), domainPath.get(0)));
            request.getRequesters().add(this.createRequester(login, domainPath.get(domainPath.size()-1)));
        }else{
          //if one domain add requester
            request.getRequesters().add(this.createRequester(login, this.localDomain));
        }
        
        //set action
        request.setAction(action);
        
        //convert resv to WSDL
        ReservationResourceType resvResourceType = WSDLTypeConverter.reservationToResource(resv, localDomain);
        if(modifyResv != null){
            this.log.debug("modifying times");
            resvResourceType.setStartTime(modifyResv.getStartTime());
            resvResourceType.setEndTime(modifyResv.getEndTime());
        }
        try {
            request.getResources().add(
                    resvResourceType.getOMElement(ReservationResource.MY_QNAME,
                            OMAbstractFactory.getOMFactory()));
        } catch (ADBException e) {
            throw new BSSException(e.getMessage());
        }
        
        //Send request
        CheckPolicyResponse response = null;
        try {
            response =  this.checkPolicy(this.policyServiceUrl, request);
            this.log.debug("response.allow=" +response.getAllow());
            this.log.debug("response.reason=" +response.getReason());
        } catch (PolicyClientException e) {
            throw new BSSException(e.getMessage());
        }
        
        if(!response.getAllow()){
            throw new BSSException("Request denied based on policy: " + 
                    (response.getReason() == null ? "No reason given.": 
                        response.getReason()));
        }
        this.log.debug("checkPolicy.end");
    }
    
    private Requester createRequester(String name, String domainId) throws BSSException {
        Requester requester = null;
        try {
            requester = new Requester();
        } catch (PolicyClientException e) {
            throw new BSSException(e.getMessage());
        }
        
        String qualifier = null;
        if(!name.contains(",")){
            qualifier = domainId;
        }
        requester.setSubject(name, qualifier);
        
        DomainDAO domainDAO = new DomainDAO(OSCARSCore.getInstance().getBssDbName());
        Domain domain = domainDAO.fromTopologyIdent(domainId);
        if(domain == null){
            //Try to find domain URL using LS
            try {
                DCNLookupClient psClient = OSCARSCore.getInstance().getLookupClient().getClient();
                String[] idcUrl = psClient.lookupIDCUrl(domainId);
                if(idcUrl != null){
                    requester.setSubjectAuthentication(idcUrl[0]);
                }
            } catch (Exception e) {
                this.log.debug("No SubjectAuthentication url found");
            }
        }else{
            requester.setSubjectAuthentication(domain.getUrl());
        }
        
        return requester;
    }
    
    public boolean isActivated(){
        return this.activated;
    }
}
