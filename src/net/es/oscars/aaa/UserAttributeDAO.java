package net.es.oscars.aaa;

import java.util.*;

import net.es.oscars.database.GenericHibernateDAO;
import  org.apache.log4j.*;;

/** UserAttributeDAO is the data access object for the aaa.userAttribute table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */

    public class UserAttributeDAO
    extends GenericHibernateDAO<UserAttribute, Integer> {

    private String dbname;
    private Logger log;

    /**
     * Constructor
     * 
     * @param dbname
     */
    public UserAttributeDAO(String dbname) {
        this.setDatabase(dbname);
        this.dbname = dbname;
        this.log = Logger.getLogger(this.getClass());
    }
    
    /**
     * Get the list of all the attributes for a user
     * 
     * @param userId Index into the users table
     * @return  List of attribute ids
     */
    public List<UserAttribute> getAttributesByUser(int userId) {
        
        String hsql = "from UserAttribute "  +
                      "where userId = :userId" ; 
        List<UserAttribute> userAttrs = this.getSession().createQuery(hsql)
                          .setInteger("userId", userId)
                          .list();
        return userAttrs;
   
 }
     /**
      * Removes a userAttribute, given a user and attribute name. 
      * @param login A String with the user login name.
      * @param attributeName A String with the attribute name.
      * @return status A string with deletion status.
      * @throws AAAException.
      */
     public String remove(String login, String attributeName)
                   throws AAAException {

         String status = null;

         UserDAO userDAO = new UserDAO(this.dbname);
         User user = (User) userDAO.queryByParam("login", login);

         AttributeDAO attributeDAO = new AttributeDAO(this.dbname);
         Attribute attribute = (Attribute) attributeDAO.queryByParam(
                                              "attributeName", attributeName);

         String hsql = "from UserAttribute " + 
                       "where userId = :userId and " +
                       "attributeId = :attributeId";
         UserAttribute rp = (UserAttribute) this.getSession().createQuery(hsql)
                       .setInteger("userId", user.getId())
                       .setInteger("attributeId", attribute.getId())
                       .setMaxResults(1)
                       .uniqueResult();
         super.remove(rp);
         return status;
     }
}
    
    
    
 
