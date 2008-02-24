package net.es.oscars.bss.topology;

import java.util.*;
import org.apache.log4j.*;

import net.es.oscars.database.GenericHibernateDAO;

/**
 * StaticLIDPDAO is the data access object for the bss.staticLIDP table.
 *
 * @author Andrew Lake (alake@internet2.edu)
 */
public class StaticLIDPDAO extends GenericHibernateDAO<StaticLIDP, Integer> {
    private Logger log;
    
    public StaticLIDPDAO(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.setDatabase(dbname);
    }
}
