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
 * Query reservation servlet
 * 
 * @author David Robertson, Mary Thompson
 *
 */

public class QueryReservation extends HttpServlet {

    /**
     * doGet
     * 
     * @param request HttpServletRequest contains the gri of the reservation
     * @param response HttpServletResponse contains: gri, status, user, description
     *        start, end and create times, bandwidth, vlan tag, and path information. 
     */
    private Logger log;
    public void
        doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.log = Logger.getLogger(this.getClass());
        String methodName = "QueryReservation";
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

        String[] paramValues = request.getParameterValues("gri");
        inputMap.put("gri", paramValues);

        try {
            CoreRmiInterface rmiClient = new CoreRmiClient();
            rmiClient.init();
            outputMap = rmiClient.queryReservation(inputMap, userName);
        } catch (Exception ex) {
            this.log.error("rmiClient failed: " + ex.getMessage());
            Utils.handleFailure(out, "failed to query Reservations: " +
                                ex.getMessage(), methodName, null);
        }
        String errorMsg = (String) outputMap.get("error");
        if (errorMsg != null) {
            this.log.error(errorMsg);
            Utils.handleFailure(out, errorMsg, methodName, null);
            return;
        }

       
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("/* " + jsonObject + " */");
        this.log.info("servlet.end");
        return;

    }

    public void
        doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }

    
}
