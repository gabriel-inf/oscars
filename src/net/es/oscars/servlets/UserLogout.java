package net.es.oscars.servlets;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import net.sf.json.*;
import org.apache.log4j.Logger;

public class UserLogout extends HttpServlet {

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException, ServletException {

        Logger log = Logger.getLogger(this.getClass());
        log.info("servlet.start");
        PrintWriter out = response.getWriter();
        response.setContentType("text/json-comment-filtered");
        UserSession userSession = new UserSession();
        Map outputMap = new HashMap();
        outputMap.put("method", "UserLogout");
        outputMap.put("success", Boolean.TRUE);
        outputMap.put("status", "User logged out.");
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("/* " + jsonObject + " */");
        userSession.expireCookie("userName", "", response);
        userSession.expireCookie("sessionName", "", response);
        log.info("servlet.end");
    }

    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }
}
