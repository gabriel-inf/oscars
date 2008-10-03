package net.es.oscars.notify.db;

import org.apache.log4j.Logger;

import net.es.oscars.database.GenericHibernateDAO;

/**
 * ExternalServiceDAO is the data access object for
 * the notify.externalServices table.
 *
 */
public class ExternalServiceDAO extends GenericHibernateDAO<ExternalService, Integer> {
	private Logger log;
    private String dbname;

    public ExternalServiceDAO(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.setDatabase(dbname);
        this.dbname = dbname;
    }
}
