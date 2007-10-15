package net.es.oscars.bss.topology;

import java.util.List;

import net.es.oscars.database.GenericHibernateDAO;

/**
 * MPLSDataDAO is the data access object for the bss.mplsData table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class MPLSDataDAO extends GenericHibernateDAO<MPLSData, Integer> {

    public MPLSDataDAO(String dbname) {
        this.setDatabase(dbname);
    }
}
