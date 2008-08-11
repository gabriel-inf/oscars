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
        String methodName = "AuthorizationList";
        this.log.debug("servlet.start");
        UserSession userSession = new UserSession();
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
        UserManager mgr = new UserManager(Utils.getDbName());
        AuthValue authVal = mgr.checkAccess(userName, "AAA", "list");
        if (authVal == AuthValue.DENIED) {
            this.log.error("no permission to list authorizations");
            Utils.handleFailure(out, "no permission to list authorizations",
                                methodName, aaa);
            return;
        }
        Map outputMap = new HashMap();
        // only do once
        String rpcParam = request.getParameter("rpc");
        if ((rpcParam == null) || rpcParam.trim().equals("")) {
            this.outputRpcs(outputMap);
        }
        this.outputAuthorizations(outputMap);
        outputMap.put("status", "Authorization list");
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("/* " + jsonObject + " */");
        aaa.getTransaction().commit();
        this.log.debug("servlet.end");
    }

    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }

    /**
     * Sets the list of authorizations to display in a grid.
     *  
     * @param outputMap Map containing JSON data
     */
    public void outputAuthorizations(Map outputMap) {

        AuthorizationDAO authDAO = new AuthorizationDAO(Utils.getDbName());
        List<Authorization> auths = authDAO.list();
        AttributeDAO attrDAO = new AttributeDAO(Utils.getDbName());
        ResourceDAO resourceDAO = new ResourceDAO(Utils.getDbName());
        PermissionDAO permissionDAO = new PermissionDAO(Utils.getDbName());
        ConstraintDAO constraintDAO = new ConstraintDAO(Utils.getDbName());
        int id = -1;
        
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
 
            String constraintName = authDAO.getConstraintName(auth);
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
                     constraintName.equals("my-site") ||
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

    /**
     * Outputs permitted resource/permission/constraint triplets.   Could
     * eventually be used in a split container on the right side of the
     * authorization details page, assuming the grid would display in
     * such a case.
     *  
     * @param outputMap Map containing JSON data
     */
    public void outputRpcs(Map outputMap) {

        RpcDAO rpcDAO = new RpcDAO(Utils.getDbName());
        List<Rpc> rpcs = rpcDAO.list();
        ResourceDAO resourceDAO = new ResourceDAO(Utils.getDbName());
        PermissionDAO permissionDAO = new PermissionDAO(Utils.getDbName());
        ConstraintDAO constraintDAO = new ConstraintDAO(Utils.getDbName());
        int id = -1;
        
        ArrayList rpcList = new ArrayList();
        for (Rpc rpc: rpcs) {
            ArrayList rpcEntry = new ArrayList();
            // ignore illegal triplets
            Resource resource = resourceDAO.findById(rpc.getResourceId(),
                                                     false);
            if (resource != null) {
                rpcEntry.add(resource.getName());
            } else {
                continue;
            }
            Permission perm = permissionDAO.findById(rpc.getPermissionId(),
                                                     false);
            if (perm != null) {
                rpcEntry.add(perm.getName());
            } else {
                continue;
            }
            // null constraints are added on the client side; rpc table
            // has constraintId as not null, so one here would be an error.
            Constraint constraint =
                constraintDAO.findById(rpc.getConstraintId(), false);
            if (constraint != null) {
                rpcEntry.add(constraint.getName());
            } else {
                continue;
            }
            rpcList.add(rpcEntry);
        }
        outputMap.put("rpcData", rpcList);
    }
}
