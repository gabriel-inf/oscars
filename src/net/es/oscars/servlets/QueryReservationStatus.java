package net.es.oscars.servlets;


import java.io.*;
import java.util.*;

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
import net.es.oscars.rmi.bss.xface.RmiQueryResReply;

/**
 * Query reservation servlet
 *
 * @author David Robertson, Mary Thompson
 *
 */
public class QueryReservationStatus extends HttpServlet {
    private Logger log = Logger.getLogger(QueryReservationStatus.class);

    /**
     * Handles QueryReservationStatus servlet request.
     *
     * @param request HttpServletRequest contains the gri of the reservation
     * @param response HttpServletResponse contains: gri, status, user,
     *        description start, end and create times, bandwidth, vlan tag,
     *        and path information.
     */
    public void
        doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String methodName = "QueryReservationStatus";
        this.log.info(methodName + ":start");

        UserSession userSession = new UserSession();
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        String userName = userSession.checkSession(out, request, methodName);
        if (userName == null) {
            this.log.warn("No user session: cookies invalid");
            return;
        }
        RmiQueryResReply rmiReply = new RmiQueryResReply();
        Map<String, Object> outputMap = new HashMap<String, Object>();
        String gri = request.getParameter("gri");
        try {
            BssRmiInterface bssRmiClient =
                RmiUtils.getBssRmiClient(methodName, log);
            rmiReply = bssRmiClient.queryReservation(gri, userName);
        } catch (Exception e) {
            ServletUtils.handleFailure(out, log, e, methodName);
            return;
        }
        try {
            this.contentSection(rmiReply, outputMap);
        } catch (BSSException e) {
            ServletUtils.handleFailure(out, log, e, methodName);
            return;
        }
        Reservation resv = rmiReply.getReservation();
        if (resv.getStatusMessage() == null) {
            outputMap.put("status", "Reservation details for " + gri);
        } else {
            outputMap.put("status", resv.getStatusMessage());
        }
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

    /**
     * Only fills in those fields that might have changed due to change
     * in reservation status.
     */
    public void
        contentSection(RmiQueryResReply rmiReply, Map<String,Object> outputMap)
            throws BSSException {

        Reservation resv = rmiReply.getReservation();
        Path path = resv.getPath(PathType.LOCAL);
        if (path == null) {
            path = resv.getPath(PathType.REQUESTED);
        }
        Path interPath = resv.getPath(PathType.INTERDOMAIN);
        Layer2Data layer2Data = null;
        if (path != null) {
            layer2Data = path.getLayer2Data();
        }
        String status = resv.getStatus();
        outputMap.put("griReplace", resv.getGlobalReservationId());
        outputMap.put("statusReplace", status);
        if (layer2Data != null) {
            QueryReservation.handleVlans(path, interPath, status, outputMap);
        }
        QueryReservation.outputPaths(path, interPath,
                                rmiReply.isInternalPathAuthorized(), outputMap);
    }
}
