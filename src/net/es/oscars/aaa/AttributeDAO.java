package net.es.oscars.aaa;

import java.util.*;

import net.es.oscars.database.GenericHibernateDAO;

/** AttributeDAO is the data access object for the aaa.permissions table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class AttributeDAO extends GenericHibernateDAO<Attribute, Integer> {

    public AttributeDAO(String dbname) {
        this.setDatabase(dbname);
    }

}
