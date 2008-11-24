package net.es.oscars.servlets;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.*;

import net.sf.json.*;

import net.es.oscars.rmi.bss.BssRmiInterface;


public class PathTeardownReservation extends HttpServlet {
    private Logger log = Logger.getLogger(PathTeardownReservation.class);

    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {

        String methodName = "PathTeardownReservation";
        this.log.info("servlet.start");

        UserSession userSession = new UserSession();
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");

        String userName = userSession.checkSession(out, request, methodName);
        if (userName == null) {
            this.log.error("No user session: cookies invalid");
            return;
        }

        HashMap<String, Object> params = new HashMap<String, Object>();
        HashMap<String, Object> outputMap = new HashMap<String, Object>();
        
        params.put("style", "wbui");

        Enumeration e = request.getParameterNames();
        while (e.hasMoreElements()) {
            String paramName = (String) e.nextElement();
            String[] paramValues = request.getParameterValues(paramName);
            params.put(paramName, paramValues);
        }
        try {
            BssRmiInterface rmiClient = Utils.getCoreRmiClient(methodName, log, out);
            outputMap = rmiClient.teardownPath(params, userName);
        } catch (Exception ex) {
            this.log.error("rmiClient failed: " + ex.getMessage());
            Utils.handleFailure(out, "TeardownPathReservation not completed: " + ex.getMessage(), methodName);
            return;
        }

        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("{}&&" + jsonObject);
        this.log.info("servlet.end");
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        this.doGet(request, response);
    }


}
