package net.es.oscars.bss.topology;

import java.util.List;

import net.es.oscars.database.GenericHibernateDAO;

/**
 * Layer2DataDAO is the data access object for the bss.layer2Data table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class Layer2DataDAO extends GenericHibernateDAO<Layer2Data, Integer> {

    public Layer2DataDAO(String dbname) {
        this.setDatabase(dbname);
    }
}
