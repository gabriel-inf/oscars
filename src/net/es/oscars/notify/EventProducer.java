package net.es.oscars.notify;

import java.io.PrintWriter;
import java.io.StringWriter;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;

import org.apache.log4j.*;

import net.es.oscars.bss.Reservation;

/**
 * EventProducer is used by the entity generating events to populate 
 * the EventQueue. EventProducer accesses the EventQueue through a 
 * the RemoteEventQueue class shared over RMI. This class hides the details of
 * RMI and adding the event to the queue from the event generating entity. It 
 * also contains many convenience methods for adding events to the queue.
 */
public class EventProducer{
    private Logger log;
    private RemoteEventProducer remote;
    private boolean connected;
    
    /**
     * Constructor that obtains RemoteEventProducer via RMI.
     */
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
    
    /**
     * Adds an event to the event queue.
     *
     * @param type the type of event.
     * @param userLogin the login of the user that triggered the event
     * @param source the entity that caused the event (API, WBUI, or SCHEDULER)
     * @param resv the reservation affected by this event
     */
    public void addEvent(String type, String userLogin, String source,
        Reservation resv){
        this.addEvent(type, userLogin, source, resv, null, null);
    }
    
    /**
     * Adds an event to the event queue.
     *
     * @param type the type of event.
     * @param userLogin the login of the user that triggered the event
     * @param source the entity that caused the event (API, WBUI, or SCHEDULER)
     * @param resv the reservation affected by this event
     * @param errorCode the error code of the event. null if no error.
     * @param errorMessage a message describing an error. null if no error.
     */
    public void addEvent(String type, String userLogin, String source,
        Reservation resv, String errorCode, String errorMessage){
        OSCARSEvent event = new OSCARSEvent();
        Reservation resvCopy = null;
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        try {
            resvCopy = resv.copy();
        } catch (Exception e) {
            this.log.info("caught exception");
            e.printStackTrace(pw);
            this.log.info(sw.toString());
        }
        event.setType(type);
        event.setTimestamp(System.currentTimeMillis());
        event.setUserLogin(userLogin);
        event.setSource(source);
        event.setReservation(resvCopy);
        event.setErrorCode(errorCode);
        event.setErrorMessage(errorMessage);
        this.addEvent(event);
    }
    
    /**
     * Adds an event to the event queue.
     *
     * @param event the event to add to the queue
     */
    public void addEvent(OSCARSEvent event){
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
