package net.es.oscars.servlets;

import java.io.*;
import java.util.*;
import java.rmi.RemoteException;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;
import net.sf.json.*;

import net.es.oscars.rmi.aaa.AaaRmiInterface;
import net.es.oscars.aaa.AuthValue;

public class CreateReservationForm extends HttpServlet {
    private Logger log = Logger.getLogger(CreateReservationForm.class);


    public void
        doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        UserSession userSession = new UserSession();

        String methodName = "CreateReservationForm";
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        String userName = userSession.checkSession(out, request, methodName);
        if (userName == null) {
            return;
        }
        AaaRmiInterface coreRmiClient = ServletUtils.getCoreRmiClient(methodName, log, out);
        AuthValue authVal = null;
        try {
            authVal = coreRmiClient.checkModResAccess(userName, "Reservations", "create", 0, 0, false, false );
        } catch (Exception ex) {
            this.log.error("rmiClient failed with " + ex.getMessage());
            ServletUtils.handleFailure(out, "CreateReservationForm internal error: " + ex.getMessage(), methodName);
            return;
        }
        if (authVal == null || authVal == AuthValue.DENIED ) {
            ServletUtils.handleFailure(out, "No permission granted to create a reservation", methodName);
            return;
        }

        Map<String, Object> outputMap = new HashMap<String, Object>();
        try {
            // this form does not reset status
            outputMap.put("method", methodName);
            outputMap.put("success", Boolean.TRUE);
            this.contentSection(outputMap, userName, coreRmiClient, out);
        } catch (RemoteException ex) {
            return;
        }
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("{}&&" + jsonObject);
    }

    public void
        doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }

    public void contentSection(Map<String, Object> outputMap, String userName, AaaRmiInterface rmiClient, PrintWriter out) throws RemoteException {
        String methodName = "CreateReservationForm.contentSection";
        // check to see if user may specify path elements
        HashMap<String, Object> authResult = new HashMap<String, Object>();
        AuthValue authVal = rmiClient.checkModResAccess(userName, "Reservations", "create", 0, 0, true, false );

        if  (authVal!= null && authVal != AuthValue.DENIED ) {
            outputMap.put("authorizedWarningDisplay", Boolean.TRUE);
            outputMap.put("authorizedPathDisplay", Boolean.TRUE);
        } else {
            outputMap.put("authorizedWarningDisplay", Boolean.FALSE);
            outputMap.put("authorizedPathDisplay", Boolean.FALSE);
        }
    }
}
