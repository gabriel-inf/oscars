package net.es.oscars.dojoServlets;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.hibernate.*;

import net.es.oscars.database.HibernateUtil;
import net.es.oscars.bss.ReservationManager;
import net.es.oscars.bss.Reservation;
import net.es.oscars.pathfinder.Domain;
import net.es.oscars.bss.BSSException;
import net.es.oscars.interdomain.*;


public class ReservationMethods {
    private ReservationManager rm;

    public ReservationMethods() {
        this.rm = new ReservationManager();
    }

    public Reservation query(HttpServletRequest request)
            throws IOException, ServletException, BSSException {

        String tag = request.getParameter("tag");
        return this.rm.query(tag, true);
    }

    public void create(HttpServletRequest request, String userName)
            throws IOException, ServletException, BSSException,
              Exception {

        Domain nextDomain = null;

        Map<String,String> params = null;
        List<Map<String,String>> forwardResponse = null;

        Reservation resv = this.toReservation(request, userName);
        String ingressRouterIP = request.getParameter("ingressRouter");
        String egressRouterIP = request.getParameter("egressRouter");

        nextDomain = this.rm.create(resv, userName, ingressRouterIP,
                                    egressRouterIP);
        if (nextDomain != null) {
            // checks whether next domain should be contacted, forwards to
            // the next domain if necessary, and handles the response
            //Forwarder.create(resv, nextDomain);
        }
    }

    public Reservation cancel(HttpServletRequest request, String userName)
            throws IOException, ServletException, BSSException {

        Reservation reservation = null;
        String reply = null;

        String tag = request.getParameter("tag");
        reply = this.rm.cancel(tag, userName);
        reservation = this.rm.query(tag, true);
        return reservation;
    }

    public List<Reservation> list(HttpServletRequest request, String login) 
            throws BSSException {

        boolean authorized = true;

        List<Reservation> reservations = this.rm.list(login, authorized);
        if (reservations == null) {
            return null;
        }
        // this.contentSection(reservations, login);
        return reservations;
    }

    public Reservation toReservation(HttpServletRequest request,
                                     String userName) {

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

    public Map<String,String> toClientRequest(Reservation resv) {

        Map<String,String> params = new HashMap<String,String>();

        params.put("srcHost", resv.getSrcHost());
        params.put("destHost", resv.getDestHost());
        params.put("startTime", resv.getStartTime().toString());
        params.put("endTime", resv.getEndTime().toString());
        params.put("bandwidth", resv.getBandwidth().toString());
        params.put("protocol", resv.getProtocol());
        params.put("description", resv.getDescription());
        params.put("routeDirection", "FORWARD");
        return params;
    }
}
