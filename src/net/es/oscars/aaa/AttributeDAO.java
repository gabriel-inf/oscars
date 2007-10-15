package net.es.oscars.aaa;

import java.util.*;

import net.es.oscars.database.GenericHibernateDAO;

/** AttributeDAO is the data access object for the aaa.attributes table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class AttributeDAO extends GenericHibernateDAO<Attribute, Integer> {

    public AttributeDAO(String dbname) {
        this.setDatabase(dbname);
    }

    /* Given an attribute id, return the attribute name
     * 
     * @param attrId  int with attribute id
     * @returns a string with name of attribute
     */
    public String getAttributeName(int attrId) {
        Attribute attr = super.findById(attrId, false);
        return attr.getName();
    }
}
