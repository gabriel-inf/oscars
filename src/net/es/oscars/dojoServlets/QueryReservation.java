package net.es.oscars.dojoServlets;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.hibernate.*;
import net.sf.json.*;

import net.es.oscars.database.HibernateUtil;
import net.es.oscars.aaa.AAAException;
import net.es.oscars.aaa.UserManager;
import net.es.oscars.aaa.UserManager.AuthValue;
import net.es.oscars.bss.ReservationManager;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.Utils;
import net.es.oscars.bss.topology.*;


public class QueryReservation extends HttpServlet {

    public void
        doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        boolean allUsers = false;
        Reservation reservation = null;
        ReservationManager rm = new ReservationManager("bss");
        UserManager userMgr = new UserManager("aaa");
        UserSession userSession = new UserSession();
        net.es.oscars.dojoServlets.Utils utils =
            new net.es.oscars.dojoServlets.Utils();

        PrintWriter out = response.getWriter();
        response.setContentType("text/json-comment-filtered");
        String userName = userSession.checkSession(out, request);
        if (userName == null) { return; }
        Session aaa = HibernateUtil.getSessionFactory("aaa").getCurrentSession();
        aaa.beginTransaction();
         AuthValue authVal = userMgr.checkAccess(userName, "Reservations", "query");
         if (authVal == AuthValue.DENIED) {
             utils.handleFailure(out, "no permission to query Reservations",  aaa, null);
             return;
         }
         if (authVal == AuthValue.ALLUSERS) {allUsers=true;}
         aaa.getTransaction().commit();
         
        String gri = request.getParameter("gri");
        Session bss = 
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        bss.beginTransaction();
        try {
            reservation = rm.query(gri, userName, allUsers);
        } catch (BSSException e) {
            utils.handleFailure(out, e.getMessage(), null, bss);
            return;
        }
        if (reservation == null) {
            utils.handleFailure(out, "reservation does not exist", null, bss);
        }
        Map outputMap = new HashMap();
        outputMap.put("status", "Successfully got reservation details for " +
                                reservation.getGlobalReservationId());
        this.contentSection(outputMap, reservation, userName);
        outputMap.put("method", "QueryReservation");
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("/* " + jsonObject + " */");
        bss.getTransaction().commit();
    }

    public void
        doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }

    public void
        contentSection(Map outputMap, Reservation resv, String userName) {

        Long longParam = null;
        Integer intParam = null;
        String strParam = null;
        Long ms = null;

        String gri = resv.getGlobalReservationId();
        Path path = resv.getPath();
        Layer2Data layer2Data = path.getLayer2Data();
        Layer3Data layer3Data = path.getLayer3Data();
        MPLSData mplsData = path.getMplsData();
        String status = resv.getStatus();
        outputMap.put("gri", gri);
        outputMap.put("statusReplace", status);
        outputMap.put("userReplace", resv.getLogin());
        String sanitized = resv.getDescription().replace("<", "");
        String sanitized2 = sanitized.replace(">", "");
        outputMap.put("descriptionReplace", sanitized2);

        outputMap.put("startTimeConvert", resv.getStartTime());
        outputMap.put("endTimeConvert", resv.getEndTime());
        outputMap.put("createdTimeConvert", resv.getCreatedTime());
        outputMap.put("bandwidthReplace", resv.getBandwidth());
        if (layer2Data != null) {
            outputMap.put("sourceReplace", layer2Data.getSrcEndpoint());
            outputMap.put("destinationReplace", layer2Data.getDestEndpoint());
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
                outputMap.put("vlanReplace", vlanTag);
            } else {
                outputMap.put("vlanReplace",
                              "Warning: No VLAN tag present");
            }
        } else if (layer3Data != null) {
            outputMap.put("sourceReplace", layer3Data.getSrcHost());
            outputMap.put("destinationReplace", layer3Data.getDestHost());
            intParam = layer3Data.getSrcIpPort();
            if ((intParam != null) && (intParam != 0)) {
                outputMap.put("sourcePortReplace", intParam);
            }
            intParam = layer3Data.getDestIpPort();
            if ((intParam != null) && (intParam != 0)) {
                outputMap.put("destinationPortReplace", intParam);
            }
            strParam = layer3Data.getProtocol();
            if (strParam != null) {
                outputMap.put("protocolReplace", strParam);
            }
            strParam = layer3Data.getDscp();
            if (strParam !=  null) {
                outputMap.put("dscpReplace", strParam);
            }
        }
        if (mplsData != null) {
            longParam = mplsData.getBurstLimit();
            if (longParam != null) {
                outputMap.put("burstLimitReplace", longParam);
            }
            if (mplsData.getLspClass() != null) {
                outputMap.put("lspClassReplace", mplsData.getLspClass());
            }
        }
        net.es.oscars.bss.Utils utils = new net.es.oscars.bss.Utils("bss");
        String pathStr = utils.pathToString(path);
        if (pathStr != null) {
            outputMap.put("pathReplace", pathStr);
        }
    }
}
