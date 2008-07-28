package net.es.oscars.notify;

import java.util.*;
import org.apache.log4j.*;
import org.hibernate.*;
import net.es.oscars.database.GenericHibernateDAO;

/**
 * SubscriptionDAO is the data access object for
 * the notify.subscriptions table.
 *
 */
public class PublisherDAO
    extends GenericHibernateDAO<Publisher, Integer> {

    private Logger log;
    private String dbname;

    public PublisherDAO(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.setDatabase(dbname);
        this.dbname = dbname;
    }
}
