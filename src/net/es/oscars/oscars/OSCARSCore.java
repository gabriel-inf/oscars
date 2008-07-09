package net.es.oscars.oscars;

import java.util.*;
import java.rmi.*;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import net.es.oscars.bss.*;
import net.es.oscars.pss.*;
import net.es.oscars.tss.*;
import net.es.oscars.aaa.*;
import net.es.oscars.pathfinder.*;
import net.es.oscars.notify.*;
import net.es.oscars.lookup.*;
import net.es.oscars.database.*;
import net.es.oscars.interdomain.*;
import net.es.oscars.scheduler.*;
import net.es.oscars.rmi.*;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

public class OSCARSCore {

    private Logger log;

    public boolean initialized = false;

    private static OSCARSCore instance = null;

    // Hardcoded but one can set them for tests etc
    private String bssDbName = "bss";
    private String aaaDbName = "aaa";

    private ReservationManager reservationManager = null;
    private TopologyManager topologyManager = null;
    private TopologyExchangeManager topologyExchangeManager = null;
    private UserManager userManager = null;
    private PCEManager pceManager = null;
    private PathSetupManager pathSetupManager = null;
    private PolicyManager policyManager = null;

    private NotifyInitializer notifier = null;
    private PSLookupClient lookupClient = null;

    private TopologyExchangeAdapter topologyExchangeAdapter = null;
    private PathSetupAdapter pathSetupAdapter = null;
    private ReservationAdapter reservationAdapter = null;
    private TypeConverter typeConverter = null;
    private Forwarder forwarder = null;
    private ScheduleManager scheduleManager = null;

    private CoreRmiServer rmiServer = null;


    private OSCARSCore() {
        this.log = Logger.getLogger(this.getClass());
        // singleton class!
    }

    public static OSCARSCore getInstance() {
        if (OSCARSCore.instance == null) {
            OSCARSCore.instance = new OSCARSCore();
        }
        return OSCARSCore.instance;
    }

    public static OSCARSCore init() {
        if (OSCARSCore.instance == null) {
            OSCARSCore.instance = new OSCARSCore();
        }
        OSCARSCore instance = OSCARSCore.instance;
        instance.initAll();

        return instance;
    }

    public void initAll() {
        this.log.debug("initAll.start");

        this.initDatabases();
        this.initReservationManager();
        this.initPathSetupManager();
        this.initTopologyManager();
        this.initTopologyExchangeManager();
        this.initUserManager();
        this.initScheduleManager();
        this.initPCEManager();
        this.initPolicyManager();

        this.initLookupClient();
        this.initNotifier();

        this.initReservationAdapter();
        this.initTopologyExchangeAdapter();
        this.initPathSetupAdapter();
        this.initTypeConverter();
        this.initForwarder();
        this.initRMIServer();
        this.initialized = true;

        this.log.debug("initAll.end");
    }

    public void shutdown() {
        this.log.info("shutdown.start");
        try {
            this.scheduleManager.getScheduler().shutdown(false);
        } catch (SchedulerException ex) {
            this.log.error("Scheduler error shutting down", ex);
        }
        this.rmiServer.shutdown();
        HibernateUtil.closeSessionFactory(this.aaaDbName);
        HibernateUtil.closeSessionFactory(this.bssDbName);
        this.log.info("shutdown.end");
    }




    public void initDatabases() {
        this.log.debug("initDatabases.start");
        ArrayList<String> dbnames = new ArrayList<String>();
        dbnames.add(this.bssDbName);
        dbnames.add(this.aaaDbName);
        Initializer dbInitializer = new Initializer();
        dbInitializer.initDatabase(dbnames);
        Session aaa = this.getAaaSession();
        Session bss = this.getBssSession();
        this.log.debug("initDatabases.end");
    }


    public void initReservationManager() {
        this.log.debug("initReservationManager.start");
        this.reservationManager = new ReservationManager(this.bssDbName);
        this.log.debug("initReservationManager.end");
    }


    public void initLookupClient() {
        this.log.debug("initLookupClient.start");
        LookupFactory lookupFactory = new LookupFactory();
        this.lookupClient = lookupFactory.getPSLookupClient();
        this.log.debug("initLookupClient.end");
    }

    public void initNotifier()  {
        this.log.debug("initNotifier.start");
        this.notifier = new NotifyInitializer();
        try {
            this.notifier.init();
        } catch (NotifyException ex) {
            this.log.error("Could not init notifier", ex);
        }
        this.log.debug("initNotifier.end");
    }

    public void initPCEManager() {
        this.log.debug("initPCEManager.start");
        this.pceManager = new PCEManager(this.bssDbName);
        this.log.debug("initPCEManager.end");
    }

    public void initPathSetupManager() {
        this.log.debug("initPathSetupManager.start");
        this.pathSetupManager = new PathSetupManager(this.bssDbName);
        this.log.debug("initPathSetupManager.end");
    }

    public void initTopologyManager() {
        this.log.debug("initTopologyManager.start");
        this.topologyManager = new TopologyManager(this.bssDbName);
        this.log.debug("initTopologyManager.end");
    }

    public void initUserManager() {
        this.log.debug("initUserManager.start");
        this.userManager = new UserManager(this.aaaDbName);
        this.log.debug("initUserManager.end");
    }

