package net.es.oscars.servlets;

import java.io.*;
import java.util.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.*;
import org.hibernate.*;

import net.es.oscars.database.HibernateUtil;
import net.es.oscars.bss.ReservationManager;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.BSSException;
import net.es.oscars.aaa.UserManager;
import net.es.oscars.aaa.User;
import net.es.oscars.aaa.AAAException;
import net.es.oscars.aaa.UserManager.AuthValue;
import net.es.oscars.bss.topology.Layer2Data;
import net.es.oscars.bss.topology.Layer3Data;

public class ListReservations extends HttpServlet {

    public void
        doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        List<Reservation> reservations = null;

        UserSession userSession = new UserSession();
        Utils utils = new Utils();

        PrintWriter out = response.getWriter();
        response.setContentType("text/xml");
        String userName = userSession.checkSession(out, request);
        if (userName == null) { return; }
        Session aaa = 
            HibernateUtil.getSessionFactory("aaa").getCurrentSession();
        aaa.beginTransaction();
        Session bss = 
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        bss.beginTransaction();
        reservations = this.getReservations(out, request, userName);
        if (reservations == null) {
            /* the status has already been reported in getReservations
            String msg = "Error in getting reservations";
            */
            aaa.getTransaction().rollback();
            bss.getTransaction().rollback();
            return;
        }
        out.println("<xml>");
        out.println("<status>Successfully retrieved reservations</status>");
        utils.tabSection(out, request, response, "ListReservations");
        this.contentSection(out, request, response, reservations, userName);
        out.println("</xml>");
        aaa.getTransaction().commit();
        bss.getTransaction().commit();
    }

    public void
        doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }

    /**
     *  GetReservations returns either all reservations or just the reservations
     *  for this user, depending on his permissions
     *  called by this servlet and AuthenticateUser which brings up  a default
     *  page of reservations listing when a user logs in.
     *  
     * @param out PrintWriter used to report errors on the status line
     * @param login string with login name of user
     * @return list of Reservation instances
     */
    public List<Reservation>
        getReservations(PrintWriter out, HttpServletRequest request,
                        String login)  {

        ReservationManager rm = new ReservationManager("bss");
        UserSession userSession = new UserSession();
        List<Reservation> reservations = null;
        List<String> logins = null;
        List<String> statuses = this.getStatuses(request, userSession);
        String description = this.getDescription(request, userSession);
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
        if (!allUsers) {
            logins = new ArrayList<String>();
            logins.add(login);
        }
        try {
            // TODO:  other criteria
            reservations = rm.list(login, logins, statuses, description, null, null, null);
        } catch (BSSException e) {
            utils.handleFailure(out, e.getMessage(),  null, null);
            return null;
        }
        return reservations;
    }

    public void
        contentSection(PrintWriter out, HttpServletRequest request,
                       HttpServletResponse response,
                       List<Reservation> reservations, String login) {

        InetAddress inetAddress = null;
        String gri = "";
        String source = null;
        String hostName = null;
        String destination = null;

        UserSession userSession = new UserSession();
        out.println("<content>");
        out.println("<p>Click on a column header to sort by that column. " +
            "Times given are in the time zone of the browser.  Click on " +
            "the Reservation GRI link to view detailed information about " +
            "the reservation.</p>");
        out.println("<p><form method='post' action='' onsubmit=\"" +
            "return submitForm(this, 'ListReservations');\">");
        out.println("<table cellspacing='0' width='30%'>");
        out.println("<tbody>");
        out.println("<tr>");
        out.println("<td><input type='submit' value='Refresh'></input></td>");
        List<String> statuses = this.getStatuses(request, userSession);
        this.outputStatusMenu(out, statuses);

        String description = this.getDescription(request, userSession); 
        
        out.println("<td>Description: <input type='text' name='description' value='"+description+"'/></td>");
        out.println("</tr>");
        out.println("</tbody>");
        out.println("</table>");
        out.println("</form></p>");

        out.println("<table cellspacing='0' width='90%' class='sortable'>");
        out.println("<thead>");
        out.println("<tr><td>GRI</td><td>User</td><td>Start Time</td><td>End Time</td><td>Status</td>");
        out.println("<td>Origin</td><td>Destination</td>");
        out.println("</tr></thead> <tbody>");
        for (Reservation resv: reservations) {
            gri = resv.getGlobalReservationId();
            Layer3Data layer3Data = resv.getPath().getLayer3Data();
            Layer2Data layer2Data = resv.getPath().getLayer2Data();
            out.println("<tr>");
            out.println("<td>");
            out.println("<a href='#' " +
                "onclick=\"return newSection('QueryReservation', 'gri=" +
                         gri + "');\">" +
                gri + "</a>");
            out.println("</td>");
            out.println("<td>" + resv.getLogin() + "</td>");
            out.println("<td name='startTime' class='dt'>" +
                         resv.getStartTime() + "</td>");
            out.println("<td name='endTime' class='dt'>" +
                         resv.getEndTime() + "</td>");
            out.println("<td>" + resv.getStatus() + "</td>");
            if (layer2Data != null) {
                source = layer2Data.getSrcEndpoint();
                out.println("<td>" + source + "</td>");
                destination = layer2Data.getDestEndpoint();
                out.println("<td>" + destination + "</td>");
            } else if (layer3Data != null) {
                source = layer3Data.getSrcHost();
                try {
                    inetAddress = InetAddress.getByName(source);
                    hostName = inetAddress.getHostName();
                    out.println("<td>" + hostName + "</td>");
                } catch (UnknownHostException e) {
                    out.println("<td>" + source + "</td>");
                }
                destination = layer3Data.getDestHost();
                try {
                    inetAddress = InetAddress.getByName(destination);
                    hostName = inetAddress.getHostName();
                    out.println("<td>" + hostName + "</td>");
                } catch (UnknownHostException e) {
                    out.println("<td>" + destination + "</td>");
                }
            }
            out.println("</tr>");
        }
        out.println("</tbody></table>");
        out.println("</content>");
        String cookieValue = "";
        for (String status: statuses) {
            cookieValue += status + " ";
        }
        cookieValue.trim();
        userSession.setCookie("statusList", cookieValue, response);
        
        userSession.setCookie("description", description, response);
    }
    
    public String getDescription(HttpServletRequest request, UserSession userSession) {
    	String description = null;
    	description = request.getParameter("description"); 
    	if (description == null) {
    		description = "";
    	}
    	return description;
    }

    public  List<String> getStatuses(HttpServletRequest request,
                                     UserSession userSession) {

        List<String> statuses = new ArrayList<String>();
      	String paramStatuses[] = request.getParameterValues("statuses");
        // if coming in from tab
        if ((paramStatuses == null) || (paramStatuses.length == 0)) {
            String statusList = userSession.getCookie("statusList", request);
            if (statusList != null) {
                String[] statusCookies = statusList.split(" ");
                for (int i=0; i < statusCookies.length; i++) {
                    statuses.add(statusCookies[i]);
                }
            // first time
            } else {
                statuses.add("ACTIVE");
                statuses.add("PENDING");
            }
        // else if refreshing with possibly new menu selections
        } else {
            for (int i=0 ; i < paramStatuses.length; i++) {
                statuses.add(paramStatuses[i]);
            }
        }
        return statuses;
    }
    

    public void
        outputStatusMenu(PrintWriter out, List<String> statuses) {

        List<String> fullStatuses = new ArrayList<String>();
        Map<String,String> selectedStatuses = new HashMap<String,String>();
        for (String status: statuses) {
            selectedStatuses.put(status, null);
        }
        fullStatuses.add("ACTIVE");
        fullStatuses.add("PENDING");
        fullStatuses.add("FINISHED");
        fullStatuses.add("CANCELLED");
        fullStatuses.add("FAILED");
        fullStatuses.add("INVALIDATED");
        out.println("<td>Statuses</td>");
        out.println("<td><select class='required' name='statuses' multiple='multiple'>");
        for (String fullStatus: fullStatuses) {
            out.println("<option value='" + fullStatus + "' ");
            if (selectedStatuses.containsKey(fullStatus)) {
                out.println("selected='selected'");
            }
            out.println(">" + fullStatus + "</option>" );
        }
        out.println("</select>");
        out.println("</td>");
    }
}
