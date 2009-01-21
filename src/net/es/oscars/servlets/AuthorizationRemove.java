package net.es.oscars.servlets;

import java.io.*;
import java.rmi.RemoteException;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;
import net.sf.json.*;

import net.es.oscars.aaa.AuthValue;
import net.es.oscars.rmi.aaa.AaaRmiInterface;
import net.es.oscars.rmi.model.ModelObject;
import net.es.oscars.rmi.model.ModelOperation;


public class AuthorizationRemove extends HttpServlet {
    private Logger log = Logger.getLogger(AuthorizationRemove.class);
    public void
        doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        log.debug("AuthorizationRemove.start");

        UserSession userSession = new UserSession();
        String methodName = "AuthorizationRemove";
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");

        String userName = userSession.checkSession(out, request, methodName);
        if (userName == null) {
            log.error("No user session: cookies invalid");
            return;
        }

        String attributeName = request.getParameter("authAttributeName");
        String permissionName = request.getParameter("permissionName");
        String resourceName = request.getParameter("resourceName");
        String constraintName = request.getParameter("constraintName");

        log.debug("Removing attribute: " + attributeName +" resource: " + resourceName + " permission: "
                + permissionName + " constraintName: " + constraintName );

        HashMap<String, Object> rmiParams = new HashMap<String, Object>();
        rmiParams.put("attributeName", attributeName);
        rmiParams.put("permissionName", permissionName);
        rmiParams.put("resourceName", resourceName);
        rmiParams.put("constraintName", constraintName);

        rmiParams.put("objectType", ModelObject.AUTHORIZATION);
        rmiParams.put("operation", ModelOperation.DELETE);



        try {
            AaaRmiInterface rmiClient = ServletUtils.getAaaRmiClient(methodName, log, out);
            AuthValue authVal = ServletUtils.getAuth(userName, "AAA", "modify", rmiClient, methodName, log, out);

            if (authVal == AuthValue.DENIED)  {
                String errorMsg = "User "+userName+" not allowed to remove authorizations";
                log.error(errorMsg);
                ServletUtils.handleFailure(out, errorMsg, methodName);
                return;
            }

            HashMap<String, Object> rmiResult = new HashMap<String, Object>();
            rmiResult = ServletUtils.manageAaaObject(rmiClient, methodName, log, out, rmiParams);
        } catch (RemoteException e) {
            return;
        }

        Map<String, Object> outputMap = new HashMap<String, Object>();
        outputMap.put("status", "authorization deleted");
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("{}&&" + jsonObject);
        log.debug("AuthorizationRemove.end");
    }

    public void
        doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }
}
