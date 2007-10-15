package net.es.oscars.bss.topology;

import java.util.List;

import net.es.oscars.database.GenericHibernateDAO;

/**
 * Layer3DataDAO is the data access object for the bss.layer3Data table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class Layer3DataDAO extends GenericHibernateDAO<Layer3Data, Integer> {

    public Layer3DataDAO(String dbname) {
        this.setDatabase(dbname);
    }
}
