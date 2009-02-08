package net.es.oscars.rmi.notifybroker;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Element;

import net.es.oscars.aaa.AuthValue;
import net.es.oscars.notifybroker.NotifyBrokerCore;
import net.es.oscars.notifybroker.policy.NotifyPEP;
import net.es.oscars.rmi.aaa.AaaRmiClient;

public class NBValidator {
    public static void validateRegisterPublisher(String publisherUrl, List<String> topics,
            Boolean demand, Long termTime, String user, Logger log) throws RemoteException{
        
        //Check user parameters
        if(publisherUrl == null){
            throw new RemoteException("Required argument publisherUrl is null");
        }
        try {
            new URL(publisherUrl);
        } catch (MalformedURLException e) {
            throw new RemoteException("Invalid publisher URL provided");
        }
        if(demand!= null && demand){
            throw new RemoteException("Demand publishing not currently supported");
        }
        if(user == null){
            throw new RemoteException("Required argument user is null");
        }
        
        //Check user permissions
        AaaRmiClient aaaRmiClient = NBValidator.createAaaRmiClient(log);
        AuthValue authVal = aaaRmiClient.checkAccess(user, "Publishers", "create");
        if (authVal.equals(AuthValue.DENIED)) {
            String msg = "RegisterPublisher access denied because " +
            "user " + user + " does not have create permissons for " +
            "Publisher resources.";
            log.error(msg);
            throw new RemoteException(msg);
        }
    }
    
    public static HashMap<String, List<String>> validateNotify(String publisherUrl, String publisherRegId, 
            List<String> topics, List<Element> msg, Logger log) 
            throws RemoteException{
        
        if(publisherUrl == null){
            throw new RemoteException("Required argument publisherUrl is null");
        }
        try {
            new URL(publisherUrl);
        } catch (MalformedURLException e) {
            throw new RemoteException("Invalid publisher URL provided");
        }
        
        if(publisherRegId == null){
            throw new RemoteException("Required argument publisherRegId is null");
        }
        
        if(topics == null || topics.isEmpty()){
            throw new RemoteException("Required argument topics is null or empty");
        }
        
        if(msg == null || msg.size() < 1){
            throw new RemoteException("Required argument msg is null or empty");
        }
        for(Element msgElem : msg){
            //Call detach because it used to have a parent when controlled by Axis2
            msgElem.detach();
        }
        
        //Check for message specific constraints
        HashMap<String,List<String>> pepMap = new HashMap<String,List<String>>();
        NotifyBrokerCore core = NotifyBrokerCore.getInstance();
        for(NotifyPEP notifyPep : core.getNotifyPEPs()){
            if(!notifyPep.matches(topics)){
                continue;
            }
            HashMap<String,List<String>> tmpPepMap = notifyPep.enforce(msg);
            if(tmpPepMap != null){
                pepMap.putAll(tmpPepMap);
            }
        }
        
        return pepMap;
    }
    
    public static void validateSubscribe(String consumerUrl, Long termTime,
            HashMap<String, List<String>> filters, String user, Logger log) 
            throws RemoteException{
        if(consumerUrl == null){
            throw new RemoteException("Required argument consumerUrl is null");
        }
        try {
            new URL(consumerUrl);
        } catch (MalformedURLException e) {
            throw new RemoteException("Invalid consumer URL provided");
        }
        
        if(filters == null){
            throw new RemoteException("Required argument filters is null");
        }
        
        if(user == null){
            throw new RemoteException("Required argument user is null");
        }
        
        //Check user permissions
        AaaRmiClient aaaRmiClient = NBValidator.createAaaRmiClient(log);
        AuthValue authVal = aaaRmiClient.checkAccess(user, "Subscriptions", "create");
        if (authVal.equals(AuthValue.DENIED)) {
            String msg = "You do not have permission to create subscriptions.";
            log.error(msg);
            throw new RemoteException(msg);
        }
        
        //Further constrain user on topic specific values.
        List<String> topics = filters.get("TOPIC");
        NotifyBrokerCore core = NotifyBrokerCore.getInstance();
        for(NotifyPEP notifyPep : core.getNotifyPEPs()){
            if(!notifyPep.matches(topics)){
                continue;
            }
            HashMap<String,List<String>> pepMap = notifyPep.prepare(user);
            if(pepMap != null){
                filters.putAll(pepMap);
            }
        }
    }
    
    public static AuthValue validateSubscriptionMod(String subscriptionId,
            String user, Logger log) throws RemoteException{
        if(subscriptionId == null){
            throw new RemoteException("Required argument subscriptionId not provided");
        }
        if(user == null){
            throw new RemoteException("Required argument user not provided");
        }
        //Check user permissions
        AaaRmiClient aaaRmiClient = NBValidator.createAaaRmiClient(log);
        AuthValue authVal = aaaRmiClient.checkAccess(user, "Subscriptions", "modify");
        if (authVal.equals(AuthValue.DENIED)) {
            String msg = "User " + user + " does not have modify permissons for " +
            "Subscription resources.";
            log.error(msg);
            throw new RemoteException(msg);
        }
        
        return authVal;
    }
    
    public static AuthValue validateDestroyRegistration(String publisherId, 
            String user, Logger log) throws RemoteException{
        if(publisherId == null){
            throw new RemoteException("Required argument publisherId is null");
        }
        
        if(user == null){
            throw new RemoteException("Required argument user is null");
        }
        
        //Check user permissions
        AaaRmiClient aaaRmiClient = NBValidator.createAaaRmiClient(log);
        AuthValue authVal = aaaRmiClient.checkAccess(user, "Publishers", "modify");
        if (authVal.equals(AuthValue.DENIED)) {
            String msg = "DestroyRegistration access denied because " +
            "user " + user + " does not have modify permissons for " +
            "Publisher resources.";
            log.error(msg);
            throw new RemoteException(msg);
        }
        
        return authVal;
    }
    
        
    public static AaaRmiClient createAaaRmiClient(Logger log) throws RemoteException{
        AaaRmiClient aaaRmiClient = new AaaRmiClient();
        try {
            aaaRmiClient.init();
        } catch (RemoteException e) {
            log.error("Verify that your aaa-core is running!");
            throw new RemoteException("The server was unable " +
                        "to reach an AAA service to authenticate your request");
        }
        return aaaRmiClient;
    }
}
