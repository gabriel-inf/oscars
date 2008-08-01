package net.es.oscars.servlets;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;
import org.hibernate.*;
import net.sf.json.*;
import org.apache.log4j.Logger;

import net.es.oscars.database.HibernateUtil;
import net.es.oscars.aaa.User;
import net.es.oscars.aaa.UserManager;
import net.es.oscars.aaa.AAAException;
import net.es.oscars.aaa.UserManager.AuthValue;


public class UserRemove extends HttpServlet {

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException, ServletException {

        Logger log = Logger.getLogger(this.getClass());
        log.info("servlet.start");
        String methodName = "UserRemove";
        UserSession userSession = new UserSession();
        UserManager mgr = new UserManager(Utils.getDbName());
        PrintWriter out = response.getWriter();
        response.setContentType("text/json-comment-filtered");
        String userName = userSession.checkSession(out, request, methodName);
        if (userName == null) { return; }

        String profileName = request.getParameter("profileName");
        Session aaa = 
            HibernateUtil.getSessionFactory("aaa").getCurrentSession();
        aaa.beginTransaction();
 
        AuthValue authVal = mgr.checkAccess(userName, "Users", "modify");
        try {
            // cannot remove oneself
            if (profileName == userName) { 
                log.error("may not remove yourself");
                Utils.handleFailure(out, "may not remove yourself",
                                    methodName, aaa);
                return;
            }
            if (authVal == AuthValue.ALLUSERS) {
                mgr.remove(profileName);
            } else {
                log.error("no permission to modify users");
                Utils.handleFailure(out,"no permission modify users",
                                    methodName, aaa);
                return;
            }
        } catch (AAAException e) {
            log.error(e.getMessage());
            Utils.handleFailure(out, e.getMessage(), methodName, aaa);
            return;
        }
        authVal = mgr.checkAccess(userName, "Users", "list");
        // shouldn't be able to get to this point, but just in case
        if (!(authVal == AuthValue.ALLUSERS)) {
            log.error("no permission to list users");
            Utils.handleFailure(out, "no permission to list users",
                                methodName, aaa);
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
        log.info("servlet.end");
    }

    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }
}
