package net.es.oscars.rmi;

import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.HashMap;

import net.es.oscars.rmi.core.CoreRmiClient;
import net.es.oscars.rmi.core.CoreRmiInterface;

import org.apache.log4j.Logger;

public class RmiUtils {

    public static CoreRmiInterface getCoreRmiClient(String methodName, Logger log) throws RemoteException{
        CoreRmiInterface rmiClient;
        rmiClient = new CoreRmiClient();
        try {
            rmiClient.init();
        } catch (RemoteException ex) {
        	log.error(ex);
        	throw ex;
        }
        return rmiClient;
    }

    
}
