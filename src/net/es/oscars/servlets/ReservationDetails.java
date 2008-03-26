package net.es.oscars.servlets;

import java.util.Date;
import java.io.PrintWriter;

import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.Utils;
import net.es.oscars.bss.topology.*;

public class ReservationDetails {

    public void
        contentSection(PrintWriter out, Reservation resv, String userName) {

        Long longParam = null;
        Integer intParam = null;
        String strParam = null;
        Long ms = null;

        String gri = resv.getGlobalReservationId();
        Path path = resv.getPath();
        Layer2Data layer2Data = path.getLayer2Data();
        Layer3Data layer3Data = path.getLayer3Data();
        MPLSData mplsData = path.getMplsData();
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
            out.println("<input type='hidden' class='SOAP' name='gri' " +
                        "value='" + gri + "'></input>");
            out.println("<input type='submit' value='CANCEL'></input>");
            out.println("</form>");
        }

        String refreshSubmitStr = "return submitForm(this, 'QueryReservation');";
        out.println("<form method='post' action='' " +
                    "onsubmit=\"" + refreshSubmitStr + "\">");
        out.println("<input type='hidden' class='SOAP' name='gri' " +
                    "value='" + gri + "'></input>");
        out.println("<input type='submit' value='Refresh'>");
        out.println("</input>");
        out.println("</form>");
        out.println("</p>");
        out.println("<table width='90%' class='sortable'>");
        out.println("<thead><tr><td>Attribute</td><td>Value</td></tr></thead>");
        out.println("<tbody>");
        out.println("<tr><td>GRI</td><td>" + gri +
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
        out.println("<tr><td>Status</td><td>" +
                    resv.getStatus() + "</td></tr>");
        if (layer2Data != null) {
            out.println("<tr><td>Source</td><td>" +
                        layer2Data.getSrcEndpoint() + "</td></tr>");
            out.println("<tr><td>Destination</td><td>" +
                        layer2Data.getDestEndpoint() + "</td></tr>");
            // get VLAN tag
            String vlanTag = null;
            PathElem pathElem = path.getPathElem();
            while (pathElem != null) {
                if (pathElem.getDescription() != null) {
                    if (pathElem.getDescription().equals("ingress")) {
                        // assume just one VLAN for now
                        vlanTag = pathElem.getLinkDescr();
                        break;
                    }
                }
                pathElem = pathElem.getNextElem();
            }
            if (vlanTag != null) {
                out.println("<tr><td>VLAN</td><td>" +
                            vlanTag + "</td></tr>");
            } else {
                out.println("<tr><td>VLAN</td><td>" +
                            "Warning: No VLAN tag present</td></tr>");
            }
        } else if (layer3Data != null) {
            out.println("<tr><td>Source</td><td>" +
                        layer3Data.getSrcHost() + "</td></tr>");
            out.println("<tr><td>Destination</td><td>" +
                        layer3Data.getDestHost() + "</td></tr>");
            intParam = layer3Data.getSrcIpPort();
            if (intParam != null) {
                out.println("<tr><td>Source port</td><td>" +
                            intParam + "</td></tr>");
            }
            intParam = layer3Data.getDestIpPort();
            if (intParam != null) {
                out.println("<tr><td>Destination port</td><td>" +
                            intParam + "</td></tr>");
            }
            strParam = layer3Data.getProtocol();
            if (strParam != null) {
                out.println("<tr><td>Protocol</td><td>" +
                            strParam + "</td></tr>");
            }
            strParam = layer3Data.getDscp();
            if (strParam !=  null) {
                out.println("<tr><td>DSCP</td><td>" +
                            strParam + "</td></tr>");
            }
        }
        if (mplsData != null) {
            longParam = mplsData.getBurstLimit();
            if (longParam != null) {
                out.println("<tr><td>Burst limit</td><td>" +
                            longParam + "</td></tr>");
            }
            if (mplsData.getLspClass() != null) {
                out.println("<tr><td>Class</td><td>" +
                            mplsData.getLspClass() + "</td></tr>");
            }
        }
        Utils utils = new Utils("bss");
        String pathStr = utils.pathToString(path, false);
        if (pathStr != null) {
            out.println("<tr><td>Intradomain nodes in path</td><td>" +
                        pathStr + "</td></tr>");
        }
        String interPathStr = utils.pathToString(path, true);
        if ((interPathStr != null) &&
                !interPathStr.trim().equals("")) {
            out.println("<tr><td>Interdomain nodes</td><td>" +
                        interPathStr + "</td></tr>");
        }
        out.println("</tbody></table>");
        out.println("</content>");
    }
}
