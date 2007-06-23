package net.es.oscars.servlets;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.hibernate.*;

import net.es.oscars.database.HibernateUtil;
import net.es.oscars.aaa.AAAException;
import net.es.oscars.aaa.UserManager;
import net.es.oscars.aaa.UserManager.AuthValue;
import net.es.oscars.bss.ReservationManager;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.BSSException;


public class QueryReservation extends HttpServlet {

    public void
        doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        boolean allUsers = false;
        Reservation reservation = null;
        ReservationManager rm = new ReservationManager("bss");
        UserManager userMgr = new UserManager("aaa");
        ReservationDetails detailsOutput = new ReservationDetails();
        UserSession userSession = new UserSession();
        Utils utils = new Utils();

        PrintWriter out = response.getWriter();
        response.setContentType("text/xml");
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
         
        String tag = request.getParameter("tag");
        Session bss = 
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        bss.beginTransaction();
        try {
            reservation = rm.query(tag, userName, allUsers);
        } catch (BSSException e) {
            utils.handleFailure(out, e.getMessage(), null, bss);
            return;
        }
        if (reservation == null) {
            utils.handleFailure(out, "reservation does not exist", null, bss);
        }
        detailsOutput = new ReservationDetails();
        out.println("<xml>");
        out.println("<status>Successfully got reservation details</status>");
        utils.tabSection(out, request, response, "ListReservations");
        detailsOutput.contentSection(out, reservation, userName);
        out.println("</xml>");
        bss.getTransaction().commit();
    }

    public void
        doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }
}
