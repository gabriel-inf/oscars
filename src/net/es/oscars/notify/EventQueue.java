package net.es.oscars.notify;

import java.util.concurrent.LinkedBlockingQueue;

public class EventQueue {
    public static LinkedBlockingQueue<OSCARSEvent> queue;
    
    public static void init(){
        EventQueue.queue = new LinkedBlockingQueue<OSCARSEvent>();
    }
    
    public static void init(int capacity){
        EventQueue.queue = new LinkedBlockingQueue<OSCARSEvent>(capacity);
    }
    
    public static OSCARSEvent take() throws InterruptedException{
        return EventQueue.queue.take();
    }
    
    public static void put(OSCARSEvent event) throws InterruptedException{
        EventQueue.queue.put(event);
    }
}