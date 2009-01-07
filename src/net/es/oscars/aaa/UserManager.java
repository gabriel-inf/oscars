package net.es.oscars.aaa;

import java.util.*;
import org.apache.log4j.*;

import net.es.oscars.PropHandler;


/**
 * UserManager handles all AAA method calls at this time, and makes
 * all calls to data access objects.
 *
 * @author David Robertson, Mary Thompson, Jason Lee, Evangelos Chaniotakis
 */
public class UserManager {
    private Logger log;
    private String salt;
    private String dbname;

    public UserManager(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.dbname = dbname;
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("aaa", true);
        this.salt = props.getProperty("salt");
    }

    /** Creates a system user.
     * This constructor is used by the test suite
     *
     * @param user a user instance containing user parameters
     * @param institutionName a string with the new user's affiliation
     */
    public void create(User user, String institutionName)
            throws AAAException {

        this.create(user, new ArrayList<Integer>());
    }

    /** Creates a system user.
     *
     * @param user a user instance containing user parameters
     * @param institutionName a string with the new user's affiliation
     * @param roles a list of attributes for the user
     */
    public void create(User user, List<Integer> roles)
            throws AAAException {

        User currentUser = null;
        String institutionName = user.getInstitution().getName();

        this.log.info("create.start: " + user.getLogin());
        this.log.debug("create.institution: " + institutionName);

        String userName = user.getLogin();
        this.log.debug("create.userName: " + userName);

        UserDAO userDAO = new UserDAO(this.dbname);
        currentUser = userDAO.query(userName);
        if (currentUser != null) {
            throw new AAAException("User " + userName + " already exists.");
        }

        InstitutionDAO institutionDAO = new InstitutionDAO(this.dbname);
        Institution inst = institutionDAO.queryByParam("name", institutionName);

        if (inst == null) {
            throw new AAAException("Institution "+institutionName+" not found!");
        }

        user.setInstitution(inst);
        // encrypt the password before persisting it to the database
        String encryptedPwd = Jcrypt.crypt(this.salt, user.getPassword());
        user.setPassword(encryptedPwd);

        userDAO.create(user);
        // add any attributes for this user to the UserAttributes table
        if (roles != null) {
            int UserId = user.getId();
            UserAttributeDAO uaDAO = new UserAttributeDAO(this.dbname);
            AttributeDAO attrDAO = new AttributeDAO(this.dbname);
            for (int attrId: roles) {
                UserAttribute ua = new UserAttribute();
                ua.setUser(user);
                Attribute attr = attrDAO.findById(attrId, false);
                ua.setAttribute(attr);
                uaDAO.create(ua);
            }
        }
        this.log.info("create.finish: " + user.getLogin());
    }

    /**
     * Finds a system user based on their login name.
     *
     * @param userName a user's login name
     * @return user the corresponding user, if one exists
     */
    public User query(String userName) {
        User user = null;

        this.log.info("query.start: " + userName);
        UserDAO userDAO = new UserDAO(this.dbname);
        user = userDAO.query(userName);
        this.log.info("query.finish: " + userName);
        return user;
    }

    /**
     * Lists all users.
     *
     * @return a list of users
     */
    public List<User> list() {
        List<User> users = null;

        this.log.info("list.start");
        UserDAO userDAO = new UserDAO(this.dbname);
        users = userDAO.list();
        this.log.info("list.finish");
        return users;
    }

    /**
     * Retrieves all institutions that current users could belong to.
     *
     * @return a list of institutions
     */
    public List<Institution> getInstitutions() {
        List<Institution> institutions = null;

        this.log.info("getInstitutions.start");
        InstitutionDAO institutionDAO = new InstitutionDAO(this.dbname);
        institutions = institutionDAO.list();
        this.log.info("getInstitutions.finish");
        return institutions;
    }

    /**
     * Removes a user and all their attributes
     *
     * @param userName a string with the user's login name
     */
    public void remove(String userName) throws AAAException {

        this.log.info("remove.start: " + userName);
        UserDAO userDAO = new UserDAO(this.dbname);
        UserAttributeDAO userAttrDAO = new UserAttributeDAO(this.dbname);

        User user = userDAO.query(userName); // check to make sure user exists
        if (user == null) {
            throw new AAAException("Cannot remove user " + userName +
                                   ". The user does not exist.");
        }
        int userId = user.getId();
        userAttrDAO.removeAllAttributes(userId);
        userDAO.remove(user);
        this.log.info("remove.finish: " + userName);
    }

