package net.es.oscars.notify.db;

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
    
    public Publisher queryByRefId(String referenceId, String login, boolean onlyValid){
        String sql = "SELECT * FROM publishers WHERE referenceId=?";
        if(login != null){
            sql += " AND userLogin=?";
        }
        if(onlyValid){
            sql += " AND status=1";
        }
        SQLQuery query = (SQLQuery) this.getSession().createSQLQuery(sql)
                                 .addEntity(Publisher.class)
                                 .setString(0, referenceId);
        if(login != null){
            query = (SQLQuery) query.setString(1, login);
        }
        
        return (Publisher) query.uniqueResult();
   }
    
    public List<Publisher> queryActive(){
        String sql = "SELECT * FROM publishers WHERE terminationTime < ? && status=1";
        long now = System.currentTimeMillis()/1000;
         return (List<Publisher>) this.getSession().createSQLQuery(sql)
         .addEntity(Publisher.class)
         .setLong(0, now)
         .list();
    }
}
