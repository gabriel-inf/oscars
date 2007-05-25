package net.es.oscars.servlets;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.hibernate.*;

import net.es.oscars.oscars.TypeConverter;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.bss.ReservationManager;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.BSSException;
import net.es.oscars.interdomain.*;


public class CreateReservation extends HttpServlet {

    public void
        doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        Forwarder forwarder = new Forwarder();
        ReservationManager rm = new ReservationManager("bss");
        UserSession userSession = new UserSession();
        Utils utils = new Utils();
        PrintWriter out = response.getWriter();
        ReservationDetails detailsOutput = new ReservationDetails();

        response.setContentType("text/xml");
        String userName = userSession.checkSession(out, request);
        if (userName == null) { return; }

        Reservation resv = this.toReservation(out, userName, request);
        String ingressRouter = request.getParameter("ingressRouter");
        String egressRouter = request.getParameter("egressRouter");

        Session bss = 
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        bss.beginTransaction();
        try {
            // url returned, if not null, indicates location of next domain
            // manager
            String url = rm.create(resv, userName, ingressRouter, egressRouter,
                                   null);
            // checks whether next domain should be contacted, forwards to
            // the next domain if necessary, and handles the response
            forwarder.create(resv, null);
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
            rm.store(resv);
        } catch (BSSException e) {
            utils.handleFailure(out, e.getMessage(), null, bss);
            return;
        }
        TypeConverter tc = new TypeConverter();
        out.println("<xml>");
        out.println("<status>Created reservation with tag " +
                    tc.getReservationTag(resv) + "</status>");
        utils.tabSection(out, request, response, "ListReservations");
        detailsOutput.contentSection(out, resv, userName);
        out.println("</xml>");
        bss.getTransaction().commit();
    }

    public void 
        doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }

    public Reservation toReservation(PrintWriter out, String userName,
                                     HttpServletRequest request) {

        String strParam = null;
        Long millis = null;
        Long bandwidth = null;

        Reservation resv = new Reservation();
        resv.setLogin(userName);

        // necessary type conversions performed here; validation done in
        // ReservationManager
        strParam = request.getParameter("startTime");
        millis = (strParam != null) ?  Long.parseLong(strParam) : null;
        resv.setStartTime(millis);
        strParam = request.getParameter("endTime");
        millis = (strParam != null) ?  Long.parseLong(strParam) : null;
        resv.setEndTime(millis);

        resv.setSrcHost(request.getParameter("srcHost"));
        strParam = request.getParameter("srcPort");
        if (strParam != null) {
            resv.setSrcPort(Integer.valueOf(strParam));
        } else {
            resv.setSrcPort(null);
        }
        resv.setDestHost(request.getParameter("destHost"));
        strParam = request.getParameter("destPort");
        if (strParam != null) {
            resv.setDestPort(Integer.valueOf(strParam));
        } else {
            resv.setDestPort(null);
        }

        strParam = request.getParameter("bandwidth");
        bandwidth = (strParam != null) ?  Long.valueOf(strParam.trim()) * 1000000L :
                                          null;
        resv.setBandwidth(bandwidth);

        resv.setProtocol(request.getParameter("protocol"));
        resv.setDscp(request.getParameter("dscp"));
        resv.setDescription(request.getParameter("description"));
        return resv;
    }
}