    /**
     * Updates a user's profile in the database.
     *
     * @param user a transient user instance with modified field(s).
     * @param newPassword - if true the password in user is new and needs to be
     *      encrypted; if false the password is the current, already encrypted
     *      value.
     */
    public void update(User user, boolean newPassword) throws AAAException {

        this.log.info("update.start: " + user.getLogin());
        UserDAO userDAO = new UserDAO(this.dbname);
        String userName = user.getLogin();

        // TODO:  keep next two checks?
        // check whether this person is in the database
        User currentInfo = userDAO.query(userName);
        if (currentInfo == null) {
            throw new AAAException("No such user " + userName + ".");
        }
        // make sure institution is set properly
        if (user.getInstitution() == null) {
            user.setInstitution(currentInfo.getInstitution());
        }
        // make sure password is set properly
        if (newPassword) {
            // encrypt user's new password before persisting
            String encryptedPwd = Jcrypt.crypt(this.salt, user.getPassword());
            user.setPassword(encryptedPwd);
        }
        // persist to the database
        userDAO.update(user);
        this.log.info("update.finish:" + user.getLogin());
    }

    /**
     * Gets user login name, given their DN.
     * Check to see that the user has at least one attribute.
     *
     * @param dn a string with the distinguished name from a certificate
     * @return the login id of the user if found, null if user is not found
     */
    public String loginFromDN(String dn)  throws AAAException {

        this.log.debug("loginFromDN.start");
        UserDAO userDAO = new UserDAO(this.dbname);
        User user = userDAO.fromDN(dn);
        if (user == null) { return null; }
        String login = user.getLogin();
        this.log.debug("got login: " + login);

        UserAttributeDAO userAttrDAO = new UserAttributeDAO(this.dbname);
        List<UserAttribute> userAttributes =
                userAttrDAO.getAttributesByUser(user.getId());
        if (userAttributes.isEmpty()) {
            throw new AAAException(
                    "loginFromDN: no attributes for user " + login + ".");
        }
        this.log.debug("loginFromDN.end");
        return login;
    }

    /**
     * Returns the institution of the user
     *
     * @param login String login name of the user
     *
     * @return String name of the institution of the user, null if user not found
     */
    public String getInstitution (String login){
        UserDAO userDAO = new UserDAO(this.dbname);
        User user = userDAO.query(login);
        if (user == null) {
            return null;
        }
        return user.getInstitution().getName();
    }

    /**
     * Authenticates user via login name and password.
     * Check to see that user exists, the password is correct
     * and that the user has at least one attribute
     *
     * @param userName a string with the user's login name
     * @param password a string with the user's password
     * @param sessionName a string with session name to set if auth successful
     * @return a string with the login name if verification was successful
     */
    public String verifyLogin(String userName, String password, String sessionName)
            throws AAAException {

        User user = null;

        this.log.debug("verifyLogin.start");
        UserDAO userDAO = new UserDAO(this.dbname);
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

        UserAttributeDAO  userAttrDAO = new UserAttributeDAO(this.dbname);
        List<UserAttribute> userAttributes =
                 userAttrDAO.getAttributesByUser(user.getId());
        if (userAttributes == null) {
             throw new AAAException(
                     "verifyLogin: no attributes for user " +userName + ".");
        }
        user.setCookieHash(sessionName);
        userDAO.update(user);
        this.log.debug("verifyLogin.end: " + userName);
        return userName;
    }

    /**
     * Checks session cookie for validity.
     *
     * @param userName string with user's login name
     * @param sessionName string with session cookie value
     * @return boolean indicating whether cookie is valid
     */
    public boolean validSession(String userName, String sessionName) {
        UserDAO userDAO = new UserDAO(this.dbname);
        boolean valid = userDAO.validSession(userName, sessionName);
        return valid;
    }

    /**
     * Gets all the Attributes associated with a user
     *
     * @param userName string with user's login name
     * @return List<Attribute> attributes for user
     */

    public List <Attribute> getAttributesForUser(String targetUser) {
        this.log.debug("getUserAttributes: start");
        List <Attribute> attributes = new ArrayList<Attribute>();
        UserDAO userDAO = new UserDAO(this.dbname);
        UserAttributeDAO userAttrDAO = new UserAttributeDAO(this.dbname);
        User user = userDAO.query(targetUser);
        if (user == null) {
            return attributes;
        }
        List <UserAttribute> userAttributes = userAttrDAO.getAttributesByUser(user.getId());
        if (userAttributes == null) {
            return attributes;
        }
        for (UserAttribute ua : userAttributes) {
            attributes.add(ua.getAttribute());
        }

        return attributes;
    }


