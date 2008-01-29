package net.es.oscars.dojoServlets;

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
import net.es.oscars.aaa.Institution;
import net.es.oscars.aaa.AAAException;


public class UserQuery extends HttpServlet {
    private Logger log;
    private String dbname;
    
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException, ServletException {

        this.log = Logger.getLogger(this.getClass());
        this.dbname = "aaa";
        this.log.debug("userQuery:start");

        User targetUser = null;
        boolean self =  false; // is query about the current user
        boolean modifyAllowed = false;

        UserSession userSession = new UserSession();
        UserManager mgr = new UserManager("aaa");
        UserDetails userDetails = new UserDetails();
        List<Institution> institutions = null;
        List<String> attrNames = new ArrayList<String>();
        Utils utils = new Utils();

        PrintWriter out = response.getWriter();
        response.setContentType("text/json-comment-filtered");
        String userName = userSession.checkSession(out, request);
        if (userName == null) { return; }

        String profileName = request.getParameter("login");
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
       if (self) {attrNames = mgr.getAttrNames();}
       else {attrNames = mgr.getAttrNames(profileName);}
        
        if ((authVal == AuthValue.ALLUSERS)  ||  ( self && (authVal == AuthValue.SELFONLY))) {
              modifyAllowed = true;
         } else {
            modifyAllowed = false;;
        }
        institutions = mgr.getInstitutions();
        Map outputMap = new HashMap();
        outputMap.put("status", "Profile for user " + profileName);
        userDetails.contentSection(
                outputMap, targetUser, modifyAllowed,
                (authVal == AuthValue.ALLUSERS),
                institutions, attrNames);
        outputMap.put("method", "UserQuery");
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("/* " + jsonObject + " */");
        aaa.getTransaction().commit();
        this.log.debug("userQuery:finish");
    }

    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }
}
