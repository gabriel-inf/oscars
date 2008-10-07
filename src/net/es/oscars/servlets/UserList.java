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


public class UserList extends HttpServlet {
    private Logger log;
    
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException, ServletException {

        this.log = Logger.getLogger(this.getClass());
        String methodName = "UserList";
        this.log.debug("servlet.start");

        UserSession userSession = new UserSession();
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        String userName = userSession.checkSession(out, request, methodName);
        if (userName == null) {
            this.log.error("No user session: cookies invalid");
            return;
        }
        Session aaa = 
            HibernateUtil.getSessionFactory(Utils.getDbName()).getCurrentSession();
        aaa.beginTransaction();     
        UserManager mgr = new UserManager(Utils.getDbName());
        Map outputMap = new HashMap();
        AuthValue authVal = mgr.checkAccess(userName, "Users", "query");
        // if allowed to see all users, show help information on clicking on
        // row to see user details
        if  (authVal == AuthValue.ALLUSERS) {
            outputMap.put("userRowSelectableDisplay", Boolean.TRUE);
        } else {
            outputMap.put("userRowSelectableDisplay", Boolean.FALSE);
        }
        outputMap.put("status", "User list");
        try {
            this.outputUsers(outputMap, userName, request);
        } catch (AAAException e) {
            this.log.error(e.getMessage());
            Utils.handleFailure(out, e.getMessage(), methodName, aaa);
            return;
        }
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("{}&&" + jsonObject);
        aaa.getTransaction().commit();
        this.log.debug("servlet.end");
    }

    /**
     * Checks access and gets the list of users if allowed.
     *  
     * @param outputMap Map containing JSON data
     * @param userName String containing name of user making request
     * @param request HttpServletRequest with form parameters
     * @throws AAAException
     */
    public void outputUsers(Map outputMap, String userName,
                            HttpServletRequest request)
            throws AAAException {

        String institutionName; 
        List<User> users = null;
        UserManager mgr = new UserManager(Utils.getDbName());
        
        String attributeName = request.getParameter("attributeName");
        if (attributeName != null) {
            attributeName = attributeName.trim();
        } else {
            attributeName = "";
        }
        String attrsUpdated = request.getParameter("userListAttrsUpdated");
        if (attrsUpdated != null) {
            attrsUpdated = attrsUpdated.trim();
        } else {
            attrsUpdated = "";
        }
        AuthValue authVal = mgr.checkAccess(userName, "Users", "list");
        AuthValue aaaVal = mgr.checkAccess(userName, "AAA", "list");
        if (authVal == AuthValue.ALLUSERS) {
            // check to see if need to (re)display menu
            if (attributeName.equals("") ||
                ((attrsUpdated != null) && !attrsUpdated.equals(""))) {
                if (aaaVal != AuthValue.DENIED) {
                    outputMap.put("attributeInfoDisplay", Boolean.TRUE);
                    outputMap.put("attributeMenuDisplay", Boolean.TRUE);
                    this.outputAttributeMenu(outputMap);
                } else {
                    outputMap.put("attributeInfoDisplay", Boolean.FALSE);
                    outputMap.put("attributeMenuDisplay", Boolean.FALSE);
                }
            }
            if (attributeName.equals("") || (attributeName.equals("Any"))) {
                users = mgr.list();
            } else {
                if (aaaVal == AuthValue.DENIED) {
                   users = mgr.list();
                } else {
                    UserAttributeDAO dao =
                        new UserAttributeDAO(Utils.getDbName());
                    users = dao.getUsersByAttribute(attributeName);
                }
            }
        } else if (authVal== AuthValue.SELFONLY) {
            users = (List<User>) mgr.query(userName);
        } else {
            throw new AAAException("no permission to list users");
        }
        ArrayList userList = new ArrayList();
        for (User user: users) {
            ArrayList userEntry = new ArrayList();
            userEntry.add(user.getLogin());
            userEntry.add(user.getLastName());
            userEntry.add(user.getFirstName());
            /* if an unknown institution id is found in the users table an
             * exception is thrown in hibernate.gclib generated code
             */
            try {
                institutionName = user.getInstitution().getName();
            } catch (org.hibernate.ObjectNotFoundException e) {
                institutionName = "unknown";
            }
            userEntry.add(institutionName);
            userEntry.add(user.getPhonePrimary());
            userList.add(userEntry);
        }
        outputMap.put("userData", userList);
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
