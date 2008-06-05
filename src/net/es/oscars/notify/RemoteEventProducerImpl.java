package net.es.oscars.notify;

import java.rmi.RemoteException;
import org.apache.log4j.*;


public class RemoteEventProducerImpl implements RemoteEventProducer{
    private Logger log;
    
    public RemoteEventProducerImpl(){
        this.log = Logger.getLogger(this.getClass());
    }
    
    public void addEvent(Event event) throws RemoteException{
        this.log.info("Adding event " + event.getType() + " to queue");
        try{
            EventQueue.put(event);
            this.log.info("Done adding event " + event.getType() + 
                " to queue");
        }catch(InterruptedException e){
            this.log.warn("Interruption occurred before event " +
                "added to queue: " + e.getMessage());
        }catch(NullPointerException e){
            this.log.warn("Cannot add NULL event to queue");
        }
    }
}