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
import net.es.oscars.aaa.AAAException;


public class UserQuery extends HttpServlet {
    private Logger log;
    
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException, ServletException {

        this.log = Logger.getLogger(this.getClass());
        String methodName = "UserQuery";
        this.log.debug("servlet.start");

        User targetUser = null;
        boolean self =  false; // is query about the current user
        boolean modifyAllowed = false;

        UserSession userSession = new UserSession();
        UserManager mgr = new UserManager(Utils.getDbName());
        List<Institution> institutions = null;
        List<String> attrNames = new ArrayList<String>();

        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        String userName = userSession.checkSession(out, request, methodName);
        if (userName == null) {
            this.log.error("No user session: cookies invalid");
            return;
        }

        String profileName = request.getParameter("profileName");
        Session aaa = 
            HibernateUtil.getSessionFactory(Utils.getDbName()).getCurrentSession();
        aaa.beginTransaction();

        // get here by clicking on a name in the users list
        if ((profileName != null) && !profileName.equals("")) {
            this.log.info("profileName: " + profileName);
            if (profileName.equals(userName)) { 
                self =true; 
            } else {
                self = false;
            }
        } else { // profileName is null - get here by clicking on tab navigation
            this.log.info("profileName is null, using " + userName);
            profileName = userName;
            self=true;
        }
        Map outputMap = new HashMap();
        if (!self) {
            outputMap.put("userDeleteDisplay", Boolean.TRUE);
        } else {
            outputMap.put("userDeleteDisplay", Boolean.FALSE);
        }
        AuthValue authVal = mgr.checkAccess(userName, "Users", "query");
        
        if ((authVal == AuthValue.ALLUSERS)  ||  ( self && (authVal == AuthValue.SELFONLY))) {
              targetUser= mgr.query(profileName);
         } else {
            Utils.handleFailure(out,"no permission to query users",
                                methodName, aaa);
            return;
        }
        /* check to see if user has modify permission for this user
         *     used by conentSection to set the action on submit
         */
        authVal = mgr.checkAccess(userName, "Users", "modify");
        if (self) {attrNames = mgr.getAttrNames();}
        else {attrNames = mgr.getAttrNames(profileName);}
        
        if ((authVal == AuthValue.ALLUSERS) ||
            (self && (authVal == AuthValue.SELFONLY))) {
            modifyAllowed = true;
        } else {
            modifyAllowed = false;;
        }
        institutions = mgr.getInstitutions();
        outputMap.put("status", "Profile for user " + profileName);
        this.contentSection(
                outputMap, targetUser, modifyAllowed,
                (authVal == AuthValue.ALLUSERS),
                institutions, attrNames);
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("{}&&" + jsonObject);
        aaa.getTransaction().commit();
        this.log.debug("servlet.end");
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
        contentSection(Map outputMap, User user, boolean modifyAllowed,
                       boolean modifyRights, List<Institution> insts,
                       List<String> attrNames) {

        this.log.debug("contentSection: start");
        if (modifyAllowed) {
            outputMap.put("allowModify", Boolean.TRUE);
            outputMap.put("userHeader",
                          "Editing profile for user: " + user.getLogin());
        } else {
            outputMap.put("allowModify", Boolean.FALSE);
            outputMap.put("userHeader", "Profile for user: " + user.getLogin());
        } 
        if (attrNames == null) {
            this.log.debug("contentSection: attrNames is null");
        }
        if (attrNames.isEmpty()) {
            this.log.debug("contentSection: attrNames is empty");
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
        this.outputAttributeMenu(outputMap, attrNames, modifyRights);
        
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

    public void
        outputInstitutionMenu(Map outputMap, List<Institution> insts,
                              User user) {

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
    
    public void outputAttributeMenu(Map outputMap, List<String> attrNames,
                                    boolean modify) {

        AttributeDAO dao = new AttributeDAO(Utils.getDbName());
        List<Attribute> attributes = dao.list();
        List<String> attributeList = new ArrayList<String>();
        // default is none 
        attributeList.add("None");
        if (attrNames.isEmpty()) {
            attributeList.add("true");
        } else {
            attributeList.add("false");
        }
        for (Attribute a: attributes) {
            attributeList.add(a.getName() + " -> " + a.getDescription());
            if (attrNames.contains(a.getName())) {
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
