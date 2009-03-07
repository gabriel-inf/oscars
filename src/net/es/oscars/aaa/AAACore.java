package net.es.oscars.aaa;

import java.rmi.RemoteException;
import java.util.ArrayList;

import net.es.oscars.database.HibernateUtil;
import net.es.oscars.database.Initializer;
import net.es.oscars.rmi.aaa.AaaRmiServer;

import org.apache.log4j.*;
import org.hibernate.Session;

/**
 * 
 * @author Evangelos Chaniotakis
 *
 * A singleton class that initializes and keeps an aaaRmiServer instance, 
 * a userManager instance, the name of the aaa database. Also initializes
 * the aaa database, and opens and returns a Hibernate aaa session,
 * and provide a shutdown method for aaaRmiServer.
 */
public class AAACore {
    private static Logger log = Logger.getLogger(AAACore.class);

    private static AaaRmiServer aaaRmiServer = null;
    private static AAACore instance = null;
    // Hardcoded but one can set it for tests etc
    private String aaaDbName = "aaa";
    private UserManager userManager = null;

    /**
     * Constructor - private because this is a Singleton
     */
    private AAACore() {
    }

    /**
     * @return the OSCARSCore singleton instance
     */
    public static AAACore getInstance() {
        if (AAACore.instance == null) {
            AAACore.instance = new AAACore();
        }
        return AAACore.instance;
    }

    /**
     * Initializer
     * @return the OSCARSCore singleton instance
     */
    public AAACore init() {
        if (AAACore.instance == null) {
            AAACore.instance = new AAACore();
        }
        AAACore instance = AAACore.instance;
        instance.initAll();
        return instance;
    }

    private void initAll() {
        this.initAaaDatabase();
        this.initRMIServer();
    }

    /**
     * Shuts down all modules
     */
    public void shutdown() {
        log.info("shutdown.start");
        aaaRmiServer.shutdown();
        HibernateUtil.closeSessionFactory(this.aaaDbName);
        log.info("shutdown.end");
    }


    /**
     * @return the current AAA DB session for the current thread
     */
    public Session getAaaSession() {
        Session aaa = HibernateUtil.getSessionFactory(this.aaaDbName).getCurrentSession();
        if (aaa == null || !aaa.isOpen()) {
            log.info("opening AAA session");
            HibernateUtil.getSessionFactory(this.aaaDbName).openSession();
            aaa = HibernateUtil.getSessionFactory(this.aaaDbName).getCurrentSession();
        }
        if (aaa == null || !aaa.isOpen()) {
            log.error("AAA session is still closed!");
        }
        return aaa;
    }

    /**
     * Initializes the DB module
     */
    public void initAaaDatabase() {
        log.debug("initAaaDatabase.start");
        ArrayList<String> dbnames = new ArrayList<String>();
        dbnames.add(this.aaaDbName);
        Initializer dbInitializer = new Initializer();
        dbInitializer.initDatabase(dbnames);
        log.debug("initAaaDatabase.end");
    }


    /**
     * Initializes the RMIServer module
     */
    private void initRMIServer() {
        log.info("initRMIServer.start");
        try {
            aaaRmiServer = new AaaRmiServer();
            aaaRmiServer.init();
        } catch (RemoteException ex) {
            log.error("Error initializing AAA RMI server", ex);
            aaaRmiServer.shutdown();
            aaaRmiServer = null;
            log.error("AAAcore exiting");
            System.out.println("AAA RMI server already running, exiting. Exception was " + ex.getMessage());
            System.exit (1);
        }
        log.info("initRMIServer.end");
    }

    /**
     * Initializes the UserManager module
     */
    public void initUserManager() {
        log.debug("initUserManager.start");
        this.userManager = new UserManager(this.aaaDbName);
        log.debug("initUserManager.end");
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
     * @return the userManager
     */
    public UserManager getUserManager() {
        if (this.userManager == null) {
            this.initUserManager();
        }
        return userManager;
    }
    /**
     *   NOT CALLED, this.userManager is set by initUserManager
     * @param userManager the userManager to set
     */
    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }



}
