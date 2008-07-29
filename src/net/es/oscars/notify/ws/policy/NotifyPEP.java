package net.es.oscars.notify.ws.policy;

import java.util.ArrayList;
import java.util.HashMap;
import org.apache.axiom.om.OMElement;
import net.es.oscars.notify.ws.AAAFaultMessage;

/**
 * NotificationBrokerPEP is an interface for writing policy enforcement points
 * that filter notification messages.
 *
 * @author Andrew Lake (alake@internet2.edu)
 */
public interface NotifyPEP{
    
    public void init(String dbname);
    
    public boolean matches(OMElement message);
    
    public HashMap<String, ArrayList<String>> enforce(OMElement message) throws AAAFaultMessage;
}