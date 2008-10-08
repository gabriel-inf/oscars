package net.es.oscars.rmi;

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
import net.es.oscars.aaa.UserManager.*;
import net.es.oscars.bss.*;
import net.es.oscars.bss.topology.Layer2Data;
import net.es.oscars.bss.topology.Layer3Data;
import net.es.oscars.bss.topology.Link;
import net.es.oscars.bss.topology.Path;
import net.es.oscars.bss.topology.TopologyUtil;
import net.es.oscars.oscars.*;

public class ListResRmiHandler {
    private OSCARSCore core;
    private Logger log;

    public ListResRmiHandler() {
        this.log = Logger.getLogger(this.getClass());
        this.core = OSCARSCore.getInstance();
    }

    public HashMap<String, Object>
           listReservations(HashMap<String, String[]> inputMap, String userName)
               throws IOException {

        this.log.debug("listReservations.start");
        HashMap<String, Object> result = new HashMap<String, Object>();
        String institution = null;
        String loginConstraint = null;
        String methodName = "ListReservations";
        ReservationManager rm = core.getReservationManager();

        int numRowsReq =0;
        Long startTimeSeconds = null;
        Long endTimeSeconds = null;
        List<String> statuses = this.getStatuses(inputMap);
        List<String> vlans = this.getVlanTags(inputMap);
        String description = this.getDescription(inputMap);
        List<Link> inLinks = null;
        
        List<Reservation> reservations = null;
        if (inputMap.get("startTimeSeconds") != null) {
            String startTimeStr = inputMap.get("startTimeSeconds")[0];
            if ( !startTimeStr.equals("")) {
                startTimeSeconds = Long.valueOf(startTimeStr.trim());
            }
        }
        if (inputMap.get("endTimeSeconds") != null) {
            String endTimeStr = inputMap.get("endTimeSeconds")[0];
            if ( !endTimeStr.equals("")) {
                endTimeSeconds = Long.valueOf(endTimeStr.trim());
            }
        }
        String numRowsParam[] = inputMap.get("numRows");
        if (numRowsParam != null) {
            String numRowParam =numRowsParam[0].trim();
            if (!numRowParam.equals("") && !numRowParam.equals("all")) {
                numRowsReq = Integer.parseInt(numRowParam);
            } else {
                numRowsReq = 0;  // special case to get all results
            }
        }
        // won't be set unless user search input enabled
        if (inputMap.get("resvLogin") != null) {
            String loginEntered = inputMap.get("resvLogin")[0];
            if (!loginEntered.equals("")) {
                loginConstraint = loginEntered;
            }
        }
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();
        UserManager userMgr = core.getUserManager();

        AuthValue authVal = userMgr.checkAccess(userName, "Reservations",
                "list");

        if (authVal == AuthValue.DENIED) {
            result.put("error", "no permission to list Reservations");
            this.log.debug("query failed: no permission to list Reservations");
            return result;
        }
        // only display user search field if can look at other users'
        // reservations
        if (authVal.equals(AuthValue.MYSITE)) {
            result.put("resvLoginDisplay", Boolean.TRUE);
            institution = userMgr.getInstitution(userName);
        } else if (authVal.equals(AuthValue.SELFONLY)){
            result.put("resvLoginDisplay", Boolean.FALSE);
            loginConstraint = userName;
        } else {
            result.put("resvLoginDisplay", Boolean.TRUE);
        }
        aaa.getTransaction().commit();

        Session bss = core.getBssSession();
        bss.beginTransaction();
        String errMessage = null;
        try {
            inLinks = this.getLinks(inputMap);
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
        result.put("status", "list reservations successful ");
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
    public List<Link> getLinks(HashMap<String, String[]> request)
            throws BSSException {

        List<Link> inLinks = new ArrayList<Link>();
        String linkList;
        String linkParam [] = request.get("linkIds");
        if (linkParam != null) {
            linkList = linkParam[0].trim();
        } else {
            return inLinks;
        }
        String[] linkIds = linkList.split(" ");
        if (linkIds.length > 0) {
            for (String s : linkIds) {
                if (s != null && !s.trim().equals("")) {
                    Link link = null;
                    try {
                        link = TopologyUtil.getLink(s.trim(), core.getBssDbName());
                        inLinks.add(link);
                    } catch (BSSException ex) {
                        this.log.error("Could not get link for string: [" +
                                   s.trim()+"], error: ["+ex.getMessage()+"]");
                        throw new BSSException ("invalid link" + s.trim() );
                    }                   
                }
            }
        }
        return inLinks;
    }
 
    /**
     * Gets description search parameter and sets to blank field if empty.
     *
     * @param request servlet request
     * @return string with description
     */
    public String getDescription(HashMap<String, String[]> request) {

        String description = "";
        String descriptions [] = request.get("resvDescription"); 
        if (descriptions != null) {
            description = descriptions[0];
        }
        return description;
    }

    /**
     * Gets reservation statuses to search for.
     *
     * @param request HashMap passed from servlet 
     * @return list of statuses to send to BSS
     */
    public List<String> getStatuses(HashMap<String, String[]> request) {

        List<String> statuses = new ArrayList<String>();
        String paramStatuses[] = request.get("statuses");
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
    public List<String> getVlanTags(HashMap<String, String[]> request) {

        List<String> vlanTags = new ArrayList<String>();
        String vlanParam [] = request.get("vlanSearch");
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
     * @param request servlet request
     */
    public void
        outputReservations(Map outputMap, List<Reservation> reservations) {

        InetAddress inetAddress = null;
        String gri = "";
        String source = null;
        String hostName = null;
        String destination = null;
        
        net.es.oscars.bss.Utils utils = new net.es.oscars.bss.Utils(core.getBssDbName());
        ArrayList resvList = new ArrayList();

        for (Reservation resv: reservations) {
            Path path = resv.getPath();
            String pathStr = utils.pathToString(path, false);
            String localSrc = null;
            String localDest = null;
            if (pathStr != null) {
                String[] hops = pathStr.trim().split("\n");
                localSrc = hops[0];
                localDest = hops[hops.length-1];
            }
            ArrayList resvEntry = new ArrayList();
            gri = resv.getGlobalReservationId();
            Layer3Data layer3Data = path.getLayer3Data();
            Layer2Data layer2Data = path.getLayer2Data();
            resvEntry.add(gri);
            resvEntry.add(resv.getStatus());
            Long mbps = resv.getBandwidth()/1000000;
            String bandwidthField = mbps.toString() + "Mbps";
            resvEntry.add(bandwidthField);
            // entries are converted on the fly on the client to standard
            // date and time format before the model's data is set
            resvEntry.add(resv.getStartTime().toString());
            if (layer2Data != null) {
                resvEntry.add(this.abbreviate(layer2Data.getSrcEndpoint()));
            } else if (layer3Data != null) {
                source = layer3Data.getSrcHost();
                try {
                    inetAddress = InetAddress.getByName(source);
                    hostName = inetAddress.getHostName();
                    resvEntry.add(hostName);
                } catch (UnknownHostException e) {
                    resvEntry.add(source);
                }
            }
            if (localSrc != null) {
                if (layer2Data != null) {
                    resvEntry.add(this.abbreviate(localSrc));
                } else {
                    resvEntry.add(localSrc);
                }
            } else {
                resvEntry.add("");
            }
            // start of second sub-row
            resvEntry.add(resv.getLogin());
            String vlanTag = utils.getVlanTag(path);
            if (vlanTag != null) {
                int vlanNum = Math.abs(Integer.parseInt(vlanTag));
                resvEntry.add(vlanNum + "");
            } else {
                resvEntry.add("");
            }
            resvEntry.add(resv.getEndTime().toString());
            if (layer2Data != null) {
                resvEntry.add(this.abbreviate(layer2Data.getDestEndpoint()));
            } else if (layer3Data != null) {
                destination = layer3Data.getDestHost();
                try {
                    inetAddress = InetAddress.getByName(destination);
                    hostName = inetAddress.getHostName();
                    resvEntry.add(hostName);
                } catch (UnknownHostException e) {
                    resvEntry.add(destination);
                }
            }
            if (localDest != null) {
                if (layer2Data != null) {
                    resvEntry.add(this.abbreviate(localDest));
                } else {
                    resvEntry.add(localDest);
                }
            } else {
                resvEntry.add("");
            }
            resvList.add(resvEntry);
        }
        outputMap.put("resvData", resvList);
    }
    
    /**
     * Returns an abbreviated version of the full layer 2 topology identifier.
     * (adapted from bss.topology.URNParser)
     *
     * @param topoId string with full topology identifier, for example
     * urn:ogf:network:domain=es.net:node=bnl-mr1:port=TenGigabitEthernet1/3:link=*
     * @return string abbreviation such as es.net:bnl-mr1:TenGigabitEthernet1/3
     */
    public String abbreviate(String topoIdent) {
        Pattern p = Pattern.compile(
            "^urn:ogf:network:domain=([^:]+):node=([^:]+):port=([^:]+):link=([^:]+)$");
        Matcher matcher = p.matcher(topoIdent);
        String domainId = "";
        String nodeId = "";
        String portId = "";
        // punt if not in expected format
        if (!matcher.matches()) {
            return topoIdent;
        } else {
            domainId = matcher.group(1);
            nodeId = matcher.group(2);
            portId = matcher.group(3);
            return domainId + ":" + nodeId + ":" + portId;
        }
    }
}
