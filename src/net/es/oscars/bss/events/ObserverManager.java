package net.es.oscars.bss.events;

import java.util.*;

import org.apache.log4j.Logger;

import net.es.oscars.PropHandler;


public class ObserverManager {
    private Properties props;
    private Logger log;
    
    private ObserverSource source;
    private ArrayList<Observer> observers;

    public ObserverManager() {
        this.log = Logger.getLogger(this.getClass());
        PropHandler propHandler = new PropHandler("oscars.properties");
        this.props = propHandler.getPropertyGroup("notify", true);
        this.source = new ObserverSource();
        this.observers = new ArrayList<Observer>();
    }

    public void init() {
        this.log.info("init.start");
        
        int i = 1;
        while (this.props.getProperty("observer." + i) != null) {
            String className = this.props.getProperty("observer." + i);
            className = className.trim();
            if("net.es.oscars.notify.EmailObserver".equals(className)){
                this.log.info("Loading the email notification module");
                this.addObserver(new EmailObserver());
            }else if("net.es.oscars.notify.WSObserver".equals(className)){
                this.log.info("Loading the web service notification module");
                this.addObserver(new WSObserver());
            }else if("net.es.oscars.notify.FileWriterObserver".equals(className)){
                this.log.info("Loading the FileWriter notification module");
                this.addObserver(new FileWriterObserver());
            }else{
                this.log.info("Unrecognized module " + className);
            }
            i++;
        }
        
        if(i == 0){
            this.log.info("No notification module selected. It is recommended " +
                          "you set the notify.observer.1 property in oscars.properties" +
                          " so that you can share notifications about IDC activity.");
        }
        this.log.info("init.end");
    }

    public ObserverSource getSource() {
        return source;
    }

    public void addObserver(Observer obs) {
        this.observers.add(obs);
        this.source.addObserver(obs);
    }

}
