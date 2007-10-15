package net.es.oscars.servlets;

import net.es.oscars.aaa.UserManager;
import net.es.oscars.aaa.UserManager.AuthValue;
import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.ReservationManager;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.interdomain.*;
import net.es.oscars.oscars.TypeConverter;
import net.es.oscars.wsdlTypes.*;

import org.apache.log4j.*;

import org.hibernate.*;

import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlanePathContent;

import java.io.*;

import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;


public class CreateReservation extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        Logger log = Logger.getLogger(this.getClass());
        log.info("CreateReservation.start");

        TypeConverter tc = new TypeConverter();
        Forwarder forwarder = new Forwarder();
        ReservationManager rm = new ReservationManager("bss");
        UserSession userSession = new UserSession();
        Utils utils = new Utils();
        PrintWriter out = response.getWriter();
        ReservationDetails detailsOutput = new ReservationDetails();

        response.setContentType("text/xml");

        String userName = userSession.checkSession(out, request);

        if (userName == null) {
            return;
        }

        Reservation resv = this.toReservation(userName, request);
        PathInfo pathInfo = this.handlePath(request);

        Session aaa = HibernateUtil.getSessionFactory("aaa").getCurrentSession();
        aaa.beginTransaction();

        UserManager userMgr = new UserManager("aaa");

        // Check to see if user can create this  reservation
        // bandwidth limits are stored in megaBits
        int reqBandwidth = (int) (resv.getBandwidth() / 1000000);

        // convert from milli-seconds to minutes
        int reqDuration = (int) (resv.getEndTime() - resv.getStartTime()) / 6000;
        boolean specifyPath = false;

        if (request.getParameter("explicitPath") != null) {
            specifyPath = true;
        }

        AuthValue authVal = userMgr.checkModResAccess(userName, "Reservations",
                "create", reqBandwidth, reqDuration, specifyPath, false);

        if (authVal == AuthValue.DENIED) {
            utils.handleFailure(out, "createReservation permission denied",
                aaa, null);

            return;
        }

        aaa.getTransaction().commit();

        Session bss = HibernateUtil.getSessionFactory("bss").getCurrentSession();
        bss.beginTransaction();

        try {
            // url returned, if not null, indicates location of next domain
            // manager
            rm.create(resv, userName, pathInfo);
            // checks whether next domain should be contacted, forwards to
            // the next domain if necessary, and handles the response
            forwarder.create(resv, pathInfo);
        } catch (BSSException e) {
            utils.handleFailure(out, e.getMessage(), null, bss);

            return;
        } catch (Exception e) {
            // use this so we can find NullExceptions
            utils.handleFailure(out, e.toString(), null, bss);

            return;
        }

        // reservation was modified in place by ReservationManager create
        // and Forwarder create; now store it in the db
        try {
            log.info("to store");
            rm.store(resv);
            log.info("past store");
        } catch (BSSException e) {
            utils.handleFailure(out, e.getMessage(), null, bss);

            return;
        }

        log.info("to page creation");
        out.println("<xml>");
        out.println("<status>Created reservation with GRI " +
            resv.getGlobalReservationId() + "</status>");
        utils.tabSection(out, request, response, "ListReservations");
        detailsOutput.contentSection(out, resv, userName);
        out.println("</xml>");
        bss.getTransaction().commit();
        log.info("CreateReservation.end");
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        this.doGet(request, response);
    }

    public Reservation toReservation(String userName, HttpServletRequest request) {
        String strParam = null;
        Long millis = null;
        Long bandwidth = null;

        Reservation resv = new Reservation();
        resv.setLogin(userName);

        // necessary type conversions performed here; validation done in
        // ReservationManager
        strParam = request.getParameter("startTime");
        millis = (strParam != null) ? Long.parseLong(strParam) : null;
        resv.setStartTime(millis);
        strParam = request.getParameter("endTime");
        millis = (strParam != null) ? Long.parseLong(strParam) : null;
        resv.setEndTime(millis);

        strParam = request.getParameter("bandwidth");
        bandwidth = (strParam != null)
            ? (Long.valueOf(strParam.trim()) * 1000000L) : null;
        resv.setBandwidth(bandwidth);
        resv.setDescription(request.getParameter("description"));

        return resv;
    }

    /**
     * Takes form parameters and builds PathInfo structures.
     *
     * @param request contains form request parameters
     * @return pathInfo a PathInfo instance with layer 3 information
     */
    public PathInfo handlePath(HttpServletRequest request) {
        CtrlPlanePathContent path = null;
        Logger log = Logger.getLogger(this.getClass());

        PathInfo pathInfo = new PathInfo();
        String explicitPath = request.getParameter("explicitPath");

        if ((explicitPath != null) && !explicitPath.trim().equals("")) {
            log.info("explicit path: " + explicitPath);
            path = new CtrlPlanePathContent();
            path.setId("userPath"); //id doesn't matter in this context

            String[] hops = explicitPath.split("\\s+");

            for (int i = 0; i < hops.length; i++) {
                hops[i] = hops[i].trim();

                if (hops[i].equals(" ") || hops[i].equals("")) {
                    continue;
                }

                CtrlPlaneHopContent hop = new CtrlPlaneHopContent();
                // these can currently be either topology identifiers
                // or IP addresses
                hop.setId(i + "");
                hop.setLinkIdRef(hops[i]);
                log.info("explicit path hop: " + hops[i]);
                path.addHop(hop);
            }

            pathInfo.setPath(path);
        }

        String vlanTag = request.getParameter("vlanTag");

        // TODO:  layer 2 parameters trump layer 3 parameters for now, until
        // handle in Javascript
        if (vlanTag != null) {
            Layer2Info layer2Info = new Layer2Info();
            VlanTag vtagObject = new VlanTag();
            vtagObject.setString(vlanTag);
            vtagObject.setTagged(true);
            layer2Info.setSrcEndpoint(request.getParameter("source"));
            layer2Info.setDestEndpoint(request.getParameter("destination"));
            layer2Info.setSrcVtag(vtagObject);
            layer2Info.setDestVtag(vtagObject);
            pathInfo.setLayer2Info(layer2Info);

            return pathInfo;
        }

        Layer3Info layer3Info = new Layer3Info();
        layer3Info.setSrcHost(request.getParameter("source"));
        layer3Info.setDestHost(request.getParameter("destination"));

        String strParam = request.getParameter("srcPort");

        if (strParam != null) {
            layer3Info.setSrcIpPort(Integer.valueOf(strParam));
        } else {
            layer3Info.setSrcIpPort(0);
        }

        strParam = request.getParameter("destPort");

        if (strParam != null) {
            layer3Info.setDestIpPort(Integer.valueOf(strParam));
        } else {
            layer3Info.setDestIpPort(0);
        }

        layer3Info.setProtocol(request.getParameter("protocol"));
        layer3Info.setDscp(request.getParameter("dscp"));
        pathInfo.setLayer3Info(layer3Info);

        MplsInfo mplsInfo = new MplsInfo();
        mplsInfo.setBurstLimit(10000000);
        pathInfo.setMplsInfo(mplsInfo);

        return pathInfo;
    }
}
