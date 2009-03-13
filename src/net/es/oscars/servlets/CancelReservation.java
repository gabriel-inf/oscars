package net.es.oscars.servlets;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.log4j.Logger;
import net.sf.json.*;

import net.es.oscars.rmi.RmiUtils;
import net.es.oscars.rmi.bss.BssRmiInterface;

/**
 * Cancel Reservation servlet
 *
 * @author David Robertson, Mary Thompson
 *
 */

public class CancelReservation extends HttpServlet {
    private Logger log = Logger.getLogger(CancelReservation.class);

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
        this.log.info(methodName + ":start");

        UserSession userSession = new UserSession();
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        String userName = userSession.checkSession(out, request, methodName);
        if (userName == null) {
            this.log.warn("No user session: cookies invalid");
            return;
        }

 
        HashMap<String, Object> outputMap = new HashMap<String, Object>();
        String gri = request.getParameterValues("gri")[0];
        String status = "unknown";

        try {
            BssRmiInterface rmiClient =
                RmiUtils.getBssRmiClient(methodName, log);
           status = rmiClient.cancelReservation(gri, userName);

        } catch (Exception e) {
            ServletUtils.handleFailure(out, log, e, methodName);
            return;
        }
        outputMap.put("status", "Reservation " + gri + " cancellation: " +
                                status); 
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("{}&&" + jsonObject);
        this.log.info(methodName + ":end");
        return;
    }

    public void
        doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }
}
