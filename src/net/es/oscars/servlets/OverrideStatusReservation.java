package net.es.oscars.servlets;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.*;
import net.sf.json.*;

import net.es.oscars.rmi.RmiUtils;
import net.es.oscars.rmi.bss.BssRmiInterface;
import net.es.oscars.rmi.bss.xface.RmiModifyStatusRequest;


public class OverrideStatusReservation extends HttpServlet {
    private Logger log = Logger.getLogger(OverrideStatusReservation.class);

    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {

        String methodName = "OverrideStatusReservation";
        this.log.info("servlet.start");
        UserSession userSession = new UserSession();
        PrintWriter out = response.getWriter();

        response.setContentType("application/json");
        String userName = userSession.checkSession(out, request, methodName);
        if (userName == null) {
            this.log.error("No user session: cookies invalid");
            return;
        }
        RmiModifyStatusRequest rmiRequest = new RmiModifyStatusRequest();
        String gri = request.getParameter("gri");
        String status = request.getParameter("forcedStatus");
        rmiRequest.setGlobalReservationId(gri);
        rmiRequest.setStatus(status);
        String result = null;
        try {
            BssRmiInterface rmiClient =
                RmiUtils.getBssRmiClient(methodName, log);
            result = rmiClient.unsafeModifyStatus(rmiRequest, userName);
        } catch (Exception ex) {
            ServletUtils.handleFailure(out, null, ex, methodName);
            return;
        }
        HashMap<String, Object> outputMap = new HashMap<String, Object>();
        outputMap.put("gri", gri);
        outputMap.put("status", "New status for reservation with GRI: " + gri +
                             " is " + status);
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("{}&&" + jsonObject);
        this.log.info("servlet.end");
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        this.doGet(request, response);
    }
}
