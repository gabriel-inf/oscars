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

public class UserAdd extends HttpServlet {

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException, ServletException {

        List<User> users = null;
        UserList ulist = null;

        Session aaa;
        UserSession userSession = new UserSession();
        UserManager mgr = new UserManager("aaa");
        User newUser = null;
        Utils utils = new Utils();

        PrintWriter out = response.getWriter();
        response.setContentType("text/xml");
        String userName = userSession.checkSession(out, request);
        if (userName == null) { return; }
        String profileName = request.getParameter("profileName");
        aaa = HibernateUtil.getSessionFactory("aaa").getCurrentSession();
        aaa.beginTransaction();
        try {
            // admin cannot add him or herself
            if (profileName != userName) {
                newUser = this.toUser(out, profileName, request);
                mgr.create(newUser, request.getParameter("institutionName"));
            }
        } catch (AAAException e) {
            utils.handleFailure(out, e.getMessage(), aaa, null);
            return;
        }
        ulist = new UserList();
        out.println("<xml>");
        out.println("<status>Successfully read user list.</status>");
        utils.tabSection(out, request, response, "UserList");
        ulist.outputList(out);
        out.println("</xml>");
        aaa.getTransaction().commit();
    }

    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }

    public User toUser(PrintWriter out, String userName,
                       HttpServletRequest request) {

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
