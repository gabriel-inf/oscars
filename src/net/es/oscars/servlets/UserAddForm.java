package net.es.oscars.servlets;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;
import org.hibernate.*;

import net.es.oscars.database.HibernateUtil;
import net.es.oscars.aaa.User;
import net.es.oscars.aaa.UserManager;
import net.es.oscars.aaa.Institution;
import net.es.oscars.aaa.AAAException;
import net.es.oscars.aaa.UserManager.AuthValue;


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
        List<String> attrNames = new ArrayList<String>();
        Logger log = Logger.getLogger(this.getClass());
        log.debug("UserAddForm: start");

        PrintWriter out = response.getWriter();
        response.setContentType("text/xml");
        String userName = userSession.checkSession(out, request);
        if (userName == null) { return; }

        Session aaa = 
            HibernateUtil.getSessionFactory("aaa").getCurrentSession();
        aaa.beginTransaction();
        AuthValue authVal = mgr.checkAccess(userName, "Users", "modify");
        if (authVal != AuthValue.ALLUSERS) {
            utils.handleFailure(out, "not allowed to add a new user", aaa, null);
            return;
        }
        institutions = mgr.getInstitutions();

        out.println("<xml>");
        out.println("<status>Add user form</status>");
        utils.tabSection(out, request, response, "UserList");
        userDetails.contentSection(out, user, true, (authVal == AuthValue.ALLUSERS),
        	institutions,attrNames,"UserAddForm");
        out.println("</xml>");
        aaa.getTransaction().commit();
        log.debug("UserAddForm: finish");      
    }

    public void
        doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }
}
