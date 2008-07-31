package net.es.oscars.servlets;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.*;
import net.sf.json.*;

import net.es.oscars.rmi.CoreRmiClient;
import net.es.oscars.rmi.CoreRmiInterface;


public class PathTeardownReservation extends HttpServlet {
    private Logger log;

    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {

        this.log = Logger.getLogger(this.getClass());
        this.log.info("PathTeardownReservation.start");
        String methodName = "PathTeardownReservation";

        UserSession userSession = new UserSession();
        PrintWriter out = response.getWriter();

        response.setContentType("text/json-comment-filtered");

        String userName = userSession.checkSession(out, request);
        if (userName == null) { return; }
        
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
            outputMap = rmiClient.teardownPath(inputMap, userName);
        } catch (Exception ex) {
            Utils.handleFailure(out, "TeardownPathReservation not completed: " + ex.getMessage(), 
                    methodName, null);
            this.log.error("Error calling rmiClient for TeardownPathReservation", ex);
            return;
        }
        String errorMsg = (String)outputMap.get("error");
        if (errorMsg != null) {
            Utils.handleFailure(out, errorMsg, methodName, null);
            this.log.error("TeardownPathReservation failed: " + errorMsg);
            return;
        }

        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("/* " + jsonObject + " */");
        this.log.info("TeardownPathReservation.end");
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        this.doGet(request, response);
    }

   
}
