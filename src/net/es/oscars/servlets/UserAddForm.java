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


public class UserAddForm extends HttpServlet {

    public void
        doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        UserSession userSession = new UserSession();
        UserManager mgr = new UserManager("aaa");
        User user = new User();
        UserDetails userDetails = new UserDetails();
        Utils utils = new Utils();
        List<Institution> institutions = null;

        PrintWriter out = response.getWriter();
        response.setContentType("text/xml");
        String userName = userSession.checkSession(out, request);
        if (userName == null) { return; }

        Session aaa = 
            HibernateUtil.getSessionFactory("aaa").getCurrentSession();
        aaa.beginTransaction();
        institutions = mgr.getInstitutions();

        out.println("<xml>");
        out.println("<status>Add user form</status>");
        utils.tabSection(out, request, response, "UserList");
        userDetails.contentSection(out, user, null, institutions,
                                  "UserAddForm");
        out.println("</xml>");
        aaa.getTransaction().commit();
    }

    public void
        doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }
}
