package net.es.oscars.bss;

import java.util.*;
import java.rmi.*;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import net.es.oscars.pss.*;
import net.es.oscars.tss.*;
import net.es.oscars.pathfinder.*;
import net.es.oscars.lookup.*;
import net.es.oscars.database.*;
import net.es.oscars.interdomain.*;
import net.es.oscars.scheduler.*;
import net.es.oscars.rmi.bss.*;
import net.es.oscars.bss.events.ObserverManager;
import net.es.oscars.bss.policy.*;

import org.quartz.SchedulerException;

/**
 * OSCARS main object
 * @author Evangelos Chaniotakis @ ESnet
 *
 * Called by OSCARSRunner. Initializes all major objects including Hibernate and
 * its database access.
 * Starts the RMI repository, the coreRMIserver and the scheduler.
 */
public class OSCARSCore {

    private Logger log;

    public boolean initialized = false;

    private static OSCARSCore instance = null;

    // Hardcoded but one can set them for tests etc
    private String bssDbName = "bss";

    private StateEngine stateEngine = null;
    private ReservationManager reservationManager = null;
    private PathManager pathManager = null;
    private TopologyManager topologyManager = null;
    private TopologyExchangeManager topologyExchangeManager = null;
    private PCEManager pceManager = null;
    private PathSetupManager pathSetupManager = null;
    private PolicyManager policyManager = null;

    private ObserverManager observerMgr = null;
    private PSLookupClient lookupClient = null;

    private Forwarder forwarder = null;
    private ScheduleManager scheduleManager = null;
    private ServiceManager serviceManager = null;

    private BssRmiServer bssRmiServer = null;

    /**
     * Constructor - private because this is a Singleton
     */
    private OSCARSCore() {
        this.log = Logger.getLogger(this.getClass());
    }

    /**
     * @return the OSCARSCore singleton instance
     */
    public static OSCARSCore getInstance() {
        if (OSCARSCore.instance == null) {
            OSCARSCore.instance = new OSCARSCore();
        }
        return OSCARSCore.instance;
    }

    /**
     * Initializer
     * @return the OSCARSCore singleton instance
     */
    public static OSCARSCore init() {
        if (OSCARSCore.instance == null) {
            OSCARSCore.instance = new OSCARSCore();
        }
        OSCARSCore instance = OSCARSCore.instance;
        instance.initAll();
        return instance;
    }

    /**
     * Initializes all the modules
     */
    public void initAll() {
        this.log.debug("initAll.start");
        
        this.initBssDatabase();
        this.initStateEngine();
        this.initReservationManager();
        this.initPathSetupManager();
        this.initTopologyManager();
        this.initTopologyExchangeManager();
        this.initScheduleManager();
        this.initPCEManager();
        this.initPolicyManager();

        this.initLookupClient();
        this.initObservers();

        this.initForwarder();
        this.initRMIServer();
        this.initServiceManager();
        this.initialized = true;

        this.log.debug("initAll.end");
    }

    /**
     * Shuts down all modules
     */
    public void shutdown() {
        this.log.info("shutdown.start");
        try {
            this.scheduleManager.getScheduler().shutdown(false);
        } catch (SchedulerException ex) {
            this.log.error("Scheduler error shutting down", ex);
        } catch (Exception e) {
            this.log.error("Scheduler threw exception", e);
        }
        try {
            this.bssRmiServer.shutdown();
        } catch (Exception e) {
            this.log.error("Caught Exception on bssRmiServer.shutdown", e);
        }
        try {
        HibernateUtil.closeSessionFactory(this.bssDbName);
        } catch (Exception e) {
            this.log.error("Caught Exception on Hibernate.closeSessionFactory", e);
        }
        this.log.info("shutdown.end");
    }

    /**
     * Initializes the DB module
     */
    public void initBssDatabase() {
        this.log.debug("initBssDatabase.start");
        ArrayList<String> dbnames = new ArrayList<String>();
        dbnames.add(this.bssDbName);
        Initializer dbInitializer = new Initializer();
        dbInitializer.initDatabase(dbnames);
        this.log.debug("initBssDatabase.end");
    }


    /**
     * Initializes the StateEngine module
     */
    public void initStateEngine() {
        this.log.debug("initStateEngine.start");
        this.stateEngine = new StateEngine();
        this.log.debug("initStateEngine.end");
    }

