package net.es.oscars.notifybroker.policy;

import java.rmi.RemoteException;
import java.util.*;

import org.apache.log4j.*;
import org.jdom.Element;

import edu.internet2.perfsonar.utils.URNParser;

import net.es.oscars.notifybroker.ws.WSNotifyConstants;
import net.es.oscars.rmi.aaa.AaaRmiClient;
import net.es.oscars.rmi.notifybroker.NBValidator;
import net.es.oscars.aaa.AuthValue;


/**
 * IDCEventPEP extracts the user login from and idc:event message
 * and finds the institution.
 *
 * @author Andrew Lake (alake@internet2.edu)
 */
public class IDCEventPEP implements NotifyPEP{
    private Logger log;
    
    final public static String FILTER_RESV_USER = "IDC_RESV_USER";
    final public static String FILTER_RESV_ENDSITE = "IDC_RESV_ENDSITE_INSTITUTION";
    final public static String ALLOW_ALL_USERS = "ALL";
    
    public IDCEventPEP(){
        this.log = Logger.getLogger(this.getClass());
    }

    public void init(){
        return;
    }

    public boolean matches(List<String> topics){
        for(String topic : topics){
            if(topic.startsWith("idc:")){
                return true;
            }
        }
        return false;
    }

    public HashMap<String,List<String>> prepare(String subscriberLogin) throws RemoteException{
        HashMap<String,List<String>> permissionMap = new HashMap<String,List<String>>();
        String permissionKey = IDCEventPEP.FILTER_RESV_USER;
        List<String> permissionValue = new ArrayList<String>();
        AaaRmiClient aaaRmiClient = NBValidator.createAaaRmiClient(log);
        AuthValue authVal = aaaRmiClient.checkAccess(subscriberLogin, "Reservations", "query");
        if (authVal.equals(AuthValue.DENIED)) {
            throw new RemoteException("Subscriber " + subscriberLogin +
                    "does not have permission to view this notification.");
        }else if (authVal.equals(AuthValue.SELFONLY)){
            permissionValue.add(subscriberLogin);
        }else if (authVal.equals(AuthValue.MYSITE)) {
            String institution = aaaRmiClient.getInstitution(subscriberLogin);
            permissionKey = IDCEventPEP.FILTER_RESV_ENDSITE;
            permissionValue.add(institution);
        }else{
            permissionValue.add(IDCEventPEP.ALLOW_ALL_USERS);
        }
        permissionMap.put(permissionKey, permissionValue);
        
        return permissionMap;
    }

    public HashMap<String, List<String>> enforce(List<Element> events) throws RemoteException{
        HashMap<String, List<String>> permissionMap = new HashMap<String, List<String>>();
        for(Element event : events){
            HashMap<String, List<String>> tmpMap = this.enforce(event);
            if(tmpMap != null){
                permissionMap.putAll(tmpMap);
            }
        }
        return permissionMap;
    }

    public HashMap<String, List<String>> enforce(Element event) throws RemoteException{
        this.log.debug("enforce.start");
        HashMap<String, List<String>> permissionMap = new HashMap<String, List<String>>();
        ArrayList<String> userList = new ArrayList<String>();
        ArrayList<String> institutionList = new ArrayList<String>();
        HashMap<String,Boolean> domainInsts = new HashMap<String,Boolean>();
        String userLogin = null;
        String resvLogin = null;
        Element resDetails =  event.getChild("resDetails", WSNotifyConstants.IDC_NS);
        
        try{
            userLogin = event.getChildText("userLogin", WSNotifyConstants.IDC_NS);
            if(resDetails != null){
                resvLogin = resDetails.getChildText("login", WSNotifyConstants.IDC_NS);
            }
        }catch(Exception e){
            this.log.debug("notification not an idc:event");
            return permissionMap;
        }

        //Add user
        userList.add(IDCEventPEP.ALLOW_ALL_USERS);
        if(userLogin != null && (!userLogin.trim().equals(""))){
            this.log.debug("Adding user login '"+ userLogin + "'");
            userList.add(userLogin);
            this.addInst(userLogin, institutionList);
        }
        if(resvLogin != null && (!resvLogin.trim().equals("")) &&
                (!resvLogin.equals(userLogin))){
            this.log.debug("Adding resv login '"+ resvLogin + "'");
            userList.add(resvLogin);
            this.addInst(resvLogin, institutionList);
        }

        permissionMap.put(IDCEventPEP.FILTER_RESV_USER, userList);
        if(!institutionList.isEmpty()){
            permissionMap.put(IDCEventPEP.FILTER_RESV_ENDSITE, institutionList);
        }
        
        if(resDetails == null){
            this.log.debug("no resDetails");
            return permissionMap;
        }
        Element pathInfo = resDetails.getChild("pathInfo", WSNotifyConstants.IDC_NS);
        if(pathInfo == null){
            this.log.debug("no pathInfo");
            return permissionMap;
        }
        Element path = pathInfo.getChild("path", WSNotifyConstants.IDC_NS);
        if(path  == null){
            this.log.debug("no path");
            return permissionMap;
        }
        List<Element> hops = path.getChildren("hop", WSNotifyConstants.NMWG_CP);
        if(hops  == null || hops.isEmpty()){
            this.log.debug("no hops");
            return permissionMap;
        }

        //Check path for end site institutions that may have not yet been added
         try{
            for(Element hop : hops){
                Element link = hop.getChild("link", WSNotifyConstants.NMWG_CP);
                String linkIdRef = hop.getChildText("linkIdRef", WSNotifyConstants.NMWG_CP);
                String urn = null;
                if(link != null){
                    urn = link.getAttributeValue("id");
                }else if(linkIdRef != null){
                    urn = linkIdRef;
                }else{
                    this.log.debug("no urn");
                    continue;
                }
                this.addLinkInst(urn,institutionList, domainInsts);
            }
            if(!institutionList.isEmpty()){
                permissionMap.put(IDCEventPEP.FILTER_RESV_ENDSITE, institutionList);
            }
            this.log.debug("enforce.end");
        }catch(Exception e){
            e.printStackTrace();
            this.log.info("Ignoring error");
        }

        this.log.debug("enforce.end");
        return permissionMap;
    }

    private void addLinkInst(String urn, List<String> insts, HashMap<String,Boolean> domainInsts) throws RemoteException{
        //note this is the perfSONAR URNParser
        Hashtable<String, String> parseResults = URNParser.parseTopoIdent(urn);
        this.log.debug("urn=" + urn);
        String domainId = parseResults.get("domainValue");
        if(domainId == null || domainInsts.containsKey(domainId)){
            return;
        }
        
        AaaRmiClient aaaRmiClient = NBValidator.createAaaRmiClient(this.log);
        List<String> siteInsts = aaaRmiClient.getDomainInstitutions(domainId);
        if(siteInsts == null || siteInsts.isEmpty()){
            return;
        }
        domainInsts.put(domainId, true);
        for(String siteInst : siteInsts){
            if(insts.contains(siteInst)){
                continue;
            }
            insts.add(siteInst);
            this.log.debug("site=" + siteInst);
        }
    }

    private void addInst(String userName, List<String> insts) throws RemoteException{
        if(userName == null){ return; }
        AaaRmiClient aaaRmiClient = NBValidator.createAaaRmiClient(this.log);
        String institution = null;
        try {
            institution = aaaRmiClient.getInstitution(userName);
        } catch (RemoteException e) {
            this.log.debug("No institution found for user");
        }
        if(institution == null){ return; }
        insts.add(institution);
    }
}