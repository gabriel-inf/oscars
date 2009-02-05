package net.es.oscars.rmi.notifybroker;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.List;

import org.apache.log4j.Logger;

import net.es.oscars.aaa.AuthValue;
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
    
    public static void validateDestroyRegistration(String publisherId, 
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