    /**
     * Checks to make sure that that user has the permission to use the
     *     resource by checking for the corresponding triplet in the
     *     authorizations table. This method is called for everything except
     *     createReservation, so the constraints of interest are 'allUsers' and 'mySite'.
     *     The least restrained access that any of the user's attributes grants
     *     is returned, where ALLUSERS > SITEONLY > SELFONLY > DENIED. If there is only an
     *     authorization with no constraint, the default of SELFONLY is returned.
     *
     * @param userName a string with the login name of the user
     * @param resourceName a string identifying a resource
     * @param permissionName a string identifying a permission
     * @return one of DENIED, SELFONLY, SITEONLY, ALLUSERS
     */
    public AuthValue checkAccess(String userName, String resourceName, String permissionName) {
        this.log.debug("checkAccess.start");

        AuthValue retVal = null;
        String retValSt = "DENIED";

        List<Authorization> auths = null;
        AuthorizationDAO authDAO = new AuthorizationDAO(this.dbname);

        Boolean site = false;
        Boolean self = false;

        HashMap<String, Object> urp = this.getURPObjects(userName, resourceName, permissionName);
        if (urp == null) {
            // user has  no attributes or other error
            return AuthValue.DENIED;
        }

        Permission permission = (Permission) urp.get("permission");
        Resource resource = (Resource) urp.get("resource");
        User user = (User) urp.get("user");

        /* check to see if any of the users attributes grants this
         * authorization, and look for a selfOnly or MySite constraint
         */
        List<Attribute> attributes = this.getAttributesForUser(userName);
        for (Attribute attribute : attributes) {
            auths = authDAO.query(attribute.getId(), resource.getId(), permission.getId());
            if (auths.isEmpty()) {
               // this.log.debug("attrId:  no authorization" );
                continue;
            }
            for (Authorization auth : auths) {
                String constraintName = auth.getConstraint().getName();
                if (constraintName.equals("none")) {
                    // found an authorization with no constraints,
                    // user is allowed for self only
                    // this.log.debug("attrId: authorized for SELFONLY");
                    self = true;
                } else if (constraintName.equals("my-site")) {
                    //if (auth.getConstraintValue().equals("true") {
                        // found a constrained authorization, remember it
                        site = true;
                        // this.log.debug("checkAccess MYSITE access");
                    //}
                } else if (constraintName.equals("all-users")) {
                    // leave this test in for historic reasons, the value should always be true
                    if (auth.getConstraintValue().equals("true")) {
                        // found an authorization with allUsers allowed,
                        // highest level access, so return it
                        this.log.info("checkAccess: " +userName + ":" + resourceName + ":" + resourceName + ":ALLUSERS");
                        return AuthValue.ALLUSERS;
                   } else {
                        // found a self-only constrained authorization, remember it
                        self=true;
                    }
                }
            } // end of all authorizations for this attribute
        } // end of all attributes
        if (site) {
            retVal = AuthValue.MYSITE;
        } else if (self) {
            retVal = AuthValue.SELFONLY;
        }

        if (retVal == null ) {
            this.log.info("checkAccess: no auth found for " +userName + ":" + resourceName + ":" + resourceName);
            retVal = AuthValue.DENIED;
        }
        retValSt = retVal.toString();
        this.log.info("checkAccess: " +userName + ":" + resourceName + ":" + resourceName + ":" + retValSt);
        return retVal;
    }


    /**
     * Checks to make sure that that user has the permission to
     * perform "unsafe" operations on reservations.
     *
     * @param userName a string with the login name of the user
     * @return true or false
     */
    public boolean checkAccessForUnsafe(String userName) {

        // get list of  attributes for this user
        List<Attribute> attributes = this.getAttributesForUser(userName);

        if (attributes.isEmpty())  {
            return false;
        }

        for (Attribute attribute: attributes) {
            if (attribute.getName().equals("OSCARS-engineer")){
                return true;
            }
        }
        return false;
    }



