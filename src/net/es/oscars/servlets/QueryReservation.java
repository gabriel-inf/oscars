package net.es.oscars.servlets;


import java.io.*;
import java.util.*;
import java.rmi.RemoteException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;
import net.sf.json.*;

import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.BssUtils;
import net.es.oscars.bss.topology.*;
import net.es.oscars.rmi.RmiUtils;
import net.es.oscars.rmi.bss.BssRmiInterface;
import net.es.oscars.rmi.bss.xface.RmiQueryResRequest;
import net.es.oscars.rmi.bss.xface.RmiQueryResReply;
import net.es.oscars.rmi.aaa.AaaRmiInterface;
import net.es.oscars.aaa.AuthValue;

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
        RmiQueryResRequest rmiRequest = new RmiQueryResRequest();
        RmiQueryResReply rmiReply = new RmiQueryResReply();
        Map<String, Object> outputMap = new HashMap<String, Object>();
        rmiRequest.setGlobalReservationId(request.getParameter("gri"));
        AuthValue authVal = null;
        try {
            BssRmiInterface bssRmiClient =
                RmiUtils.getBssRmiClient(methodName, log);
            rmiReply = bssRmiClient.queryReservation(rmiRequest, userName);
            AaaRmiInterface aaaRmiClient =
                RmiUtils.getAaaRmiClient(methodName, log);
            authVal =
                aaaRmiClient.checkAccess(userName, "Reservations", "modify");
            // check to see if user is allowed to see the buttons allowing
            // reservation modification
            if (authVal != AuthValue.DENIED) {
                outputMap.put("resvModifyDisplay", Boolean.TRUE);
                outputMap.put("resvCautionDisplay", Boolean.TRUE);
            } else {
                outputMap.put("resvModifyDisplay", Boolean.FALSE);
                outputMap.put("resvCautionDisplay", Boolean.FALSE);
            }
            // check to see if user is allowed to see the clone button, which
            // requires generic reservation create authorization
            authVal = aaaRmiClient.checkModResAccess(userName, "Reservations",
                                                "create", 0, 0, false, false);
            if (authVal != AuthValue.DENIED) {
                outputMap.put("resvCloneDisplay", Boolean.TRUE);
            } else {
                outputMap.put("resvCloneDisplay", Boolean.FALSE);
            }
        } catch (Exception e) {
            ServletUtils.handleFailure(out, e, methodName);
            return;
        }
        try {
            this.contentSection(rmiReply, outputMap);
        } catch (BSSException ex) {
            ;
        }
        Reservation resv = rmiReply.getReservation();
        outputMap.put("status", "Reservation details for " +
                                resv.getGlobalReservationId());
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
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

    public void
        contentSection(RmiQueryResReply rmiReply, Map<String,Object> outputMap)
            throws BSSException {

            // TODO:  add back
    }
}
