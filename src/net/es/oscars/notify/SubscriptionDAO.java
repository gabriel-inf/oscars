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
public class SubscriptionDAO
    extends GenericHibernateDAO<Subscription, Integer> {

    private Logger log;
    private String dbname;

    public SubscriptionDAO(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.setDatabase(dbname);
        this.dbname = dbname;
    }
    
    public List<Subscription> getAuthorizedSubscriptions(HashMap<String, ArrayList<String>> permissionMap){
        String sql = "SELECT DISTINCT s.* FROM subscriptions s INNER JOIN " +
                     "subscriptionFilters sf ON s.id = sf.subscriptionId " + 
                     "WHERE ";
        boolean firstKey = true;           
        for(String key: permissionMap.keySet()){
            sql += (!firstKey ? "AND " : "");
            sql += "(sf.type='" + key + "' AND (";
            boolean firstValue = true;
            for(String value : permissionMap.get(key)){
                sql += (!firstValue ? " OR " : "");
                sql += " sf.value='" + value + "'";
                firstValue = false;
            }
            sql += ")) ";
            firstKey = false;
        }
        this.log.info(sql);
        
        List<Subscription> subscriptions =
              (List<Subscription>) this.getSession().createSQLQuery(sql)
                                            .addEntity(Subscription.class)
                                            .list();
        return subscriptions;
    }
}