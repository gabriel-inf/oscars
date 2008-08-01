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
    private String resvLogin;
    private String resvInstitution;
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
        this.resvLogin = null;
        this.resvInstitution = null;
    }
    
    public boolean matches(OMElement message){
        boolean retVal = false;
        try{
            AXIOMXPath xpathExpression = new AXIOMXPath(this.ROOT_XPATH); 
            SimpleNamespaceContext nsContext = new SimpleNamespaceContext(this.namespaces);
            xpathExpression.setNamespaceContext(nsContext);
            retVal = xpathExpression.booleanValueOf(message);
         }catch(Exception e){
            this.log.error(e); 
         }
         
         return retVal;
    }
    
    public HashMap<String, ArrayList<String>> prepare(OMElement omMessage) throws AAAFaultMessage{
        this.log.debug("prepare.start");
        HashMap<String, ArrayList<String>> permissionMap = new HashMap<String, ArrayList<String>>();
        ArrayList<String> userList = new ArrayList<String>();
        ArrayList<String> institutionList = new ArrayList<String>();
        UserDAO dao = new UserDAO(this.dbname);
        User user = null;
        OMElement userLogin = null;
        OMElement resvLoginElem = null;
        try{
            AXIOMXPath xpathExpression = new AXIOMXPath(this.USERLOGIN_XPATH); 
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
            this.log.info("Adding user login '"+ userString + "'");
            userList.add(userString);
            user = dao.query(userString);
        }else{
            this.log.debug("Did not find user login in notify message!");
        }
        permissionMap.put("USERLOGIN", userList);
        
        //Add institution
        if(user != null){
            institutionList.add(user.getInstitution().getName());
            permissionMap.put("INSTITUTION", institutionList);
        }
        
        //pre-load reservation login
        try{
            AXIOMXPath xpathExpression = new AXIOMXPath(this.USERLOGIN_XPATH); 
            SimpleNamespaceContext nsContext = new SimpleNamespaceContext(this.namespaces);
            xpathExpression.setNamespaceContext(nsContext);
            resvLoginElem = (OMElement) xpathExpression.selectSingleNode(omMessage);
        }catch(Exception e){
            e.printStackTrace();
            throw new AAAFaultMessage(e.getMessage());
        }
        if(resvLoginElem != null && resvLoginElem.getText() != null){
            this.resvLogin = resvLoginElem.getText();
            this.log.info("Reservation login '"+ this.resvLogin + "'"); 
        }else{
            this.log.debug("Did not find reservation login in notify message!");
            return permissionMap;
        }
        
        User resvUser = dao.query(this.resvLogin);
	    if(resvUser == null){
	        this.log.debug("Did not find reservation user in database!");
	    }else{
	        this.resvInstitution = resvUser.getInstitution().getName();
        }
        
        this.log.debug("prepare.end");
        return permissionMap;
    }
    
    public void enforce(String subscriberLogin, OMElement omMessage) throws AAAFaultMessage{
        this.log.debug("enforce.start:subscriber=" + subscriberLogin);
        UserDAO userDAO = new UserDAO(this.dbname);
	    User user = userDAO.query(subscriberLogin);
	    if(user == null){
	        throw new AAAFaultMessage("Subscriber " + subscriberLogin +
                    "not found in database.");
	    }
        UserManager.AuthValue authVal = this.userMgr.checkAccess(subscriberLogin, "Reservations", "query");
        if (authVal.equals(AuthValue.DENIED)) {
            throw new AAAFaultMessage("Subscriber " + subscriberLogin +
                    "does not have permission to view this notification.");
        }
        if(authVal.equals(AuthValue.ALLUSERS)){
            this.log.debug("enforce.end: allusers=1");
            return;
        }
        if(this.resvLogin != null && subscriberLogin.equals(this.resvLogin)){
            this.log.debug("enforce.end: owner=1");
            return;
        }
        String subInstitution = this.userMgr.getInstitution(subscriberLogin);
        if (this.resvInstitution != null && authVal.equals(AuthValue.MYSITE)
            && subInstitution.equals(this.resvInstitution)) {
            this.log.debug("enforce.end: siteadmin=1");
            return;
        }
        throw new AAAFaultMessage("Subscriber " + subscriberLogin +
                    "does not have permission to view this notification.");
        
    }
}