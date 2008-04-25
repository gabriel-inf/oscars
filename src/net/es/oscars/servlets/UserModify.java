package net.es.oscars.servlets;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;
import org.hibernate.*;
import net.sf.json.*;

import net.es.oscars.database.HibernateUtil;
import net.es.oscars.aaa.*;
import net.es.oscars.aaa.UserManager.AuthValue;


public class UserModify extends HttpServlet {
    private Logger log;
    private String dbname;
    
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException, ServletException {

        this.log = Logger.getLogger(this.getClass());
        this.dbname = "aaa";
        this.log.info("userModify:start");

        UserManager mgr = null;
        User user = null;
        int userId;
        //User requester = null;
        boolean self = false; // is user modifying own profile
        boolean setPassword = false; 
        List<String> attrNames = new ArrayList<String>();
        AttributeDAO attrDAO = new AttributeDAO(this.dbname);

        UserSession userSession = new UserSession();
        Utils utils = new Utils();
        PrintWriter out = response.getWriter();
        response.setContentType("text/json-comment-filtered");
        String userName = userSession.checkSession(out, request);
        if (userName == null) { return; }

        mgr = new UserManager(this.dbname);
        String profileName = request.getParameter("profileName");
        Session aaa = 
            HibernateUtil.getSessionFactory(this.dbname).getCurrentSession();
        aaa.beginTransaction();
        
        Map outputMap = new HashMap();
        if (profileName != null) { // get here by clicking on a name in the users list
            if (profileName.equals(userName)) { 
                self =true; 
            } else {
                self = false;
            }
        } else { // profileName is null - clicked on userProfile nav tab
            profileName = userName;
            self = true;
        }
        // just in case renamed to oneself
        if (!self) {
            outputMap.put("userDeleteDisplay", Boolean.TRUE);
        } else {
            outputMap.put("userDeleteDisplay", Boolean.FALSE);
        }
        AuthValue authVal = mgr.checkAccess(userName, "Users", "modify");
        if (self) {attrNames = mgr.getAttrNames();}
        else {attrNames = mgr.getAttrNames(profileName);}
 
        
        if ((authVal == AuthValue.ALLUSERS)  ||  ( self && (authVal == AuthValue.SELFONLY))) {
              user= mgr.query(profileName);
              userId=user.getId();
         } else {
            utils.handleFailure(out,"no permission to modify users", aaa,null);
            return;
        }

        if (user == null) {
            String msg = "User " + profileName + " does not exist";
            utils.handleFailure(out, msg, aaa, null);
        }

        try {
            this.convertParams(request, user);
            String password = request.getParameter("password");
            String confirmationPassword =
            request.getParameter("passwordConfirmation");

            // handle password modification if necessary
            // checkpassword will return null, if password is  not to be changed
            String newPassword = utils.checkPassword(password, confirmationPassword);
            if (newPassword != null) {
                user.setPassword(newPassword);
                setPassword = true;
            }
            mgr.update(user,setPassword);
            
            // see if any attributes need to be added or removed
            if (authVal == AuthValue.ALLUSERS) {
                String roles[] = request.getParameterValues("roles");
                ArrayList<Integer> newRoles = null;
                if (roles == null) {
                    log.info("AddUser: roles = null");
                    newRoles = new ArrayList<Integer>();
                } else {
                    this.log.info("number of roles input is " + roles.length);
                    newRoles = utils.convertRoles(roles);
                }
                ArrayList<Integer> curRoles = new ArrayList<Integer>();
                for (String s : attrNames) {
                    curRoles.add(attrDAO.getAttributeId(s));
                }
                /*
                 * form only sets OSCARS-user, OSCARS-service, 
                 * OSCARS-operator, OSCARS-engineer or
                 * OSCARS-administrator so we need to compare those three
                 * values between the new and current.
                 */
                 for (Integer newRoleItem : newRoles) {
                     int intNewRoleItem = newRoleItem.intValue();
                     if (!curRoles.contains(intNewRoleItem)) {
                         this.log.info("add attrId " + intNewRoleItem);
                         this.addUserAttribute(intNewRoleItem, userId);
                     }
                 }
                 for (Integer curRoleItem : curRoles){
                    int intCurRoleItem  = curRoleItem.intValue();
                    if (!newRoles.contains(intCurRoleItem)) {
                         this.log.info("delete attrId " + intCurRoleItem);
                         UserAttributeDAO userAttrDAO = new UserAttributeDAO(
                                                                 this.dbname);
                         userAttrDAO.remove(userId, intCurRoleItem);
                     }
                 }
                 String newRole = request.getParameter("newRole");
                 if ((newRole != null) && !newRole.trim().equals("")) {
                     Attribute newAttr = new Attribute();
                     newAttr.setName(newRole);
                     attrDAO.create(newAttr);
                     try {
                         this.addUserAttribute(attrDAO.getAttributeId(newRole),
                                               userId);
                     } catch (AAAException ex) {
                         this.log.error("oops,no id was assigned by create");
                     }
                 }
             }             
        } catch (AAAException e) {
            utils.handleFailure(out, e.getMessage(), aaa, null);
            return;
        }
        outputMap.put("status", "Profile for user " +
                                profileName + " successfully modified");
        // user may have changed his own attributes
        authVal = mgr.checkAccess(userName, "Users", "modify");
        // or user may  have changed a target users attributes
        attrNames =  mgr.getAttrNames(profileName); 
        outputMap.put("method", "UserModify");
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("/* " + jsonObject + " */");
        aaa.getTransaction().commit();
        this.log.info("UserModify: finish");
    }

    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }

    /**
     * Changes the value of user to correspond to the new input values
     * 
     * @param request - input from modifyUser form
     * @param user in/out as the current user specified in profile name
     *        modified by the parameters in the request.
     * @throws AAAException
     */
    public void convertParams(HttpServletRequest request,
                              User user) 
        throws AAAException {

        String strParam = null;
        String DN = null;
        Utils utils = new Utils();

        strParam = request.getParameter("institutionName");
        if (strParam != null) {
            InstitutionDAO institutionDAO = new InstitutionDAO(this.dbname);
            Institution institution =
                institutionDAO.queryByParam("name", strParam);
            if (institution != null) {
                user.setInstitution(institution);
            }
        }
        strParam = request.getParameter("certIssuer");
        if ((strParam != null) && (!strParam.trim().equals(""))) {
            DN = utils.checkDN(strParam);
        }
        // allow setting existent non-required field to null
        if ((DN != null) || (user.getCertIssuer() != null)) {
            user.setCertIssuer(DN);
        }
        strParam = request.getParameter("certSubject");
        if ((strParam != null) && (!strParam.trim().equals(""))) {
            DN = utils.checkDN(strParam);
        }
        if ((DN != null) || (user.getCertSubject() != null)) {
            user.setCertSubject(DN);
        }
        // required fields by client
        strParam = request.getParameter("lastName");
        if (strParam != null) { user.setLastName(strParam); }
        strParam = request.getParameter("firstName");
        if (strParam != null) { user.setFirstName(strParam); }
        strParam = request.getParameter("emailPrimary");
        if (strParam != null) { user.setEmailPrimary(strParam); }
        strParam = request.getParameter("phonePrimary");
        if (strParam != null) { user.setPhonePrimary(strParam); }
        // doesn't matter if blank
        strParam = request.getParameter("description");
        if ((strParam != null) || (user.getDescription() != null)) {
            user.setDescription(strParam);
        }
        strParam = request.getParameter("emailSecondary");
        if ((strParam != null) || (user.getEmailSecondary() != null)) {
            user.setEmailSecondary(strParam);
        }
        strParam = request.getParameter("phoneSecondary");
        if ((strParam != null) || (user.getPhoneSecondary() != null)) {
            user.setPhoneSecondary(strParam);
        }
        strParam = request.getParameter("activationKey");
        if ((strParam != null) || (user.getActivationKey() != null)) {
            user.setActivationKey(strParam);
        }
    }

    private void addUserAttribute(int attrId, int userId){

        UserAttributeDAO userAttrDAO = new UserAttributeDAO(this.dbname);
        UserAttribute userAttr = new UserAttribute();

        userAttr.setAttributeId(attrId);
        userAttr.setUserId(userId);
        userAttrDAO.create(userAttr);
    }
}

 

