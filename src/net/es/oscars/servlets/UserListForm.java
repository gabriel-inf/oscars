package net.es.oscars.servlets;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.hibernate.*;
import net.sf.json.*;

import net.es.oscars.database.HibernateUtil;
import net.es.oscars.aaa.*;
import net.es.oscars.aaa.UserManager.AuthValue;

public class UserListForm extends HttpServlet {

    public void
        doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        UserSession userSession = new UserSession();

        String methodName = "UserListForm";
        PrintWriter out = response.getWriter();
        response.setContentType("text/json-comment-filtered");
        String userName = userSession.checkSession(out, request, methodName);
        if (userName == null) { return; }
        UserManager mgr = new UserManager(Utils.getDbName());
        Session aaa = 
            HibernateUtil.getSessionFactory(Utils.getDbName()).getCurrentSession();
        aaa.beginTransaction();
        AuthValue authVal = mgr.checkAccess(userName, "Users", "query");
        Map outputMap = new HashMap();
        if  (authVal == AuthValue.ALLUSERS) {
            outputMap.put("userRowSelectableDisplay", Boolean.TRUE);
        } else {
            outputMap.put("userRowSelectableDisplay", Boolean.FALSE);
        }
        authVal = mgr.checkAccess(userName, "AAA", "list");
        if (authVal != AuthValue.DENIED) {
            outputMap.put("attributeInfoDisplay", Boolean.TRUE);
            this.outputAttributeMenu(outputMap);
        } else {
            outputMap.put("attributeInfoDisplay", Boolean.FALSE);
        }
        outputMap.put("status", "User list form");
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("/* " + jsonObject + " */");
        aaa.getTransaction().commit();
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
        List<String> attrList = new ArrayList<String>();
        attrList.add("Any");
        attrList.add("true");
        for (Attribute attr: attributes) {
            attrList.add(attr.getName());
            attrList.add("false");
        }
        outputMap.put("attributeMenu", attrList);
    }
}
