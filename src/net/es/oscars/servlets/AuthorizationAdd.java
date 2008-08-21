package net.es.oscars.servlets;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;
import org.hibernate.*;
import net.sf.json.*;

import net.es.oscars.database.HibernateUtil;
import net.es.oscars.aaa.*;
import net.es.oscars.aaa.UserManager.AuthValue;


public class AuthorizationAdd extends HttpServlet {
    private Logger log;
    public void
        doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        UserSession userSession = new UserSession();
        UserManager mgr = new UserManager(Utils.getDbName());
        this.log = Logger.getLogger(this.getClass());
        this.log.debug("servlet.start");

        String methodName = "AuthorizationAdd";
        PrintWriter out = response.getWriter();
        response.setContentType("text/json-comment-filtered");
        String userName = userSession.checkSession(out, request, methodName);
        if (userName == null) {
            this.log.error("No user session: cookies invalid");
            return;
        }
        Session aaa = 
            HibernateUtil.getSessionFactory(Utils.getDbName()).getCurrentSession();
        aaa.beginTransaction();
        AuthValue authVal = mgr.checkAccess(userName, "AAA", "modify");
        if (authVal == AuthValue.DENIED)  { 
            this.log.error("Not allowed to add an authorization");
            Utils.handleFailure(out, "not allowed to add an authorization",
                                methodName, aaa);
            return;
        }
        try {
        String attribute = request.getParameter("authAttributeName");
        String permission = request.getParameter("permissionName");
        String resource = request.getParameter("resourceName");
        String constraintName = request.getParameter("constraintName");
        String constraintValue = null;
        if (constraintName != null) {
            constraintValue = request.getParameter("constraintValue");
        }
        this.log.debug("Adding attribute: " + attribute +" resource: " + resource + " permission: "
                + permission + " constraintName: " + constraintName + " constraintValue: " + constraintValue);
        AuthorizationDAO authDAO = new AuthorizationDAO(Utils.getDbName());
        try {
            authDAO.create(attribute, resource, permission, constraintName, constraintValue);
        } catch ( AAAException e) {
            this.log.error(e.getMessage());
            Utils.handleFailure(out, e.getMessage(), methodName, aaa);
            return;           
        }
        } catch (Exception e) {
            this.log.error ("caught exception" + e.getMessage());
        }
        Map outputMap = new HashMap();
        outputMap.put("status", "Added authorization");
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("/* " + jsonObject + " */");
        aaa.getTransaction().commit();
        this.log.debug("servlet.end");      
    }

    public void
        doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }
}
