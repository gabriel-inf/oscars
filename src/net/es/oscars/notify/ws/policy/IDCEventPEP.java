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
import net.es.oscars.notify.ws.AAAFaultMessage;

/**
 * IDCEventPEP extracts the user login from and idc:event message
 * and finds the institution.
 *
 * @author Andrew Lake (alake@internet2.edu)
 */
public class IDCEventPEP implements NotificationBrokerPEP{
    private Logger log;
    private HashMap<String,String> namespaces;
    
    final public String ROOT_XPATH = "/idc:event";
    final public String USERLOGIN_XPATH = "/idc:event/idc:userLogin";
    
    public IDCEventPEP(){
        this.log = Logger.getLogger(this.getClass());
        this.namespaces = new HashMap<String,String>();
        this.namespaces.put("idc", "http://oscars.es.net/OSCARS");
        this.namespaces.put("nmwg-ctrlp", "http://ogf.org/schema/network/topology/ctrlPlane/20070626/");
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
    
    public HashMap<String, ArrayList<String>> enforce(OMElement omMessage) throws AAAFaultMessage{
        HashMap<String, ArrayList<String>> permissionMap = new HashMap<String, ArrayList<String>>();
        ArrayList<String> userList = new ArrayList<String>();
        OMElement userLogin = null;
        try{
            AXIOMXPath xpathExpression = new AXIOMXPath(this.USERLOGIN_XPATH); 
            SimpleNamespaceContext nsContext = new SimpleNamespaceContext(this.namespaces);
            xpathExpression.setNamespaceContext(nsContext);
            userLogin = (OMElement) xpathExpression.selectSingleNode(omMessage);
        }catch(Exception e){
            e.printStackTrace();
            throw new AAAFaultMessage(e.getMessage());
        }
        
        userList.add("ALL");
        if(userLogin != null){
            this.log.info("Adding user login '"+ userLogin.getText() + "'");
            userList.add(userLogin.getText());
        }else{
            this.log.debug("Did not find user login in notify message!");
        }
        permissionMap.put("USERLOGIN", userList);
        
        return permissionMap;
    }
}