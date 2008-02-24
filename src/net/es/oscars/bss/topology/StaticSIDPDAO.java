package net.es.oscars.bss.topology;

import java.util.*;
import org.apache.log4j.*;

import net.es.oscars.database.GenericHibernateDAO;

/**
 * StaticSIDPDAO is the data access object for the bss.staticSIDP table.
 *
 * @author Andrew Lake (alake@internet2.edu)
 */
public class StaticSIDPDAO extends GenericHibernateDAO<StaticSIDP, Integer> {
    private Logger log;
    
    public StaticSIDPDAO(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.setDatabase(dbname);
    }
}
