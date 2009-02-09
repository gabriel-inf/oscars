package net.es.oscars.notifybroker;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.*;
import org.apache.log4j.*;
import org.quartz.*;
import org.quartz.impl.*;
import net.es.oscars.database.Initializer;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.notifybroker.jobs.ServiceManager;
import net.es.oscars.notifybroker.policy.NotifyPEP;
import net.es.oscars.notifybroker.policy.NotifyPEPFactory;
import net.es.oscars.rmi.notifybroker.NotifyRmiServer;

/**
 * Initializes and allows access to core NotificationBroker
 * functionality. This includes initializing databases,
 * managing the scheduler, and loading PEPs.
 *
 * @author Andrew Lake (alake@internet2.edu)
 */
public class NotifyBrokerCore{
    private Logger log;
    private Scheduler scheduler;
    private ServiceManager serviceManager = null;
    private NotifyBrokerManager notifyBrokerManager = null;
    private ArrayList<NotifyPEP> notifyPEPs;
    private NotifyRmiServer rmiServer;
    
    
    public boolean initialized = false;
    private static NotifyBrokerCore instance = null;
    final private String notifyDbName = "notify";
    
    private NotifyBrokerCore() {
        this.log = Logger.getLogger(this.getClass());
    }

    public static NotifyBrokerCore getInstance() {
        if (NotifyBrokerCore.instance == null) {
            NotifyBrokerCore.instance = new NotifyBrokerCore();
        }
        return NotifyBrokerCore.instance;
    }

    public static NotifyBrokerCore init() {
        if (NotifyBrokerCore.instance == null) {
            NotifyBrokerCore.instance = new NotifyBrokerCore();
        }
        NotifyBrokerCore instance = NotifyBrokerCore.instance;
        instance.initDatabases();
        instance.initScheduler();
        instance.initNotifyPEPs();
        instance.initServiceManager();
        instance.initNotifyBrokerManager();
        instance.initRMIServer();
        
        return instance;
    }


    public void shutdown() {
        this.log.info("shutdown.start");
        try {
            if(this.scheduler != null){
                this.scheduler.shutdown(false);
            }
        } catch (SchedulerException ex) {
            this.log.error("Scheduler error shutting down", ex);
        }
        if(this.rmiServer != null){
            this.rmiServer.shutdown();
        }
        HibernateUtil.closeSessionFactory(this.notifyDbName);
        this.log.info("shutdown.end");
    }


    public void initDatabases(){
        this.log.debug("initDatabases.start");
        Initializer initializer = new Initializer();
        List<String> dbnames = new ArrayList<String>();
        dbnames.add(notifyDbName);
        initializer.initDatabase(dbnames);
        this.log.debug("initDatabases.end");
    }


    public void initNotifyPEPs(){
        this.log.debug("initNotifyPEPs.start");
        this.notifyPEPs = NotifyPEPFactory.createPEPs("");
        this.log.debug("initNotifyPEPs.end");
    }


    public void initScheduler(){
        this.log.debug("initScheduler.start");
        try {
            SchedulerFactory schedFact = new StdSchedulerFactory();
            this.scheduler = schedFact.getScheduler();
            this.scheduler.start();
        } catch (SchedulerException ex) {
            this.log.error("Scheduler init exception", ex);
        }
        this.log.debug("initScheduler.end");
    }


    public void initServiceManager() {
        this.log.debug("initServiceManager.start");
        this.serviceManager = new ServiceManager();
        this.log.debug("initServiceManager.end");
    }
    
    public void initNotifyBrokerManager() {
        this.log.debug("initNotifyBrokerManager.start");
        this.notifyBrokerManager = new NotifyBrokerManager(this.notifyDbName);
        this.log.debug("initNotifyBrokerManager.end");
    }


    /**
     * Initializes the RMIServer module
     */
    public void initRMIServer() {
        this.log.info("initRMIServer.start");
        try {
            this.rmiServer = new NotifyRmiServer();
            this.rmiServer.init();
        } catch (RemoteException ex) {
            this.log.error("Error initializing RMI server", ex);
            this.rmiServer.shutdown();
            this.rmiServer = null;
        }
        this.log.info("initRMIServer.end");
    }


    public Session getNotifySession() {
        Session notify = HibernateUtil.getSessionFactory(this.notifyDbName).getCurrentSession();
        if (notify == null || !notify.isOpen()) {
            this.log.debug("opening BSS session");
            notify = HibernateUtil.getSessionFactory(this.notifyDbName).openSession();
            notify = HibernateUtil.getSessionFactory(this.notifyDbName).getCurrentSession();
        }
        if (notify == null || !notify.isOpen()) {
            this.log.error("BSS session is still closed!");
        }
        return notify;
    }


    /**
     * @return the notifyDbName
     */
    public String getNotifyDbName() {
        return notifyDbName;
    }


    public ArrayList<NotifyPEP> getNotifyPEPs() {
        return this.notifyPEPs;
    }


    public Scheduler getScheduler(){
        return this.scheduler;
    }


    public ServiceManager getServiceManager(){
        return this.serviceManager;
    }


    public NotifyBrokerManager getNotifyBrokerManager(){
        return this.notifyBrokerManager;
    }
}