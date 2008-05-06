package net.es.oscars.servlets;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;
import org.hibernate.*;
import net.sf.json.*;

import net.es.oscars.database.HibernateUtil;
import net.es.oscars.bss.ReservationManager;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.Utils;
import net.es.oscars.aaa.UserManager;
import net.es.oscars.aaa.User;
import net.es.oscars.aaa.AAAException;
import net.es.oscars.aaa.UserManager.AuthValue;
import net.es.oscars.bss.topology.*;

public class ListReservations extends HttpServlet {
    private String dbname;

    /**
     * Handles servlet request (both get and post) from list reservations form.
     * 
     * @param request servlet request
     * @param response servlet response
     */
    public void
        doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

	      this.dbname = "bss";
        List<Reservation> reservations = null;
        UserSession userSession = new UserSession();
        net.es.oscars.servlets.Utils utils = new net.es.oscars.servlets.Utils();

        PrintWriter out = response.getWriter();
        response.setContentType("text/json-comment-filtered");
        String userName = userSession.checkSession(out, request);
        if (userName == null) { return; }
        Session aaa = 
            HibernateUtil.getSessionFactory("aaa").getCurrentSession();
        aaa.beginTransaction();
        Session bss = 
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        bss.beginTransaction();
        Map outputMap = new HashMap();
        reservations = this.getReservations(out, request, userName);
        this.outputReservations(outputMap, reservations, request);
        outputMap.put("status", "Reservations list");
        outputMap.put("method", "ListReservations");
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("/* " + jsonObject + " */");
        aaa.getTransaction().commit();
        bss.getTransaction().commit();
    }

    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }

    /**
     *  Gets search parameters from servlet request and calls the reservation
     *  manager to get the resulting reservations, if any. 
     *  
     * @param out PrintWriter used to report errors on the status line
     * @param request servlet request
     * @param login string with login name of user
     * @return list of Reservation instances
     */
    public List<Reservation>
        getReservations(PrintWriter out, HttpServletRequest request,
                        String login)  {

        Long startTimeSeconds = null;
        Long endTimeSeconds = null;

        ReservationManager rm = new ReservationManager("bss");
        List<Reservation> reservations = null;
        List<String> logins = null;
        List<String> statuses = this.getStatuses(request);
        List<String> vlans = this.getVlanTags(request);
        String description = this.getDescription(request);
        List<Link> inLinks = this.getLinks(request);
        String startTimeStr = request.getParameter("startTimeSeconds");
        String endTimeStr = request.getParameter("endTimeSeconds");
        if ((startTimeStr != null) && !startTimeStr.equals("")) {
            startTimeSeconds = Long.valueOf(startTimeStr.trim());
        }
        if ((endTimeStr != null) && !endTimeStr.equals("")) {
            endTimeSeconds = Long.valueOf(endTimeStr.trim());
        }
        boolean allUsers = false;
        net.es.oscars.servlets.Utils utils = new net.es.oscars.servlets.Utils();

        UserManager mgr = new UserManager("aaa");
        List<User> users = mgr.list();
        AuthValue authVal = mgr.checkAccess(login, "Reservations", "list");
        if (authVal == AuthValue.DENIED) {
            utils.handleFailure(out, "no permission to list Reservations",  null, null);
            return null;
        }
        if (authVal == AuthValue.ALLUSERS) { allUsers=true; }
        // if logins are null, reservations from all users are returned
        if (!allUsers) {
            logins = new ArrayList<String>();
            logins.add(login);
        }
        try {
            reservations =
                rm.list(login, logins, statuses, description, inLinks,
                        vlans, startTimeSeconds, endTimeSeconds);
        } catch (BSSException e) {
            utils.handleFailure(out, e.getMessage(),  null, null);
            return null;
        }
        return reservations;
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
        outputReservations(Map outputMap, List<Reservation> reservations,
                           HttpServletRequest request) {

        InetAddress inetAddress = null;
        String gri = "";
        String source = null;
        String hostName = null;
        String destination = null;

        // TODO:  fix hard-wired database name
        net.es.oscars.bss.Utils utils = new net.es.oscars.bss.Utils("bss");
        List<String> statuses = this.getStatuses(request);
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
            resvEntry.add(vlanTag);
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
    
    public String getLinkIds(HttpServletRequest request) {

        String linkList = request.getParameter("linkIds");
        if (linkList == null) {
            // space needed for text area
            linkList = " ";
        }
        return linkList;
    }

    /**
     * Gets list of links to search for.  If a reservation's path includes
     * one of these links, it is returned as part of the list.
     *
     * @param request servlet request
     * @
     * @return list of links to send to BSS
     */
    public List<Link> getLinks(HttpServletRequest request) {

        Logger log = Logger.getLogger(this.getClass());
        List<Link> inLinks = new ArrayList<Link>();
        String linkList = this.getLinkIds(request).trim();
        if (linkList.equals("")) {
            return inLinks;
        }
        String[] linkIds = linkList.trim().split(" ");
        if (linkIds.length > 0) {
            for (String s : linkIds) {
                if (s != null && !s.trim().equals("")) {
                    Link link = null;
                    try {
                        link = TopologyUtil.getLink(s.trim(), this.dbname);
                        inLinks.add(link);
                    } catch (BSSException ex) {
	        			        log.error("Could not get link for string: ["+s.trim()+"], error: ["+ex.getMessage()+"]");
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
     * @
     * @return string with description
     */
    public String getDescription(HttpServletRequest request) {

        String description = request.getParameter("resvDescription"); 
        if (description == null) {
            description = "";
        }
        return description;
    }

    /**
     * Gets reservation statuses to search for.
     *
     * @param request servlet request
     * @
     * @return list of statuses to send to BSS
     */
    public List<String> getStatuses(HttpServletRequest request) {

        List<String> statuses = new ArrayList<String>();
        String paramStatuses[] = request.getParameterValues("statuses");
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
     * @
     * @return list of vlans and/or ranges to send to BSS
     */
    public List<String> getVlanTags(HttpServletRequest request) {

        List<String> vlanTags = new ArrayList<String>();
        String vlanParam = request.getParameter("vlanSearch").trim();
        if ((vlanParam != null) && !vlanParam.equals("")) {
            String[] paramTags = vlanParam.split(" ");
            for (int i=0; i < paramTags.length; i++) {
                vlanTags.add(paramTags[i]);
            }
        }
        return vlanTags;
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
