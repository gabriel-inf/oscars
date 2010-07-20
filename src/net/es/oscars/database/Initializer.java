package net.es.oscars.database;

import java.util.List;

// import org.apache.log4j.Logger;

/**
 * Initializer handles Hibernate initialization.
 */
public class Initializer {
    // private Logger log = Logger.getLogger(Initializer.class);


    /**
     * Initializes Hibernate.
     */
    public void initDatabase(List<String> dbnames) {
        // initializes session factories for aaa and bss databases
        HibernateUtil.initSessionFactories(dbnames);
    }
}
