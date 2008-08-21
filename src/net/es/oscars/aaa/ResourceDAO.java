package net.es.oscars.aaa;

import net.es.oscars.database.GenericHibernateDAO;

/**
 * ResourceDAO is the data access object for the aaa.resources table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class ResourceDAO extends GenericHibernateDAO<Resource, Integer> {

    public ResourceDAO(String dbname) {
        this.setDatabase(dbname);
    }

    /** 
     * given an resource name, return the resource id
     * 
     * @param resourceName String name of the resource
     * @returns an Integer containing the resource id
     */
    public Integer getIdByName(String resourceName) throws AAAException {
        Resource resource = super.queryByParam("name", resourceName);
        if (resource != null ) {
            return resource.getId();
        } else {
            throw new AAAException ("No resource with name "+ resourceName);
        }
    }
}
