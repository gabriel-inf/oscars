package net.es.oscars.aaa;

import java.util.*;
import org.apache.log4j.*;

import net.es.oscars.PropHandler;


/**
 * UserManager handles all AAA method calls at this time, and makes
 * all calls to data access objects.
 *
 * @author David Robertson, Mary Thompson, Jason Lee
 */
public class UserManager {
    private Logger log;
    private String salt;
    private String dbname;
    private List<UserAttribute> userAttrs = null;
    private int resourceId;
    private int permissionId;
    
    public UserManager(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.dbname = dbname;
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("aaa", true);
        this.salt = props.getProperty("salt");
    }

    /** Creates a system user.
     *
     * @param user a user instance containing user parameters
     * @param institutionName a string with the new user's affiliation
     */
    public void create(User user, String institutionName)
            throws AAAException {
        User currentUser = null;

        this.log.info("create.start: " + user.getLogin());
        this.log.info("create.institution: " + institutionName);

        String userName = user.getLogin();
        UserDAO userDAO = new UserDAO(this.dbname);
        currentUser = userDAO.query(userName);
        this.log.info("create.userName: " + userName);
        // check whether this entity is already in the database
        if (currentUser != null) {
            throw new AAAException("User " + userName + " already exists.");
        }
        InstitutionDAO institutionDAO = new InstitutionDAO(this.dbname);
        Institution inst =
            institutionDAO.queryByParam("name", institutionName);
        user.setInstitution(inst);
        // encrypt the password before persisting it to the database
        String encryptedPwd = Jcrypt.crypt(this.salt, user.getPassword());
        user.setPassword(encryptedPwd);

        userDAO.create(user);
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
     * Removes a reservation system user.
     *
     * @param userName a string with the user's login name
     */
    public void remove(String userName) throws AAAException {

        this.log.info("remove.start: " + userName);
        UserDAO userDAO = new UserDAO(this.dbname);
        User user = userDAO.query(userName); // check to make sure user exists
        if (user == null) {
            throw new AAAException("Cannot remove user " + userName +
                                   ". The user does not exist.");
        }
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
     * Authenticates user via login name and password.
     * Check to see that user exists, the password is correct
     * and that the user has at least one attribute
     *
     * @param userName a string with the user's login name
     * @param password a string with the user's password
     * @return a string with the login name if verification was successful
     */
    public String verifyLogin(String userName, String password)
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
        this.log.debug("verifyLogin.end: " + userName);
        return userName;
    }


    public enum AuthValue { DENIED, ALLUSERS, SELFONLY };
    
   
    /**
     * Checks to make sure that that user has the permission to use the
     *     resource by checking for the corresponding triplet in the
     *     authorizations table. This method is called for everything except 
     *     createReservation, so the only constraint of interest is 'allUsers'.
     *     If it exists, it is used to determine if allUsers or selfOnly should
     *     be returned.
     *
     * @param userName a string with the login name of the user
     * @param resourceName a string identifying a resource
     * @param permissionName a string identifying a permission
     * @return one of DENIED, SELFONLY, ALLUSERS
     */
    public AuthValue checkAccess(String userName, String resourceName,
                                 String permissionName) {

        List<Authorization> auths = null;
        AuthorizationDAO authDAO = new AuthorizationDAO(this.dbname);
        UserAttribute currentAttr = null;
        AuthValue retVal =null;
         
        this.log.info("checkAccess.start");
        if (!this.getIds(userName, resourceName, permissionName)) {
            // user has  no attributes 
            return AuthValue.DENIED;
        }
        /* check to see if any of the users attributes grants this
         * authorization, and look for a selfOnly constraint
         */
        Iterator attrIter = this.userAttrs.iterator();
        while (attrIter.hasNext()) {
            currentAttr = (UserAttribute) attrIter.next();
            this.log.debug("attrId: " + currentAttr.getAttributeId() +
                           ", resourceId: " + this.resourceId +
                           ", permissionId: " + this.permissionId);
            auths = authDAO.query(currentAttr.getAttributeId(),
                                  this.resourceId, this.permissionId);
            if (auths.isEmpty()) { 
                this.log.debug("attrId:  no authorization" );
                continue;
            } 
            Iterator authItr = auths.iterator();
            Authorization auth = null;
            while (authItr.hasNext()){
                auth = (Authorization) authItr.next();
                if (auth.getConstraintName() == null) {
                    // found an authorization with no constraints,
                    // user is allowed for self only
                    this.log.debug("attrId: authorized with no constraint");
                    retVal =  AuthValue.SELFONLY;
                } else if (auth.getConstraintName().equals("all-users")) {
                    if (auth.getConstraintValue() ==
                            AuthValue.ALLUSERS.ordinal()) {
                        // found an authorization with allUsers allowed,
                        // user is allowed
                        this.log.info("checkAccess.finish: ALLUSERS access");
                        return AuthValue.ALLUSERS;
                    } else {
                        // found a contrained authorization, remember it
                        // and see if we can do better
                        retVal = AuthValue.SELFONLY;
                    }
                }
            } // end of all authorizations for this attribute
        }
        if (retVal == null ) {
            this.log.info("No authorizations found for user " + userName +
                          ", permission " + permissionName +
                          ", resource " + resourceName);
            retVal = AuthValue.DENIED;
        }
        this.log.info("checkAccess.finish: " + retVal);
        return retVal;
    }   
    /**
     * Checks to make sure that that user has the permission to use the
     *     resource by checking for the corresponding triplet in the
     *     authorizations table. This method is only called for
     *     createReservation, so the constraints of interest are max-bandwidth,
     *     max-duration and specify-path-constraints
     *
     * @param userName a string with the login name of the user
     * @param resourceName a string identifying a resource
     * @param permissionName a string identifying a permission
     * @param reqBandWidth int with bandwidth requested
     * @param reqDuration int with reservation duration, -1 means persistant
     * @param specPathElems boolean, true means pathElements have been input
     * @return one of DENIED, SELFONLY, ALLUSERS
     */
    public AuthValue checkModResAccess(String userName, String resourceName,
            String permissionName, int reqBandwidth, int reqDuration, 
                                   boolean specPathElems) {

        List<Authorization> auths = null;
        AuthorizationDAO authDAO = new AuthorizationDAO(this.dbname);
        UserAttribute currentAttr = null;
        boolean bandwidthAllowed = false;
        boolean durationAllowed = false;
        boolean specifyPE = false;
        AuthValue retVal = AuthValue.DENIED;
         
        this.log.info("checkModResAccess.start");
        if (!this.getIds(userName, resourceName, permissionName)) {
            // no attributes found for user
            return retVal;
        }
        /* check to see if any of the users attributes grants this
         * authorization and look for a all-users constraint */
        Iterator attrIter = this.userAttrs.iterator();
        while (attrIter.hasNext()) {
            currentAttr = (UserAttribute) attrIter.next();
            this.log.debug("looking up authorization for attrId: " +
                           currentAttr.getAttributeId());
            auths = authDAO.query(currentAttr.getAttributeId(),
                                  this.resourceId, this.permissionId);
            if (auths.isEmpty()) {                   
                this.log.debug("no authorization for attrId: " +
                               currentAttr.getAttributeId());
                continue;
            } 
            Iterator authItr = auths.iterator();
            while (authItr.hasNext()) {
                Authorization auth = (Authorization) authItr.next();
                // no constraint
                if (auth.getConstraintName() == null) {
                    retVal = AuthValue.SELFONLY;
                    bandwidthAllowed = true;
                    durationAllowed = true;
                } else if (auth.getConstraintName().equals("max-bandwidth")) {
                    this.log.debug("Allowed bandwidth; " +
                                   auth.getConstraintValue() +
                                   "requested bandwidth: " + reqBandwidth);
                    if (reqBandwidth <= auth.getConstraintValue() ) {
                        // found an authorization that allows the bandwidth
                        bandwidthAllowed = true;
                    }
                } else if (auth.getConstraintName().equals("max-duration")) {
                    this.log.debug("Allowed duration: " +
                                   auth.getConstraintValue() +
                                   "requested duration: " + reqDuration);
                    if (reqDuration <= auth.getConstraintValue()) {
                        // found an authorization that allows the duration
                        durationAllowed = true;
                    }
                } else if (auth.getConstraintName().equals(
                                                    "specify-path-elements")) {
                    if (auth.getConstraintValue() == 1) {
                        specifyPE = true;
                    }
                } else if (auth.getConstraintName().equals("all-users")){
                    if (auth.getConstraintValue() == 0) {
                        retVal = AuthValue.SELFONLY;
                    } else { retVal = AuthValue.ALLUSERS; }
                } // end of looking at constraint
            }     // end of loop over authorizations for one attribute
        }

        if (!bandwidthAllowed || !durationAllowed) { 
            this.log.info("denied, over bandwidth or duration limits");
            retVal= AuthValue.DENIED;
        }  else { retVal = AuthValue.SELFONLY; }
        if (specPathElems && !specifyPE) {
            this.log.info("denied, not permitted to specify path");
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
    private boolean getIds(String userName, String resourceName,
                           String permissionName) {

        UserDAO userDAO = new UserDAO(this.dbname);
        User user = userDAO.query(userName);
        if (user == null) { return false; }

        ResourceDAO resourceDAO = new ResourceDAO(this.dbname);
        Resource resource =
                (Resource) resourceDAO.queryByParam("name", resourceName);
        if (resource == null) { return false; }
        this.resourceId = resource.getId();

        PermissionDAO permissionDAO = new PermissionDAO(this.dbname);
        Permission permission = (Permission)
                permissionDAO.queryByParam("name", permissionName);
        if (permission == null) { return false; }
        this.permissionId = permission.getId();

        // get list of  attributes for this user
        UserAttributeDAO userAttrDAO = new UserAttributeDAO(this.dbname);
        this.userAttrs = userAttrDAO.getAttributesByUser(user.getId());
        this.log.info("getIds: userId, " + user.getId() + " permissionId, " +
                      this.permissionId + " resourceId, " + this.resourceId);
        if (this.userAttrs.isEmpty())  {
            this.log.info("getIds: userAttrs is empty");
            return false;
        } else {
            return true;
        }
    }
}
