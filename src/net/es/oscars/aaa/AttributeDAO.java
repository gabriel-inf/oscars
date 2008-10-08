package net.es.oscars.aaa;

import net.es.oscars.database.GenericHibernateDAO;
import net.es.oscars.aaa.AAAException;

/** AttributeDAO is the data access object for the aaa.attributes table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class AttributeDAO extends GenericHibernateDAO<Attribute, Integer> {

    public AttributeDAO(String dbname) {
        this.setDatabase(dbname);
    }

    /** 
     * Given an attribute id, return the attribute name
     * 
     * @param attrId  int with attribute id
     * @return a string with name of attribute
     * 
     * This is currently only called by AuthorizationDAO.listAuthByUser 
     */
    public String getAttributeName(int attrId)  {
        Attribute attr = super.findById(attrId, false);
        if (attr != null ) {
           return attr.getName();
        } else {
            return "unknown attribute";
        }
    }
    
    /** 
     * given an attribute name, return the attribute id
     * 
     * @param attrName string name of the attribute
     * @return an Integer containing the attribute id
     */
    public Integer getIdByName(String attrName) throws AAAException {
        Attribute attr = super.queryByParam("name", attrName);
        if (attr != null ) {
            return attr.getId();
        } else {
            throw new AAAException ("No attribute with name "+ attrName);
        }
    }
}
