package net.es.oscars.database;

import java.util.*;
import java.lang.reflect.ParameterizedType;
import java.io.Serializable;

import org.hibernate.*;
import org.hibernate.criterion.*;

/**
 * GenericHibernateDAO, an abstract class, is from the Hibernate web site:
 * http://www.hibernate.org/328.html.  It is subclassed by all OSCARS
 * DAO classes.
 *
 * @author christian(at)hibernate.org
 * @author dwrobertson@lbl.gov
 */
public abstract class GenericHibernateDAO<T, ID extends Serializable>
        implements GenericDAO<T, ID> {

    private Class<T> persistentClass;
    private Session session;

    public GenericHibernateDAO() {
        this.persistentClass = (Class<T>) ((ParameterizedType) getClass()
                                .getGenericSuperclass())
                                .getActualTypeArguments()[0];
    }

    public void setSession(Session s) { this.session = s; }

    public Session getSession() {
        if (session == null)
            throw new IllegalStateException(
                    "Session has not been set on DAO before usage");
        return session;
    }

    public Class<T> getPersistentClass() { return persistentClass; }

    public T findById(ID id, boolean lock) {
        T entity;
        if (lock)
            entity = (T)
                getSession().load(getPersistentClass(), id, LockMode.UPGRADE);
        else
            entity = (T) getSession().load(getPersistentClass(), id);

        return entity;
    }

    public List<T> findAll() { return findByCriteria(); }

    public List<T> findByExample(T exampleInstance, String[] excludeProperty) {
        Criteria crit = getSession().createCriteria(getPersistentClass());
        Example example =  Example.create(exampleInstance);
        for (String exclude : excludeProperty) {
            example.excludeProperty(exclude);
        }
        crit.add(example);
        return crit.list();
    }

    public T makePersistent(T entity) {
        getSession().saveOrUpdate(entity);
        return entity;
    }

    public void makeTransient(T entity) { getSession().delete(entity); }
    public void flush() { getSession().flush(); }
    public void clear() { getSession().clear(); }

   /**
    * Finds unique row based on query with parameter name and value.
    *     Parameter name must also be a column name for this to work.
    *     NOTE:  This will not work with dates.
    *
    * @param paramName A String with parameter name
    * @param paramValue An Object with parameter value
    * @return An instance T of the associated persistent class.
    */
    public T queryByParam(String paramName, Object paramValue) {
        String hsql = "from " + this.persistentClass.getName() +
                                " where " + paramName + " = :" + paramName; 
        return (T) getSession().createQuery(hsql)
                               .setParameter(paramName, paramValue)
                               .setMaxResults(1)
                               .uniqueResult();
    }

    /**
     * Use this inside subclasses as convenience method.
     */
    protected List<T> findByCriteria(Criterion... criterion) {
        Criteria crit = getSession().createCriteria(getPersistentClass());
        for (Criterion c : criterion) {
            crit.add(c);
        }
        return crit.list();
   }
}
