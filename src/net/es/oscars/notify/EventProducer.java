package net.es.oscars.notify;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;

import org.apache.log4j.*;

import net.es.oscars.bss.Reservation;

public class EventProducer{
    private Logger log;
    private RemoteEventProducer remote;
    
    public EventProducer() throws RemoteException, NotBoundException{
        this.log = Logger.getLogger(this.getClass());
        Registry registry = LocateRegistry.getRegistry(8090);
        this.remote = (RemoteEventProducer) 
            registry.lookup("OSCARSRemoteEventProducer");
    }
    
    public void addEvent(String type, String userLogin, Reservation resv)
        throws RemoteException{
        this.addEvent(type, userLogin, resv, null, null);
    }
    
    public void addEvent(String type, String userLogin, Reservation resv,
        String errorCode, String errorMessage) throws RemoteException{
        Event event = new Event();
        //initialize reservation
        resv.toString();
        event.setType(type);
        event.setTimestamp(System.currentTimeMillis());
        event.setUserLogin(userLogin);
        event.setReservation(resv);
        event.setErrorCode(errorCode);
        event.setErrorMessage(errorMessage);
        this.addEvent(event);
    }
    
    public void addEvent(Event event) throws RemoteException{
        this.log.info("Adding event " + event.getType() + " to queue");
        remote.addEvent(event);
        this.log.info("Done adding event " + event.getType() + " to queue");
    }
}