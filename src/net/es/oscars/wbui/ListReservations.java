package net.es.oscars.wbui;

import java.io.*;
import java.util.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.servlet.*;
import javax.servlet.http.*;
import org.hibernate.*;

import net.es.oscars.database.HibernateUtil;
import net.es.oscars.bss.ReservationManager;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.BSSException;


public class ListReservations extends HttpServlet {

    public void
        doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        List<Reservation> reservations = null;

        ReservationManager rm = new ReservationManager();
        rm.setSession();
        UserSession userSession = new UserSession();
        Utils utils = new Utils();

        PrintWriter out = response.getWriter();
        response.setContentType("text/xml");
        String userName = userSession.checkSession(out, request);
        if (userName == null) { return; }
        Session bss = 
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        bss.beginTransaction();
        reservations = this.getReservations(out, rm);
        if (reservations == null) {
            String msg = "Error in getting reservations";
            utils.handleFailure(out, msg, null, bss);
            return;
        }
        out.println("<xml>");
        out.println("<status>Successfully retrieved reservations</status>");
        utils.tabSection(out, request, response, "ListReservations");
        this.contentSection(out, reservations, rm, userName);
        out.println("</xml>");
        bss.getTransaction().commit();
    }

    public void
        doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }

    public List<Reservation>
        getReservations(PrintWriter out, ReservationManager rm) {

        List<Reservation> reservations = null;
        try {
            reservations = rm.list(null);
        } catch (BSSException e) {
            out.println("<xml><status>");
            out.println(e.getMessage());
            out.println("</status></xml>");
            return null;
        }
        return reservations;
    }

    public void
        contentSection(PrintWriter out, List<Reservation> reservations,
                       ReservationManager rm, String userName) {

        InetAddress inetAddress = null;
        String tag = "";
        String srcHost = null;
        String hostName = null;
        String destHost = null;

        out.println("<content>");
        out.println("<p>Click on a column header to sort by that column. " +
            "Times given are in the time zone of the browser.  Click on " +
            "the Reservation Tag link to view detailed information about " +
            "the reservation.</p>");
        out.println("<p><form method='post' action='' onsubmit=\"" +
            "return submitForm(this, 'ListReservations');\">");
        out.println("<input type='submit' value='Refresh'></input>");
        out.println("</form></p>");

        out.println("<table cellspacing='0' width='90%' class='sortable'>");
        out.println("<thead>");
        out.println("<tr><td>Tag</td><td>Start Time</td><td>End Time</td><td>Status</td>");
        out.println("<td>Origin</td><td>Destination</td>");
        out.println("</tr></thead> <tbody>");
        for (Reservation resv: reservations) {
            tag = rm.toTag(resv);
            out.println("<tr>");
            out.println("<td>");
            out.println("<a href='#' " +
                "onclick=\"return newSection('QueryReservation', 'tag=" +
                         tag + "');\">" +
                tag + "</a>");
            out.println("</td>");
            out.println("<td name='startTime' class='dt'>" +
                         resv.getStartTime() + "</td>");
            out.println("<td name='endTime' class='dt'>" +
                         resv.getEndTime() + "</td>");
            out.println("<td>" + resv.getStatus() + "</td>");
            srcHost = resv.getSrcHost();
            try {
                inetAddress = InetAddress.getByName(srcHost);
                hostName = inetAddress.getHostName();
                out.println("<td>" + hostName + "</td>");
            } catch (UnknownHostException e) {
                out.println("<td>" + srcHost + "</td>");
            }
            destHost = resv.getDestHost();
            try {
                inetAddress = InetAddress.getByName(destHost);
                hostName = inetAddress.getHostName();
                out.println("<td>" + hostName + "</td>");
            } catch (UnknownHostException e) {
                out.println("<td>" + destHost + "</td>");
            }
            out.println("</tr>");
        }
        out.println("</tbody></table>");
        out.println("</content>");
    }
}
