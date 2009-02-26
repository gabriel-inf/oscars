package net.es.oscars.servlets;


import java.io.*;
import java.util.*;
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
        AuthValue authVal = null;
        try {
            BssRmiInterface bssRmiClient =
                RmiUtils.getBssRmiClient(methodName, log);
            rmiReply = bssRmiClient.queryReservation(gri, userName);
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

    public void
        contentSection(RmiQueryResReply rmiReply, Map<String,Object> outputMap)
            throws BSSException {

        InetAddress inetAddress = null;
        String hostName = null;
        Long longParam = null;
        Integer intParam = null;
        String strParam = null;

        Reservation resv = rmiReply.getReservation();
        String gri = resv.getGlobalReservationId();

        Path path = resv.getPath(PathType.LOCAL);
        if (path == null) {
            path = resv.getPath(PathType.REQUESTED);
        }
        Path interPath = resv.getPath(PathType.INTERDOMAIN);
        Layer2Data layer2Data = null;
        Layer3Data layer3Data = null;
        MPLSData mplsData = null;
        if (path != null) {
            layer2Data = path.getLayer2Data();
            layer3Data = path.getLayer3Data();
            mplsData = path.getMplsData();
        }
        String status = resv.getStatus();
        // always blank NEW GRI field, current GRI is in griReplace's
        // innerHTML
        outputMap.put("newGri", "");
        outputMap.put("griReplace", gri);
        outputMap.put("statusReplace", status);
        outputMap.put("userReplace", resv.getLogin());
        String sanitized = resv.getDescription().replace("<", "");
        String sanitized2 = sanitized.replace(">", "");
        outputMap.put("descriptionReplace", sanitized2);

        outputMap.put("modifyStartSeconds", resv.getStartTime());
        outputMap.put("modifyEndSeconds", resv.getEndTime());
        outputMap.put("createdTimeConvert", resv.getCreatedTime());
        // convert to Mbps, commas added by Dojo
        outputMap.put("bandwidthReplace", resv.getBandwidth()/1000000);
        if (layer2Data != null) {
            outputMap.put("sourceReplace", layer2Data.getSrcEndpoint());
            outputMap.put("destinationReplace", layer2Data.getDestEndpoint());
            QueryReservation.handleVlans(path, interPath, status, outputMap);
        } else if (layer3Data != null) {
            strParam = layer3Data.getSrcHost();
            try {
                inetAddress = InetAddress.getByName(strParam);
                hostName = inetAddress.getHostName();
            } catch (UnknownHostException e) {
                hostName = strParam;
            }
            outputMap.put("sourceReplace", hostName);
            strParam = layer3Data.getDestHost();
            try {
                inetAddress = InetAddress.getByName(strParam);
                hostName = inetAddress.getHostName();
            } catch (UnknownHostException e) {
                hostName = strParam;
            }
            outputMap.put("destinationReplace", hostName);
            intParam = layer3Data.getSrcIpPort();
            if ((intParam != null) && (intParam != 0)) {
                outputMap.put("sourcePortReplace", intParam);
            }
            intParam = layer3Data.getDestIpPort();
            if ((intParam != null) && (intParam != 0)) {
                outputMap.put("destinationPortReplace", intParam);
            }
            strParam = layer3Data.getProtocol();
            if (strParam != null) {
                outputMap.put("protocolReplace", strParam);
            }
            strParam = layer3Data.getDscp();
            if (strParam !=  null) {
                outputMap.put("dscpReplace", strParam);
            }
        }
        if (mplsData != null) {
            longParam = mplsData.getBurstLimit();
            if (longParam != null) {
                outputMap.put("burstLimitReplace", longParam);
            }
            if (mplsData.getLspClass() != null) {
                outputMap.put("lspClassReplace", mplsData.getLspClass());
            }
        }
        QueryReservation.outputPaths(path, interPath,
                                rmiReply.isInternalPathAuthorized(), outputMap);
    }

    public static void handleVlans(Path path, Path interPath, String status,
                                   Map<String,Object> outputMap) 
            throws BSSException {

        List<String> vlanTags = BssUtils.getVlanTags(path);
        List<String> interVlanTags = null;
        String srcVlanTag = "";
        String destVlanTag = "";
        // use interdomain path for VLAN source and dest if present
        if (interPath != null) {
            interVlanTags = BssUtils.getVlanTags(interPath);
            if (!interVlanTags.isEmpty()) {
                srcVlanTag = interVlanTags.get(0);
                destVlanTag = interVlanTags.get(interVlanTags.size()-1);
            }
        }
        // necessary for older reservations; check local path if no
        // associated information for interdomain path
        if (srcVlanTag.equals("")) {
            if (!vlanTags.isEmpty()) {
                srcVlanTag = vlanTags.get(0);
                destVlanTag = vlanTags.get(vlanTags.size()-1);
            }
        }
        if (!srcVlanTag.equals("")) {
            QueryReservation.outputVlan(srcVlanTag, outputMap, "src");
            QueryReservation.outputVlan(destVlanTag, outputMap, "dest");
        } else {
            if (status.equals("SUBMITTED") || status.equals("ACCEPTED")) {
                outputMap.put("srcVlanReplace", "VLAN setup in progress");
                outputMap.put("destVlanReplace", "VLAN setup in progress");
                outputMap.put("srcTaggedReplace", "");
                outputMap.put("destTaggedReplace", "");
            } else {
                outputMap.put("srcVlanReplace",
                              "No VLAN tag was ever set up");
                outputMap.put("destVlanReplace",
                              "No VLAN tag was ever set up");
                outputMap.put("srcTaggedReplace", "");
                outputMap.put("destTaggedReplace", "");
            }
        }
        if (!vlanTags.isEmpty()) {
            QueryReservation.outputVlanTable(vlanTags, "vlanPathReplace",
                                             outputMap);
        }
        // if two or less hops, strictly local
        if ((interPath != null) && (interVlanTags.size() > 2)) {
            QueryReservation.outputVlanTable(interVlanTags,
                                             "vlanInterPathReplace", outputMap);
        }
    }

    public static void outputVlan(String vlanTag, Map<String, Object> outputMap,
                                  String prefix) {

        if (vlanTag != null) {
            //If its a negative number try converting it
            //Prior to reservation completing may be a range or "any"
            int storedVlan = 0;
            try {
                storedVlan = Integer.parseInt(vlanTag);
                vlanTag = Math.abs(storedVlan) + "";
            } catch (Exception e) {}
            outputMap.put(prefix + "VlanReplace", vlanTag);
            if (storedVlan >= 0) {
                outputMap.put(prefix + "TaggedReplace", "true");
            } else {
                outputMap.put(prefix + "TaggedReplace", "false");
            }
        }
    }

    public static void
        outputPaths(Path path, Path interPath, boolean internalAuth,
                    Map<String,Object> outputMap) throws BSSException {

        String pathStr = BssUtils.pathToString(path, false);
        // in this case, path has not been set up yet, or an error has occurred
        // and the path will never be set up
        if (pathStr.equals("")) {
            return;
        }
        // don't allow non-authorized user to see internal hops
        if (!internalAuth) {
            String[] hops = pathStr.split("\n");
            pathStr = hops[0] + "\n";
            pathStr += hops[hops.length-1];
        } else {
            StringBuilder sb = new StringBuilder();
            // Utils.pathToString has new line separated hops
            String[] hops = pathStr.split("\n");
            sb.append("<tbody>");
            // enforce one hop per line in outer table cell
            for (int i=0; i < hops.length; i++) {
                sb.append("<tr><td class='innerHops'>" + hops[i] + "</td></tr>");
            }
            sb.append("</tbody>");
            if (path.getLayer2Data() != null) {
                outputMap.put("pathReplace", sb.toString());
            } else {
                outputMap.put("path3Replace", sb.toString());
            }
        }
        String interPathStr = BssUtils.pathToString(interPath, true);
        if (!interPathStr.equals("")) {
            StringBuilder sb = new StringBuilder();
            String[] hops = interPathStr.split("\n");
            // enforce one hop per line in outer table cell
            sb.append("<tbody>");
            for (int i=0; i < hops.length; i++) {
                sb.append("<tr><td class='innerHops'>" + hops[i] +
                          "</td></tr>");
            }
            sb.append("</tbody>");
            if (interPath.getLayer2Data() != null) {
                outputMap.put("interPathReplace", sb.toString());
            } else {
                outputMap.put("interPath3Replace", sb.toString());
            }
        }
    }

    public static void outputVlanTable(List<String> vlanTags, String nodeId,
                                       Map<String,Object> outputMap) {
        StringBuilder sb = new StringBuilder();
        sb.append("<tbody>");
        for (String vlanTag: vlanTags) {
            sb.append("<tr><td class='innerHops'>" + vlanTag + "</td></tr>");
        }
        sb.append("</tbody>");
        outputMap.put(nodeId, sb.toString());
    }
}
