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
    private List<UserAttribute> userAttrs = null; // set by checkAccess, checkModResAccess
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
     * This constructor is used by the test suite
     *
     * @param user a user instance containing user parameters
     * @param institutionName a string with the new user's affiliation
     */
    public void create(User user, String institutionName)
            throws AAAException {

        this.create(user, institutionName, null);
    }

    /** Creates a system user.
     *
     * @param user a user instance containing user parameters
     * @param institutionName a string with the new user's affiliation
     * @param roles a list of attributes for the user
     */
    public void create(User user, String institutionName, List<Integer> roles)
            throws AAAException {

        User currentUser = null;

        this.log.info("create.start: " + user.getLogin());
        this.log.debug("create.institution: " + institutionName);

        String userName = user.getLogin();
        UserDAO userDAO = new UserDAO(this.dbname);
        currentUser = userDAO.query(userName);
        this.log.debug("create.userName: " + userName);
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
        // add any attributes for this user to the UserAttributes table
        if (roles != null) {
            int UserId = user.getId();
            UserAttributeDAO uaDAO = new UserAttributeDAO(this.dbname);  
            for (int attrID : roles) {
                UserAttribute ua = new UserAttribute();
                ua.setUserId(UserId);
                ua.setAttributeId(attrID);
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
     * Removes a reservation system user and all their attributes
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
     * @return String name of the institution of the user
     */
    public String getInstitution (String login){
	UserDAO userDAO = new UserDAO(this.dbname);
	User user = userDAO.query(login);
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
    public String verifyLogin(String userName, String password,
                              String sessionName)
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

    /* return a list of the user's attributes
     * 
     * @returns a list of the attribute names for user associated with 
     *     manager instance
     */
    public List <String> getAttrNames () {
        this.log.debug("getAttrNames: start");
        ArrayList <String> attrNames = new ArrayList<String>();
        AttributeDAO attrDAO = new AttributeDAO(this.dbname);
        if (this.userAttrs != null){
            for (UserAttribute ua : this.userAttrs){
                attrNames.add(attrDAO.getAttributeName(ua.getAttributeId()));
            }   
        }
        this.log.debug("getAttrNames: finish");
        return attrNames;
    }

    /* return a list of the user's attributes
     * 
     * @param userlogin string containing a user login name
     * @returns a list of the attribute names for this  user
     */
    public List <String> getAttrNames (String targetUser) {
        this.log.debug("getAttrNames: start");
        ArrayList <String> attrNames = new ArrayList<String>();
        AttributeDAO attrDAO = new AttributeDAO(this.dbname);
        UserAttributeDAO userAttrDAO = new UserAttributeDAO(this.dbname);
        UserDAO userDAO = new UserDAO(this.dbname);

        User user = userDAO.query(targetUser);
        if (user == null) { return attrNames; }
        this.userAttrs = userAttrDAO.getAttributesByUser(user.getId());
        if (this.userAttrs != null) {
            for (UserAttribute ua : this.userAttrs) {
                attrNames.add(attrDAO.getAttributeName(ua.getAttributeId()));
            }   
        }
        this.log.debug("getAttrNames: finish");
        return attrNames;
    }

    /**
     * Authorization values returned by checkAccess. <br>
     * DENIED means the requested action is not allowed<br>
     * ALLUSERS means the requested action is allowed on objects that belong
     *     to any user<br>
     * MYSITE means the requested actions is allowed only on objects that 
     * 	    belong to the same site as the requester<br>
     * SELFONLY  means the requested action is allowed only on objects that
     *      belong to the requester.    
     * @author mrt
     *
     */
    public enum AuthValue { DENIED, ALLUSERS, MYSITE, SELFONLY};
    
   
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
    public AuthValue checkAccess(String userName, String resourceName,
                                 String permissionName) {

        List<Authorization> auths = null;
        AuthorizationDAO authDAO = new AuthorizationDAO(this.dbname);
        UserAttribute currentAttr = null;
        AuthValue retVal =null;
        String retValSt = "DENIED";
        Boolean site = false;
        Boolean self = false;
         
        this.log.info("checkAccess.start");
        if (!this.getIds(userName, resourceName, permissionName)) {
            // user has  no attributes 
            return AuthValue.DENIED;
        }
        /* check to see if any of the users attributes grants this
         * authorization, and look for a selfOnly or MySite constraint
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
                    // this.log.debug("attrId: authorized for SELFONLY");
                    self=true;
                } else if (auth.getConstraintName().equals("my-site")) {
                    if (auth.getConstraintValue().intValue() == 1) {
                        // found a constrained authorization, remember it
                        site=true;
                        // this.log.debug("checkAccess MYSITE access");
                    }
                }
                else if (auth.getConstraintName().equals("all-users")) {
                    if (auth.getConstraintValue().intValue() == 1) {
                        // found an authorization with allUsers allowed,
                        // highest level access, so return it
                        this.log.info("checkAccess:finish ALLUSERS access for "
                                + permissionName + " on " + resourceName);
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
            this.log.info("No authorizations found for user " + userName +
                          ", permission " + permissionName +
                          ", resource " + resourceName);
            retVal = AuthValue.DENIED;
        }
        retValSt = retVal.toString();
        this.log.info("checkAccess.finish: " + retValSt + " access for " 
                + permissionName + " on " + resourceName);
        return retVal;
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

        List<Authorization> auths = null;
        AuthorizationDAO authDAO = new AuthorizationDAO(this.dbname);
        UserAttribute currentAttr = null;
        // set default values
        boolean bandwidthAllowed = true;
        boolean durationAllowed = true;
        boolean specifyPE = false;
        boolean specifyGRI = false;
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
            /*
            this.log.debug("looking up authorization for attrId: " +
                           currentAttr.getAttributeId());
            */
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
                } else if (auth.getConstraintName().equals("max-bandwidth")) {
                    this.log.debug("Allowed bandwidth: " +
                                   auth.getConstraintValue() +
                                   ", requested bandwidth: " + reqBandwidth);
                    if (reqBandwidth > auth.getConstraintValue() ) {
                        // found an authorization that limits the bandwidth
                        bandwidthAllowed = false;
                    }
                } else if (auth.getConstraintName().equals("max-duration")) {
                    this.log.debug("Allowed duration: " +
                                   auth.getConstraintValue() +
                                   ", requested duration: " + reqDuration);
                    if (reqDuration > auth.getConstraintValue()) {
                        // found an authorization that limits the duration
                        durationAllowed = false;
                    }
                } else if (auth.getConstraintName().equals(
                                                    "specify-path-elements")) {
                    if (auth.getConstraintValue() == 1) {
                        specifyPE = true;
                    }
                } else if (auth.getConstraintName().equals("specify-gri")){
                    if (auth.getConstraintValue() == 1) {
                        specifyGRI = true;
                    }
                }else if (auth.getConstraintName().equals("all-users")){
                    if (auth.getConstraintValue() == 0) {
                        retVal = AuthValue.SELFONLY;
                    } else { retVal = AuthValue.ALLUSERS; }
                } // end of looking at constraint
            }     // end of loop over authorizations for one attribute
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
        /*
        this.log.info("getIds: userId, " + user.getId() + " permissionId, " +
                      this.permissionId + " resourceId, " + this.resourceId);
        */
        if (this.userAttrs.isEmpty())  {
            this.log.info("getIds: userAttrs is empty");
            return false;
        } else {
            return true;
        }
    }
}
