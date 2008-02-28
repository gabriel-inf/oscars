package net.es.oscars.servlets;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.hibernate.*;
import net.es.oscars.aaa.AAAException;

import net.es.oscars.database.HibernateUtil;
import net.es.oscars.aaa.UserManager;
import net.es.oscars.aaa.UserManager.AuthValue;

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
        UserManager mgr = new UserManager("aaa");
        Session aaa = 
            HibernateUtil.getSessionFactory("aaa").getCurrentSession();
        aaa.beginTransaction();
        AuthValue authVal = mgr.checkModResAccess(userName,
                "Reservations", "create", 0, 0, false, false );
        if (authVal == AuthValue.DENIED ) {
            utils.handleFailure(out, "No permission granted to create a reservation" , aaa, null);
            return;
        }
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

        out.println("<p>Required inputs are bordered in green.  Ranges or " +
            "types of valid entries are given in parentheses after the " +
            "default values, if any.  If date and time fields are left " +
            "blank, they are filled in with the defaults.  The " +
            "time zone is your local time zone.</p>");

        //if (returnParams.get("loopbacksAllowed") != null) {
        // check to see if user may specify path elements
        AuthValue authVal = mgr.checkModResAccess(userName,
                "Reservations", "create", 0, 0, true, false );
        if  (authVal != AuthValue.DENIED ) {
            out.println("<p><strong>WARNING</strong>:  Entering a value in " +
            "a red-outlined field may change default routing behavior for " +
            "the selected flow.</p>");
        }
        out.println("<table>");
        out.println("<tbody>");
        out.println("<tr>");
        out.println("<td><input type='submit' value='Reserve bandwidth'></input></td>");
        out.println("<td><input class='SOAP' type='radio' name='production' value='production'>");
        out.println("Production circuit</input></td>");
        out.println("<td><input type='reset' value='Reset form fields'></input></td>");
        out.println("</tr>");
        out.println("<tr><td>Source</td>");
        out.println("<td class='required'>");
        out.println("<input type='text' class='SOAP' name='source' " +
                    "size='48'></input></td>");
        out.println("<td>(Host name or IP address)</td></tr>");
        out.println("<tr><td>Destination</td>");
        out.println("<td class='required'>");
        out.println("<input type='text' class='SOAP' name='destination' size='48'></input></td>");
        out.println("<td>(Host name or IP address)</td></tr>");
        out.println("<tr><td>Bandwidth (Mbps)</td>");
        out.println("<td class='required'>");
        out.println("<input type='text' class='SOAP' name='bandwidth' maxlength='7' size='48'>");
        out.println("</input>");
        out.println("</td>");
        out.println("<td>(10-5000)</td></tr>");
        out.println("<tr><td>Purpose of reservation</td>");
        out.println("<td class='required'>");
        out.println("<input type='text' class='SOAP' name='description' size='48'></input></td>");
        out.println("<td>(For our records)</td></tr>");
        //if (returnParams.get("loopbacksAllowed") != null) {
        if (authVal != AuthValue.DENIED) {
            out.println("<tr>");
            out.println("<td>Path</td>");
            out.println("<td class='warning'>");
            out.println("<textarea class='SOAP' name='explicitPath' rows='6' cols='46'> </textarea>");
            out.println("</td>");
            out.println("<td>(series of hops)</td>");
            out.println("</tr>");
        }
        out.println("<tr><td>Day of year</td>");
        out.println("<td><input type='text' name='startDofY' size='48'></input></td>");
        out.println("<td id='odate'> </td></tr>");
        out.println("<tr><td>Time</td>");
        out.println("<td><input type='text' name='startHourMinute' size='48'></input></td>");
        out.println("<td id='otime'> </td></tr>");
        out.println("<tr><td>Duration (Hours)</td>");
        out.println("<td><input type='text' name='durationHour' maxlength='16' size='48'></input>");
        out.println("</td>");
        out.println("<td>0.01 to 8760 (1 year)</td></tr>");
   
        out.println("<tr><td colspan='3'>Layer 2 parameters</td></tr>");
        out.println("<tr><td>VLAN</td>");
        out.println("<td><input type='text' class='SOAP' name='vlanTag' " +
                    "size='48'>");
        out.println("</input>");
        out.println("</td>");
        out.println("<td>tag, or range, e.g. 3000-3100</td></tr>");
        out.println("<tr><td>Source Port</td>");
        out.println("<td><select name='tagSrcPort' class='SOAP'>");
        out.println("<option value='1'>Tagged</option>");
        out.println("<option value='0'>Untagged</option>");
        out.println("</select>");
        out.println("</td>");
        out.println("<td></td></tr>");
        out.println("<tr><td>Destination Port</td>");
        out.println("<td><select name='tagDestPort' class='SOAP'>");
        out.println("<option value='1'>Tagged</option>");
        out.println("<option value='0'>Untagged</option>");
        out.println("</select>");
        out.println("</td>");
        out.println("<td></td></tr>");

        out.println("<tr><td colspan='3'>Layer 3 parameters</td></tr>");
        out.println("<tr><td>Source port</td>");
        out.println("<td><input type='text' class='SOAP' name='srcPort' " +
                    "maxlength='5' size='48'>");
        out.println("</input>");
        out.println("</td>");
        out.println("<td>(1024-65535)</td></tr>");
        out.println("<tr><td>Destination port</td>");
        out.println("<td><input type='text' class='SOAP' name='destPort' maxlength='5' size='48'>");
        out.println("</input></td>");
        out.println("<td>(1024-65535)</td></tr>");
        out.println("<tr><td>Protocol</td>");
        out.println("<td><input type='text' class='SOAP' name='protocol' size='48'></input></td>");
        out.println("<td>(0-255, or string)</td></tr>");
        out.println("<tr><td>Differentiated service code point</td>");
        out.println("<td><input type='text' class='SOAP' name='dscp' maxlength='2' size='48'>");
        out.println("</input></td>");
        out.println("<td>(0-63)</td></tr>");
        out.println("</tbody></table></form>");
        out.println("</content>");
    }
}
