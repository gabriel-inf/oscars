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
    
    public String getConstraintType(String constraintName) {
        
        if (constraintName == null) {
            return null;
        }
        String hsql = "from Constraint where name = :name"; 
        Constraint constraint =  (Constraint) this.getSession().createQuery(hsql)
         .setString("name", constraintName)
         .setMaxResults(1)
         .uniqueResult(); 
        if (constraint != null) {
            return constraint.getType();
        } else {
            return null;
        }        
    }
   public Integer getIdByName(String constraintName) {
        
       if (constraintName == null) {
          // TODO maybe change it "none"
           return null;
       }
       String hsql = "from Constraint where name = :name"; 
       Constraint constraint =  (Constraint) this.getSession().createQuery(hsql)
        .setString("name", constraintName)
        .setMaxResults(1)
        .uniqueResult(); 
       if (constraint != null) {
           return constraint.getId();
       } else {
           return null;
       }        
    }
    
}
