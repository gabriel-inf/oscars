package net.es.oscars.servlets;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;
import org.hibernate.*;
import net.sf.json.*;

import net.es.oscars.database.HibernateUtil;
import net.es.oscars.aaa.User;
import net.es.oscars.aaa.UserManager;
import net.es.oscars.aaa.AAAException;
import net.es.oscars.aaa.UserManager.AuthValue;


public class UserList extends HttpServlet {
    private Logger log;
    
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException, ServletException {


        this.log = Logger.getLogger(this.getClass());
        this.log.debug("userList:start");

        UserSession userSession = new UserSession();
        Utils utils = new Utils();
        PrintWriter out = response.getWriter();
        response.setContentType("text/json-comment-filtered");
        String userName = userSession.checkSession(out, request);
        if (userName == null) { return; }
        Session aaa = 
            HibernateUtil.getSessionFactory("aaa").getCurrentSession();
        aaa.beginTransaction();     
    
        Map outputMap = new HashMap();
        outputMap.put("status", "User list");
        try {
            this.outputUsers(outputMap, userName);
        } catch (AAAException e) {
            utils.handleFailure(out, e.getMessage(), aaa, null);
            return;
        }
        outputMap.put("method", "UserList");
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
     * @throws AAAException
     */
    public void outputUsers(Map outputMap, String userName) 
       throws AAAException {

        String institutionName; 
        List<User> users = null;
        UserManager mgr = new UserManager("aaa");
        
        AuthValue authVal = mgr.checkAccess(userName, "Users", "list");
        if (authVal == AuthValue.ALLUSERS) {
            users = mgr.list();
        } else if (authVal== AuthValue.SELFONLY) {
            users= (List<User>) mgr.query(userName);
        } else {
            throw new AAAException("no permission to list users");
        }
        long seconds = System.currentTimeMillis()/1000;
        outputMap.put("timestamp", seconds);
        ArrayList<HashMap<String,String>> userList = new ArrayList<HashMap<String,String>>();
        for (User user: users) {
            HashMap<String,String> userMap = new HashMap<String,String>();
            userMap.put("login", user.getLogin());
            userMap.put("lastName", user.getLastName());
            userMap.put("firstName", user.getFirstName());
            /* if an unknown institution id is found in the users table an
             * exception is thrown in hibernate.gclib generated code
             */
            try {
                institutionName = user.getInstitution().getName();
            } catch (org.hibernate.ObjectNotFoundException e) {
                institutionName = "unknown";
            }
            userMap.put("organization", institutionName);
            userMap.put("phone", user.getPhonePrimary());
            userList.add(userMap);
        }
        outputMap.put("items", userList);
    }


    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }
}
