package net.es.oscars.notify;

import org.apache.log4j.*;

public class EventConsumer extends Thread{
    private Logger log;
    boolean keepAlive;
    private NotifyInitializer notifier;
    
    public EventConsumer() throws NotifyException{
        this.log = Logger.getLogger(this.getClass());
        this.keepAlive = true;
        this.notifier = new NotifyInitializer();
        this.notifier.init();
        EventQueue.init();
    }
    
    public void shutdown(){
        this.keepAlive = false;
        this.interrupt();
    }
    
    public void run() {
        while(this.keepAlive){
            this.log.info("Listening for events...");
            try{
                Event event = EventQueue.take();
                this.log.info("Received event: " + event.getType());
                NotifierSource observable = this.notifier.getSource();
                Object obj = (Object) event;
                observable.eventOccured(obj);
            }catch(InterruptedException e){
                this.log.warn("Interrupted while listening for event");
            }
        }
    }
}