package net.es.oscars.servlets;

import java.io.*;
import java.util.*;
import java.rmi.RemoteException;

import javax.servlet.http.*;

import org.apache.log4j.*;

import net.es.oscars.PropHandler;
import net.es.oscars.rmi.RmiUtils;
import net.es.oscars.rmi.aaa.AaaRmiInterface;
import net.es.oscars.rmi.bss.BssRmiInterface;

public class UserSession {

    private String userCookieName;
    private String sessionCookieName;
    private boolean secureCookie;
    private String guestLogin;
    private AaaRmiInterface aaaInterface;
    private BssRmiInterface bssInterface;
    private Logger log = Logger.getLogger(UserSession.class);

    public  UserSession() {
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("aaa", true);
        this.userCookieName = props.getProperty("userName");
        this.sessionCookieName = props.getProperty("sessionName");
        this.guestLogin = props.getProperty("guestLogin");
        String secureCookieProp = props.getProperty("secureCookie");
        if (secureCookieProp != null) {
            this.secureCookie = secureCookieProp.equals("1") ? true : false;
        } else {
            log.warn("please re-configure to use secure cookies");
            this.secureCookie = false;
        }
    }

    public String checkSession(PrintWriter out, HttpServletRequest request, String methodName) {
        return checkSession(out,null,request,methodName);
    }
    
    public String
        checkSession(PrintWriter out, AaaRmiInterface rmiClient,
                     HttpServletRequest request, String methodName) {
        String userName = this.getCookie(this.userCookieName, request);
        String sessionName = this.getCookie(this.sessionCookieName, request);
        String errorMsg;

        if ((userName == null) || (sessionName == null)) {
            String status = "Your login session has expired. ";
            if ((userName == null) && (sessionName == null)) {
                status += "Login cookies are not set. ";
            } else if (userName == null) {
                status += "The user name cookie is not set. ";
            } else if (sessionName == null) {
                status += "The session name cookie is not set. ";
            }
            status += "Please try logging in again.";
            ServletUtils.handleFailure(out, status, methodName);
            return null;
        }
        Boolean validSession = false;
        try {
            rmiClient =
                RmiUtils.getAaaRmiClient(methodName, log);
            validSession = rmiClient.validSession(userName, sessionName);
        } catch (Exception e) {
            ServletUtils.handleFailure(out, log, e, methodName);
            return null;
        }
        this.aaaInterface = rmiClient;
        String cookieUserName = userName;
        if (!validSession) {
            userName = null;
            errorMsg = "Your login session has an error. " +
                "There is a problem with the login for user " + cookieUserName +
                ".";
            log.error(errorMsg);
            errorMsg += " Please try logging in again.";
            ServletUtils.handleFailure(out, errorMsg, methodName);
        }
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

    public String getGuestLogin() {
        return this.guestLogin;
    }

    public AaaRmiInterface getAaaInterface() {
        return aaaInterface;
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
        cookie.setVersion(1);
        // whether has to go over SSL
        cookie.setSecure(this.secureCookie);
        return cookie;
    }
}
