package net.es.oscars.bss.topology;

import java.util.List;

import net.es.oscars.database.GenericHibernateDAO;

/**
 * PortDAO is the data access object for the bss.ports table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class PortDAO extends GenericHibernateDAO<Port, Integer> {

    public PortDAO(String dbname) {
        this.setDatabase(dbname);
    }

    public void invalidateAll() {
        List<Port> ports = this.list();
        for (Port port: ports) {
            port.setValid(false);
            this.update(port);
        }
    }
}
