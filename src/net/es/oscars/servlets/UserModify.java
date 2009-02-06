package net.es.oscars.servlets;

import java.io.*;
import java.util.*;
import java.rmi.RemoteException;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;
import net.sf.json.*;

import net.es.oscars.aaa.Attribute;
import net.es.oscars.aaa.User;
import net.es.oscars.aaa.Institution;
import net.es.oscars.aaa.AuthValue;
import net.es.oscars.aaa.AAAException;
import net.es.oscars.rmi.RmiUtils;
import net.es.oscars.rmi.aaa.AaaRmiInterface;
import net.es.oscars.rmi.model.ModelObject;
import net.es.oscars.rmi.model.ModelOperation;


public class UserModify extends HttpServlet {
    private Logger log = Logger.getLogger(UserModify.class);

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String methodName = "UserModify";
        this.log.info(methodName + ":start");

        //User requester = null;
        boolean self = false; // is user modifying own profile
        boolean setPassword = false;

        UserSession userSession = new UserSession();
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        String userName = userSession.checkSession(out, request, methodName);
        if (userName == null) {
            this.log.warn("No user session: cookies invalid");
            return;
        }
        String profileName = request.getParameter("profileName");

        Map<String, Object> outputMap = new HashMap<String, Object>();
        // got here by clicking on a name in the user list
        if (profileName != null) {
            if (profileName.equals(userName)) {
                self = true;
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
        User user = null;
        ArrayList<String> newRoles = new ArrayList<String>();
        ArrayList<String> curRoles = new ArrayList<String>();
        try {
            AaaRmiInterface rmiClient =
                RmiUtils.getAaaRmiClient(methodName, log);
            AuthValue authVal =
                rmiClient.checkAccess(userName, "Users", "modify");
            if ((authVal == AuthValue.ALLUSERS) ||
                    ( self && (authVal == AuthValue.SELFONLY))) {
                user = ServletUtils.getUser(profileName, rmiClient, out, log);
            } else {
                ServletUtils.handleFailure(out,"no permission to modify users",
                                           methodName);
                log.warn(userName + " does not have permission to modify users");
                return;
            }
            if (user == null) {
                String msg = "User " + profileName + " does not exist";
                ServletUtils.handleFailure(out, msg, methodName);
            }
            List<Attribute> attributesForUser =
                ServletUtils.getAttributesForUser(profileName, rmiClient, out,
                                                  log);
            List<Attribute> allAttributes =
                ServletUtils.getAllAttributes(rmiClient, out, log);

            this.convertParams(request, user);
            String password = request.getParameter("password");
            String confirmationPassword =
                request.getParameter("passwordConfirmation");
            // handle password modification if necessary
            // check will return null, if password is  not to be changed
            String newPassword = ServletUtils.checkPassword(password,
                                                          confirmationPassword);
            if (newPassword != null) {
                user.setPassword(newPassword);
                setPassword = true;
            }

            // see if any attributes need to be added or removed
            if (authVal == AuthValue.ALLUSERS) {
                RoleUtils roleUtils = new RoleUtils();
                String roles[] = request.getParameterValues("attributeName");
                for (int i=0; i < roles.length; i++) {
                    roles[i] = ServletUtils.dropDescription(roles[i].trim());
                }
                if (roles[0].equals("None")) {
                    log.info("AddUser: roles = null");
                    newRoles = new ArrayList<String>();
                } else {
                    this.log.info("number of roles input is " + roles.length);
                    newRoles = roleUtils.checkRoles(roles, allAttributes);
                }
                for (Attribute attr : attributesForUser) {
                    curRoles.add(attr.getName());
                }
            }
            for (String role: newRoles) {
                this.log.info("new role: " + role);
            }

            HashMap<String, Object> rmiParams = new HashMap<String, Object>();
            rmiParams.put("user", user);
            rmiParams.put("newRoles", newRoles);
            rmiParams.put("curRoles", curRoles);
            rmiParams.put("objectType", ModelObject.USER);
            rmiParams.put("operation", ModelOperation.MODIFY);
            HashMap<String, Object> rmiResult = new HashMap<String, Object>();
            rmiResult = ServletUtils.manageAaaObject(rmiClient, methodName, log,
                                                     out, rmiParams);
        } catch (Exception e) {
            ServletUtils.handleFailure(out, log, e, methodName);
            return;
        }
        outputMap.put("status", "Profile for user " + profileName +
                      " successfully modified");
        // user may have changed his own attributes
        //authVal = mgr.checkAccess(userName, "Users", "modify");
        // or user may  have changed a target users attributes
        //attrNames =  mgr.getAttrNames(profileName);
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("{}&&" + jsonObject);
        this.log.info(methodName + ":end");
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
    public void convertParams(HttpServletRequest request, User user)
            throws AAAException {

        String strParam = null;
        String DN = null;

        strParam = request.getParameter("institutionName");
        if (strParam != null) {
            Institution institution = new Institution();
            institution.setName(strParam);
            user.setInstitution(institution);
        }
        strParam = request.getParameter("certIssuer");
        if ((strParam != null) && (!strParam.trim().equals(""))) {
            DN = ServletUtils.checkDN(strParam);
        }
        // allow setting existent non-required field to null
        if ((DN != null) || (user.getCertIssuer() != null)) {
            user.setCertIssuer(DN);
        }
        strParam = request.getParameter("certSubject");
        if ((strParam != null) && (!strParam.trim().equals(""))) {
            DN = ServletUtils.checkDN(strParam);
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
}
