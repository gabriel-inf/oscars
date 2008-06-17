package net.es.oscars.bss.topology;

import java.util.List;

import net.es.oscars.database.GenericHibernateDAO;

/**
 * SiteDAO is the data access object for the bss.institutions table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class SiteDAO extends GenericHibernateDAO<Site, Integer> {

    public SiteDAO(String dbname) {
        this.setDatabase(dbname);
    }
}
