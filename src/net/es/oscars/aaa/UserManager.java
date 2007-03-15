package net.es.oscars.aaa;

import java.util.*;
import org.hibernate.*;

import net.es.oscars.LogWrapper;
import net.es.oscars.PropHandler;
import net.es.oscars.database.HibernateUtil;


/**
 * UserManager handles all AAA method calls at this time, and makes
 * all calls to data access objects.
 *
 * @author David Robertson, Mary Thompson, Jason Lee
 */
public class UserManager {
    private LogWrapper log;
    private String salt;
    Session session;
    
    public UserManager() {
        this.log = new LogWrapper(getClass());
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("aaa", true);
        this.salt = props.getProperty("salt");
    }

    public void setSession() {
        this.session = 
            HibernateUtil.getSessionFactory("aaa").getCurrentSession();
    }

    /**
     * Creates a system user.
     *
     * @param user A user instance containing user parameters
     * @param institutionName A string with the new user's affiliation
     */
    public void create(User user, String institutionName)
            throws AAAException {
        User currentUser = null;

        this.log.info("create.start", user.getLogin());
        this.log.info("create.institution", institutionName);

        String userName = user.getLogin();
        UserDAO userDAO = new UserDAO();
        userDAO.setSession(this.session);
        currentUser = userDAO.query(userName);
        this.log.info("create.userName", userName);
        System.out.println(currentUser);
        // check whether this entity is already in the database
        if (currentUser != null) {
            throw new AAAException("User " + userName + " already exists.");
        }
        InstitutionDAO institutionDAO = new InstitutionDAO();
        institutionDAO.setSession(this.session);
        Institution inst =
            institutionDAO.queryByParam("name", institutionName);
        user.setInstitution(inst);
        // encrypt the password before persisting it to the database
        String encryptedPwd = Jcrypt.crypt(this.salt, user.getPassword());
        user.setPassword(encryptedPwd);

        userDAO.create(user);
        this.log.info("create.finish", user.getLogin());
    }

    /**
     * Finds a system user based on their login name.
     *
     * @param userName A user's login name
     * @return user The corresponding user, if one exists
     */
    public User query(String userName) {
        User user = null;

        this.log.info("query.start", userName);
        UserDAO userDAO = new UserDAO();
        userDAO.setSession(this.session);
        user = userDAO.query(userName);
        this.log.info("query.finish", userName);
        return user;
    }

    /**
     * Lists all users.
     *
     * @return A list of users
     */
    public List<User> list() {
        List<User> users = null;

        this.log.info("list.start", "");
        UserDAO userDAO = new UserDAO();
        userDAO.setSession(this.session);
        users = userDAO.list();
        this.log.info("list.finish", "");
        return users;
    }

    /**
     * Retrieves all institutions that current users could belong to.
     *
     * @return A list of institutions
     */
    public List<Institution> getInstitutions() {
        List<Institution> institutions = null;

        this.log.info("getInstitutions.start", "");
        InstitutionDAO institutionDAO = new InstitutionDAO();
        institutionDAO.setSession(this.session);
        institutions = institutionDAO.findAll();
        this.log.info("getInstitutions.finish", "");
        return institutions;
    }

    /**
     * Removes a reservation system user.
     *
     * @param userName A string with the user's login name
     */
    public void remove(String userName) throws AAAException {

        this.log.info("remove.start", userName);
        UserDAO userDAO = new UserDAO();
        userDAO.setSession(this.session);
        User user = userDAO.query(userName); // check to make sure user exists
        if (user == null) {
            throw new AAAException("Cannot remove user " + userName +
                                   ". The user does not exist.");
        }
        userDAO.remove(user);
        this.log.info("remove.finish", userName);
    }

