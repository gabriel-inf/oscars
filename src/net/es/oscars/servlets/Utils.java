package net.es.oscars.servlets;

import java.io.PrintWriter;
import javax.servlet.http.*;
import org.hibernate.*;

public class Utils {

    public void tabSection(PrintWriter out, HttpServletRequest request,
                           HttpServletResponse response, String tabName) {

        UserSession userSession = new UserSession();
        String previousTab = userSession.getCookie("tabName", request);
        if (previousTab == null) { previousTab = ""; }
        if (!previousTab.equals(tabName)) {
            userSession.setCookie("tabName", tabName, response);
        }
        out.println("<tabs>");
        out.println("<active>" + tabName + "</active>");
        out.println("<previous>" + previousTab + "</previous>");
        out.println("</tabs>");
    }

    public void handleFailure(PrintWriter out, String message, Session aaa,
                              Session bss) {

        if (aaa != null) { aaa.getTransaction().rollback(); }
        if (bss != null) { bss.getTransaction().rollback(); }
        out.println("<xml><status>");
        out.println(message);
        out.println("</status></xml>");
        return;
    }
}
