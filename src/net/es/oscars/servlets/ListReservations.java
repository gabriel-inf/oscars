package net.es.oscars.servlets;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;
import net.sf.json.*;

import net.es.oscars.rmi.CoreRmiClient;
import net.es.oscars.rmi.CoreRmiInterface;


public class ListReservations extends HttpServlet {

    private Logger log;

    /**
     * Handles servlet request (both get and post) from list reservations form.
     * 
     * @param request servlet request
     * @param response servlet response
     */
    public void
        doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        
        this.log = Logger.getLogger(this.getClass());
        this.log.info("ListReservations.start");
        String methodName = "ListReservations";
        HashMap<String, String[]> inputMap = new HashMap<String, String[]>();
        HashMap<String, Object> outputMap = new HashMap<String, Object>();
        UserSession userSession = new UserSession();
        net.es.oscars.servlets.Utils utils = new net.es.oscars.servlets.Utils();

        PrintWriter out = response.getWriter();
        response.setContentType("text/json-comment-filtered");
        String userName = userSession.checkSession(out, request);
        if (userName == null) { return; }
        
        Enumeration e = request.getParameterNames();
        while (e.hasMoreElements()) {
            String paramName = (String) e.nextElement();
            String[] paramValues = request.getParameterValues(paramName);
            inputMap.put(paramName, paramValues);
        }
        try {
            CoreRmiInterface rmiClient = new CoreRmiClient();
            rmiClient.init();
            outputMap = rmiClient.listReservations(inputMap, userName);
        } catch (Exception ex) {
            utils.handleFailure(out, "ListReservations not completed: " + ex.getMessage(), 
                   methodName, null, null);
            this.log.error("Error calling rmiClient for ListReservations", ex);
            return;
        }
        String errorMsg = (String)outputMap.get("error");
        if (errorMsg != null) {
            utils.handleFailure(out, errorMsg, methodName, null,null);
            this.log.error(errorMsg);
            this.log.info("ListReservations.finish with error" + errorMsg);
            return;
        }
        outputMap.put("status", "Reservations list");
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        this.log.info("ListReservation.finish: success");

        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("/* " + jsonObject + " */");

    }

    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }
}
