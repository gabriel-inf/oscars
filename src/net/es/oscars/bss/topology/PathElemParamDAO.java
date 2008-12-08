package net.es.oscars.bss.topology;

import java.util.*;
import org.apache.log4j.*;

import net.es.oscars.database.GenericHibernateDAO;

/**
 * PathElemParamDAO is the data access object for the bss.pathElemParams table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class PathElemParamDAO extends GenericHibernateDAO<PathElemParam, Integer> {
    private Logger log;
    
    public PathElemParamDAO(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.setDatabase(dbname);
    }
}
