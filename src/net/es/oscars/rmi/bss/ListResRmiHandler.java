package net.es.oscars.rmi.bss;

/**
 * Interface between rmi listReservations call and reservationManager.listReservations
 *
 * @author Mary Thompson, David Robertson
 */

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.*;
import org.hibernate.*;
import net.es.oscars.aaa.*;
import net.es.oscars.bss.*;
import net.es.oscars.bss.topology.*;
import net.es.oscars.oscars.*;
import net.es.oscars.rmi.RmiUtils;
import net.es.oscars.rmi.aaa.AaaRmiInterface;

public class ListResRmiHandler {
    private OSCARSCore core;
    private Logger log;

    public ListResRmiHandler() {
        this.log = Logger.getLogger(this.getClass());
        this.core = OSCARSCore.getInstance();
    }

    public HashMap<String, Object>
           listReservations(HashMap<String, Object> params, String userName)
               throws IOException {

        this.log.debug("listReservations.start");
        String caller = (String) params.get("caller");
        if (caller.equals("WBUI")) {
            HashMap<String, Object> result = this.handleWBUI(params, userName);
            this.log.debug("listReservations.end");
            return result;
        } else if (caller.equals("AAR")) {
            HashMap<String, Object> result = this.handleAAR(params, userName);
            this.log.debug("listReservations.end");
            return result;
        } else {
            throw new IOException("Invalid caller!");
        }
    }

    public HashMap<String, Object>
        handleAAR(HashMap<String, Object> params, String userName)
            throws IOException {

        // FIXME: implement this part

        HashMap<String, Object> result = new HashMap<String, Object>();
        return result;
    }

    public HashMap<String, Object>
        handleWBUI(HashMap<String, Object> params, String userName)
            throws IOException {

        HashMap<String, Object> result = new HashMap<String, Object>();
        String institution = null;
        String loginConstraint = null;
        String methodName = "ListReservations";
        ReservationManager rm = core.getReservationManager();

        List<String> inLinks = null;
        List<Reservation> reservations = null;
        int numRowsReq =0;
        Long startTimeSeconds = null;
        Long endTimeSeconds = null;

        List<String> statuses = this.getStatuses(params);
        List<String> vlans = this.getVlanTags(params);
        String description = this.getSingleValue(params, "resvDescription");
        String startTimeStr = this.getSingleValue(params, "startTimeSeconds");
        String endTimeStr = this.getSingleValue(params, "endTimeSeconds");
        String numRowParam = this.getSingleValue(params, "numRows");
        String loginEntered = this.getSingleValue(params, "resvLogin");

        if ( !startTimeStr.equals("")) {
            startTimeSeconds = Long.valueOf(startTimeStr.trim());
        }
        if ( !endTimeStr.equals("")) {
            endTimeSeconds = Long.valueOf(endTimeStr.trim());
        }

        if (!numRowParam.equals("") && !numRowParam.equals("all")) {
            numRowsReq = Integer.parseInt(numRowParam);
        } else {
            numRowsReq = 0;  // special case to get all results
        }
        if (!loginEntered.equals("")) {
            loginConstraint = loginEntered;
        }
        AaaRmiInterface rmiClient = RmiUtils.getAaaRmiClient(methodName, log);
        AuthValue authVal = rmiClient.checkAccess(userName, "Reservations", "list");
        if (authVal == AuthValue.DENIED) {
            result.put("error", "no permission to list Reservations");
            this.log.debug("query failed: no permission to list Reservations");
            return result;
        }
        // only display user search field if can look at other users'
        // reservations
        if (authVal.equals(AuthValue.MYSITE)) {
            result.put("resvLoginDisplay", Boolean.TRUE);
            institution = rmiClient.getInstitution(userName);
        } else if (authVal.equals(AuthValue.SELFONLY)){
            result.put("resvLoginDisplay", Boolean.FALSE);
            loginConstraint = userName;
        } else {
            result.put("resvLoginDisplay", Boolean.TRUE);
        }

        Session bss = core.getBssSession();
        bss.beginTransaction();
        String errMessage = null;
        try {
            inLinks = this.getLinks(params);
            reservations =
                rm.list(numRowsReq, 0, loginConstraint, institution, statuses,
                        description, inLinks, vlans,
                        startTimeSeconds, endTimeSeconds);
        } catch (BSSException e) {
            errMessage = e.getMessage();
        } catch (Exception e) {
            // use this so we can find NullExceptions
            errMessage = e.getMessage();
        } finally {
            if (errMessage != null) {
                result.put("error",  errMessage);
                bss.getTransaction().rollback();
                this.log.debug("list failed: " + errMessage);
                return result;
            }
        }
        outputReservations(result, reservations);
        result.put("totalRowsReplace", "Total rows: " + reservations.size());
        result.put("status", "list reservations successful ");        this.log.debug("listReservations.end");
        result.put("method", methodName);
        result.put("success", Boolean.TRUE);
        bss.getTransaction().commit();
        this.log.debug("listReservations.end");
        return result;
    }

