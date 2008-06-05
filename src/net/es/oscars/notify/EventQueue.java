package net.es.oscars.notify;

import java.util.concurrent.LinkedBlockingQueue;

public class EventQueue {
    public static LinkedBlockingQueue<Event> queue;
    
    public static void init(){
        EventQueue.queue = new LinkedBlockingQueue<Event>();
    }
    
    public static void init(int capacity){
        EventQueue.queue = new LinkedBlockingQueue<Event>(capacity);
    }
    
    public static Event take() throws InterruptedException{
        return EventQueue.queue.take();
    }
    
    public static void put(Event event) throws InterruptedException{
        EventQueue.queue.put(event);
    }
}