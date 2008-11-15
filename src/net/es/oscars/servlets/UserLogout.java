package net.es.oscars.servlets;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import net.sf.json.*;
import org.apache.log4j.Logger;

public class UserLogout extends HttpServlet {
    private Logger log = Logger.getLogger(UserLogout.class);

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException, ServletException {

        log.info("UserLogout.start");
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        UserSession userSession = new UserSession();
        Map<String, Object> outputMap = new HashMap<String, Object>();
        outputMap.put("method", "UserLogout");
        outputMap.put("success", Boolean.TRUE);
        outputMap.put("status", "User logged out.");
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("{}&&" + jsonObject);
        userSession.expireCookie("userName", "", response);
        userSession.expireCookie("sessionName", "", response);
        log.info("UserLogout.end");
    }

    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }
}
