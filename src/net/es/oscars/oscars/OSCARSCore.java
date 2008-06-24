package net.es.oscars.oscars;

import java.util.*;

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

public class OSCARSCore {

    private Logger log;

    private static OSCARSCore instance = null;

    // Hardcoded but you can set them for tests etc
    private String bssDbName = "bss";
    private String aaaDbName = "aaa";

    private ReservationManager reservationManager = null;
    private TopologyManager topologyManager = null;
    private TopologyExchangeManager topologyExchangeManager = null;
    private UserManager userManager = null;
    private PCEManager pceManager = null;
    private PathSetupManager pathSetupManager = null;
    private NotifyInitializer notifier = null;
    private PSLookupClient lookupClient = null;

    private TopologyExchangeAdapter topologyExchangeAdapter = null;
    private PathSetupAdapter pathSetupAdapter = null;
    private ReservationAdapter reservationAdapter = null;
    private TypeConverter typeConverter = null;
    private Forwarder forwarder = null;


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

    public static OSCARSCore init()
            throws BSSException, AAAException, PSSException, LookupException, NotifyException, PathfinderException {
        if (OSCARSCore.instance == null) {
            OSCARSCore.instance = new OSCARSCore();
        }
        OSCARSCore instance = OSCARSCore.instance;
        instance.initAll();

        return instance;
    }

    public void initAll()
        throws BSSException, AAAException, PSSException, LookupException, NotifyException, PathfinderException {
        this.log.debug("initAll.start");

        this.initDatabases();
        this.initReservationManager();
        this.initLookupClient();
        this.initNotifier();
        this.initPCEManager();
        this.initPathSetupManager();
        this.initTopologyManager();
        this.initTopologyExchangeManager();
        this.initUserManager();

        this.initReservationAdapter();
        this.initTopologyExchangeAdapter();
        this.initPathSetupAdapter();
        this.initTypeConverter();
        this.initForwarder();

        this.log.debug("initAll.end");
    }

    public void shutdown() {
        this.log.info("shutdown.start");
        HibernateUtil.destroySessionFactories();
        this.log.info("shutdown.end");
    }




    public void initDatabases() throws BSSException, AAAException {
        this.log.debug("initDatabases.start");
        if (this.bssDbName == null) {
            this.log.error("BSS DB name not set in OSCARS core");
            throw new BSSException("BSS DB name not set in OSCARS core");
        }
        if (this.aaaDbName == null) {
            this.log.error("AAA DB name not set in OSCARS core");
            throw new AAAException("AAA DB name not set in OSCARS core");
        }
        ArrayList<String> dbnames = new ArrayList<String>();
        dbnames.add(this.bssDbName);
        dbnames.add(this.aaaDbName);
        Initializer dbInitializer = new Initializer();
        dbInitializer.initDatabase(dbnames);
        this.log.debug("initDatabases.end");
    }


    public void initReservationManager() throws BSSException {
        this.log.debug("initReservationManager.start");
        if (this.bssDbName == null) {
            this.log.error("BSS DB name not set in OSCARS core");
            throw new BSSException("BSS DB name not set in OSCARS core");
        }
        this.reservationManager = new ReservationManager(this.bssDbName);
        this.log.debug("initReservationManager.end");
    }


    public void initLookupClient() throws LookupException {
        this.log.debug("initLookupClient.start");
        LookupFactory lookupFactory = new LookupFactory();
        this.lookupClient = lookupFactory.getPSLookupClient();
        this.log.debug("initLookupClient.end");
    }

    public void initNotifier() throws NotifyException {
        this.log.debug("initNotifier.start");
        this.notifier = new NotifyInitializer();
        this.notifier.init();
        this.log.debug("initNotifier.end");
    }

    public void initPCEManager() throws PathfinderException, BSSException {
        this.log.debug("initPCEManager.start");
        if (this.bssDbName == null) {
            this.log.error("BSS DB name not set in OSCARS core");
            throw new BSSException("BSS DB name not set in OSCARS core");
        }
        this.pceManager = new PCEManager(this.bssDbName);
        this.log.debug("initPCEManager.end");
    }

    public void initPathSetupManager() throws PSSException, BSSException {
        this.log.debug("initPathSetupManager.start");
        if (this.bssDbName == null) {
            this.log.error("BSS DB name not set in OSCARS core");
            throw new BSSException("BSS DB name not set in OSCARS core");
        }
        this.pathSetupManager = new PathSetupManager(this.bssDbName);
        this.log.debug("initPathSetupManager.end");
    }

    public void initTopologyManager() throws BSSException {
        this.log.debug("initTopologyManager.start");
        if (this.bssDbName == null) {
            this.log.error("BSS DB name not set in OSCARS core");
            throw new BSSException("BSS DB name not set in OSCARS core");
        }
        this.topologyManager = new TopologyManager(this.bssDbName);
        this.log.debug("initTopologyManager.end");
    }

    public void initUserManager() throws AAAException {
        this.log.debug("initUserManager.start");
        if (this.aaaDbName == null) {
            this.log.error("AAA DB name not set in OSCARS core");
            throw new AAAException("AAA DB name not set in OSCARS core");
        }
        this.userManager = new UserManager(this.aaaDbName);
        this.log.debug("initUserManager.end");
    }
    public void initTopologyExchangeManager() {
        this.log.debug("initTopologyExchangeManager.start");
        this.topologyExchangeManager = new TopologyExchangeManager();
        this.log.debug("initTopologyExchangeManager.end");
    }

    public void initReservationAdapter() throws BSSException {
        this.log.debug("initReservationAdapter.start");
        this.reservationAdapter = new ReservationAdapter();
        this.log.debug("initReservationAdapter.end");
    }

    public void initTopologyExchangeAdapter() throws BSSException {
        this.log.debug("initTopologyExchangeAdapter.start");
        this.topologyExchangeAdapter = new TopologyExchangeAdapter();
        this.log.debug("initTopologyExchangeAdapter.end");
    }

    public void initPathSetupAdapter() throws BSSException, PSSException {
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

    public Session getAaaSession() {
        Session aaa = HibernateUtil.getSessionFactory(this.aaaDbName).getCurrentSession();
        return aaa;
    }

    public Session getBssSession() {
        Session bss = HibernateUtil.getSessionFactory(this.bssDbName).getCurrentSession();
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
    public ReservationManager getReservationManager() throws BSSException {
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
    public TopologyManager getTopologyManager() throws BSSException {
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
    public UserManager getUserManager() throws AAAException {
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
    public PCEManager getPCEManager() throws PathfinderException, BSSException {
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
    public PathSetupManager getPathSetupManager() throws PSSException, BSSException {
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
    public NotifyInitializer getNotifier() throws NotifyException {
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
    public PSLookupClient getLookupClient() throws LookupException {
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
    public TopologyExchangeAdapter getTopologyExchangeAdapter() throws BSSException {
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
    public PathSetupAdapter getPathSetupAdapter() throws BSSException, PSSException {
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
    public ReservationAdapter getReservationAdapter() throws BSSException {
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



}
