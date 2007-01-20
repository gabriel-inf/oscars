package net.es.oscars.bss.topology;

import java.util.List;

import net.es.oscars.database.GenericHibernateDAO;

/**
 * InterfaceDAO is the data access object for the bss.interfaces table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class InterfaceDAO extends GenericHibernateDAO<Interface, Integer> {

    /**
     * Inserts a row into the interfaces table.
     *
     * @param xface an Interface instance to be persisted
     */
    public void create(Interface xface) {
        this.makePersistent(xface);
    }

    /**
     * List all interfaces.
     *
     * @return a list of interfaces
     */
    public List<Interface> list() {
        List<Interface> xfaces = null;

        String hsql = "from Interface";
        xfaces = this.getSession().createQuery(hsql).list();
        return xfaces;
    }

    /**
     * Deletes a row from the interfaces table.
     *
     * @param xface an Interface instance to remove from the database
     */
    public void remove(Interface xface) {
        this.makeTransient(xface);
    }

}
