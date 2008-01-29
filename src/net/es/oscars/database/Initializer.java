package net.es.oscars.database;

import java.util.List;

import org.apache.log4j.*;

/**
 * Initializer handles Hibernate initialization.
 */
public class Initializer {
    private Logger log;

    public Initializer() {
        this.log = Logger.getLogger(this.getClass());
    }

    /**
     * Initializes Hibernate.
     */
    public void initDatabase(List<String> dbnames) {
        ClassLoader OSCARSCL = this.getClass().getClassLoader();
        if (OSCARSCL == null) {
            this.log.error("initDatabase: Class loader is null");
        }
        // initializes session factories for aaa and bss databases
        HibernateUtil.initSessionFactories(OSCARSCL, dbnames);
    }
}