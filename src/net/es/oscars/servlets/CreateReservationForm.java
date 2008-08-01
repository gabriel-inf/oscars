package net.es.oscars.servlets;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.hibernate.*;
import net.sf.json.*;

import net.es.oscars.database.HibernateUtil;
import net.es.oscars.aaa.AAAException;
import net.es.oscars.aaa.UserManager;
import net.es.oscars.aaa.UserManager.AuthValue;

public class CreateReservationForm extends HttpServlet {

    public void
        doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        UserSession userSession = new UserSession();

        String methodName = "CreateReservationForm";
        PrintWriter out = response.getWriter();
        response.setContentType("text/json-comment-filtered");
        String userName = userSession.checkSession(out, request, methodName);
        if (userName == null) {
            return;
        }
        UserManager mgr = new UserManager(Utils.getDbName());
        Session aaa = 
            HibernateUtil.getSessionFactory(Utils.getDbName()).getCurrentSession();
        aaa.beginTransaction();
        AuthValue authVal = mgr.checkModResAccess(userName,
                "Reservations", "create", 0, 0, false, false );
        if (authVal == AuthValue.DENIED ) {
            Utils.handleFailure(out,
                    "No permission granted to create a reservation",
                    methodName, aaa);
            return;
        }
        Map outputMap = new HashMap();
        outputMap.put("status", "Reservation creation form");
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        this.contentSection(outputMap, userName);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("/* " + jsonObject + " */");
        aaa.getTransaction().commit();
    }

    public void
        doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }

    public void
        contentSection(Map outputMap, String userName) {

        StringBuilder sb = new StringBuilder();
        UserManager mgr = new UserManager(Utils.getDbName());
        // check to see if user may specify path elements
        AuthValue authVal = mgr.checkModResAccess(userName,
                "Reservations", "create", 0, 0, true, false );
        if  (authVal != AuthValue.DENIED ) {
            outputMap.put("authorizedWarningDisplay", Boolean.TRUE);
            outputMap.put("authorizedPathDisplay", Boolean.TRUE);
        } else {
            outputMap.put("authorizedWarningDisplay", Boolean.FALSE);
            outputMap.put("authorizedPathDisplay", Boolean.FALSE);
        }
    }
}
