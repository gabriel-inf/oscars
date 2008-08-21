package net.es.oscars.aaa;

import java.util.*;

import net.es.oscars.database.GenericHibernateDAO;

/** PermissionDAO is the data access object for the aaa.permissions table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class PermissionDAO extends GenericHibernateDAO<Permission, Integer> {

    public PermissionDAO(String dbname) {
        this.setDatabase(dbname);
    }

    /** 
     * given an permission name, return the permission id
     * 
     * @param permissionName String name of the permission
     * @returns an Integer containing the permission id
     */
    public Integer getIdByName(String permissionName) throws AAAException {
        Permission permission = super.queryByParam("name", permissionName);
        if (permission != null ) {
            return permission.getId();
        } else {
            throw new AAAException ("No permission with name "+ permissionName);
        }
    }
}
