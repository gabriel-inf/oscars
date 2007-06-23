package net.es.oscars.bss.topology;

import java.util.List;

import net.es.oscars.database.GenericHibernateDAO;

/**
 * LinkDAO is the data access object for the bss.links table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class LinkDAO extends GenericHibernateDAO<Link, Integer> {

    public LinkDAO(String dbname) {
        this.setDatabase(dbname);
    }

    public void invalidateAll() {
        List<Link> links = this.list();
        for (Link link: links) {
            link.setValid(false);
            this.update(link);
        }
    }
}
