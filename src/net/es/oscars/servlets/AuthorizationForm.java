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
        response.setContentType("text/json-comment-filtered");
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
        Map outputMap = new HashMap();
        this.outputAttributeMenu(outputMap);
        this.outputResourceMenu(outputMap);
        this.outputPermissionMenu(outputMap);
        this.outputConstraintMenu(outputMap);
        outputMap.put("status", "Authorization");
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("/* " + jsonObject + " */");
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
        // default is just first in list
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
        // TODO:  will need hidden parameter to distinguish between details
        // and add form if go that route
        outputMap.put("authAttributeNameMenu", attributeList);
    }

    public void
        outputResourceMenu(Map outputMap) {

        ResourceDAO resourceDAO = new ResourceDAO(Utils.getDbName());
        List<Resource> resources = resourceDAO.list();
        List<String> resourceList = new ArrayList<String>();
        // default is just first in list
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
        // TODO:  will need hidden parameter to distinguish between details
        // and add form if go that route
        outputMap.put("resourceNameMenu", resourceList);
    }

    public void
        outputPermissionMenu(Map outputMap) {

        PermissionDAO permissionDAO = new PermissionDAO(Utils.getDbName());
        List<Permission> permissions = permissionDAO.list();
        List<String> permissionList = new ArrayList<String>();
        // default is just first in list
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
        // TODO:  will need hidden parameter to distinguish between details
        // and add form if go that route
        outputMap.put("permissionNameMenu", permissionList);
    }

    public void
        outputConstraintMenu(Map outputMap) {

        ConstraintDAO constraintDAO = new ConstraintDAO(Utils.getDbName());
        List<Constraint> constraints = constraintDAO.list();
        List<String> constraintList = new ArrayList<String>();
        constraintList.add("None");
        constraintList.add("true");
        for (Constraint constraint: constraints) {
            constraintList.add(constraint.getName());
            constraintList.add("false");
        }
        outputMap.put("constraintNameMenu", constraintList);
    }
}
