package net.es.oscars.servlets;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.hibernate.*;

import net.es.oscars.database.HibernateUtil;
import net.es.oscars.aaa.UserManager;

public class CreateReservationForm extends HttpServlet {

    public void
        doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

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
        out.println("<xml>");
        out.println("<status>Reservation creation form</status>");
        utils.tabSection(out, request, response, "CreateReservationForm");
        this.contentSection(out, request, userName);
        out.println("</xml>");
        aaa.getTransaction().commit();
        bss.getTransaction().commit();
    }

    public void
        doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }

    public void
        contentSection(PrintWriter out, HttpServletRequest request,
                       String userName) {

        UserManager mgr = new UserManager("aaa");
        out.println("<content>");
        out.println("<form method='post' action='' onsubmit=\"" +
                    "return submitForm(this, 'CreateReservation');\">");
        out.println("<input type='hidden' class='SOAP' name='startTime'>" +
                    "</input>");
        out.println("<input type='hidden' class='SOAP' name='endTime'>" +
                    "</input>");
        out.println("<input type='submit' value='Reserve bandwidth'></input>");
        out.println("<input type='reset' value='Reset form fields'></input>");

        out.println("<p>Required inputs are bordered in green.  Ranges or " +
            "types of valid entries are given in parentheses after the " +
            "default values, if any.  If date and time fields are left " +
            "blank, they are filled in with the defaults.  The " +
            "time zone is your local time zone.</p>");

        //if (returnParams.get("loopbacksAllowed") != null) {
        if (mgr.verifyAuthorized(userName, "Reservations", "manage")) {
            out.println("<p><strong>WARNING</strong>:  Entering a value in " +
            "a red-outlined field may change default routing behavior for " +
            "the selected flow.</p>");
        }
        out.println("<table>");
        out.println("<tbody>");
        out.println("<tr><td>Source</td>");
        out.println("<td class='required'>");
        out.println("<input type='text' class='SOAP' name='srcHost' " +
                    "size='40'></input></td>");
        out.println("<td>(Host name or IP address)</td></tr>");
        out.println("<tr><td>Source port</td>");
        out.println("<td><input type='text' class='SOAP' name='srcPort' " +
                    "maxlength='5' size='40'>");
        out.println("</input>");
        out.println("</td>");
        out.println("<td>(1024-65535)</td></tr>");
        out.println("<tr><td>Destination</td>");
        out.println("<td class='required'>");
        out.println("<input type='text' class='SOAP' name='destHost' size='40'></input></td>");
        out.println("<td>(Host name or IP address)</td></tr>");
        out.println("<tr><td>Destination port</td>");
        out.println("<td><input type='text' class='SOAP' name='destPort' maxlength='5' size='40'>");
        out.println("</input></td>");
        out.println("<td>(1024-65535)</td></tr>");
        out.println("<tr><td>Bandwidth (Mbps)</td>");
        out.println("<td class='required'>");
        out.println("<input type='text' class='SOAP' name='bandwidth' maxlength='7' size='40'>");
        out.println("</input>");
        out.println("</td>");
        out.println("<td>(10-5000)</td></tr>");
        out.println("<tr><td>Protocol</td>");
        out.println("<td><input type='text' class='SOAP' name='protocol' size='40'></input></td>");
        out.println("<td>(0-255, or string)</td></tr>");
        out.println("<tr><td>Differentiated service code point</td>");
        out.println("<td><input type='text' class='SOAP' name='dscp' maxlength='2' size='40'>");
        out.println("</input></td>");
        out.println("<td>(0-63)</td></tr>");
        out.println("<tr><td>Purpose of reservation</td>");
        out.println("<td class='required'>");
        out.println("<input type='text' class='SOAP' name='description' size='40'></input></td>");
        out.println("<td>(For our records)</td></tr>");

        //if (returnParams.get("loopbacksAllowed") != null) {
        if (mgr.verifyAuthorized(userName, "Reservations", "manage")) {
            out.println("<tr>");
            out.println("<td>Ingress loopback</td>");
            out.println("<td class='warning'>");
            out.println("<input type='text' class='SOAP' name='ingressRouter' size='40'></input>");
            out.println("</td>");
            out.println("<td>(Host name or IP address)</td>");
            out.println("</tr>");
            out.println("<tr>");
            out.println("<td>Egress loopback</td>");
            out.println("<td class='warning'>");
            out.println("<input type='text' class='SOAP' name='egressRouter' size='40'></input>");
            out.println("</td>");
            out.println("<td>(Host name or IP address)</td>");
            out.println("</tr>");
        }
        //if (returnParams.get("persistentAllowed") != null) {
        if (mgr.verifyAuthorized(userName, "Reservations", "persistent")) {
            out.println("<tr><td>Persistent reservation</td>");
            out.println("<td><input type='checkbox' name='persistent' size='8' value='0'></input></td>");
            out.println("<td>Doesn't expire until explicitly cancelled.</td></tr>");
        }
        out.println("<tr><td>Year</td>");
        out.println("<td><input type='text' name='startYear' maxlength='4' size='40'></input></td>");
        out.println("<td id='oyear'> </td></tr>");
        out.println("<tr><td>Month</td>");
        out.println("<td><input type='text' name='startMonth' maxlength='2' size='40'></input></td>");
        out.println("<td id='omonth'> </td></tr>");
        out.println("<tr><td>Date</td>");
        out.println("<td><input type='text' name='startDate' maxlength='2' size='40'></input></td>");
        out.println("<td id='odate'> </td></tr>");

        out.println("<tr><td>Hour</td>");
        out.println("<td><input type='text' name='startHour' maxlength='2' size='40'></input></td>");
        out.println("<td id='ohour'> </td></tr>");
        out.println("<tr><td>Minute</td>");
        out.println("<td><input type='text' name='startMinute' maxlength='2' size='40'></input></td>");
        out.println("<td id='ominute'> </td></tr>");

        out.println("<tr><td>Duration (Hours)</td>");
        out.println("<td><input type='text' name='durationHour' maxlength='16' size='40'></input>");
        out.println("</td>");
        out.println("<td>0.01 (0.01 to 4 years)</td></tr>");
   
        out.println("</tbody></table></form>");
        out.println("</content>");
    }
}
