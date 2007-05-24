package net.es.oscars.dojoServlets;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import net.es.oscars.aaa.UserManager;
import net.es.oscars.aaa.User;
import net.es.oscars.aaa.Institution;
import net.es.oscars.aaa.AAAException;

public class UserMethods {
    private UserManager mgr;

    public UserMethods(UserManager mgr) {
        this.mgr = mgr;
    }

    public void query(HttpServletRequest request, String userName)
            throws IOException, ServletException {

        User user = null;
        User requester = null;

        List<Institution> institutions = null;

        String profileName = request.getParameter("profileName");

        if (profileName == null)  {
            user = this.mgr.query(userName);
        } else {
        // if admin is modifying any user's profile, including their own
            requester = this.mgr.query(userName);
            user = this.mgr.query(profileName);
        }
        institutions = this.mgr.getInstitutions();
    }

    public void create(HttpServletRequest request, String userName)
            throws IOException, ServletException, AAAException {

        List<User> users = null;
        User newUser = null;

        String profileName = request.getParameter("profileName");
        // admin cannot add him or herself
        if (profileName != userName) {
            newUser = this.toUser(request, profileName);
            this.mgr.create(newUser, request.getParameter("institutionName"));
        }
        return;
    }

    public User modify(HttpServletRequest request, String userName)
            throws IOException, ServletException, AAAException {

        User user = null;
        User requester = null;
        List<Institution> institutions = null;

        String profileName = request.getParameter("profileName");
        // if admin is modifying the profile
        if (profileName != userName) { requester = this.mgr.query(userName); }

        user = this.mgr.query(profileName);
        if (user == null) {
            return null;
        }
        this.convertParams(request, user);

        this.mgr.update(user);
        institutions = this.mgr.getInstitutions();
        return null;
    }

    public void list(HttpServletRequest request)
            throws IOException, ServletException {

        List<User> users = null;

        users = this.mgr.list();

    }

    public void remove(HttpServletRequest request, String userName)
            throws IOException, ServletException, AAAException {

        List<User> users = null;

        String profileName = request.getParameter("profileName");
        // admin cannot remove him or herself
        if (profileName != userName) { this.mgr.remove(profileName); }
    }

    public void convertParams(HttpServletRequest request, User user) {

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

    public User toUser(HttpServletRequest request, String userName) {

        User user = new User();
        user.setLogin(userName);
        user.setCertIssuer(request.getParameter("certIssuer"));
        user.setCertSubject(request.getParameter("certSubject"));
        user.setLastName(request.getParameter("lastName"));
        user.setFirstName(request.getParameter("firstName"));
        user.setEmailPrimary(request.getParameter("emailPrimary"));
        user.setPhonePrimary(request.getParameter("phonePrimary"));
        user.setPassword(request.getParameter("password"));
        user.setDescription(request.getParameter("description"));
        user.setEmailSecondary(request.getParameter("emailSecondary"));
        user.setPhoneSecondary(request.getParameter("phoneSecondary"));
        user.setStatus(request.getParameter("status"));
        user.setActivationKey(request.getParameter("activationKey"));
        return user;
    }
}
