package net.es.oscars.servlets;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.*;

public class UserLogout extends HttpServlet {

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException, ServletException {

        UserSession userSession = new UserSession();
        userSession.expireCookie("statusList", "", response);
        userSession.expireCookie("description", "", response);
        userSession.expireCookie("linkIds", "", response);
        userSession.expireCookie("startDateSearch", "", response);
        userSession.expireCookie("startTimeSearch", "", response);
        userSession.expireCookie("endDateSearch", "", response);
        userSession.expireCookie("endTimeSearch", "", response);
        userSession.expireCookie("startTimeSeconds", "", response);
        userSession.expireCookie("endTimeSeconds", "", response);
        response.sendRedirect("/OSCARS/index.html");
    }

    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }
}
