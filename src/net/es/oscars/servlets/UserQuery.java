package net.es.oscars.servlets;

import java.io.*;
import java.rmi.RemoteException;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;
import net.sf.json.*;

import net.es.oscars.aaa.Attribute;
import net.es.oscars.aaa.AuthValue;
import net.es.oscars.aaa.User;
import net.es.oscars.aaa.Institution;
import net.es.oscars.rmi.aaa.AaaRmiInterface;
import net.es.oscars.rmi.model.ModelObject;
import net.es.oscars.rmi.model.ModelOperation;


public class UserQuery extends HttpServlet {
    private Logger log = Logger.getLogger(UserQuery.class);

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String methodName = "UserQuery";
        this.log.debug("UserQuery.start");

        boolean self =  false; // is query about the current user
        boolean modifyAllowed = false;

        UserSession userSession = new UserSession();

        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        String userName = userSession.checkSession(out, request, methodName);

        if (userName == null) {
            this.log.error("No user session: cookies invalid");
            return;
        }

        String profileName = request.getParameter("profileName");

        // get here by clicking on a name in the users list
        if ((profileName != null) && !profileName.equals("")) {
            this.log.info("profileName: " + profileName);
            if (profileName.equals(userName)) {
                self = true;
            } else {
                self = false;
            }
        } else { // profileName is null - get here by clicking on tab navigation
            this.log.info("profileName is null, using " + userName);
            profileName = userName;
            self=true;
        }

        Map<String, Object> outputMap = new HashMap<String, Object>();
        if (!self) {
            outputMap.put("userDeleteDisplay", Boolean.TRUE);
        } else {
            outputMap.put("userDeleteDisplay", Boolean.FALSE);
        }


        try {
            AaaRmiInterface rmiClient = ServletUtils.getCoreRmiClient(methodName, log, out);
            AuthValue authVal = ServletUtils.getAuth(userName, "Users", "query", rmiClient, methodName, log, out);

            if ((authVal == AuthValue.ALLUSERS)  ||  ( self && (authVal == AuthValue.SELFONLY))) {
                // either have permission to see others OR see self
             } else {
                ServletUtils.handleFailure(out,"no permission to query users", methodName);
                return;
            }

            /* check to see if user has modify permission for this user
             *     used by contentSection to set the action on submit
             */
            authVal = ServletUtils.getAuth(userName, "Users", "modify", rmiClient, methodName, log, out);

            if ((authVal == AuthValue.ALLUSERS) ||
                (self && (authVal == AuthValue.SELFONLY))) {
                modifyAllowed = true;
            } else {
                modifyAllowed = false;
            }


            User targetUser = null;


            List<Attribute> attributesForUser = null;

            if (self) {
                attributesForUser = ServletUtils.getAttributesForUser(userName, rmiClient, out, log);
                targetUser = ServletUtils.getUser(userName, rmiClient, out, log);
            } else {
                attributesForUser = ServletUtils.getAttributesForUser(profileName, rmiClient, out, log);
                targetUser = ServletUtils.getUser(profileName, rmiClient, out, log);
            }

            List<Attribute> allAtributes = ServletUtils.getAllAttributes(rmiClient, out, log);
            List<Institution> institutions = ServletUtils.getAllInstitutions(rmiClient, out, log);

            this.contentSection( outputMap, targetUser, modifyAllowed,
                    (authVal == AuthValue.ALLUSERS),
                    institutions, attributesForUser, allAtributes);

        } catch (RemoteException ex) {
            return;
        }






        outputMap.put("status", "Profile for user " + profileName);
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("{}&&" + jsonObject);

