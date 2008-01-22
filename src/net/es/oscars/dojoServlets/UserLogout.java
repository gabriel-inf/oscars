package net.es.oscars.dojoServlets;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import net.sf.json.*;

public class UserLogout extends HttpServlet {

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException, ServletException {

        PrintWriter out = response.getWriter();
        response.setContentType("text/json-comment-filtered");
        UserSession userSession = new UserSession();
        Map outputMap = new HashMap();
        outputMap.put("method", "UserLogout");
        outputMap.put("success", Boolean.TRUE);
        outputMap.put("status", "User logged out.");
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("/* " + jsonObject + " */");
        userSession.expireCookie("sessionName", "", response);
    }

    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }
}
