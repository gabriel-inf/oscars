package net.es.oscars.notify;

import java.util.*;


public class NotifyInitializer {
	private NotifierSource source;
	
	public NotifierSource getSource() {
		return source;
	}
	
	private ArrayList<Observer> observers;
	
	public void addObserver(Observer obs) {
		this.observers.add(obs);
    	this.source.addObserver(obs);
	}
	

    public NotifyInitializer() {
    	this.source = new NotifierSource();
    	this.observers = new ArrayList<Observer>();
    	
    	// TODO: dynamically add observers from config params

    	EmailObserver email = new EmailObserver();
    	this.addObserver(email);
    }
    
    
}
