package net.es.oscars.notifybroker.db;

import org.apache.log4j.*;
import net.es.oscars.database.GenericHibernateDAO;

/**
 * SubscriptionFilterDAO is the data access object for
 * the notify.subscriptionFilters table.
 *
 */
public class SubscriptionFilterDAO
    extends GenericHibernateDAO<SubscriptionFilter, Integer> {

    private Logger log;
    private String dbname;

    public SubscriptionFilterDAO(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.setDatabase(dbname);
        this.dbname = dbname;
    }
}
