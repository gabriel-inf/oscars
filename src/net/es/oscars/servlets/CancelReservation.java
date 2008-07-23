package net.es.oscars.servlets;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.log4j.Logger;
import net.sf.json.*;
import net.es.oscars.rmi.CoreRmiClient;
import net.es.oscars.rmi.CoreRmiInterface;

/**
 * Cancel Reservation servlet
 * 
 * @author David Robertson, Mary Thompson
 *
 */

public class CancelReservation extends HttpServlet {
    private Logger log;
    /**
     * doGet
     * 
     * @param request HttpServletRequest - contains gri of reservation to cancel
     * @return response HttpServletResponse -contains gri of reservation, success or error status
     */
    public void
        doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.log = Logger.getLogger(this.getClass());
        this.log.info("CancelReservation.start");

        String methodName = "CancelReservation";
        UserSession userSession = new UserSession();
        PrintWriter out = response.getWriter();
        
        response.setContentType("text/json-comment-filtered");
        String userName = userSession.checkSession(out, request);
        if (userName == null) { 
            this.log.info("CancelReservation.end: no user session" );
            return; }
 
        HashMap<String, String[]> inputMap = new HashMap<String, String[]>();
        HashMap<String, Object> outputMap = new HashMap<String, Object>();

        String[] paramValues = request.getParameterValues("gri");
        inputMap.put("gri", paramValues);

        try {
            CoreRmiInterface rmiClient = new CoreRmiClient();
            rmiClient.init();
            outputMap = rmiClient.cancelReservation(inputMap, userName);
        } catch (Exception ex) {
            Utils.handleFailure(out, "failed to cancel Reservation: " + ex.getMessage(),
                                      methodName, null);
            this.log.info("CancelReservation.end: " + ex.getMessage());
            return;
        }
        String errorMsg = (String) outputMap.get("error");
        if (errorMsg != null) {
            Utils.handleFailure(out, errorMsg, methodName, null);
            this.log.info("CancelReservation.end: " + errorMsg);
            return;
        }

        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("/* " + jsonObject + " */");
        this.log.info("CancelReservation.end");
        return;
    }

    public void
        doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }
}
