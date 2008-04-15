package net.es.oscars.servlets;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.hibernate.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.aaa.UserManager;

public class UserSession {

    private String userCookieName;
    private String sessionCookieName;
    private boolean secureCookie;

    public  UserSession() {
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("aaa", true);
        this.userCookieName = props.getProperty("userName");
        this.sessionCookieName = props.getProperty("sessionName");
        this.secureCookie =
            props.getProperty("secureCookie").equals("1") ? true : false;
    }

    public String checkSession(PrintWriter out, HttpServletRequest request) {
        String userName = this.getCookie(this.userCookieName, request);
        String sessionName = this.getCookie(this.sessionCookieName, request);
        if ((userName == null) || (sessionName == null)) {
            out.println("<xml><status>");
            out.println("No session has been established");
            out.println("</status></xml>");
            userName = null;
            return null;
        }

        SessionFactory sf = HibernateUtil.getSessionFactory("aaa");
        if (sf == null) {
            out.println("<xml><status>");
            out.println("No session has been established");
            out.println("</status></xml>");
            userName = null;
            return null;
        }

        Session aaa = sf.getCurrentSession();
        aaa.beginTransaction();
        UserManager userMgr = new UserManager("aaa");
        if (!userMgr.validSession(userName, sessionName)) {
            out.println("<xml><status>");
            out.println("No session has been established");
            out.println("</status></xml>");
            userName = null;
        }
        aaa.getTransaction().commit();
        return userName;
    }

    public void setCookie(String cookieName, String cookieValue,
            HttpServletResponse response) {

        Cookie cookie = this.handleCookie(cookieName, cookieValue);
        cookie.setMaxAge(60*60*8); // expire after 8 hours
        response.addCookie(cookie);
    }

    public void expireCookie(String cookieName, String cookieValue,
            HttpServletResponse response) {

        Cookie cookie = this.handleCookie(cookieName, cookieValue);
        cookie.setMaxAge(0); // remove cookie
        response.addCookie(cookie);
    }

    public String getCookie(String cookieName, HttpServletRequest request) {

        String receivedCookieName = null;

        if (cookieName.equals("userName")) {
            receivedCookieName = this.userCookieName;
        } else if (cookieName.equals("sessionName")) {
            receivedCookieName = this.sessionCookieName;
        } else {
            receivedCookieName = cookieName;
        }
        Cookie[] cookies = request.getCookies();
        if (cookies == null) { return null; }
        for (int i=0; i < cookies.length; i++) {
            Cookie c = cookies[i];
            if ((c.getName().equals(receivedCookieName)) &&
                    (c.getValue() != null)) {
                return c.getValue();
            }
        }
        return null;
    }

    private Cookie handleCookie(String cookieName, String cookieValue) {

        String sentCookieName = null;

        // special cases to handle less obvious cookie names being used
        if (cookieName.equals("userName")) {
            sentCookieName = this.userCookieName;
        } else if (cookieName.equals("sessionName")) {
            sentCookieName = this.sessionCookieName;
        } else {
            sentCookieName = cookieName;
        }
        Cookie cookie = new Cookie(sentCookieName, cookieValue);
        // whether has to go over SSL
        cookie.setSecure(this.secureCookie);
        return cookie;
    }
}
