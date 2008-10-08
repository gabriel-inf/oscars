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
     * Handles CancelReservation servlet request.
     * 
     * @param request HttpServletRequest - contains gri of reservation to cancel
     * @param response HttpServletResponse -contains gri of reservation, success or error status
     */
    public void
        doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.log = Logger.getLogger(this.getClass());
        String methodName = "CancelReservation";
        this.log.info("servlet.start");

        UserSession userSession = new UserSession();
        PrintWriter out = response.getWriter();
        
        response.setContentType("application/json");
        String userName = userSession.checkSession(out, request, methodName);
        if (userName == null) { 
            this.log.error("No user session: cookies invalid");
            return;
        }
 
        HashMap<String, String[]> inputMap = new HashMap<String, String[]>();
        HashMap<String, Object> outputMap = new HashMap<String, Object>();

        String[] paramValues = request.getParameterValues("gri");
        inputMap.put("gri", paramValues);

        try {
            CoreRmiInterface rmiClient = new CoreRmiClient();
            rmiClient.init();
            outputMap = rmiClient.cancelReservation(inputMap, userName);
        } catch (Exception ex) {
            this.log.error(ex.getMessage());
            Utils.handleFailure(out, "failed to cancel Reservation: " + ex.getMessage(),
                                      methodName, null);
            return;
        }
        String errorMsg = (String) outputMap.get("error");
        if (errorMsg != null) {
            this.log.error(errorMsg);
            Utils.handleFailure(out, errorMsg, methodName, null);
            return;
        }

        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("{}&&" + jsonObject);
        this.log.info("servlet.end");
        return;
    }

    public void
        doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }
}
