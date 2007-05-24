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


public class UserList extends HttpServlet {

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException, ServletException {

        UserSession userSession = new UserSession();
        Utils utils = new Utils();
        PrintWriter out = response.getWriter();
        response.setContentType("text/xml");
        String userName = userSession.checkSession(out, request);
        if (userName == null) { return; }
        Session aaa = 
            HibernateUtil.getSessionFactory("aaa").getCurrentSession();
        aaa.beginTransaction();
        out.println("<xml>");
        out.println("<status>Successfully read user list.</status>");
        utils.tabSection(out, request, response, "UserList");
        this.outputList(out);
        out.println("</xml>");
        aaa.getTransaction().commit();
    }

    public void outputList(PrintWriter out) {

        List<User> users = null;

        UserManager mgr = new UserManager("aaa");
        mgr.setSession();
        users = mgr.list();
        this.outputContent(out, users);
    }

    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }

    public void outputContent(PrintWriter out, List<User> users) {

        out.println("<content>");
        String submitStr = "return submitForm(this, 'UserAddForm');";
        out.println("<form method='post' action='' onsubmit=\"" +
                    submitStr + "\">");

        out.println("<p>Click on the user's last name to view detailed user information.</p>");
        out.println("<p><input type='submit' value='Add User'></input></p>");
        out.println("<table id='Users.Users' cellspacing='0' width='90%' class='sortable'>");
        out.println("<thead><tr>");
        out.println("<td>Last Name</td><td>First Name</td><td>Login Name</td>");
        out.println("<td>Organization</td><td>Phone</td><td>Action</td></tr>");
        out.println("</thead>");
        out.println("<tbody>");
        for (User user: users) { this.printUser(out, user); }
        out.println("</tbody></table></form>");
        out.println("</content>");
    }


    public void printUser(PrintWriter out, User user) {

        String profileHrefStr = "return newSection('UserQuery', " +
                    "'profileName=" + user.getLogin() + "');";
        String removeHrefStr = "return newSection('UserRemove', " +
                    "'profileName=" + user.getLogin() + "');";
        String institutionName = user.getInstitution().getName();
        out.println("<tr>");
        out.println("<td><a href='#' ");
        out.println("onclick=\"" + profileHrefStr + "\"> " +
                    user.getLastName() + "</a></td>");

        out.println("<td>" + user.getFirstName() + "</td> <td>" +
                    user.getLogin() + "</td>");
        out.println("<td>" + institutionName + "</td>");
        out.println("<td>" + user.getPhonePrimary() + "</td>");
        out.println("<td><a href='#' ");
        out.println("onclick=\"" + removeHrefStr + "\">DELETE</a></td>");
        out.println("</tr>");
    }
}
