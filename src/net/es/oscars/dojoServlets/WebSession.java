package net.es.oscars.dojoServlets;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.hibernate.*;

import org.apache.log4j.*;

import net.es.oscars.database.HibernateUtil;
import net.es.oscars.database.Initializer;
import net.es.oscars.aaa.UserManager;
import net.es.oscars.aaa.AAAException;

public class WebSession extends HttpServlet {
    private UserManager mgr;

    // This is only be called once.
    public void init() throws ServletException {
        Logger log = Logger.getLogger(this.getClass());
        log.info("init.start");
        Initializer initializer = new Initializer();
        List<String> dbnames = new ArrayList<String>();
        dbnames.add("aaa");
        dbnames.add("bss");
        initializer.initDatabase(dbnames);
        log.info("init.end");
    }

    // This is only be called once.
    public void destroy() {
        Logger log = Logger.getLogger(this.getClass());
        log.info("destroy.start");
        HibernateUtil.closeSessionFactory("aaa");
        HibernateUtil.closeSessionFactory("bss");
        log.info("destroy.end");
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {

        HttpSession httpSession = null;
        Utils utils = new Utils();

        // when using login method, a new HttpSession is created
        if (request.getParameter("login") == null) {
            httpSession = request.getSession(false);
            if (httpSession == null) {
                // TODO:  dialog box automatically brought up if
                // session expired?
                utils.handleError(response, HttpServletResponse.SC_FORBIDDEN,
                                  "Session expired.  Please log in again.");
                return;
            }
        }
        this.mgr = new UserManager("aaa");
        // Get method to call from submit button name.  All servlet
        // requests are dispatched from here.  Depends on submit buttons
        // be named properly.
        if (request.getParameter("login") != null) {
            this.login(request, response, utils);
        } else if (request.getParameter("logout") != null) {
            this.logout(request, response);
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }

    public void login(HttpServletRequest request,
                      HttpServletResponse response, Utils utils)
            throws IOException, ServletException {

        PrintWriter out = response.getWriter();

        this.mgr.setSession();
        String userName = request.getParameter("userName");
        response.setContentType("text/xml");
        // NOTE:  Be careful to distinguish between Hibernate Sessions and
        //        HTTP Sessions.  Latter uses HttpSession class.
        Session aaa = 
            HibernateUtil.getSessionFactory("aaa").getCurrentSession();
        aaa.beginTransaction();
        try {
            String unused =
                this.mgr.verifyLogin(userName, request.getParameter("password"));
        } catch (AAAException e) {
            aaa.getTransaction().rollback();
            //utils.handleError(response, HttpServletResponse.SC_FORBIDDEN,
                              //e.getMessage());
            //return;
        }
        //aaa.getTransaction().commit();
        HttpSession httpSession = request.getSession(true);
        // for now, set HTTP session to expire after 8 hours
        httpSession.setMaxInactiveInterval(3600 * 8);
    }

    public void logout(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        HttpSession httpSession = request.getSession(false);
        httpSession.invalidate();
        response.sendRedirect("/OSCARS/index.html");
    }
}
