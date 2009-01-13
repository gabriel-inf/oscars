package net.es.oscars.servlets;


import java.io.*;
import java.util.*;
import java.rmi.RemoteException;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.log4j.Logger;
import net.sf.json.*;
import net.es.oscars.rmi.bss.BssRmiInterface;

/**
 * Query reservation servlet
 *
 * @author David Robertson, Mary Thompson
 *
 */
public class QueryReservation extends HttpServlet {
    private Logger log = Logger.getLogger(QueryReservation.class);

    /**
     * Handles QueryReservation servlet request.
     *
     * @param request HttpServletRequest contains the gri of the reservation
     * @param response HttpServletResponse contains: gri, status, user,
     *        description start, end and create times, bandwidth, vlan tag,
     *        and path information.
     */
    public void
        doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String methodName = "QueryReservation";
        this.log.debug("servlet.start");

        UserSession userSession = new UserSession();
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        String userName = userSession.checkSession(out, request, methodName);
        if (userName == null) {
            this.log.error("No user session: cookies invalid");
            return;
        }
        HashMap<String, Object> params = new HashMap<String, Object>();
        HashMap<String, Object> outputMap = new HashMap<String, Object>();

        params.put("gri", request.getParameterValues("gri")[0]);
        params.put("caller", "WBUI");
        // which sections of the page to display are controlled on the
        // RMI server side in the rmi module
        try {
            BssRmiInterface rmiClient = ServletUtils.getCoreRmiClient(methodName, log, out);
            outputMap = rmiClient.queryReservation(params, userName);
        } catch (RemoteException ex) {
            this.log.debug("RemoteException rmiClient failed: " + ex.getMessage());
            ServletUtils.handleFailure(out, "failed to query Reservations: " + ex.getMessage(), methodName);
            return;
        } catch (Exception ex) {
            this.log.debug("Exception rmiClient failed: " + ex.getMessage());
            ServletUtils.handleFailure(out, "failed to query Reservations: " + ex.getMessage(), methodName);
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
