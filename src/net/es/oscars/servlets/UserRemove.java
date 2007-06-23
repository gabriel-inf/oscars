package net.es.oscars.servlets;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;
import org.hibernate.*;

import net.es.oscars.database.HibernateUtil;
import net.es.oscars.aaa.User;
import net.es.oscars.aaa.UserManager;
import net.es.oscars.aaa.AAAException;
import net.es.oscars.aaa.UserManager.AuthValue;


public class UserRemove extends HttpServlet {

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException, ServletException {

        List<User> users = null;
        UserList ulist = null;

        UserSession userSession = new UserSession();
        Utils utils = new Utils();
        UserManager mgr = new UserManager("aaa");
        PrintWriter out = response.getWriter();
        response.setContentType("text/xml");
        String userName = userSession.checkSession(out, request);
        if (userName == null) { return; }

        String profileName = request.getParameter("profileName");
        Session aaa = 
            HibernateUtil.getSessionFactory("aaa").getCurrentSession();
        aaa.beginTransaction();
 
        AuthValue authVal = mgr.checkAccess(userName, "Users", "modify");
        
        try {
            // cannot remove oneself
            if (profileName == userName) { 
                utils.handleFailure(out, "may not remove yourself", aaa, null);
                return;
            }
            if (authVal == AuthValue.ALLUSERS) {
                 mgr.remove(profileName);
            } else {
                   utils.handleFailure(out,"no permission modify users", aaa,null);
                   return;
            }
        } catch (AAAException e) {
            utils.handleFailure(out, e.getMessage(), aaa, null);
            return;
        }
        authVal = mgr.checkAccess(userName, "Users", "list");
        if (authVal == AuthValue.ALLUSERS) {
            users = mgr.list();
        } else if (authVal== AuthValue.SELFONLY) {
            users= (List<User>) mgr.query(userName);
        } else {
            utils.handleFailure(out,"no permission to list users", aaa,null);
            return;
        }
        ulist = new UserList();
        out.println("<xml>");
        out.println("<status>Successfully read user list.</status>");
        utils.tabSection(out, request, response, "UserList");
        ulist.outputContent(out, users, authVal);
        out.println("</xml>");
        aaa.getTransaction().commit();
    }

    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }
}
