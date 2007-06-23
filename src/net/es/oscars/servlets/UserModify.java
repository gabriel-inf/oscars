package net.es.oscars.servlets;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;
import org.hibernate.*;

import net.es.oscars.database.HibernateUtil;
import net.es.oscars.aaa.User;
import net.es.oscars.aaa.UserManager;
import net.es.oscars.aaa.Institution;
import net.es.oscars.aaa.AAAException;
import net.es.oscars.aaa.UserManager.AuthValue;


public class UserModify extends HttpServlet {

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException, ServletException {

        UserManager mgr = null;
        //UserAdd adder = null;
        UserDetails userDetails = null;
        User user = null;
        //User requester = null;
        boolean self = false; // is user modifying own profile
        boolean setPassword = false; 
        List<Institution> institutions = null;

        UserSession userSession = new UserSession();
        Utils utils = new Utils();
        PrintWriter out = response.getWriter();
        response.setContentType("text/xml");
        String userName = userSession.checkSession(out, request);
        if (userName == null) { return; }

        mgr = new UserManager("aaa");
        String profileName = request.getParameter("profileName");
        Session aaa = 
            HibernateUtil.getSessionFactory("aaa").getCurrentSession();
        aaa.beginTransaction();
        
        if (profileName != null) { // get here by clicking on a name in the users list
            if (profileName.equals(userName)) { 
                self =true; 
            } else {
                self = false;
            }
        } else { // profileName is null - not sure how we get here -mrt
            profileName = userName;
            self = false;
        }
        AuthValue authVal = mgr.checkAccess(userName, "Users", "modify");
        
        if ((authVal == AuthValue.ALLUSERS)  ||  ( self && (authVal == AuthValue.SELFONLY))) {
              user= mgr.query(profileName);
         } else {
            utils.handleFailure(out,"no permission modify users", aaa,null);
            return;
        }

        if (user == null) {
            String msg = "User " + profileName + " does not exist";
            utils.handleFailure(out, msg, aaa, null);
        }

        try {
            this.convertParams(out, request, user);
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
        } catch (AAAException e) {
            utils.handleFailure(out, e.getMessage(), aaa, null);
            return;
        }
        institutions = mgr.getInstitutions();
        userDetails = new UserDetails();
        out.println("<xml>");
        out.println("<status>User profile successfully modified</status>");
        utils.tabSection(out, request, response, "UserList");
        userDetails.contentSection(out, user, true, institutions,
                                   "UserModify");
        out.println("</xml>");
        aaa.getTransaction().commit();
    }

    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }

    public void convertParams(PrintWriter out, HttpServletRequest request,
                              User user) 
        throws AAAException {

        String strParam = null;
        String DN = null;
        Utils utils = new Utils();

        strParam = request.getParameter("certIssuer");
        if (strParam != null) {  DN = utils.checkDN(strParam);   }
        // allow setting existent non-required field to null
        if ((DN != null) || (user.getCertIssuer() != null)) {
            user.setCertIssuer(DN);
        }
         strParam = request.getParameter("certSubject");
        if (strParam != null) {  DN = utils.checkDN(strParam);   }
        if ((DN != null) || (user.getCertSubject() != null)) {
            user.setCertSubject(DN);
        }
        strParam = request.getParameter("lastName");
        if (strParam != null) { user.setLastName(strParam); }
        strParam = request.getParameter("firstName");
        if (strParam != null) { user.setFirstName(strParam); }
        strParam = request.getParameter("emailPrimary");
        if (strParam != null) { user.setEmailPrimary(strParam); }
        strParam = request.getParameter("phonePrimary");
        if (strParam != null) { user.setPhonePrimary(strParam); }
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
