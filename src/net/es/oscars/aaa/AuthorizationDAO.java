package net.es.oscars.aaa;

import java.util.*;

import org.hibernate.*;
import net.es.oscars.database.GenericHibernateDAO;

/**
 * AuthorizationDAO is the data access object for the aaa.authorizations
 * table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class AuthorizationDAO
    extends GenericHibernateDAO<Authorization, Integer> {

    /**
     * Deletes a row containing an authorization triple.
     *
     * @param userId int with primary key of user
     * @param resourceId int with primary key of resource
     * @param permissionId int with primary key of permission
     * @throws AAAException.
     */
    public void remove(int userId, int resourceId, int permissionId)
            throws AAAException {

        Authorization auth = this.query(userId, resourceId, permissionId);
        if (auth == null) {
            throw new AAAException(
                    "Trying to remove non-existent authorization");
        }
        this.makeTransient(auth);
    }

    /**
     * Retrieves authorizations for a given user if a user name is given.
     *     Otherwise, all authorizations are returned.
     *
     * @param userName A string containing the user's name
     * @return auths A list of authorization instances
     * @throws AAAException.
     */
    public List<Authorization> list(String userName) throws AAAException {

        List<Authorization> auths = null;
        User user = null;
        UserDAO userDAO = new UserDAO();

        if (userName != null) {
            user = userDAO.query(userName);
            if (user == null)  {
                throw new AAAException(
                      "AuthorizationDAO.list: User not found " +
                      userName + ".");
            }
            String hsql = "from Authorization where userId = :userId";
            auths = this.getSession().createQuery(hsql)
                           .setInteger("userId", user.getId())
                           .list();
            return auths;
        }
        auths = this.findAll();
        return auths;
    }

    /**
     * Retrieves authorization, if any, based on presence of corresponding
     *     triplet in authorizations table.
     *
     * @param userId int with primary key of user
     * @param resourceId int with primary key of resource
     * @param permissionId int with primary key of permission
     * @return auth The associated authorization instance, if any
     */
    public Authorization query(int userId, int resourceId, int permissionId) {

        Authorization auth = null;

        String hsql = "from Authorization where userId = :userId and " +
                     "resourceId = :resourceId and " +
                     "permissionId = :permissionId";
        auth = (Authorization) this.getSession().createQuery(hsql)
                      .setInteger("userId", userId)
                      .setInteger("resourceId", resourceId)
                      .setInteger("permissionId", permissionId)
                      .setMaxResults(1)
                      .uniqueResult();
        return auth;
    }
}
