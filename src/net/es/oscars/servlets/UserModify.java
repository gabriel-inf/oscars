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


public class UserModify extends HttpServlet {

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException, ServletException {

        UserManager mgr = null;
        UserAdd adder = null;
        UserDetails userDetails = null;
        User user = null;
        User requester = null;
        List<Institution> institutions = null;

        UserSession userSession = new UserSession();
        Utils utils = new Utils();
        PrintWriter out = response.getWriter();
        response.setContentType("text/xml");
        String userName = userSession.checkSession(out, request);
        if (userName == null) { return; }

        mgr = new UserManager("aaa");
        mgr.setSession();
        String profileName = request.getParameter("profileName");
        Session aaa = 
            HibernateUtil.getSessionFactory("aaa").getCurrentSession();
        aaa.beginTransaction();
        // if admin is modifying the profile
        if (profileName != userName) { requester = mgr.query(userName); }

        user = mgr.query(profileName);
        if (user == null) {
            String msg = "User " + profileName + " does not exist";
            utils.handleFailure(out, msg, aaa, null);
        }
        this.convertParams(out, request, user);
        String password = user.getPassword();
        String confirmationPassword =
            request.getParameter("passwordConfirmation");

        try {
            // handle password modification if necessary
            user.setPassword(
                    this.checkPassword(password, confirmationPassword));
            mgr.update(user);
        } catch (AAAException e) {
            utils.handleFailure(out, e.getMessage(), aaa, null);
            return;
        }
        institutions = mgr.getInstitutions();
        userDetails = new UserDetails();
        out.println("<xml>");
        out.println("<status>User profile</status>");
        utils.tabSection(out, request, response, "UserList");
        userDetails.contentSection(out, user, requester, institutions,
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
                              User user) {

        String strParam = null;

        strParam = request.getParameter("certIssuer");
        // allow setting existent non-required field to null
        if ((strParam != null) || (user.getCertIssuer() != null)) {
            user.setCertIssuer(strParam);
        }
        strParam = request.getParameter("certSubject");
        if ((strParam != null) || (user.getCertSubject() != null)) {
            user.setCertSubject(strParam);
        }
        strParam = request.getParameter("lastName");
        if (strParam != null) { user.setLastName(strParam); }
        strParam = request.getParameter("firstName");
        if (strParam != null) { user.setFirstName(strParam); }
        strParam = request.getParameter("emailPrimary");
        if (strParam != null) { user.setEmailPrimary(strParam); }
        strParam = request.getParameter("phonePrimary");
        if (strParam != null) { user.setPhonePrimary(strParam); }
        strParam = request.getParameter("password");
        if (strParam != null) { user.setPassword(strParam); }
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

    /**
     * Checks for proper confirmation of password change. 
     *
     * @param password  A string with the desired password
     * @param confirmationPassword  A string with the confirmation password
     */
    public String checkPassword(String password, String confirmationPassword)
            throws AAAException {

        // If the password needs to be updated, make sure there is a
        // confirmation password, and that it matches the given password.
        if ((password != null) && (!password.equals("")) &&
                (!password.equals("********"))) {
           if (confirmationPassword == null) {
                throw new AAAException(
                    "Cannot update password without confirmation password");
            } else if (!confirmationPassword.equals(password)) {
                throw new AAAException(
                     "Password and password confirmation do not match");
            }
        }
        return password;
    }
}
