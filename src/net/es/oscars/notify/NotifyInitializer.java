package net.es.oscars.notify;

import java.util.*;

import org.apache.log4j.Logger;

import net.es.oscars.PropHandler;


public class NotifyInitializer {
    private Properties props;
    private Logger log;
    
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
        this.log = Logger.getLogger(this.getClass());

        PropHandler propHandler = new PropHandler("oscars.properties");
        this.props = propHandler.getPropertyGroup("notify.observer", true);

    	this.source = new NotifierSource();
    	this.observers = new ArrayList<Observer>();
    	
    	
    	ClassLoader cl = ClassLoader.getSystemClassLoader();
    	
    	Integer i = 1;
    	while (this.props.getProperty(i.toString()) != null) {
    		String className = this.props.getProperty(i.toString());
    		try {
    			Class theClass = cl.loadClass(className);
	    		Observer o = (Observer) theClass.newInstance();
	    		this.addObserver(o);
    		} catch (java.lang.ClassNotFoundException cnfex) {
    			this.log.error("notifyInitializer: class not found for: "+className);
    		} catch (java.lang.InstantiationException initex) {
    			this.log.error("notifyInitializer: initialization error for: "+className);
    		} catch (java.lang.IllegalAccessException accex) {
    			this.log.error("notifyInitializer: illegal access for: "+className);
    		}
    		i++;
    	}
    	
    }
    
    
}
