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


public class AuthorizationModify extends HttpServlet {

    public void
        doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        UserSession userSession = new UserSession();
        UserManager mgr = new UserManager(Utils.getDbName());
        Logger log = Logger.getLogger(this.getClass());
        log.debug("servlet.start");

        String methodName = "AuthorizationModify";
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
        if (authVal == AuthValue.DENIED)  { 
            log.error("Not allowed to modify an authorization");
            Utils.handleFailure(out, "not allowed to modify an authorization",
                                methodName, aaa);
            return;
        }
        Map outputMap = new HashMap();
        outputMap.put("status", "Unimplemented yet");
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
}
