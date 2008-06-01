package net.es.oscars.servlets;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;
import org.hibernate.*;
import net.sf.json.*;

import net.es.oscars.database.HibernateUtil;
import net.es.oscars.aaa.User;
import net.es.oscars.aaa.UserManager;
import net.es.oscars.aaa.AAAException;
import net.es.oscars.aaa.UserManager.AuthValue;


public class UserRemove extends HttpServlet {

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException, ServletException {

        String methodName = "UserRemove";
        UserSession userSession = new UserSession();
        Utils utils = new Utils();
        UserManager mgr = new UserManager("aaa");
        PrintWriter out = response.getWriter();
        response.setContentType("text/json-comment-filtered");
        String userName = userSession.checkSession(out, request);
        if (userName == null) { return; }

        String profileName = request.getParameter("profileName");
        Session aaa = 
            HibernateUtil.getSessionFactory("aaa").getCurrentSession();
        aaa.beginTransaction();
 
        AuthValue authVal = mgr.checkAccess(userName, "Users", "modify");
        
        try {
            // cannot remove oneself
            if (profileName == userName) { 
                utils.handleFailure(out, "may not remove yourself",
                                    methodName, aaa, null);
                return;
            }
            if (authVal == AuthValue.ALLUSERS) {
                 mgr.remove(profileName);
            } else {
                   utils.handleFailure(out,"no permission modify users",
                                       methodName, aaa,null);
                   return;
            }
        } catch (AAAException e) {
            utils.handleFailure(out, e.getMessage(), methodName, aaa, null);
            return;
        }
        authVal = mgr.checkAccess(userName, "Users", "list");
        // shouldn't be able to get to this point, but just in case
        if (!(authVal == AuthValue.ALLUSERS)) {
            utils.handleFailure(out, "no permission to list users",
                                methodName, aaa, null);
            return;
        }
        Map outputMap = new HashMap();
        outputMap.put("status", "User " + profileName +
                                " successfully removed");
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("/* " + jsonObject + " */");
        aaa.getTransaction().commit();
    }

    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }
}
