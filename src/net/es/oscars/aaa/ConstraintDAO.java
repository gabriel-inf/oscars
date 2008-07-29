package net.es.oscars.aaa;

import net.es.oscars.database.GenericHibernateDAO;

/**
 * ConstraintDAO is the data access object for the aaa.constraints table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class ConstraintDAO extends GenericHibernateDAO<Constraint, Integer> {

    public ConstraintDAO(String dbname) {
        this.setDatabase(dbname);
    }
}
