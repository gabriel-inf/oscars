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
        outputMap.put("status", "Reservation details for " + gri);
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
        Layer2Data layer2Data = null;
        if (path != null) {
            layer2Data = path.getLayer2Data();
        }
        String status = resv.getStatus();
        outputMap.put("griReplace", resv.getGlobalReservationId());
        outputMap.put("statusReplace", status);
        if (layer2Data != null) {
            String vlanTag = BssUtils.getVlanTag(path);
            if (vlanTag != null) {
                //If its a negative number try converting it
                //Prior to reservation completing may be a range or "any"
                int storedVlan = 0;
                try {
                    storedVlan = Integer.parseInt(vlanTag);
                    vlanTag = Math.abs(storedVlan) + "";
                } catch(Exception e) {}
                outputMap.put("vlanReplace", vlanTag);
                if (storedVlan >= 0) {
                    outputMap.put("taggedReplace", "true");
                } else {
                    outputMap.put("taggedReplace", "false");
                }
            } else {
                if (status.equals("SUBMITTED") || status.equals("ACCEPTED")) {
                    outputMap.put("vlanReplace", "VLAN setup in progress");
                } else {
                    outputMap.put("vlanReplace",
                                  "No VLAN tag was ever set up");
                }
            }
        }
        String pathStr = BssUtils.pathToString(path, false);
        // in this case, path has not been set up yet, or an error has occurred
        // and the path will never be set up
        if ((pathStr != null) && pathStr.equals("")) {
            return;
        }
        // don't allow non-authorized user to see internal hops
        if ((pathStr != null) && !rmiReply.isInternalPathAuthorized()) {
            String[] hops = pathStr.trim().split("\n");
            pathStr = hops[0] + "\n";
            pathStr += hops[hops.length-1];
        }
        if (pathStr != null) {
            StringBuilder sb = new StringBuilder();
            // Utils.pathToString has new line separated hops
            String[] hops = pathStr.trim().split("\n");
            sb.append("<tbody>");
            // enforce one hop per line in outer table cell
            for (int i=0; i < hops.length; i++) {
                sb.append("<tr><td class='innerHops'>" + hops[i] + "</td></tr>");
            }
            sb.append("</tbody>");
            outputMap.put("pathReplace", sb.toString());
        }
        path = resv.getPath(PathType.INTERDOMAIN);
        String interPathStr = BssUtils.pathToString(path, true);
        if ((interPathStr != null) &&
                !interPathStr.trim().equals("")) {
            StringBuilder sb = new StringBuilder();
            String[] hops = interPathStr.trim().split("\n");
            // enforce one hop per line in outer table cell
            sb.append("<tbody>");
            for (int i=0; i < hops.length; i++) {
                sb.append("<tr><td class='innerHops'>" + hops[i] +
                          "</td></tr>");
            }
            sb.append("</tbody>");
            outputMap.put("interPathReplace", sb.toString());
        }
    }
}
