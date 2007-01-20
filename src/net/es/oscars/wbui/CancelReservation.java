package net.es.oscars.wbui;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.hibernate.*;

import net.es.oscars.database.HibernateUtil;
import net.es.oscars.bss.ReservationManager;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.BSSException;



public class CancelReservation extends HttpServlet {

    public void
        doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        Reservation reservation = null;
        String reply = null;

        ReservationManager rm = new ReservationManager();
        rm.setSession();
        UserSession userSession = new UserSession();
        Utils utils = new Utils();
        ReservationDetails detailsOutput = new ReservationDetails();

        PrintWriter out = response.getWriter();
        response.setContentType("text/xml");
        String userName = userSession.checkSession(out, request);
        if (userName == null) { return; }

        String tag = request.getParameter("tag");
        Session bss = 
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        bss.beginTransaction();
        try {
            reply = rm.cancel(tag, userName);
            reservation = rm.query(tag, true);
        } catch (BSSException e) {
            utils.handleFailure(out, e.getMessage(), null, bss);
            return;
        }
        out.println("<xml>");
        out.println("<status>Successfully got reservation details</status>");
        utils.tabSection(out, request, response, "ListReservations");
        detailsOutput.contentSection(out, reservation, rm, userName);
        out.println("</xml>");
        bss.getTransaction().commit();
    }

    public void
        doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }
}
