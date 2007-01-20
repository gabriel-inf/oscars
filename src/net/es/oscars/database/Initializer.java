package net.es.oscars.database;

import net.es.oscars.LogWrapper;

/**
 * Initializer handles Hibernate initialization.
 */
public class Initializer {
    private LogWrapper log;

    public Initializer() {
        this.log = new LogWrapper(this.getClass());
    }

    /**
     * Initializes Hibernate.
     */
    public void initDatabase() {
        ClassLoader OSCARSCL = this.getClass().getClassLoader();
        if (OSCARSCL == null) {
            this.log.error("initDatabase", "Class loader is null");
        }
        // initializes session factories for aaa and bss databases
        HibernateUtil.initSessionFactories(OSCARSCL);
    }
}