    /**
     * Initializes the ReservationManager module
     */
    public void initReservationManager() {
        this.log.debug("initReservationManager.start");
        this.reservationManager = new ReservationManager(this.bssDbName);
        this.log.debug("initReservationManager.end");
    }


    /**
     * Initializes the PathManager module
     */
    public void initPathManager() {
        this.log.debug("initPathManager.start");
        this.pathManager = new PathManager(this.bssDbName);
        this.log.debug("initPathManager.end");
    }


    /**
     * Initializes the LookupClient module
     */
    public void initLookupClient() {
        this.log.debug("initLookupClient.start");
        LookupFactory lookupFactory = new LookupFactory();
        this.lookupClient = lookupFactory.getPSLookupClient();
        this.log.debug("initLookupClient.end");
    }

    /**
     * Initializes the Notifier module
     */
    public void initObservers()  {
        this.log.debug("initNotifier.start");
        this.observerMgr = new ObserverManager();
        this.observerMgr.init();
        this.log.debug("initNotifier.end");
    }

    /**
     * Initializes the PCEManager module
     */
    public void initPCEManager() {
        this.log.debug("initPCEManager.start");
        this.pceManager = new PCEManager(this.bssDbName);
        this.log.debug("initPCEManager.end");
    }

    /**
     * Initializes the PathSetupManager module
     */
    public void initPathSetupManager() {
        this.log.debug("initPathSetupManager.start");
        this.pathSetupManager = PathSetupManager.getInstance(this.bssDbName);
        this.log.debug("initPathSetupManager.end");
    }

    /**
     * Initializes the TopologyManager module
     */
    public void initTopologyManager() {
        this.log.debug("initTopologyManager.start");
        this.topologyManager = new TopologyManager(this.bssDbName);
        this.log.debug("initTopologyManager.end");
    }


    /**
     * Initializes the TopologyExchangeManager module
     */
    public void initTopologyExchangeManager() {
        this.log.debug("initTopologyExchangeManager.start");
        this.topologyExchangeManager = new TopologyExchangeManager();
        this.log.debug("initTopologyExchangeManager.end");
    }

    /**
     * Initializes the Forwarder module
     */
    public void initForwarder() {
        this.log.debug("initForwarder.start");
        this.forwarder = new Forwarder();
        this.log.debug("initForwarder.end");
    }

    /**
     * Initializes the ScheduleManager module
     */
    synchronized public void initScheduleManager() {
        this.log.debug("initScheduleManager.start");
        this.scheduleManager = ScheduleManager.getInstance();
        this.log.debug("initScheduleManager.end");
    }
    /**
     * Initializes the PolicyManager module
     */
    public void initPolicyManager() {
        this.log.debug("initPolicyManager.start");
        this.policyManager = new PolicyManager(this.bssDbName);
        this.log.debug("initPolicyManager.end");
    }

    /**
     * Initializes the RMIServer module
     */
    public void initRMIServer() {
        this.log.info("initRMIServer.start");
        try {
            this.bssRmiServer = new BssRmiServer();
            this.bssRmiServer.init();
        } catch (RemoteException ex) {
            this.log.error("Error initializing RMI server", ex);
            this.bssRmiServer.shutdown();
            this.bssRmiServer = null;
            System.out.println("Failure to initialize BSS RMI server, exiting. Exception was " + ex.getMessage());
            this.log.error("OSCARSCore exiting");
            System.exit(1);
        }
        this.log.info("initRMIServer.end");
    }

    /**
     * Initializes the ServiceManager module
     */
    public void initServiceManager() {
        this.log.debug("initServiceManager.start");
        this.serviceManager = new ServiceManager();
        this.log.debug("initServiceManager.end");
    }



