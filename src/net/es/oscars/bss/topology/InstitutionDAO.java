package net.es.oscars.bss.topology;

import java.util.List;

import net.es.oscars.database.GenericHibernateDAO;

/**
 * InstitutionDAO is the data access object for the bss.institutions table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class InstitutionDAO extends GenericHibernateDAO<Institution, Integer> {

    public InstitutionDAO(String dbname) {
        this.setDatabase(dbname);
    }
}
