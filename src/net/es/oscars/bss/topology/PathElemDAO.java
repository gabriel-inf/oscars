package net.es.oscars.bss.topology;

import java.util.*;
import org.apache.log4j.*;

import net.es.oscars.database.GenericHibernateDAO;

/**
 * PathElemDAO is the data access object for the bss.pathElems table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class PathElemDAO extends GenericHibernateDAO<PathElem, Integer> {
    private Logger log;
    
    public PathElemDAO(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.setDatabase(dbname);
    }
}
