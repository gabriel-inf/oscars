package net.es.oscars.rmi;

import java.rmi.RemoteException;

import net.es.oscars.rmi.aaa.AaaRmiClient;
import net.es.oscars.rmi.aaa.AaaRmiInterface;
import net.es.oscars.rmi.bss.BssRmiClient;
import net.es.oscars.rmi.bss.BssRmiInterface;

import org.apache.log4j.Logger;

public class RmiUtils {

    public static AaaRmiInterface getAaaRmiClient(String methodName, Logger log) throws RemoteException {
        AaaRmiInterface rmiClient;
        rmiClient = new AaaRmiClient();

        try {
            rmiClient.init();
        } catch (RemoteException ex) {
            log.error("could not init RMI client for method: "+methodName);
            log.error(ex);
            throw ex;
        }
        return rmiClient;
    }
    public static BssRmiInterface getBssRmiClient(String methodName, Logger log) throws RemoteException {
        BssRmiInterface rmiClient;
        rmiClient = new BssRmiClient();
        try {
            rmiClient.init();
        } catch (RemoteException ex) {
            log.error("could not init RMI client for method: "+methodName);
            log.error(ex);
            throw ex;
        }
        return rmiClient;
    }

}
