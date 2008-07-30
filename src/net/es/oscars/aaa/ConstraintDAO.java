package net.es.oscars.aaa;

import net.es.oscars.database.GenericHibernateDAO;
import net.es.oscars.aaa.Constraint;

/**
 * ConstraintDAO is the data access object for the aaa.constraints table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class ConstraintDAO extends GenericHibernateDAO<Constraint, Integer> {

    public ConstraintDAO (String dbname) {
        this.setDatabase(dbname);
    }
    
    public String getConstraintName (Integer constraintId) {
        
        if (constraintId == null) {
            return null;
        }
        Constraint constraint =  (Constraint) this.findById(constraintId, false);
        if (constraint != null) {
            return constraint.getName();
        } else {
            return null;
        }
    }
    
}
