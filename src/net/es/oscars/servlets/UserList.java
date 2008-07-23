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
        this.log.debug("userList:start");

        String methodName = "UserList";
        UserSession userSession = new UserSession();
        PrintWriter out = response.getWriter();
        response.setContentType("text/json-comment-filtered");
        String userName = userSession.checkSession(out, request);
        if (userName == null) { return; }
        String attributeName = request.getParameter("attributeName");
        if (attributeName != null) {
            attributeName = attributeName.trim();
        }
        Session aaa = 
            HibernateUtil.getSessionFactory(Utils.getDbName()).getCurrentSession();
        aaa.beginTransaction();     
    
        Map outputMap = new HashMap();
        outputMap.put("status", "User list");
        try {
            this.outputUsers(outputMap, userName, attributeName);
        } catch (AAAException e) {
            Utils.handleFailure(out, e.getMessage(), methodName, aaa);
            return;
        }
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("/* " + jsonObject + " */");
        aaa.getTransaction().commit();
        this.log.debug("userList:finish");
    }

    /**
     * outputUsers - checks access and gets the list of users if allowed.
     *  
     * @param outputMap Map containing JSON data
     * @param userName String containing name of user making request
     * @param attributeName String containing attribute name
     * @throws AAAException
     */
    public void outputUsers(Map outputMap, String userName,
                            String attributeName) 
            throws AAAException {

        String institutionName; 
        List<User> users = null;
        UserManager mgr = new UserManager(Utils.getDbName());
        
        AuthValue authVal = mgr.checkAccess(userName, "Users", "list");
        if (authVal == AuthValue.ALLUSERS) {
            if ((attributeName == null) || (attributeName.equals("Any"))) {
                users = mgr.list();
            } else {
                authVal = mgr.checkAccess(userName, "AAA", "list");
                if (authVal == AuthValue.DENIED) {
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
        long seconds = System.currentTimeMillis()/1000;
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
}