        this.log.debug("UserQuery.end");
    }

    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }

    /**
     * writes out the parameter values that are the result of a user query
     *
     * @param outputMap map with parameter values for userPane
     * @param user the user whose information is being displayed
     * @param modifyAllowed - true if the  user displaying this information has
     *                        permission to modify it
     * @param modifyRights true if the  user displaying this information has
     *                     permission to modify the target user's attributes
     * @param insts list of all institutions (a constant?)
     * @param attrNames all the attributes of the target user
     */
    public void
        contentSection(Map<String, Object> outputMap, User user, boolean modifyAllowed,
                       boolean modifyRights, List<Institution> insts,
                       List<Attribute> attributesForUser, List<Attribute> allAttributes) {

        this.log.debug("contentSection: start");
        if (modifyAllowed) {
            outputMap.put("allowModify", Boolean.TRUE);
            outputMap.put("userHeader",
                          "Editing profile for user: " + user.getLogin());
        } else {
            outputMap.put("allowModify", Boolean.FALSE);
            outputMap.put("userHeader", "Profile for user: " + user.getLogin());
        }

        String strParam = user.getLogin();
        if (strParam == null) { strParam = ""; }
        outputMap.put("profileName", strParam);
        strParam = user.getPassword();
        if (strParam != null) {
            outputMap.put("password", "********");
        }
        if (strParam != null) {
           outputMap.put("passwordConfirmation", "********");
        }
        strParam = user.getFirstName();
        if (strParam != null) {
           outputMap.put("firstName", strParam);
        }
        strParam = user.getLastName();
        if (strParam != null) {
           outputMap.put("lastName", strParam);
        }
        strParam = user.getCertSubject();
        if (strParam != null) {
           outputMap.put("certSubject", strParam);
        }
        strParam = user.getCertIssuer();
        if (strParam != null) {
           outputMap.put("certIssuer", strParam);
        }
        this.outputInstitutionMenu(outputMap, insts, user);
        this.outputAttributeMenu(outputMap, attributesForUser, allAttributes, modifyRights);

        strParam = user.getDescription();
        if (strParam != null) {
           outputMap.put("description", strParam);
        }
        strParam = user.getEmailPrimary();
        if (strParam != null) {
           outputMap.put("emailPrimary", strParam);
        }
        strParam = user.getEmailSecondary();
        if (strParam != null) {
           outputMap.put("emailSecondary", strParam);
        }
        strParam = user.getPhonePrimary();
        if (strParam != null) {
           outputMap.put("phonePrimary", strParam);
        }
        strParam = user.getPhoneSecondary();
        if (strParam != null) {
           outputMap.put("phoneSecondary", strParam);
        }
        this.log.debug("contentSection: finish");
    }

    public void outputInstitutionMenu(Map<String, Object> outputMap, List<Institution> insts, User user) {

        Institution userInstitution = null;
        String institutionName = "";

        userInstitution = user.getInstitution();
        if (userInstitution != null) {
            institutionName = userInstitution.getName();
        }
        List<String> institutionList = new ArrayList<String>();
        int ctr = 0;
        for (Institution i: insts) {
            institutionList.add(i.getName());
            // use first in list if no institution associated with user
            if ((ctr == 0) && institutionName.equals("")) {
                institutionList.add("true");
            } else if (i.getName().equals(institutionName)) {
                institutionList.add("true");
            } else {
                institutionList.add("false");
            }
            ctr++;
        }
        outputMap.put("institutionMenu", institutionList);
    }

    public void outputAttributeMenu(Map<String, Object> outputMap, List<Attribute> attributesForUser,
            List<Attribute> allAttributes, boolean modify) {

        List<String> attributeList = new ArrayList<String>();
        // default is none
        attributeList.add("None");
        if (attributesForUser.isEmpty()) {
            attributeList.add("true");
        } else {
            attributeList.add("false");
        }
        for (Attribute a: allAttributes) {
            attributeList.add(a.getName() + " -> " + a.getDescription());
            boolean foundForUser = false;
            for (Attribute aa : attributesForUser) {
                if (a.getName().equals(aa.getName())) {
                    foundForUser = true;
                }
            }
            if (foundForUser) {
                attributeList.add("true");
            } else {
                attributeList.add("false");
            }
        }
        if (modify) {
            outputMap.put("attributeNameEnable", Boolean.TRUE);
        } else {
            outputMap.put("attributeNameEnable", Boolean.FALSE);
        }
        outputMap.put("attributeNameMenu", attributeList);
    }
}
