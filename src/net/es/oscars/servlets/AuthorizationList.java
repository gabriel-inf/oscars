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
        AttributeDAO attrDAO = new AttributeDAO(Utils.getDbName());
        ResourceDAO resourceDAO = new ResourceDAO(Utils.getDbName());
        PermissionDAO permissionDAO = new PermissionDAO(Utils.getDbName());
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
            Attribute attr = attrDAO.findById(auth.getAttrId(), false);
            if (attr != null) {
                authEntry.add(attr.getName());
            } else {
                authEntry.add("illegal id: " + auth.getAttrId());
            }
            Resource resource = resourceDAO.findById(auth.getResourceId(),
                                                     false);
            if (resource != null) {
                authEntry.add(resource.getName());
            } else {
                authEntry.add("illegal id: " + auth.getResourceId());
            }
            Permission perm = permissionDAO.findById(auth.getPermissionId(),
                                                     false);
            if (perm != null) {
                authEntry.add(perm.getName());
            } else {
                authEntry.add("illegal id: " + auth.getPermissionId());
            }
            String constraintName = auth.getConstraintName();
            if (constraintName != null) {
                authEntry.add(constraintName);
            } else {
                authEntry.add("");
            }
            // handle special cases
            Integer constraintValue = auth.getConstraintValue();
            if (constraintValue != null) {
                if ((constraintName != null) &&
                    (constraintName.equals("all-users") ||
                     constraintName.equals("specify-gri") ||
                     constraintName.equals("specify-path-elements"))) {
                    if (constraintValue == 1) {
                        authEntry.add("true");
                    } else {
                        authEntry.add("false");
                    }
                } else {
                    authEntry.add(constraintValue);
                }
            } else {
                authEntry.add("");
            }
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
