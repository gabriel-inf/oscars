package net.es.oscars.servlets;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.*;
import net.sf.json.*;

import net.es.oscars.bss.Reservation;
import net.es.oscars.rmi.RmiUtils;
import net.es.oscars.rmi.bss.BssRmiInterface;


public class ModifyReservation extends HttpServlet {
    private Logger log = Logger.getLogger(ModifyReservation.class);

    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {

        String methodName = "ModifyReservation";
        this.log.info(methodName + ":start");

        UserSession userSession = new UserSession();
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        String userName = userSession.checkSession(out, request, methodName);
        if (userName == null) {
            this.log.warn("No user session: cookies invalid");
            return;
        }
        Reservation resv = this.toReservation(request);
        Reservation persistentResv = null;
        try {
            BssRmiInterface rmiClient =
                RmiUtils.getBssRmiClient(methodName, log);
            persistentResv = rmiClient.modifyReservation(resv, userName);
        } catch (Exception ex) {
            ServletUtils.handleFailure(out, log, ex, methodName);
            return;
        }
        HashMap<String, Object> outputMap = new HashMap<String, Object>();
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        outputMap.put("status", "modified reservation with GRI " +
                                resv.getGlobalReservationId());
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        out.println("{}&&" + jsonObject);
        this.log.info(methodName + ":end");
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        this.doGet(request, response);
    }

    public Reservation toReservation(HttpServletRequest request) {

        String strParam = null;
        Long bandwidth = null;
        Long seconds = 0L;
        Reservation resv = new Reservation();

        strParam = request.getParameter("gri");
        resv.setGlobalReservationId(strParam);
        // necessary type conversions performed here; validation done in
        // ReservationManager
        strParam = request.getParameter("modifyStartSeconds");
        if ((strParam != null) && (!strParam.equals(""))) {
            seconds = Long.parseLong(strParam);
        }
        resv.setStartTime(seconds);
        strParam = request.getParameter("modifyEndSeconds");
        if ((strParam != null) && (!strParam.equals(""))) {
            seconds = Long.parseLong(strParam);
        }
        resv.setEndTime(seconds);

        // currently hidden form fields; not modifiable
        strParam = request.getParameter("modifyBandwidth");
        bandwidth = ((strParam != null) && !strParam.trim().equals(""))
            ? (Long.valueOf(strParam.trim()) * 1000000L) : 0L;
        resv.setBandwidth(bandwidth);
        String description = request.getParameter("modifyDescription");
        resv.setDescription(description);
        return resv;
    }
}
