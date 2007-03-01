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


public class UserQuery extends HttpServlet {

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException, ServletException {

        User user = null;
        User requester = null;

        UserSession userSession = new UserSession();
        UserManager mgr = new UserManager();
        mgr.setSession();
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

        // if user is modifying their own profile, and is coming in from
        // tab navigation
        if (profileName == null)  {
            user = mgr.query(userName);
        } else {
        // if admin is modifying any user's profile, including their own
            requester = mgr.query(userName);
            user = mgr.query(profileName);
        }
        institutions = mgr.getInstitutions();
        out.println("<xml>");
        out.println("<status>User profile</status>");
        utils.tabSection(out, request, response, "UserList");
        userDetails.contentSection(out, user, requester, institutions, 
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
