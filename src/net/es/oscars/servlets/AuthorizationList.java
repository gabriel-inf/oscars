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


public class AuthorizationList extends HttpServlet {
    private Logger log;
    
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException, ServletException {

        this.log = Logger.getLogger(this.getClass());
        this.log.debug("authorizationList:start");

        String methodName = "AuthorizationList";
        UserSession userSession = new UserSession();
        PrintWriter out = response.getWriter();
        response.setContentType("text/json-comment-filtered");
        String userName = userSession.checkSession(out, request);
        if (userName == null) { return; }
        Session aaa = 
            HibernateUtil.getSessionFactory(Utils.getDbName()).getCurrentSession();
        aaa.beginTransaction();     
    
        Map outputMap = new HashMap();
        outputMap.put("status", "Authorization list");
        try {
            this.outputAuthorizations(outputMap, userName);
        } catch (AAAException e) {
            Utils.handleFailure(out, e.getMessage(), methodName, aaa);
            return;
        }
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("/* " + jsonObject + " */");
        aaa.getTransaction().commit();
        this.log.debug("authorizationList:finish");
    }

    /**
     * Checks access and gets the list of authorizations if allowed.
     *  
     * @param outputMap Map containing JSON data
     * @param userName String containing name of user making request
     * @throws AAAException
     */
    public void outputAuthorizations(Map outputMap, String userName)
            throws AAAException {

        List<Authorization> auths = null;
        AuthorizationDAO authDAO = new AuthorizationDAO(Utils.getDbName());
        UserManager mgr = new UserManager(Utils.getDbName());
        int id = -1;
        
        AuthValue authVal = mgr.checkAccess(userName, "AAA", "list");
        if (authVal != AuthValue.DENIED) {
            auths = authDAO.list();
        } else {
            throw new AAAException("no permission to list authorizations");
        }
        ArrayList authList = new ArrayList();
        for (Authorization auth: auths) {
            ArrayList authEntry = new ArrayList();
            authEntry.add(auth.getAttrId());
            authEntry.add(auth.getResourceId());
            authEntry.add(auth.getPermissionId());
            authEntry.add(auth.getConstraintName());
            authEntry.add(auth.getConstraintValue());
            authList.add(authEntry);
        }
        outputMap.put("authData", authList);
    }

    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }
}
