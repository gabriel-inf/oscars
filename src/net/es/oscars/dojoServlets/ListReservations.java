package net.es.oscars.dojoServlets;

import java.io.*;
import java.util.*;
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
import net.es.oscars.aaa.UserManager;
import net.es.oscars.aaa.User;
import net.es.oscars.aaa.AAAException;
import net.es.oscars.aaa.UserManager.AuthValue;
import net.es.oscars.bss.topology.*;

public class ListReservations extends HttpServlet {
    private String dbname;

    public void
        doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

	      this.dbname = "bss";
        List<Reservation> reservations = null;
        UserSession userSession = new UserSession();
        Utils utils = new Utils();

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
     *  GetReservations returns either all reservations or just the reservations
     *  for this user, depending on his or her permissions.
     *  
     * @param out PrintWriter used to report errors on the status line
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
        Utils utils = new Utils();

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
            reservations = rm.list(login, logins, statuses, description,
                                   inLinks, startTimeSeconds, endTimeSeconds);
        } catch (BSSException e) {
            utils.handleFailure(out, e.getMessage(),  null, null);
            return null;
        }
        return reservations;
    }

    public void
        outputReservations(Map outputMap, List<Reservation> reservations,
                           HttpServletRequest request) {

        InetAddress inetAddress = null;
        String gri = "";
        String source = null;
        String hostName = null;
        String destination = null;

        List<String> statuses = this.getStatuses(request);
        ArrayList resvList = new ArrayList();
        for (Reservation resv: reservations) {
            ArrayList resvEntry = new ArrayList();
            gri = resv.getGlobalReservationId();
            Layer3Data layer3Data = resv.getPath().getLayer3Data();
            Layer2Data layer2Data = resv.getPath().getLayer2Data();
            resvEntry.add(gri);
            resvEntry.add(resv.getLogin());
            // TODO:  need to do time conversions on the server instead of
            // the client for this
            resvEntry.add(resv.getStartTime().toString());
            resvEntry.add(resv.getEndTime().toString());
            resvEntry.add(resv.getStatus());
            if (layer2Data != null) {
                resvEntry.add(layer2Data.getSrcEndpoint());
                resvEntry.add(layer2Data.getDestEndpoint());
            } else if (layer3Data != null) {
                source = layer3Data.getSrcHost();
                try {
                    inetAddress = InetAddress.getByName(source);
                    hostName = inetAddress.getHostName();
                    resvEntry.add(hostName);
                } catch (UnknownHostException e) {
                    resvEntry.add(source);
                }
                destination = layer3Data.getDestHost();
                try {
                    inetAddress = InetAddress.getByName(destination);
                    hostName = inetAddress.getHostName();
                    resvEntry.add(hostName);
                } catch (UnknownHostException e) {
                    resvEntry.add(destination);
                }
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

    public String getDescription(HttpServletRequest request) {

        String description = request.getParameter("resvDescription"); 
        if (description == null) {
            description = "";
        }
        return description;
    }

    public  List<String> getStatuses(HttpServletRequest request) {

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
}
