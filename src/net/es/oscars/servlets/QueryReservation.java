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
        this.log.info("QueryReservation.start");
        
        String methodName = "QueryReservation";
        UserSession userSession = new UserSession();
        net.es.oscars.servlets.Utils utils =
            new net.es.oscars.servlets.Utils();

        PrintWriter out = response.getWriter();
        response.setContentType("text/json-comment-filtered");
        String userName = userSession.checkSession(out, request);
        if (userName == null) { 
            this.log.info("QueryReservation.end: no user session");
            return; }
        
        HashMap<String, String[]> inputMap = new HashMap<String, String[]>();
        HashMap<String, Object> outputMap = new HashMap<String, Object>();

        String[] paramValues = request.getParameterValues("gri");
        inputMap.put("gri", paramValues);


        try {
            CoreRmiInterface rmiClient = new CoreRmiClient();
            rmiClient.init();
            outputMap = rmiClient.queryReservation(inputMap, userName);
        } catch (Exception ex) {
            utils.handleFailure(out, "failed to query Reservations: " + ex.getMessage(),
                                      methodName, null, null);
            this.log.info("QueryReservation.end: " + ex.getMessage());
        }
        String errorMsg = (String) outputMap.get("error");
        if (errorMsg != null) {
            utils.handleFailure(out, errorMsg, methodName, null, null);
            this.log.info("QueryReservation.end: " + errorMsg);
            return;
        }

       
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("/* " + jsonObject + " */");
        this.log.info("QueryReservation.end - success");
        return;

    }

    public void
        doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }

    
}
