package net.es.oscars.bss.topology;

import java.util.List;

import net.es.oscars.database.GenericHibernateDAO;

/**
 * NodeAddressDAO is the data access object for the bss.nodeAddresses table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class NodeAddressDAO extends GenericHibernateDAO<NodeAddress, Integer> {

    public NodeAddressDAO(String dbname) {
        this.setDatabase(dbname);
    }
}
