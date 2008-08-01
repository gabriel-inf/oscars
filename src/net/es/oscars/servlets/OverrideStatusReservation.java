package net.es.oscars.servlets;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.*;
import net.sf.json.*;

import net.es.oscars.rmi.CoreRmiClient;
import net.es.oscars.rmi.CoreRmiInterface;


public class OverrideStatusReservation extends HttpServlet {
    private Logger log;

    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {

        this.log = Logger.getLogger(this.getClass());
        String methodName = "OverrideStatusReservation";
        this.log.info("servlet.start");
        UserSession userSession = new UserSession();
        PrintWriter out = response.getWriter();

        response.setContentType("text/json-comment-filtered");
        String userName = userSession.checkSession(out, request, methodName);
        if (userName == null) {
            this.log.error("No user session: cookies invalid");
            return;
        }
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
            outputMap = rmiClient.modifyStatus(inputMap, userName);
        } catch (Exception ex) {
            this.log.error("rmiClient failed: " + ex.getMessage());
            Utils.handleFailure(out,
                    "OverrideStatusReservation not completed: " +
                    ex.getMessage(), methodName, null);
            return;
        }
        String errorMsg = (String)outputMap.get("error");
        if (errorMsg != null) {
            this.log.error(errorMsg);
            Utils.handleFailure(out, errorMsg, methodName, null);
            return;
        }

        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("/* " + jsonObject + " */");
        this.log.info("servlet.end");
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        this.doGet(request, response);
    }

   
}
