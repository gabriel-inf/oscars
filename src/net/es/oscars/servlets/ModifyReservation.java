package net.es.oscars.servlets;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.*;

import net.sf.json.*;
import net.es.oscars.rmi.bss.BssRmiInterface;


public class ModifyReservation extends HttpServlet {
    private Logger log = Logger.getLogger(ModifyReservation.class);

    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {

        String methodName = "ModifyReservation";
        this.log.info("servlet.start");

        UserSession userSession = new UserSession();
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        String userName = userSession.checkSession(out, request, methodName);
        if (userName == null) {
            this.log.error("No user session: cookies invalid");
            return;
        }

        HashMap<String, String[]> inputMap = new HashMap<String, String[]>();
        HashMap<String, Object> outputMap = new HashMap<String, Object>();


        Enumeration e = request.getParameterNames();
        while (e.hasMoreElements()) {
            String paramName = (String) e.nextElement();
            String[] paramValues = request.getParameterValues(paramName);
            inputMap.put(paramName, paramValues);
        }
        try {
            BssRmiInterface rmiClient = Utils.getCoreRmiClient(methodName, log, out);
            outputMap = rmiClient.modifyReservation(inputMap, userName);
        } catch (Exception ex) {
            this.log.error("rmiClient failed: " + ex);
            Utils.handleFailure(out, "ModifyReservation not completed: " + ex.getMessage(), methodName);
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
