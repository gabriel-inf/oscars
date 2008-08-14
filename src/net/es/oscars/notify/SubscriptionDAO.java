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
                     "INNER JOIN subscriptionFilters sf2 ON sf.subscriptionId = sf2.subscriptionId " + 
                     "WHERE s.status=1 AND s.terminationTime > UNIX_TIMESTAMP(NOW()) AND " +
                     "(sf.type='TOPIC' AND (";
        ArrayList<String> queryParams = new ArrayList<String>();
        SQLQuery query = null;
        List<Subscription> subscriptions = null;
        boolean firstKey = true;
        boolean firstTopic = true;
        ArrayList<String> topics = permissionMap.remove("TOPIC");
        
        //Parse topics. Will always have at least one TOPIC (ALL).
        for(String topic : topics){
            sql += (!firstTopic ? " OR " : "");
            sql += "sf.value=?";
            queryParams.add(topic);
            this.log.debug("TOPIC=" + topic);
            firstTopic = false;
        }
        sql += "))";
        
        //Add PEP specific parameters.
        firstKey = true;
        for(String key: permissionMap.keySet()){
            sql += (!firstKey ? " OR " : " AND (");
            sql += "(sf2.type=? AND (";
            queryParams.add(key);
            boolean firstValue = true;
            for(String value : permissionMap.get(key)){
                sql += (!firstValue ? " OR " : "");
                sql += "sf2.value=?";
                queryParams.add(value);
                this.log.debug(key + "=" + value);
                firstValue = false;
            }
            sql += "))";
            firstKey = false;
        }
        sql += (!firstKey ? ")" : "");
        
        //Apply parameters to prepared statement to avoid SQL injection
        query = this.getSession().createSQLQuery(sql)
                                 .addEntity(Subscription.class);
        for(int i = 0; i < queryParams.size(); i++){
            query = (SQLQuery) query.setString(i, queryParams.get(i));
        }
        this.log.debug(sql);
        
        subscriptions = (List<Subscription>) query.list();
        return subscriptions;
    }
    
   public Subscription queryByRefId(String referenceId, String login){
        String sql = "SELECT * FROM subscriptions WHERE referenceId=?";
        if(login != null){
            sql += " AND userLogin=?";
        }
        SQLQuery query = (SQLQuery) this.getSession().createSQLQuery(sql)
                                 .addEntity(Subscription.class)
                                 .setString(0, referenceId);
        if(login != null){
            query = (SQLQuery) query.setString(1, login);
        }
        
        return (Subscription) query.uniqueResult();
   }
   
   public List<Subscription> getAllActiveForUser(String login){
        String sql = "SELECT * FROM subscriptions WHERE userLogin=? AND " + 
                     "status=1 AND terminationTime > UNIX_TIMESTAMP(NOW())";
        

        return (List<Subscription>) this.getSession().createSQLQuery(sql)
                                 .addEntity(Subscription.class)
                                 .setString(0, login)
                                 .list();
   }
}