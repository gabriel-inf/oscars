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


/**
 * Class that sets menu options in attributes, resources, permissions, and
 * constraints menus.  May be called more than once, if attributes
 * modified.  Initially called on user login.
 */
public class AuthorizationForm extends HttpServlet {

    public void
        doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        UserSession userSession = new UserSession();
        UserManager mgr = new UserManager(Utils.getDbName());
        Logger log = Logger.getLogger(this.getClass());
        String methodName = "AuthorizationForm";
        log.debug("servlet.start");

        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        String userName = userSession.checkSession(out, request, methodName);
        if (userName == null) {
            log.error("No user session: cookies invalid");
            return;
        }

        Session aaa = 
            HibernateUtil.getSessionFactory(Utils.getDbName()).getCurrentSession();
        aaa.beginTransaction();
        AuthValue authVal = mgr.checkAccess(userName, "AAA", "modify");
        if (authVal == AuthValue.DENIED) {
            log.error("Not authorized to perform admin operations");
            Utils.handleFailure(out,
                    "not authorized to perform admin operations",
                    methodName, aaa);
            return;
        }
        String attrsUpdated = request.getParameter("authAttrsUpdated");
        if (attrsUpdated != null) {
            attrsUpdated = attrsUpdated.trim();
        } else {
            attrsUpdated = "";
        }
        Map outputMap = new HashMap();
        this.outputAttributeMenu(outputMap);
        String rpcParam = request.getParameter("rpc");
        // Make sure to update these exactly once.
        // rpc being unset makes sure they get updated in the beginning.
        // If just the attributes have been updated, don't redisplay.
        if (((rpcParam == null) || (rpcParam.trim().equals("")) ||
                attrsUpdated.equals(""))) {
            this.outputResourceMenu(outputMap);
            this.outputPermissionMenu(outputMap);
            this.outputConstraintMenu(outputMap);
        }
        if ((rpcParam == null) || rpcParam.trim().equals("")) {
            this.outputRpcs(outputMap);
        }
        outputMap.put("method", methodName);
        // this form does not reset status
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("{}&&" + jsonObject);
        aaa.getTransaction().commit();
        log.debug("servlet.end");      
    }

    public void
        doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }

    public void
        outputAttributeMenu(Map outputMap) {

        AttributeDAO attributeDAO = new AttributeDAO(Utils.getDbName());
        List<Attribute> attributes = attributeDAO.list();
        List<String> attributeList = new ArrayList<String>();
        RoleUtils utils = new RoleUtils();
        int ctr = 0;
        for (Attribute attr: attributes) {
            attributeList.add(attr.getName());
            if (ctr == 0) {
                attributeList.add("true");
            } else {
                attributeList.add("false");
            }
            ctr++;
        }
        outputMap.put("authAttributeNameMenu", attributeList);
    }

    public void
        outputResourceMenu(Map outputMap) {

        ResourceDAO resourceDAO = new ResourceDAO(Utils.getDbName());
        List<Resource> resources = resourceDAO.list();
        List<String> resourceList = new ArrayList<String>();
        int ctr = 0;
        for (Resource resource: resources) {
            resourceList.add(resource.getName());
            if (ctr == 0) {
                resourceList.add("true");
            } else {
                resourceList.add("false");
            }
            ctr++;
        }
        outputMap.put("resourceNameMenu", resourceList);
    }

    public void
        outputPermissionMenu(Map outputMap) {

        PermissionDAO permissionDAO = new PermissionDAO(Utils.getDbName());
        List<Permission> permissions = permissionDAO.list();
        List<String> permissionList = new ArrayList<String>();
        int ctr = 0;
        for (Permission permission: permissions) {
            permissionList.add(permission.getName());
            if (ctr == 0) {
                permissionList.add("true");
            } else {
                permissionList.add("false");
            }
            ctr++;
        }
        outputMap.put("permissionNameMenu", permissionList);
    }

    public void
        outputConstraintMenu(Map outputMap) {

        ConstraintDAO constraintDAO = new ConstraintDAO(Utils.getDbName());
        List<Constraint> constraints = constraintDAO.list();
        List<String> constraintList = new ArrayList<String>();
        int ctr = 0;
        for (Constraint constraint: constraints) {
            constraintList.add(constraint.getName());
            if (ctr == 0) {
                constraintList.add("true");
            } else {
                constraintList.add("false");
            }
            ctr++;
        }
        outputMap.put("constraintNameMenu", constraintList);
    }

    /**
     * Outputs permitted resource/permission/constraint triplets, along
     * with constraint type.   Could
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
        ConstraintDAO constraintDAO= new ConstraintDAO(Utils.getDbName());
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
                this.log("couldn't find resource: " + rpc.getResourceId());
                continue;
            }
            Permission perm = permissionDAO.findById(rpc.getPermissionId(),
                                                     false);
            if (perm != null) {
                rpcEntry.add(perm.getName());
            } else {
                this.log("couldn't find permission: " + rpc.getPermissionId());
                continue;
            }
            // null constraints are added on the client side; rpc table
            // has constraintId as not null, so one here would be an error.
            Constraint constraint =
                constraintDAO.findById(rpc.getConstraintId(), false);
            if (constraint != null) {
                rpcEntry.add(constraint.getName());
                rpcEntry.add(constraint.getType());
            } else {
                this.log("couldn't find constraint: " + rpc.getConstraintId());
                continue;
            }
            rpcList.add(rpcEntry);
        }
        outputMap.put("rpcData", rpcList);
    }
}