    /**
     * Updates a user's profile in the database.
     *
     * @param user A user instance, including a set password
     * @param institutionName A string with the user's affiliation
     * @param passwordConfirm A string with the confirmation password
     */
    public void update(User user, String institutionName,
                       String passwordConfirm) throws AAAException {
        String status = null;
        User currentUser = null;

        this.log.info("update.start", user.getLogin());
        UserDAO userDAO = new UserDAO();
        userDAO.setSession(this.session);
        String userName = user.getLogin();
        String password = user.getPassword();

        // authorization needed
        // check whether this person is in the database
        currentUser = userDAO.query(userName);
        if (user == null) {
            throw new AAAException("No such user " + userName + ".");
        }

        // If the password needs to be updated, make sure there is a
        // confirmation password, and that it matches the given password.
        // Otherwise, use the password from the above query, because the
        // password cannot be null in the db.  With the API, authentication
        // has already been performed via certificate, and with the
        // WBUI, a user must already be in a current session to get here.
        if ((password != null) && (!password.equals("")) &&
                (!password.equals("********"))) {
           if (passwordConfirm == null) {
                throw new AAAException(
                    "Cannot update password without confirmation password");
            } else if (!passwordConfirm.equals(password)) {
                throw new AAAException(
                     "Password and password confirmation do not match");
            }
        } else { password = currentUser.getPassword(); }
        
        // encrypt the password before persisting it to the database
        String encryptedPwd = Jcrypt.crypt(this.salt, password);
        user.setPassword(encryptedPwd);
        InstitutionDAO institutionDAO = new InstitutionDAO();
        institutionDAO.setSession(this.session);
        Institution inst =
            institutionDAO.queryByParam("name", institutionName);
        user.setInstitution(inst);
        userDAO.update(user);
        this.log.info("update.finish", user.getLogin());
    }

    /**
     * Gets user login name, given their DN.
     *
     * @param dn A string with the distinguished name from a certificate
     */
    public String loginFromDN(String dn)  {
        User user = null;

        UserDAO userDAO = new UserDAO();
        userDAO.setSession(this.session);
        this.log.debug("userName", dn);
        user = userDAO.fromDN(dn);
        if (user == null) { return null; }
        this.log.debug("userName.end", user.getLogin());
        return user.getLogin();
    }

    /**
     * Authenticates user via login name and password.
     *
     * @param userName A string with the user's login name
     * @param password A string with the user's password
     * @return A string with the login name if verification was successful
     */
    public String verifyLogin(String userName, String password)
            throws AAAException {

        User user = null;

        UserDAO userDAO = new UserDAO();
        userDAO.setSession(this.session);
        if (userDAO.isAuthenticated(userName)) { return userName; }
        if (password == null) {
            throw new AAAException(
                "verifyLogin: null password given for user " + userName);
        }
        user = userDAO.query(userName);
        if (user == null) {
            throw new AAAException(
                    "verifyLogin: User not registered " + userName + ".");
        }
        if (this.salt == null) {
            throw new AAAException(
                     "verifyLogin: no salt property in oscars.properties");
        }
        // encrypt the password before comparison
        String encryptedPwd = Jcrypt.crypt(this.salt, password);
        if (!encryptedPwd.equals(user.getPassword())) {
            throw new AAAException( "verifyLogin: password is incorrect");
        }
        return userName;
    }

    /**
     * Checks to make sure that that user has the permission to use the
     *     resource by checking for the corresponding triplet in the
     *     authorizations table.
     *
     * @param userName A string with the login name of the user
     * @param resourceName A string identifying a resource
     * @param permissionName A string identifying a permission
     */
    public boolean verifyAuthorized(String userName, String resourceName,
                                    String permissionName) {

        User user = null;
        Resource resource = null;
        Permission permission = null;
        Authorization auth = null;

        UserDAO userDAO = new UserDAO();
        userDAO.setSession(this.session);
        user = userDAO.query(userName);
        if (user == null) { return false; }

        ResourceDAO resourceDAO = new ResourceDAO();
        resourceDAO.setSession(this.session);
        resource = (Resource) resourceDAO.queryByParam("name", resourceName);
        if (resource == null) { return false; }

        PermissionDAO permissionDAO = new PermissionDAO();
        permissionDAO.setSession(this.session);
        permission = (Permission)
                    permissionDAO.queryByParam("name", permissionName);
        if (permission == null) { return false; }

        AuthorizationDAO authDAO = new AuthorizationDAO();
        authDAO.setSession(this.session);
        auth = authDAO.query(user.getId(), resource.getId(),
                                           permission.getId());
        return (auth != null);
    }
}
