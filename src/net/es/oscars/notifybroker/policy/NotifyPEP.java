package net.es.oscars.notifybroker.policy;

import java.util.ArrayList;
import java.util.HashMap;
import org.apache.axiom.om.OMElement;

import net.es.oscars.notifybroker.ws.AAAFaultMessage;

import java.io.Serializable;

/**
 * NotificationBrokerPEP is an interface for writing policy enforcement points
 * that filter notification messages.
 *
 * @author Andrew Lake (alake@internet2.edu)
 */
public interface NotifyPEP extends Serializable{
    
    public void init(String dbname);
    
    public boolean matches(ArrayList<String> topics);
    
    public HashMap<String, String> prepare(String subscriberLogin) throws AAAFaultMessage;
    
    public HashMap<String, ArrayList<String>> enforce(OMElement[] messages) throws AAAFaultMessage;
    
}