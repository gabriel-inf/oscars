package net.es.oscars.aaa;

import java.util.*;

import org.hibernate.*;
import net.es.oscars.database.GenericHibernateDAO;
import net.es.oscars.aaa.UserAttributeDAO;
import net.es.oscars.aaa.UserManager.AuthValue;

/**
 * AuthorizationDAO is the data access object for the aaa.authorizations
 * table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 * @author Mary Thompson (mrthompson@lbl.gov)
 */
public class AuthorizationDAO
    extends GenericHibernateDAO<Authorization, Integer> {

    String dbname;

    public AuthorizationDAO(String dbname) {
        this.setDatabase(dbname);
        this.dbname = dbname;
    }

    /**
     * Deletes a row containing an authorization triple.
     *
     * @param attrId int with primary key of attribute
     * @param resourceId int with primary key of resource
     * @param permissionId Integer with primary key of permission (can be null)
     * @throws AAAException.
     */
    public void remove(int attrId, int resourceId, int permissionId,
                       Integer constraintId)
            throws AAAException {
        
        /* not called yet - designed for use by Web interface to manage
           authorizations */

        Authorization auth =
            this.query(attrId, resourceId, permissionId, constraintId);
        if (auth == null) {
            throw new AAAException(
                    "Trying to remove non-existent authorization");
        }
        super.remove(auth);
    }
    
    /**
     * Retrieves authorizations for a given user if userName is given.
     *     Otherwise, all authorizations are returned.
     *
     * @param userName A string containing a user name
     * @return auths A list of authorization instances
     * @throws AAAException.
     */
    public List<Authorization> listAuthByUser(String userName)
            throws AAAException {
        
        /* currently not called - designed for use by Web interface to manage
           authorizations */

        List<Authorization> auths = null;
        User user = null;
        UserDAO userDAO = new UserDAO(this.dbname);

        if (userName != null) {
            user = userDAO.query(userName);
            if (user == null)  {
                throw new AAAException(
                      "AuthorizationDAO.listAuthByUser: User not found " +
                      userName + ".");
            }
            UserAttributeDAO userAttrDAO = new UserAttributeDAO(this.dbname);
            List <UserAttribute> userAttrs =
                    userAttrDAO.getAttributesByUser(user.getId());
            if (userAttrs.isEmpty()) {
                throw new AAAException(
                    "AuthorizationDAO.listAuthByUser no attributes for user " +
                    userName + ".");
            }
            
            AttributeDAO attrDAO = new AttributeDAO(dbname);
            Iterator attrIter = userAttrs.iterator();
            UserAttribute currentAttr;
            currentAttr = (UserAttribute) attrIter.next();

            try {
                while (true ) {
                    int attrId = currentAttr.getAttributeId();
                    auths.addAll(
                            listAuthByAttr(attrDAO.getAttributeName(attrId)));
                    currentAttr= (UserAttribute) attrIter.next();
                }
            } catch ( NoSuchElementException ex) {
                /* end of loop over all the attributes for this user */
            }
            return auths;
        }
        auths = super.list();
        return auths;
        }
    
    /**
     * Retrieves authorizations for a given attribute if attrName is given.
     *     Otherwise, all authorizations are returned.
     *
     * @param attrName A string containing an attribute name
     * @return auths A list of authorization instances
     * @throws AAAException.
     */
    public List<Authorization> listAuthByAttr(String attrName)
            throws AAAException {
        
        /* currently  not called - designed for use by Web interface to manage
           authorizations */

        List<Authorization> auths = null;
        AttributeDAO attrDAO = new AttributeDAO(this.dbname);
        
        if (attrName != null)  {
            Attribute attr = attrDAO.queryByParam("name",attrName);
            if (attr == null)  {
                throw new AAAException(
                        "AuthorizationDAO.listAuthByAttr: Attr not found " +
                        attrName + ".");
            }
 
            String hsql = "from Authorization where attrrId = :attrId";
            auths = this.getSession().createQuery(hsql)
                           .setInteger("attrId", attr.getId())
                           .list();
            return auths;
        }
        auths = super.list();
        return auths;
    }

    /**
     * Retrieves authorization, if any, based on presence of corresponding
     *     triplet in authorizations table.
     *
     * @param attrId int with primary key of attribute
     * @param resourceId int with primary key of resource
     * @param permissionId int with primary key of permission
     * @return auths - list of the associated authorization instances, if any
     */
    public List<Authorization> query(int attrId, int resourceId,
                                     int permissionId) {

        List<Authorization> auths = null;

        String hsql = "from Authorization where attrId = :attrId and " +
                     "resourceId = :resourceId and " +
                     "permissionId = :permissionId";
        auths = this.getSession().createQuery(hsql)
                      .setInteger("attrId", attrId)
                      .setInteger("resourceId", resourceId)
                      .setInteger("permissionId", permissionId)
                      .list();

        return auths;
    }

    /**
     * Retrieves authorization, if any, based on presence of corresponding
     *    four-tuple in authorizations table.
     *
     * @param attrId int with primary key of attribute
     * @param resourceId int with primary key of resource
     * @param permissionId int with primary key of permission
     * @param constraintId - Integer with primary key of constraint, could be null
     * @return auths - list of the associated authorization instances, if any
     */
    public Authorization query(int attrId, int resourceId, int permissionId,
                               Integer constraintId) {

        Authorization auth = null;
        if (constraintId == 0){
            String hsql = "from Authorization where attrId = :attrId and " +
                "resourceId = :resourceId and " +
                "permissionId = :permissionId" +
                "constraintId IS NULL";
            auth = (Authorization) this.getSession().createQuery(hsql)
             .setInteger("attrId", attrId)
             .setInteger("resourceId", resourceId)
             .setInteger("permissionId", permissionId)
             .setMaxResults(1)
             .uniqueResult();
        } else {
            String hsql = "from Authorization where attrId = :attrId and " +
                "resourceId = :resourceId and " +
                "permissionId = :permissionId" +
                "constraintId = :constraintId";
            auth = (Authorization) this.getSession().createQuery(hsql)
                 .setInteger("attrId", attrId)
                 .setInteger("resourceId", resourceId)
                 .setInteger("permissionId", permissionId)
                 .setInteger("constraintId", constraintId)
                 .setMaxResults(1)
                 .uniqueResult(); 
        }
        return auth;
    }
    
    /**
     * Add a new authorization, given the attrId, resourceId, permissionId 
     * 
     * @param attrId int with primary key of attribute
     * @param resourceId int with primary key of resource
     * @param permissionId int with primary key of permission
     */
    
    public void create(int attrId, int resourceId, int permissionId) 
        throws AAAException {
       
        this.create(attrId,resourceId,permissionId,0,0);
    }
    
    /**
     * Add a new authorization, given the attrId, resourceId, permissionId 
     * 
     * @param attrId int with primary key of attribute
     * @param resourceId int with primary key of resource
     * @param permissionId int with primary key of permission
     * @param constraintId - int with primary key of constraint, could be null
     * @param constaintValue - int with value of constraint, could be null
     */
    
    public void create(int attrId, int resourceId, int permissionId,
                       int constraintId, int constraintValue) throws AAAException {
        
        // check for an already existing authorization
        Authorization auth = this.query(attrId,resourceId, permissionId, constraintId);
        if (auth != null) {
            throw new AAAException("duplicate entry");
        }
        auth = new Authorization();
        auth.setAttrId(attrId);
        auth.setAttrId(resourceId);
        auth.setAttrId(permissionId);
        if (constraintId != 0) {
            auth.setAttrId(constraintId);
        }
        if (constraintValue != 0) {
            auth.setConstraintValue(constraintValue);
        }
        super.create(auth);
        
    }
    
    /**
     * getConstraintName returns the constraint name for an authorization
     * 
     * @param Authorization auth
     * @return String constraintName
     * 
     */
    public String getConstraintName(Authorization auth) {
        
        ConstraintDAO constDAO = new ConstraintDAO(this.dbname);
        Integer test = auth.getConstraintId();
        return constDAO.getConstraintName(test);
        //return constDAO.getConstraintName(auth.getConstraintId());
    }
}
