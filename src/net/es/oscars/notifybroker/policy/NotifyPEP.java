package net.es.oscars.notifybroker.policy;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;

import java.io.Serializable;

import org.jdom.Element;

/**
 * NotificationBrokerPEP is an interface for writing policy enforcement points
 * that filter notification messages.
 *
 * @author Andrew Lake (alake@internet2.edu)
 */
public interface NotifyPEP extends Serializable{
    
    public void init();
    
    public boolean matches(List<String> topics);
    
    public HashMap<String, List<String>> prepare(String subscriberLogin) throws RemoteException;
    
    public HashMap<String, List<String>> enforce(List<Element> messages) throws RemoteException;
    
}