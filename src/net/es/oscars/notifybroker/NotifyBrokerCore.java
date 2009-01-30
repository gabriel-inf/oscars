package net.es.oscars.notifybroker;

import java.util.ArrayList;
import java.util.List;
import org.hibernate.*;
import org.apache.log4j.*;
import org.quartz.*;
import org.quartz.impl.*;
import net.es.oscars.aaa.UserManager;
import net.es.oscars.database.Initializer;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.notifybroker.jobs.ServiceManager;
import net.es.oscars.notifybroker.policy.NotifyPEP;
import net.es.oscars.notifybroker.policy.NotifyPEPFactory;
import net.es.oscars.notifybroker.ws.SubscriptionAdapter;

/**
 * Initializes and allows access to core NotificatioNBroker
 * functionality. This includes initializing databases,
 * managing the scheduler, and loading PEPs.
 *
 * @author Andrew Lake (alake@internet2.edu)
 */
public class NotifyBrokerCore{
    private Logger log;
    private Scheduler scheduler;
    private SubscriptionAdapter sa;
    private ServiceManager serviceManager = null;
    private UserManager userManager = null;
    private ArrayList<NotifyPEP> notifyPEPs;

    public boolean initialized = false;
    private static NotifyBrokerCore instance = null;
    final private String aaaDbName = "aaa";
    final private String notifyDbName = "notify";
    final private String bssDbName = "bss";
    
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
        instance.initUserManager();
        instance.initNotifyPEPs();
        instance.initServiceManager();
        instance.initSubscriptionAdapter();

        return instance;
    }


    public void shutdown() {
        this.log.info("shutdown.start");
        try {
            this.scheduler.shutdown(false);
        } catch (SchedulerException ex) {
            this.log.error("Scheduler error shutting down", ex);
        }
        HibernateUtil.closeSessionFactory(this.aaaDbName);
        HibernateUtil.closeSessionFactory(this.bssDbName);
        HibernateUtil.closeSessionFactory(this.notifyDbName);
        this.log.info("shutdown.end");
    }


    public void initDatabases(){
        this.log.debug("initDatabases.start");
        Initializer initializer = new Initializer();
        List<String> dbnames = new ArrayList<String>();
        dbnames.add(aaaDbName);
        dbnames.add(notifyDbName);
        dbnames.add(bssDbName);
        initializer.initDatabase(dbnames);
        this.log.debug("initDatabases.end");
    }

    public void initNotifyPEPs(){
        this.log.debug("initNotifyPEPs.start");
        this.notifyPEPs = NotifyPEPFactory.createPEPs(aaaDbName);
        this.log.debug("initNotifyPEPs.end");
    }

    public void initScheduler(){
        this.log.debug("initScheduler.start");
        try {
            SchedulerFactory schedFact = new StdSchedulerFactory();
            this.scheduler = schedFact.getScheduler();
            //TODO: Add lookup service job
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
    
    public void initSubscriptionAdapter(){
        this.log.debug("initSubscriptionAdapter.start");
        this.sa = new SubscriptionAdapter(notifyDbName);
        this.log.debug("initSubscriptionAdapter.end");
    }

    public void initUserManager() {
        this.log.debug("initUserManager.start");
        this.userManager = new UserManager(this.aaaDbName);
        this.log.debug("initUserManager.end");
    }

    public Session getAAASession() {
        Session aaa = HibernateUtil.getSessionFactory(this.aaaDbName).getCurrentSession();
        if (aaa == null || !aaa.isOpen()) {
            this.log.debug("opening AAA session");
            HibernateUtil.getSessionFactory(this.aaaDbName).openSession();
            aaa = HibernateUtil.getSessionFactory(this.aaaDbName).getCurrentSession();
        }
        if (aaa == null || !aaa.isOpen()) {
            this.log.error("AAA session is still closed!");
        }

        return aaa;
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

	public Session getBssSession() {
        Session bss = HibernateUtil.getSessionFactory(this.bssDbName).getCurrentSession();
        if (bss == null || !bss.isOpen()) {
            this.log.debug("opening BSS session");
            bss = HibernateUtil.getSessionFactory(this.bssDbName).openSession();
            bss = HibernateUtil.getSessionFactory(this.bssDbName).getCurrentSession();
        }
        if (bss == null || !bss.isOpen()) {
            this.log.error("BSS session is still closed!");
        }
        return bss;
    }


    /**
     * @return the bssDbName
     */
    public String getBssDbName() {
        return bssDbName;
    }
    
    /**
     * @return the aaDbName
     */
    public String getAaaDbName() {
        return aaaDbName;
    }


    public ArrayList<NotifyPEP> getNotifyPEPs() {
        return this.notifyPEPs;
    }

    public Scheduler getScheduler(){
        return this.scheduler;
    }

    public SubscriptionAdapter getSubscriptionAdapter(){
        return this.sa;
    }

    /**
	 * @return the serviceManager
	 */
	public ServiceManager getServiceManager() {
		return serviceManager;
	}

	/**
	 * @param serviceManager the serviceManager to set
	 */
	public void setServiceManager(ServiceManager serviceManager) {
		this.serviceManager = serviceManager;
	}

	public UserManager getUserManager(){
        return this.userManager;
    }
}