package net.es.oscars.notify;

import java.util.*;

import org.apache.log4j.Logger;

import net.es.oscars.PropHandler;


public class NotifyInitializer {
    private Properties props;
    private Logger log;
    
    private NotifierSource source;
    private ArrayList<Observer> observers;

    public NotifyInitializer() {
        this.log = Logger.getLogger(this.getClass());
        PropHandler propHandler = new PropHandler("oscars.properties");
        this.props = propHandler.getPropertyGroup("notify", true);
        this.source = new NotifierSource();
        this.observers = new ArrayList<Observer>();
    }

    public void init() throws NotifyException {
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
        /* Tomcat can't find the class
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        Integer i = 1;
        while (this.props.getProperty(i.toString()) != null) {
            this.log.error("This should not be happening");
            String className = this.props.getProperty(i.toString());
            try {
                Class theClass = cl.loadClass(className);
                Observer o = (Observer) theClass.newInstance();
                this.addObserver(o);
            } catch (java.lang.ClassNotFoundException cnfex) {
                String msg = "notifyInitializer: class not found for: " +
                             className;
                this.log.error(msg);
                throw new NotifyException(msg);
            } catch (java.lang.InstantiationException initex) {
                String msg = "notifyInitializer: initialization error for: " +
                             className;
                this.log.error(msg);
                throw new NotifyException(msg);
            } catch (java.lang.IllegalAccessException accex) {
                String msg = "notifyInitializer: illegal access for: " +
                             className;
                this.log.error(msg);
                throw new NotifyException(msg);
            }
            i++;
        }
        */
    }

    public NotifierSource getSource() {
        return source;
    }

    public void addObserver(Observer obs) {
        this.observers.add(obs);
        this.source.addObserver(obs);
    }

}
