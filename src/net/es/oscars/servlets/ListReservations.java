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
import net.es.oscars.rmi.bss.xface.RmiListResRequest;
import net.es.oscars.rmi.bss.xface.RmiListResReply;
import net.es.oscars.rmi.aaa.AaaRmiInterface;
import net.es.oscars.aaa.AuthValue;


public class ListReservations extends HttpServlet {
    private Logger log = Logger.getLogger(ListReservations.class);

    /**
     * Handles servlet request (both get and post) from list reservations form.
     *
     * @param request servlet request
     * @param response servlet response
     */
    public void
        doGet(HttpServletRequest servletRequest, HttpServletResponse response)
            throws IOException, ServletException {

        String methodName = "ListReservations";
        this.log.info(methodName + ":start");

        RmiListResRequest rmiRequest = this.getParameters(servletRequest);
        RmiListResReply rmiReply = new RmiListResReply();

        UserSession userSession = new UserSession();
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        String userName = userSession.checkSession(out, servletRequest, methodName);
        if (userName == null) {
            this.log.warn("No user session: cookies invalid");
            return;
        }
        AuthValue authVal = null;
        try {
            BssRmiInterface bssRmiClient =
                RmiUtils.getBssRmiClient(methodName, log);
            rmiReply = bssRmiClient.listReservations(rmiRequest, userName);
            AaaRmiInterface aaaRmiClient =userSession.getAaaInterface();
            authVal =
                aaaRmiClient.checkAccess(userName, "Reservations", "list");
        } catch (Exception e) {
            ServletUtils.handleFailure(out, log, e, methodName);
            return;
        }
        HashMap<String,Object> outputMap = new HashMap<String,Object>();
        // only display user search field if can look at other users'
        // reservations
        if (authVal.equals(AuthValue.MYSITE)) {
            outputMap.put("resvLoginDisplay", Boolean.TRUE);
        } else if (authVal.equals(AuthValue.SELFONLY)){
            outputMap.put("resvLoginDisplay", Boolean.FALSE);
        } else {
            outputMap.put("resvLoginDisplay", Boolean.TRUE);
        }
        List<Reservation> reservations = rmiReply.getReservations();
        this.outputReservations(outputMap, reservations);
        outputMap.put("totalRowsReplace", "Total rows: " + reservations.size());
        outputMap.put("status", "Reservations list");
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("{}&&" + jsonObject);
        this.log.info(methodName + ":end");
    }

    public void doPost(HttpServletRequest servletRequest,
                       HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(servletRequest, response);
    }

    public RmiListResRequest getParameters(HttpServletRequest servletRequest) {

        RmiListResRequest rmiRequest = new RmiListResRequest();
        Long startTimeSeconds = null;
        Long endTimeSeconds = null;
        int numRowsReq = 0;  // if default, return all results

        String numRowParam = servletRequest.getParameter("numRows");
        String loginEntered = servletRequest.getParameter("resvLogin");
        String description = servletRequest.getParameter("resvDescription");
        String startTimeStr = servletRequest.getParameter("startTimeSeconds");
        String endTimeStr = servletRequest.getParameter("endTimeSeconds");

        if (!startTimeStr.equals("")) {
            startTimeSeconds = Long.valueOf(startTimeStr.trim());
        }
        if (!endTimeStr.equals("")) {
            endTimeSeconds = Long.valueOf(endTimeStr.trim());
        }
        if (!numRowParam.trim().equals("") && !numRowParam.equals("all")) {
            numRowsReq = Integer.parseInt(numRowParam);
        }
        rmiRequest.setNumRequested(numRowsReq);
        if (!loginEntered.trim().equals("")) {
            rmiRequest.setLogin(loginEntered.trim());
        }
        if (!description.trim().equals("")) {
            rmiRequest.setDescription(description.trim());
        }
        rmiRequest.setStartTime(startTimeSeconds);
        rmiRequest.setEndTime(endTimeSeconds);
        rmiRequest.setStatuses(this.getStatuses(servletRequest));
        rmiRequest.setVlanTags(this.getList(servletRequest, "vlanSearch"));
        rmiRequest.setLinkIds(this.getList(servletRequest, "linkIds"));
        return rmiRequest;
    }

    /**
     * Gets list of statuses selected from menu.
     *
     * @param servletRequest servlet request
     * @return list of statuses to send to BSS
     */
    public List<String> getStatuses(HttpServletRequest servletRequest) {

        List<String> statuses = new ArrayList<String>();
        String paramStatuses[] = servletRequest.getParameterValues("statuses");
        if (paramStatuses != null) {
            for (int i=0 ; i < paramStatuses.length; i++) {
                statuses.add(paramStatuses[i]);
            }
        }
        return statuses;
    }

    /**
     * Gets list of parameter values from a space separated paramater string
     *
     * @param servletRequest servlet request
     * @param paramName string with parameter name
     * @return list of parameters to send to BSS
     */
    public List<String> getList(HttpServletRequest servletRequest,
                                String paramName) {

        List<String> paramList = new ArrayList<String>();
        String param = servletRequest.getParameter(paramName);
        if ((param != null) && !param.trim().equals("")) {
            String[] paramTags = param.trim().split(" ");
            for (int i=0; i < paramTags.length; i++) {
                paramList.add(paramTags[i]);
            }
        }
        return paramList;
    }

