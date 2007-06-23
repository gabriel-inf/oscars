package net.es.oscars.servlets;

import java.util.Date;
import java.io.PrintWriter;

import net.es.oscars.oscars.TypeConverter;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.Utils;

public class ReservationDetails {

    public void
        contentSection(PrintWriter out, Reservation resv, String userName) {

        TypeConverter tc = new TypeConverter();
        Long longParam = null;
        Integer intParam = null;
        String strParam = null;
        Long ms = null;

        String tag = tc.getReservationTag(resv);
        out.println("<content>");
        out.println("<p><strong>Reservation Details</strong></p>");
        out.println("<p>To return to the reservations list, click on the ");
        out.println("Reservations tab.</p><p>");

        String status = resv.getStatus();
        if ((status != null) &&
                (status.equals("PENDING") || status.equals("ACTIVE"))) {
            String cancelSubmitStr = "return submitForm(this, 'CancelReservation');";
            out.println("<form method='post' action='' " +
                        "onsubmit=\"" + cancelSubmitStr + "\">");
            out.println("<input type='hidden' class='SOAP' name='tag' " +
                        "value='" + tag + "'></input>");
            out.println("<input type='submit' value='CANCEL'></input>");
            out.println("</form>");
        }

        String refreshSubmitStr = "return submitForm(this, 'QueryReservation');";
        out.println("<form method='post' action='' " +
                    "onsubmit=\"" + refreshSubmitStr + "\">");
        out.println("<input type='hidden' class='SOAP' name='tag' " +
                    "value='" + tag + "'></input>");
        out.println("<input type='submit' value='Refresh'>");
        out.println("</input>");
        out.println("</form>");
        out.println("</p>");
        out.println("<table width='90%' class='sortable'>");
        out.println("<thead><tr><td>Attribute</td><td>Value</td></tr></thead>");
        out.println("<tbody>");
        out.println("<tr><td>Tag</td><td>" + tag +
                    "</td></tr>");
        out.println("<tr><td>User</td><td>" + resv.getLogin() +
                    "</td></tr>");
        String sanitized = resv.getDescription().replace("<", "");
        String sanitized2 = sanitized.replace(">", "");
        out.println("<tr><td>Description</td><td>" +
                    sanitized2 + "</td></tr>");

        out.println("<tr><td>Start time</td><td class='dt'>");
        ms = resv.getStartTime();
        if (ms != null) {
            out.println(ms + "</td></tr>");
        } else { out.println("</td></tr>"); }
        out.println("<tr><td>End time</td><td class='dt'>");
        ms = resv.getEndTime();
        if (ms != null) {
            out.println(ms + "</td></tr>");
        } else { out.println("</td></tr>"); }
        out.println("<tr><td>Created time</td><td class='dt'>");
        ms = resv.getCreatedTime();
        if (ms != null) {
            out.println(ms + "</td></tr>");
        } else { out.println("</td></tr>"); }

        out.println("<tr><td>Bandwidth</td><td>" +
                    resv.getBandwidth() + "</td></tr>");
        longParam = resv.getBurstLimit();
        if (longParam != null) {
            out.println("<tr><td>Burst limit</td><td>" +
                        longParam + "</td></tr>");
        }
        out.println("<tr><td>Status</td><td>" +
                    resv.getStatus() + "</td></tr>");
        out.println("<tr><td>Source</td><td>" +
                    resv.getSrcHost() + "</td></tr>");
        out.println("<tr><td>Destination</td><td>" +
                    resv.getDestHost() + "</td></tr>");
        intParam = resv.getSrcIpPort();
        if (intParam != null) {
            out.println("<tr><td>Source port</td><td>" +
                        intParam + "</td></tr>");
        }
        intParam = resv.getDestIpPort();
        if (intParam != null) {
            out.println("<tr><td>Destination port</td><td>" +
                        intParam + "</td></tr>");
        }
        strParam = resv.getProtocol();
        if (strParam != null) {
            out.println("<tr><td>Protocol</td><td>" +
                        strParam + "</td></tr>");
        }
        strParam = resv.getDscp();
        if (strParam !=  null) {
            out.println("<tr><td>DSCP</td><td>" +
                        strParam + "</td></tr>");
        }
        if (resv.getLspClass() != null) {
            out.println("<tr><td>Class</td><td>" +
                    resv.getLspClass() + "</td></tr>");
        }
        Utils utils = new Utils("bss");  // FIX
        String path = utils.pathToString(resv.getPath());
        if (path != null) {
            out.println("<tr><td>Nodes in path</td><td>" +
                        path + "</td></tr>");
        }

        out.println("</tbody></table>");
        out.println("</content>");
    }
}
