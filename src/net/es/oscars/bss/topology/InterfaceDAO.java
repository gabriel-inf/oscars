package net.es.oscars.bss.topology;

import java.util.List;

import net.es.oscars.database.GenericHibernateDAO;

/**
 * InterfaceDAO is the data access object for the bss.interfaces table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class InterfaceDAO extends GenericHibernateDAO<Interface, Integer> {

    public InterfaceDAO(String dbname) {
        this.setDatabase(dbname);
    }

    public void invalidateAll() {
        List<Interface> xfaces = this.list();
        for (Interface xface: xfaces) {
            xface.setValid(false);
            this.update(xface);
        }
    }
}
