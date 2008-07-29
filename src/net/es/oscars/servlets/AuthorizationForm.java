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
        log.debug("AuthorizationForm: start");

        String methodName = "AuthorizationForm";
        PrintWriter out = response.getWriter();
        response.setContentType("text/json-comment-filtered");
        String userName = userSession.checkSession(out, request);
        if (userName == null) { return; }

        Session aaa = 
            HibernateUtil.getSessionFactory(Utils.getDbName()).getCurrentSession();
        aaa.beginTransaction();
        AuthValue authVal = mgr.checkAccess(userName, "AAA", "modify");
        if (authVal == AuthValue.DENIED) {
            Utils.handleFailure(out, "not authorized to perform admin operations",
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
        log.debug("AuthorizationForm: finish");      
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
        StringBuffer sb = new StringBuffer();
        sb.append("<select class='required' name='authAttributeName'>");
        // default is just first in list
        int ctr = 0;
        for (Attribute attr: attributes) {
            sb.append("<option value='" + attr.getName() + "' ");
            if (ctr == 0) {
                sb.append("selected='selected'" );
            }
            sb.append(">" + attr.getName() + "</option>" );
            ctr++;
        }
        sb.append("</select>");
        // TODO:  will need hidden parameter to distinguish between details
        // and add form if go that route
        outputMap.put("authAttrMenuReplace", sb.toString());
    }

    public void
        outputResourceMenu(Map outputMap) {

        ResourceDAO resourceDAO = new ResourceDAO(Utils.getDbName());
        List<Resource> resources = resourceDAO.list();
        StringBuffer sb = new StringBuffer();
        sb.append("<select class='required' name='resourceName'>");
        // default is just first in list
        int ctr = 0;
        for (Resource resource: resources) {
            sb.append("<option value='" + resource.getName() + "' ");
            if (ctr == 0) {
                sb.append("selected='selected'" );
            }
            sb.append(">" + resource.getName() + "</option>" );
            ctr++;
        }
        sb.append("</select>");
        // TODO:  will need hidden parameter to distinguish between details
        // and add form if go that route
        outputMap.put("resourceMenuReplace", sb.toString());
    }

    public void
        outputPermissionMenu(Map outputMap) {

        PermissionDAO permissionDAO = new PermissionDAO(Utils.getDbName());
        List<Permission> permissions = permissionDAO.list();
        StringBuffer sb = new StringBuffer();
        sb.append("<select class='required' name='permissionName'>");
        // default is just first in list
        int ctr = 0;
        for (Permission permission: permissions) {
            sb.append("<option value='" + permission.getName() + "' ");
            if (ctr == 0) {
                sb.append("selected='selected'" );
            }
            sb.append(">" + permission.getName() + "</option>" );
            ctr++;
        }
        sb.append("</select>");
        // TODO:  will need hidden parameter to distinguish between details
        // and add form if go that route
        outputMap.put("permissionMenuReplace", sb.toString());
    }

    public void
        outputConstraintMenu(Map outputMap) {

        ConstraintDAO constraintDAO = new ConstraintDAO(Utils.getDbName());
        List<Constraint> constraints = constraintDAO.list();
        StringBuffer sb = new StringBuffer();
        sb.append("<select name='constraintName'>");
        sb.append("<option value='None' selected='selected'>None</option>");
        for (Constraint constraint: constraints) {
            sb.append("<option value='" + constraint.getName() + "' ");
            sb.append(">" + constraint.getName() + "</option>" );
        }
        sb.append("</select>");
        // TODO:  will need hidden parameter to distinguish between details
        // and add form if go that route
        outputMap.put("constraintMenuReplace", sb.toString());
    }

}
