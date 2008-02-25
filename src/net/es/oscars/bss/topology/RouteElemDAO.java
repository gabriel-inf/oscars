package net.es.oscars.bss.topology;

import java.util.*;
import org.apache.log4j.*;

import net.es.oscars.database.GenericHibernateDAO;

/**
 * RouteElemDAO is the data access object for the bss.routeElems table.
 *
 * @author Andrew Lake (alake@internet2.edu)
 */
public class RouteElemDAO extends GenericHibernateDAO<RouteElem, Integer> {
    private Logger log;
    
    public RouteElemDAO(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.setDatabase(dbname);
    }
}
