package net.es.oscars.notify.db;

import java.util.List;

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
    
    public List<ExternalService> getByType(String type){
        String sql = "SELECT * FROM externalServices WHERE type=?";
        return (List<ExternalService>) this.getSession().createSQLQuery(sql)
                           .addEntity(ExternalService.class)
                           .setString(0, type)
                           .list();
    }
}
