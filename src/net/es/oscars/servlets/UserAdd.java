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
import net.es.oscars.aaa.UserManager.AuthValue;
import net.es.oscars.aaa.Attribute;
import net.es.oscars.aaa.AttributeDAO;
import net.es.oscars.aaa.AAAException;

public class UserAdd extends HttpServlet {
    private Logger log;
    private String dbname;
    
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException, ServletException {

	this.log = Logger.getLogger(this.getClass());
	this.dbname = "aaa";
	this.log.debug("userAdd:start");

        //List<User> users = null;
        UserList ulist = null;
        //String roles[] = null;
        String newRole = null;
        ArrayList <Integer> addRoles = null;

        Session aaa;
        UserSession userSession = new UserSession();
        UserManager mgr = new UserManager(this.dbname);
        User newUser = null;
        Utils utils = new Utils();
        AttributeDAO attrDAO = new AttributeDAO(this.dbname);

        PrintWriter out = response.getWriter();
        response.setContentType("text/xml");
        String userName = userSession.checkSession(out, request);
        if (userName == null) { return; }
        String profileName = request.getParameter("profileName");
        aaa = HibernateUtil.getSessionFactory(this.dbname).getCurrentSession();
        aaa.beginTransaction();
        try {
            AuthValue authVal = mgr.checkAccess(userName, "Users", "modify");
            if ((authVal == AuthValue.ALLUSERS) && 
                        (profileName != userName)) {
                newUser = this.toUser(out, profileName, request);
                
        	String roles[] = request.getParameterValues("roles");
        	if (roles == null ){ 
        	    this.log.debug("AddUser: roles = null");
        	    addRoles = new ArrayList<Integer>();
        	} else {
        	    this.log.debug("number of roles input is "+roles.length);
        	    addRoles = utils.convertRoles(roles);
        	}
                newRole = request.getParameter("newRole");
                if (newRole != null) {
                    Attribute newAttr = new Attribute();
                    newAttr.setName(newRole);
                    attrDAO.create(newAttr); 
                    try {
                	 addRoles.add(attrDAO.getAttributeId(newRole));
                    } catch (AAAException ex) {
                	this.log.error("oops,no id was assigned by create");
                    }
                }
                mgr.create(newUser, request.getParameter("institutionName"), addRoles);  
            }  else {
                utils.handleFailure(out, "not allowed to add a new user", aaa, null);
                return;
            }
        } catch (AAAException e) {
            log.error("UserAdd caught exception: "+ e.getMessage());
            utils.handleFailure(out, e.getMessage(), aaa, null);
            return;
        }
        ulist = new UserList();
        out.println("<xml>");
        try {
            ulist.outputList(out,userName);
        } catch (AAAException e) {
            utils.handleFailure(out, e.getMessage(), aaa, null);
        }
        out.println("<status>Successfully read user list.</status>");
        utils.tabSection(out, request, response, "UserList");
        out.println("</xml>");
        aaa.getTransaction().commit();
        this.log.debug("UserAdd: finish");
    }

    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }

    public User toUser(PrintWriter out, String userName,
                       HttpServletRequest request)  
         throws AAAException {

        Utils utils = new Utils();
        String strParam;
        String DN;
        String password;
 
        
        User user = new User();
        user.setLogin(userName);
        strParam = request.getParameter("certIssuer");
        if (strParam != null) {  DN = utils.checkDN(strParam);   }
        else { DN = ""; }
        user.setCertIssuer(DN);
        strParam = request.getParameter("certSubject");
        if (strParam != null) {  DN = utils.checkDN(strParam);   }
        else { DN = ""; }
        user.setCertSubject(DN);
        user.setLastName(request.getParameter("lastName"));
        user.setFirstName(request.getParameter("firstName"));
        user.setEmailPrimary(request.getParameter("emailPrimary"));
        user.setPhonePrimary(request.getParameter("phonePrimary"));
        password = utils.checkPassword(request.getParameter("password"),
                request.getParameter("passwordConfirmation"));
        user.setPassword(password); 
        user.setDescription(request.getParameter("description"));
        user.setEmailSecondary(request.getParameter("emailSecondary"));
        user.setPhoneSecondary(request.getParameter("phoneSecondary"));
        user.setStatus(request.getParameter("status"));
        user.setActivationKey(request.getParameter("activationKey"));
        return user;
    }
}
