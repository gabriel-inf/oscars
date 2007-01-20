package net.es.oscars.database;

import java.io.Serializable;
import java.util.*;

/**
 * GenericDAO is the interface implemented by GenericHibernateDAO.
 *
 * @author christian(at)hibernate.org
 */
public interface GenericDAO<T, ID extends Serializable> {

    T findById(ID id, boolean lock);

    List<T> findAll();

    List<T> findByExample(T exampleInstance, String[] excludeProperty);

    T makePersistent(T entity);

    void makeTransient(T entity);
}
