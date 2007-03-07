package net.es.oscars.dojoServlets;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.hibernate.*;

import net.es.oscars.database.HibernateUtil;
import net.es.oscars.database.Initializer;
import net.es.oscars.aaa.UserManager;
import net.es.oscars.aaa.AAAException;

public class WebSession extends HttpServlet {
    private UserManager mgr;

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String method = request.getParameter("method");
        this.mgr = new UserManager();
        // TODO:  call method
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }

    public void login(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        Initializer initializer = new Initializer();
        initializer.initDatabase();

        this.mgr.setSession();
        String userName = request.getParameter("userName");
        response.setContentType("text/xml");
        Session aaa = 
            HibernateUtil.getSessionFactory("aaa").getCurrentSession();
        aaa.beginTransaction();
        try {
            String unused =
                this.mgr.verifyLogin(userName, request.getParameter("password"));
        } catch (AAAException e) {
            this.handleFailure(e.getMessage(), aaa, null);
            return;
        }
        this.setCookie("userName", userName, response);

        aaa.getTransaction().commit();
    }

    public void logout(HttpServletResponse response)
        throws IOException, ServletException {

        response.sendRedirect("/OSCARS/index.html");
    }

    public String checkSession(HttpServletRequest request) {
        String userName = this.getCookie("userName", request);
        return userName;
    }

    public void setCookie(String cookieName, String cookieValue,
            HttpServletResponse response) {

        Cookie cookie = new Cookie(cookieName, cookieValue);
        cookie.setMaxAge(60*60*8); // 8 hours
        response.addCookie(cookie);
    }

    public String getCookie(String cookieName, HttpServletRequest request) {

        Cookie[] cookies = request.getCookies();
        if (cookies == null) { return null; }
        for (int i=0; i < cookies.length; i++) {
            Cookie c = cookies[i];
            if ((c.getName().equals(cookieName)) &&
                    (c.getValue() != null)) {
                return c.getValue();
            }
        }
        return null;
    }

    public void handleFailure(String message, Session aaa,
                              Session bss) {

        if (aaa != null) { aaa.getTransaction().rollback(); }
        if (bss != null) { bss.getTransaction().rollback(); }
        return;
    }
}
