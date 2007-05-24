package net.es.oscars.aaa;

import net.es.oscars.database.GenericHibernateDAO;

/**
 * ResourcePermissionDAO is currently not functional.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class ResourcePermissionDAO
    extends GenericHibernateDAO<ResourcePermission, Integer> {

    String dbname;

    public ResourcePermissionDAO(String dbname) {
        this.setDatabase(dbname);
        this.dbname = dbname;
    }

    /**
     * Removes a resourcepermission, given a resource and permission name. 
     * @param resourceName A String with the resource name.
     * @param permissionName A String with the permission name.
     * @return status A string with deletion status.
     * @throws AAAException.
     */
    public String remove(String resourceName, String permissionName)
                  throws AAAException {
        String status = null;

        ResourceDAO resourceDAO = new ResourceDAO(this.dbname);
        Resource resource = (Resource) resourceDAO.queryByParam(
                                             "resourceName", resourceName);

        PermissionDAO permissionDAO = new PermissionDAO(this.dbname);
        Permission permission = (Permission) permissionDAO.queryByParam(
                                             "permissionName", permissionName);

        String hsql = "from Resourcepermission " + 
                      "where resourceId = :resourceId and " +
                      "permissionId = :permissionId";
        ResourcePermission rp = (ResourcePermission) this.getSession().createQuery(hsql)
                      .setInteger("resourceId", resource.getId())
                      .setInteger("permissionId", permission.getId())
                      .setMaxResults(1)
                      .uniqueResult();
        super.remove(rp);
        return status;
    }
}
