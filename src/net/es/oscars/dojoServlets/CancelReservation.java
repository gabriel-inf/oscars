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
import net.es.oscars.interdomain.*;


public class CancelReservation extends HttpServlet {

    public void
        doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        Reservation reservation = null;
        String reply = null;
        boolean allUsers = false;
        
        UserManager userMgr =  new UserManager("aaa");
        ReservationManager rm = new ReservationManager("bss");
        UserSession userSession = new UserSession();
        Utils utils = new Utils();
        Forwarder forwarder = new Forwarder();
        
        PrintWriter out = response.getWriter();
        response.setContentType("text/json-comment-filtered");
        String userName = userSession.checkSession(out, request);
        if (userName == null) { return; }
 
        Session aaa = HibernateUtil.getSessionFactory("aaa").getCurrentSession();
        aaa.beginTransaction();
        AuthValue authVal = userMgr.checkAccess(userName, "Reservations", "modify");
        if (authVal == AuthValue.DENIED) {
            utils.handleFailure(out, "no permission to cancel Reservations",  aaa, null);
            return;
        }
        if (authVal == AuthValue.ALLUSERS) {allUsers=true;}
        aaa.getTransaction().commit();
        
        String gri = request.getParameter("gri");
        
        Session bss = 
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        bss.beginTransaction();
        try {
        	reservation = rm.cancel(gri, userName, allUsers);
            String remoteStatus = forwarder.cancel(reservation);
            rm.finalizeCancel(reservation, remoteStatus);
        } catch (BSSException e) {
            utils.handleFailure(out, e.getMessage(), null, bss);
            return;
        } catch (InterdomainException ex) {
            utils.handleFailure(out, ex.getMessage(), null, bss);
            return;
        }
        Map outputMap = new HashMap();
        outputMap.put("status", "Successfully cancelled reservation with " +
                                "GRI " + reservation.getGlobalReservationId());
        outputMap.put("method", "CancelReservation");
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
}