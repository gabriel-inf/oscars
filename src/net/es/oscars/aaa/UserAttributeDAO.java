package net.es.oscars.aaa;

import net.es.oscars.database.GenericHibernateDAO;

/*
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 * @author Mary Thompson (mrthompson@lbl.gov)
 */
public class UserAttributeDAO
    extends GenericHibernateDAO<UserAttribute, Integer> {

    String dbname;

    public UserAttributeDAO(String dbname) {
        this.setDatabase(dbname);
        this.dbname = dbname;
    }

    /**
     * Removes a userAttribute, given a user and attribute name. 
     * @param userName A String with the user name.
     * @param attributeName A String with the attribute name.
     * @return status A string with deletion status.
     * @throws AAAException.
     */
    public String remove(String userName, String attributeName)
                  throws AAAException {
        String status = null;

        UserDAO userDAO = new UserDAO(this.dbname);
        User user = (User) userDAO.queryByParam(
                                             "userName", userName);

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
