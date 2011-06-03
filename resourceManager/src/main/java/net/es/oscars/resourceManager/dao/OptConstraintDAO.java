package net.es.oscars.resourceManager.dao;

import java.util.List;

import net.es.oscars.database.hibernate.GenericHibernateDAO;
import net.es.oscars.resourceManager.beans.OptConstraint;

/**
 * OptConstraintDAO is the data access object for the rm.OptConstraint table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class OptConstraintDAO extends GenericHibernateDAO<OptConstraint, Integer> {

    public OptConstraintDAO(String dbname) {
        this.setDatabase(dbname);
    }
}
