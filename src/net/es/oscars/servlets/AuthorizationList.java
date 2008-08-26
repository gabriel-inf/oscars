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
        String attributeName = request.getParameter("attributeName");
        if (attributeName != null) {
            attributeName = attributeName.trim();
        } else {
            attributeName = "";
        }
        try {
            this.outputAuthorizations(outputMap, attributeName);
        } catch (AAAException e) {
            Utils.handleFailure(out, e.getMessage(), methodName, aaa);
            return;
        }
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

    public void
        outputAttributeMenu(Map outputMap) {

        AttributeDAO attributeDAO = new AttributeDAO(Utils.getDbName());
        List<Attribute> attributes = attributeDAO.list();
        List<String> attributeList = new ArrayList<String>();
        attributeList.add("Any");
        attributeList.add("true");
        for (Attribute attr: attributes) {
            attributeList.add(attr.getName());
            attributeList.add("false");
        }
        outputMap.put("attributeSelectMenu", attributeList);
    }

    /**
     * Sets the list of authorizations to display in a grid.
     *  
     * @param outputMap Map containing JSON data
     * @throws AAAException
     */
    public void outputAuthorizations(Map outputMap, String attributeName) 
            throws AAAException {

        List<Authorization> auths = null;
        if (attributeName.equals("")) {
            this.outputAttributeMenu(outputMap);
        }
        AuthorizationDAO authDAO = new AuthorizationDAO(Utils.getDbName());
        if (attributeName.equals("") || (attributeName.equals("Any"))) {
            auths = authDAO.list();
        } else {
            auths = authDAO.listAuthByAttr(attributeName);
        }
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
            authEntry.add(constraintName);
            String constraintValue = auth.getConstraintValue();
            String constraintType = constraintDAO.getConstraintType(constraintName);
            if (constraintValue == null) {
                if (constraintType.equals("boolean") &&
                    !constraintName.equals("none")) {
                    authEntry.add("true");
                } else {
                    authEntry.add("");
                }
            } else {
                authEntry.add(constraintValue);
            }
            authList.add(authEntry);
        }
        outputMap.put("authData", authList);
    }
}