    /**
     * Gets list of links to search for.  If a reservation's path includes
     * one of these links, it is returned as part of the list.
     *
     * @param request servlet request
     * @return list of links to send to BSS
     * @throws BSSException
     */
    public List<String> getLinks(HashMap<String, Object> request) {

        List<String> inLinks = new ArrayList<String>();
        String linkList;
        String linkParam [] = (String[]) request.get("linkIds");
        if (linkParam != null) {
            linkList = linkParam[0].trim();
        } else {
            return inLinks;
        }
        String[] linkIds = linkList.split(" ");
        if (linkIds.length > 0) {
            for (String s : linkIds) {
                if (s != null && !s.trim().equals("")) {
                    inLinks.add(s);
                }
            }
        }
        return inLinks;
    }

    public String getSingleValue(HashMap<String, Object> params, String paramName) {
        String paramArgs[] = (String[]) params.get(paramName);
        if (paramArgs == null) {
            return null;
        } else {
            return (String) paramArgs[0];
        }

    }

    /**
     * Gets reservation statuses to search for.
     *
     * @param request HashMap passed from servlet
     * @return list of statuses to send to BSS
     */
    public List<String> getStatuses(HashMap<String, Object> request) {

        List<String> statuses = new ArrayList<String>();
        String paramStatuses[] = (String[]) request.get("statuses");
        if (paramStatuses == null) {
            statuses.add("");
        } else {
            for (int i=0 ; i < paramStatuses.length; i++) {
                statuses.add(paramStatuses[i]);
            }
        }
        return statuses;
    }

    /**
     * Gets VLAN tags and/or ranges of VLAN tags to search for from
     * servlet request.
     *
     * @param request servlet request
     * @return list of vlans and/or ranges to send to BSS
     */
    public List<String> getVlanTags(HashMap<String, Object> request) {

        List<String> vlanTags = new ArrayList<String>();
        String vlanParam[] = (String[]) request.get("vlanSearch");
        if ((vlanParam != null) && !vlanParam[0].equals("")) {
            String[] paramTags = vlanParam[0].trim().split(" ");
            for (int i=0; i < paramTags.length; i++) {
                vlanTags.add(paramTags[i]);
            }
        }
        return vlanTags;
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

        ArrayList<HashMap<String,Object>> resvList =
            new ArrayList<HashMap<String,Object>>();
        int ctr = 0;
        for (Reservation resv: reservations) {
            resv.initializePaths();
            // INTERDOMAIN
            Path path = null;
            try {
                path = resv.getPath(PathType.INTERDOMAIN);
            } catch (BSSException ex) {
                outputMap.put("error", ex.getMessage());
                return;
            }
            String pathStr = BssUtils.pathToString(path, false);
            String localSrc = null;
            String localDest = null;
            if (pathStr != null) {
                String[] hops = pathStr.trim().split("\n");
                localSrc = hops[0];
                localDest = hops[hops.length-1];
            }
            HashMap<String,Object> resvMap = new HashMap<String,Object>();
            gri = resv.getGlobalReservationId();
            Layer3Data layer3Data = null;
            Layer2Data layer2Data = null;
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
            String vlanTag = null;
            if ( path != null) {
                try {
                    vlanTag = BssUtils.getVlanTag(path);
                } catch (BSSException ex) {
                    outputMap.put("error", ex.getMessage());
                }
            }
            if (vlanTag != null) {
                int vlanNum = Math.abs(Integer.parseInt(vlanTag));
                resvMap.put("vlan", vlanNum + "");
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
