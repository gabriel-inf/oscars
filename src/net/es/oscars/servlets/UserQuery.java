package net.es.oscars.servlets;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;
import org.hibernate.*;

import net.es.oscars.database.HibernateUtil;
import net.es.oscars.aaa.User;
import net.es.oscars.aaa.UserManager;
import net.es.oscars.aaa.UserManager.AuthValue;
import net.es.oscars.aaa.Institution;
import net.es.oscars.aaa.AAAException;


public class UserQuery extends HttpServlet {

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException, ServletException {

        User targetUser = null;
        boolean self =  false; // is query about the current user
        boolean modifyAllowed = false;

        UserSession userSession = new UserSession();
        UserManager mgr = new UserManager("aaa");
        UserDetails userDetails = new UserDetails();
        List<Institution> institutions = null;
        Utils utils = new Utils();

        PrintWriter out = response.getWriter();
        response.setContentType("text/xml");
        String userName = userSession.checkSession(out, request);
        if (userName == null) { return; }

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
        } else { // profileName is null - get here by clicking on tab navigation
            profileName = userName;
            self=true;
        }
        AuthValue authVal = mgr.checkAccess(userName, "Users", "query");
        
        if ((authVal == AuthValue.ALLUSERS)  ||  ( self && (authVal == AuthValue.SELFONLY))) {
              targetUser= mgr.query(profileName);
         } else {
            utils.handleFailure(out,"no permission to query users", aaa,null);
            return;
        }
        /* check to see if user has modify permission for this user
         *     used by conentSection to set the action on submit
         */
       authVal = mgr.checkAccess(userName, "Users", "modify");
        
        if ((authVal == AuthValue.ALLUSERS)  ||  ( self && (authVal == AuthValue.SELFONLY))) {
              modifyAllowed = true;
         } else {
            modifyAllowed = false;;
        }
        institutions = mgr.getInstitutions();
        out.println("<xml>");
        out.println("<status>User profile</status>");
        utils.tabSection(out, request, response, "UserList");
        userDetails.contentSection(out, targetUser, modifyAllowed, institutions, 
                                   "UserQuery");
        out.println("</xml>");
        aaa.getTransaction().commit();
    }

    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }
}
