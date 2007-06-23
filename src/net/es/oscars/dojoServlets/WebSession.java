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

    // This is only called the first time this servlet is called.
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

    // This is only be called once, when Tomcat stops.
    public void destroy() {
        Logger log = Logger.getLogger(this.getClass());
        log.info("destroy.start");
        HibernateUtil.closeSessionFactory("aaa");
        HibernateUtil.closeSessionFactory("bss");
        log.info("destroy.end");
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {

        Utils utils = new Utils();
        Logger log = Logger.getLogger(this.getClass());

        log.info("doGet.start");
        HttpSession httpSession = request.getSession(false);
        // first time
        if (httpSession == null) {
            httpSession = request.getSession();
            // for now, set HTTP session to expire after 8 hours
            httpSession.setMaxInactiveInterval(3600 * 8);
        } else {
            // TODO:  handle session expired
            //utils.handleError(response, HttpServletResponse.SC_FORBIDDEN,
                              //"Session expired.  Please log in again.");
            ;
        }
        log.info(request.getParameter("logout"));
        this.mgr = new UserManager("aaa");
        // Get method to call from submit button name.  All servlet
        // requests are dispatched from here.  Depends on submit buttons
        // be named properly.
        if (request.getParameter("logout") != null) {
            this.logout(request, response);
        }
        log.info("doGet.finish");
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }


    public void logout(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        HttpSession httpSession = request.getSession();
        httpSession.invalidate();
        response.sendRedirect("/OSCARSDojo/");
    }
}
