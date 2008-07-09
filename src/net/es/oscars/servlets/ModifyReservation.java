package net.es.oscars.servlets;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.*;
import net.sf.json.*;

import net.es.oscars.rmi.CoreRmiClient;
import net.es.oscars.rmi.CoreRmiInterface;


public class ModifyReservation extends HttpServlet {
    private Logger log;

    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {

        this.log = Logger.getLogger(this.getClass());
        this.log.info("ModifyReservation.start");
        String methodName = "ModifyReservation";

        UserSession userSession = new UserSession();
        Utils utils = new Utils();
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
            outputMap = rmiClient.modifyReservation(inputMap, userName);
        } catch (Exception ex) {
            utils.handleFailure(out, "ModifyReservation not completed: " + ex.getMessage(), 
                    methodName, null, null);
            this.log.error("Error calling rmiClient for ModifyReservation", ex);
            return;
        }
        String errorMsg = (String)outputMap.get("error");
        if (errorMsg != null) {
            utils.handleFailure(out, errorMsg, methodName, null,null);
            this.log.error("ModifyReservation failed: " + errorMsg);
            return;
        }

        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("/* " + jsonObject + " */");
        this.log.info("ModifyReservation.end");
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        this.doGet(request, response);
    }

   
}
