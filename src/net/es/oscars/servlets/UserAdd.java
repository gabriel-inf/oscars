package net.es.oscars.servlets;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;
import org.hibernate.*;
import net.sf.json.*;

import net.es.oscars.database.HibernateUtil;
import net.es.oscars.aaa.User;
import net.es.oscars.aaa.UserManager;
import net.es.oscars.aaa.UserManager.AuthValue;
import net.es.oscars.aaa.Attribute;
import net.es.oscars.aaa.AttributeDAO;
import net.es.oscars.aaa.AAAException;

public class UserAdd extends HttpServlet {
    private Logger log;
    
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException, ServletException {

        User newUser = null;
        String newRole = null;
        Session aaa;
        ArrayList <Integer> addRoles = null;

        this.log = Logger.getLogger(this.getClass());
        String methodName = "UserAdd";
        this.log.info("servlet.start");

        UserSession userSession = new UserSession();
        UserManager mgr = new UserManager(Utils.getDbName());
        AttributeDAO attrDAO = new AttributeDAO(Utils.getDbName());

        PrintWriter out = response.getWriter();
        response.setContentType("text/json-comment-filtered");
        String userName = userSession.checkSession(out, request, methodName);
        if (userName == null) {
            this.log.error("No user session: cookies invalid");
            return;
        }
        String profileName = request.getParameter("profileName");
        aaa = HibernateUtil.getSessionFactory(Utils.getDbName()).getCurrentSession();
        aaa.beginTransaction();
        try {
            RoleUtils roleUtils = new RoleUtils();
            AuthValue authVal = mgr.checkAccess(userName, "Users", "create");
            if ((authVal == AuthValue.ALLUSERS) && 
                        (profileName != userName)) {
                newUser = this.toUser(out, profileName, request);
                String roles[] = request.getParameterValues("roles");
                if (roles == null ) { 
                    this.log.debug("roles = null");
                    addRoles = new ArrayList<Integer>();
                } else {
                    this.log.debug("number of roles input is "+roles.length);
                    addRoles = roleUtils.convertRoles(roles);
                }
                newRole = request.getParameter("newRole");
                if ((newRole != null) && !newRole.trim().equals("")) {
                    Attribute newAttr = new Attribute();
                    newAttr.setName(newRole);
                    attrDAO.create(newAttr); 
                    try {
                        addRoles.add(attrDAO.getAttributeId(newRole));
                    } catch (AAAException ex) {
                        this.log.error("error: no attribute id was assigned by create");
                    }
                }
                mgr.create(newUser, request.getParameter("institutionName"),
                           addRoles);  
            }  else {
                // this also makes sure won't list users either if don't
                // have permission
                this.log.error("Not allowed to add a new user");
                Utils.handleFailure(out, "not allowed to add a new user",
                                    methodName, aaa);
                return;
            }
        } catch (AAAException e) {
            this.log.error(e.getMessage());
            Utils.handleFailure(out, e.getMessage(), methodName, aaa);
            return;
        }
        Map outputMap = new HashMap();
        outputMap.put("status", "User " + profileName +
                                " successfully created");
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("/* " + jsonObject + " */");
        aaa.getTransaction().commit();
        this.log.info("servlet.end");
    }

    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }

    public User toUser(PrintWriter out, String userName,
                       HttpServletRequest request)  
         throws AAAException {

        String strParam;
        String DN;
        String password;
 
        User user = new User();
        user.setLogin(userName);
        strParam = request.getParameter("certIssuer");
        if ((strParam != null) && (!strParam.trim().equals(""))) {
            DN = Utils.checkDN(strParam);
        }
        else { DN = ""; }
        user.setCertIssuer(DN);
        strParam = request.getParameter("certSubject");
        if ((strParam != null) && (!strParam.trim().equals(""))) {
            DN = Utils.checkDN(strParam);
        }
        else { DN = ""; }
        user.setCertSubject(DN);
        // required fields by client, so always filled in
        user.setLastName(request.getParameter("lastName"));
        user.setFirstName(request.getParameter("firstName"));
        user.setEmailPrimary(request.getParameter("emailPrimary"));
        user.setPhonePrimary(request.getParameter("phonePrimary"));
        password = Utils.checkPassword(request.getParameter("password"),
                request.getParameter("passwordConfirmation"));
        user.setPassword(password); 
        // doesn't matter if blank
        user.setDescription(request.getParameter("description"));
        user.setEmailSecondary(request.getParameter("emailSecondary"));
        user.setPhoneSecondary(request.getParameter("phoneSecondary"));
        // noops currently
        user.setStatus(request.getParameter("status"));
        user.setActivationKey(request.getParameter("activationKey"));
        return user;
    }
}
