package net.es.oscars.bss.topology;

import java.util.*;

import org.apache.log4j.*;

import net.es.oscars.database.GenericHibernateDAO;
import net.es.oscars.bss.BSSException;


/**
 * VlanDAO is the data access object for the bss.vlans table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class VlanDAO extends GenericHibernateDAO<Domain, Integer> { 
    private Logger log;

    public VlanDAO(String dbname) {
        this.setDatabase(dbname);
        this.log = Logger.getLogger(this.getClass());
    }
}
