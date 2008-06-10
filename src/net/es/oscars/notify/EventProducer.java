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
    private boolean connected;
    
    public EventProducer(){
        this.log = Logger.getLogger(this.getClass());
        this.connected = true;
        try{
            Registry registry = LocateRegistry.getRegistry(8090);
            this.remote = (RemoteEventProducer) 
                registry.lookup("OSCARSRemoteEventProducer");
        }catch(RemoteException e){
            this.connected = false;
            this.log.warn("Remote exception from RMI server: " + e);
        }catch(NotBoundException e){
            this.connected = false;
            this.log.warn("Trying to access unregistered remote object: " + e);
        }catch(Exception e){
            this.connected = false;
            this.log.warn(e.getMessage());
        }
    }
    
    public void addEvent(String type, String userLogin, String source,
        Reservation resv){
        this.addEvent(type, userLogin, source, resv, null, null);
    }
    
    public void addEvent(String type, String userLogin, String source,
        Reservation resv, String errorCode, String errorMessage){
        Event event = new Event();
        Reservation resvCopy = resv.copy();
        event.setType(type);
        event.setTimestamp(System.currentTimeMillis());
        event.setUserLogin(userLogin);
        event.setSource(source);
        event.setReservation(resvCopy);
        event.setErrorCode(errorCode);
        event.setErrorMessage(errorMessage);
        this.addEvent(event);
    }
    
    public void addEvent(Event event){
        //if RMI server connection failed then return
        if(!this.connected){
            return;
        }
        this.log.info("Adding event " + event.getType() + " to queue");
        try{
            remote.addEvent(event);
        }catch(RemoteException e){
            this.log.warn("Remote exception from RMI server: " + e);
        }catch(Exception e){
            this.log.warn("Cannot send event: " + e.getMessage());
        }
        this.log.info("Done adding event " + event.getType() + " to queue");
    }
}