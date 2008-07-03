package net.es.oscars.servlets;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.*;

import net.es.oscars.rmi.*;
import net.sf.json.JSONObject;

public class CreateReservation extends HttpServlet {
    private Logger log;

    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {

        this.log = Logger.getLogger(this.getClass());
        this.log.info("CreateReservation.start");

        PrintWriter out = response.getWriter();
        UserSession userSession = new UserSession();
        String userName = userSession.checkSession(out, request);
        if (userName == null) { return; }

        response.setContentType("text/json-comment-filtered");

        HashMap<String, String[]> inputMap = new HashMap<String, String[]>();
        HashMap<String, Object> outputMap = new HashMap<String, Object>();

        Enumeration e = request.getParameterNames();
        while (e.hasMoreElements()) {
            String paramName = (String) e.nextElement();
            String[] paramValues = request.getParameterValues(paramName);
            inputMap.put(paramName, paramValues);
        }


        try {
            CoreRmiInterface rmiClient = new CoreRmiClient();
            rmiClient.init();
            outputMap = rmiClient.createReservation(inputMap, userName);
        } catch (Exception ex) {
// FIXME needs proper error handling
            this.log.error("Error!", ex);
        }

        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("/* " + jsonObject + " */");
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        this.doGet(request, response);
    }


}