    /**
     * Formats reservation data sent back by list request from the reservation
     * manager into grid format that Dojo understands.
     *
     * @param outputMap map containing grid data
     * @param reservations list of reservations satisfying search criteria
     */
    public void outputReservations(Map<String, Object> outputMap,
                                   List<Reservation> reservations) {

        InetAddress inetAddress = null;
        String gri = "";
        String source = null;
        String hostName = null;
        String destination = null;

        this.log.debug("to outputReservations");
        ArrayList<HashMap<String,Object>> resvList =
            new ArrayList<HashMap<String,Object>>();
        int ctr = 0;
        for (Reservation resv: reservations) {
            HashMap<String,Object> resvMap = new HashMap<String,Object>();
            Path path = null;
            Path interPath = null;
            try {
                // no path info will be displayed if only a requested path
                path = resv.getPath(PathType.LOCAL);
                interPath = resv.getPath(PathType.INTERDOMAIN);
            } catch (BSSException ex) {
                outputMap.put("error", ex.getMessage());
                return;
            }
            this.log.debug("past getPath");
            if (path == null) {
                this.log.debug("path is null");
            }
            String pathStr = BssUtils.pathToString(path, false);
            String localSrc = null;
            String localDest = null;
            if (!pathStr.equals("")) {
                this.log.debug(pathStr);
                String[] hops = pathStr.trim().split("\n");
                localSrc = hops[0];
                localDest = hops[hops.length-1];
            }
            gri = resv.getGlobalReservationId();
            Layer3Data layer3Data = null;
            Layer2Data layer2Data = null;
            // NOTE:  using local path for this info for now
            if (path != null ) {
                layer3Data = path.getLayer3Data();
                layer2Data = path.getLayer2Data();
            }
            resvMap.put("id", Integer.toString(ctr));
            resvMap.put("gri", gri);
            resvMap.put("status", resv.getStatus());
            Long mbps = resv.getBandwidth()/1000000;

            String bandwidthField = mbps.toString() + "Mbps";
            resvMap.put("bandwidth", bandwidthField);
            // entries are converted on the fly on the client to standard
            // date and time format before the model's data is set
            resvMap.put("startTime", resv.getStartTime().toString());
            if (layer2Data != null) {
                resvMap.put("source",
                            URNParser.abbreviate(layer2Data.getSrcEndpoint()));
            } else if (layer3Data != null) {
                source = layer3Data.getSrcHost();
                try {
                    inetAddress = InetAddress.getByName(source);
                    hostName = inetAddress.getHostName();
                    resvMap.put("source", hostName);
                } catch (UnknownHostException e) {
                    resvMap.put("source", source);
                }
            }
            if (localSrc != null) {
                if (layer2Data != null) {
                    resvMap.put("localSource", URNParser.abbreviate(localSrc));
                } else {
                    resvMap.put("localSource", localSrc);
                }
            } else {
                resvMap.put("localSource", "");
            }
            // start of second sub-row
            resvMap.put("user", resv.getLogin());
            List<String> vlanTags = null;
            String vlanTag = "";
            try {
                // use interdomain if present for starting VLAN
                if (interPath != null) {
                    vlanTags = BssUtils.getVlanTags(interPath);
                    if (!vlanTags.isEmpty()) {
                        vlanTag = vlanTags.get(0);
                    }
                }
                // may not have interdomain path, or may be older reservation
                // with incomplete info for interdomain path
                if (vlanTag.equals("")) {
                    vlanTags = BssUtils.getVlanTags(path);
                    if (!vlanTags.isEmpty()) {
                        vlanTag = vlanTags.get(0);
                    }
                }
            } catch (BSSException ex) {
                outputMap.put("error", ex.getMessage());
            }
            if (!vlanTag.equals("")) {
                // if not a range
                if (!vlanTag.contains("-") && (!"any".equals(vlanTag))) {
                    String vlanStr = "";
                    try{
                        vlanStr = Math.abs(Integer.parseInt(vlanTag)) +"";
                    }catch(Exception e){}
                    resvMap.put("vlan", vlanStr);
                } else {
                    resvMap.put("vlan", "");
                }
            } else {
                resvMap.put("vlan", "");
            }
            resvMap.put("endTime", resv.getEndTime().toString());
            if (layer2Data != null) {
                resvMap.put("destination",
                            URNParser.abbreviate(layer2Data.getDestEndpoint()));
            } else if (layer3Data != null) {
                destination = layer3Data.getDestHost();
                try {
                    inetAddress = InetAddress.getByName(destination);
                    hostName = inetAddress.getHostName();
                    resvMap.put("destination", hostName);
                } catch (UnknownHostException e) {
                    resvMap.put("destination", destination);
                }
            }
            if (localDest != null) {
                if (layer2Data != null) {
                resvMap.put("localDestination", URNParser.abbreviate(localDest));
                } else {
                    resvMap.put("localDestination", localDest);
                }
            } else {
                resvMap.put("localDestination", "");
            }
            resvList.add(resvMap);
            ctr++;
        }
        outputMap.put("resvData", resvList);
    }
}
