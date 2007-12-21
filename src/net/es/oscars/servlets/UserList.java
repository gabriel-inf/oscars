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
import net.es.oscars.aaa.AAAException;
import net.es.oscars.aaa.UserManager.AuthValue;


public class UserList extends HttpServlet {
    private Logger log;
    
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException, ServletException {


	this.log = Logger.getLogger(this.getClass());
	this.log.debug("userList:start");

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
        try {
            this.outputList(out, userName);
        } catch (AAAException e) {
            utils.handleFailure(out, e.getMessage(), aaa, null);
            return;
        }
        out.println("<status>Successfully read user list.</status>");
        utils.tabSection(out, request, response, "UserList");
        out.println("</xml>");
        aaa.getTransaction().commit();
	this.log.debug("userList:finish");
    }

    /**
     * outputList - checks access and gets the list of users if allowed.
     *  prints out the users to the response xml page
     *  
     * @param out PrintWriter connected to response page
     * @param userName String containing name of user making request
     * @throws AAAException
     */
    public void outputList(PrintWriter out, String userName) 
       throws AAAException {

        List<User> users = null;
        UserManager mgr = new UserManager("aaa");
        //Utils utils = new Utils();
        
        AuthValue authVal = mgr.checkAccess(userName, "Users", "list");
        if (authVal == AuthValue.ALLUSERS) {
            users = mgr.list();
        } else if (authVal== AuthValue.SELFONLY) {
            users= (List<User>) mgr.query(userName);
        } else {
            throw new AAAException("no permission to list users");
         }
   // check for modify permission to determine if the addUser button should be displayed 
  
        authVal = mgr.checkAccess(userName, "Users", "modify");
        this.outputContent(out, users, authVal);
   
    }


    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }
    
    /**
     *  Prints out the whole list users page.
     *  called from UserList and UserRemove
     *  
     * @param out PrintWriter that writes to the output html page
     * @param users list of all users
     * @param authVal if set to  ALLUSERS the user has permission to modify all users and
     *                gets to see the UserAdd button.
     */

    public void outputContent(PrintWriter out, List<User> users, AuthValue authVal) {
 
        String submitStr= "return submitForm(this,'UserList');";
        String buttonName = "List Users";
        out.println("<content>");
        if (authVal == AuthValue.ALLUSERS){
            submitStr = "return submitForm(this, 'UserAddForm');";
            buttonName = "Add User";
        }
        out.println("<form method='post' action='' onsubmit=\"" +
                    submitStr + "\">");
       

        out.println("<p>Click on the user's last name to view detailed user information.</p>");
        out.println("<p><input type='submit' value='" + buttonName+  "'></input></p>");
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

/**
 * Prints a line on the list users page
 
 *  
 * @param out PrintWriter that writes to the output html page
 * @param user User structure
 */
  /*
    *  This could be tweaked to not allow a reference to the user profile
    *  or the delete page if the user does not have permission. As it is now the
    *  user will get an error when he tries to follow a link for which he has  no
    *  permission. However, as it is, it is easier for a user to what permissions are 
    *  needed for the query and delete user operations.
   */
    public void printUser(PrintWriter out, User user) {

        String profileHrefStr = "return newSection('UserQuery', " +
                    "'profileName=" + user.getLogin() + "');";
        String removeHrefStr = "return newSection('UserRemove', " +
                    "'profileName=" + user.getLogin() + "');";
        String institutionName; 
        
        /* if an unknown institution id is found in the users table an exception is thrown
         * in hibernate.gclib generated code
         */
        try {
            institutionName = user.getInstitution().getName();
        } catch (org.hibernate.ObjectNotFoundException e) {
            institutionName = "unknown";
        }
        
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
