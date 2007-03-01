package net.es.oscars.servlets;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class UserSession {

    public String checkSession(PrintWriter out, HttpServletRequest request) {
        String userName = this.getCookie("userName", request);
        if (userName == null) {
            out.println("<xml><status>");
            out.println("No session has been established");
            out.println("</status></xml>");
        }
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
}
