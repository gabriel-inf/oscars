package net.es.oscars.notify.ws.policy;

import java.util.ArrayList;
import java.util.HashMap;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.databinding.ADBException;
import org.apache.log4j.*;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMFactory;
import org.oasis_open.docs.wsn.b_2.*;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import net.es.oscars.notify.ws.OSCARSNotifyCore;
import net.es.oscars.aaa.*;
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
    private HashMap<String,String> namespaces;
    private UserManager userMgr;
    private String dbname;
    
    final public String ROOT_XPATH = "/idc:event";
    final public String USERLOGIN_XPATH = "/idc:event/idc:userLogin";
    final public String RESV_LOGIN_XPATH = "/idc:event/idc:resDetails/idc:login";
    
    public IDCEventPEP(){
        this.log = Logger.getLogger(this.getClass());
        this.namespaces = new HashMap<String,String>();
        this.namespaces.put("idc", "http://oscars.es.net/OSCARS");
        this.namespaces.put("nmwg-ctrlp", "http://ogf.org/schema/network/topology/ctrlPlane/20070626/");
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
        
        //TODO: Parse endpoints and enforce institution
        /*if(user != null){
            institutionList.add(user.getInstitution().getName());
            permissionMap.put("IDC_RESV_ENDSITE_INSTITUTION", institutionList);
        }*/
        
        this.log.debug("prepare.end");
        return permissionMap;
    }
}