    /**
     * Checks to make sure that that user has the permission to use the
     *     resource by checking for the corresponding quadruplet in the
     *     authorizations table. This method is only called for
     *     createReservation, so the constraints of interest are max-bandwidth,
     *     max-duration, specify-path-constraints, and specify-gri
     *
     * @param userName a string with the login name of the user
     * @param resourceName a string identifying a resource
     * @param permissionName a string identifying a permission
     * @param reqBandwidth int with bandwidth requested
     * @param reqDuration int with reservation duration, -1 means persistent
     * @param specPathElems boolean, true means pathElements have been input
     * @return one of DENIED, SELFONLY, ALLUSERS
     *
     * Note: SELFONLY and ALLUSERS don't make sense and aren't used
     *       MYSITE might make sense -mrt
     */
    public AuthValue checkModResAccess(String userName, String resourceName,
            String permissionName, int reqBandwidth, int reqDuration,
                                   boolean specPathElems, boolean specGRI) {

        AuthorizationDAO authDAO = new AuthorizationDAO(this.dbname);
        // set default values
        boolean bandwidthAllowed = true;
        boolean durationAllowed = true;
        boolean specifyPE = false;
        boolean specifyGRI = false;
        AuthValue retVal = AuthValue.DENIED;

        this.log.info("checkModResAccess.start");
        HashMap<String, Object> urp = this.getURPObjects(userName, resourceName, permissionName);
        if (urp == null) {
            // user has  no attributes or other error
            return AuthValue.DENIED;
        }
        Permission permission = (Permission) urp.get("permission");
        Resource resource = (Resource) urp.get("resource");
        User user = (User) urp.get("user");


        /* check to see if any of the users attributes grants this
         * authorization and look for a all-users constraint */
        List<Attribute> attributes = this.getAttributesForUser(userName);

        for (Attribute attribute: attributes) {
            List<Authorization> auths = authDAO.query(attribute.getId(), resource.getId(), permission.getId());
            if (auths.isEmpty()) {
                continue;
            }
            for (Authorization auth : auths) {

                retVal = AuthValue.SELFONLY;  // minimum authorization
                String constraintName = auth.getConstraint().getName();
                if (constraintName.equals("none")) {
                    continue;
                }
                if (constraintName.equals("max-bandwidth")) {
                    if (reqBandwidth > Integer.parseInt(auth.getConstraintValue()) ) {
                        bandwidthAllowed = false;
                    }
                } else if (constraintName.equals("max-duration")) {
                    if (reqDuration > Integer.parseInt(auth.getConstraintValue()) ) {
                        durationAllowed = false;
                    }
                } else if (constraintName.equals("specify-path-elements")) {
                    specifyPE = true;
                } else if (constraintName.equals("specify-gri")){
                    specifyGRI = true;
                } else if (constraintName.equals("all-users")){
                    if (auth.getConstraintValue().equals("true")) {
                        retVal = AuthValue.ALLUSERS;
                    }
                }
            }
        }

        if (!bandwidthAllowed || !durationAllowed) {
            this.log.info("denied, over bandwidth or duration limits");
            retVal= AuthValue.DENIED;
        }
        if (specPathElems && !specifyPE) {
            this.log.info("denied, not permitted to specify path");
            retVal = AuthValue.DENIED;
        }
        if (specGRI && !specifyGRI) {
            this.log.info("denied, not permitted to specify GRI");
            retVal = AuthValue.DENIED;
        }

        this.log.info("checkModResAccess.finish: " + retVal);
        return retVal;
    }

    /**
     *  Checks that the user, resource and permission all exist and get the
     *  list of attributes associated with the user. If all is well the
     *  resourceId, attibuteId and attribute list parameters are set and true
     *  is returned.
     *
     *  side-effects - resourceId is set to the db id of the resource
     *                 permissionid is set to the db id of the permission
     *                 userAttributes is set to the list of all user attributes
     *
     * @param userName a string with the login name of the user
     * @param resourceName a string identifying a resource
     * @param permissionName a string identifying a permission
     * @return true if everything was found, false otherwise.
     */
    private HashMap<String, Object> getURPObjects(String userName, String resourceName, String permissionName) {

        HashMap<String, Object> result = new HashMap<String, Object>();


        UserDAO userDAO = new UserDAO(this.dbname);
        User user = userDAO.query(userName);
        if (user == null) { return null; }
        result.put("user", user);

        ResourceDAO resourceDAO = new ResourceDAO(this.dbname);
        Resource resource = (Resource) resourceDAO.queryByParam("name", resourceName);
        if (resource == null) { return null; }
        result.put("resource", resource);

        PermissionDAO permissionDAO = new PermissionDAO(this.dbname);
        Permission permission = (Permission) permissionDAO.queryByParam("name", permissionName);
        if (permission == null) { return null; }
        result.put("permission", permission);
        return result;
    }
}