    /**
     * @return Grabs the current AAA DB session for the current thread
     */
    public Session getBssSession() {
        SessionFactory sf = HibernateUtil.getSessionFactory(this.bssDbName);
        if (sf == null) {
            log.error("cannot open BSS session");
            return null;
        }
        Session bss = sf.getCurrentSession();
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
     * @param bssDbName the bssDbName to set
     */
    public void setBssDbName(String bssDbName) {
        this.bssDbName = bssDbName;
    }


    /**
     * @return the stateEngine
     */
    public StateEngine getStateEngine() {
        if (this.stateEngine == null) {
            this.initStateEngine();
        }
        return this.stateEngine;
    }

    /**
     * @return the reservationManager
     */
    public ReservationManager getReservationManager() {
        if (this.reservationManager == null) {
            this.initReservationManager();
        }
        return reservationManager;
    }

    /**
     * @param reservationManager the reservationManager to set
     */
    public void setReservationManager(ReservationManager reservationManager) {
        this.reservationManager = reservationManager;
    }

    /**
     * @return the pathManager
     */
    public PathManager getPathManager() {
        if (this.pathManager == null) {
            this.initPathManager();
        }
        return pathManager;
    }

    /**
     * @param pathManager the pathManager to set
     */
    public void setPathManager(PathManager pathManager) {
        this.pathManager = pathManager;
    }

    /**
     * @return the topologyManager
     */
    public TopologyManager getTopologyManager() {
        if (this.topologyManager == null) {
            this.initTopologyManager();
        }
        return topologyManager;
    }

    /**
     * @param topologyManager the topologyManager to set
     */
    public void setTopologyManager(TopologyManager topologyManager) {
        this.topologyManager = topologyManager;
    }



    /**
     * @return the PCEManager
     */
    public PCEManager getPCEManager() {
        if (this.pceManager == null) {
            this.initPCEManager();
        }
        return pceManager;
    }

    /**
     * @param PCEManager the PCEManager to set
     */
    public void setPCEManager(PCEManager PCEManager) {
        this.pceManager = PCEManager;
    }

    /**
     * @return the pathSetupManager
     */
    public PathSetupManager getPathSetupManager() {
        if (this.pathSetupManager == null) {
            this.initPathSetupManager();
        }
        return pathSetupManager;
    }

    /**
     * @param pathSetupManager the pathSetupManager to set
     */
    public void setPathSetupManager(PathSetupManager pathSetupManager) {
        this.pathSetupManager = pathSetupManager;
    }

    /**
     * @return the notifier
     */
    public ObserverManager getObserverMgr() {
        if (this.observerMgr == null) {
            this.initObservers();
        }
        return observerMgr;
    }

    /**
     * @param notifier the notifier to set
     */
    public void setObserverMgr(ObserverManager observerMgr) {
        this.observerMgr = observerMgr;
    }

    /**
     * @return the lookupClient
     */
    public PSLookupClient getLookupClient() {
        if (this.lookupClient == null) {
            this.initLookupClient();
        }
        return lookupClient;
    }

    /**
     * @param lookupClient the lookupClient to set
     */
    public void setLookupClient(PSLookupClient lookupClient) {
        this.lookupClient = lookupClient;
    }

    /**
     * @return the forwarder
     */
    public Forwarder getForwarder() {
        if (this.forwarder == null) {
            this.initForwarder();
        }
        return forwarder;
    }

    /**
     * @param forwarder the forwarder to set
     */
    public void setForwarder(Forwarder forwarder) {
        this.forwarder = forwarder;
    }

    /**
     * @return the topologyExchangeManager
     */
    public TopologyExchangeManager getTopologyExchangeManager() {
        if (this.topologyExchangeManager == null) {
            this.initTopologyExchangeManager();
        }
        return topologyExchangeManager;
    }

    /**
     * @param topologyExchangeManager the topologyExchangeManager to set
     */
    public void setTopologyExchangeManager(
            TopologyExchangeManager topologyExchangeManager) {
        this.topologyExchangeManager = topologyExchangeManager;
    }

    /**
     * @return the scheduleManager
     */
    public ScheduleManager getScheduleManager() {
        if (this.scheduleManager == null) {
            this.initScheduleManager();
        }

        return scheduleManager;
    }

    /**
     * @param scheduleManager the scheduleManager to set
     */
    public void setScheduleManager(ScheduleManager scheduleManager) {
        this.scheduleManager = scheduleManager;
    }

    /**
     * @return the policyManager
     */
    public PolicyManager getPolicyManager() {
        if (this.policyManager == null) {
            this.initPolicyManager();
        }
        return policyManager;
    }

    /**
     * @param policyManager the policyManager to set
     */
    public void setPolicyManager(PolicyManager policyManager) {
        this.policyManager = policyManager;
    }


    /**
     * @return the ServiceManager
     */
    public ServiceManager getServiceManager(){
        return this.serviceManager;
    }

    /**
     * @param sm the ServiceManager to set
     */
    public void setServiceManager(ServiceManager sm){
        this.serviceManager = sm;
    }

}