    public void initTopologyExchangeManager() {
        this.log.debug("initTopologyExchangeManager.start");
        this.topologyExchangeManager = new TopologyExchangeManager();
        this.log.debug("initTopologyExchangeManager.end");
    }

    public void initReservationAdapter() {
        this.log.debug("initReservationAdapter.start");
        this.reservationAdapter = new ReservationAdapter();
        this.log.debug("initReservationAdapter.end");
    }

    public void initTopologyExchangeAdapter() {
        this.log.debug("initTopologyExchangeAdapter.start");
        this.topologyExchangeAdapter = new TopologyExchangeAdapter();
        this.log.debug("initTopologyExchangeAdapter.end");
    }

    public void initPathSetupAdapter() {
        this.log.debug("initPathSetupAdapter.start");
        this.pathSetupAdapter = new PathSetupAdapter();
        this.log.debug("initPathSetupAdapter.end");
    }

    public void initTypeConverter() {
        this.log.debug("initTypeConverter.start");
        this.typeConverter = new TypeConverter();
        this.log.debug("initTypeConverter.end");
    }

    public void initForwarder() {
        this.log.debug("initForwarder.start");
        this.forwarder = new Forwarder();
        this.log.debug("initForwarder.end");
    }

    public void initScheduleManager() {
        this.log.debug("initScheduleManager.start");
        this.scheduleManager = ScheduleManager.getInstance();
        this.log.debug("initScheduleManager.end");
    }
    public void initPolicyManager() {
        this.log.debug("initPolicyManager.start");
        this.policyManager = new PolicyManager(this.bssDbName);
        this.log.debug("initPolicyManager.end");
    }

    public void initRMIServer() {
        this.log.debug("initRMIServer.start");
        try {
            this.rmiServer = new CoreRmiServer();
            this.rmiServer.init();
        } catch (RemoteException ex) {
            this.log.error("Error initializing RMI server", ex);
            this.rmiServer = null;
        }
        this.log.debug("initRMIServer.end");
    }


    public Session getAaaSession() {
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
     * @param bssDbName the bssDbName to set
     */
    public void setBssDbName(String bssDbName) {
        this.bssDbName = bssDbName;
    }

    /**
     * @return the aaaDbName
     */
    public String getAaaDbName() {
        return aaaDbName;
    }

    /**
     * @param aaaDbName the aaaDbName to set
     */
    public void setAaaDbName(String aaaDbName) {
        this.aaaDbName = aaaDbName;
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
     * @return the userManager
     */
    public UserManager getUserManager() {
        if (this.userManager == null) {
            this.initUserManager();
        }
        return userManager;
    }

    /**
     * @param userManager the userManager to set
     */
    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
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
    public NotifyInitializer getNotifier() {
        if (this.notifier == null) {
            this.initNotifier();
        }
        return notifier;
    }

    /**
     * @param notifier the notifier to set
     */
    public void setNotifier(NotifyInitializer notifier) {
        this.notifier = notifier;
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
     * @return the topologyExchangeAdapter
     */
    public TopologyExchangeAdapter getTopologyExchangeAdapter() {
        if (this.topologyExchangeAdapter == null) {
            this.initTopologyExchangeAdapter();
        }
        return topologyExchangeAdapter;
    }

    /**
     * @param topologyExchangeAdapter the topologyExchangeAdapter to set
     */
    public void setTopologyExchangeAdapter(
            TopologyExchangeAdapter topologyExchangeAdapter) {
        this.topologyExchangeAdapter = topologyExchangeAdapter;
    }

    /**
     * @return the pathSetupAdapter
     */
    public PathSetupAdapter getPathSetupAdapter() {
        if (this.pathSetupAdapter == null) {
            this.initPathSetupAdapter();
        }
        return pathSetupAdapter;
    }

    /**
     * @param pathSetupAdapter the pathSetupAdapter to set
     */
    public void setPathSetupAdapter(PathSetupAdapter pathSetupAdapter) {
        this.pathSetupAdapter = pathSetupAdapter;
    }

    /**
     * @return the reservationAdapter
     */
    public ReservationAdapter getReservationAdapter() {
        if (this.reservationAdapter == null) {
            this.initReservationAdapter();
        }
        return reservationAdapter;
    }

    /**
     * @param reservationAdapter the reservationAdapter to set
     */
    public void setReservationAdapter(ReservationAdapter reservationAdapter) {
        this.reservationAdapter = reservationAdapter;
    }

    /**
     * @return the typeConverter
     */
    public TypeConverter getTypeConverter() {
        if (this.typeConverter == null) {
            this.initTypeConverter();
        }
        return typeConverter;
    }

    /**
     * @param typeConverter the typeConverter to set
     */
    public void setTypeConverter(TypeConverter typeConverter) {
        this.typeConverter = typeConverter;
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
     * @return the rmiServer
     */
    public CoreRmiServer getRmiServer() {
        if (this.rmiServer == null) {
            this.initRMIServer();
        }
        return rmiServer;
    }

    /**
     * @param rmiServer the rmiServer to set
     */
    public void setRmiServer(CoreRmiServer rmiServer) {
        this.rmiServer = rmiServer;
    }



}
